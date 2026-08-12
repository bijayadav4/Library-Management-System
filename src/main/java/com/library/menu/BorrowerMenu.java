package com.library.menu;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.BorrowService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Console menu shown after a BORROWER logs in.
 */
public class BorrowerMenu {

    private final Scanner scanner;
    private final BookService bookService = new BookService();
    private final BorrowService borrowService = new BorrowService();

    public BorrowerMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void show(User borrower) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== BORROWER MENU (" + borrower.getName() + ") =====");
            System.out.println("1. View Books");
            System.out.println("2. Search Book");
            System.out.println("3. Borrow Book");
            System.out.println("4. Return Book");
            System.out.println("5. View My Borrowed Books");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> viewBooks();
                    case "2" -> searchBook();
                    case "3" -> borrowBook(borrower);
                    case "4" -> returnBook(borrower);
                    case "5" -> viewMyBorrowedBooks(borrower);
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

    private void viewBooks() throws SQLException {
        printBooks(bookService.getAllBooks());
    }

    private void searchBook() throws SQLException {
        System.out.print("Search keyword (title or author): ");
        String keyword = scanner.nextLine().trim();
        printBooks(bookService.searchBooks(keyword));
    }

    private void borrowBook(User borrower) throws SQLException {
        System.out.print("ISBN of book to borrow: ");
        String isbn = scanner.nextLine().trim();

        BorrowRecord record = borrowService.borrowBook(borrower, isbn);
        System.out.println("Borrowed successfully. Due date: " + record.getDueDate());
    }

    private void returnBook(User borrower) throws SQLException {
        System.out.print("Borrow record ID to return: ");
        int recordId = Integer.parseInt(scanner.nextLine().trim());

        int lateFee = borrowService.returnBook(recordId, borrower);
        if (lateFee > 0) {
            System.out.println("Book returned. Late fee due: Rs." + lateFee);
        } else {
            System.out.println("Book returned on time. No late fee.");
        }
    }

    private void viewMyBorrowedBooks(User borrower) throws SQLException {
        List<BorrowRecord> records = borrowService.getBorrowedBooksForUser(borrower.getId());
        if (records.isEmpty()) {
            System.out.println("You have not borrowed any books yet.");
            return;
        }
        for (BorrowRecord record : records) {
            System.out.println(record);
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
