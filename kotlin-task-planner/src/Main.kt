import java.awt.*
import java.io.File
import java.util.Locale
import java.util.Scanner
import javax.swing.*
import javax.swing.table.DefaultTableModel

// Shared default categories
val STUDY_CATEGORIES = listOf(
    "Planning", "Research", "Implementation",
    "Troubleshooting", "Documentation", "Video Production", "Publishing"
)

// ==============================================================================
// 1. CONSOLE MODE (CLI) - With all C# functionalities
// ==============================================================================
class StudyTrackerCLI(private val filePath: String = "study_logs.csv") {
    private val logs = mutableListOf<StudyLog>()

    init { loadFromCsv() }

    fun start() {
        val scanner = Scanner(System.`in`)
        var running = true

        println("\n==========================================")
        println("    STUDY TIME TRACKER (Kotlin Console)   ")
        println("==========================================")

        while (running) {
            println("\nMain Menu:")
            println("1. Add New Study Log Entry")
            println("2. View All Study Logs")
            println("3. Show Summary Statistics")
            println("4. Reload Data from File")
            println("5. Exit Console Mode")
            print("Option > ")

            when (scanner.nextLine().trim()) {
                "1" -> addNewLog(scanner)
                "2" -> listAllLogs()
                "3" -> showSummary()
                "4" -> {
                    loadFromCsv()
                    println("\n[INFO] Data reloaded from $filePath successfully.")
                }
                "5" -> {
                    println("\nExiting Console Mode. Goodbye!")
                    running = false
                }
                else -> println("\n[ERROR] Invalid option. Please enter 1 to 5.")
            }
        }
    }

    private fun addNewLog(scanner: Scanner) {
        print("\nEnter date (YYYY-MM-DD) [Default: 2026-08-04]: ")
        var date = scanner.nextLine().trim()
        if (date.isBlank()) date = "2026-08-04"

        println("\nSelect Category:")
        STUDY_CATEGORIES.forEachIndexed { i, cat -> println("  ${i + 1}. $cat") }
        print("Category number (1-${STUDY_CATEGORIES.size}) > ")
        val catIndex = scanner.nextLine().trim().toIntOrNull()?.minus(1) ?: 2
        val category = STUDY_CATEGORIES.getOrElse(catIndex) { "Implementation" }

        print("Enter brief description: ")
        val description = scanner.nextLine().trim()

        print("Enter hours spent (e.g. 2.0): ")
        val hours = scanner.nextLine().trim().toDoubleOrNull() ?: 1.0

        logs.add(StudyLog(date, category, description, hours))
        saveToCsv()
        println("\n[SUCCESS] Entry saved to $filePath!")
    }

    private fun listAllLogs() {
        if (logs.isEmpty()) {
            println("\n[INFO] No entries found in $filePath.")
            return
        }
        println("\n==========================================================================")
        println("                        LOGGED STUDY SESSIONS                             ")
        println("==========================================================================")
        println(String.format("%-5s | %-12s | %-18s | %-8s | %s", "#", "Date", "Category", "Hours", "Description"))
        println("--------------------------------------------------------------------------")

        logs.forEachIndexed { index, log ->
            println(String.format("%-5d | %-12s | %-18s | %-8s | %s",
                index + 1, log.date, log.category, "%.1f".format(Locale.US, log.hours), log.description))
        }
        println("--------------------------------------------------------------------------")
    }

    private fun showSummary() {
        if (logs.isEmpty()) {
            println("\n[INFO] No data available to display summary.")
            return
        }

        val totalHours = logs.sumOf { it.hours }
        val totalSessions = logs.size
        val avgHours = totalHours / totalSessions

        val categoryTotals = logs.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.hours } }

        println("\n==========================================")
        println("       STUDY TIME ANALYSIS SUMMARY        ")
        println("==========================================")
        println("Total Logged Hours : ${"%.1f".format(Locale.US, totalHours)} hrs")
        println("Total Sessions     : $totalSessions")
        println("Average Session    : ${"%.2f".format(Locale.US, avgHours)} hrs\n")

        println("--- Hours Breakdown by Category ---")
        for ((cat, hrs) in categoryTotals.entries.sortedByDescending { it.value }) {
            val pct = (hrs / totalHours) * 100
            println(" - %-18s : %4.1f hrs (%5.1f%%)".format(Locale.US, cat, hrs, pct))
        }
        println("==========================================")
    }

    private fun saveToCsv() {
        File(filePath).writeText(logs.joinToString("\n") { it.toCsvRow() })
    }

    private fun loadFromCsv() {
        logs.clear()
        val file = File(filePath)
        if (file.exists()) {
            file.readLines().filter { it.isNotBlank() }.forEach { line ->
                StudyLog.fromCsvRow(line)?.let { logs.add(it) }
            }
        }
    }
}

