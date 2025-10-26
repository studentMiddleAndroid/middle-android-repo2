import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.yandex.praktikumchatapp.presentation.ChatViewModel
import ru.yandex.praktikumchatapp.presentation.Message

@ExperimentalCoroutinesApi
class ChatViewModelTest {

    private var testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChatViewModel(isWithReplies = false)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `send message should update state with MyMessage`() = runTest {
        val testMessage = "TestMessage"

        viewModel.sendMyMessage(testMessage)

        val state = viewModel.chatState.value
        val messages = state.messages

        assertTrue("Messages list should not be empty", messages.isNotEmpty())

        val lastMessage = messages.last()

        when (lastMessage) {
            is Message.MyMessage -> {
                assertEquals("Message text should match", testMessage, lastMessage.text)
            }
            is Message.OtherMessage -> {
                fail("Message should be MyMessage, but was OtherMessage")
            }
        }
    }

    @Test
    fun testReceiveMessage_concurrentMessages() = runTest {
        val messagesToSend = (1..100).map { Message.MyMessage("Message $it") }

        coroutineScope {
            val jobs = messagesToSend.map { message ->
                launch {
                    viewModel.sendMyMessage(message.text)
                }
            }

            jobs.joinAll()
        }


        val state = viewModel.chatState.value
        val finalMessages = state.messages

        assertEquals("Should contain all 100 messages", 100, finalMessages.size)

        assertTrue("All messages should be MyMessage",
            finalMessages.all { it is Message.MyMessage })

        val expectedTexts = messagesToSend.map { it.text }.toSet()
        val actualTexts = finalMessages
            .filterIsInstance<Message.MyMessage>()
            .map { it.text }
            .toSet()

        assertEquals("All message texts should be present",
            expectedTexts, actualTexts)

        assertEquals("No duplicate messages should exist",
            finalMessages.size, finalMessages.distinct().size)
    }
}