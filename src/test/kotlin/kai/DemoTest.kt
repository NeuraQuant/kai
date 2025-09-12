// FILE: src/test/kotlin/kai/DemoTest.kt
package kai

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

public class DemoTest {
    
    @Test
    public fun `demonstrate complete Kai library usage`(): Unit = runTest {
        println("🚀 Kai Library Demonstration")
        println("=".repeat(50))
        
        // 1. Create a mock LM Studio client
        val mockLlm = MockLmStudioClient()
        println("✅ Created mock LM Studio client")
        
        // 2. Create an agent with custom configuration
        val agent = Agent(
            name = "demo-agent",
            system = "You are a helpful demonstration assistant.",
            llm = mockLlm
        )
        println("✅ Created agent: ${agent.name}")
        
        // 3. Test basic chat functionality
        val response1 = agent.chat("Hello! What's your name?")
        println("✅ Chat response: ${response1.text}")
        assertNotNull(response1.text)
        
        // 4. Test memory functionality
        val recentMessages = agent.memory.recent()
        println("✅ Memory contains ${recentMessages.size} messages")
        assertEquals(2, recentMessages.size) // User + Assistant
        
        // 5. Add tools to the agent
        val agentWithTools = agent.use(timeNowTool, httpGetTool)
        println("✅ Added ${agentWithTools.tools.size} tools to agent")
        assertEquals(2, agentWithTools.tools.size)
        
        // 6. Test tool-enabled conversation
        val response2 = agentWithTools.chat("What time is it?")
        println("✅ Tool-enabled response: ${response2.text}")
        
        // 7. Test remember functionality
        agent.remember("User prefers detailed explanations")
        val messagesAfterRemember = agent.memory.recent()
        val hasRememberedNote = messagesAfterRemember.any { it.content.contains("detailed explanations") }
        println("✅ Remembered note: $hasRememberedNote")
        assertTrue(hasRememberedNote)
        
        // 8. Test reply extension function
        val replyResponse = agent.reply("Can you summarize our conversation?")
        println("✅ Reply extension: ${replyResponse}")
        assertNotNull(replyResponse)
        
        // 9. Test memory limits
        val limitedMemory = InMemoryMemory(maxMessages = 3)
        val agentWithLimitedMemory = Agent(llm = mockLlm, memory = limitedMemory)
        
        agentWithLimitedMemory.chat("Message 1")
        agentWithLimitedMemory.chat("Message 2") 
        agentWithLimitedMemory.chat("Message 3")
        agentWithLimitedMemory.chat("Message 4") // This should push out Message 1
        
        val limitedRecent = agentWithLimitedMemory.memory.recent()
        println("✅ Memory limit working: ${limitedRecent.size} messages (max 3)")
        assertEquals(3, limitedRecent.size)
        
        // 10. Test built-in tools directly
        val timeResult = timeNowTool.invoke("{}", AgentContext(agent))
        println("✅ Time tool result: ${timeResult.take(20)}...")
        assertTrue(timeResult.contains("T")) // ISO format
        
        // 11. Test custom tool creation
        val customTool = tool(
            name = "demo.calculator",
            description = "A simple calculator tool",
            schema = """{"type":"object","properties":{"operation":{"type":"string"},"a":{"type":"number"},"b":{"type":"number"}},"required":["operation","a","b"]}"""
        ) { args, _ -> 
            val json = Json.parseToJsonElement(args).jsonObject
            val operation = json["operation"]?.jsonPrimitive?.content ?: "add"
            val a = json["a"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            val b = json["b"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            
            when (operation) {
                "add" -> (a + b).toString()
                "multiply" -> (a * b).toString()
                else -> "Unknown operation"
            }
        }
        
        val calcResult = customTool.invoke("""{"operation":"add","a":5,"b":3}""", AgentContext(agent))
        println("✅ Custom tool result: 5 + 3 = $calcResult")
        assertEquals("8.0", calcResult)
        
        // 12. Test agent context
        val context = AgentContext(agent)
        context.scratch["test"] = "value"
        println("✅ Agent context scratch: ${context.scratch["test"]}")
        assertEquals("value", context.scratch["test"])
        
        // 13. Test LLM result with usage information
        val resultWithUsage = agent.chat("Test message with usage tracking")
        println("✅ Usage tracking: ${resultWithUsage.usage}")
        assertNotNull(resultWithUsage.usage)
        
        println("=".repeat(50))
        println("🎉 All Kai library features demonstrated successfully!")
        println("📊 Test Summary:")
        println("   - Agent creation and configuration ✅")
        println("   - Chat functionality ✅")
        println("   - Memory management ✅")
        println("   - Tool integration ✅")
        println("   - Built-in tools (time.now, http.get) ✅")
        println("   - Custom tool creation ✅")
        println("   - Remember functionality ✅")
        println("   - Reply extension function ✅")
        println("   - Memory limits ✅")
        println("   - Agent context ✅")
        println("   - Usage tracking ✅")
        println("=".repeat(50))
    }
}
