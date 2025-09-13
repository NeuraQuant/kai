# Kai - Lightweight Agentic AI Library for Kotlin

Kai is a minimal, elegant library for building and managing AI agents in Kotlin. It prioritizes simplicity, readability, and ease of use while providing powerful features for agent-based AI applications.

## Features

- **Simple Setup**: One-liner agent creation and chat
- **Memory Management**: Automatic conversation context persistence
- **Tool Integration**: Extensible tool system for agent capabilities
- **Multiple LLM Support**: Built-in support for LM Studio and OpenAI
- **Minimal Dependencies**: Only uses kotlinx.serialization
- **Kotlin-First**: Leverages Kotlin idioms for clean, concise code

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.kai:kai:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.kai:kai:1.0.0'
}
```

### Building from Source

```bash
git clone https://github.com/yourusername/kai.git
cd kai
./gradlew build
./gradlew publishToMavenLocal
```

## Quick Start

### Basic Usage

```kotlin
import kai.agent.Agent
import kai.client.LmStudioClient

fun main() {
    // Create agent with LM Studio (assumes LM Studio running on localhost:1234)
    val agent = Agent(LmStudioClient())
    
    // Chat with the agent
    val response = agent.chat("Hello! What can you help me with?")
    println(response)
}
```

### With System Prompt

```kotlin
val agent = Agent(
    llmClient = LmStudioClient(),
    systemPrompt = "You are a helpful coding assistant specializing in Kotlin."
)

val response = agent.chat("How do I create a data class?")
println(response)
```

### Using Tools

```kotlin
import kai.tool.CalculatorTool
import kai.tool.DateTimeTool

val agent = Agent(
    llmClient = LmStudioClient(),
    systemPrompt = "You are a helpful assistant with access to tools.",
    tools = listOf(CalculatorTool(), DateTimeTool())
)

// Direct tool execution
val result = agent.executeTool("calculator", "2 + 2")
println(result) // Result: 4.0

// Or let the agent decide when to use tools
val response = agent.chat("What's 15 * 7?")
```

### Memory Management

```kotlin
val agent = Agent(LmStudioClient())

// Have a conversation
agent.chat("My name is Alice")
agent.chat("I like programming in Kotlin")

// The agent remembers context
val response = agent.chat("What's my name and what do I like?")
// Response will reference Alice and Kotlin programming

// View conversation history
println(agent.getHistory())

// Clear memory when needed
agent.clearMemory()
```

### Using the Fluent Builder

```kotlin
import kai.agent.agent
import kai.tool.CalculatorTool
import kai.tool.WebSearchTool

val myAgent = agent {
    withClient(LmStudioClient("http://localhost:1234"))
    withSystemPrompt("You are a research assistant.")
    withTool(CalculatorTool())
    withTool(WebSearchTool())
}

val response = myAgent.chat("What's the square root of 144?")
```

### Custom LLM Client

```kotlin
import kai.client.LlmClient
import kai.model.Message
import kai.model.Response

class CustomLlmClient : LlmClient {
    override fun chat(
        messages: List<Message>,
        temperature: Double,
        maxTokens: Int
    ): Response {
        // Your custom implementation
        return Response("Custom response", null)
    }
}

val agent = Agent(CustomLlmClient())
```

### Creating Custom Tools

```kotlin
import kai.tool.Tool

class CodeExecutorTool : Tool {
    override val name = "code_executor"
    override val description = "Executes Kotlin code snippets"
    
    override fun execute(input: String): String {
        // Implementation for code execution
        return "Code executed successfully"
    }
}

val agent = Agent(
    llmClient = LmStudioClient(),
    tools = listOf(CodeExecutorTool())
)
```

### Integration with Micronaut

```kotlin
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import kai.agent.Agent
import kai.client.LmStudioClient

@Factory
class AgentFactory {
    
    @Bean
    @Singleton
    fun agent(): Agent {
        return Agent(
            llmClient = LmStudioClient(),
            systemPrompt = "You are a helpful API assistant."
        )
    }
}

// In your service
@Singleton
class ChatService(private val agent: Agent) {
    
    fun processUserQuery(query: String): String {
        return agent.chat(query)
    }
}
```

### Configuration Properties

When using Kai with dependency injection frameworks like Micronaut, you can configure the library using application properties. The following properties are supported:

#### LM Studio Configuration

| Property | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `kai.lmstudio.url` | Base URL for LM Studio server | `http://localhost:1234` | No |
| `kai.lmstudio.model` | Specific model to use (optional) | None | No |

**Example configuration in `application.yml`:**

```yaml
kai:
  lmstudio:
    url: http://localhost:1234
    model: qwen2.5-coder-7b
```

**Example configuration in `application.properties`:**

```properties
kai.lmstudio.url=http://localhost:1234
kai.lmstudio.model=qwen2.5-coder-7b
```

#### OpenAI Configuration

| Property | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `kai.openai.api-key` | OpenAI API key | None | Yes (when using OpenAI) |
| `kai.openai.model` | OpenAI model to use | `gpt-4` | No |

**Example configuration in `application.yml`:**

```yaml
kai:
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-4-turbo-preview
```

