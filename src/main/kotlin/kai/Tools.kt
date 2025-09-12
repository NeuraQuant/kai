// FILE: src/main/kotlin/kai/Tools.kt
package kai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL
import java.time.OffsetDateTime

public fun tool(
    name: String,
    description: String,
    schema: String,
    body: suspend (args: String, AgentContext) -> String
): ToolSpec = ToolSpec(name, description, schema, body)

public val timeNowTool: ToolSpec = tool(
    name = "time.now",
    description = "Return the current ISO-8601 time.",
    schema = """{"type":"object","properties":{},"additionalProperties":false}"""
) { _, _ -> OffsetDateTime.now().toString() }

public val httpGetTool: ToolSpec = tool(
    name = "http.get",
    description = "Make a safe GET request to a URL. Only allows http/https URLs and returns text content.",
    schema = """{"type":"object","properties":{"url":{"type":"string","description":"The URL to fetch"}},"required":["url"],"additionalProperties":false}"""
) { argsJson, _ ->
    val args = Json.parseToJsonElement(argsJson).jsonObject
    val url = args["url"]?.jsonPrimitive?.content ?: throw IllegalArgumentException("URL is required")
    
    val parsedUrl = URL(url)
    if (parsedUrl.protocol !in listOf("http", "https")) {
        throw IllegalArgumentException("Only HTTP and HTTPS URLs are allowed")
    }
    
    val http = HttpClient(Java) {
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
        }
    }
    
    try {
        val response = http.get(url) {
            // 8KB cap - simplified for now
        }
        
        if (response.status.isSuccess()) {
            response.body<String>()
        } else {
            "HTTP ${response.status.value}: ${response.status.description}"
        }
    } catch (e: Exception) {
        "Error: ${e.message}"
    } finally {
        http.close()
    }
}