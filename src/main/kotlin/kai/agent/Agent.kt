package kai.agent

import kai.client.LlmClient
import kai.model.Message
import kai.tool.Tool

/**
 * Central Agent class for managing AI conversations and tools
 * 
 * Example usage:
 * ```kotlin
 * val agent = Agent(LmStudioClient())
 * val response = agent.chat("Hello!")
 * ```
 */
class Agent(
    val llmClient: LlmClient,
    var systemPrompt: String = "",
    val memory: MutableList<Message> = mutableListOf(),
    val tools: List<Tool> = emptyList()
) {
    
    /**
     * Send a message to the agent and get a response
     * Automatically manages conversation memory
     */
    fun chat(
        message: String,
        temperature: Double = 0.7,
        maxTokens: Int = -1
    ): String {
        // Add user message to memory
        val userMessage = Message("user", message)
        memory.add(userMessage)
        
        // Build full context with system prompt and tools
        val fullMessages = buildContext()
        
        // Get response from LLM
        val response = llmClient.chat(fullMessages, temperature, maxTokens)
        
        // Process potential tool calls in response
        val finalResponse = processToolCalls(response.content)
        
        // Add assistant response to memory
        memory.add(Message("assistant", finalResponse))
        
        return finalResponse
    }
    
    /**
     * Clear conversation memory
     */
    fun clearMemory() {
        memory.clear()
    }
    
    /**
     * Get a formatted string of the conversation history
     */
    fun getHistory(): String {
        return memory.joinToString("\n\n") { msg ->
            "${msg.role.uppercase()}: ${msg.content}"
        }
    }
    
    /**
     * Build full message context including system prompt and tools
     */
    private fun buildContext(): List<Message> {
        val messages = mutableListOf<Message>()
        
        // Add system prompt with tools if present
        val systemContent = buildSystemContent()
        if (systemContent.isNotEmpty()) {
            messages.add(Message("system", systemContent))
        }
        
        // Add conversation memory
        messages.addAll(memory)
        
        return messages
    }
    
    /**
     * Build system content including prompt and tool descriptions
     */
    private fun buildSystemContent(): String {
        val parts = mutableListOf<String>()
        
        if (systemPrompt.isNotEmpty()) {
            parts.add(systemPrompt)
        }
        
        if (tools.isNotEmpty()) {
            val toolsDescription = buildString {
                appendLine("\nAvailable tools:")
                tools.forEach { tool ->
                    appendLine("- ${tool.name}: ${tool.description}")
                }
                appendLine("\nTo use a tool, respond with: TOOL:toolname:input")
                appendLine("Example: TOOL:calculator:2+2")
            }
            parts.add(toolsDescription)
        }
        
        return parts.joinToString("\n\n")
    }
    
    /**
     * Process tool calls in the response if present
     */
    private fun processToolCalls(response: String): String {
        if (!response.startsWith("TOOL:")) {
            return response
        }
        
        // Parse tool call format: TOOL:toolname:input
        val parts = response.substring(5).split(":", limit = 2)
        if (parts.size != 2) {
            return response
        }
        
        val toolName = parts[0].trim()
        val toolInput = parts[1].trim()
        
        val tool = tools.find { it.name.equals(toolName, ignoreCase = true) }
        
        return if (tool != null) {
            try {
                tool.execute(toolInput)
            } catch (e: Exception) {
                "Error executing tool '$toolName': ${e.message}"
            }
        } else {
            "Tool '$toolName' not found. Available tools: ${tools.joinToString { it.name }}"
        }
    }
    
    /**
     * Execute a tool directly without going through the LLM
     */
    fun executeTool(toolName: String, input: String): String {
        val tool = tools.find { it.name.equals(toolName, ignoreCase = true) }
        return tool?.execute(input) ?: "Tool '$toolName' not found"
    }
    
    /**
     * Get list of available tool names
     */
    fun getToolNames(): List<String> = tools.map { it.name }
}