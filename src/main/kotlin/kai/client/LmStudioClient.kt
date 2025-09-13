package kai.client

import kai.model.Message
import kai.model.Response
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * LM Studio client implementation using OpenAI-compatible API
 */
class LmStudioClient(
    private val baseUrl: String = "http://localhost:1234",
    private val model: String? = null  // Optional model override
) : LlmClient {
    
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build()
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    override fun chat(
        messages: List<Message>,
        temperature: Double,
        maxTokens: Int
    ): Response {
        val requestBody = buildRequestBody(messages, temperature, maxTokens)
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofMinutes(5))
            .build()
        
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() != 200) {
            throw RuntimeException("LM Studio API error: ${response.statusCode()} - ${response.body()}")
        }
        
        return parseResponse(response.body())
    }
    
    private fun buildRequestBody(
        messages: List<Message>,
        temperature: Double,
        maxTokens: Int
    ): String {
        val jsonMessages = messages.map { msg ->
            buildJsonObject {
                put("role", msg.role)
                put("content", msg.content)
            }
        }
        
        val requestJson = buildJsonObject {
            model?.let { put("model", it) }
            put("messages", JsonArray(jsonMessages))
            put("temperature", temperature)
            if (maxTokens > 0) {
                put("max_tokens", maxTokens)
            }
            put("stream", false)
        }
        
        return requestJson.toString()
    }
    
    private fun parseResponse(responseBody: String): Response {
        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        
        val content = jsonResponse["choices"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject
            ?.get("content")?.jsonPrimitive?.content
            ?: throw RuntimeException("Invalid response format from LM Studio")
        
        val usage = jsonResponse["usage"]?.jsonObject?.let { usageObj ->
            usageObj.entries.associate { (key, value) ->
                key to when (value) {
                    is JsonPrimitive -> {
                        when {
                            value.isString -> value.content
                            else -> value.intOrNull ?: value.doubleOrNull ?: value.booleanOrNull ?: value.content
                        }
                    }
                    else -> value.toString()
                }
            }
        }
        
        return Response(content, usage)
    }
    
    /**
     * List available models in LM Studio
     */
    fun listModels(): List<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/v1/models"))
            .GET()
            .build()
        
        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                val jsonResponse = json.parseToJsonElement(response.body()).jsonObject
                jsonResponse["data"]?.jsonArray?.mapNotNull { 
                    it.jsonObject["id"]?.jsonPrimitive?.content 
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}