public class Theatre {
    private int id;
    private String name;
    private String city;
    private String address;

    public Theatre() {
    }

    public Theatre(int id, String name, String city, String address) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"name\":\"").append(User.escapeJson(name)).append("\",");
        sb.append("\"city\":\"").append(User.escapeJson(city)).append("\",");
        sb.append("\"address\":\"").append(User.escapeJson(address)).append("\"");
        sb.append("}");
        return sb.toString();
    }
}
