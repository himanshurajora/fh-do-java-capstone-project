package application;

public class Main {
 
    public static void main(String[] args) {
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
		tm.startTask(t1.getTaskId());
		tm.completeTask(t1.getTaskId());
		tm.startTask(t2.getTaskId());
		tm.cancelTask(t2.getTaskId());
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
