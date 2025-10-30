package application;

public class LogEntry {
    private String timestamp;
    private String task;

    public LogEntry(String timestamp, String task) {
        this.timestamp = timestamp;
        this.task = task;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTask() {
        return task;
    }
}
