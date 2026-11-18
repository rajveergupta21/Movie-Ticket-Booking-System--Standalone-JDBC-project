public class Booking {
    private int id;
    private String bookingCode;
    private int userId;
    private String userName;
    private String userEmail;
    private int showId;
    private String movieTitle;
    private String movieGenre;
    private String movieLanguage;
    private String movieRating;
    private String theatreName;
    private String theatreCity;
    private String theatreAddress;
    private String screenName;
    private String showDate;
    private String showTime;
    private String seatLabels;
    private String seatCategories;
    private int seatCount;
    private String status;
    private String bookedAt;

    public Booking() {
    }

    public Booking(int id, String bookingCode, int userId, String userName, String userEmail,
                   int showId, String movieTitle, String movieGenre, String movieLanguage, String movieRating,
                   String theatreName, String theatreCity, String theatreAddress,
                   String screenName, String showDate, String showTime,
                   String seatLabels, String seatCategories, int seatCount, String status, String bookedAt) {
        this.id = id;
        this.bookingCode = bookingCode;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.movieGenre = movieGenre;
        this.movieLanguage = movieLanguage;
        this.movieRating = movieRating;
        this.theatreName = theatreName;
        this.theatreCity = theatreCity;
        this.theatreAddress = theatreAddress;
        this.screenName = screenName;
        this.showDate = showDate;
        this.showTime = showTime;
        this.seatLabels = seatLabels;
        this.seatCategories = seatCategories;
        this.seatCount = seatCount;
        this.status = status;
        this.bookedAt = bookedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMovieGenre() {
        return movieGenre;
    }

    public void setMovieGenre(String movieGenre) {
        this.movieGenre = movieGenre;
    }

    public String getMovieLanguage() {
        return movieLanguage;
    }

    public void setMovieLanguage(String movieLanguage) {
        this.movieLanguage = movieLanguage;
    }

    public String getMovieRating() {
        return movieRating;
    }

    public void setMovieRating(String movieRating) {
        this.movieRating = movieRating;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getTheatreCity() {
        return theatreCity;
    }

    public void setTheatreCity(String theatreCity) {
        this.theatreCity = theatreCity;
    }

    public String getTheatreAddress() {
        return theatreAddress;
    }

    public void setTheatreAddress(String theatreAddress) {
        this.theatreAddress = theatreAddress;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getSeatLabels() {
        return seatLabels;
    }

    public void setSeatLabels(String seatLabels) {
        this.seatLabels = seatLabels;
    }

    public String getSeatCategories() {
        return seatCategories;
    }

    public void setSeatCategories(String seatCategories) {
        this.seatCategories = seatCategories;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(String bookedAt) {
        this.bookedAt = bookedAt;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"bookingCode\":\"").append(User.escapeJson(bookingCode)).append("\",");
        sb.append("\"userId\":").append(userId).append(",");
        sb.append("\"userName\":\"").append(User.escapeJson(userName)).append("\",");
        sb.append("\"userEmail\":\"").append(User.escapeJson(userEmail)).append("\",");
        sb.append("\"showId\":").append(showId).append(",");
        sb.append("\"movieTitle\":\"").append(User.escapeJson(movieTitle)).append("\",");
        sb.append("\"movieGenre\":\"").append(User.escapeJson(movieGenre)).append("\",");
        sb.append("\"movieLanguage\":\"").append(User.escapeJson(movieLanguage)).append("\",");
        sb.append("\"movieRating\":\"").append(User.escapeJson(movieRating)).append("\",");
        sb.append("\"theatreName\":\"").append(User.escapeJson(theatreName)).append("\",");
        sb.append("\"theatreCity\":\"").append(User.escapeJson(theatreCity)).append("\",");
        sb.append("\"theatreAddress\":\"").append(User.escapeJson(theatreAddress)).append("\",");
        sb.append("\"screenName\":\"").append(User.escapeJson(screenName)).append("\",");
        sb.append("\"showDate\":\"").append(User.escapeJson(showDate)).append("\",");
        sb.append("\"showTime\":\"").append(User.escapeJson(showTime)).append("\",");
        sb.append("\"seatLabels\":\"").append(User.escapeJson(seatLabels)).append("\",");
        sb.append("\"seatCategories\":\"").append(User.escapeJson(seatCategories)).append("\",");
        sb.append("\"seatCount\":").append(seatCount).append(",");
        sb.append("\"status\":\"").append(User.escapeJson(status)).append("\",");
        sb.append("\"bookedAt\":\"").append(User.escapeJson(bookedAt)).append("\"");
        sb.append("}");
        return sb.toString();
    }
}
