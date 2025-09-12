// FILE: src/test/kotlin/kai/IntegrationTest.kt
package kai

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

public class IntegrationTest {
    
    @Test
    public fun `agent should work end-to-end with mock LM Studio`(): Unit = runTest {
        val mockLlm = MockLmStudioClient()
        val agent = Agent(
            name = "test-agent",
            system = "You are a helpful test assistant.",
            llm = mockLlm
        ).use(timeNowTool, httpGetTool)
        
        // Test basic chat
        val result1 = agent.chat("Hello, what's your name?")
        assertNotNull(result1.text)
        assertTrue(result1.text.isNotEmpty())
        
        // Test memory persistence
        val result2 = agent.chat("What did I just ask you?")
        assertNotNull(result2.text)
        
        // Test tool usage
        val result3 = agent.chat("What time is it?")
        assertNotNull(result3.text)
        
        // Verify memory contains conversation
        val recent = agent.memory.recent()
        assertTrue(recent.size >= 4) // At least 2 user + 2 assistant messages
        
        // Test remember functionality
        agent.remember("User likes cats")
        val recentAfterRemember = agent.memory.recent()
        assertTrue(recentAfterRemember.any { it.content.contains("cats") })
    }
    
    @Test
    public fun `agent should handle multiple tools`(): Unit = runTest {
        val mockLlm = MockLmStudioClient()
        val agent = Agent(llm = mockLlm).use(timeNowTool, httpGetTool)
        
        assertEquals(2, agent.tools.size)
        assertEquals("time.now", agent.tools[0].name)
        assertEquals("http.get", agent.tools[1].name)
        
        val result = agent.chat("Test message")
        assertNotNull(result.text)
    }
    
    @Test
    public fun `agent should handle memory limits`(): Unit = runTest {
        val mockLlm = MockLmStudioClient()
        val memory = InMemoryMemory(maxMessages = 3)
        val agent = Agent(llm = mockLlm, memory = memory)
        
        // Add more messages than the limit
        agent.chat("Message 1")
        agent.chat("Message 2")
        agent.chat("Message 3")
        agent.chat("Message 4")
        
        val recent = agent.memory.recent()
        assertEquals(3, recent.size) // Should be limited to 3
    }
}

public class MockLmStudioClient : LlmClient {
    private var callCount = 0
    
    public override suspend fun chat(
        messages: List<Msg>,
        tools: List<ToolSpec>,
        params: GenParams
    ): LlmResult {
        callCount++
        
        // Simulate different responses based on call count
        val response = when (callCount) {
            1 -> "Hello! I'm a test assistant. How can I help you?"
            2 -> "You asked me what my name is. I'm a test assistant."
            3 -> "The current time is 2024-01-01T12:00:00Z"
            else -> "I'm here to help with your questions."
        }
        
        return LlmResult(
            text = response,
            usage = LlmResult.Usage(
                promptTokens = 10 + messages.size * 5,
                completionTokens = response.length / 4,
                totalTokens = 10 + messages.size * 5 + response.length / 4
            ),
            raw = """{"choices":[{"message":{"content":"$response"}}],"usage":{"prompt_tokens":${10 + messages.size * 5},"completion_tokens":${response.length / 4},"total_tokens":${10 + messages.size * 5 + response.length / 4}}}"""
        )
    }
}