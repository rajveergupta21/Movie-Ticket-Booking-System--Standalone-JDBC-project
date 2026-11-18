public class Seat {
    private int id;
    private int screenId;
    private String seatRow;
    private int seatCol;
    private String seatLabel;
    private String category;
    private boolean booked;

    public Seat() {
    }

    public Seat(int id, int screenId, String seatRow, int seatCol, String seatLabel, String category, boolean booked) {
        this.id = id;
        this.screenId = screenId;
        this.seatRow = seatRow;
        this.seatCol = seatCol;
        this.seatLabel = seatLabel;
        this.category = category;
        this.booked = booked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScreenId() {
        return screenId;
    }

    public void setScreenId(int screenId) {
        this.screenId = screenId;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }

    public int getSeatCol() {
        return seatCol;
    }

    public void setSeatCol(int seatCol) {
        this.seatCol = seatCol;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"screenId\":").append(screenId).append(",");
        sb.append("\"seatRow\":\"").append(User.escapeJson(seatRow)).append("\",");
        sb.append("\"seatCol\":").append(seatCol).append(",");
        sb.append("\"seatLabel\":\"").append(User.escapeJson(seatLabel)).append("\",");
        sb.append("\"category\":\"").append(User.escapeJson(category)).append("\",");
        sb.append("\"status\":\"").append(booked ? "BOOKED" : "AVAILABLE").append("\",");
        sb.append("\"booked\":").append(booked);
        sb.append("}");
        return sb.toString();
    }
}
