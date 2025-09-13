package kai.client

import kai.model.Message
import kai.model.Response

/**
 * Interface for LLM client implementations
 */
interface LlmClient {
    /**
     * Send messages to the LLM and receive a response
     */
    fun chat(
        messages: List<Message>,
        temperature: Double = 0.7,
        maxTokens: Int = -1
    ): Response
}