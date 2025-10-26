package ru.yandex.praktikumchatapp.data

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException

class ChatRepository(
    private val api: ChatApi = ChatApi()
) {
    companion object {
        private const val MAX_RETRIES = 5
        private const val BASE_DELAY_MS = 1000L
        private const val MAX_DELAY_MS = 10000L
    }

    fun getReplyMessage(): Flow<String> {
        return api.getReply()
            .retryWhen { cause, attempt ->
                if (cause is IOException && attempt <= MAX_RETRIES) {
                    val delayTime = BASE_DELAY_MS shl (attempt.toInt() - 1)
                    delay(minOf(delayTime, MAX_DELAY_MS))
                    true
                } else {
                    false
                }
            }
            .catch { exception ->
                Log.e("ChatRepository", "Не удалось получить ответ после повторных попыток", exception)

                when (exception) {
                    is IOException -> emit("Проблемы с соединением. Попробуйте позже.")
                    else -> emit("Неизвестная ошибка")
                }
            }
    }
}