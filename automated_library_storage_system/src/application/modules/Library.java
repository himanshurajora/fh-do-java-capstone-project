package application.modules;
import java.util.ArrayList;
import java.util.List;


public class Library {
	
	    
	    private List<Book> books;
	    private List<Shelf> shelves;
	    private List<Robot> robots;
	    private List<ChargingStation> stations;
	    private TaskManager taskManager;

	   
	    public Library() {
	       
	        books = new ArrayList<>();
	        shelves = new ArrayList<>();
	        robots = new ArrayList<>();
	        stations = new ArrayList<>();

	       
	        taskManager = new TaskManager();

	        System.out.println("Library System Initialized Successfully!");
	        application.Logger.logSystem("INFO", "Library initialized");
	    }

	  
	    public void addBook(Book book) {
	        try {
	            if (book != null) {
	                books.add(book);
	                System.out.println("Book added successfully: " + book.getTitle());
	                application.Logger.logSystem("INFO", "Book added: " + book.getTitle());
	            } else {
	                System.out.println("Cannot add a null book.");
	            }
	        } catch (Exception e) {
	            System.out.println("Error while adding book: " + e.getMessage());
	            application.Logger.logSystem("ERROR", "Add book failed: " + e.getMessage());
	        }
	    }

	    public void addShelf(Shelf shelf) {
	        try {
	            if (shelf != null) {
	                shelves.add(shelf);
	                System.out.println("Shelf added successfully.");
	                application.Logger.logSystem("INFO", "Shelf added");
	            } else {
	                System.out.println("Cannot add a null shelf.");
	            }
	        } catch (Exception e) {
	            System.out.println("Error while adding shelf: " + e.getMessage());
	            application.Logger.logSystem("ERROR", "Add shelf failed: " + e.getMessage());
	        }
	    }

	    public void addRobot(Robot robot) {
	        try {
	            if (robot != null) {
	                robots.add(robot);
	                System.out.println("Robot added successfully: " + robot.getId());
	                application.Logger.logSystem("INFO", "Robot added: " + robot.getId());
	            } else {
	                System.out.println("Cannot add a null robot.");
	            }
	        } catch (Exception e) {
	            System.out.println("Error while adding robot: " + e.getMessage());
	            application.Logger.logSystem("ERROR", "Add robot failed: " + e.getMessage());
	        }
	    }

	    public void addStation(ChargingStation station) {
	        try {
	            if (station != null) {
	                stations.add(station);
	                System.out.println("Charging Station added successfully.");
	                application.Logger.logSystem("INFO", "Charging station added");
	            } else {
	                System.out.println("Cannot add a null station.");
	            }
	        } catch (Exception e) {
	            System.out.println("Error while adding station: " + e.getMessage());
	            application.Logger.logSystem("ERROR", "Add station failed: " + e.getMessage());
	        }
	    }

	    public void monitorSystem() {
	        System.out.println("\n--- System Monitoring ---");
	        System.out.println("Total Books: " + books.size());
	        System.out.println("Total Shelves: " + shelves.size());
	        System.out.println("Total Robots: " + robots.size());
	        System.out.println("Total Charging Stations: " + stations.size());
	        System.out.println("Tasks in Manager: " + taskManager.getTaskCount());
	        System.out.println("-------------------------\n");
	        application.Logger.logSystem("INFO", "Monitor: books=" + books.size() + ", shelves=" + shelves.size() + ", robots=" + robots.size() + ", stations=" + stations.size());
	    }

	    
	    public List<Book> getBooks() { return books; }
	    public List<Shelf> getShelves() { return shelves; }
	    public List<Robot> getRobots() { return robots; }
	    public List<ChargingStation> getStations() { return stations; }
	    public TaskManager getTaskManager() { return taskManager; }
	

}
