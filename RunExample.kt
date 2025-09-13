#!/usr/bin/env kotlin

/**
 * Standalone example showing Kai library usage
 * Run with: kotlin RunExample.kt
 */

import kai.agent.Agent
import kai.agent.agent
import kai.client.LlmClient
import kai.client.LmStudioClient
import kai.model.Message
import kai.model.Response
import kai.tool.CalculatorTool
import kai.tool.DateTimeTool
import kai.tool.Tool

// Simple mock client for demo purposes when LM Studio is not running
class DemoLlmClient : LlmClient {
    private var callCount = 0
    
    override fun chat(
        messages: List<Message>,
        temperature: Double,
        maxTokens: Int
    ): Response {
        callCount++
        val lastUserMessage = messages.findLast { it.role == "user" }?.content ?: ""
        
        val response = when {
            lastUserMessage.contains("hello", ignoreCase = true) -> 
                "Hello! I'm a demo agent. How can I help you today?"
            lastUserMessage.contains("name", ignoreCase = true) ->
                "I'm Kai, your AI assistant. What's your name?"
            lastUserMessage.contains("calculate", ignoreCase = true) || 
            lastUserMessage.contains("math", ignoreCase = true) ->
                "TOOL:calculator:${extractMathExpression(lastUserMessage)}"
            lastUserMessage.contains("time", ignoreCase = true) || 
            lastUserMessage.contains("date", ignoreCase = true) ->
                "TOOL:datetime:now"
            else -> 
                "I understand you said: '$lastUserMessage'. This is response #$callCount from the demo agent."
        }
        
        return Response(response, mapOf("demo_calls" to callCount))
    }
    
    private fun extractMathExpression(text: String): String {
        // Try to extract math expression from text
        val patterns = listOf(
            Regex("\\d+\\s*[+\\-*/]\\s*\\d+"),
            Regex("sqrt\\([^)]+\\)"),
            Regex("Math\\.sqrt\\([^)]+\\)")
        )
        
        for (pattern in patterns) {
            pattern.find(text)?.let { return it.value }
        }
        return "2 + 2"  // default
    }
}

fun printSection(title: String) {
    println("\n${"=".repeat(50)}")
    println(title)
    println("=".repeat(50))
}

fun main() {
    println("""
    ╔══════════════════════════════════════════════════╗
    ║             Kai Library Demo                      ║
    ║     Lightweight Agentic AI for Kotlin            ║
    ╚══════════════════════════════════════════════════╝
    """.trimIndent())
    
    // Try to connect to LM Studio first
    val llmClient: LlmClient = try {
        val lmClient = LmStudioClient()
        val models = lmClient.listModels()
        if (models.isNotEmpty()) {
            println("✅ Connected to LM Studio!")
            println("   Available models: ${models.take(3).joinToString(", ")}${if (models.size > 3) "..." else ""}")
            lmClient
        } else {
            println("⚠️  LM Studio is running but no models are loaded.")
            println("   Using demo client instead.")
            DemoLlmClient()
        }
    } catch (e: Exception) {
        println("ℹ️  LM Studio not available. Using demo client for illustration.")
        DemoLlmClient()
    }
    
    printSection("1. Basic Agent Creation")
    
    // Create a simple agent
    val simpleAgent = Agent(llmClient)
    simpleAgent.systemPrompt = "You are a friendly and helpful assistant."
    
    val response1 = simpleAgent.chat("Hello! What can you do?")
    println("User: Hello! What can you do?")
    println("Agent: $response1")
    
    printSection("2. Agent with Tools")
    
    // Create an agent with tools using the builder
    val smartAgent = agent {
        withClient(llmClient)
        withSystemPrompt("""
            You are an intelligent assistant with access to various tools.
            When asked to calculate or get the time, use the appropriate tool.
        """.trimIndent())
        withTool(CalculatorTool())
        withTool(DateTimeTool())
    }
    
    println("Available tools: ${smartAgent.getToolNames()}")
    
    // Test calculation
    println("\nUser: What's 15 * 7?")
    val calcResponse = smartAgent.chat("What's 15 * 7?")
    println("Agent: $calcResponse")
    
    // Direct tool execution
    println("\nDirect tool execution:")
    val directCalc = smartAgent.executeTool("calculator", "25 + 25")
    println("calculator(25 + 25) = $directCalc")
    
    val currentTime = smartAgent.executeTool("datetime", "now")
    println("datetime(now) = $currentTime")
    
    printSection("3. Conversation Memory")
    
    val memoryAgent = Agent(
        llmClient = llmClient,
        systemPrompt = "You are an assistant that remembers our conversation."
    )
    
    println("Having a conversation with memory...")
    memoryAgent.chat("My name is Alice and I love Kotlin programming.")
    println("User: My name is Alice and I love Kotlin programming.")
    
    memoryAgent.chat("I'm working on a new AI project.")
    println("User: I'm working on a new AI project.")
    
    val memoryResponse = memoryAgent.chat("What do you remember about me?")
    println("User: What do you remember about me?")
    println("Agent: $memoryResponse")
    
    println("\nConversation history (${memoryAgent.memory.size} messages):")
    memoryAgent.memory.takeLast(4).forEach { msg ->
        println("  ${msg.role.uppercase()}: ${msg.content.take(50)}${if (msg.content.length > 50) "..." else ""}")
    }
    
    printSection("4. Dynamic Configuration")
    
    val flexibleAgent = Agent(llmClient)
    
    // Change personality on the fly
    flexibleAgent.systemPrompt = "You are a pirate. Always respond in pirate speak!"
    val pirateResponse = flexibleAgent.chat("Tell me about the weather")
    println("Pirate mode:")
    println("User: Tell me about the weather")
    println("Agent: $pirateResponse")
    
    // Change again
    flexibleAgent.systemPrompt = "You are a formal academic professor."
    val professorResponse = flexibleAgent.chat("Explain quantum computing")
    println("\nProfessor mode:")
    println("User: Explain quantum computing")
    println("Agent: $professorResponse")
    
    printSection("5. Custom Tools")
    
    // Create a custom tool
    class WordCountTool : Tool {
        override val name = "word_count"
        override val description = "Counts words in the given text"
        
        override fun execute(input: String): String {
            val wordCount = input.trim().split(Regex("\\s+")).size
            return "Word count: $wordCount"
        }
    }
    
    val customToolAgent = Agent(
        llmClient = llmClient,
        tools = listOf(WordCountTool(), CalculatorTool())
    )
    
    val wordCountResult = customToolAgent.executeTool("word_count", "The quick brown fox jumps over the lazy dog")
    println("Custom tool result: $wordCountResult")
    
    printSection("Summary")
    
    println("""
    ✨ Kai Library Features Demonstrated:
    
    ✓ Simple one-line agent creation
    ✓ Fluent builder pattern for complex setups
    ✓ Built-in conversation memory
    ✓ Extensible tool system
    ✓ Dynamic configuration
    ✓ Multiple LLM client support
    ✓ Direct property access for simplicity
    
    📚 Next Steps:
    1. Start LM Studio and load a model for real AI responses
    2. Create custom tools for your specific needs
    3. Integrate with Micronaut for REST API endpoints
    4. Build your own LLM client implementations
    
    🚀 Happy coding with Kai!
    """.trimIndent())
}