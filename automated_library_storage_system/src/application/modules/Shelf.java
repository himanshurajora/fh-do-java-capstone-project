
package application.modules;
import java.util.ArrayList;
import java.util.List;

public class Shelf {
    private String id;
    private String name;
    private int maxCapacity;
    private List<Book> books = new ArrayList<>();

    public Shelf(String id, String name, int maxCapacity) {
        this.id = id;
        this.name = name;
        this.maxCapacity = maxCapacity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    
    public List<Book> getBooks() { return new ArrayList<>(books); }
    
    public int getCurrentCount() { return books.size(); }
    
    public boolean isFull() { return books.size() >= maxCapacity; }
    
    public boolean hasSpace() { return books.size() < maxCapacity; }

    public void addBook(Book book) {
        if (book == null) throw new IllegalArgumentException("book is null");
        if (isFull()) {
            application.Logger.logStorage(id, "ERROR", "Shelf full, cannot add: " + book.getTitle());
            throw new IllegalStateException("Shelf is full");
        }
        books.add(book);
        book.setShelfId(this.id);
        application.Logger.logStorage(id, "INFO", "Book added: " + book.getTitle() + " (" + books.size() + "/" + maxCapacity + ")");
    }

    public void showBooks() {
        for (Book b : books) {
            System.out.println(b);
        }
    }

    public void removeBook(Book book) throws RobotExceptions.InvalidOperationException {
        if (book == null) throw new IllegalArgumentException("book is null");
        boolean removed = books.remove(book);
        if (!removed) {
            application.Logger.logStorage(id, "ERROR", "Remove failed, not found: " + book.getTitle());
            throw new RobotExceptions.InvalidOperationException("Book not on shelf: " + book.getTitle());
        }
        book.setShelfId(null);
        application.Logger.logStorage(id, "INFO", "Book removed: " + book.getTitle() + " (" + books.size() + "/" + maxCapacity + ")");
    }
    
    public Book findBookById(String bookId) {
        for (Book book : books) {
            if (book.getId().equals(bookId)) {
                return book;
            }
        }
        return null;
    }
    
    public Book findBookByTitle(String title) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;
    }
}
