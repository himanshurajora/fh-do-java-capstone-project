package application.modules;

public class Book {
    private String title;
    private String author;

    public Book(String title, String author) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title is null/empty");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("author is null/empty");
        }
        this.title = title;
        this.author = author;
        application.Logger.logStorage(title, "INFO", "Book created: " + title + " by " + author);
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }

    @Override
    public String toString() {
        return title + " by " + author;
    }

	public float getWeightKg() {
		// TODO Auto-generated method stub
		return 0;
	}
}
