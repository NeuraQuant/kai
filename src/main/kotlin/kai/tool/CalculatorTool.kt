package kai.tool

/**
 * Simple calculator tool for evaluating math expressions
 */
class CalculatorTool : Tool {
    override val name = "calculator"
    override val description = "Evaluates mathematical expressions (e.g., '2 + 2', '10 * 5', 'sqrt(16)')"
    
    override fun execute(input: String): String {
        return try {
            // Simple expression evaluator for basic operations
            val result = evaluateExpression(input.trim())
            "Result: $result"
        } catch (e: Exception) {
            "Error evaluating expression: ${e.message}"
        }
    }
    
    private fun evaluateExpression(expr: String): Double {
        // Handle basic math operations - this is a simplified evaluator
        return when {
            // Handle sqrt function
            expr.startsWith("sqrt(") && expr.endsWith(")") -> {
                val num = expr.substring(5, expr.length - 1).toDouble()
                kotlin.math.sqrt(num)
            }
            // Handle Math.sqrt syntax
            expr.startsWith("Math.sqrt(") && expr.endsWith(")") -> {
                val num = expr.substring(10, expr.length - 1).toDouble()
                kotlin.math.sqrt(num)
            }
            // Handle simple arithmetic
            expr.contains("+") -> {
                val parts = expr.split("+")
                parts[0].trim().toDouble() + parts[1].trim().toDouble()
            }
            expr.contains("-") -> {
                val parts = expr.split("-")
                parts[0].trim().toDouble() - parts[1].trim().toDouble()
            }
            expr.contains("*") -> {
                val parts = expr.split("*")
                parts[0].trim().toDouble() * parts[1].trim().toDouble()
            }
            expr.contains("/") -> {
                val parts = expr.split("/")
                parts[0].trim().toDouble() / parts[1].trim().toDouble()
            }
            // Just a number
            else -> expr.toDouble()
        }
    }
}