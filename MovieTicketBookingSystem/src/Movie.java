public class Movie {
    private int id;
    private String title;
    private String genre;
    private String language;
    private int durationMinutes;
    private String rating;
    private String releaseDate;
    private String description;
    private String posterColor;
    private String badgeText;

    public Movie() {
    }

    public Movie(int id, String title, String genre, String language, int durationMinutes,
                 String rating, String releaseDate, String description, String posterColor, String badgeText) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.description = description;
        this.posterColor = posterColor;
        this.badgeText = badgeText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterColor() {
        return posterColor;
    }

    public void setPosterColor(String posterColor) {
        this.posterColor = posterColor;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"title\":\"").append(User.escapeJson(title)).append("\",");
        sb.append("\"genre\":\"").append(User.escapeJson(genre)).append("\",");
        sb.append("\"language\":\"").append(User.escapeJson(language)).append("\",");
        sb.append("\"durationMinutes\":").append(durationMinutes).append(",");
        sb.append("\"rating\":\"").append(User.escapeJson(rating)).append("\",");
        sb.append("\"releaseDate\":\"").append(User.escapeJson(releaseDate)).append("\",");
        sb.append("\"description\":\"").append(User.escapeJson(description)).append("\",");
        sb.append("\"posterColor\":\"").append(User.escapeJson(posterColor)).append("\",");
        sb.append("\"badgeText\":\"").append(User.escapeJson(badgeText)).append("\"");
        sb.append("}");
        return sb.toString();
    }
}
