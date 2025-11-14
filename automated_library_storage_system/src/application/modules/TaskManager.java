package application.modules;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskManager {
    private List<Task> tasks;
    private List<String> systemLogs;

    public TaskManager() {
        this.tasks = new ArrayList<>();
        this.systemLogs = new ArrayList<>();
    }

    public Task createTask(String taskName, String description, TaskPriority priority, String assignedTo) {
        String taskId = "TASK_" + System.currentTimeMillis();
        Task task = new Task(taskId, taskName, description, priority, assignedTo);
        tasks.add(task);
        logSystemActivity("TASK_CREATED", "New task created: " + taskName + " assigned to " + assignedTo);
        return task;
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByPriority(TaskPriority priority) {
        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByAssignee(String assignee) {
        return tasks.stream()
                .filter(task -> task.getAssignedTo().equals(assignee))
                .collect(Collectors.toList());
    }

    public Task getTaskById(String taskId) {
        return tasks.stream()
                .filter(task -> task.getTaskId().equals(taskId))
                .findFirst()
                .orElse(null);
    }

    public void startTask(String taskId) throws RobotExceptions.TaskNotFoundException {
        Task task = getTaskById(taskId);
        if (task == null) {
            logSystemActivity("ERROR", "Task " + taskId + " not found");
            throw new RobotExceptions.TaskNotFoundException("Task not found: " + taskId);
        }
        task.startTask();
        logSystemActivity("TASK_STARTED", "Task " + taskId + " started");
    }

    public void completeTask(String taskId) throws RobotExceptions.TaskNotFoundException {
        Task task = getTaskById(taskId);
        if (task == null) {
            logSystemActivity("ERROR", "Task " + taskId + " not found");
            throw new RobotExceptions.TaskNotFoundException("Task not found: " + taskId);
        }
        task.completeTask();
        logSystemActivity("TASK_COMPLETED", "Task " + taskId + " completed");
    }

    public void cancelTask(String taskId) throws RobotExceptions.TaskNotFoundException {
        Task task = getTaskById(taskId);
        if (task == null) {
            logSystemActivity("ERROR", "Task " + taskId + " not found");
            throw new RobotExceptions.TaskNotFoundException("Task not found: " + taskId);
        }
        task.cancelTask();
        logSystemActivity("TASK_CANCELLED", "Task " + taskId + " cancelled");
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    private void logSystemActivity(String action, String description) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] SYSTEM_%s: %s", timestamp, action, description);
        systemLogs.add(logEntry);
        application.Logger.logTasks("INFO", logEntry);
    }

    public List<String> getSystemLogs() {
        return new ArrayList<>(systemLogs);
    }

    public int getTaskCount() {
        return tasks.size();
    }

    public int getTaskCountByStatus(TaskStatus status) {
        return (int) tasks.stream().filter(task -> task.getStatus() == status).count();
    }
}