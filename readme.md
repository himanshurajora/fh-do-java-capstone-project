# Automated Library Storage System

An automated library management system that uses robots to retrieve and return books from shelves. The system features a JavaFX dashboard for real-time monitoring and control of books, robots, tasks, and charging stations.

## Features

### Book Management
- Add new books with title, author, and category
- Search and filter books by title or author
- View detailed book information including status and shelf location
- Books are automatically organized on shelves by category
- Track book status: Available, Taken, Reserved

### Robot Management
- Multiple robots that can retrieve and return books
- Real-time battery level monitoring for each robot
- Robots automatically charge when battery falls below threshold
- Each robot can carry one book at a time
- Monitor robot status: Idle, Busy, or Charging
- Add and manage robots dynamically

### Task Management
- Create tasks to retrieve books from shelves
- Create tasks to return books to shelves
- Task queue system with priority levels
- Real-time task status tracking (Pending, In Progress, Completed, Failed)
- Automatic task assignment to available robots
- Task duration and battery requirement calculation based on shelf distance

### Shelf Management
- Multiple shelves organized by category (Fiction, Science, History, Technology, Literature)
- Track shelf capacity and occupancy
- View books stored on each shelf
- Shelves have different distances affecting task duration and battery drain

### Charging Station Management
- Multiple charging stations with configurable slots
- Robots automatically queue for charging when battery is low
- Monitor charging queue and station availability
- Add and manage charging stations

### Dashboard Features
- Real-time clock and system time display
- Live statistics: total books, robot availability, task counts
- Searchable book table with copy-to-clipboard functionality
- Task monitoring table
- Robot status table with battery levels
- Shelf overview table
- System logs with filtering by scope (System, Tasks, Resources)
- Configurable settings: battery threshold and log refresh interval
- System state persistence to JSON file

## Setup Instructions

### Prerequisites
- Java JDK 11 or higher
- Eclipse IDE
- JavaFX SDK 21.0.8
- Gson library (version 2.10.1 or compatible)

### Step 1: Download JavaFX SDK 21.0.8

1. Go to https://openjfx.io/
2. Download JavaFX SDK 21.0.8 for your operating system (Linux, Windows, or macOS)
3. Extract the downloaded archive to a location on your computer (for example: `/home/username/libs/javafx-sdk-21.0.8`)

### Step 2: Download Gson Library

1. Go to https://mvnrepository.com/artifact/com.google.code.gson/gson/2.10.1
2. Download the JAR file (gson-2.10.1.jar)
3. Save it to a location on your computer (for example: `/home/username/libs/gson/gson-2.10.1.jar`)

### Step 3: Import Project into Eclipse

1. Open Eclipse IDE
2. Go to File > Import
3. Select General > Existing Projects into Workspace
4. Click Next
5. Browse to the project folder: `automated_library_storage_system`
6. Make sure the project is checked in the projects list
7. Click Finish

### Step 4: Configure JavaFX User Library

1. In Eclipse, go to Window > Preferences
2. Navigate to Java > Build Path > User Libraries
3. Click New to create a new user library
4. Name it "javafx" (all lowercase)
5. Click Add External JARs
6. Navigate to your JavaFX SDK folder and select all JAR files from the `lib` folder:
   - javafx.base.jar
   - javafx.controls.jar
   - javafx.fxml.jar
   - javafx.graphics.jar
   - javafx.media.jar (optional)
   - javafx.swing.jar (optional)
   - javafx.web.jar (optional)
7. For each JAR file, right-click and select Properties
8. Check "Module" in the properties dialog
9. Click OK to save the user library

### Step 5: Add Gson Library to Project

1. Right-click on the project in Package Explorer
2. Select Properties
3. Go to Java Build Path > Libraries tab
4. Click Add External JARs
5. Navigate to where you saved gson-2.10.1.jar and select it
6. Click OK

### Step 6: Configure Run Configuration

1. Right-click on the project in Package Explorer
2. Select Run As > Run Configurations
3. If a configuration doesn't exist, right-click "Java Application" and select New Configuration
4. Set the following:
   - Name: Automated Library Storage System
   - Project: automated_library_storage_system
   - Main class: application.Main
5. Go to the Arguments tab
6. In the VM arguments field, add:
   ```
   --module-path <javafx lib path> --add-modules javafx.controls,javafx.fxml
   ```
   Replace `<javafx lib path>` with the actual path to your JavaFX lib folder.
   
   Example for Linux:
   ```
   --module-path /home/username/libs/javafx-sdk-21.0.8/lib --add-modules javafx.controls,javafx.fxml
   ```
   
   Example for Windows:
   ```
   --module-path "C:\libs\javafx-sdk-21.0.8\lib: --add-modules javafx.controls,javafx.fxml
   ```
   
7. Click Apply and then Run

### Step 7: Run the Application

1. Make sure the run configuration is set up correctly
2. Click the Run button (green play icon) or press Ctrl+F11
3. The application window should open showing the Library Dashboard

## Troubleshooting

### JavaFX Module Not Found Error
- Make sure the VM arguments are correctly set in the run configuration
- Verify the JavaFX lib path is correct and points to the lib folder containing the JAR files
- Ensure all required JavaFX modules are included in the --add-modules argument

### Gson Class Not Found Error
- Verify that gson-2.10.1.jar is added to the project's build path
- Check that the JAR file is not corrupted

### Application Won't Start
- Check that the main class is set to `application.Main`
- Verify Java version is 11 or higher
- Check the Console view in Eclipse for error messages

## Project Structure

- `src/application/` - Main application code
  - `Main.java` - Entry point
  - `LibraryApplication.java` - JavaFX application class
  - `DashboardController.java` - Main dashboard controller
  - `LibrarySystemManager.java` - Core system manager
  - `modules/` - System modules (Book, Robot, Shelf, Task, etc.)
- `automated_library_storage_system/store.json` - System state persistence file

## Notes

- The system automatically saves its state to `store.json` when changes are made
- System state is loaded automatically when the application starts
- Robots automatically charge when battery falls below the configured threshold
- Tasks are automatically assigned to available robots based on priority and battery level
