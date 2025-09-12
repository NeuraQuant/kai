// FILE: src/main/kotlin/kai/Models.kt
package kai

import kotlinx.serialization.Serializable

@Serializable
public data class Msg(
    public val role: Role,
    public val content: String
) {
    public enum class Role {
        System, User, Assistant, Tool
    }
}

@Serializable
public data class GenParams(
    public val temperature: Double? = null,
    public val maxTokens: Int? = null,
    public val stop: List<String>? = null,
    public val topP: Double? = null
)

public data class ToolSpec(
    public val name: String,
    public val description: String,
    public val jsonSchema: String,
    public val invoke: suspend (argsJson: String, AgentContext) -> String
)

public data class LlmResult(
    public val text: String,
    public val usage: Usage? = null,
    public val raw: String? = null
) {
    public data class Usage(
        public val promptTokens: Int?,
        public val completionTokens: Int?,
        public val totalTokens: Int?
    )
}

public data class AgentContext(
    public val agent: Agent,
    public val scratch: MutableMap<String, Any?> = mutableMapOf()
)