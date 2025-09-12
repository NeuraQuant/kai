// FILE: src/test/kotlin/kai/ToolsTest.kt
package kai

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

public class ToolsTest {
    
    @Test
    public fun `time now tool should return current time`(): Unit = runTest {
        val result = timeNowTool.invoke("{}", AgentContext(Agent(llm = FakeLlmClient())))
        
        assert(result.isNotEmpty())
        assert(result.contains("T")) // ISO format should contain T
    }
    
    @Test
    public fun `http get tool should require url parameter`(): Unit = runTest {
        assertFailsWith<IllegalArgumentException> {
            httpGetTool.invoke("{}", AgentContext(Agent(llm = FakeLlmClient())))
        }
    }
    
    @Test
    public fun `http get tool should reject non-http urls`(): Unit = runTest {
        assertFailsWith<IllegalArgumentException> {
            httpGetTool.invoke("""{"url":"ftp://example.com"}""", AgentContext(Agent(llm = FakeLlmClient())))
        }
    }
    
    @Test
    public fun `tool helper should create tool spec`(): Unit {
        val tool = tool(
            name = "test",
            description = "Test tool",
            schema = """{"type":"object"}"""
        ) { _, _ -> "test result" }
        
        assertEquals("test", tool.name)
        assertEquals("Test tool", tool.description)
        assertEquals("""{"type":"object"}""", tool.jsonSchema)
    }
}