# Automated Library Storage System - UI Application

## Overview

This is a comprehensive JavaFX-based GUI application for managing an automated library storage system with robots, charging stations, shelves, and books. The system features concurrent task execution, automatic robot charging, and real-time monitoring.

## Features

### Single Unified Dashboard
- **System Statistics Bar**: Real-time display of total books, available/busy/charging robots, task queue status, and completion metrics
- **Books Management**: Search, add, and view books with automatic shelf assignment
- **Task Management**: Create "Get Book" and "Return Book" tasks that are automatically assigned to available robots
- **Equipment Monitoring**: View all robots with battery levels, status, and current load
- **Charging Stations**: Monitor charging station status and slot occupancy
- **Shelves Overview**: View all shelves with capacity and book count
- **Live Logs**: Real-time log display with scope filtering (SYSTEM, TASKS, RESOURCES, STORAGE, COMMON)
- **Configurable Settings**: Adjust battery threshold and log refresh interval
- **Status Bar**: Real-time status messages and system time

### Key Capabilities

1. **Automatic Robot Assignment**: Tasks are automatically assigned to available robots based on battery level and availability
2. **Automatic Charging**: Robots with battery < 15% (configurable) are automatically sent to charging stations
3. **Concurrent Task Execution**: Multiple robots can execute tasks simultaneously using the UnifiedConcurrentSystem
4. **Persistent State**: All system state (books, shelves, robots, stations) is saved to `store.json` and loaded on startup
5. **Real-time Updates**: UI updates every 500ms to reflect current system state
6. **Log Monitoring**: Logs refresh every 2 seconds (configurable) with scope-based filtering

## Architecture

### Core Components

1. **LibrarySystemManager**: Coordinates all system components
   - Manages Library, UnifiedConcurrentSystem, and SystemState
   - Provides Observable properties for UI binding
   - Handles auto-save every 30 seconds
   - Coordinates between UI and business logic

2. **UnifiedConcurrentSystem**: Handles concurrent robot and task execution
   - Thread pool for charging operations
   - Thread pool for task execution
   - Automatic robot assignment
   - Queue management for tasks and charging

3. **SystemState**: JSON-based persistence
   - Saves/loads books, shelves, robots, stations, and configuration
   - Uses Gson for serialization
   - Default state creation on first run

4. **LibraryApplication**: JavaFX application entry point
   - Loads FXML layout
   - Handles application lifecycle

5. **DashboardController**: UI controller
   - Binds UI components to LibrarySystemManager properties
   - Handles all user interactions
   - Manages timelines for clock and log updates

### Data Flow

```
User Action → DashboardController → LibrarySystemManager → 
    ├─ Library (Books, Shelves, Stations)
    ├─ UnifiedConcurrentSystem (Robots, Tasks)
    └─ SystemState (Persistence)
```

## Enhanced Modules

### Book
- Added ID, shelfId, and weight tracking
- Automatic logging of operations

### Shelf
- Added ID, name, and max capacity (10 books)
- Track books and check capacity
- Find books by ID or title

### ChargingStation
- Added ID, name, and dynamic slot management
- Track occupied vs available slots
- Robot plug-in/plug-out with validation

### Robot
- Battery monitoring and auto-charging
- Book carrying with weight/count validation
- Integration with concurrent system

## User Interface Guide

### Books Management Panel
- **Search**: Type book title or author and click "Search"
- **Add Book**: Click "Add Book", enter details (title, author, weight)
- **View Details**: Select a book and click "View Details"

### Task Management Panel
- **Get Book**: Enter book title and click "Get Book" to create a retrieval task
- **Return Book**: Enter book title and click "Return Book" to create a return task
- **Active Tasks Table**: Shows tasks that are pending or in progress

### Equipment Status Panel
- **Robots Table**: Shows all robots with:
  - Battery percentage
  - Status (Idle/Busy/Charging)
  - Current load vs max capacity
- **Add Robot**: Configure and add new robots to the system
- **Details**: View detailed robot information

### Charging Stations Panel
- **Station List**: Shows each station with occupied/total slots
- **Add Station**: Create new charging stations
- **View Details**: See slot availability and status

### Shelves Panel
- **Shelves Table**: Shows all shelves with:
  - Book count
  - Capacity (current/max)
  - Status (Empty/Available/Full)
- **Add Shelf**: Create new shelves (max capacity: 10 books)
- **View Books**: See all books on a selected shelf

### Logs Panel
- **Filter**: Select scope (ALL, SYSTEM, TASKS, RESOURCES, STORAGE, COMMON)
- **Refresh**: Manual log refresh (auto-refreshes based on settings)
- **Table**: Shows time and message for last 100 log entries

