package kai.tool

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Tool for getting current date and time information
 */
class DateTimeTool : Tool {
    override val name = "datetime"
    override val description = "Gets current date and time information (input can be 'now', 'date', 'time', or a timezone like 'UTC', 'America/New_York')"
    
    override fun execute(input: String): String {
        return try {
            val zone = when (input.lowercase()) {
                "now", "date", "time", "" -> ZoneId.systemDefault()
                else -> ZoneId.of(input)
            }
            
            val now = LocalDateTime.now(zone)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            
            "Current date/time in $zone: ${now.format(formatter)}"
        } catch (e: Exception) {
            "Error: Invalid timezone or input. Use 'now' or a valid timezone ID (e.g., 'UTC', 'America/New_York')"
        }
    }
}