package application;

import application.modules.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import java.util.*;
import java.util.concurrent.*;

public class LibrarySystemManager {
    private Library library;
    private UnifiedConcurrentSystem concurrentSystem;
    private SystemState systemState;
    private SystemConfig config;
    
    private Map<String, Robot> robotMap;
    private Map<String, ChargingStation> stationMap;
    private Map<String, Shelf> shelfMap;
    private Map<String, Book> bookMap;
    
    private ScheduledExecutorService stateUpdateExecutor;
    private ScheduledExecutorService autoSaveExecutor;
    
    // Observable properties for UI binding
    private final IntegerProperty totalBooks = new SimpleIntegerProperty(0);
    private final IntegerProperty totalShelves = new SimpleIntegerProperty(0);
    private final IntegerProperty totalRobots = new SimpleIntegerProperty(0);
    private final IntegerProperty availableRobots = new SimpleIntegerProperty(0);
    private final IntegerProperty busyRobots = new SimpleIntegerProperty(0);
    private final IntegerProperty chargingRobots = new SimpleIntegerProperty(0);
    private final IntegerProperty tasksInQueue = new SimpleIntegerProperty(0);
    private final IntegerProperty tasksCompleted = new SimpleIntegerProperty(0);
    private final IntegerProperty tasksFailed = new SimpleIntegerProperty(0);
    private final StringProperty statusMessage = new SimpleStringProperty("System ready");
    
    public LibrarySystemManager() {
        this.robotMap = new ConcurrentHashMap<>();
        this.stationMap = new ConcurrentHashMap<>();
        this.shelfMap = new ConcurrentHashMap<>();
        this.bookMap = new ConcurrentHashMap<>();
        
        loadSystemState();
        initializeSystem();
        startBackgroundTasks();
        
        Logger.logSystem("INFO", "LibrarySystemManager initialized");
    }
    
    private void loadSystemState() {
        systemState = SystemState.load();
        config = systemState.getConfig();
    }
    
    private void initializeSystem() {
        library = new Library();
        
        // Initialize shelves
        for (SystemState.ShelfData shelfData : systemState.getShelves()) {
            Shelf shelf = new Shelf(shelfData.getId(), shelfData.getName(), shelfData.getMaxCapacity());
            library.addShelf(shelf);
            shelfMap.put(shelf.getId(), shelf);
        }
        
        // Initialize books
        for (SystemState.BookData bookData : systemState.getBooks()) {
            Book book = new Book(bookData.getId(), bookData.getTitle(), bookData.getAuthor(), bookData.getWeightKg());
            book.setShelfId(bookData.getShelfId());
            library.addBook(book);
            bookMap.put(book.getId(), book);
            
            // Add book to its shelf
            if (bookData.getShelfId() != null) {
                Shelf shelf = shelfMap.get(bookData.getShelfId());
                if (shelf != null && !shelf.isFull()) {
                    shelf.addBook(book);
                }
            }
        }
        
        // Initialize charging stations
        for (SystemState.ChargingStationData stationData : systemState.getStations()) {
            ChargingStation station = new ChargingStation(
                stationData.getId(), 
                stationData.getName(), 
                stationData.getNumSlots()
            );
            library.addStation(station);
            stationMap.put(station.getId(), station);
        }
        
        // Initialize robots
        for (SystemState.RobotData robotData : systemState.getRobots()) {
            Robot robot = new Robot(
                robotData.getId(),
                robotData.getExecutionDuration(),
                robotData.getMaxWeightKg(),
                robotData.getMaxBookCount()
            );
            robot.setCurrentChargePercent(robotData.getCurrentChargePercent());
            library.addRobot(robot);
            robotMap.put(robot.getId(), robot);
        }
        
        // Initialize concurrent system
        concurrentSystem = new UnifiedConcurrentSystem(
            config.getNumChargingStations(), 
            systemState.getRobots().size()
        );
        
        // Add robots to concurrent system
        for (Robot robot : robotMap.values()) {
            concurrentSystem.addRobot(robot);
        }
        
        updateObservableProperties();
    }
    
    private void startBackgroundTasks() {
        // Update UI properties periodically
        stateUpdateExecutor = Executors.newScheduledThreadPool(1);
        stateUpdateExecutor.scheduleAtFixedRate(() -> {
            Platform.runLater(this::updateObservableProperties);
        }, 0, 500, TimeUnit.MILLISECONDS);
        
        // Auto-save state every 30 seconds
        autoSaveExecutor = Executors.newScheduledThreadPool(1);
        autoSaveExecutor.scheduleAtFixedRate(this::saveState, 30, 30, TimeUnit.SECONDS);
    }
    
