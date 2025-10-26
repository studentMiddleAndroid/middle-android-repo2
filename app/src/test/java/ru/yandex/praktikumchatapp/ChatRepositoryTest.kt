package ru.yandex.praktikumchatapp

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import ru.yandex.praktikumchatapp.data.ChatApi
import ru.yandex.praktikumchatapp.data.ChatRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryTest {
    private var testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var chatRepository: ChatRepository
    private val chatApi: ChatApi = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        chatRepository = ChatRepository(chatApi)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getReplyMessage should return a non-empty string`() = runTest {
        val expectedReply = "Test successful reply"
        `when`(chatApi.getReply()).thenReturn(flow { emit(expectedReply) })

        chatRepository.getReplyMessage().test {
            val actualReply = awaitItem()
            assertThat(actualReply, equalTo(expectedReply))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getReplyMessage should retry on error then successfully return string`() = runTest {
        val replyText = "Hello"
        var isException = true

        `when`(chatApi.getReply())
            .thenReturn(
                flow {
                    if (isException) {
                        isException = false
                        throw Exception("test exception")
                    }
                    emit(replyText)
                }
            )

        chatRepository.getReplyMessage().test {
            assert(awaitItem() == replyText)
            cancelAndIgnoreRemainingEvents()
        }
    }
}