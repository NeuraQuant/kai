// FILE: src/main/kotlin/kai/LmStudioClient.kt
package kai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

public class LmStudioClient(
    private val model: String,
    private val baseUrl: String = "http://localhost:1234/v1",
    private val apiKey: String? = null,
    private val http: HttpClient = defaultHttp()
) : LlmClient {
    
    public override suspend fun chat(
        messages: List<Msg>,
        tools: List<ToolSpec>,
        params: GenParams
    ): LlmResult {
        val request = ChatRequest(
            model = model,
            messages = messages.map { it.toOpenAI() },
            temperature = params.temperature,
            topP = params.topP,
            stop = params.stop,
            maxTokens = params.maxTokens,
            tools = if (tools.isNotEmpty()) tools.map { it.toOpenAI() } else null
        )
        
        return retry(maxRetries = 2) {
            val response = http.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                apiKey?.let { header("Authorization", "Bearer $it") }
                setBody(request)
            }
            
            val responseBody = response.body<String>()
            val json = Json.parseToJsonElement(responseBody).jsonObject
            
            val choices = json["choices"]?.jsonObject
            val choice = choices?.values?.firstOrNull()?.jsonObject
            val message = choice?.get("message")?.jsonObject
            
            val content = message?.get("content")?.jsonPrimitive?.content
            val toolCalls = message?.get("tool_calls")
            
            val usage = json["usage"]?.jsonObject?.let { usageJson ->
                LlmResult.Usage(
                    promptTokens = usageJson["prompt_tokens"]?.jsonPrimitive?.content?.toIntOrNull(),
                    completionTokens = usageJson["completion_tokens"]?.jsonPrimitive?.content?.toIntOrNull(),
                    totalTokens = usageJson["total_tokens"]?.jsonPrimitive?.content?.toIntOrNull()
                )
            }
            
            LlmResult(
                text = content ?: "",
                usage = usage,
                raw = responseBody
            )
        }
    }
    
    private suspend fun retry(maxRetries: Int, block: suspend () -> LlmResult): LlmResult {
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    delay(1000L * (attempt + 1))
                }
            }
        }
        
        throw lastException ?: RuntimeException("Max retries exceeded")
    }
    
    public companion object {
        public fun defaultHttp(): HttpClient = HttpClient(Java) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
            }
        }
    }
}

@Serializable
private data class ChatRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val temperature: Double? = null,
    val topP: Double? = null,
    val stop: List<String>? = null,
    val maxTokens: Int? = null,
    val tools: List<OpenAITool>? = null
)

@Serializable
private data class OpenAIMessage(
    val role: String,
    val content: String? = null,
    val toolCalls: List<OpenAIToolCall>? = null
)

@Serializable
private data class OpenAIToolCall(
    val id: String,
    val type: String = "function",
    val function: OpenAIToolCallFunction
)

@Serializable
private data class OpenAIToolCallFunction(
    val name: String,
    val arguments: String
)

@Serializable
private data class OpenAITool(
    val type: String = "function",
    val function: OpenAIToolFunction
)

@Serializable
private data class OpenAIToolFunction(
    val name: String,
    val description: String,
    val parameters: JsonObject
)

private fun Msg.toOpenAI(): OpenAIMessage = when (role) {
    Msg.Role.System -> OpenAIMessage(role = "system", content = content)
    Msg.Role.User -> OpenAIMessage(role = "user", content = content)
    Msg.Role.Assistant -> OpenAIMessage(role = "assistant", content = content)
    Msg.Role.Tool -> OpenAIMessage(role = "tool", content = content)
}

private fun ToolSpec.toOpenAI(): OpenAITool = OpenAITool(
    function = OpenAIToolFunction(
        name = name,
        description = description,
        parameters = Json.parseToJsonElement(jsonSchema).jsonObject
    )
)