package ru.yandex.praktikumchatapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.yandex.praktikumchatapp.data.ChatRepository

data class ChatState(
    val messages: List<Message> = emptyList(),
    val shouldShowKeyboard: Boolean = false
)

class ChatViewModel(
    val isWithReplies: Boolean = true
) : ViewModel() {
    private val repository = ChatRepository()
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    init {
        viewModelScope.launch {
            while (isWithReplies) {
                repository.getReplyMessage().collect { response ->
                    _chatState.update { currentState ->
                        val newMessages = currentState.messages + Message.OtherMessage(response)
                        val shouldShowKeyboard = currentState.messages.isEmpty() && newMessages.isNotEmpty()

                        currentState.copy(
                            messages = newMessages,
                            shouldShowKeyboard = shouldShowKeyboard
                        )
                    }
                }
            }
        }
    }

    fun sendMyMessage(messageText: String) {
        if (messageText.isNotBlank()) {
            _chatState.update { currentState ->
                currentState.copy(
                    messages = currentState.messages + Message.MyMessage(messageText.trim())
                )
            }
        }
    }

    fun resetKeyboardState() {
        _chatState.update { currentState ->
            currentState.copy(shouldShowKeyboard = false)
        }
    }
}