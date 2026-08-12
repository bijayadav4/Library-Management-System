# Library Management System (Java + PostgreSQL)

A minimal, console-based Library Management System built to demonstrate
core Java (OOP, layered design, JDBC) and ANSI SQL fundamentals. No web
UI, no cart/checkout flow, no membership-card logic — every method is
short enough to explain line-by-line in an interview.

## Architecture

```
com.library
├── Main.java              entry point
├── model/                 plain data classes (Book, User, BorrowRecord) + enums
├── database/               DBConnection — single place JDBC connections are opened
├── repository/             raw SQL, one class per table (BookRepository, UserRepository, BorrowRecordRepository)
├── service/                business rules (validation, loan period, late fee)
└── menu/                   console I/O (MainMenu, AdminMenu, BorrowerMenu)
```

Each layer only talks to the layer directly below it:
`menu -> service -> repository -> database`.
No SQL appears outside the `repository` package, and no `System.out`
appears outside the `menu` package.

## Entities

| Table            | Columns                                                                 |
|-------------------|--------------------------------------------------------------------------|
| `books`            | isbn (PK), title, author, price, quantity                               |
| `users`            | id (PK), name, email, password, role (ADMIN / BORROWER)                 |
| `borrow_records`   | id (PK), user_id (FK -> users), isbn (FK -> books), borrow_date, due_date, return_date, status (ACTIVE / RETURNED) |

`User` is a single class for both admins and borrowers — the `role`
column decides which console menu is shown after login. There is no
subclassing.

## Features

**Admin**
- Add Book, View All Books, Search Book (title/author), Update Quantity, Delete Book
- Add User (this is how borrower accounts get created — no self-signup)
- Active Borrows Report (shows the required JOIN + GROUP BY queries live)

**Borrower**
- View Books, Search Book, Borrow Book (direct — no cart), Return Book, View My Borrowed Books

**Late fee:** flat Rs. 10/day overdue, calculated on return. No compounding, no caps.

## Database requirements covered

- 3 tables with proper primary/foreign keys — see `sql/schema.sql`
- Multi-table JOIN — `BorrowRecordRepository.findActiveBorrowsWithDetails()`
  joins `borrow_records`, `users`, and `books` to list active loans with
  the borrower's name and the book's title.
- GROUP BY — `BorrowRecordRepository.countActiveBorrowsPerUser()` counts
  active borrows per user.

Both queries are also written out in plain SQL as comments at the bottom
of `sql/schema.sql`, and both are reachable from the console via
**Admin -> Active Borrows Report**.

## Setup

### 1. Create the database

```bash
createdb library_db
psql -d library_db -f sql/schema.sql
```

This also seeds one admin account (`admin@library.com` / `admin123`)
and two borrower accounts so you can log in immediately.

### 2. Configure the connection

`DBConnection.java` defaults to:

```
URL:      jdbc:postgresql://localhost:5432/library_db
USER:     postgres
PASSWORD: postgres
```

Override any of these with environment variables instead of editing
source, if you prefer:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/library_db"
export DB_USER="postgres"
export DB_PASSWORD="yourpassword"
```

### 3. Build and run

With Maven (bundles the PostgreSQL JDBC driver into one runnable jar):

```bash
mvn clean package
java -jar target/library-management-system-jar-with-dependencies.jar
```

Without Maven, if you have the PostgreSQL JDBC driver jar downloaded
locally (e.g. `postgresql-42.7.3.jar`):

```bash
javac -d out $(find src -name "*.java")
java -cp "out:postgresql-42.7.3.jar" com.library.Main
```
(on Windows, replace `:` with `;` in the classpath)

## Sample login

| Role     | Email               | Password |
|----------|----------------------|----------|
| Admin    | admin@library.com    | admin123 |
| Borrower | asha@example.com     | asha123  |
| Borrower | ravi@example.com     | ravi123  |

## Explicitly out of scope

Cart/checkout flow, book exchange, membership card loss, tenure
extension, security deposits, exponential fine formulas, and any web
UI. This project is scoped to console I/O + JDBC + ANSI SQL only.
