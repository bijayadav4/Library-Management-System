package com.library.service;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.BorrowStatus;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business rules for borrowing and returning books.
 *
 * Kept intentionally simple:
 *  - fixed 14-day loan period
 *  - flat late fee of Rs.10/day overdue (no compounding, no caps)
 *  - one active borrow per (user, book) pair at a time
 */
public class BorrowService {

    private static final int LOAN_PERIOD_DAYS = 14;
    private static final int LATE_FEE_PER_DAY = 10;

    private final BorrowRecordRepository borrowRecordRepository = new BorrowRecordRepository();
    private final BookRepository bookRepository = new BookRepository();

    /**
     * Borrows one copy of a book directly for a user (no cart step).
     * Decrements the book's available quantity by one.
     */
    public BorrowRecord borrowBook(User user, String isbn) throws SQLException {
        Optional<Book> bookOpt = bookRepository.findByIsbn(isbn);
        if (bookOpt.isEmpty()) {
            throw new IllegalArgumentException("No book found with ISBN: " + isbn);
        }

        Book book = bookOpt.get();
        if (book.getQuantity() <= 0) {
            throw new IllegalStateException("\"" + book.getTitle() + "\" has no copies available right now.");
        }

        if (borrowRecordRepository.hasActiveBorrow(user.getId(), isbn)) {
            throw new IllegalStateException("You already have an active borrow for this book.");
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(LOAN_PERIOD_DAYS);

        BorrowRecord record = new BorrowRecord(user, book, borrowDate, dueDate, BorrowStatus.ACTIVE);
        int recordId = borrowRecordRepository.createBorrowRecord(record);
        record.setId(recordId);

        bookRepository.updateQuantity(isbn, book.getQuantity() - 1);

        return record;
    }

    /**
     * Returns a book: marks the record RETURNED, restores the book's
     * quantity, and reports the late fee (if any) for the caller to show.
     * Returns the late fee amount in rupees (0 if returned on/before due date).
     */
    public int returnBook(int recordId, User requestingUser) throws SQLException {
        Optional<BorrowRecord> recordOpt = borrowRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("No borrow record found with id: " + recordId);
        }

        BorrowRecord record = recordOpt.get();

        if (record.getUser().getId() != requestingUser.getId()) {
            throw new IllegalStateException("This borrow record does not belong to you.");
        }
        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new IllegalStateException("This book has already been returned.");
        }

        LocalDate returnDate = LocalDate.now();
        borrowRecordRepository.markReturned(recordId, returnDate);

        Book book = record.getBook();
        bookRepository.updateQuantity(book.getIsbn(), book.getQuantity() + 1);

        return calculateLateFee(record.getDueDate(), returnDate);
    }

    /**
     * Flat late fee: Rs.10 for every day past the due date.
     * Returns 0 if not overdue. Deliberately simple — no exponential
     * growth, no caps, no grace period.
     */
    public int calculateLateFee(LocalDate dueDate, LocalDate returnDate) {
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (daysLate <= 0) {
            return 0;
        }
        return (int) daysLate * LATE_FEE_PER_DAY;
    }

    public List<BorrowRecord> getBorrowedBooksForUser(int userId) throws SQLException {
        return borrowRecordRepository.findByUserId(userId);
    }

    /** Backing query for the required multi-table JOIN. */
    public List<BorrowRecord> getActiveBorrowsWithDetails() throws SQLException {
        return borrowRecordRepository.findActiveBorrowsWithDetails();
    }

    /** Backing query for the required GROUP BY. */
    public Map<String, Integer> getActiveBorrowCountPerUser() throws SQLException {
        return borrowRecordRepository.countActiveBorrowsPerUser();
    }
}
