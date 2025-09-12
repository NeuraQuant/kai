// FILE: src/test/kotlin/kai/AgentTest.kt
package kai

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

public class AgentTest {
    
    @Test
    public fun `agent should store messages in memory`(): Unit = runTest {
        val fakeLlm = FakeLlmClient()
        val agent = Agent(llm = fakeLlm)
        
        agent.chat("Hello")
        
        val recent = agent.memory.recent()
        assertEquals(2, recent.size) // User + Assistant (System is not stored in memory)
        assertEquals(Msg.Role.User, recent[0].role)
        assertEquals(Msg.Role.Assistant, recent[1].role)
    }
    
    @Test
    public fun `agent should use tools`(): Unit = runTest {
        val fakeLlm = FakeLlmClient()
        val agent = Agent(llm = fakeLlm).use(timeNowTool)
        
        val result = agent.chat("What time is it?")
        
        assertNotNull(result.text)
        assertEquals(1, agent.tools.size)
        assertEquals("time.now", agent.tools[0].name)
    }
    
    @Test
    public fun `agent should remember notes`(): Unit = runTest {
        val fakeLlm = FakeLlmClient()
        val agent = Agent(llm = fakeLlm)
        
        agent.remember("User prefers short answers")
        
        val recent = agent.memory.recent()
        assertEquals(1, recent.size)
        assertEquals(Msg.Role.System, recent[0].role)
        assertEquals("User prefers short answers", recent[0].content)
    }
}

public class FakeLlmClient : LlmClient {
    public override suspend fun chat(
        messages: List<Msg>,
        tools: List<ToolSpec>,
        params: GenParams
    ): LlmResult {
        return LlmResult(
            text = "Hello! I'm a fake LLM response.",
            usage = LlmResult.Usage(10, 5, 15)
        )
    }
}