// FILE: examples/QuickStart.kt
import kai.*

suspend fun main() {
    val agent = Agent(
        name = "kai",
        system = "Be concise and correct.",
        llm = LmStudioClient(model = "lmstudio-community/qwen2.5-7b-instruct")
    ).use(timeNowTool)

    val text = agent.reply("What time is it?")
    println(text)
    
    val summary = agent.reply("Summarize Micronaut in one sentence.")
    println(summary)
}