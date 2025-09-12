// FILE: src/main/kotlin/kai/LlmClient.kt
package kai

public interface LlmClient {
    public suspend fun chat(
        messages: List<Msg>,
        tools: List<ToolSpec> = emptyList(),
        params: GenParams = GenParams()
    ): LlmResult
}