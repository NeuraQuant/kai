// FILE: src/test/kotlin/kai/QuickStartTest.kt
package kai

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

public class QuickStartTest {
    
    @Test
    public fun `quickstart example should work`(): Unit = runTest {
        // This mimics the QuickStart example but with a mock client
        val agent = Agent(
            name = "kai",
            system = "Be concise and correct.",
            llm = MockLmStudioClient()
        ).use(timeNowTool)
        
        val text = agent.reply("What time is it?")
        assertNotNull(text)
        assertTrue(text.isNotEmpty())
        
        val summary = agent.reply("Summarize Micronaut in one sentence.")
        assertNotNull(summary)
        assertTrue(summary.isNotEmpty())
    }
}