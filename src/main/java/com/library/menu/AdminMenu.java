package com.library.menu;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Role;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.UserService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console menu shown after an ADMIN logs in.
 * Purely input/output + delegation — no SQL and no business rules here.
 */
public class AdminMenu {

    private final Scanner scanner;
    private final BookService bookService = new BookService();
    private final UserService userService = new UserService();
    private final BorrowService borrowService = new BorrowService();

    public AdminMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void show(User admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== ADMIN MENU (" + admin.getName() + ") =====");
            System.out.println("1. Add Book");
            System.out.println("2. View All Books");
            System.out.println("3. Search Book");
            System.out.println("4. Update Quantity");
            System.out.println("5. Delete Book");
            System.out.println("6. Add User");
            System.out.println("7. Active Borrows Report");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addBook();
                    case "2" -> viewAllBooks();
                    case "3" -> searchBook();
                    case "4" -> updateQuantity();
                    case "5" -> deleteBook();
                    case "6" -> addUser();
                    case "7" -> activeBorrowsReport();
                    case "0" -> {
                        System.out.println("Logging out...");
                        running = false;
                    }
                    default -> System.out.println("Invalid option, try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void addBook() throws SQLException {
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Price (Rs.): ");
        BigDecimal price = new BigDecimal(scanner.nextLine().trim());
        System.out.print("Quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine().trim());

        bookService.addBook(isbn, title, author, price, quantity);
        System.out.println("Book added successfully.");
    }

    private void viewAllBooks() throws SQLException {
        List<Book> books = bookService.getAllBooks();
        printBooks(books);
    }

    private void searchBook() throws SQLException {
        System.out.print("Search keyword (title or author): ");
        String keyword = scanner.nextLine().trim();
        printBooks(bookService.searchBooks(keyword));
    }

    private void updateQuantity() throws SQLException {
        System.out.print("ISBN of book to update: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("New quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine().trim());

        bookService.updateQuantity(isbn, quantity);
        System.out.println("Quantity updated.");
    }

    private void deleteBook() throws SQLException {
        System.out.print("ISBN of book to delete: ");
        String isbn = scanner.nextLine().trim();

        bookService.deleteBook(isbn);
        System.out.println("Book deleted.");
    }

    private void addUser() throws SQLException {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Role (ADMIN/BORROWER): ");
        Role role = Role.valueOf(scanner.nextLine().trim().toUpperCase());

        User user = userService.addUser(name, email, password, role);
        System.out.println("User created with id: " + user.getId());
    }

    /**
     * Demonstrates the two DB requirements end-to-end:
     *  - a multi-table JOIN (active borrows with borrower name + book title)
     *  - a GROUP BY (count of active borrows per user)
     */
    private void activeBorrowsReport() throws SQLException {
        System.out.println("\n-- Active Borrows (JOIN: borrow_records + users + books) --");
        List<BorrowRecord> active = borrowService.getActiveBorrowsWithDetails();
        if (active.isEmpty()) {
            System.out.println("No active borrows.");
        } else {
            for (BorrowRecord record : active) {
                System.out.printf(
                    "Record #%d | Borrower: %-20s | Book: %-30s | Due: %s%n",
                    record.getId(), record.getUser().getName(), record.getBook().getTitle(), record.getDueDate()
                );
            }
        }

        System.out.println("\n-- Active Borrow Count per User (GROUP BY) --");
        Map<String, Integer> counts = borrowService.getActiveBorrowCountPerUser();
        if (counts.isEmpty()) {
            System.out.println("No active borrows.");
        } else {
            counts.forEach((name, count) -> System.out.printf("%-20s : %d%n", name, count));
        }
    }

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        for (Book book : books) {
            System.out.println(book);
        }
    }
}
