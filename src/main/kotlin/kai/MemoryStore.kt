// FILE: src/main/kotlin/kai/MemoryStore.kt
package kai

public interface MemoryStore {
    public fun add(msg: Msg)
    public fun recent(limit: Int = 20): List<Msg>
    public var summary: String?
}

public class InMemoryMemory(
    private val maxMessages: Int = 20
) : MemoryStore {
    private val messages = mutableListOf<Msg>()
    public override var summary: String? = null
    
    public override fun add(msg: Msg) {
        messages.add(msg)
        if (messages.size > maxMessages) {
            messages.removeAt(0)
        }
    }
    
    public override fun recent(limit: Int): List<Msg> {
        return messages.takeLast(limit)
    }
}