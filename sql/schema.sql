-- ============================================================
-- Library Management System — Schema
-- Plain ANSI-style SQL, PostgreSQL dialect
-- ============================================================

DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- ------------------------------------------------------------
-- users
-- ------------------------------------------------------------
CREATE TABLE users (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(10)  NOT NULL CHECK (role IN ('ADMIN', 'BORROWER'))
);

-- ------------------------------------------------------------
-- books
-- ------------------------------------------------------------
CREATE TABLE books (
    isbn     VARCHAR(20)   PRIMARY KEY,
    title    VARCHAR(200)  NOT NULL,
    author   VARCHAR(100)  NOT NULL,
    price    NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    quantity INT           NOT NULL CHECK (quantity >= 0)
);

-- ------------------------------------------------------------
-- borrow_records
-- ------------------------------------------------------------
CREATE TABLE borrow_records (
    id           SERIAL PRIMARY KEY,
    user_id      INT         NOT NULL REFERENCES users(id),
    isbn         VARCHAR(20) NOT NULL REFERENCES books(isbn),
    borrow_date  DATE        NOT NULL,
    due_date     DATE        NOT NULL,
    return_date  DATE,
    status       VARCHAR(10) NOT NULL CHECK (status IN ('ACTIVE', 'RETURNED'))
);

-- Helpful indexes for lookups the app performs often
CREATE INDEX idx_borrow_records_user_id ON borrow_records(user_id);
CREATE INDEX idx_borrow_records_isbn    ON borrow_records(isbn);
CREATE INDEX idx_borrow_records_status  ON borrow_records(status);

-- ============================================================
-- Seed data (optional, useful for quick testing)
-- ============================================================

-- Default admin login: admin@library.com / admin123
INSERT INTO users (name, email, password, role) VALUES
    ('Admin User',  'admin@library.com', 'admin123', 'ADMIN'),
    ('Asha Rao',    'asha@example.com',  'asha123',  'BORROWER'),
    ('Ravi Kumar',  'ravi@example.com',  'ravi123',  'BORROWER');

INSERT INTO books (isbn, title, author, price, quantity) VALUES
    ('9780134685991', 'Effective Java',              'Joshua Bloch',      799.00, 5),
    ('9780596009205', 'Head First Design Patterns',  'Freeman & Robson',  649.00, 3),
    ('9781491910774', 'Designing Data-Intensive Apps','Martin Kleppmann', 899.00, 2);

-- ============================================================
-- Reference queries required by the assignment
-- (also implemented in BorrowRecordRepository.java)
-- ============================================================

-- 1) Multi-table JOIN: active borrows with borrower name + book title
-- SELECT br.id, u.name AS borrower_name, b.title AS book_title,
--        br.borrow_date, br.due_date
-- FROM borrow_records br
-- JOIN users u ON br.user_id = u.id
-- JOIN books b ON br.isbn = b.isbn
-- WHERE br.status = 'ACTIVE'
-- ORDER BY br.due_date;

-- 2) GROUP BY: count of active borrows per user
-- SELECT u.name, COUNT(*) AS active_borrow_count
-- FROM borrow_records br
-- JOIN users u ON br.user_id = u.id
-- WHERE br.status = 'ACTIVE'
-- GROUP BY u.name
-- ORDER BY active_borrow_count DESC;
