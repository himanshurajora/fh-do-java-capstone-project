package application.tests;

public class TestSuite {
    public static void run(String[] args) {
        System.out.println("Running test suite...\n");
        TaskManagerTests.run();
        RobotTests.run();
        LibraryTests.run();
        BookTests.run();
        ShelfTests.run();
        ChargingStationTests.run();
        System.out.println("\nTest suite finished.");
    }
}


