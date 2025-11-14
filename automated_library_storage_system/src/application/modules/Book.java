package application.modules;

public class Book {
    public enum BookStatus {
        AVAILABLE,
        IN_TRANSIT,
        TAKEN
    }
    
    private String id;
    private String title;
    private String author;
    private String category;
    private String shelfId;
    private BookStatus status;
    private String assignedRobotId;

    public Book(String id, String title, String author, String category) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("id is null/empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title is null/empty");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("author is null/empty");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("category is null/empty");
        }
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.status = BookStatus.AVAILABLE;
        application.Logger.logStorage(id, "INFO", "Book created: " + title + " by " + author + " [" + category + "]");
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getShelfId() { return shelfId; }
    public void setShelfId(String shelfId) { this.shelfId = shelfId; }
    
    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { 
        this.status = status;
        application.Logger.logStorage(id, "INFO", "Book status changed to: " + status);
    }
    
    public String getAssignedRobotId() { return assignedRobotId; }
    public void setAssignedRobotId(String assignedRobotId) { this.assignedRobotId = assignedRobotId; }
    
    public boolean isAvailable() {
        return status == BookStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return title + " by " + author + " [" + category + ", " + status + "]";
    }
}
