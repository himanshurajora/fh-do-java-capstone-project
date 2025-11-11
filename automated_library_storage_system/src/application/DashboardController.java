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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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
    private String currentBookFilter = ""; // Store current search filter
    
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
    
    // Charging
    @FXML private Label lblChargingQueue;
    
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
        
        // Enable text selection in book title column for easy copying
        colBookTitle.setCellFactory(column -> {
            return new TableCell<BookDisplay, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        // Make text selectable
                        setStyle("-fx-cursor: text;");
                    }
                }
            };
        });
        
        // Add context menu for copying book titles
        ContextMenu bookContextMenu = new ContextMenu();
        MenuItem copyTitle = new MenuItem("Copy Title");
        copyTitle.setOnAction(e -> {
            BookDisplay selected = tblBooks.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(selected.getTitle());
                clipboard.setContent(content);
                systemManager.setStatusMessage("Copied: " + selected.getTitle());
            }
        });
        bookContextMenu.getItems().add(copyTitle);
        tblBooks.setContextMenu(bookContextMenu);
        
        // Add row factory to color TAKEN books red
        tblBooks.setRowFactory(tv -> new TableRow<BookDisplay>() {
            @Override
            protected void updateItem(BookDisplay item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setStyle("");
                    getStyleClass().remove("taken-book-row");
                } else {
                    Book book = systemManager.getBookById(item.getId());
                    if (book != null && book.getStatus() == Book.BookStatus.TAKEN) {
                        getStyleClass().add("taken-book-row");
                    } else {
                        getStyleClass().remove("taken-book-row");
                    }
                }
            }
        });
        
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
        
        // Add row factory to color robots by status
        tblRobots.setRowFactory(tv -> new TableRow<RobotDisplay>() {
            @Override
            protected void updateItem(RobotDisplay item, boolean empty) {
                super.updateItem(item, empty);
                
                getStyleClass().removeAll("robot-charging", "robot-low-battery", "robot-busy", "robot-available");
                
                if (empty || item == null) {
                    setStyle("");
                } else {
                    Robot robot = systemManager.getRobotById(item.getId());
                    if (robot != null) {
                        if (robot.isDocked()) {
                            // Charging → Yellow
                            getStyleClass().add("robot-charging");
                        } else if (robot.getCurrentChargePercent() < 15.0f) {
                            // Low battery → Red
                            getStyleClass().add("robot-low-battery");
                        } else if (robot.isBusy()) {
                            // Assigned task → Blue
                            getStyleClass().add("robot-busy");
                        } else {
                            // Available → Green
                            getStyleClass().add("robot-available");
                        }
                    }
                }
            }
        });
        
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
        lblChargingQueue.textProperty().bind(systemManager.chargingQueueSizeProperty().asString());
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
    
    private void startDataRefresh() {
        // Refresh all panels every second for live updates
        Timeline dataRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            refreshBooks();
            refreshRobots();
            refreshShelves();
            refreshChargingStations();
            refreshTasks();
        }));
        dataRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        dataRefreshTimeline.play();
    }
    
    private void loadInitialData() {
        refreshBooks();
        refreshRobots();
        refreshShelves();
        refreshChargingStations();
        refreshTasks();
        refreshLogs();
        
        // Start auto-refresh for all panels
        startDataRefresh();
    }
    
    // Books Management
    @FXML
    private void handleSearchBook() {
        String query = txtSearchBook.getText().trim();
        currentBookFilter = query; // Store the filter
        refreshBooks(); // Apply filter through refresh
        
        if (!query.isEmpty()) {
            systemManager.setStatusMessage("Filtering books: \"" + query + "\"");
        }
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
        ComboBox<String> cmbCategory = new ComboBox<>();
        cmbCategory.getItems().addAll("Fiction", "Science", "History", "Technology", "Literature");
        cmbCategory.setValue("Fiction");
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(txtTitle, 1, 0);
        grid.add(new Label("Author:"), 0, 1);
        grid.add(txtAuthor, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(cmbCategory, 1, 2);
        
        Label lblNote = new Label("Note: Book will be placed on shelf with matching category");
        lblNote.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        grid.add(lblNote, 0, 3, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                systemManager.addBook(txtTitle.getText(), txtAuthor.getText(), cmbCategory.getValue());
                refreshBooks();
                refreshShelves();
            } catch (Exception e) {
                showError("Failed to add book", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleClearSearch() {
        txtSearchBook.clear();
        currentBookFilter = "";
        refreshBooks();
        systemManager.setStatusMessage("Search filter cleared");
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
            Shelf shelf = book.getShelfId() != null ? systemManager.getShelfById(book.getShelfId()) : null;
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Book Details");
            alert.setHeaderText(book.getTitle());
            
            StringBuilder details = new StringBuilder();
            details.append("ID: ").append(book.getId()).append("\n");
            details.append("Title: ").append(book.getTitle()).append("\n");
            details.append("Author: ").append(book.getAuthor()).append("\n");
            details.append("Category: ").append(book.getCategory()).append("\n");
            details.append("Status: ").append(book.getStatus()).append("\n");
            
            if (shelf != null) {
                details.append("Shelf: ").append(shelf.getName()).append(" (").append(shelf.getId()).append(")\n");
                details.append("Distance: ").append(shelf.getDistance()).append(" units\n");
                details.append("Task Time: ").append(shelf.getTaskDurationSeconds()).append(" seconds\n");
                details.append("Battery Cost: ").append(String.format("%.1f", shelf.getTaskBatteryDrain())).append("%");
            } else {
                details.append("Shelf: Not assigned");
            }
            
            alert.setContentText(details.toString());
            alert.showAndWait();
        }
    }
    
    private void refreshBooks() {
        ObservableList<BookDisplay> books = FXCollections.observableArrayList();
        
        // Apply filter if exists
        for (Book book : systemManager.getAllBooks()) {
            if (currentBookFilter.isEmpty()) {
                // No filter - show all books
                books.add(new BookDisplay(book));
            } else {
                // Filter by title or author
                if (book.getTitle().toLowerCase().contains(currentBookFilter.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(currentBookFilter.toLowerCase())) {
                    books.add(new BookDisplay(book));
                }
            }
        }
        
        tblBooks.setItems(books);
        
        // Enable text selection for copying
        tblBooks.setEditable(false);
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
        TextField txtExecDuration = new TextField("15.0");
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(new Label("Exec Duration (s):"), 0, 1);
        grid.add(txtExecDuration, 1, 1);
        grid.add(new Label("Note: Each robot carries 1 book at a time"), 0, 2, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                systemManager.addRobot(
                    txtId.getText(),
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
            
            String carryingInfo = robot.getCarryingBook() != null ? 
                robot.getCarryingBook().getTitle() : "None";
            
            String taskInfo = robot.getCurrentTaskId() != null ?
                robot.getCurrentTaskId() : "None";
            
            alert.setContentText(
                "Battery: " + String.format("%.1f", robot.getCurrentChargePercent()) + "%\n" +
                "Status: " + (robot.isDocked() ? "Charging" : robot.isBusy() ? "Busy" : "Idle") + "\n" +
                "Current Task: " + taskInfo + "\n" +
                "Carrying Book: " + carryingInfo + "\n" +
                "Execution Time: 15 seconds per task\n" +
                "Battery Drain: 5% per task\n" +
                "Auto-Charge Threshold: <15%\n" +
                "Capacity: 1 book at a time"
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
    private void handleViewChargingQueue() {
        List<UnifiedConcurrentSystem.ChargingRequest> queue = 
            systemManager.getConcurrentSystem().getChargingQueue();
        
        if (queue.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Charging Queue");
            alert.setHeaderText("Queue is Empty");
            alert.setContentText("No robots waiting for charging");
            alert.showAndWait();
        } else {
            StringBuilder queueInfo = new StringBuilder();
            queueInfo.append("Robots waiting to charge:\n\n");
            
            for (int i = 0; i < queue.size(); i++) {
                UnifiedConcurrentSystem.ChargingRequest req = queue.get(i);
                queueInfo.append((i + 1)).append(". ")
                    .append(req.getRobot().getId())
                    .append(" - Battery: ")
                    .append(String.format("%.1f", req.getRobot().getCurrentChargePercent()))
                    .append("%\n");
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Charging Queue");
            alert.setHeaderText("Robots in Queue: " + queue.size());
            alert.setContentText(queueInfo.toString());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleStationDetails() {
        String selected = lstChargingStations.getSelectionModel().getSelectedItem();
        if (selected == null || selected.startsWith("  Slot")) {
            showWarning("No Selection", "Please select a station header to view details");
            return;
        }
        
        if (!selected.startsWith("CHG-")) {
            return;
        }
        
        String stationId = selected.split(":")[0];
        ChargingStation station = systemManager.getStationById(stationId);
        if (station != null) {
            StringBuilder details = new StringBuilder();
            details.append("ID: ").append(station.getId()).append("\n");
            details.append("Total Slots: ").append(station.getTotalSlots()).append("\n");
            details.append("Occupied: ").append(station.getOccupiedSlots()).append("\n");
            details.append("Available: ").append(station.getAvailableSlots()).append("\n\n");
            details.append("Slots:\n");
            
            List<Slot> slots = station.getSlots();
            for (int i = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);
                if (slot.getRobot() != null) {
                    Robot robot = slot.getRobot();
                    details.append("Slot ").append(i + 1).append(": ")
                        .append(robot.getId())
                        .append(" (").append(String.format("%.1f", robot.getCurrentChargePercent())).append("%)\n");
                } else {
                    details.append("Slot ").append(i + 1).append(": Empty\n");
                }
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Station Details");
            alert.setHeaderText(station.getName());
            alert.setContentText(details.toString());
            alert.showAndWait();
        }
    }
    
    private void refreshChargingStations() {
        ObservableList<String> stationLines = FXCollections.observableArrayList();
        
        for (ChargingStation station : systemManager.getAllStations()) {
            // Add station header
            stationLines.add(station.getId() + ": " + station.getName() + 
                " (" + station.getOccupiedSlots() + "/" + station.getTotalSlots() + ")");
            
            // Add each slot status
            List<Slot> slots = station.getSlots();
            for (int i = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);
                String slotInfo;
                if (slot.getRobot() != null) {
                    Robot robot = slot.getRobot();
                    slotInfo = "  Slot " + (i + 1) + ": " + robot.getId() + 
                        " (" + String.format("%.1f", robot.getCurrentChargePercent()) + "%)";
                } else {
                    slotInfo = "  Slot " + (i + 1) + ": [Empty]";
                }
                stationLines.add(slotInfo);
            }
        }
        
        lstChargingStations.setItems(stationLines);
        
        // Add cell factory to color stations and slots
        lstChargingStations.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                getStyleClass().removeAll("station-partial", "station-full", "slot-occupied", "slot-empty");
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    if (item.startsWith("CHG-")) {
                        // Station header line
                        String stationId = item.split(":")[0];
                        ChargingStation station = systemManager.getStationById(stationId);
                        
                        if (station != null) {
                            int occupied = station.getOccupiedSlots();
                            int total = station.getTotalSlots();
                            
                            if (occupied == total && total > 0) {
                                // All slots full → Red
                                getStyleClass().add("station-full");
                            } else if (occupied > 0) {
                                // Some slots full → Yellow
                                getStyleClass().add("station-partial");
                            }
                        }
                    } else if (item.contains("Slot")) {
                        // Slot line
                        if (!item.contains("[Empty]")) {
                            // Slot occupied
                            getStyleClass().add("slot-occupied");
                        } else {
                            // Slot empty
                            getStyleClass().add("slot-empty");
                        }
                    }
                }
            }
        });
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
        ComboBox<String> cmbCategory = new ComboBox<>();
        cmbCategory.getItems().addAll("Fiction", "Science", "History", "Technology", "Literature");
        cmbCategory.setValue("Fiction");
        Spinner<Integer> spnDistance = new Spinner<>(10, 50, 30, 5);
        TextField txtCapacity = new TextField("10");
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(cmbCategory, 1, 2);
        grid.add(new Label("Distance (10-50):"), 0, 3);
        grid.add(spnDistance, 1, 3);
        grid.add(new Label("Capacity:"), 0, 4);
        grid.add(txtCapacity, 1, 4);
        
        Label lblNote = new Label("Distance: Time (sec) = Distance, Battery = Distance/2");
        lblNote.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        grid.add(lblNote, 0, 5, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                systemManager.addShelf(
                    txtId.getText(),
                    txtName.getText(),
                    cmbCategory.getValue(),
                    spnDistance.getValue(),
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
            StringBuilder details = new StringBuilder();
            details.append("Category: ").append(shelf.getCategory()).append("\n");
            details.append("Distance: ").append(shelf.getDistance()).append(" units\n");
            details.append("Task Duration: ").append(shelf.getTaskDurationSeconds()).append(" seconds\n");
            details.append("Battery Cost: ").append(String.format("%.1f", shelf.getTaskBatteryDrain())).append("%\n\n");
            details.append("Books (").append(shelf.getCurrentCount()).append("/").append(shelf.getMaxCapacity()).append("):\n");
            
            if (shelf.getBooks().isEmpty()) {
                details.append("- No books on this shelf");
            } else {
                for (Book book : shelf.getBooks()) {
                    details.append("- ").append(book.getTitle()).append(" by ").append(book.getAuthor())
                        .append(" [").append(book.getCategory()).append("]\n");
                }
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Shelf Details");
            alert.setHeaderText(shelf.getName() + " - " + shelf.getCategory());
            alert.setContentText(details.toString());
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
            this.title = book.getTitle() + " [" + book.getCategory() + "]";
            this.author = book.getAuthor();
            
            // Show status in shelf column
            if (book.getStatus() == Book.BookStatus.TAKEN) {
                this.shelf = "[TAKEN]";
            } else if (book.getStatus() == Book.BookStatus.IN_TRANSIT) {
                this.shelf = "[IN TRANSIT]";
            } else {
                this.shelf = book.getShelfId() != null ? book.getShelfId() : "None";
            }
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
            
            if (robot.isDocked()) {
                this.status = "Charging";
            } else if (robot.getCurrentTaskId() != null) {
                this.status = "Executing Task";
            } else if (robot.getCarryingBook() != null) {
                this.status = "Carrying Book";
            } else {
                this.status = "Idle";
            }
            
            if (robot.getCarryingBook() != null) {
                String bookTitle = robot.getCarryingBook().getTitle();
                if (bookTitle.length() > 15) {
                    bookTitle = bookTitle.substring(0, 12) + "...";
                }
                this.load = bookTitle;
            } else {
                this.load = "Empty";
            }
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
            this.name = shelf.getName() + " [" + shelf.getCategory() + "]";
            this.bookCount = String.valueOf(shelf.getCurrentCount());
            this.capacity = shelf.getCurrentCount() + "/" + shelf.getMaxCapacity() + 
                " (D:" + shelf.getDistance() + ")";
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

