package examples

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.*
import jakarta.inject.Singleton
import kai.agent.Agent
import kai.agent.agent
import kai.client.LmStudioClient
import kai.client.OpenAIClient
import kai.tool.CalculatorTool
import kai.tool.DateTimeTool
import kai.tool.WebSearchTool

/**
 * Micronaut Factory for creating Kai agents
 */
@Factory
class AgentFactory {
    
    @Bean
    @Singleton
    fun lmStudioAgent(
        @Value("\${kai.lmstudio.url:http://localhost:1234}") baseUrl: String,
        @Value("\${kai.lmstudio.model:}") model: String?
    ): Agent {
        val client = if (model.isNullOrEmpty()) {
            LmStudioClient(baseUrl)
        } else {
            LmStudioClient(baseUrl, model)
        }
        
        return agent {
            withClient(client)
            withSystemPrompt("You are a helpful API assistant integrated with Micronaut.")
            withTool(CalculatorTool())
            withTool(DateTimeTool())
            withTool(WebSearchTool())
        }
    }
    
    @Bean
    @Singleton
    @Requires(property = "kai.openai.api-key")
    fun openAIAgent(
        @Value("\${kai.openai.api-key}") apiKey: String,
        @Value("\${kai.openai.model:gpt-4}") model: String
    ): Agent {
        return Agent(
            llmClient = OpenAIClient(apiKey, model),
            systemPrompt = "You are a GPT-powered assistant in a Micronaut application."
        )
    }
}

/**
 * REST Controller for chat interactions
 */
@Controller("/api/chat")
class ChatController(private val agent: Agent) {
    
    @Post("/message")
    fun chat(@Body request: ChatRequest): ChatResponse {
        val response = agent.chat(
            message = request.message,
            temperature = request.temperature ?: 0.7,
            maxTokens = request.maxTokens ?: -1
        )
        
        return ChatResponse(
            response = response,
            conversationLength = agent.memory.size
        )
    }
    
    @Get("/history")
    fun getHistory(): HistoryResponse {
        return HistoryResponse(
            history = agent.getHistory(),
            messageCount = agent.memory.size
        )
    }
    
    @Delete("/history")
    fun clearHistory(): MessageResponse {
        agent.clearMemory()
        return MessageResponse("Conversation history cleared")
    }
    
    @Post("/tool/{toolName}")
    fun executeTool(
        @PathVariable toolName: String,
        @Body request: ToolRequest
    ): ToolResponse {
        val result = agent.executeTool(toolName, request.input)
        return ToolResponse(toolName, result)
    }
    
    @Get("/tools")
    fun listTools(): ToolListResponse {
        return ToolListResponse(agent.getToolNames())
    }
    
    @Put("/system-prompt")
    fun updateSystemPrompt(@Body request: SystemPromptRequest): MessageResponse {
        agent.systemPrompt = request.prompt
        return MessageResponse("System prompt updated")
    }
}

// Data classes for API
data class ChatRequest(
    val message: String,
    val temperature: Double? = null,
    val maxTokens: Int? = null
)

data class ChatResponse(
    val response: String,
    val conversationLength: Int
)

data class HistoryResponse(
    val history: String,
    val messageCount: Int
)

data class MessageResponse(
    val message: String
)

data class ToolRequest(
    val input: String
)

data class ToolResponse(
    val tool: String,
    val result: String
)

data class ToolListResponse(
    val tools: List<String>
)

data class SystemPromptRequest(
    val prompt: String
)

/**
 * WebSocket endpoint for streaming chat
 */
@ServerWebSocket("/ws/chat")
class ChatWebSocket(private val agent: Agent) {
    
    @OnMessage
    fun onMessage(message: String, session: WebSocketSession): String {
        return agent.chat(message)
    }
    
    @OnOpen
    fun onOpen(session: WebSocketSession) {
        session.send("Connected to Kai agent. Send a message to start chatting.")
    }
    
    @OnClose
    fun onClose(session: WebSocketSession) {
        // Optionally clear memory for this session
    }
}

/**
 * Application configuration in application.yml:
 * 
 * kai:
 *   lmstudio:
 *     url: http://localhost:1234
 *     model: qwen2.5-coder-7b  # optional
 *   openai:
 *     api-key: ${OPENAI_API_KEY}
 *     model: gpt-4
 */