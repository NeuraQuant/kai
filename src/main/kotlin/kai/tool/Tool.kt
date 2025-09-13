package kai.tool

/**
 * Interface for agent tools
 */
interface Tool {
    val name: String
    val description: String
    
    /**
     * Execute the tool with given input and return result
     */
    fun execute(input: String): String
}