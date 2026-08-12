package com.library.model;

import java.time.LocalDate;

/**
 * One row = one instance of a user borrowing one copy of a book.
 * Holds the full User and Book objects (not just their ids) so the
 * service/menu layers can print friendly details without extra lookups.
 */
public class BorrowRecord {

    private int id;
    private User user;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;

    public BorrowRecord() {
    }

    public BorrowRecord(User user, Book book, LocalDate borrowDate,
                         LocalDate dueDate, BorrowStatus status) {
        this.user = user;
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String returned = (returnDate == null) ? "-" : returnDate.toString();
        return String.format(
            "Record #%d | Book: %-30s | Borrowed: %s | Due: %s | Returned: %s | Status: %s",
            id, book.getTitle(), borrowDate, dueDate, returned, status
        );
    }
}
