import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:movies.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: SQLite JDBC Driver not found in classpath. Make sure sqlite-jdbc-<version>.jar is placed inside lib/");
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
        return conn;
    }

    public static String hashPassword(String plainText) {
        if (plainText == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static String nowTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static synchronized void init() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "phone TEXT NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "role TEXT NOT NULL DEFAULT 'USER', " +
                "created_at TEXT NOT NULL" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS movies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "genre TEXT NOT NULL, " +
                "language TEXT NOT NULL, " +
                "duration_minutes INTEGER NOT NULL, " +
                "rating TEXT NOT NULL, " +
                "release_date TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "poster_color TEXT NOT NULL, " +
                "badge_text TEXT NOT NULL" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS theatres (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "city TEXT NOT NULL, " +
                "address TEXT NOT NULL" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS screens (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "theatre_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "total_seats INTEGER NOT NULL DEFAULT 96, " +
                "FOREIGN KEY (theatre_id) REFERENCES theatres(id) ON DELETE CASCADE" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS seats (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "screen_id INTEGER NOT NULL, " +
                "seat_row TEXT NOT NULL, " +
                "seat_col INTEGER NOT NULL, " +
                "seat_label TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "UNIQUE(screen_id, seat_label), " +
                "FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS shows (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER NOT NULL, " +
                "screen_id INTEGER NOT NULL, " +
                "show_date TEXT NOT NULL, " +
                "show_time TEXT NOT NULL, " +
                "FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_code TEXT NOT NULL UNIQUE, " +
                "user_id INTEGER NOT NULL, " +
                "show_id INTEGER NOT NULL, " +
                "seat_labels TEXT NOT NULL, " +
                "seat_categories TEXT NOT NULL, " +
                "seat_count INTEGER NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'CONFIRMED', " +
                "booked_at TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS booked_seats (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_id INTEGER NOT NULL, " +
                "show_id INTEGER NOT NULL, " +
                "seat_id INTEGER NOT NULL, " +
                "UNIQUE(show_id, seat_id), " +
                "FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE" +
                ");"
            );

            seedDataIfEmpty(conn);
            System.out.println("SQLite Database (movies.db) initialized and verified.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedDataIfEmpty(Connection conn) throws SQLException {
        int userCount = 0;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next()) {
                userCount = rs.getInt(1);
            }
        }
        if (userCount > 0) {
            return;
        }

        System.out.println("Seeding initial cinema data into movies.db...");
        conn.setAutoCommit(false);
        try {
            String now = nowTimestamp();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (name, email, phone, password_hash, role, created_at) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, "System Administrator");
                ps.setString(2, "admin@cineverse.com");
                ps.setString(3, "+91 98000 11111");
                ps.setString(4, hashPassword("admin123"));
                ps.setString(5, "ADMIN");
                ps.setString(6, now);
                ps.executeUpdate();

                ps.setString(1, "Aarav Sharma");
                ps.setString(2, "user@cineverse.com");
                ps.setString(3, "+91 98300 22222");
                ps.setString(4, hashPassword("user123"));
                ps.setString(5, "USER");
                ps.setString(6, now);
                ps.executeUpdate();

                ps.setString(1, "Priya Nair");
                ps.setString(2, "priya@cineverse.com");
                ps.setString(3, "+91 98765 43210");
                ps.setString(4, hashPassword("user123"));
                ps.setString(5, "USER");
                ps.setString(6, now);
                ps.executeUpdate();
            }

            Object[][] moviesData = new Object[][] {
                {
                    "Eclipse of Eternity",
                    "Sci-Fi",
                    "English",
                    164,
                    "UA 13+",
                    "2026-11-06",
                    "When a deep-space relay station detects a gravitational anomaly near Saturn, an elite crew embarks on an interstellar mission that bends time and human destiny.",
                    "linear-gradient(135deg, #0f172a 0%, #1e3a8a 50%, #e50914 100%)",
                    "IMAX LASER"
                },
                {
                    "The Velvet Syndicate",
                    "Thriller",
                    "English",
                    138,
                    "UA 16+",
                    "2026-10-24",
                    "An investigative archivist in Zurich uncovers a century-old cipher hidden inside central bank vaults, triggering a high-stakes cat-and-mouse chase across Europe.",
                    "linear-gradient(135deg, #18181b 0%, #7f1d1d 55%, #b91c1c 100%)",
                    "DOLBY ATMOS"
                },
                {
                    "Monsoon Sonata",
                    "Drama",
                    "Hindi",
                    145,
                    "U",
                    "2026-11-12",
                    "Two estranged classical musicians reunite in Mumbai during a torrential monsoon week to restore a legendary heritage opera house before its scheduled demolition.",
                    "linear-gradient(135deg, #064e3b 0%, #0f766e 55%, #f59e0b 100%)",
                    "CRITICS CHOICE"
                },
                {
                    "Valhalla Protocol",
                    "Action",
                    "English",
                    152,
                    "UA 16+",
                    "2026-11-14",
                    "A disavowed cyber-defense operative must infiltrate an orbital weapons platform inside the Arctic Circle before an autonomous defense grid locks out human command.",
                    "linear-gradient(135deg, #1e1b4b 0%, #4338ca 50%, #dc2626 100%)",
                    "4DX MOTION"
                },
                {
                    "Aarambham: Rise of the Chola",
                    "Action",
                    "Tamil",
                    172,
                    "UA 13+",
                    "2026-11-01",
                    "An epic historical saga following a fearless naval commander who leads an armada across treacherous seas to protect ancient maritime trade routes.",
                    "linear-gradient(135deg, #451a03 0%, #9a3412 50%, #f5c518 100%)",
                    "EPIC SAGA"
                },
                {
                    "Kallol: Echoes of Hooghly",
                    "Thriller",
                    "Bengali",
                    132,
                    "UA 13+",
                    "2026-11-15",
                    "Set along the fog-laden ghats of Kolkata, a forensic linguist decodes cryptic radio transmissions linking a vanished river steamer to a modern conspiracy.",
                    "linear-gradient(135deg, #111827 0%, #374151 50%, #d97706 100%)",
                    "REGIONAL GEM"
                },
                {
                    "Starlight Odyssey",
                    "Animation",
                    "English",
                    118,
                    "U",
                    "2026-11-10",
                    "A clockwork astronomer and a luminescent sky-fox journey across floating archipelago islands to relight the fallen constellations of the northern sky.",
                    "linear-gradient(135deg, #311042 0%, #6d28d9 50%, #38bdf8 100%)",
                    "FAMILY 3D"
                },
                {
                    "Deccan Chronicle: 1947",
                    "Drama",
                    "Telugu",
                    158,
                    "UA 13+",
                    "2026-11-05",
                    "Inside a turbulent princely state on the eve of independence, a fearless journalist risks everything to broadcast the truth on an underground radio network.",
                    "linear-gradient(135deg, #3b0764 0%, #831843 50%, #f43f5e 100%)",
                    "BLOCKBUSTER"
                }
            };

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO movies (title, genre, language, duration_minutes, rating, release_date, description, poster_color, badge_text) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (Object[] m : moviesData) {
                    ps.setString(1, (String) m[0]);
                    ps.setString(2, (String) m[1]);
                    ps.setString(3, (String) m[2]);
                    ps.setInt(4, (Integer) m[3]);
                    ps.setString(5, (String) m[4]);
                    ps.setString(6, (String) m[5]);
                    ps.setString(7, (String) m[6]);
                    ps.setString(8, (String) m[7]);
                    ps.setString(9, (String) m[8]);
                    ps.executeUpdate();
                }
            }

            String[][] theatresData = new String[][] {
                {"CineVerse Grand IMAX", "Mumbai", "Lower Parel, Phoenix Palladium, Mumbai"},
                {"CineVerse Onyx Multiplex", "Delhi", "Saket District Centre, New Delhi"},
                {"CineVerse Royal Salt Lake", "Kolkata", "Sector V, Salt Lake Bypass, Kolkata"},
                {"CineVerse Symphony Laser", "Bengaluru", "MG Road, Indiranagar Junction, Bengaluru"},
                {"CineVerse Marina Luxe", "Chennai", "OMR IT Expressway, Egmore, Chennai"},
                {"CineVerse Banjara Hills", "Hyderabad", "Road No. 12, Banjara Hills, Hyderabad"}
            };

            List<Integer> screenIds = new ArrayList<>();
            try (PreparedStatement psTheatre = conn.prepareStatement(
                    "INSERT INTO theatres (name, city, address) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psScreen = conn.prepareStatement(
                    "INSERT INTO screens (theatre_id, name, total_seats) VALUES (?, ?, 96)", Statement.RETURN_GENERATED_KEYS)) {

                for (String[] t : theatresData) {
                    psTheatre.setString(1, t[0]);
                    psTheatre.setString(2, t[1]);
                    psTheatre.setString(3, t[2]);
                    psTheatre.executeUpdate();

                    int theatreId = 0;
                    try (ResultSet rs = psTheatre.getGeneratedKeys()) {
                        if (rs.next()) {
                            theatreId = rs.getInt(1);
                        }
                    }

                    String[] screenNames = new String[] {"Screen 1 - IMAX Laser", "Screen 2 - Dolby Atmos"};
                    for (String sName : screenNames) {
                        psScreen.setInt(1, theatreId);
                        psScreen.setString(2, sName);
                        psScreen.executeUpdate();
                        try (ResultSet rsS = psScreen.getGeneratedKeys()) {
                            if (rsS.next()) {
                                int scId = rsS.getInt(1);
                                screenIds.add(scId);
                                generateSeatsForScreen(conn, scId);
                            }
                        }
                    }
                }
            }

            String[] dates = new String[] {"2026-11-19", "2026-11-20", "2026-11-21"};
            String[] times = new String[] {"10:30 AM", "02:15 PM", "06:30 PM", "09:45 PM"};

            try (PreparedStatement psShow = conn.prepareStatement(
                    "INSERT INTO shows (movie_id, screen_id, show_date, show_time) VALUES (?, ?, ?, ?)")) {
                int screenIdx = 0;
                for (int movieId = 1; movieId <= moviesData.length; movieId++) {
                    for (int sOffset = 0; sOffset < 3; sOffset++) {
                        int screenId = screenIds.get((screenIdx + sOffset) % screenIds.size());
                        String date = dates[sOffset % dates.length];
                        String time1 = times[(movieId + sOffset) % times.length];
                        String time2 = times[(movieId + sOffset + 2) % times.length];

                        psShow.setInt(1, movieId);
                        psShow.setInt(2, screenId);
                        psShow.setString(3, date);
                        psShow.setString(4, time1);
                        psShow.executeUpdate();

                        psShow.setInt(1, movieId);
                        psShow.setInt(2, screenId);
                        psShow.setString(3, dates[(sOffset + 1) % dates.length]);
                        psShow.setString(4, time2);
                        psShow.executeUpdate();
                    }
                    screenIdx += 2;
                }
            }

            seedInitialBooking(conn, 2, 1, new String[] {"E5", "E6"});
            seedInitialBooking(conn, 3, 1, new String[] {"H7", "H8"});
            seedInitialBooking(conn, 2, 3, new String[] {"D4", "D5", "D6"});

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public static void generateSeatsForScreen(Connection conn, int screenId) throws SQLException {
        String[] rows = new String[] {"A", "B", "C", "D", "E", "F", "G", "H"};
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO seats (screen_id, seat_row, seat_col, seat_label, category) VALUES (?, ?, ?, ?, ?)")) {
            for (String r : rows) {
                String category;
                if ("A".equals(r) || "B".equals(r) || "C".equals(r)) {
                    category = "Regular";
                } else if ("D".equals(r) || "E".equals(r) || "F".equals(r)) {
                    category = "Premium";
                } else {
                    category = "Recliner";
                }
                for (int col = 1; col <= 12; col++) {
                    String label = r + col;
                    ps.setInt(1, screenId);
                    ps.setString(2, r);
                    ps.setInt(3, col);
                    ps.setString(4, label);
                    ps.setString(5, category);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private static void seedInitialBooking(Connection conn, int userId, int showId, String[] labels) throws SQLException {
        int screenId = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT screen_id FROM shows WHERE id = ?")) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    screenId = rs.getInt(1);
                }
            }
        }
        if (screenId == 0) {
            return;
        }

        List<Integer> seatIds = new ArrayList<>();
        Set<String> categories = new LinkedHashSet<>();
        for (String label : labels) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, category FROM seats WHERE screen_id = ? AND seat_label = ?")) {
                ps.setInt(1, screenId);
                ps.setString(2, label);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        seatIds.add(rs.getInt("id"));
                        categories.add(rs.getString("category"));
                    }
                }
            }
        }

        if (seatIds.isEmpty()) {
            return;
        }

        String bookingCode = "CV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String joinedLabels = String.join(", ", labels);
        String joinedCategories = String.join(", ", categories);

        int bookingId = 0;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO bookings (booking_code, user_id, show_id, seat_labels, seat_categories, seat_count, status, booked_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED', ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bookingCode);
            ps.setInt(2, userId);
            ps.setInt(3, showId);
            ps.setString(4, joinedLabels);
            ps.setString(5, joinedCategories);
            ps.setInt(6, seatIds.size());
            ps.setString(7, nowTimestamp());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    bookingId = rs.getInt(1);
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO booked_seats (booking_id, show_id, seat_id) VALUES (?, ?, ?)")) {
            for (int seatId : seatIds) {
                ps.setInt(1, bookingId);
                ps.setInt(2, showId);
                ps.setInt(3, seatId);
                ps.executeUpdate();
            }
        }
    }

    // =========================================================================
    // USER AUTHENTICATION & MANAGEMENT QUERIES
    // =========================================================================

    public static User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        String sql = "SELECT id, name, email, phone, password_hash, role, created_at FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserById(int id) {
        String sql = "SELECT id, name, email, phone, password_hash, role, created_at FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User registerUser(String name, String email, String phone, String rawPassword) throws SQLException {
        User existing = getUserByEmail(email);
        if (existing != null) {
            return null;
        }
        String hashed = hashPassword(rawPassword);
        String now = nowTimestamp();
        String sql = "INSERT INTO users (name, email, phone, password_hash, role, created_at) VALUES (?, ?, ?, ?, 'USER', ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, phone.trim());
            ps.setString(4, hashed);
            ps.setString(5, now);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    return new User(newId, name.trim(), email.trim().toLowerCase(), phone.trim(), hashed, "USER", now);
                }
            }
        }
        return null;
    }

    public static User authenticateUser(String email, String rawPassword) {
        User user = getUserByEmail(email);
        if (user == null) {
            return null;
        }
        String hashedInput = hashPassword(rawPassword);
        if (hashedInput.equalsIgnoreCase(user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, name, email, phone, password_hash, role, created_at FROM users ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("password_hash"),
                    rs.getString("role"),
                    rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean updateUserRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "ADMIN".equalsIgnoreCase(newRole) ? "ADMIN" : "USER");
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // MOVIE MANAGEMENT & FILTERING QUERIES
    // =========================================================================

    public static List<Movie> getMovies(String city, String genre, String language, String search) {
        List<Movie> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT m.id, m.title, m.genre, m.language, m.duration_minutes, " +
            "m.rating, m.release_date, m.description, m.poster_color, m.badge_text " +
            "FROM movies m "
        );

        List<String> params = new ArrayList<>();
        if (city != null && !city.trim().isEmpty() && !"All".equalsIgnoreCase(city.trim())) {
            sql.append("JOIN shows sh ON sh.movie_id = m.id ");
            sql.append("JOIN screens sc ON sh.screen_id = sc.id ");
            sql.append("JOIN theatres t ON sc.theatre_id = t.id ");
        }

        sql.append("WHERE 1=1 ");
        if (city != null && !city.trim().isEmpty() && !"All".equalsIgnoreCase(city.trim())) {
            sql.append("AND LOWER(t.city) = LOWER(?) ");
            params.add(city.trim());
        }
        if (genre != null && !genre.trim().isEmpty() && !"All".equalsIgnoreCase(genre.trim())) {
            sql.append("AND LOWER(m.genre) = LOWER(?) ");
            params.add(genre.trim());
        }
        if (language != null && !language.trim().isEmpty() && !"All".equalsIgnoreCase(language.trim())) {
            sql.append("AND LOWER(m.language) = LOWER(?) ");
            params.add(language.trim());
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (LOWER(m.title) LIKE LOWER(?) OR LOWER(m.description) LIKE LOWER(?) OR LOWER(m.genre) LIKE LOWER(?)) ");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append("ORDER BY m.id ASC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getString("language"),
                        rs.getInt("duration_minutes"),
                        rs.getString("rating"),
                        rs.getString("release_date"),
                        rs.getString("description"),
                        rs.getString("poster_color"),
                        rs.getString("badge_text")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Movie getMovieById(int id) {
        String sql = "SELECT id, title, genre, language, duration_minutes, rating, release_date, description, poster_color, badge_text FROM movies WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getString("language"),
                        rs.getInt("duration_minutes"),
                        rs.getString("rating"),
                        rs.getString("release_date"),
                        rs.getString("description"),
                        rs.getString("poster_color"),
                        rs.getString("badge_text")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addMovie(String title, String genre, String language, int durationMinutes,
                                   String rating, String releaseDate, String description, String posterColor, String badgeText) {
        String sql = "INSERT INTO movies (title, genre, language, duration_minutes, rating, release_date, description, poster_color, badge_text) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title.trim());
            ps.setString(2, genre.trim());
            ps.setString(3, language.trim());
            ps.setInt(4, durationMinutes);
            ps.setString(5, rating.trim());
            ps.setString(6, releaseDate.trim());
            ps.setString(7, description.trim());
            ps.setString(8, (posterColor == null || posterColor.trim().isEmpty())
                    ? "linear-gradient(135deg, #1e1b4b 0%, #7f1d1d 55%, #e50914 100%)" : posterColor.trim());
            ps.setString(9, (badgeText == null || badgeText.trim().isEmpty()) ? "NOW SHOWING" : badgeText.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateMovie(int id, String title, String genre, String language, int durationMinutes,
                                      String rating, String releaseDate, String description, String posterColor, String badgeText) {
        String sql = "UPDATE movies SET title = ?, genre = ?, language = ?, duration_minutes = ?, rating = ?, " +
                     "release_date = ?, description = ?, poster_color = ?, badge_text = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title.trim());
            ps.setString(2, genre.trim());
            ps.setString(3, language.trim());
            ps.setInt(4, durationMinutes);
            ps.setString(5, rating.trim());
            ps.setString(6, releaseDate.trim());
            ps.setString(7, description.trim());
            ps.setString(8, (posterColor == null || posterColor.trim().isEmpty())
                    ? "linear-gradient(135deg, #1e1b4b 0%, #7f1d1d 55%, #e50914 100%)" : posterColor.trim());
            ps.setString(9, (badgeText == null || badgeText.trim().isEmpty()) ? "NOW SHOWING" : badgeText.trim());
            ps.setInt(10, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteMovie(int id) {
        String sql = "DELETE FROM movies WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // THEATRE & SCREEN MANAGEMENT QUERIES
    // =========================================================================

    public static List<Theatre> getAllTheatres() {
        List<Theatre> list = new ArrayList<>();
        String sql = "SELECT id, name, city, address FROM theatres ORDER BY city ASC, name ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Theatre(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("city"),
                    rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean addTheatre(String name, String city, String address) {
        String sql = "INSERT INTO theatres (name, city, address) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, city.trim());
            ps.setString(3, address.trim());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int theatreId = rs.getInt(1);
                        addScreen(theatreId, "Screen 1 - Digital Laser");
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteTheatre(int id) {
        String sql = "DELETE FROM theatres WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Screen> getAllScreens() {
        List<Screen> list = new ArrayList<>();
        String sql = "SELECT sc.id, sc.theatre_id, t.name AS theatre_name, t.city, sc.name, sc.total_seats " +
                     "FROM screens sc JOIN theatres t ON sc.theatre_id = t.id " +
                     "ORDER BY t.city ASC, t.name ASC, sc.name ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Screen(
                    rs.getInt("id"),
                    rs.getInt("theatre_id"),
                    rs.getString("theatre_name"),
                    rs.getString("city"),
                    rs.getString("name"),
                    rs.getInt("total_seats")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean addScreen(int theatreId, String screenName) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO screens (theatre_id, name, total_seats) VALUES (?, ?, 96)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, theatreId);
                ps.setString(2, screenName.trim());
                ps.executeUpdate();
                int screenId = 0;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        screenId = rs.getInt(1);
                    }
                }
                if (screenId > 0) {
                    generateSeatsForScreen(conn, screenId);
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteScreen(int id) {
        String sql = "DELETE FROM screens WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // SHOWTIMES & SEAT OCCUPANCY QUERIES
    // =========================================================================

    public static List<Show> getShows(Integer movieId, String city, String showDate) {
        List<Show> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT sh.id, sh.movie_id, sh.screen_id, t.id AS theatre_id, " +
            "m.title AS movie_title, m.genre AS movie_genre, m.language AS movie_language, " +
            "m.duration_minutes AS movie_duration, m.rating AS movie_rating, " +
            "t.name AS theatre_name, t.city AS theatre_city, t.address AS theatre_address, " +
            "sc.name AS screen_name, sh.show_date, sh.show_time, sc.total_seats, " +
            "(SELECT COUNT(*) FROM booked_seats bs WHERE bs.show_id = sh.id) AS booked_seats " +
            "FROM shows sh " +
            "JOIN movies m ON sh.movie_id = m.id " +
            "JOIN screens sc ON sh.screen_id = sc.id " +
            "JOIN theatres t ON sc.theatre_id = t.id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (movieId != null && movieId > 0) {
            sql.append("AND sh.movie_id = ? ");
            params.add(movieId);
        }
        if (city != null && !city.trim().isEmpty() && !"All".equalsIgnoreCase(city.trim())) {
            sql.append("AND LOWER(t.city) = LOWER(?) ");
            params.add(city.trim());
        }
        if (showDate != null && !showDate.trim().isEmpty() && !"All".equalsIgnoreCase(showDate.trim())) {
            sql.append("AND sh.show_date = ? ");
            params.add(showDate.trim());
        }

        sql.append("ORDER BY sh.show_date ASC, t.city ASC, t.name ASC, sh.show_time ASC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else {
                    ps.setString(i + 1, p.toString());
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Show(
                        rs.getInt("id"),
                        rs.getInt("movie_id"),
                        rs.getInt("screen_id"),
                        rs.getInt("theatre_id"),
                        rs.getString("movie_title"),
                        rs.getString("movie_genre"),
                        rs.getString("movie_language"),
                        rs.getInt("movie_duration"),
                        rs.getString("movie_rating"),
                        rs.getString("theatre_name"),
                        rs.getString("theatre_city"),
                        rs.getString("theatre_address"),
                        rs.getString("screen_name"),
                        rs.getString("show_date"),
                        rs.getString("show_time"),
                        rs.getInt("total_seats"),
                        rs.getInt("booked_seats")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Show getShowById(int showId) {
        String sql =
            "SELECT sh.id, sh.movie_id, sh.screen_id, t.id AS theatre_id, " +
            "m.title AS movie_title, m.genre AS movie_genre, m.language AS movie_language, " +
            "m.duration_minutes AS movie_duration, m.rating AS movie_rating, " +
            "t.name AS theatre_name, t.city AS theatre_city, t.address AS theatre_address, " +
            "sc.name AS screen_name, sh.show_date, sh.show_time, sc.total_seats, " +
            "(SELECT COUNT(*) FROM booked_seats bs WHERE bs.show_id = sh.id) AS booked_seats " +
            "FROM shows sh " +
            "JOIN movies m ON sh.movie_id = m.id " +
            "JOIN screens sc ON sh.screen_id = sc.id " +
            "JOIN theatres t ON sc.theatre_id = t.id " +
            "WHERE sh.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Show(
                        rs.getInt("id"),
                        rs.getInt("movie_id"),
                        rs.getInt("screen_id"),
                        rs.getInt("theatre_id"),
                        rs.getString("movie_title"),
                        rs.getString("movie_genre"),
                        rs.getString("movie_language"),
                        rs.getInt("movie_duration"),
                        rs.getString("movie_rating"),
                        rs.getString("theatre_name"),
                        rs.getString("theatre_city"),
                        rs.getString("theatre_address"),
                        rs.getString("screen_name"),
                        rs.getString("show_date"),
                        rs.getString("show_time"),
                        rs.getInt("total_seats"),
                        rs.getInt("booked_seats")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addShow(int movieId, int screenId, String showDate, String showTime) {
        String sql = "INSERT INTO shows (movie_id, screen_id, show_date, show_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setInt(2, screenId);
            ps.setString(3, showDate.trim());
            ps.setString(4, showTime.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteShow(int showId) {
        String sql = "DELETE FROM shows WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Seat> getSeatsForShow(int showId) {
        List<Seat> list = new ArrayList<>();
        Show show = getShowById(showId);
        if (show == null) {
            return list;
        }

        String sql =
            "SELECT s.id, s.screen_id, s.seat_row, s.seat_col, s.seat_label, s.category, " +
            "CASE WHEN bs.id IS NOT NULL THEN 1 ELSE 0 END AS is_booked " +
            "FROM seats s " +
            "LEFT JOIN booked_seats bs ON bs.seat_id = s.id AND bs.show_id = ? " +
            "WHERE s.screen_id = ? " +
            "ORDER BY s.seat_row ASC, s.seat_col ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ps.setInt(2, show.getScreenId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Seat(
                        rs.getInt("id"),
                        rs.getInt("screen_id"),
                        rs.getString("seat_row"),
                        rs.getInt("seat_col"),
                        rs.getString("seat_label"),
                        rs.getString("category"),
                        rs.getInt("is_booked") == 1
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // TRANSACTIONAL SEAT BOOKING & CANCELLATION (CONCURRENCY PROTECTED)
    // =========================================================================

    public static synchronized Booking createBookingTransaction(int userId, int showId, List<Integer> seatIds) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one seat to book.");
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int screenId = 0;
                try (PreparedStatement psShow = conn.prepareStatement("SELECT screen_id FROM shows WHERE id = ?")) {
                    psShow.setInt(1, showId);
                    try (ResultSet rs = psShow.executeQuery()) {
                        if (rs.next()) {
                            screenId = rs.getInt(1);
                        } else {
                            throw new SQLException("Selected showtime does not exist.");
                        }
                    }
                }

                List<String> labels = new ArrayList<>();
                Set<String> categories = new LinkedHashSet<>();

                try (PreparedStatement psSeat = conn.prepareStatement(
                        "SELECT id, seat_label, category FROM seats WHERE id = ? AND screen_id = ?")) {
                    for (int seatId : seatIds) {
                        psSeat.setInt(1, seatId);
                        psSeat.setInt(2, screenId);
                        try (ResultSet rs = psSeat.executeQuery()) {
                            if (rs.next()) {
                                labels.add(rs.getString("seat_label"));
                                categories.add(rs.getString("category"));
                            } else {
                                throw new SQLException("Invalid seat selection for this auditorium screen.");
                            }
                        }
                    }
                }

                String bookingCode = "CV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                String joinedLabels = String.join(", ", labels);
                String joinedCategories = String.join(", ", categories);
                String now = nowTimestamp();

                int bookingId = 0;
                try (PreparedStatement psBook = conn.prepareStatement(
                        "INSERT INTO bookings (booking_code, user_id, show_id, seat_labels, seat_categories, seat_count, status, booked_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED', ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    psBook.setString(1, bookingCode);
                    psBook.setInt(2, userId);
                    psBook.setInt(3, showId);
                    psBook.setString(4, joinedLabels);
                    psBook.setString(5, joinedCategories);
                    psBook.setInt(6, seatIds.size());
                    psBook.setString(7, now);
                    psBook.executeUpdate();
                    try (ResultSet rs = psBook.getGeneratedKeys()) {
                        if (rs.next()) {
                            bookingId = rs.getInt(1);
                        }
                    }
                }

                try (PreparedStatement psLock = conn.prepareStatement(
                        "INSERT INTO booked_seats (booking_id, show_id, seat_id) VALUES (?, ?, ?)")) {
                    for (int seatId : seatIds) {
                        psLock.setInt(1, bookingId);
                        psLock.setInt(2, showId);
                        psLock.setInt(3, seatId);
                        psLock.executeUpdate();
                    }
                }

                conn.commit();
                return getBookingById(bookingId);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static Booking getBookingById(int bookingId) {
        String sql =
            "SELECT b.id, b.booking_code, b.user_id, u.name AS user_name, u.email AS user_email, " +
            "b.show_id, m.title AS movie_title, m.genre AS movie_genre, m.language AS movie_language, m.rating AS movie_rating, " +
            "t.name AS theatre_name, t.city AS theatre_city, t.address AS theatre_address, " +
            "sc.name AS screen_name, sh.show_date, sh.show_time, " +
            "b.seat_labels, b.seat_categories, b.seat_count, b.status, b.booked_at " +
            "FROM bookings b " +
            "JOIN users u ON b.user_id = u.id " +
            "JOIN shows sh ON b.show_id = sh.id " +
            "JOIN movies m ON sh.movie_id = m.id " +
            "JOIN screens sc ON sh.screen_id = sc.id " +
            "JOIN theatres t ON sc.theatre_id = t.id " +
            "WHERE b.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBookingRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Booking getBookingByCode(String bookingCode) {
        String sql =
            "SELECT b.id, b.booking_code, b.user_id, u.name AS user_name, u.email AS user_email, " +
            "b.show_id, m.title AS movie_title, m.genre AS movie_genre, m.language AS movie_language, m.rating AS movie_rating, " +
            "t.name AS theatre_name, t.city AS theatre_city, t.address AS theatre_address, " +
            "sc.name AS screen_name, sh.show_date, sh.show_time, " +
            "b.seat_labels, b.seat_categories, b.seat_count, b.status, b.booked_at " +
            "FROM bookings b " +
            "JOIN users u ON b.user_id = u.id " +
            "JOIN shows sh ON b.show_id = sh.id " +
            "JOIN movies m ON sh.movie_id = m.id " +
            "JOIN screens sc ON sh.screen_id = sc.id " +
            "JOIN theatres t ON sc.theatre_id = t.id " +
            "WHERE LOWER(b.booking_code) = LOWER(?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBookingRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Booking> getBookingsByUser(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql =
            "SELECT b.id, b.booking_code, b.user_id, u.name AS user_name, u.email AS user_email, " +
            "b.show_id, m.title AS movie_title, m.genre AS movie_genre, m.language AS movie_language, m.rating AS movie_rating, " +
            "t.name AS theatre_name, t.city AS theatre_city, t.address AS theatre_address, " +
            "sc.name AS screen_name, sh.show_date, sh.show_time, " +
            "b.seat_labels, b.seat_categories, b.seat_count, b.status, b.booked_at " +
            "FROM bookings b " +
            "JOIN users u ON b.user_id = u.id " +
            "JOIN shows sh ON b.show_id = sh.id " +
            "JOIN movies m ON sh.movie_id = m.id " +
            "JOIN screens sc ON sh.screen_id = sc.id " +
            "JOIN theatres t ON sc.theatre_id = t.id " +
            "WHERE b.user_id = ? " +
            "ORDER BY b.id DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBookingRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql =
            "SELECT b.id, b.booking_code, b.user_id, u.name AS user_name, u.email AS user_email, " +
            "b.show_id, m.title AS movie_title, m.genre AS movie_genre, m.language AS movie_language, m.rating AS movie_rating, " +
            "t.name AS theatre_name, t.city AS theatre_city, t.address AS theatre_address, " +
            "sc.name AS screen_name, sh.show_date, sh.show_time, " +
            "b.seat_labels, b.seat_categories, b.seat_count, b.status, b.booked_at " +
            "FROM bookings b " +
            "JOIN users u ON b.user_id = u.id " +
            "JOIN shows sh ON b.show_id = sh.id " +
            "JOIN movies m ON sh.movie_id = m.id " +
            "JOIN screens sc ON sh.screen_id = sc.id " +
            "JOIN theatres t ON sc.theatre_id = t.id " +
            "ORDER BY b.id DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBookingRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static synchronized boolean cancelBooking(int bookingId, int requestUserId, boolean isAdmin) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int ownerId = -1;
                String status = "";
                try (PreparedStatement psCheck = conn.prepareStatement("SELECT user_id, status FROM bookings WHERE id = ?")) {
                    psCheck.setInt(1, bookingId);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            ownerId = rs.getInt("user_id");
                            status = rs.getString("status");
                        } else {
                            return false;
                        }
                    }
                }

                if (!isAdmin && ownerId != requestUserId) {
                    return false;
                }
                if ("CANCELLED".equalsIgnoreCase(status)) {
                    return false;
                }

                try (PreparedStatement psFree = conn.prepareStatement("DELETE FROM booked_seats WHERE booking_id = ?")) {
                    psFree.setInt(1, bookingId);
                    psFree.executeUpdate();
                }

                try (PreparedStatement psUpdate = conn.prepareStatement("UPDATE bookings SET status = 'CANCELLED' WHERE id = ?")) {
                    psUpdate.setInt(1, bookingId);
                    psUpdate.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Booking mapBookingRow(ResultSet rs) throws SQLException {
        return new Booking(
            rs.getInt("id"),
            rs.getString("booking_code"),
            rs.getInt("user_id"),
            rs.getString("user_name"),
            rs.getString("user_email"),
            rs.getInt("show_id"),
            rs.getString("movie_title"),
            rs.getString("movie_genre"),
            rs.getString("movie_language"),
            rs.getString("movie_rating"),
            rs.getString("theatre_name"),
            rs.getString("theatre_city"),
            rs.getString("theatre_address"),
            rs.getString("screen_name"),
            rs.getString("show_date"),
            rs.getString("show_time"),
            rs.getString("seat_labels"),
            rs.getString("seat_categories"),
            rs.getInt("seat_count"),
            rs.getString("status"),
            rs.getString("booked_at")
        );
    }

    public static List<String> getDistinctValues(String column, String table) {
        List<String> values = new ArrayList<>();
        String sql = "SELECT DISTINCT " + column + " FROM " + table + " ORDER BY " + column + " ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String val = rs.getString(1);
                if (val != null && !val.trim().isEmpty()) {
                    values.add(val.trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }
}
