package kai.model

import kotlinx.serialization.Serializable

/**
 * Simple message representation for agent conversations
 */
@Serializable
data class Message(
    val role: String,  // "system", "user", "assistant"
    val content: String
)