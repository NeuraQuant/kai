package kai.model

/**
 * Response from an LLM client
 */
data class Response(
    val content: String,
    val usage: Map<String, Any>? = null
)