package application.modules;

public class Book {
    private String id;
    private String title;
    private String author;
    private float weightKg;
    private String shelfId;

    public Book(String id, String title, String author, float weightKg) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("id is null/empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title is null/empty");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("author is null/empty");
        }
        this.id = id;
        this.title = title;
        this.author = author;
        this.weightKg = weightKg;
        application.Logger.logStorage(id, "INFO", "Book created: " + title + " by " + author);
    }

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

    @Override
    public String toString() {
        return title + " by " + author;
    }
}
