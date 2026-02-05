package dev.agentos.launcher.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import dev.agentos.launcher.ui.chat.ChatScreen
import dev.agentos.launcher.ui.chat.ChatViewModel

/**
 * Root composable for the Agent OS app.
 */
@Composable
fun AgentApp(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        ChatScreen(
            uiState = uiState,
            onSendMessage = viewModel::sendMessage,
            onStartVoiceInput = viewModel::startVoiceInput,
            onStopVoiceInput = viewModel::stopVoiceInput,
            onUiInteraction = viewModel::handleUiInteraction,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
