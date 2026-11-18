import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class MovieServer {
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "WebContent";
    private static final Map<String, Integer> SESSIONS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        Database.init();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/", new ApiHandler());
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("===================================================================");
        System.out.println("  CineVerse Standalone Movie Ticket Booking System Started");
        System.out.println("  URL         : http://localhost:" + PORT);
        System.out.println("  Admin Login : admin@cineverse.com / admin123");
        System.out.println("  User Login  : user@cineverse.com  / user123");
        System.out.println("===================================================================");
    }

    private static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod().toUpperCase();

            try {
                if ("/api/session".equals(path) && "GET".equals(method)) {
                    handleSession(exchange);
                } else if ("/api/login".equals(path) && "POST".equals(method)) {
                    handleLogin(exchange);
                } else if ("/api/register".equals(path) && "POST".equals(method)) {
                    handleRegister(exchange);
                } else if ("/api/logout".equals(path) && "POST".equals(method)) {
                    handleLogout(exchange);
                } else if ("/api/movies".equals(path) && "GET".equals(method)) {
                    handleGetMovies(exchange);
                } else if ("/api/shows".equals(path) && "GET".equals(method)) {
                    handleGetShows(exchange);
                } else if ("/api/seats".equals(path) && "GET".equals(method)) {
                    handleGetSeats(exchange);
                } else if ("/api/book".equals(path) && "POST".equals(method)) {
                    handleCreateBooking(exchange);
                } else if ("/api/booking".equals(path) && "GET".equals(method)) {
                    handleGetBooking(exchange);
                } else if ("/api/my-bookings".equals(path) && "GET".equals(method)) {
                    handleGetMyBookings(exchange);
                } else if ("/api/cancel-booking".equals(path) && "POST".equals(method)) {
                    handleCancelBooking(exchange);
                } else if ("/api/admin/overview".equals(path) && "GET".equals(method)) {
                    handleAdminOverview(exchange);
                } else if ("/api/admin/movie".equals(path) && "POST".equals(method)) {
                    handleAdminSaveMovie(exchange);
                } else if ("/api/admin/movie/delete".equals(path) && "POST".equals(method)) {
                    handleAdminDeleteMovie(exchange);
                } else if ("/api/admin/theatre".equals(path) && "POST".equals(method)) {
                    handleAdminAddTheatre(exchange);
                } else if ("/api/admin/theatre/delete".equals(path) && "POST".equals(method)) {
                    handleAdminDeleteTheatre(exchange);
                } else if ("/api/admin/screen".equals(path) && "POST".equals(method)) {
                    handleAdminAddScreen(exchange);
                } else if ("/api/admin/screen/delete".equals(path) && "POST".equals(method)) {
                    handleAdminDeleteScreen(exchange);
                } else if ("/api/admin/show".equals(path) && "POST".equals(method)) {
                    handleAdminAddShow(exchange);
                } else if ("/api/admin/show/delete".equals(path) && "POST".equals(method)) {
                    handleAdminDeleteShow(exchange);
                } else if ("/api/admin/user/role".equals(path) && "POST".equals(method)) {
                    handleAdminUpdateUserRole(exchange);
                } else if ("/api/admin/user/delete".equals(path) && "POST".equals(method)) {
                    handleAdminDeleteUser(exchange);
                } else {
                    sendJson(exchange, 404, "{\"error\":\"API endpoint not found\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Server error: " + User.escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private static void handleSession(HttpExchange exchange) throws IOException {
        User user = getAuthenticatedUser(exchange);
        if (user == null) {
            sendJson(exchange, 200, "{\"authenticated\":false}");
        } else {
            sendJson(exchange, 200, "{\"authenticated\":true,\"user\":" + user.toJson() + "}");
        }
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseFormBody(exchange);
        String email = form.getOrDefault("email", "").trim();
        String password = form.getOrDefault("password", "");

        if (email.isEmpty() || password.isEmpty()) {
            sendJson(exchange, 400, "{\"error\":\"Email and password are required.\"}");
            return;
        }

        User user = Database.authenticateUser(email, password);
        if (user == null) {
            sendJson(exchange, 401, "{\"error\":\"Invalid email or password.\"}");
            return;
        }

        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, user.getId());
        exchange.getResponseHeaders().add("Set-Cookie", "CINEVERSE_SESSION=" + token + "; Path=/; HttpOnly; SameSite=Lax");
        sendJson(exchange, 200, "{\"message\":\"Login successful\",\"user\":" + user.toJson() + "}");
    }

    private static void handleRegister(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseFormBody(exchange);
        String name = form.getOrDefault("name", "").trim();
        String email = form.getOrDefault("email", "").trim();
        String phone = form.getOrDefault("phone", "+91 98000 00000").trim();
        String password = form.getOrDefault("password", "");

        if (name.length() < 2) {
            sendJson(exchange, 400, "{\"error\":\"Full name must be at least 2 characters.\"}");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            sendJson(exchange, 400, "{\"error\":\"Please enter a valid email address.\"}");
            return;
        }
        if (password.length() < 6) {
            sendJson(exchange, 400, "{\"error\":\"Password must be at least 6 characters.\"}");
            return;
        }
        if (phone.isEmpty()) {
            phone = "+91 98000 00000";
        }

        try {
            User created = Database.registerUser(name, email, phone, password);
            if (created == null) {
                sendJson(exchange, 409, "{\"error\":\"An account with this email already exists.\"}");
                return;
            }
            String token = UUID.randomUUID().toString();
            SESSIONS.put(token, created.getId());
            exchange.getResponseHeaders().add("Set-Cookie", "CINEVERSE_SESSION=" + token + "; Path=/; HttpOnly; SameSite=Lax");
            sendJson(exchange, 200, "{\"message\":\"Registration successful\",\"user\":" + created.toJson() + "}");
        } catch (SQLException e) {
            sendJson(exchange, 400, "{\"error\":\"" + User.escapeJson(e.getMessage()) + "\"}");
        }
    }

    private static void handleLogout(HttpExchange exchange) throws IOException {
        String token = getSessionCookie(exchange);
        if (token != null) {
            SESSIONS.remove(token);
        }
        exchange.getResponseHeaders().add("Set-Cookie", "CINEVERSE_SESSION=; Path=/; Max-Age=0; HttpOnly");
        sendJson(exchange, 200, "{\"message\":\"Logged out\"}");
    }

    private static void handleGetMovies(HttpExchange exchange) throws IOException {
        Map<String, String> q = parseQueryParams(exchange.getRequestURI().getRawQuery());
        String idStr = q.get("id");
        if (idStr != null && !idStr.isEmpty()) {
            Movie m = Database.getMovieById(parseIntSafe(idStr, 0));
            if (m == null) {
                sendJson(exchange, 404, "{\"error\":\"Movie not found\"}");
                return;
            }
            sendJson(exchange, 200, m.toJson());
            return;
        }

        String city = q.getOrDefault("city", "All");
        String genre = q.getOrDefault("genre", "All");
        String language = q.getOrDefault("language", "All");
        String search = q.getOrDefault("search", "");

        List<Movie> movies = Database.getMovies(city, genre, language, search);
        List<Theatre> theatres = Database.getAllTheatres();
        List<String> cities = Database.getDistinctValues("city", "theatres");
        List<String> genres = Database.getDistinctValues("genre", "movies");
        List<String> languages = Database.getDistinctValues("language", "movies");

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"movies\":[");
        for (int i = 0; i < movies.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(movies.get(i).toJson());
        }
        sb.append("],\"theatres\":[");
        for (int i = 0; i < theatres.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(theatres.get(i).toJson());
        }
        sb.append("],\"cities\":").append(stringListToJson(cities));
        sb.append(",\"genres\":").append(stringListToJson(genres));
        sb.append(",\"languages\":").append(stringListToJson(languages));
        sb.append("}");
        sendJson(exchange, 200, sb.toString());
    }

    private static void handleGetShows(HttpExchange exchange) throws IOException {
        Map<String, String> q = parseQueryParams(exchange.getRequestURI().getRawQuery());
        int movieId = parseIntSafe(q.get("movieId"), 0);
        String city = q.getOrDefault("city", "All");
        String showDate = q.getOrDefault("showDate", "All");

        List<Show> shows = Database.getShows(movieId > 0 ? movieId : null, city, showDate);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < shows.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(shows.get(i).toJson());
        }
        sb.append("]");
        sendJson(exchange, 200, sb.toString());
    }

    private static void handleGetSeats(HttpExchange exchange) throws IOException {
        Map<String, String> q = parseQueryParams(exchange.getRequestURI().getRawQuery());
        int showId = parseIntSafe(q.get("showId"), 0);
        Show show = Database.getShowById(showId);
        if (show == null) {
            sendJson(exchange, 404, "{\"error\":\"Showtime not found\"}");
            return;
        }
        Movie movie = Database.getMovieById(show.getMovieId());
        List<Seat> seats = Database.getSeatsForShow(showId);

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"show\":").append(show.toJson()).append(",");
        sb.append("\"movie\":").append(movie != null ? movie.toJson() : "null").append(",");
        sb.append("\"seats\":[");
        for (int i = 0; i < seats.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(seats.get(i).toJson());
        }
        sb.append("]}");
        sendJson(exchange, 200, sb.toString());
    }

    private static void handleCreateBooking(HttpExchange exchange) throws IOException {
        User user = getAuthenticatedUser(exchange);
        if (user == null) {
            sendJson(exchange, 401, "{\"error\":\"Please log in to book seats.\"}");
            return;
        }

        Map<String, String> form = parseFormBody(exchange);
        int showId = parseIntSafe(form.get("showId"), 0);
        String seatIdsStr = form.getOrDefault("seatIds", "").trim();

        if (showId <= 0 || seatIdsStr.isEmpty()) {
            sendJson(exchange, 400, "{\"error\":\"Please select a showtime and at least one seat.\"}");
            return;
        }

        List<Integer> seatIds = new ArrayList<>();
        for (String part : seatIdsStr.split(",")) {
            int sid = parseIntSafe(part.trim(), 0);
            if (sid > 0 && !seatIds.contains(sid)) {
                seatIds.add(sid);
            }
        }

        try {
            Booking booking = Database.createBookingTransaction(user.getId(), showId, seatIds);
            sendJson(exchange, 200, "{\"message\":\"Seat Booked!\",\"booking\":" + booking.toJson() + "}");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.toLowerCase().contains("unique")) {
                msg = "One or more selected seats were just booked by another user. Please choose different seats.";
            }
            sendJson(exchange, 409, "{\"error\":\"" + User.escapeJson(msg) + "\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\":\"" + User.escapeJson(e.getMessage()) + "\"}");
        }
    }

    private static void handleGetBooking(HttpExchange exchange) throws IOException {
        User user = getAuthenticatedUser(exchange);
        if (user == null) {
            sendJson(exchange, 401, "{\"error\":\"Authentication required\"}");
            return;
        }

        Map<String, String> q = parseQueryParams(exchange.getRequestURI().getRawQuery());
        int id = parseIntSafe(q.get("id"), 0);
        String code = q.getOrDefault("code", "").trim();

        Booking booking = (id > 0) ? Database.getBookingById(id) : Database.getBookingByCode(code);
        if (booking == null) {
            sendJson(exchange, 404, "{\"error\":\"Booking not found\"}");
            return;
        }
        if (booking.getUserId() != user.getId() && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            sendJson(exchange, 403, "{\"error\":\"Access denied\"}");
            return;
        }
        sendJson(exchange, 200, booking.toJson());
    }

    private static void handleGetMyBookings(HttpExchange exchange) throws IOException {
        User user = getAuthenticatedUser(exchange);
        if (user == null) {
            sendJson(exchange, 401, "{\"error\":\"Please log in to view your bookings.\"}");
            return;
        }
        List<Booking> list = Database.getBookingsByUser(user.getId());
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(list.get(i).toJson());
        }
        sb.append("]");
        sendJson(exchange, 200, sb.toString());
    }

    private static void handleCancelBooking(HttpExchange exchange) throws IOException {
        User user = getAuthenticatedUser(exchange);
        if (user == null) {
            sendJson(exchange, 401, "{\"error\":\"Authentication required.\"}");
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        int bookingId = parseIntSafe(form.get("bookingId"), 0);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        boolean ok = Database.cancelBooking(bookingId, user.getId(), isAdmin);
        if (!ok) {
            sendJson(exchange, 400, "{\"error\":\"Unable to cancel booking.\"}");
            return;
        }
        sendJson(exchange, 200, "{\"message\":\"Booking cancelled and seats released back to Available.\"}");
    }

    private static boolean requireAdmin(HttpExchange exchange) throws IOException {
        User user = getAuthenticatedUser(exchange);
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            sendJson(exchange, 403, "{\"error\":\"Administrator privileges required.\"}");
            return false;
        }
        return true;
    }

    private static void handleAdminOverview(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        List<Movie> movies = Database.getMovies(null, null, null, null);
        List<Theatre> theatres = Database.getAllTheatres();
        List<Screen> screens = Database.getAllScreens();
        List<Show> shows = Database.getShows(null, null, null);
        List<Booking> bookings = Database.getAllBookings();
        List<User> users = Database.getAllUsers();

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"movies\":[");
        for (int i = 0; i < movies.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(movies.get(i).toJson());
        }
        sb.append("],\"theatres\":[");
        for (int i = 0; i < theatres.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(theatres.get(i).toJson());
        }
        sb.append("],\"screens\":[");
        for (int i = 0; i < screens.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(screens.get(i).toJson());
        }
        sb.append("],\"shows\":[");
        for (int i = 0; i < shows.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(shows.get(i).toJson());
        }
        sb.append("],\"bookings\":[");
        for (int i = 0; i < bookings.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(bookings.get(i).toJson());
        }
        sb.append("],\"users\":[");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(users.get(i).toJson());
        }
        sb.append("]}");
        sendJson(exchange, 200, sb.toString());
    }

    private static void handleAdminSaveMovie(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        int id = parseIntSafe(form.get("id"), 0);
        String title = form.getOrDefault("title", "").trim();
        String genre = form.getOrDefault("genre", "").trim();
        String language = form.getOrDefault("language", "").trim();
        int duration = parseIntSafe(form.get("durationMinutes"), 135);
        String rating = form.getOrDefault("rating", "UA 13+").trim();
        String releaseDate = form.getOrDefault("releaseDate", "2026-11-20").trim();
        String description = form.getOrDefault("description", "").trim();
        String posterColor = form.getOrDefault("posterColor", "").trim();
        String badgeText = form.getOrDefault("badgeText", "IMAX LASER").trim();

        if (title.isEmpty() || genre.isEmpty() || language.isEmpty()) {
            sendJson(exchange, 400, "{\"error\":\"Title, genre, and language are required.\"}");
            return;
        }

        boolean ok;
        if (id > 0) {
            ok = Database.updateMovie(id, title, genre, language, duration, rating, releaseDate, description, posterColor, badgeText);
        } else {
            ok = Database.addMovie(title, genre, language, duration, rating, releaseDate, description, posterColor, badgeText);
        }

        if (!ok) {
            sendJson(exchange, 400, "{\"error\":\"Failed to save movie.\"}");
            return;
        }
        sendJson(exchange, 200, "{\"message\":\"Movie saved successfully.\"}");
    }

    private static void handleAdminDeleteMovie(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        int id = parseIntSafe(form.get("id"), 0);
        Database.deleteMovie(id);
        sendJson(exchange, 200, "{\"message\":\"Movie deleted.\"}");
    }

    private static void handleAdminAddTheatre(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        String name = form.getOrDefault("name", "").trim();
        String city = form.getOrDefault("city", "").trim();
        String address = form.getOrDefault("address", "").trim();
        if (name.isEmpty() || city.isEmpty()) {
            sendJson(exchange, 400, "{\"error\":\"Theatre name and city are required.\"}");
            return;
        }
        Database.addTheatre(name, city, address);
        sendJson(exchange, 200, "{\"message\":\"Theatre added with a default 96-seat screen.\"}");
    }

    private static void handleAdminDeleteTheatre(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        int id = parseIntSafe(form.get("id"), 0);
        Database.deleteTheatre(id);
        sendJson(exchange, 200, "{\"message\":\"Theatre deleted.\"}");
    }

    private static void handleAdminAddScreen(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        int theatreId = parseIntSafe(form.get("theatreId"), 0);
        String name = form.getOrDefault("name", "").trim();
        if (theatreId <= 0 || name.isEmpty()) {
            sendJson(exchange, 400, "{\"error\":\"Theatre and screen name are required.\"}");
            return;
        }
        Database.addScreen(theatreId, name);
        sendJson(exchange, 200, "{\"message\":\"Screen added with 96 seats (Rows A-H).\"}");
    }

    private static void handleAdminDeleteScreen(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        int id = parseIntSafe(form.get("id"), 0);
        Database.deleteScreen(id);
        sendJson(exchange, 200, "{\"message\":\"Screen deleted.\"}");
    }

    private static void handleAdminAddShow(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        int movieId = parseIntSafe(form.get("movieId"), 0);
        int screenId = parseIntSafe(form.get("screenId"), 0);
        String showDate = form.getOrDefault("showDate", "").trim();
        String showTime = form.getOrDefault("showTime", "").trim();

        if (movieId <= 0 || screenId <= 0 || showDate.isEmpty() || showTime.isEmpty()) {
            sendJson(exchange, 400, "{\"error\":\"Movie, screen, date, and time are required.\"}");
            return;
        }
        Database.addShow(movieId, screenId, showDate, showTime);
        sendJson(exchange, 200, "{\"message\":\"Showtime scheduled.\"}");
    }

    private static void handleAdminDeleteShow(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        int id = parseIntSafe(form.get("id"), 0);
        Database.deleteShow(id);
        sendJson(exchange, 200, "{\"message\":\"Showtime deleted.\"}");
    }

    private static void handleAdminUpdateUserRole(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        Map<String, String> form = parseFormBody(exchange);
        int userId = parseIntSafe(form.get("userId"), 0);
        String role = form.getOrDefault("role", "USER").trim();
        Database.updateUserRole(userId, role);
        sendJson(exchange, 200, "{\"message\":\"User role updated.\"}");
    }

    private static void handleAdminDeleteUser(HttpExchange exchange) throws IOException {
        if (!requireAdmin(exchange)) {
            return;
        }
        User current = getAuthenticatedUser(exchange);
        Map<String, String> form = parseFormBody(exchange);
        int userId = parseIntSafe(form.get("userId"), 0);
        if (current != null && current.getId() == userId) {
            sendJson(exchange, 400, "{\"error\":\"Cannot delete your own active admin account.\"}");
            return;
        }
        Database.deleteUser(userId);
        sendJson(exchange, 200, "{\"message\":\"User deleted.\"}");
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || path.isEmpty()) {
                path = "/index.html";
            }
            if (path.contains("..")) {
                sendText(exchange, 403, "403 Forbidden");
                return;
            }
            File file = new File(WEB_ROOT, path);
            if (!file.exists() || file.isDirectory()) {
                File fallback = new File(WEB_ROOT, "index.html");
                if (fallback.exists()) {
                    serveFile(exchange, fallback, "text/html; charset=UTF-8");
                    return;
                }
                sendText(exchange, 404, "404 Not Found");
                return;
            }
            serveFile(exchange, file, getMimeType(file.getName()));
        }

        private void serveFile(HttpExchange exchange, File file, String mime) throws IOException {
            byte[] bytes = Files.readAllBytes(file.toPath());
            Headers h = exchange.getResponseHeaders();
            h.set("Content-Type", mime);
            h.set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String getMimeType(String name) {
            String lower = name.toLowerCase();
            if (lower.endsWith(".html")) {
                return "text/html; charset=UTF-8";
            }
            if (lower.endsWith(".css")) {
                return "text/css; charset=UTF-8";
            }
            if (lower.endsWith(".js")) {
                return "application/javascript; charset=UTF-8";
            }
            if (lower.endsWith(".svg")) {
                return "image/svg+xml";
            }
            if (lower.endsWith(".png")) {
                return "image/png";
            }
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return "image/jpeg";
            }
            return "application/octet-stream";
        }
    }

    private static User getAuthenticatedUser(HttpExchange exchange) {
        String token = getSessionCookie(exchange);
        if (token == null) {
            return null;
        }
        Integer userId = SESSIONS.get(token);
        if (userId == null) {
            return null;
        }
        return Database.getUserById(userId);
    }

    private static String getSessionCookie(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies == null) {
            return null;
        }
        for (String header : cookies) {
            for (String part : header.split(";")) {
                String trimmed = part.trim();
                if (trimmed.startsWith("CINEVERSE_SESSION=")) {
                    String val = trimmed.substring("CINEVERSE_SESSION=".length()).trim();
                    if (!val.isEmpty()) {
                        return val;
                    }
                }
            }
        }
        return null;
    }

    private static Map<String, String> parseFormBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return parseQueryParams(buffer.toString(StandardCharsets.UTF_8.name()));
    }

    private static Map<String, String> parseQueryParams(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.trim().isEmpty()) {
            return map;
        }
        for (String pair : raw.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = urlDecode(pair.substring(0, idx));
                String v = urlDecode(pair.substring(idx + 1));
                map.put(k, v);
            }
        }
        return map;
    }

    private static String urlDecode(String val) {
        try {
            return URLDecoder.decode(val, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return val;
        }
    }

    private static int parseIntSafe(String val, int fallback) {
        if (val == null || val.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String stringListToJson(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(User.escapeJson(list.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        Headers h = exchange.getResponseHeaders();
        h.set("Content-Type", "application/json; charset=UTF-8");
        h.set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
