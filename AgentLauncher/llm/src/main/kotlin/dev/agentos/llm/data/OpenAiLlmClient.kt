package dev.agentos.llm.data

import dev.agentos.core.common.AgentLogger
import dev.agentos.core.common.DispatcherProvider
import dev.agentos.core.common.Result
import dev.agentos.core.domain.model.InputSource
import dev.agentos.core.domain.model.Task
import dev.agentos.core.domain.model.TaskContext
import dev.agentos.llm.data.api.*
import dev.agentos.llm.domain.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OpenAI implementation of LlmClient.
 */
@Singleton
class OpenAiLlmClient @Inject constructor(
    private val api: OpenAiApi,
    private val taskParser: TaskParser,
    private val dispatchers: DispatcherProvider,
    private val apiKeyProvider: ApiKeyProvider
) : LlmClient {

    override suspend fun complete(prompt: String): Result<String> = withContext(dispatchers.io) {
        try {
            val response = api.chatCompletion(
                authorization = "Bearer ${apiKeyProvider.openAiKey}",
                request = ChatCompletionRequest(
                    messages = listOf(
                        OpenAiMessage(role = "user", content = prompt)
                    )
                )
            )
            
            val content = response.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.error("Empty response from OpenAI")
            
            AgentLogger.logLlmResponse(content)
            Result.success(content)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_LLM, "OpenAI API error", e)
            Result.error(e)
        }
    }

    override suspend fun chat(messages: List<ChatMessage>): Result<String> = 
        withContext(dispatchers.io) {
            try {
                val openAiMessages = messages.map { msg ->
                    OpenAiMessage(
                        role = when (msg.role) {
                            MessageRole.SYSTEM -> "system"
                            MessageRole.USER -> "user"
                            MessageRole.ASSISTANT -> "assistant"
                        },
                        content = msg.content
                    )
                }
                
                val response = api.chatCompletion(
                    authorization = "Bearer ${apiKeyProvider.openAiKey}",
                    request = ChatCompletionRequest(messages = openAiMessages)
                )
                
                val content = response.choices.firstOrNull()?.message?.content
                    ?: return@withContext Result.error("Empty response from OpenAI")
                
                AgentLogger.logLlmResponse(content)
                Result.success(content)
            } catch (e: Exception) {
                AgentLogger.e(AgentLogger.TAG_LLM, "OpenAI chat error", e)
                Result.error(e)
            }
        }

    override suspend fun parseIntent(
        userInput: String,
        context: IntentContext
    ): Result<Task> = withContext(dispatchers.io) {
        AgentLogger.logLlmRequest(userInput)
        
        try {
            val messages = listOf(
                OpenAiMessage(
                    role = "system",
                    content = PromptTemplates.INTENT_PARSING_SYSTEM
                ),
                OpenAiMessage(
                    role = "user",
                    content = PromptTemplates.intentParsingUserPrompt(userInput, context)
                )
            )
            
            val response = api.chatCompletion(
                authorization = "Bearer ${apiKeyProvider.openAiKey}",
                request = ChatCompletionRequest(
                    messages = messages,
                    responseFormat = ResponseFormat(type = "json_object"),
                    temperature = 0.3f
                )
            )
            
            val content = response.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.error("Empty response from OpenAI")
            
            AgentLogger.logLlmResponse(content)
            
            val taskContext = TaskContext(
                source = InputSource.TEXT,
                rawInput = userInput
            )
            
            taskParser.parse(content, taskContext)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_LLM, "Intent parsing error", e)
            Result.error(e)
        }
    }

    override suspend fun generateSlotQuestion(
        task: Task,
        slotName: String
    ): Result<String> = withContext(dispatchers.io) {
        try {
            val slot = task.slots[slotName]
                ?: return@withContext Result.error("Slot not found: $slotName")
            
            val prompt = PromptTemplates.slotQuestionPrompt(
                taskDescription = "Intent: ${task.intent.fullName}",
                slotName = slotName,
                slotType = slot.type.name
            )
            
            complete(prompt)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun generateConfirmation(task: Task): Result<String> = 
        withContext(dispatchers.io) {
            try {
                val summary = buildString {
                    append("${task.intent.fullName}: ")
                    task.slots.filter { it.value.resolved }.forEach { (name, slot) ->
                        append("$name=${slot.value}, ")
                    }
                }
                
                val prompt = PromptTemplates.confirmationPrompt(summary)
                complete(prompt)
            } catch (e: Exception) {
                Result.error(e)
            }
        }

    override suspend fun generateCompletionMessage(task: Task): Result<String> = 
        withContext(dispatchers.io) {
            try {
                val summary = buildString {
                    append("${task.intent.fullName}: ")
                    task.slots.filter { it.value.resolved }.forEach { (name, slot) ->
                        append("$name=${slot.value}, ")
                    }
                }
                
                val prompt = PromptTemplates.completionPrompt(
                    taskSummary = summary,
                    success = task.result?.success ?: false
                )
                complete(prompt)
            } catch (e: Exception) {
                Result.error(e)
            }
        }
}

/**
 * Provides API keys for LLM services.
 */
interface ApiKeyProvider {
    val openAiKey: String
    val anthropicKey: String
}
