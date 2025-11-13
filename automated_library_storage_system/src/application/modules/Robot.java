package application.modules;

import application.modules.RobotExceptions.LowBatteryException;
import application.modules.RobotExceptions.OverloadException;
import application.modules.RobotExceptions.TaskNotFoundException;

public class Robot extends Resource {
    private static final int MAX_BOOKS_PER_ROBOT = 1; // Only 1 book at a time
    
    private float currentChargePercent = 100;
    private float batteryThreshold = 15.0f; // Default threshold, can be set from config
    private boolean isDocked = false;
    private Book carryingBook = null;
    private String currentTaskId = null;

    public Robot(String id, float executionDuration) {
        super(id, executionDuration);
    }

    public boolean canCarry() {
        return carryingBook == null && currentTaskId == null;
    }

    public boolean needsCharging(float thresholdPercent) {
        return currentChargePercent < thresholdPercent;
    }

    public void dock() {
        isDocked = true;
        application.Logger.logResources(getId(), "INFO", "Docked for charging");
    }

    public void undock() {
        isDocked = false;
        currentChargePercent = 100;
        application.Logger.logResources(getId(), "INFO", "Undocked - charged to 100%");
    }

    public void pickUpBook(Book book) {
        if (carryingBook != null) {
            throw new IllegalStateException("Robot already carrying a book");
        }
        this.carryingBook = book;
        application.Logger.logResources(getId(), "INFO", "Picked up book: " + book.getTitle());
    }

    public Book deliverBook() {
        Book book = this.carryingBook;
        this.carryingBook = null;
        if (book != null) {
            application.Logger.logResources(getId(), "INFO", "Delivered book: " + book.getTitle());
        }
        return book;
    }

    public void execute(Task task) throws RobotExceptions {
        try {
            if (task == null) {
                throw new TaskNotFoundException("Task is null");
            }
            if (carryingBook != null) {
                throw new OverloadException("Robot already carrying a book");
            }
            if (needsCharging(batteryThreshold)) {
                throw new LowBatteryException("Battery too low");
            }
            
            currentTaskId = task.getTaskId();
            application.Logger.logResources(getId(), "INFO", "Executing task: " + task.getTaskId());
            
        } catch (TaskNotFoundException | OverloadException | LowBatteryException e) {
            application.Logger.logResources(getId(), "ERROR", "Execution failed: " + e.getMessage());
            throw new RobotExceptions("Robot execution failed", e);
        } catch (Exception e) {
            application.Logger.logResources(getId(), "ERROR", "Execution error: " + e.getMessage());
            throw new RobotExceptions("Error occurred during execution", e);
        }
    }
    
    public void completeTask() {
        currentTaskId = null;
        application.Logger.logResources(getId(), "INFO", "Task completed");
    }

    public String getId() {
        return super.getId();
    }

    public float getCurrentChargePercent() {
        return currentChargePercent;
    }

    public void setCurrentChargePercent(float currentChargePercent) {
        this.currentChargePercent = currentChargePercent;
    }

    public float getBatteryThreshold() {
        return batteryThreshold;
    }

    public void setBatteryThreshold(float batteryThreshold) {
        this.batteryThreshold = batteryThreshold;
    }

    public boolean isDocked() {
        return isDocked;
    }

    public void setDocked(boolean docked) {
        isDocked = docked;
    }

    public Book getCarryingBook() {
        return carryingBook;
    }

    public String getCurrentTaskId() {
        return currentTaskId;
    }
    
    public boolean isBusy() {
        return currentTaskId != null || carryingBook != null;
    }
}
