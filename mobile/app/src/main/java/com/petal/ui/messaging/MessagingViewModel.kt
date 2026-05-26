package com.petal.ui.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.petal.data.messaging.ConversationResponse
import com.petal.data.messaging.MessageResponse
import com.petal.data.messaging.MessagingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MessagingUiState(
    val conversations: List<ConversationResponse> = emptyList(),
    val messages: List<MessageResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentConversationId: Long? = null
)

class MessagingViewModel(
    private val repository: MessagingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagingUiState())
    val uiState: StateFlow<MessagingUiState> = _uiState.asStateFlow()

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getConversations()
            result.onSuccess { conversations ->
                _uiState.update { it.copy(isLoading = false, conversations = conversations) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
    
    fun startConversation(orderId: Long, onConversationStarted: (Long) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.startConversation(orderId)
            result.onSuccess { conversation ->
                _uiState.update { it.copy(isLoading = false) }
                onConversationStarted(conversation.id)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    private var isPolling = false

    fun startPollingMessages(conversationId: Long) {
        _uiState.update { it.copy(currentConversationId = conversationId, error = null) }
        isPolling = true
        viewModelScope.launch {
            while (isPolling && _uiState.value.currentConversationId == conversationId) {
                val result = repository.getMessages(conversationId)
                result.onSuccess { msgs ->
                    _uiState.update { state -> state.copy(messages = msgs) }
                }
                delay(10_000)
            }
        }
    }

    fun stopPollingMessages() {
        isPolling = false
        _uiState.update { it.copy(currentConversationId = null) }
    }

    fun sendMessage(conversationId: Long, content: String) {
        viewModelScope.launch {
            val result = repository.sendMessage(conversationId, content)
            result.onSuccess { msg ->
                _uiState.update { state -> 
                    state.copy(messages = state.messages + msg)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }
}

class MessagingViewModelFactory(
    private val repository: MessagingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessagingViewModel(repository) as T
    }
}
