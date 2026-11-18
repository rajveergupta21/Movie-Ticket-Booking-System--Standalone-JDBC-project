public class Show {
    private int id;
    private int movieId;
    private int screenId;
    private int theatreId;
    private String movieTitle;
    private String movieGenre;
    private String movieLanguage;
    private int movieDuration;
    private String movieRating;
    private String theatreName;
    private String theatreCity;
    private String theatreAddress;
    private String screenName;
    private String showDate;
    private String showTime;
    private int totalSeats;
    private int bookedSeats;

    public Show() {
    }

    public Show(int id, int movieId, int screenId, int theatreId,
                String movieTitle, String movieGenre, String movieLanguage, int movieDuration, String movieRating,
                String theatreName, String theatreCity, String theatreAddress,
                String screenName, String showDate, String showTime, int totalSeats, int bookedSeats) {
        this.id = id;
        this.movieId = movieId;
        this.screenId = screenId;
        this.theatreId = theatreId;
        this.movieTitle = movieTitle;
        this.movieGenre = movieGenre;
        this.movieLanguage = movieLanguage;
        this.movieDuration = movieDuration;
        this.movieRating = movieRating;
        this.theatreName = theatreName;
        this.theatreCity = theatreCity;
        this.theatreAddress = theatreAddress;
        this.screenName = screenName;
        this.showDate = showDate;
        this.showTime = showTime;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getScreenId() {
        return screenId;
    }

    public void setScreenId(int screenId) {
        this.screenId = screenId;
    }

    public int getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(int theatreId) {
        this.theatreId = theatreId;
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

    public int getMovieDuration() {
        return movieDuration;
    }

    public void setMovieDuration(int movieDuration) {
        this.movieDuration = movieDuration;
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

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public String toJson() {
        int availableSeats = Math.max(0, totalSeats - bookedSeats);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"movieId\":").append(movieId).append(",");
        sb.append("\"screenId\":").append(screenId).append(",");
        sb.append("\"theatreId\":").append(theatreId).append(",");
        sb.append("\"movieTitle\":\"").append(User.escapeJson(movieTitle)).append("\",");
        sb.append("\"movieGenre\":\"").append(User.escapeJson(movieGenre)).append("\",");
        sb.append("\"movieLanguage\":\"").append(User.escapeJson(movieLanguage)).append("\",");
        sb.append("\"movieDuration\":").append(movieDuration).append(",");
        sb.append("\"movieRating\":\"").append(User.escapeJson(movieRating)).append("\",");
        sb.append("\"theatreName\":\"").append(User.escapeJson(theatreName)).append("\",");
        sb.append("\"theatreCity\":\"").append(User.escapeJson(theatreCity)).append("\",");
        sb.append("\"theatreAddress\":\"").append(User.escapeJson(theatreAddress)).append("\",");
        sb.append("\"screenName\":\"").append(User.escapeJson(screenName)).append("\",");
        sb.append("\"showDate\":\"").append(User.escapeJson(showDate)).append("\",");
        sb.append("\"showTime\":\"").append(User.escapeJson(showTime)).append("\",");
        sb.append("\"totalSeats\":").append(totalSeats).append(",");
        sb.append("\"bookedSeats\":").append(bookedSeats).append(",");
        sb.append("\"availableSeats\":").append(availableSeats);
        sb.append("}");
        return sb.toString();
    }
}
