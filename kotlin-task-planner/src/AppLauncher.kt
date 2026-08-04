import java.util.Scanner
import javax.swing.SwingUtilities

fun runCLI() {
    val cli = StudyTrackerCLI()
    cli.start()
}

fun runGUI() {
    SwingUtilities.invokeLater {
        val gui = StudyTrackerGUI()
        gui.isVisible = true
    }
}

fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)

    if (args.isNotEmpty()) {
        when (args[0].lowercase()) {
            "--gui", "-g" -> {
                runGUI()
                return
            }
            "--cli", "-c" -> {
                runCLI()
                return
            }
        }
    }

    println("==========================================")
    println("      STUDY TIME TRACKER - KOTLIN         ")
    println("==========================================")
    println("Choose the version to start:")
    println("1. Graphical Interface (GUI)")
    println("2. Command Line (CLI)")
    println("3. Exit")
    print("Option > ")

    when (scanner.nextLine().trim()) {
        "1" -> runGUI()
        "2" -> runCLI()
        "3" -> println("Goodbye!")
        else -> {
            println("Invalid option. Starting GUI by default...")
            runGUI()
        }
    }
}