    private void updateObservableProperties() {
        totalBooks.set(bookMap.size());
        totalShelves.set(shelfMap.size());
        totalRobots.set(robotMap.size());
        availableRobots.set(concurrentSystem.getAvailableRobotCount());
        busyRobots.set(concurrentSystem.getBusyRobotCount());
        chargingRobots.set(concurrentSystem.getActiveChargingCount());
        tasksInQueue.set(concurrentSystem.getTaskQueueSize());
        tasksCompleted.set(concurrentSystem.getTotalTasksCompleted());
        tasksFailed.set(concurrentSystem.getTotalTasksFailed());
    }
    
    public void saveState() {
        try {
            // Update system state from current state
            systemState.getBooks().clear();
            for (Book book : bookMap.values()) {
                SystemState.BookData bookData = new SystemState.BookData();
                bookData.setId(book.getId());
                bookData.setTitle(book.getTitle());
                bookData.setAuthor(book.getAuthor());
                bookData.setWeightKg(book.getWeightKg());
                bookData.setShelfId(book.getShelfId());
                systemState.getBooks().add(bookData);
            }
            
            systemState.getRobots().clear();
            for (Robot robot : robotMap.values()) {
                SystemState.RobotData robotData = new SystemState.RobotData();
                robotData.setId(robot.getId());
                robotData.setMaxWeightKg(robot.getMaxWeightKg());
                robotData.setMaxBookCount(robot.getMaxBookCount());
                robotData.setCurrentChargePercent(robot.getCurrentChargePercent());
                robotData.setExecutionDuration(robot.getExecutionDuration());
                systemState.getRobots().add(robotData);
            }
            
            systemState.save();
        } catch (Exception e) {
            Logger.logSystem("ERROR", "Failed to save state: " + e.getMessage());
            setStatusMessage("Error saving state: " + e.getMessage());
        }
    }
    
    // Task operations
    public void createGetBookTask(String bookTitle) {
        try {
            Book book = findBookByTitle(bookTitle);
            if (book == null) {
                setStatusMessage("Book not found: " + bookTitle);
                Logger.logSystem("WARN", "Book not found for task: " + bookTitle);
                return;
            }
            
            if (book.getShelfId() == null) {
                setStatusMessage("Book is not on any shelf: " + bookTitle);
                return;
            }
            
            String taskId = "GET-" + System.currentTimeMillis();
            Task task = new Task(
                taskId,
                "Get Book",
                "Retrieve book: " + book.getTitle() + " from " + book.getShelfId(),
                TaskPriority.MEDIUM,
                "AUTO"
            );
            
            concurrentSystem.addTask(task);
            setStatusMessage("Task created: Get " + book.getTitle());
            Logger.logTasks("INFO", "Get book task created: " + book.getTitle());
            
        } catch (Exception e) {
            setStatusMessage("Error creating task: " + e.getMessage());
            Logger.logSystem("ERROR", "Failed to create get book task: " + e.getMessage());
        }
    }
    
    public void createReturnBookTask(String bookTitle, String targetShelfId) {
        try {
            Book book = findBookByTitle(bookTitle);
            if (book == null) {
                setStatusMessage("Book not found: " + bookTitle);
                return;
            }
            
            Shelf targetShelf = shelfMap.get(targetShelfId);
            if (targetShelf == null) {
                // Find any shelf with space
                targetShelf = findShelfWithSpace();
                if (targetShelf == null) {
                    setStatusMessage("No available shelf space");
                    return;
                }
            }
            
            if (targetShelf.isFull()) {
                setStatusMessage("Target shelf is full");
                return;
            }
            
            String taskId = "RETURN-" + System.currentTimeMillis();
            Task task = new Task(
                taskId,
                "Return Book",
                "Return book: " + book.getTitle() + " to " + targetShelf.getName(),
                TaskPriority.LOW,
                "AUTO"
            );
            
            concurrentSystem.addTask(task);
            
            // Update book location
            book.setShelfId(targetShelf.getId());
            if (!targetShelf.getBooks().contains(book)) {
                targetShelf.addBook(book);
            }
            
            setStatusMessage("Task created: Return " + book.getTitle());
            Logger.logTasks("INFO", "Return book task created: " + book.getTitle());
            
        } catch (Exception e) {
            setStatusMessage("Error creating task: " + e.getMessage());
            Logger.logSystem("ERROR", "Failed to create return book task: " + e.getMessage());
        }
    }
    
    // Book operations
    public void addBook(String title, String author, float weight) {
        try {
            String bookId = "BOOK-" + (bookMap.size() + 1);
            Book book = new Book(bookId, title, author, weight);
            
            Shelf shelf = findShelfWithSpace();
            if (shelf != null) {
                shelf.addBook(book);
                book.setShelfId(shelf.getId());
            }
            
            library.addBook(book);
            bookMap.put(book.getId(), book);
            
            setStatusMessage("Book added: " + title);
            Logger.logSystem("INFO", "Book added: " + title);
            
        } catch (Exception e) {
            setStatusMessage("Error adding book: " + e.getMessage());
            Logger.logSystem("ERROR", "Failed to add book: " + e.getMessage());
        }
    }
    
