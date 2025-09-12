// FILE: src/test/kotlin/kai/BuiltInToolsTest.kt
package kai

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

public class BuiltInToolsTest {
    
    @Test
    public fun `time now tool should return valid ISO timestamp`(): Unit = runTest {
        val result = timeNowTool.invoke("{}", AgentContext(Agent(llm = MockLmStudioClient())))
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("T")) // ISO format should contain T
        assertTrue(result.contains(":")) // Should contain time separators
    }
    
    @Test
    public fun `http get tool should require url parameter`(): Unit = runTest {
        assertFailsWith<IllegalArgumentException> {
            httpGetTool.invoke("{}", AgentContext(Agent(llm = MockLmStudioClient())))
        }
    }
    
    @Test
    public fun `http get tool should reject non-http urls`(): Unit = runTest {
        assertFailsWith<IllegalArgumentException> {
            httpGetTool.invoke("""{"url":"ftp://example.com"}""", AgentContext(Agent(llm = MockLmStudioClient())))
        }
    }
    
    @Test
    public fun `http get tool should reject invalid json`(): Unit = runTest {
        assertFailsWith<Exception> {
            httpGetTool.invoke("invalid json", AgentContext(Agent(llm = MockLmStudioClient())))
        }
    }
    
    @Test
    public fun `tool helper should create valid tool spec`(): Unit = runTest {
        val customTool = tool(
            name = "test.tool",
            description = "A test tool",
            schema = """{"type":"object","properties":{"input":{"type":"string"}},"required":["input"]}"""
        ) { args, _ -> "Result: $args" }
        
        assertEquals("test.tool", customTool.name)
        assertEquals("A test tool", customTool.description)
        assertEquals("""{"type":"object","properties":{"input":{"type":"string"}},"required":["input"]}""", customTool.jsonSchema)
        
        // Test the tool invocation
        val result = customTool.invoke("""{"input":"test"}""", AgentContext(Agent(llm = MockLmStudioClient())))
        assertEquals("Result: {\"input\":\"test\"}", result)
    }
}