// FILE: src/test/kotlin/kai/VerificationTest.kt
package kai

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

public class VerificationTest {
    
    @Test
    public fun `verify complete library functionality`(): Unit = runTest {
        // Test 1: Create agent with mock LM Studio client
        val mockLlm = MockLmStudioClient()
        val agent = Agent(
            name = "verification-agent",
            system = "You are a verification assistant.",
            llm = mockLlm
        )
        
        // Test 2: Basic chat functionality
        val result1 = agent.chat("Hello")
        assertNotNull(result1.text)
        assertTrue(result1.text.isNotEmpty())
        
        // Test 3: Memory functionality
        val recent1 = agent.memory.recent()
        assertEquals(2, recent1.size) // User + Assistant
        assertEquals(Msg.Role.User, recent1[0].role)
        assertEquals(Msg.Role.Assistant, recent1[1].role)
        
        // Test 4: Tool integration
        val agentWithTools = agent.use(timeNowTool, httpGetTool)
        assertEquals(2, agentWithTools.tools.size)
        
        // Test 5: Remember functionality
        agent.remember("User prefers detailed answers")
        val recent2 = agent.memory.recent()
        assertTrue(recent2.any { it.content.contains("detailed answers") })
        
        // Test 6: Reply extension function
        val replyResult = agent.reply("Test reply")
        assertNotNull(replyResult)
        assertTrue(replyResult.isNotEmpty())
        
        // Test 7: Memory limits
        val limitedMemory = InMemoryMemory(maxMessages = 2)
        val agentWithLimitedMemory = Agent(llm = mockLlm, memory = limitedMemory)
        agentWithLimitedMemory.chat("Message 1")
        agentWithLimitedMemory.chat("Message 2")
        agentWithLimitedMemory.chat("Message 3")
        
        val recent3 = agentWithLimitedMemory.memory.recent()
        assertEquals(2, recent3.size) // Should be limited to 2
        
        // Test 8: Tool creation helper
        val customTool = tool(
            name = "test.custom",
            description = "Custom test tool",
            schema = """{"type":"object"}"""
        ) { _, _ -> "Custom result" }
        
        assertEquals("test.custom", customTool.name)
        assertEquals("Custom test tool", customTool.description)
        
        // Test 9: Built-in tools
        val timeResult = timeNowTool.invoke("{}", AgentContext(agent))
        assertTrue(timeResult.contains("T")) // ISO timestamp format
        
        // Test 10: Agent context
        val context = AgentContext(agent)
        assertEquals(agent, context.agent)
        assertNotNull(context.scratch)
        
        println("✅ All library functionality verified successfully!")
    }
}