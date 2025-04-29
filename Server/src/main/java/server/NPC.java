package server;

public class NPC {
    private String id;
    private String name;
    private String description;
    private String roomId;

    public NPC(String id, String name, String description, String roomId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.roomId = roomId;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}