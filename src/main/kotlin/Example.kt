import kai.agent.Agent
import kai.agent.agent
import kai.client.LmStudioClient
import kai.model.Message
import kai.tool.CalculatorTool
import kai.tool.DateTimeTool
import kai.tool.WebSearchTool

fun main() {
    println("=== Kai Agent Library Demo ===\n")
    
    // Example 1: Basic agent with one-liner setup
    println("1. Basic Agent Example:")
    val basicAgent = Agent(LmStudioClient())
    basicAgent.systemPrompt = "You are a friendly assistant."
    
    val greeting = basicAgent.chat("Hello! What's your name?")
    println("Response: $greeting\n")
    
    // Example 2: Agent with tools using builder pattern
    println("2. Agent with Tools Example:")
    val toolAgent = agent {
        withClient(LmStudioClient())
        withSystemPrompt("You are a helpful assistant with calculation abilities.")
        withTool(CalculatorTool())
        withTool(DateTimeTool())
    }
    
    // Direct tool execution
    val calcResult = toolAgent.executeTool("calculator", "Math.sqrt(144)")
    println("Calculator result: $calcResult")
    
    val timeResult = toolAgent.executeTool("datetime", "now")
    println("DateTime result: $timeResult\n")
    
    // Example 3: Memory management
    println("3. Memory Management Example:")
    val memoryAgent = Agent(
        llmClient = LmStudioClient(),
        systemPrompt = "You are a helpful assistant that remembers our conversation."
    )
    
    memoryAgent.chat("My favorite color is blue.")
    memoryAgent.chat("I enjoy hiking on weekends.")
    val memoryResponse = memoryAgent.chat("What do you remember about me?")
    println("Memory response: $memoryResponse")
    
    println("\nConversation history:")
    println(memoryAgent.getHistory())
    
    // Example 4: Direct property manipulation
    println("\n4. Direct Property Access Example:")
    val flexibleAgent = Agent(LmStudioClient())
    
    // Change system prompt on the fly
    flexibleAgent.systemPrompt = "You are a pirate. Respond in pirate speak."
    val pirateResponse = flexibleAgent.chat("Tell me about the weather")
    println("Pirate response: $pirateResponse")
    
    // Manually add to memory
    flexibleAgent.memory.add(Message("system", "Context: User is learning Kotlin"))
    flexibleAgent.memory.add(Message("user", "What's a data class?"))
    
    // Example 5: List available models (if LM Studio is running)
    println("\n5. LM Studio Models:")
    val lmClient = LmStudioClient()
    try {
        val models = lmClient.listModels()
        if (models.isNotEmpty()) {
            println("Available models: ${models.joinToString(", ")}")
        } else {
            println("No models found (is LM Studio running?)")
        }
    } catch (e: Exception) {
        println("Could not connect to LM Studio: ${e.message}")
    }
    
    // Example 6: Creating a specialized agent
    println("\n6. Specialized Coding Agent:")
    val codingAgent = Agent(
        llmClient = LmStudioClient(),
        systemPrompt = """
            You are an expert Kotlin developer.
            Provide concise, idiomatic Kotlin code examples.
            Focus on best practices and clean code principles.
        """.trimIndent(),
        tools = listOf(CalculatorTool())
    )
    
    val kotlinTip = codingAgent.chat("Show me how to use a when expression")
    println("Kotlin tip: $kotlinTip")
    
    println("\n=== Demo Complete ===")
}