// ==============================================================================
// 2. GRAPHICAL MODE (GUI) - With all C# functionalities
// ==============================================================================
class StudyTrackerGUI : JFrame("Study Time Tracker - Kotlin GUI") {
    private val logs = mutableListOf<StudyLog>()
    private val filePath = "study_logs.csv"

    private val tableModel = DefaultTableModel(arrayOf("Date", "Category", "Description", "Hours"), 0)
    private val table = JTable(tableModel)
    private val statusLabel = JLabel("Ready")

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        size = Dimension(750, 500)
        setLocationRelativeTo(null)
        layout = BorderLayout(10, 10)

        loadFromCsv()

        // Header
        val titleLabel = JLabel("Study Time Tracker", SwingConstants.CENTER)
        titleLabel.font = Font("Arial", Font.BOLD, 20)
        titleLabel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        add(titleLabel, BorderLayout.NORTH)

        // Central table
        add(JScrollPane(table), BorderLayout.CENTER)

        // Button Panel (Same as C#)
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 10))
        val btnAdd = JButton("Add Study Log")
        val btnSummary = JButton("Show Summary")
        val btnReload = JButton("Reload File")

        buttonPanel.add(btnAdd)
        buttonPanel.add(btnSummary)
        buttonPanel.add(btnReload)

        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(buttonPanel, BorderLayout.NORTH)
        statusLabel.border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
        bottomPanel.add(statusLabel, BorderLayout.SOUTH)

        add(bottomPanel, BorderLayout.SOUTH)

        // Button Events
        btnAdd.addActionListener { openAddLogDialog() }
        btnSummary.addActionListener { showSummaryDialog() }
        btnReload.addActionListener {
            loadFromCsv()
            refreshTable()
            JOptionPane.showMessageDialog(this, "Data reloaded from $filePath", "Info", JOptionPane.INFORMATION_MESSAGE)
        }

        refreshTable()
    }

    private fun openAddLogDialog() {
        val dateField = JTextField("2026-08-04")
        val categoryBox = JComboBox(STUDY_CATEGORIES.toTypedArray())
        val descField = JTextField()
        val hoursField = JTextField("2.0")

        val formPanel = JPanel(GridLayout(4, 2, 5, 5)).apply {
            add(JLabel("Date (YYYY-MM-DD):"))
            add(dateField)
            add(JLabel("Category:"))
            add(categoryBox)
            add(JLabel("Description:"))
            add(descField)
            add(JLabel("Hours Spent:"))
            add(hoursField)
        }

        val result = JOptionPane.showConfirmDialog(
            this, formPanel, "Add New Study Log Entry",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        )

        if (result == JOptionPane.OK_OPTION && descField.text.isNotBlank()) {
            val hrs = hoursField.text.toDoubleOrNull() ?: 1.0
            val log = StudyLog(
                date = dateField.text.trim(),
                category = categoryBox.selectedItem.toString(),
                description = descField.text.trim(),
                hours = hrs
            )
            logs.add(log)
            saveToCsv()
            refreshTable()
        }
    }

    private fun showSummaryDialog() {
        if (logs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data available.", "Summary", JOptionPane.INFORMATION_MESSAGE)
            return
        }

        val totalHours = logs.sumOf { it.hours }
        val categoryTotals = logs.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.hours } }

        val summaryText = StringBuilder()
        summaryText.append("=== STUDY TIME ANALYSIS SUMMARY ===\n")
        summaryText.append("Total Logged Hours : %.1f hrs\n".format(Locale.US, totalHours))
        summaryText.append("Total Sessions     : %d\n\n".format(logs.size))
        summaryText.append("Hours Breakdown by Category:\n")

        for ((cat, hrs) in categoryTotals.entries.sortedByDescending { it.value }) {
            val pct = (hrs / totalHours) * 100
            summaryText.append(" - %-18s : %4.1f hrs (%5.1f%%)\n".format(Locale.US, cat, hrs, pct))
        }

        JOptionPane.showMessageDialog(this, summaryText.toString(), "Summary Statistics", JOptionPane.INFORMATION_MESSAGE)
    }

    private fun refreshTable() {
        tableModel.rowCount = 0
        logs.forEach { log ->
            tableModel.addRow(arrayOf(log.date, log.category, log.description, "${"%.1f".format(Locale.US, log.hours)} hrs"))
        }
        val totalHours = logs.sumOf { it.hours }
        statusLabel.text = "Total Entries: ${logs.size} | Total Study Hours: ${"%.1f".format(Locale.US, totalHours)} hrs | File: $filePath"
    }

    private fun saveToCsv() {
        File(filePath).writeText(logs.joinToString("\n") { it.toCsvRow() })
    }

    private fun loadFromCsv() {
        logs.clear()
        val file = File(filePath)
        if (file.exists()) {
            file.readLines().filter { it.isNotBlank() }.forEach { line ->
                StudyLog.fromCsvRow(line)?.let { logs.add(it) }
            }
        }
    }
}