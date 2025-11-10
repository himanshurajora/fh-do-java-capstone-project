package application.modules;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private String taskId;
    private String taskName;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private List<String> logEntries;


    public Task(String taskId, String taskName, String description, TaskPriority priority, String assignedTo) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.description = description;
        this.priority = priority;
        this.status = TaskStatus.PENDING;
        this.assignedTo = assignedTo;
        this.createdAt = LocalDateTime.now();
        this.dueDate = createdAt.plusDays(7);
        this.logEntries = new ArrayList<>();
        logActivity("TASK_CREATED", "Task created and assigned to " + assignedTo);
    }

    public void startTask() {
        if (status == TaskStatus.PENDING) {
            status = TaskStatus.IN_PROGRESS;
            logActivity("TASK_STARTED", "Task started by " + assignedTo);
        } else {
            logActivity("ERROR", "Cannot start task - current status: " + status);
        }
    }

    public void completeTask() {
        if (status == TaskStatus.IN_PROGRESS) {
            status = TaskStatus.COMPLETED;
            completedAt = LocalDateTime.now();
            logActivity("TASK_COMPLETED", "Task completed by " + assignedTo);
        } else {
            logActivity("ERROR", "Cannot complete task - current status: " + status);
        }
    }

    public void cancelTask() {
        status = TaskStatus.CANCELLED;
        logActivity("TASK_CANCELLED", "Task cancelled");
    }

    public void updatePriority(TaskPriority newPriority) {
        TaskPriority oldPriority = this.priority;
        this.priority = newPriority;
        logActivity("PRIORITY_CHANGED", "Priority changed from " + oldPriority + " to " + newPriority);
    }

    public void reassignTask(String newAssignee) {
        String oldAssignee = this.assignedTo;
        this.assignedTo = newAssignee;
        logActivity("TASK_REASSIGNED", "Task reassigned from " + oldAssignee + " to " + newAssignee);
    }

    private void logActivity(String action, String description) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] %s: %s", timestamp, action, description);
        logEntries.add(logEntry);
        // Also write to tasks scope log file
        application.Logger.logTasks("INFO", logEntry);
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public List<String> getLogEntries() {
        return new ArrayList<>(logEntries);
    }

    @Override
    public String toString() {
        return String.format("Task[ID: %s, Name: %s, Priority: %s, Status: %s, AssignedTo: %s]",
                taskId, taskName, priority, status, assignedTo);
    }

	public List<Book> getBooks() {
		return new ArrayList<>();
	}
}