**Example configuration in `application.properties`:**

```properties
kai.openai.api-key=${OPENAI_API_KEY}
kai.openai.model=gpt-4-turbo-preview
```

**Using @Value annotations in your code:**

```kotlin
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton

@Factory
class AgentFactory {
    
    @Bean
    @Singleton
    fun lmStudioAgent(
        @Value("\${kai.lmstudio.url:http://localhost:1234}") baseUrl: String,
        @Value("\${kai.lmstudio.model:}") model: String?
    ): Agent {
        val client = if (model.isNullOrEmpty()) {
            LmStudioClient(baseUrl)
        } else {
            LmStudioClient(baseUrl, model)
        }
        return Agent(client)
    }
    
    @Bean
    @Singleton
    fun openAIAgent(
        @Value("\${kai.openai.api-key}") apiKey: String,
        @Value("\${kai.openai.model:gpt-4}") model: String
    ): Agent {
        return Agent(OpenAIClient(apiKey, model))
    }
}
```

**Environment Variables:**

For security, it's recommended to use environment variables for sensitive data like API keys:

```bash
export OPENAI_API_KEY="your-api-key-here"
```

Then reference them in your configuration:

```yaml
kai:
  openai:
    api-key: ${OPENAI_API_KEY}
```

### Advanced Configuration

```kotlin
// Configure LM Studio with specific model
val lmStudioClient = LmStudioClient(
    baseUrl = "http://localhost:1234",
    model = "qwen2.5-coder-7b"
)

// List available models
val models = lmStudioClient.listModels()
println("Available models: $models")

// Create agent with custom parameters
val agent = Agent(lmStudioClient)

// Chat with custom temperature and token limit
val response = agent.chat(
    message = "Write a haiku about programming",
    temperature = 0.9,  // More creative
    maxTokens = 100     // Limit response length
)
```

### OpenAI Integration

```kotlin
import kai.client.OpenAIClient

val openAIClient = OpenAIClient(
    apiKey = System.getenv("OPENAI_API_KEY"),
    model = "gpt-4"
)

val agent = Agent(
    llmClient = openAIClient,
    systemPrompt = "You are GPT-4, a powerful language model."
)

val response = agent.chat("Explain quantum computing")
```

## Direct Property Access

Kai favors direct property access for simplicity:

```kotlin
val agent = Agent(LmStudioClient())

// Directly modify system prompt
agent.systemPrompt = "You are now a poetry expert."

// Directly access and modify memory
agent.memory.add(Message("user", "Remember this important fact"))
agent.memory.removeAt(0)

// Access tools list
val toolNames = agent.tools.map { it.name }
```

## Example: Building an Agentic Coding Assistant

```kotlin
import kai.agent.Agent
import kai.client.LmStudioClient
import kai.tool.Tool

// Custom tool for file operations
class FileReaderTool : Tool {
    override val name = "read_file"
    override val description = "Reads content from a file"
    
    override fun execute(input: String): String {
        return try {
            java.io.File(input).readText()
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
    }
}

fun main() {
    val codingAgent = Agent(
        llmClient = LmStudioClient(),
        systemPrompt = """
            You are an expert Kotlin developer.
            Help users with code reviews, refactoring, and best practices.
            When analyzing code, be thorough but concise.
        """.trimIndent(),
        tools = listOf(FileReaderTool(), CalculatorTool())
    )
    
    // Analyze a file
    codingAgent.chat("Read and analyze the file at src/Main.kt")
    
    // Get suggestions
    val suggestions = codingAgent.chat("What improvements would you suggest?")
    println(suggestions)
}
```

## Testing with LM Studio

1. Download and install [LM Studio](https://lmstudio.ai/)
2. Load a model (e.g., Qwen 2.5 Coder, Granite Code)
3. Start the local server (usually on port 1234)
4. Run your Kai agent:

```kotlin
fun main() {
    val client = LmStudioClient("http://localhost:1234")
    
    // Check available models
    println("Models: ${client.listModels()}")
    
    // Create and test agent
    val agent = Agent(client)
    println(agent.chat("Hello!"))
}
```

## Architecture

```
kai/
├── agent/
│   ├── Agent.kt          # Core agent class
│   └── AgentBuilder.kt   # Fluent builder pattern
├── client/
│   ├── LlmClient.kt      # Client interface
│   ├── LmStudioClient.kt # LM Studio implementation
│   └── OpenAIClient.kt   # OpenAI implementation
├── model/
│   ├── Message.kt        # Message data class
│   └── Response.kt       # Response data class
└── tool/
    ├── Tool.kt           # Tool interface
    ├── CalculatorTool.kt # Math evaluator
    ├── DateTimeTool.kt   # Date/time tool
    └── WebSearchTool.kt  # Web search stub
```

## Design Principles

- **Simplicity First**: Direct property access, minimal configuration
- **Elegant APIs**: Kotlin idioms, default parameters, extension functions
- **Zero Boilerplate**: No unnecessary abstractions or config classes
- **Extensible**: Easy to add custom clients, tools, and behaviors
- **Lightweight**: Minimal dependencies, fast compilation

## License

MIT License - see LICENSE file for details