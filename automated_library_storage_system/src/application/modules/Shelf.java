
package application.modules;
import java.util.ArrayList;
import java.util.List;

public class Shelf {
    private List<Book> books = new ArrayList<>();

    public void addBook(Book book) {
        if (book == null) throw new IllegalArgumentException("book is null");
        books.add(book);
        application.Logger.logStorage("shelf", "INFO", "Book added: " + book.getTitle());
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
            application.Logger.logStorage("shelf", "ERROR", "Remove failed, not found: " + book.getTitle());
            throw new RobotExceptions.InvalidOperationException("Book not on shelf: " + book.getTitle());
        }
        application.Logger.logStorage("shelf", "INFO", "Book removed: " + book.getTitle());
    }
}
