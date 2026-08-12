package com.library.repository;

import com.library.database.DBConnection;
import com.library.model.Book;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * All SQL for the `books` table lives here. Every method opens its own
 * connection with try-with-resources, so nothing is left dangling.
 */
public class BookRepository {

    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, price, quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setBigDecimal(4, book.getPrice());
            stmt.setInt(5, book.getQuantity());
            stmt.executeUpdate();
        }
    }

    public List<Book> getAllBooks() throws SQLException {
        String sql = "SELECT isbn, title, author, price, quantity FROM books ORDER BY title";
        List<Book> books = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(mapRow(rs));
            }
        }
        return books;
    }

    public Optional<Book> findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT isbn, title, author, price, quantity FROM books WHERE isbn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Simple case-insensitive partial match on title OR author.
     */
    public List<Book> searchByTitleOrAuthor(String keyword) throws SQLException {
        String sql = "SELECT isbn, title, author, price, quantity FROM books " +
                     "WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? ORDER BY title";
        List<Book> books = new ArrayList<>();
        String pattern = "%" + keyword.toLowerCase() + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        }
        return books;
    }

    public void updateQuantity(String isbn, int newQuantity) throws SQLException {
        String sql = "UPDATE books SET quantity = ? WHERE isbn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newQuantity);
            stmt.setString(2, isbn);
            stmt.executeUpdate();
        }
    }

    public boolean deleteBook(String isbn) throws SQLException {
        String sql = "DELETE FROM books WHERE isbn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            return stmt.executeUpdate() > 0;
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
            rs.getString("isbn"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getBigDecimal("price"),
            rs.getInt("quantity")
        );
    }
}
