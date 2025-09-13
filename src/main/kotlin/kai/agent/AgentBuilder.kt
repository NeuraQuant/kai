package kai.agent

import kai.client.LlmClient
import kai.tool.Tool

/**
 * Fluent builder for creating agents (optional convenience)
 */
class AgentBuilder {
    private var llmClient: LlmClient? = null
    private var systemPrompt: String = ""
    private val tools = mutableListOf<Tool>()
    
    fun withClient(client: LlmClient) = apply { 
        this.llmClient = client 
    }
    
    fun withSystemPrompt(prompt: String) = apply { 
        this.systemPrompt = prompt 
    }
    
    fun withTool(tool: Tool) = apply { 
        this.tools.add(tool) 
    }
    
    fun withTools(vararg tools: Tool) = apply { 
        this.tools.addAll(tools) 
    }
    
    fun build(): Agent {
        requireNotNull(llmClient) { "LlmClient is required" }
        return Agent(
            llmClient = llmClient!!,
            systemPrompt = systemPrompt,
            tools = tools.toList()
        )
    }
}

/**
 * Extension function for convenient agent creation
 */
fun agent(block: AgentBuilder.() -> Unit): Agent {
    return AgentBuilder().apply(block).build()
}