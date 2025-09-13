import kai.agent.Agent
import kai.agent.agent
import kai.client.LlmClient
import kai.model.Message
import kai.model.Response
import kai.tool.CalculatorTool
import kai.tool.DateTimeTool
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockLlmClient : LlmClient {
    override fun chat(
        messages: List<Message>,
        temperature: Double,
        maxTokens: Int
    ): Response {
        val lastMessage = messages.lastOrNull()?.content ?: "Hello"
        return Response(
            content = "Mock response to: $lastMessage",
            usage = mapOf("tokens" to 10)
        )
    }
}

class KaiLibraryTest {
    
    @Test
    fun testBasicAgent() {
        val agent = Agent(MockLlmClient())
        agent.systemPrompt = "You are a test assistant."
        
        val response = agent.chat("Hello!")
        assertTrue(response.contains("Mock response"))
        assertEquals(2, agent.memory.size) // user + assistant message
    }
    
    @Test
    fun testMemoryManagement() {
        val agent = Agent(MockLlmClient())
        
        agent.chat("First message")
        agent.chat("Second message")
        
        assertEquals(4, agent.memory.size)
        assertEquals("user", agent.memory[0].role)
        assertEquals("assistant", agent.memory[1].role)
        
        agent.clearMemory()
        assertEquals(0, agent.memory.size)
    }
    
    @Test
    fun testCalculatorTool() {
        val calc = CalculatorTool()
        val result = calc.execute("2 + 2")
        assertTrue(result.contains("4"))
    }
    
    @Test
    fun testDateTimeTool() {
        val datetime = DateTimeTool()
        val result = datetime.execute("now")
        assertTrue(result.contains("Current"))
    }
    
    @Test
    fun testAgentBuilder() {
        val agent = agent {
            withClient(MockLlmClient())
            withSystemPrompt("Test prompt")
            withTool(CalculatorTool())
            withTool(DateTimeTool())
        }
        
        assertEquals("Test prompt", agent.systemPrompt)
        assertEquals(2, agent.tools.size)
        assertTrue(agent.getToolNames().contains("calculator"))
    }
    
    @Test
    fun testDirectToolExecution() {
        val agent = Agent(
            llmClient = MockLlmClient(),
            tools = listOf(CalculatorTool())
        )
        
        val result = agent.executeTool("calculator", "5 * 5")
        assertTrue(result.contains("25"))
        
        val notFound = agent.executeTool("nonexistent", "test")
        assertTrue(notFound.contains("not found"))
    }
    
    @Test
    fun testDirectPropertyAccess() {
        val agent = Agent(MockLlmClient())
        
        // Direct system prompt modification
        agent.systemPrompt = "New prompt"
        assertEquals("New prompt", agent.systemPrompt)
        
        // Direct memory manipulation
        agent.memory.add(Message("system", "Test context"))
        assertEquals(1, agent.memory.size)
        assertEquals("system", agent.memory[0].role)
    }
}