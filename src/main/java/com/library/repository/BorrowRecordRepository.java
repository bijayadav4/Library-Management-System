package com.library.repository;

import com.library.database.DBConnection;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.BorrowStatus;
import com.library.model.Role;
import com.library.model.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * All SQL for the `borrow_records` table, including the joined/aggregate
 * queries required by the assignment:
 *   - findActiveBorrowsWithDetails()  -> multi-table JOIN
 *   - countActiveBorrowsPerUser()     -> GROUP BY
 */
public class BorrowRecordRepository {

    public int createBorrowRecord(BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records (user_id, isbn, borrow_date, due_date, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, record.getUser().getId());
            stmt.setString(2, record.getBook().getIsbn());
            stmt.setDate(3, java.sql.Date.valueOf(record.getBorrowDate()));
            stmt.setDate(4, java.sql.Date.valueOf(record.getDueDate()));
            stmt.setString(5, record.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public void markReturned(int recordId, LocalDate returnDate) throws SQLException {
        String sql = "UPDATE borrow_records SET return_date = ?, status = 'RETURNED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(returnDate));
            stmt.setInt(2, recordId);
            stmt.executeUpdate();
        }
    }

    public Optional<BorrowRecord> findById(int id) throws SQLException {
        String sql = "SELECT br.id, br.borrow_date, br.due_date, br.return_date, br.status, " +
                     "       u.id AS user_id, u.name, u.email, u.password, u.role, " +
                     "       b.isbn, b.title, b.author, b.price, b.quantity " +
                     "FROM borrow_records br " +
                     "JOIN users u ON br.user_id = u.id " +
                     "JOIN books b ON br.isbn = b.isbn " +
                     "WHERE br.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapFullRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * All borrow records (active + returned) belonging to one user,
     * newest first. Used for "View My Borrowed Books".
     */
    public List<BorrowRecord> findByUserId(int userId) throws SQLException {
        String sql = "SELECT br.id, br.borrow_date, br.due_date, br.return_date, br.status, " +
                     "       u.id AS user_id, u.name, u.email, u.password, u.role, " +
                     "       b.isbn, b.title, b.author, b.price, b.quantity " +
                     "FROM borrow_records br " +
                     "JOIN users u ON br.user_id = u.id " +
                     "JOIN books b ON br.isbn = b.isbn " +
                     "WHERE br.user_id = ? " +
                     "ORDER BY br.borrow_date DESC";
        List<BorrowRecord> records = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapFullRow(rs));
                }
            }
        }
        return records;
    }

    /**
     * REQUIRED MULTI-TABLE JOIN QUERY.
     * All currently active borrows, with the borrower's name and the
     * book's title pulled in from their respective tables.
     */
    public List<BorrowRecord> findActiveBorrowsWithDetails() throws SQLException {
        String sql = "SELECT br.id, br.borrow_date, br.due_date, br.return_date, br.status, " +
                     "       u.id AS user_id, u.name, u.email, u.password, u.role, " +
                     "       b.isbn, b.title, b.author, b.price, b.quantity " +
                     "FROM borrow_records br " +
                     "JOIN users u ON br.user_id = u.id " +
                     "JOIN books b ON br.isbn = b.isbn " +
                     "WHERE br.status = 'ACTIVE' " +
                     "ORDER BY br.due_date";
        List<BorrowRecord> records = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                records.add(mapFullRow(rs));
            }
        }
        return records;
    }

    /**
     * REQUIRED GROUP BY QUERY.
     * Count of currently active borrows per user. Returns a map of
     * user name -> active borrow count, ordered by count descending.
     */
    public Map<String, Integer> countActiveBorrowsPerUser() throws SQLException {
        String sql = "SELECT u.name, COUNT(*) AS active_borrow_count " +
                     "FROM borrow_records br " +
                     "JOIN users u ON br.user_id = u.id " +
                     "WHERE br.status = 'ACTIVE' " +
                     "GROUP BY u.name " +
                     "ORDER BY active_borrow_count DESC";
        Map<String, Integer> counts = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("name"), rs.getInt("active_borrow_count"));
            }
        }
        return counts;
    }

    /**
     * Whether a given user already has this book on active loan
     * (used to stop the same borrower double-borrowing the same title).
     */
    public boolean hasActiveBorrow(int userId, String isbn) throws SQLException {
        String sql = "SELECT 1 FROM borrow_records WHERE user_id = ? AND isbn = ? AND status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private BorrowRecord mapFullRow(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            Role.valueOf(rs.getString("role"))
        );

        Book book = new Book(
            rs.getString("isbn"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getBigDecimal("price"),
            rs.getInt("quantity")
        );

        BorrowRecord record = new BorrowRecord(
            user,
            book,
            rs.getDate("borrow_date").toLocalDate(),
            rs.getDate("due_date").toLocalDate(),
            BorrowStatus.valueOf(rs.getString("status"))
        );
        record.setId(rs.getInt("id"));

        java.sql.Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            record.setReturnDate(returnDate.toLocalDate());
        }

        return record;
    }
}
