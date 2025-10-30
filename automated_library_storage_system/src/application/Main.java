package application;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
 
    public static void main(String[] args) {
    	runDemo();
    	System.out.println("\n\n");
    	runCLI();
    }

    private static void runCLI() {
    	Scanner scanner = new Scanner(System.in);
    	boolean running = true;
    	
    	System.out.println("=== Library Storage System Log Viewer ===");
    	
    	while (running) {
    		System.out.println("\nOptions:");
    		System.out.println("1. Open logs by date (dd-MM-yyyy)");
    		System.out.println("2. Search logs by equipment name");
    		System.out.println("3. List available log files");
    		System.out.println("4. Exit");
    		System.out.print("Enter option (1-4): ");
    		
    		String choice = scanner.nextLine().trim();
    		
    		switch (choice) {
    			case "1":
    				handleOpenByDate(scanner);
    				break;
    			case "2":
    				handleSearchByEquipment(scanner);
    				break;
    			case "3":
    				handleListFiles();
    				break;
    			case "4":
    				running = false;
    				System.out.println("Goodbye!");
    				break;
    			default:
    				System.out.println("Invalid option. Please enter 1-4.");
    		}
    	}
    	scanner.close();
    }

    private static void handleOpenByDate(Scanner scanner) {
    	System.out.print("Enter date (dd-MM-yyyy): ");
    	String dateStr = scanner.nextLine().trim();
    	
    	try {
    		List<String> lines = Logger.openLogByDateString(dateStr);
    		if (lines.isEmpty()) {
    			System.out.println("No logs found for date: " + dateStr);
    		} else {
    			System.out.println("\n=== Logs for " + dateStr + " (" + lines.size() + " entries) ===");
    			for (String line : lines) {
    				System.out.println(line);
    			}
    		}
    	} catch (IOException e) {
    		System.out.println("Error: " + e.getMessage());
    	}
    }

    private static void handleSearchByEquipment(Scanner scanner) {
    	System.out.print("Enter equipment name (e.g., RBT-01, shelf-A2, CHG-1): ");
    	String equipmentName = scanner.nextLine().trim();
    	
    	try {
    		List<String> results = Logger.searchLogsByEquipment(equipmentName);
    		if (results.isEmpty()) {
    			System.out.println("No logs found for equipment: " + equipmentName);
    		} else {
    			System.out.println("\n=== Logs for " + equipmentName + " (" + results.size() + " entries) ===");
    			for (String result : results) {
    				System.out.println(result);
    			}
    		}
    	} catch (IOException e) {
    		System.out.println("Error: " + e.getMessage());
    	}
    }

    private static void handleListFiles() {
    	try {
    		List<String> files = Logger.listAvailableLogFiles();
    		if (files.isEmpty()) {
    			System.out.println("No log files found.");
    		} else {
    			System.out.println("\n=== Available Log Files (" + files.size() + " files) ===");
    			for (String file : files) {
    				System.out.println(file);
    			}
    		}
    	} catch (IOException e) {
    		System.out.println("Error: " + e.getMessage());
    	}
    }

    private static void runDemo() {
    	System.out.println("Starting Automated Library Storage System demo...");

    	// System level log
    	Logger.logSystem("INFO", "System boot");

    	// Simulate robots and charging stations activities
    	String robotA = "RBT-01";
    	String robotB = "RBT-02";
    	String station = "CHG-1";

		Logger.logResources(robotA, "INFO", "Picked 3 books from shelf A2");
		Logger.logResources(robotB, "WARN", "Load approaching threshold");
		Logger.logResources(station, "INFO", "Robot RBT-01 plugged in");
		Logger.logResources(station, "INFO", "Robot RBT-01 charged to 100%");

		// Task management system demo
		application.modules.TaskManager tm = new application.modules.TaskManager();
		application.modules.Task t1 = tm.createTask("PickOrder-1001", "Pick 3 books from A2", application.modules.TaskPriority.HIGH, robotA);
		application.modules.Task t2 = tm.createTask("ChargeRobot", "Charge RBT-02 at CHG-1", application.modules.TaskPriority.MEDIUM, robotB);
		try {
			tm.startTask(t1.getTaskId());
			tm.completeTask(t1.getTaskId());
			tm.startTask(t2.getTaskId());
			tm.cancelTask(t2.getTaskId());
		} catch (application.modules.RobotExceptions.TaskNotFoundException e) {
			Logger.logSystem("ERROR", "Task operation failed: " + e.getMessage());
		}
		Logger.logSystem("INFO", "Tasks summary: total=" + tm.getAllTasks().size());

    	// Regex-based log entry creation
		Logger.logFromRaw("RESOURCES:" + robotA + "|INFO|Delivered books to counter");
		Logger.logFromRaw("SYSTEM:|INFO|Nightly consistency check completed");
		Logger.logFromRaw("RESOURCES:" + station + "|ERROR|Overcurrent protection tripped");

		// Storage scope demo
		Logger.logStorage("shelf-A2", "INFO", "Book BK-100 added to shelf A2");
		Logger.logStorage("book-BK-100", "INFO", "Book metadata updated");

    	// Open and show today's system log and a robot log (character streams under the hood)
    	try {
			var today = java.time.LocalDate.now();
			var sysLines = Logger.openLogByDate(Logger.Scope.SYSTEM, today);
    		System.out.println("System log (today): " + sysLines.size() + " lines");
    		sysLines.stream().limit(3).forEach(System.out::println);

			var resLines = Logger.openLogByDate(Logger.Scope.RESOURCES, today);
			System.out.println("Resources log (today): " + resLines.size() + " lines");
			resLines.stream().limit(3).forEach(System.out::println);

			var tasksLines = Logger.openLogByDate(Logger.Scope.TASKS, today);
			System.out.println("Tasks log (today): " + tasksLines.size() + " lines");
			tasksLines.stream().limit(3).forEach(System.out::println);
    	} catch (Exception e) {
    		System.out.println("Failed to open logs: " + e.getMessage());
    	}

    	System.out.println("Demo complete. Logs are under automated_library_storage_system/logs");
    }
}
