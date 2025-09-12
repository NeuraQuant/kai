// FILE: src/test/kotlin/kai/MemoryTest.kt
package kai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

public class MemoryTest {
    
    @Test
    public fun `should store and retrieve messages`(): Unit {
        val memory = InMemoryMemory(maxMessages = 3)
        
        memory.add(Msg(Msg.Role.User, "Hello"))
        memory.add(Msg(Msg.Role.Assistant, "Hi there!"))
        
        val recent = memory.recent()
        assertEquals(2, recent.size)
        assertEquals("Hello", recent[0].content)
        assertEquals("Hi there!", recent[1].content)
    }
    
    @Test
    public fun `should limit message count`(): Unit {
        val memory = InMemoryMemory(maxMessages = 2)
        
        memory.add(Msg(Msg.Role.User, "Message 1"))
        memory.add(Msg(Msg.Role.User, "Message 2"))
        memory.add(Msg(Msg.Role.User, "Message 3"))
        
        val recent = memory.recent()
        assertEquals(2, recent.size)
        assertEquals("Message 2", recent[0].content)
        assertEquals("Message 3", recent[1].content)
    }
    
    @Test
    public fun `should handle summary`(): Unit {
        val memory = InMemoryMemory()
        
        assertNull(memory.summary)
        
        memory.summary = "Previous conversation about cats"
        assertEquals("Previous conversation about cats", memory.summary)
    }
    
    @Test
    public fun `should limit recent messages`(): Unit {
        val memory = InMemoryMemory()
        
        repeat(10) { i ->
            memory.add(Msg(Msg.Role.User, "Message $i"))
        }
        
        val recent = memory.recent(limit = 3)
        assertEquals(3, recent.size)
        assertEquals("Message 7", recent[0].content)
        assertEquals("Message 8", recent[1].content)
        assertEquals("Message 9", recent[2].content)
    }
}