### Settings Panel
- **Battery Threshold**: Configure when robots should charge (5-50%)
- **Log Refresh Interval**: Set how often logs update (1-10 seconds)
- **Save Config**: Apply settings changes
- **Save State**: Manually save system state to JSON

## Configuration

### SystemConfig (Configurable from UI)
- `batteryThreshold`: 15.0% (when robots auto-charge)
- `logRefreshInterval`: 2 seconds
- `numChargingStations`: 3
- `numSlotsPerStation`: 3
- `maxShelfCapacity`: 10 books

### Default System State
On first run, the system creates:
- **5 Shelves**: Named A-E, capacity 10 each
- **3 Charging Stations**: 3 slots each
- **5 Robots**: 100% battery, 5kg max weight, 10 books max
- **10 Books**: Classic literature titles distributed across shelves

## File Structure

```
application/
├── Main.java                      # Entry point (launches LibraryApplication)
├── LibraryApplication.java        # JavaFX Application class
├── DashboardController.java       # UI Controller
├── LibrarySystemManager.java      # System coordinator
├── SystemState.java               # JSON persistence
├── SystemConfig.java              # Configuration holder
├── Logger.java                    # Logging system
├── LibraryDashboard.fxml          # UI layout
├── Dashboard.css                  # Styling
└── modules/
    ├── Library.java               # Main library container
    ├── Book.java                  # Enhanced with ID, shelfId, weight
    ├── Shelf.java                 # Enhanced with ID, name, capacity
    ├── Robot.java                 # AGV with battery management
    ├── ChargingStation.java       # Enhanced with ID, slots
    ├── Task.java                  # Task with lifecycle
    ├── TaskManager.java           # Task coordination
    ├── UnifiedConcurrentSystem.java  # Concurrent execution
    └── [other modules...]

Data Files:
└── automated_library_storage_system/
    ├── store.json                 # Persistent state (auto-created)
    └── logs/                      # Log files by scope and date
```

## Running the Application

### Prerequisites
1. Java 11 or higher
2. JavaFX 11 or higher
3. Gson 2.10.1 or higher

See `DEPENDENCIES.md` for detailed setup instructions.

### Launch
Run `Main.java` as a Java Application. The system will:
1. Load or create default state from `store.json`
2. Initialize all components
3. Start the JavaFX UI
4. Begin monitoring and auto-save tasks

## System Behavior

### Task Execution Flow
1. User creates a task (Get/Return Book)
2. Task is added to UnifiedConcurrentSystem queue
3. System finds an available robot with sufficient battery (≥20%)
4. Robot is assigned to the task
5. Task executes (simulated 5-second execution)
6. Robot is released:
   - If battery < threshold: sent to charging
   - Otherwise: returned to available pool

### Charging Flow
1. Robot battery drops below threshold (default 15%)
2. Robot is sent to charging queue
3. When a charging slot is available:
   - Robot is plugged in
   - Battery increases 1% per 100ms
   - Robot is charged to 100%
4. Robot is unplugged and returned to available pool

### Auto-Save
- System state is saved every 30 seconds
- Manual save available via "Save State" button
- State includes all books, shelves, robots, stations, and config

## Logs

Logs are organized by scope and date:
- `system-DD-MM-YYYY.log`: System-level events
- `tasks-DD-MM-YYYY.log`: Task lifecycle events
- `resources-DD-MM-YYYY.log`: Robot and charging events
- `storage-DD-MM-YYYY.log`: Book and shelf operations
- `common-DD-MM-YYYY.log`: General operations

## Troubleshooting

### UI doesn't load
- Ensure JavaFX is properly configured
- Check `LibraryDashboard.fxml` exists
- Verify Dashboard.css is accessible

### State not saving
- Check write permissions for `automated_library_storage_system/` directory
- Verify Gson library is in classpath
- Check logs for error messages

### Robots not executing tasks
- Verify robots have sufficient battery (≥20%)
- Check if charging stations have available slots
- Review RESOURCES scope logs for errors

### Books not appearing
- Check if shelves have available capacity
- Verify book was added successfully via logs
- Try refreshing the books table

## Future Enhancements

- Drag-and-drop book organization
- Graphical visualization of robot movement
- Historical analytics and reporting
- User authentication and roles
- Book reservation system
- Barcode/RFID integration

## Support

Check logs in `automated_library_storage_system/logs/` for detailed error information.
All operations are logged with timestamps and scope for debugging.

