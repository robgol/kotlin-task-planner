import java.util.Locale

// Modelo de Dados idêntico ao C#
data class StudyLog(
    val date: String,
    val category: String,
    val description: String,
    val hours: Double
) {
    fun toCsvRow(): String = "$date;$category;$description;$hours"

    companion object {
        fun fromCsvRow(row: String): StudyLog? {
            val parts = row.split(";")
            if (parts.size < 4) return null
            val hrs = parts[3].toDoubleOrNull() ?: return null
            return StudyLog(parts[0].trim(), parts[1].trim(), parts[2].trim(), hrs)
        }
    }
}