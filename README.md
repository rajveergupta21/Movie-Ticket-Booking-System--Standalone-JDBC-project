# CineVerse — Standalone Java Movie Ticket Booking System (Zero Tomcat)

CineVerse is a 100% standalone, full-stack Movie Ticket Booking System web application built purely with standard JDK libraries (`com.sun.net.httpserver.HttpServer`, `java.sql.*`, `java.net.*`, `java.nio.*`) and SQLite (`sqlite-jdbc`).

Designed for zero container overhead, CineVerse requires **ZERO Apache Tomcat**, **ZERO external Servlet dependencies (`javax.servlet` / `jakarta.servlet`)**, **ZERO `web.xml` / WAR deployment**, and features a **Zero-Friction Instant Booking Flow** (no payment gateways, ticket prices, or checkout screens).

---

## Table of Contents

1. [Key Features](#1-key-features)
2. [Tech Stack & Architecture](#2-tech-stack--architecture)
3. [Project Directory Structure](#3-project-directory-structure)
4. [Prerequisites & SQLite JDBC Driver Setup](#4-prerequisites--sqlite-jdbc-driver-setup)
5. [Compile & Run Instructions (macOS / Linux / Windows)](#5-compile--run-instructions-macos--linux--windows)
6. [Default Seeded Credentials](#6-default-seeded-credentials)
7. [Database Schema & Concurrency Protection](#7-database-schema--concurrency-protection)
8. [REST API Reference](#8-rest-api-reference)
9. [Troubleshooting Common Issues](#9-troubleshooting-common-issues)

---

## 1. Key Features

- **100% Standalone Java Backend:** Runs directly from `main()` using the built-in JDK `HttpServer` on port `8080` with a 16-thread fixed pool (`Executors.newFixedThreadPool(16)`).
- **Zero-Payment Instant Reservation:** Selecting seats and clicking **"Book Seats"** immediately commits the reservation inside an explicit JDBC transaction and redirects to the **"Seat Booked!"** digital ticket confirmation screen (`CV-XXXXXX` reference code).
- **Atomic Double-Booking Prevention:** Enforces database-level seat locking via `UNIQUE(show_id, seat_id)` on the `booked_seats` table inside a synchronized JDBC transaction (`conn.setAutoCommit(false)`), preventing two concurrent users from reserving the same seat.
- **Interactive 96-Seat Auditorium Map:**
  - Curved glowing cinema screen indicator at the top.
  - 8 rows (`A–H`) x 12 columns (`1–12`) with dual aisle gaps after columns 3 and 9.
  - Three seating tiers: **Regular** (Rows `A–C`), **Premium** (Rows `D–F`), and **Recliner Lounge** (Rows `G–H`).
  - Real-time visual states for **Available**, **Selected**, and **Booked** seats.
- **Multi-Filter Movie & Showtime Discovery:** Filter movies dynamically by **City** (`Mumbai`, `Delhi`, `Kolkata`, `Bengaluru`, `Chennai`, `Hyderabad`), **Genre** (`Sci-Fi`, `Thriller`, `Drama`, `Action`, `Animation`), and **Language** (`English`, `Hindi`, `Tamil`, `Bengali`, `Telugu`).
- **Cookie-Based Authentication & RBAC:**
  - SHA-256 password hashing (`java.security.MessageDigest`).
  - `HttpOnly` session cookies (`CINEVERSE_SESSION`) managed in memory via `ConcurrentHashMap`.
  - Role-Based Access Control separating `USER` and `ADMIN` workflows.
- **User Booking History & Cancellations:** View all digital ticket passes in `my-bookings.html` and cancel active bookings to immediately release seats back to the `Available` pool.
- **Full-Featured Admin Control Center (`admin.html`):**
  - Add, edit, and delete movies (with custom CSS poster gradients and format badges).
  - Add and delete theatres and 96-seat auditorium screens (auto-populating all 96 seats across Rows `A–H`).
  - Schedule showtimes (Date & Time only, zero pricing) and monitor live seat occupancy percentages.
  - Inspect all customer bookings, cancel bookings to free seats, and promote/demote or delete user accounts.

---

## 2. Tech Stack & Architecture

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **HTTP Server** | `com.sun.net.httpserver.HttpServer` | Built-in JDK embedded web server serving static files & REST APIs |
| **Database Engine** | SQLite 3 (`movies.db`) | Zero-configuration relational database auto-created on startup |
| **Database Driver** | `sqlite-jdbc` (`lib/*.jar`) | Single external JAR dependency for JDBC connectivity |
| **Password Security** | `java.security.MessageDigest` (SHA-256) | One-way cryptographic hashing for user and admin passwords |
| **Frontend UI** | HTML5, CSS3, Vanilla JavaScript (ES6+) | Dark-mode cinema UI (`#0D0F14` obsidian, `#161B26` slate, `#E50914` crimson, `#F5C518` gold) |
| **IDE Configuration** | `.vscode/settings.json` | Pre-configured source path (`src`), output (`bin`), and library (`lib/**/*.jar`) |

---

## 3. Project Directory Structure

```text
MovieTicketBookingSystem/
├── .vscode/
│   └── settings.json                  # VS Code Java classpath configuration (src, bin, lib/**/*.jar)
├── lib/
│   └── README.txt                     # Place your local sqlite-jdbc-<version>.jar inside lib/
├── src/
│   ├── MovieServer.java               # Main entry point: HttpServer, StaticFileHandler, ApiHandler, Sessions
│   ├── Database.java                  # SQLite JDBC connection, schema creation, seeding & DAO methods
│   ├── Movie.java                     # Movie model & JSON serializer
│   ├── Theatre.java                   # Theatre model & JSON serializer
│   ├── Screen.java                    # Auditorium screen model & JSON serializer
│   ├── Show.java                      # Showtime & seat occupancy model & JSON serializer
│   ├── Seat.java                      # Individual auditorium seat model & JSON serializer
│   ├── Booking.java                   # Confirmed/cancelled booking ticket model & JSON serializer
│   └── User.java                      # User account model & JSON string escaper
├── WebContent/
│   ├── index.html                     # Landing hero banner & featured movies with quick showtimes
│   ├── login.html                     # User & Admin sign-in page with demo credentials
│   ├── register.html                  # New user registration page with validation
│   ├── movies.html                    # Full movie catalog with City, Genre & Language filters
│   ├── seat-selection.html            # Interactive 96-seat curved screen auditorium picker
│   ├── booking-confirmation.html      # "Seat Booked!" confirmation & digital ticket stub
│   ├── my-bookings.html               # Personal booking history & 1-click seat cancellation
│   ├── admin.html                     # 5-tab Admin Control Center (Movies, Theatres, Shows, Bookings, Users)
│   ├── css/
│   │   └── style.css                  # Cinematic dark-mode stylesheet
│   └── js/
│       └── app.js                     # Shared session navbar loader, HTML escaper & form POST client
└── README.md                          # Complete project documentation
```

---

## 4. Prerequisites & SQLite JDBC Driver Setup

### Prerequisites
1. **Java Development Kit (JDK) 11 or higher** installed and available in your system `PATH`.
   ```bash
   java -version
   javac -version
   ```
2. **SQLite JDBC Driver JAR (`sqlite-jdbc`)** placed inside the `lib/` directory.

### Adding the SQLite JDBC JAR to `lib/`
Per the standalone packaging specification, no `.jar` binary is bundled inside `lib/`. Before running the server, copy your local `sqlite-jdbc` JAR into `MovieTicketBookingSystem/lib/` or download the self-contained driver directly:

- **Option A — Copy an existing JAR from your machine:**
  ```bash
  cp /path/to/sqlite-jdbc-*.jar lib/
  ```
- **Option B — Download the self-contained SQLite JDBC driver via `curl` (macOS / Linux / Windows PowerShell):**
  ```bash
  curl -L -o lib/sqlite-jdbc-3.41.2.2.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.41.2.2/sqlite-jdbc-3.41.2.2.jar
  ```
  *(Note: `sqlite-jdbc-3.41.2.2.jar` is 100% self-contained. If you use `sqlite-jdbc` version `3.42.0.0` or newer, also place `slf4j-api-2.0.9.jar` inside `lib/`.)*

---

## 5. Compile & Run Instructions (macOS / Linux / Windows)

Open a terminal inside the root `MovieTicketBookingSystem/` directory:

### macOS & Linux (uses `:` classpath separator)
```bash
mkdir -p bin
javac -cp "lib/*" -d bin src/*.java
java -cp "bin:lib/*" MovieServer
```

### Windows Command Prompt / PowerShell (uses `;` classpath separator)
```cmd
mkdir bin
javac -cp "lib/*" -d bin src/*.java
java -cp "bin;lib/*" MovieServer
```

Once the server starts, you will see:
```text
Seeding initial cinema data into movies.db...
SQLite Database (movies.db) initialized and verified.
===================================================================
  CineVerse Standalone Movie Ticket Booking System Started
  URL         : http://localhost:8080
  Admin Login : admin@cineverse.com / admin123
  User Login  : user@cineverse.com  / user123
===================================================================
```

Open your browser and navigate to:
**http://localhost:8080**

---

## 6. Default Seeded Credentials

On first launch, `Database.init()` automatically creates `movies.db` in the project root and seeds 8 movies, 6 theatres across 6 cities, 12 auditorium screens (1,152 total seats), 48 showtimes, 3 sample bookings, and 3 user accounts:

| Role | Name | Email | Password | Access Level |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | System Administrator | `admin@cineverse.com` | `admin123` | Full access to `admin.html` + booking capabilities |
| **User** | Aarav Sharma | `user@cineverse.com` | `user123` | Browse, book seats, view & cancel own bookings |
| **User** | Priya Nair | `priya@cineverse.com` | `user123` | Browse, book seats, view & cancel own bookings |

---

## 7. Database Schema & Concurrency Protection

The SQLite database (`movies.db`) enables `PRAGMA foreign_keys = ON;` and `PRAGMA busy_timeout = 5000;` on every connection and consists of 7 normalized tables:

1. **`users`** — `id`, `name`, `email` (`UNIQUE`), `phone`, `password_hash`, `role` (`USER` / `ADMIN`), `created_at`
2. **`movies`** — `id`, `title`, `genre`, `language`, `duration_minutes`, `rating`, `release_date`, `description`, `poster_color`, `badge_text`
3. **`theatres`** — `id`, `name`, `city`, `address`
4. **`screens`** — `id`, `theatre_id` (`FK -> theatres.id ON DELETE CASCADE`), `name`, `total_seats` (`96`)
5. **`seats`** — `id`, `screen_id` (`FK -> screens.id ON DELETE CASCADE`), `seat_row` (`A–H`), `seat_col` (`1–12`), `seat_label` (`A1–H12`), `category` (`Regular`, `Premium`, `Recliner`), `UNIQUE(screen_id, seat_label)`
6. **`shows`** — `id`, `movie_id` (`FK -> movies.id ON DELETE CASCADE`), `screen_id` (`FK -> screens.id ON DELETE CASCADE`), `show_date`, `show_time`
7. **`bookings`** — `id`, `booking_code` (`UNIQUE`), `user_id`, `show_id`, `seat_labels`, `seat_categories`, `seat_count`, `status` (`CONFIRMED` / `CANCELLED`), `booked_at`
8. **`booked_seats`** — `id`, `booking_id` (`FK -> bookings.id ON DELETE CASCADE`), `show_id`, `seat_id`, **`UNIQUE(show_id, seat_id)`**

### How Double-Booking Prevention Works
When a user clicks **"Book Seats"**, `Database.createBookingTransaction()` executes inside a `synchronized` block with `conn.setAutoCommit(false)`:
1. Validates that the show exists and all requested `seat_id` values belong to that show's `screen_id`.
2. Inserts the master record into `bookings` with a generated `CV-XXXXXX` code.
3. Inserts each `seat_id` into `booked_seats`. If any seat was already taken for that `show_id`, SQLite immediately raises a `UNIQUE constraint failed: booked_seats.show_id, booked_seats.seat_id` exception.
4. On any constraint violation, `conn.rollback()` aborts the entire booking atomically and returns HTTP `409 Conflict`, refreshing the seat map in the browser.

---

## 8. REST API Reference

All API endpoints are handled by `MovieServer.ApiHandler` and return `application/json; charset=UTF-8`:

### Authentication & Session
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/session` | Returns current authenticated user session state |
| `POST` | `/api/login` | Authenticates `email` & `password`, sets `CINEVERSE_SESSION` cookie |
| `POST` | `/api/register` | Creates a new `USER` account with SHA-256 password hash |
| `POST` | `/api/logout` | Invalidates session token and clears cookie |

### Movies, Shows, Seats & Instant Booking
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/movies` | Lists movies filtered by optional `city`, `genre`, `language`, `search`, or `id` |
| `GET` | `/api/shows` | Lists showtimes with real-time `bookedSeats` & `availableSeats` counts |
| `GET` | `/api/seats?showId={id}` | Returns show details, movie metadata, and all 96 seats with `booked` boolean |
| `POST` | `/api/book` | Atomically books comma-separated `seatIds` for `showId` (Zero payment) |
| `GET` | `/api/booking?id={id}` | Retrieves a specific booking for the **"Seat Booked!"** ticket stub |
| `GET` | `/api/my-bookings` | Lists all bookings belonging to the logged-in user |
| `POST` | `/api/cancel-booking` | Cancels `bookingId` and deletes rows from `booked_seats` to free the seats |

### Admin Management (`ADMIN` Role Required)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/admin/overview` | Returns all movies, theatres, screens, shows, bookings, and users |
| `POST` | `/api/admin/movie` | Creates or updates a movie (`id=0` to add, `id>0` to edit) |
| `POST` | `/api/admin/movie/delete` | Deletes a movie and cascades associated shows |
| `POST` | `/api/admin/theatre` | Adds a new theatre and auto-creates a default 96-seat screen |
| `POST` | `/api/admin/theatre/delete` | Deletes a theatre by `id` |
| `POST` | `/api/admin/screen` | Adds a new 96-seat auditorium screen (`Rows A–H`) to a theatre |
| `POST` | `/api/admin/screen/delete` | Deletes a screen by `id` |
| `POST` | `/api/admin/show` | Schedules a showtime (`movieId`, `screenId`, `showDate`, `showTime`) |
| `POST` | `/api/admin/show/delete` | Deletes a showtime by `id` |
| `POST` | `/api/admin/user/role` | Toggles a user's role between `USER` and `ADMIN` |
| `POST` | `/api/admin/user/delete` | Deletes a user account (prevents self-deletion of active admin) |

---

## 9. Troubleshooting Common Issues

### 1. `java.net.BindException: Address already in use`
Port `8080` is already in use by another process (such as a previous instance of `MovieServer`).
- **macOS / Linux Fix:**
  ```bash
  kill -9 $(lsof -ti :8080)
  ```
- **Windows Fix:**
  ```cmd
  for /f "tokens=5" %a in ('netstat -aon ^| find ":8080" ^| find "LISTENING"') do taskkill /f /pid %a
  ```
- **Alternative:** Change `private static final int PORT = 8080;` in `src/MovieServer.java` to `8081` and recompile.

### 2. `ERROR: SQLite JDBC Driver not found in classpath` / `No suitable driver found for jdbc:sqlite:movies.db`
The `lib/` folder does not have the SQLite JDBC JAR file yet, or `lib/*` was omitted from the `java -cp` command.
- Ensure `sqlite-jdbc-<version>.jar` is inside `MovieTicketBookingSystem/lib/`.
- Run with quotes around the classpath wildcard:
  - macOS / Linux: `java -cp "bin:lib/*" MovieServer`
  - Windows: `java -cp "bin;lib/*" MovieServer`

### 3. `java.lang.NoClassDefFoundError: org/slf4j/LoggerFactory`
Newer versions of `sqlite-jdbc` (`3.42.0.0+`) depend on `slf4j-api`. Either use the self-contained `sqlite-jdbc-3.41.2.2.jar` or place `slf4j-api-2.0.9.jar` inside `lib/` alongside your `sqlite-jdbc` JAR.

### 4. Resetting the Database Back to Default Seed Data
To wipe all custom bookings or edits and restore the initial seeded dataset, stop the server, delete `movies.db`, and start the server again:
```bash
rm -f movies.db
java -cp "bin:lib/*" MovieServer
```
