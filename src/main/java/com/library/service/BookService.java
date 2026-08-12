package com.library.service;

import com.library.model.Book;
import com.library.repository.BookRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Business rules around books sit here. The menu layer never talks to
 * BookRepository directly — it always goes through this service.
 */
public class BookService {

    private final BookRepository bookRepository = new BookRepository();

    public void addBook(String isbn, String title, String author, BigDecimal price, int quantity)
            throws SQLException {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be empty.");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (bookRepository.findByIsbn(isbn).isPresent()) {
            throw new IllegalArgumentException("A book with this ISBN already exists.");
        }
        bookRepository.addBook(new Book(isbn, title, author, price, quantity));
    }

    public List<Book> getAllBooks() throws SQLException {
        return bookRepository.getAllBooks();
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        return bookRepository.searchByTitleOrAuthor(keyword);
    }

    public Optional<Book> findByIsbn(String isbn) throws SQLException {
        return bookRepository.findByIsbn(isbn);
    }

    public void updateQuantity(String isbn, int newQuantity) throws SQLException {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (bookRepository.findByIsbn(isbn).isEmpty()) {
            throw new IllegalArgumentException("No book found with ISBN: " + isbn);
        }
        bookRepository.updateQuantity(isbn, newQuantity);
    }

    public void deleteBook(String isbn) throws SQLException {
        boolean deleted = bookRepository.deleteBook(isbn);
        if (!deleted) {
            throw new IllegalArgumentException("No book found with ISBN: " + isbn);
        }
    }
}
