public class Screen {
    private int id;
    private int theatreId;
    private String theatreName;
    private String city;
    private String name;
    private int totalSeats;

    public Screen() {
    }

    public Screen(int id, int theatreId, String theatreName, String city, String name, int totalSeats) {
        this.id = id;
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.city = city;
        this.name = name;
        this.totalSeats = totalSeats;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(int theatreId) {
        this.theatreId = theatreId;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"theatreId\":").append(theatreId).append(",");
        sb.append("\"theatreName\":\"").append(User.escapeJson(theatreName)).append("\",");
        sb.append("\"city\":\"").append(User.escapeJson(city)).append("\",");
        sb.append("\"name\":\"").append(User.escapeJson(name)).append("\",");
        sb.append("\"totalSeats\":").append(totalSeats);
        sb.append("}");
        return sb.toString();
    }
}
