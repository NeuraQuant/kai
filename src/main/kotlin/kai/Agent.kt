// FILE: src/main/kotlin/kai/Agent.kt
package kai

import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

public class Agent(
    public val name: String = "agent",
    public val system: String = "You are a helpful assistant.",
    public val llm: LlmClient,
    public val memory: MemoryStore = InMemoryMemory(),
    public val tools: MutableList<ToolSpec> = mutableListOf()
) {
    public suspend fun chat(message: String, params: GenParams = GenParams()): LlmResult {
        // Add user message to memory
        memory.add(Msg(Msg.Role.User, message))
        
        // Build conversation context
        val messages = buildList {
            // Always include system prompt first
            add(Msg(Msg.Role.System, system))
            
            // Include summary if available
            memory.summary?.let { summary ->
                add(Msg(Msg.Role.System, "Previous conversation summary: $summary"))
            }
            
            // Add recent messages
            addAll(memory.recent())
        }
        
        // Chat with LLM
        var result = llm.chat(messages, tools, params)
        
        // Handle tool calls if present
        var toolCallCount = 0
        val maxToolCalls = 3
        
        val rawResponse = result.raw
        while (toolCallCount < maxToolCalls && rawResponse != null) {
            val toolCalls = extractToolCalls(rawResponse)
            if (toolCalls.isEmpty()) break
            
            // Add assistant message with tool calls to memory
            memory.add(Msg(Msg.Role.Assistant, result.text))
            
            // Execute tool calls
            for (toolCall in toolCalls) {
                val tool = tools.find { it.name == toolCall.name }
                if (tool != null) {
                    try {
                        val toolResult = tool.invoke(toolCall.arguments, AgentContext(this))
                        memory.add(Msg(Msg.Role.Tool, toolResult))
                    } catch (e: Exception) {
                        memory.add(Msg(Msg.Role.Tool, "Error: ${e.message}"))
                    }
                } else {
                    memory.add(Msg(Msg.Role.Tool, "Tool '${toolCall.name}' not found"))
                }
            }
            
            // Continue conversation with tool results
            val updatedMessages = buildList {
                add(Msg(Msg.Role.System, system))
                memory.summary?.let { summary ->
                    add(Msg(Msg.Role.System, "Previous conversation summary: $summary"))
                }
                addAll(memory.recent())
            }
            
            result = llm.chat(updatedMessages, tools, params)
            toolCallCount++
        }
        
        // Add final assistant response to memory
        memory.add(Msg(Msg.Role.Assistant, result.text))
        
        return result
    }
    
    public fun use(vararg tool: ToolSpec): Agent = apply { tools += tool }
    
    public fun remember(note: String) {
        memory.add(Msg(Msg.Role.System, note))
    }
    
    private fun extractToolCalls(rawResponse: String): List<ToolCall> {
        return try {
            val json = Json.parseToJsonElement(rawResponse).jsonObject
            val choices = json["choices"]?.jsonObject
            val choice = choices?.values?.firstOrNull()?.jsonObject
            val message = choice?.get("message")?.jsonObject
            val toolCalls = message?.get("tool_calls")
            
            if (toolCalls != null) {
                // Parse tool calls from the response
                // This is a simplified implementation - in practice you'd need to handle the full structure
                emptyList() // For now, return empty list as tool calls parsing is complex
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

public data class ToolCall(
    public val name: String,
    public val arguments: String
)

public suspend fun Agent.reply(user: String): String = chat(user).text