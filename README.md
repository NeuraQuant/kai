# Kai - Kotlin Agentic Interface

A minimal, elegant Kotlin library for defining and running Agentic AI agents. Built for simplicity, readability, and conciseness with a clean public API.

## Features

- **Minimal API**: Clean, simple interface with ergonomic defaults
- **LM Studio Support**: Built-in client for LM Studio with OpenAI-compatible API
- **Tool System**: OpenAI-style function calling with built-in tools
- **Memory Management**: In-memory conversation history with configurable limits
- **Zero Ceremony**: Just Works™ out of the box
- **Framework Agnostic**: Works great in Micronaut, Spring, or standalone

## Quick Start

### 1. Add Dependencies

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-java:2.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

### 2. Start LM Studio

1. Download and install [LM Studio](https://lmstudio.ai/)
2. Load a model (e.g., "lmstudio-community/qwen2.5-7b-instruct")
3. Start the local server (runs on `http://localhost:1234`)

### 3. Basic Usage

```kotlin
import kai.*

suspend fun main() {
    val agent = Agent(
        name = "kai",
        system = "Be concise and correct.",
        llm = LmStudioClient(model = "lmstudio-community/qwen2.5-7b-instruct")
    ).use(timeNowTool)

    val text = agent.reply("What time is it?")
    println(text)
}
```

### 4. Advanced Usage

```kotlin
import kai.*

suspend fun main() {
    // Create agent with custom configuration
    val agent = Agent(
        name = "assistant",
        system = "You are a helpful coding assistant.",
        llm = LmStudioClient(
            model = "lmstudio-community/qwen2.5-7b-instruct",
            baseUrl = "http://localhost:1234/v1",
            apiKey = null // Optional
        ),
        memory = InMemoryMemory(maxMessages = 50)
    ).use(
        timeNowTool,
        httpGetTool,
        tool(
            name = "calculator",
            description = "Perform basic math operations",
            schema = """{"type":"object","properties":{"operation":{"type":"string"},"a":{"type":"number"},"b":{"type":"number"}},"required":["operation","a","b"]}"""
        ) { args, _ ->
            val json = Json.parseToJsonElement(args).jsonObject
            val operation = json["operation"]?.jsonPrimitive?.content ?: "add"
            val a = json["a"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            val b = json["b"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            
            when (operation) {
                "add" -> (a + b).toString()
                "multiply" -> (a * b).toString()
                else -> "Unknown operation"
            }
        }
    )

    // Chat with the agent
    val result = agent.chat("What's 5 + 3?")
    println(result.text)
    println("Tokens used: ${result.usage?.totalTokens}")

    // Remember something for the agent
    agent.remember("User prefers detailed explanations")

    // Continue conversation
    val followUp = agent.reply("Can you explain how you calculated that?")
    println(followUp)
}
```

## API Reference

### Core Classes

#### `Agent`
The main class for creating and managing AI agents.

```kotlin
class Agent(
    val name: String = "agent",
    val system: String = "You are a helpful assistant.",
    val llm: LlmClient,
    val memory: MemoryStore = InMemoryMemory(),
    val tools: MutableList<ToolSpec> = mutableListOf()
)
```

**Methods:**
- `chat(message: String, params: GenParams = GenParams()): LlmResult`
- `use(vararg tool: ToolSpec): Agent`
- `remember(note: String)`

#### `LmStudioClient`
Built-in client for LM Studio with OpenAI-compatible API.

```kotlin
class LmStudioClient(
    private val model: String,
    private val baseUrl: String = "http://localhost:1234/v1",
    private val apiKey: String? = null,
    private val http: HttpClient = defaultHttp()
) : LlmClient
```

#### `MemoryStore` & `InMemoryMemory`
In-memory conversation history with configurable limits.

```kotlin
interface MemoryStore {
    fun add(msg: Msg)
    fun recent(limit: Int = 20): List<Msg>
    var summary: String?
}

class InMemoryMemory(private val maxMessages: Int = 20) : MemoryStore
```

### Built-in Tools

#### `timeNowTool`
Returns the current ISO-8601 timestamp.

```kotlin
val timeNowTool: ToolSpec
```

#### `httpGetTool`
Makes safe GET requests with URL validation and 8KB response limit.

```kotlin
val httpGetTool: ToolSpec
```

### Extension Functions

#### `Agent.reply(user: String): String`
Convenient wrapper for `chat().text`.

```kotlin
suspend fun Agent.reply(user: String): String = chat(user).text
```

### Data Classes

#### `Msg`
Represents a message in the conversation.

```kotlin
data class Msg(
    val role: Role,
    val content: String
) {
    enum class Role { System, User, Assistant, Tool }
}
```

#### `LlmResult`
Response from the LLM with usage information.

```kotlin
data class LlmResult(
    val text: String,
    val usage: Usage? = null,
    val raw: String? = null
) {
    data class Usage(
        val promptTokens: Int?,
        val completionTokens: Int?,
        val totalTokens: Int?
    )
}
```

## Creating Custom Tools

Use the `tool()` helper function to create custom tools:

```kotlin
val customTool = tool(
    name = "my.tool",
    description = "Description of what the tool does",
    schema = """{"type":"object","properties":{"param":{"type":"string"}},"required":["param"]}"""
) { argsJson, context ->
    // Parse arguments
    val args = Json.parseToJsonElement(argsJson).jsonObject
    val param = args["param"]?.jsonPrimitive?.content ?: ""
    
    // Perform tool logic
    "Tool result: $param"
}
```

## Testing

The library includes comprehensive tests. Run them with:

```bash
./gradlew test
```

## Requirements

- **Kotlin**: 1.9.21+
- **Java**: 17+
- **Dependencies**: ktor-client-core, ktor-client-java, kotlinx-serialization-json, kotlinx-coroutines

## Configuration

### LM Studio Setup

1. **Default Configuration**: Works with LM Studio defaults (`http://localhost:1234/v1`)
2. **Custom Base URL**: Specify different endpoint if needed
3. **API Key**: Optional authentication (set to `null` for no auth)

### Memory Configuration

- **Default Limit**: 20 messages
- **Custom Limit**: Set `maxMessages` in `InMemoryMemory`
- **Summary Support**: Use `memory.summary` for conversation summaries

## Error Handling

The library includes built-in error handling:

- **Retry Logic**: Automatic retry on 429/5xx errors (max 2 retries)
- **Tool Errors**: Tool execution errors are caught and returned as tool messages
- **Network Timeouts**: 30-second timeout for HTTP requests

## License

MIT License - see LICENSE file for details.

## Contributing

Contributions are welcome! Please ensure:

- Code compiles with `-Xexplicit-api=strict`
- All tests pass
- Public API remains minimal
- Documentation is updated

## Troubleshooting

### Common Issues

1. **Connection Refused**: Ensure LM Studio is running on `localhost:1234`
2. **Model Not Found**: Verify the model name matches what's loaded in LM Studio
3. **Tool Errors**: Check tool JSON schema and argument parsing

### Debug Mode

Enable debug logging by setting system properties:

```bash
-Dktor.logging.level=DEBUG
```