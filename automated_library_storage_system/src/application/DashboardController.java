package application;

import application.modules.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {
    
    private LibrarySystemManager systemManager;
    private Timeline clockTimeline;
    private Timeline logRefreshTimeline;
    
    // Top bar
    @FXML private Label clockLabel;
    
    // Stats
    @FXML private Label lblTotalBooks;
    @FXML private Label lblAvailableRobots;
    @FXML private Label lblBusyRobots;
    @FXML private Label lblChargingRobots;
    @FXML private Label lblTasksQueue;
    @FXML private Label lblTasksCompleted;
    @FXML private Label lblTasksFailed;
    
    // Books
    @FXML private TextField txtSearchBook;
    @FXML private TableView<BookDisplay> tblBooks;
    @FXML private TableColumn<BookDisplay, String> colBookTitle;
    @FXML private TableColumn<BookDisplay, String> colBookAuthor;
    @FXML private TableColumn<BookDisplay, String> colBookShelf;
    
    // Tasks
    @FXML private TextField txtTaskBookTitle;
    @FXML private TableView<TaskDisplay> tblTasks;
    @FXML private TableColumn<TaskDisplay, String> colTaskName;
    @FXML private TableColumn<TaskDisplay, String> colTaskStatus;
    @FXML private TableColumn<TaskDisplay, String> colTaskPriority;
    @FXML private TableColumn<TaskDisplay, String> colTaskAssigned;
    
    // Equipment
    @FXML private TableView<RobotDisplay> tblRobots;
    @FXML private TableColumn<RobotDisplay, String> colRobotId;
    @FXML private TableColumn<RobotDisplay, String> colRobotBattery;
    @FXML private TableColumn<RobotDisplay, String> colRobotStatus;
    @FXML private TableColumn<RobotDisplay, String> colRobotLoad;
    
    // Charging Stations
    @FXML private ListView<String> lstChargingStations;
    
    // Shelves
    @FXML private TableView<ShelfDisplay> tblShelves;
    @FXML private TableColumn<ShelfDisplay, String> colShelfName;
    @FXML private TableColumn<ShelfDisplay, String> colShelfBooks;
    @FXML private TableColumn<ShelfDisplay, String> colShelfCapacity;
    @FXML private TableColumn<ShelfDisplay, String> colShelfStatus;
    
    // Logs
    @FXML private ComboBox<String> cmbLogScope;
    @FXML private TableView<LogDisplay> tblLogs;
    @FXML private TableColumn<LogDisplay, String> colLogTime;
    @FXML private TableColumn<LogDisplay, String> colLogMessage;
    
    // Settings
    @FXML private Spinner<Integer> spnBatteryThreshold;
    @FXML private Spinner<Integer> spnLogRefresh;
    
    // Status bar
    @FXML private Label lblStatus;
    @FXML private Label lblSystemTime;
    
    @FXML
    public void initialize() {
        systemManager = new LibrarySystemManager();
        
        setupTables();
        setupSettings();
        bindProperties();
        startClockUpdates();
        loadInitialData();
        startLogRefresh();
    }
    
    private void setupTables() {
        // Books table
        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBookAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colBookShelf.setCellValueFactory(new PropertyValueFactory<>("shelf"));
        
        // Tasks table
        colTaskName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTaskStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTaskPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colTaskAssigned.setCellValueFactory(new PropertyValueFactory<>("assigned"));
        
        // Robots table
        colRobotId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRobotBattery.setCellValueFactory(new PropertyValueFactory<>("battery"));
        colRobotStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRobotLoad.setCellValueFactory(new PropertyValueFactory<>("load"));
        
        // Shelves table
        colShelfName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colShelfBooks.setCellValueFactory(new PropertyValueFactory<>("bookCount"));
        colShelfCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colShelfStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Logs table
        colLogTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colLogMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        
        // Log scope combo box
        cmbLogScope.setItems(FXCollections.observableArrayList(
            "ALL", "SYSTEM", "TASKS", "RESOURCES", "STORAGE", "COMMON"
        ));
        cmbLogScope.setValue("ALL");
    }
    
    private void setupSettings() {
        SpinnerValueFactory<Integer> batteryFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 50, 15, 5);
        spnBatteryThreshold.setValueFactory(batteryFactory);
        
        SpinnerValueFactory<Integer> logFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2, 1);
        spnLogRefresh.setValueFactory(logFactory);
        
        // Load from config
        spnBatteryThreshold.getValueFactory().setValue((int)systemManager.getConfig().getBatteryThreshold());
        spnLogRefresh.getValueFactory().setValue(systemManager.getConfig().getLogRefreshInterval());
    }
    
    private void bindProperties() {
        lblTotalBooks.textProperty().bind(systemManager.totalBooksProperty().asString());
        lblAvailableRobots.textProperty().bind(systemManager.availableRobotsProperty().asString());
        lblBusyRobots.textProperty().bind(systemManager.busyRobotsProperty().asString());
        lblChargingRobots.textProperty().bind(systemManager.chargingRobotsProperty().asString());
        lblTasksQueue.textProperty().bind(systemManager.tasksInQueueProperty().asString());
        lblTasksCompleted.textProperty().bind(systemManager.tasksCompletedProperty().asString());
        lblTasksFailed.textProperty().bind(systemManager.tasksFailedProperty().asString());
        lblStatus.textProperty().bind(systemManager.statusMessageProperty());
    }
    
    private void startClockUpdates() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            clockLabel.setText(time);
            lblSystemTime.setText("System Time: " + time);
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }
    
    private void startLogRefresh() {
        logRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshLogs()));
        logRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        logRefreshTimeline.play();
    }
    
    private void loadInitialData() {
        refreshBooks();
        refreshRobots();
        refreshShelves();
        refreshChargingStations();
        refreshTasks();
        refreshLogs();
    }
    
    // Books Management
    @FXML
    private void handleSearchBook() {
        String query = txtSearchBook.getText().trim();
        if (query.isEmpty()) {
            refreshBooks();
            return;
        }
        
        ObservableList<BookDisplay> results = FXCollections.observableArrayList();
        for (Book book : systemManager.getAllBooks()) {
            if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                book.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                results.add(new BookDisplay(book));
            }
        }
        tblBooks.setItems(results);
        systemManager.setStatusMessage("Found " + results.size() + " book(s)");
    }
    
    @FXML
    private void handleAddBook() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Book");
        dialog.setHeaderText("Enter book details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField txtTitle = new TextField();
        TextField txtAuthor = new TextField();
        TextField txtWeight = new TextField("0.5");
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(txtTitle, 1, 0);
        grid.add(new Label("Author:"), 0, 1);
        grid.add(txtAuthor, 1, 1);
        grid.add(new Label("Weight (kg):"), 0, 2);
        grid.add(txtWeight, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                float weight = Float.parseFloat(txtWeight.getText());
                systemManager.addBook(txtTitle.getText(), txtAuthor.getText(), weight);
                refreshBooks();
                refreshShelves();
            } catch (Exception e) {
                showError("Failed to add book", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleViewBookDetails() {
        BookDisplay selected = tblBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a book to view details");
            return;
        }
        
        Book book = systemManager.getBookById(selected.getId());
        if (book != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Book Details");
            alert.setHeaderText(book.getTitle());
            alert.setContentText(
                "ID: " + book.getId() + "\n" +
                "Author: " + book.getAuthor() + "\n" +
                "Weight: " + String.format("%.2f", book.getWeightKg()) + " kg\n" +
                "Shelf: " + (book.getShelfId() != null ? book.getShelfId() : "Not assigned")
            );
            alert.showAndWait();
        }
    }
    
    private void refreshBooks() {
        ObservableList<BookDisplay> books = FXCollections.observableArrayList();
        for (Book book : systemManager.getAllBooks()) {
            books.add(new BookDisplay(book));
        }
        tblBooks.setItems(books);
    }
    
    // Task Management
    @FXML
    private void handleGetBook() {
        String bookTitle = txtTaskBookTitle.getText().trim();
        if (bookTitle.isEmpty()) {
            showWarning("Missing Input", "Please enter a book title");
            return;
        }
        
        systemManager.createGetBookTask(bookTitle);
        txtTaskBookTitle.clear();
        refreshTasks();
    }
    
    @FXML
    private void handleReturnBook() {
        String bookTitle = txtTaskBookTitle.getText().trim();
        if (bookTitle.isEmpty()) {
            showWarning("Missing Input", "Please enter a book title");
            return;
        }
        
        systemManager.createReturnBookTask(bookTitle, null);
        txtTaskBookTitle.clear();
        refreshTasks();
    }
    
    private void refreshTasks() {
        ObservableList<TaskDisplay> tasks = FXCollections.observableArrayList();
        for (Task task : systemManager.getLibrary().getTaskManager().getAllTasks()) {
            if (task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CANCELLED) {
                tasks.add(new TaskDisplay(task));
            }
        }
        tblTasks.setItems(tasks);
    }
    
    // Equipment Management
    @FXML
    private void handleAddRobot() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Robot");
        dialog.setHeaderText("Enter robot specifications");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField txtId = new TextField("ROBOT-" + (systemManager.getAllRobots().size() + 1));
        TextField txtMaxWeight = new TextField("5.0");
        TextField txtMaxBooks = new TextField("10");
        TextField txtExecDuration = new TextField("5.0");
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(new Label("Max Weight (kg):"), 0, 1);
        grid.add(txtMaxWeight, 1, 1);
        grid.add(new Label("Max Books:"), 0, 2);
        grid.add(txtMaxBooks, 1, 2);
        grid.add(new Label("Exec Duration (s):"), 0, 3);
        grid.add(txtExecDuration, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                systemManager.addRobot(
                    txtId.getText(),
                    Float.parseFloat(txtMaxWeight.getText()),
                    Integer.parseInt(txtMaxBooks.getText()),
                    Float.parseFloat(txtExecDuration.getText())
                );
                refreshRobots();
            } catch (Exception e) {
                showError("Failed to add robot", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRobotDetails() {
        RobotDisplay selected = tblRobots.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a robot to view details");
            return;
        }
        
        Robot robot = systemManager.getRobotById(selected.getId());
        if (robot != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Robot Details");
            alert.setHeaderText(robot.getId());
            alert.setContentText(
                "Battery: " + String.format("%.1f", robot.getCurrentChargePercent()) + "%\n" +
                "Max Weight: " + robot.getMaxWeightKg() + " kg\n" +
                "Max Books: " + robot.getMaxBookCount() + "\n" +
                "Current Load: " + String.format("%.2f", robot.getCurrentLoadWeight()) + " kg\n" +
                "Carrying Books: " + robot.getCarryingBooks().size() + "\n" +
                "Docked: " + (robot.isDocked() ? "Yes" : "No")
            );
            alert.showAndWait();
        }
    }
    
    private void refreshRobots() {
        ObservableList<RobotDisplay> robots = FXCollections.observableArrayList();
        for (Robot robot : systemManager.getAllRobots()) {
            robots.add(new RobotDisplay(robot));
        }
        tblRobots.setItems(robots);
    }
    
    // Charging Stations
    @FXML
    private void handleAddStation() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Charging Station");
        dialog.setHeaderText("Enter station details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField txtId = new TextField("CHG-" + (systemManager.getAllStations().size() + 1));
        TextField txtName = new TextField("Station " + (systemManager.getAllStations().size() + 1));
        TextField txtSlots = new TextField("3");
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Slots:"), 0, 2);
        grid.add(txtSlots, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                systemManager.addChargingStation(
                    txtId.getText(),
                    txtName.getText(),
                    Integer.parseInt(txtSlots.getText())
                );
                refreshChargingStations();
            } catch (Exception e) {
                showError("Failed to add station", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleStationDetails() {
        String selected = lstChargingStations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a station to view details");
            return;
        }
        
        String stationId = selected.split(":")[0];
        ChargingStation station = systemManager.getStationById(stationId);
        if (station != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Station Details");
            alert.setHeaderText(station.getName());
            alert.setContentText(
                "ID: " + station.getId() + "\n" +
                "Total Slots: " + station.getTotalSlots() + "\n" +
                "Occupied: " + station.getOccupiedSlots() + "\n" +
                "Available: " + station.getAvailableSlots()
            );
            alert.showAndWait();
        }
    }
    
    private void refreshChargingStations() {
        ObservableList<String> stations = FXCollections.observableArrayList();
        for (ChargingStation station : systemManager.getAllStations()) {
            stations.add(station.getId() + ": " + station.getName() + 
                " (" + station.getOccupiedSlots() + "/" + station.getTotalSlots() + ")");
        }
        lstChargingStations.setItems(stations);
    }
    
    // Shelves Management
    @FXML
    private void handleAddShelf() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Shelf");
        dialog.setHeaderText("Enter shelf details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField txtId = new TextField("SHELF-" + (systemManager.getAllShelves().size() + 1));
        TextField txtName = new TextField("Shelf " + (char)('A' + systemManager.getAllShelves().size()));
        TextField txtCapacity = new TextField("10");
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Capacity:"), 0, 2);
        grid.add(txtCapacity, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                systemManager.addShelf(
                    txtId.getText(),
                    txtName.getText(),
                    Integer.parseInt(txtCapacity.getText())
                );
                refreshShelves();
            } catch (Exception e) {
                showError("Failed to add shelf", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleViewShelfBooks() {
        ShelfDisplay selected = tblShelves.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a shelf to view books");
            return;
        }
        
        Shelf shelf = systemManager.getShelfById(selected.getId());
        if (shelf != null) {
            StringBuilder books = new StringBuilder();
            for (Book book : shelf.getBooks()) {
                books.append("- ").append(book.getTitle()).append(" by ").append(book.getAuthor()).append("\n");
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Shelf Books");
            alert.setHeaderText(shelf.getName() + " (" + shelf.getCurrentCount() + "/" + shelf.getMaxCapacity() + ")");
            alert.setContentText(books.length() > 0 ? books.toString() : "No books on this shelf");
            alert.showAndWait();
        }
    }
    
    private void refreshShelves() {
        ObservableList<ShelfDisplay> shelves = FXCollections.observableArrayList();
        for (Shelf shelf : systemManager.getAllShelves()) {
            shelves.add(new ShelfDisplay(shelf));
        }
        tblShelves.setItems(shelves);
    }
    
    // Logs
    @FXML
    private void handleRefreshLogs() {
        refreshLogs();
        systemManager.setStatusMessage("Logs refreshed");
    }
    
    private void refreshLogs() {
        try {
            List<String> logLines = Logger.openLogByDate(
                Logger.Scope.valueOf(cmbLogScope.getValue().equals("ALL") ? "SYSTEM" : cmbLogScope.getValue()),
                java.time.LocalDate.now()
            );
            
            ObservableList<LogDisplay> logs = FXCollections.observableArrayList();
            for (String line : logLines) {
                logs.add(LogDisplay.fromLine(line));
            }
            
            // Sort by time descending (most recent first)
            logs.sort((a, b) -> b.getTime().compareTo(a.getTime()));
            
            tblLogs.setItems(logs.stream().limit(100).collect(Collectors.toCollection(FXCollections::observableArrayList)));
        } catch (Exception e) {
            Logger.logSystem("ERROR", "Failed to refresh logs: " + e.getMessage());
        }
    }
    
    // Settings
    @FXML
    private void handleSaveConfig() {
        try {
            systemManager.getConfig().setBatteryThreshold(spnBatteryThreshold.getValue().floatValue());
            systemManager.getConfig().setLogRefreshInterval(spnLogRefresh.getValue());
            
            // Restart log refresh with new interval
            logRefreshTimeline.stop();
            logRefreshTimeline = new Timeline(new KeyFrame(
                Duration.seconds(spnLogRefresh.getValue()), 
                e -> refreshLogs()
            ));
            logRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
            logRefreshTimeline.play();
            
            systemManager.setStatusMessage("Configuration saved");
            Logger.logSystem("INFO", "Configuration updated");
        } catch (Exception e) {
            showError("Failed to save config", e.getMessage());
        }
    }
    
    @FXML
    private void handleSaveState() {
        try {
            systemManager.saveState();
            systemManager.setStatusMessage("System state saved successfully");
        } catch (Exception e) {
            showError("Failed to save state", e.getMessage());
        }
    }
    
    // Utility methods
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void shutdown() {
        if (clockTimeline != null) clockTimeline.stop();
        if (logRefreshTimeline != null) logRefreshTimeline.stop();
        if (systemManager != null) systemManager.shutdown();
    }
    
    // Display classes for TableView
    public static class BookDisplay {
        private final String id;
        private final String title;
        private final String author;
        private final String shelf;
        
        public BookDisplay(Book book) {
            this.id = book.getId();
            this.title = book.getTitle();
            this.author = book.getAuthor();
            this.shelf = book.getShelfId() != null ? book.getShelfId() : "None";
        }
        
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getShelf() { return shelf; }
    }
    
    public static class TaskDisplay {
        private final String name;
        private final String status;
        private final String priority;
        private final String assigned;
        
        public TaskDisplay(Task task) {
            this.name = task.getTaskName();
            this.status = task.getStatus().toString();
            this.priority = task.getPriority().toString();
            this.assigned = task.getAssignedTo();
        }
        
        public String getName() { return name; }
        public String getStatus() { return status; }
        public String getPriority() { return priority; }
        public String getAssigned() { return assigned; }
    }
    
    public static class RobotDisplay {
        private final String id;
        private final String battery;
        private final String status;
        private final String load;
        
        public RobotDisplay(Robot robot) {
            this.id = robot.getId();
            this.battery = String.format("%.1f%%", robot.getCurrentChargePercent());
            this.status = robot.isDocked() ? "Charging" : 
                         robot.getCarryingBooks().isEmpty() ? "Idle" : "Busy";
            this.load = String.format("%.1f/%.1f kg", robot.getCurrentLoadWeight(), robot.getMaxWeightKg());
        }
        
        public String getId() { return id; }
        public String getBattery() { return battery; }
        public String getStatus() { return status; }
        public String getLoad() { return load; }
    }
    
    public static class ShelfDisplay {
        private final String id;
        private final String name;
        private final String bookCount;
        private final String capacity;
        private final String status;
        
        public ShelfDisplay(Shelf shelf) {
            this.id = shelf.getId();
            this.name = shelf.getName();
            this.bookCount = String.valueOf(shelf.getCurrentCount());
            this.capacity = shelf.getCurrentCount() + "/" + shelf.getMaxCapacity();
            this.status = shelf.isFull() ? "Full" : 
                         shelf.getCurrentCount() == 0 ? "Empty" : "Available";
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getBookCount() { return bookCount; }
        public String getCapacity() { return capacity; }
        public String getStatus() { return status; }
    }
    
    public static class LogDisplay {
        private final String time;
        private final String message;
        
        public LogDisplay(String time, String message) {
            this.time = time;
            this.message = message;
        }
        
        public static LogDisplay fromLine(String line) {
            // Parse log line: [2025-11-10 12:34:56] SCOPE.LEVEL(name): message
            if (line.startsWith("[")) {
                int endBracket = line.indexOf("]");
                if (endBracket > 0) {
                    String timestamp = line.substring(1, endBracket);
                    String time = timestamp.substring(11); // HH:mm:ss
                    String message = line.substring(endBracket + 2);
                    return new LogDisplay(time, message);
                }
            }
            return new LogDisplay("--:--:--", line);
        }
        
        public String getTime() { return time; }
        public String getMessage() { return message; }
    }
}