    public Book searchBook(String query) {
        // Search by title or author
        for (Book book : bookMap.values()) {
            if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                book.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                return book;
            }
        }
        return null;
    }
    
    // Robot operations
    public void addRobot(String id, float maxWeight, int maxBooks, float execDuration) {
        try {
            Robot robot = new Robot(id, execDuration, maxWeight, maxBooks);
            robot.setCurrentChargePercent(100.0f);
            
            library.addRobot(robot);
            robotMap.put(robot.getId(), robot);
            concurrentSystem.addRobot(robot);
            
            setStatusMessage("Robot added: " + id);
            Logger.logSystem("INFO", "Robot added: " + id);
            
        } catch (Exception e) {
            setStatusMessage("Error adding robot: " + e.getMessage());
            Logger.logSystem("ERROR", "Failed to add robot: " + e.getMessage());
        }
    }
    
    // Charging station operations
    public void addChargingStation(String id, String name, int numSlots) {
        try {
            ChargingStation station = new ChargingStation(id, name, numSlots);
            library.addStation(station);
            stationMap.put(station.getId(), station);
            
            setStatusMessage("Charging station added: " + name);
            Logger.logSystem("INFO", "Charging station added: " + name);
            
        } catch (Exception e) {
            setStatusMessage("Error adding station: " + e.getMessage());
            Logger.logSystem("ERROR", "Failed to add charging station: " + e.getMessage());
        }
    }
    
    // Shelf operations
    public void addShelf(String id, String name, int maxCapacity) {
        try {
            Shelf shelf = new Shelf(id, name, maxCapacity);
            library.addShelf(shelf);
            shelfMap.put(shelf.getId(), shelf);
            
            setStatusMessage("Shelf added: " + name);
            Logger.logSystem("INFO", "Shelf added: " + name);
            
        } catch (Exception e) {
            setStatusMessage("Error adding shelf: " + e.getMessage());
            Logger.logSystem("ERROR", "Failed to add shelf: " + e.getMessage());
        }
    }
    
    // Helper methods
    private Book findBookByTitle(String title) {
        for (Book book : bookMap.values()) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;
    }
    
    private Shelf findShelfWithSpace() {
        for (Shelf shelf : shelfMap.values()) {
            if (shelf.hasSpace()) {
                return shelf;
            }
        }
        return null;
    }
    
    // Getters
    public Library getLibrary() { return library; }
    public UnifiedConcurrentSystem getConcurrentSystem() { return concurrentSystem; }
    public SystemConfig getConfig() { return config; }
    
    public Collection<Robot> getAllRobots() { return robotMap.values(); }
    public Collection<ChargingStation> getAllStations() { return stationMap.values(); }
    public Collection<Shelf> getAllShelves() { return shelfMap.values(); }
    public Collection<Book> getAllBooks() { return bookMap.values(); }
    
    public Robot getRobotById(String id) { return robotMap.get(id); }
    public ChargingStation getStationById(String id) { return stationMap.get(id); }
    public Shelf getShelfById(String id) { return shelfMap.get(id); }
    public Book getBookById(String id) { return bookMap.get(id); }
    
    // Observable properties
    public IntegerProperty totalBooksProperty() { return totalBooks; }
    public IntegerProperty totalShelvesProperty() { return totalShelves; }
    public IntegerProperty totalRobotsProperty() { return totalRobots; }
    public IntegerProperty availableRobotsProperty() { return availableRobots; }
    public IntegerProperty busyRobotsProperty() { return busyRobots; }
    public IntegerProperty chargingRobotsProperty() { return chargingRobots; }
    public IntegerProperty tasksInQueueProperty() { return tasksInQueue; }
    public IntegerProperty tasksCompletedProperty() { return tasksCompleted; }
    public IntegerProperty tasksFailedProperty() { return tasksFailed; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    
    public void setStatusMessage(String message) {
        Platform.runLater(() -> statusMessage.set(message));
    }
    
    // Shutdown
    public void shutdown() {
        Logger.logSystem("INFO", "Shutting down LibrarySystemManager");
        
        if (stateUpdateExecutor != null) {
            stateUpdateExecutor.shutdown();
        }
        if (autoSaveExecutor != null) {
            autoSaveExecutor.shutdown();
        }
        
        saveState();
        
        if (concurrentSystem != null) {
            concurrentSystem.shutdown();
        }
    }
}

