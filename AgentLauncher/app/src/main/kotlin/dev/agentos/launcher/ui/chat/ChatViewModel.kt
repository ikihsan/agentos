package dev.agentos.launcher.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.agentos.core.common.AgentLogger
import dev.agentos.core.common.Result
import dev.agentos.core.model.Task
import dev.agentos.core.model.TaskStatus
import dev.agentos.llm.domain.LlmClient
import dev.agentos.taskengine.domain.TaskEvent
import dev.agentos.taskengine.domain.TaskManager
import dev.agentos.uirenderer.domain.UiInteraction
import dev.agentos.voice.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the chat screen.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val taskManager: TaskManager,
    private val llmClient: LlmClient,
    private val voiceInputManager: VoiceInputManager,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeVoiceInput()
        observeTaskEvents()
    }

    private fun observeVoiceInput() {
        // Voice state
        viewModelScope.launch {
            voiceInputManager.state.collect { state ->
                _uiState.update { it.copy(voiceState = state) }
            }
        }

        // Speech recognition results
        viewModelScope.launch {
            voiceInputManager.recognitionResults.collect { result ->
                when (result) {
                    is SpeechResult.Partial -> {
                        _uiState.update { it.copy(partialSpeech = result.text) }
                    }
                    is SpeechResult.Final -> {
                        _uiState.update { it.copy(partialSpeech = null) }
                        handleUserInput(result.text, isVoice = true)
                    }
                    is SpeechResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                partialSpeech = null,
                                error = result.message
                            ) 
                        }
                    }
                }
            }
        }
    }

    private fun observeTaskEvents() {
        viewModelScope.launch {
            taskManager.events.collect { event ->
                handleTaskEvent(event)
            }
        }
    }

    private fun handleTaskEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.TaskCreated -> {
                _uiState.update { state ->
                    state.copy(currentTask = event.task)
                }
            }
            is TaskEvent.TaskUpdated -> {
                _uiState.update { state ->
                    state.copy(currentTask = event.task)
                }
                
                // Generate response based on status
                when (event.task.status) {
                    TaskStatus.NEEDS_INPUT -> generateSlotQuestion(event.task)
                    TaskStatus.READY -> generateConfirmation(event.task)
                    TaskStatus.COMPLETED -> generateCompletionMessage(event.task)
                    TaskStatus.FAILED -> showError(event.task)
                    else -> {}
                }
            }
            is TaskEvent.SlotUpdated -> {
                // Slot was filled, task manager will advance state
            }
            is TaskEvent.TaskCompleted -> {
                addAssistantMessage("Task completed successfully!")
                _uiState.update { it.copy(currentTask = null, dynamicUiComponents = emptyList()) }
            }
            is TaskEvent.TaskFailed -> {
                addAssistantMessage("Task failed: ${event.reason}")
                _uiState.update { it.copy(currentTask = null, dynamicUiComponents = emptyList()) }
            }
            is TaskEvent.TaskCancelled -> {
                addAssistantMessage("Task cancelled.")
                _uiState.update { it.copy(currentTask = null, dynamicUiComponents = emptyList()) }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        handleUserInput(text, isVoice = false)
    }

    fun startVoiceInput() {
        viewModelScope.launch {
            voiceInputManager.startListening()
        }
    }

    fun stopVoiceInput() {
        voiceInputManager.stopListening()
    }

    fun handleUiInteraction(interaction: UiInteraction) {
        viewModelScope.launch {
            val task = _uiState.value.currentTask ?: return@launch
            
            when (interaction) {
                is UiInteraction.TextInput -> {
                    taskManager.updateSlot(task.id, interaction.componentId, interaction.value)
                }
                is UiInteraction.Choice -> {
                    taskManager.updateSlot(task.id, interaction.componentId, interaction.selectedLabel)
                }
                is UiInteraction.MultiChoice -> {
                    taskManager.updateSlot(task.id, interaction.componentId, interaction.selectedIds)
                }
                is UiInteraction.Confirmation -> {
                    if (interaction.confirmed) {
                        taskManager.beginExecution(task.id)
                        executeTask(task)
                    } else {
                        taskManager.cancel(task.id, "User cancelled")
                    }
                }
                is UiInteraction.DateTimePicked -> {
                    taskManager.updateSlot(task.id, interaction.componentId, interaction.timestamp)
                }
                is UiInteraction.FormSubmitted -> {
                    interaction.values.forEach { (key, value) ->
                        if (value != null) {
                            taskManager.updateSlot(task.id, key, value)
                        }
                    }
                }
                else -> {
                    AgentLogger.w(AgentLogger.TAG_MAIN, "Unhandled interaction: $interaction")
                }
            }
        }
    }

    private fun handleUserInput(text: String, isVoice: Boolean) {
        // Add user message
        addUserMessage(text)
        
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            
            val currentTask = _uiState.value.currentTask
            
            if (currentTask != null && currentTask.status == TaskStatus.NEEDS_INPUT) {
                // Fill slot with user input
                val nextSlot = currentTask.slots.find { it.value == null }
                if (nextSlot != null) {
                    taskManager.updateSlot(currentTask.id, nextSlot.name, text)
                }
            } else {
                // Parse new intent
                when (val result = llmClient.parseIntent(text)) {
                    is Result.Success -> {
                        val task = result.data
                        taskManager.createTask(task)
                        AgentLogger.logTaskStateChange(task.id, "CREATED", task.status.name)
                    }
                    is Result.Error -> {
                        addAssistantMessage("I'm sorry, I didn't understand that. Could you try again?")
                        _uiState.update { it.copy(error = result.message) }
                    }
                    is Result.Loading -> {}
                }
            }
            
            _uiState.update { it.copy(isProcessing = false) }
        }
    }

    private fun generateSlotQuestion(task: Task) {
        viewModelScope.launch {
            val nextSlot = task.slots.find { it.value == null } ?: return@launch
            
            when (val result = llmClient.generateSlotQuestion(task, nextSlot.name)) {
                is Result.Success -> {
                    addAssistantMessage(result.data)
                    speakIfEnabled(result.data)
                }
                is Result.Error -> {
                    addAssistantMessage("What is the ${nextSlot.name}?")
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun generateConfirmation(task: Task) {
        viewModelScope.launch {
            when (val result = llmClient.generateConfirmation(task)) {
                is Result.Success -> {
                    addAssistantMessage(result.data)
                    speakIfEnabled(result.data)
                    // Show confirmation UI
                    showConfirmationUi(task)
                }
                is Result.Error -> {
                    addAssistantMessage("Ready to proceed?")
                    showConfirmationUi(task)
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun generateCompletionMessage(task: Task) {
        viewModelScope.launch {
            when (val result = llmClient.generateCompletionMessage(task)) {
                is Result.Success -> {
                    addAssistantMessage(result.data)
                    speakIfEnabled(result.data)
                }
                is Result.Error -> {
                    addAssistantMessage("Done!")
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun showError(task: Task) {
        val errorMessage = task.result?.error ?: "Something went wrong"
        addAssistantMessage("Error: $errorMessage")
    }

    private fun showConfirmationUi(task: Task) {
        val details = task.slots
            .filter { it.value != null }
            .associate { it.name to it.value.toString() }
        
        val confirmation = dev.agentos.core.model.ConfirmationComponent(
            id = "confirm_${task.id}",
            type = "confirmation",
            title = "Confirm ${task.intent.action}",
            message = "Please confirm the following action:",
            details = details,
            confirmLabel = "Confirm",
            cancelLabel = "Cancel",
            style = "default"
        )
        
        _uiState.update { it.copy(dynamicUiComponents = listOf(confirmation)) }
    }

    private fun executeTask(task: Task) {
        viewModelScope.launch {
            // TODO: Execute task via automation
            // For now, simulate completion
            kotlinx.coroutines.delay(1000)
            taskManager.complete(task.id, mapOf("status" to "success"))
        }
    }

    private fun speakIfEnabled(text: String) {
        viewModelScope.launch {
            ttsManager.speak(text)
        }
    }

    private fun addUserMessage(text: String) {
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = text,
            isUser = true,
            taskId = _uiState.value.currentTask?.id
        )
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }
    }

    private fun addAssistantMessage(text: String) {
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = text,
            isUser = false,
            taskId = _uiState.value.currentTask?.id
        )
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }
    }
}
