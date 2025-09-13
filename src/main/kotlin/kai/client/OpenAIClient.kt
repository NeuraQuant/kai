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
 * OpenAI client stub - extend as needed
 */
class OpenAIClient(
    private val apiKey: String,
    private val model: String = "gpt-4",
    private val baseUrl: String = "https://api.openai.com"
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
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofMinutes(2))
            .build()
        
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() != 200) {
            throw RuntimeException("OpenAI API error: ${response.statusCode()} - ${response.body()}")
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
            put("model", model)
            put("messages", JsonArray(jsonMessages))
            put("temperature", temperature)
            if (maxTokens > 0) {
                put("max_tokens", maxTokens)
            }
        }
        
        return requestJson.toString()
    }
    
    private fun parseResponse(responseBody: String): Response {
        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        
        val content = jsonResponse["choices"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject
            ?.get("content")?.jsonPrimitive?.content
            ?: throw RuntimeException("Invalid response format")
        
        val usage = jsonResponse["usage"]?.jsonObject?.let { usageObj ->
            usageObj.entries.associate { (key, value) ->
                key to when (value) {
                    is JsonPrimitive -> value.intOrNull ?: value.content
                    else -> value.toString()
                }
            }
        }
        
        return Response(content, usage)
    }
}