package com.library.model;

import java.math.BigDecimal;

/**
 * Represents a single title in the library catalog.
 * isbn is the natural primary key — one row per distinct book title.
 * quantity is the number of copies currently owned by the library.
 */
public class Book {

    private String isbn;
    private String title;
    private String author;
    private BigDecimal price;
    private int quantity;

    public Book() {
    }

    public Book(String isbn, String title, String author, BigDecimal price, int quantity) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.quantity = quantity;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format(
            "ISBN: %-15s | %-35s | %-20s | Rs.%-8s | Qty: %d",
            isbn, title, author, price, quantity
        );
    }
}
