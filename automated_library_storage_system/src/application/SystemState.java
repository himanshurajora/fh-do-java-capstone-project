package application;

import application.modules.*;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class SystemState {
    private static final String STATE_FILE = "automated_library_storage_system/store.json";
    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();
    
    private List<BookData> books;
    private List<ShelfData> shelves;
    private List<RobotData> robots;
    private List<ChargingStationData> stations;
    private List<TaskData> tasks;
    private SystemConfig config;
    
    public SystemState() {
        this.books = new ArrayList<>();
        this.shelves = new ArrayList<>();
        this.robots = new ArrayList<>();
        this.stations = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.config = new SystemConfig();
    }
    
    public static SystemState load() {
        try {
            Path path = Paths.get(STATE_FILE);
            if (Files.exists(path)) {
                String json = Files.readString(path);
                SystemState state = gson.fromJson(json, SystemState.class);
                Logger.logSystem("INFO", "System state loaded from " + STATE_FILE);
                return state;
            }
        } catch (Exception e) {
            Logger.logSystem("ERROR", "Failed to load state: " + e.getMessage());
        }
        return createDefault();
    }
    
    public void save() {
        try {
            Path path = Paths.get(STATE_FILE);
            Files.createDirectories(path.getParent());
            String json = gson.toJson(this);
            Files.writeString(path, json);
            Logger.logSystem("INFO", "System state saved to " + STATE_FILE);
        } catch (Exception e) {
            Logger.logSystem("ERROR", "Failed to save state: " + e.getMessage());
        }
    }
    
    private static SystemState createDefault() {
        SystemState state = new SystemState();
        
        // Default config
        state.config = new SystemConfig();
        state.config.setBatteryThreshold(15.0f);
        state.config.setLogRefreshInterval(2);
        state.config.setNumChargingStations(3);
        state.config.setNumSlotsPerStation(3);
        
        // Create default shelves
        for (int i = 1; i <= 5; i++) {
            ShelfData shelf = new ShelfData();
            shelf.setId("SHELF-" + i);
            shelf.setName("Shelf " + (char)('A' + i - 1));
            shelf.setMaxCapacity(10);
            shelf.setBookIds(new ArrayList<>());
            state.shelves.add(shelf);
        }
        
        // Create default charging stations
        for (int i = 1; i <= 3; i++) {
            ChargingStationData station = new ChargingStationData();
            station.setId("CHG-" + i);
            station.setName("Station " + i);
            station.setNumSlots(3);
            state.stations.add(station);
        }
        
        // Create default robots
        for (int i = 1; i <= 5; i++) {
            RobotData robot = new RobotData();
            robot.setId("ROBOT-" + i);
            robot.setMaxWeightKg(5.0f);
            robot.setMaxBookCount(10);
            robot.setCurrentChargePercent(100.0f);
            robot.setExecutionDuration(5.0f);
            state.robots.add(robot);
        }
        
        // Create default books
        String[][] defaultBooks = {
            {"The Great Gatsby", "F. Scott Fitzgerald"},
            {"1984", "George Orwell"},
            {"To Kill a Mockingbird", "Harper Lee"},
            {"Pride and Prejudice", "Jane Austen"},
            {"The Catcher in the Rye", "J.D. Salinger"},
            {"Harry Potter", "J.K. Rowling"},
            {"The Lord of the Rings", "J.R.R. Tolkien"},
            {"Animal Farm", "George Orwell"},
            {"The Hobbit", "J.R.R. Tolkien"},
            {"Fahrenheit 451", "Ray Bradbury"}
        };
        
        for (int i = 0; i < defaultBooks.length; i++) {
            BookData book = new BookData();
            book.setId("BOOK-" + (i + 1));
            book.setTitle(defaultBooks[i][0]);
            book.setAuthor(defaultBooks[i][1]);
            book.setWeightKg(0.5f + (float)(Math.random() * 1.0));
            book.setShelfId("SHELF-" + ((i % 5) + 1));
            state.books.add(book);
            
            // Add to shelf
            ShelfData shelf = state.shelves.get(i % 5);
            shelf.getBookIds().add(book.getId());
        }
        
        Logger.logSystem("INFO", "Created default system state with " + 
            state.books.size() + " books, " + state.shelves.size() + " shelves, " +
            state.robots.size() + " robots, " + state.stations.size() + " stations");
        return state;
    }
    
    // Getters and Setters
    public List<BookData> getBooks() { return books; }
    public void setBooks(List<BookData> books) { this.books = books; }
    
    public List<ShelfData> getShelves() { return shelves; }
    public void setShelves(List<ShelfData> shelves) { this.shelves = shelves; }
    
    public List<RobotData> getRobots() { return robots; }
    public void setRobots(List<RobotData> robots) { this.robots = robots; }
    
    public List<ChargingStationData> getStations() { return stations; }
    public void setStations(List<ChargingStationData> stations) { this.stations = stations; }
    
    public List<TaskData> getTasks() { return tasks; }
    public void setTasks(List<TaskData> tasks) { this.tasks = tasks; }
    
    public SystemConfig getConfig() { return config; }
    public void setConfig(SystemConfig config) { this.config = config; }
    
    // Data classes for serialization
    public static class BookData {
        private String id;
        private String title;
        private String author;
        private float weightKg;
        private String shelfId;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public float getWeightKg() { return weightKg; }
        public void setWeightKg(float weightKg) { this.weightKg = weightKg; }
        
        public String getShelfId() { return shelfId; }
        public void setShelfId(String shelfId) { this.shelfId = shelfId; }
    }
    
    public static class ShelfData {
        private String id;
        private String name;
        private int maxCapacity;
        private List<String> bookIds;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getMaxCapacity() { return maxCapacity; }
        public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
        
        public List<String> getBookIds() { return bookIds; }
        public void setBookIds(List<String> bookIds) { this.bookIds = bookIds; }
    }
    
    public static class RobotData {
        private String id;
        private float maxWeightKg;
        private int maxBookCount;
        private float currentChargePercent;
        private float executionDuration;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public float getMaxWeightKg() { return maxWeightKg; }
        public void setMaxWeightKg(float maxWeightKg) { this.maxWeightKg = maxWeightKg; }
        
        public int getMaxBookCount() { return maxBookCount; }
        public void setMaxBookCount(int maxBookCount) { this.maxBookCount = maxBookCount; }
        
        public float getCurrentChargePercent() { return currentChargePercent; }
        public void setCurrentChargePercent(float currentChargePercent) { this.currentChargePercent = currentChargePercent; }
        
        public float getExecutionDuration() { return executionDuration; }
        public void setExecutionDuration(float executionDuration) { this.executionDuration = executionDuration; }
    }
    
    public static class ChargingStationData {
        private String id;
        private String name;
        private int numSlots;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getNumSlots() { return numSlots; }
        public void setNumSlots(int numSlots) { this.numSlots = numSlots; }
    }
    
    public static class TaskData {
        private String taskId;
        private String taskName;
        private String description;
        private String priority;
        private String status;
        private String assignedTo;
        
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    }
    
    // LocalDateTime adapter for Gson
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
        
        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString());
        }
    }
}

