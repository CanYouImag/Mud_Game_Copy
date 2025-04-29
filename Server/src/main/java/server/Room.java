package server;

import com.former.database.DatabaseManager;
import java.util.*;

public class Room {
    static final HashMap<String, Room> rooms = new HashMap<>();
    private String id;
    private String name;
    private String description;
    private HashMap<Direction, Room> exits;
    private List<Player> players;
    private List<Items> items;

    public Room(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.exits = new HashMap<>();
        this.players = new ArrayList<>();
        this.items = new ArrayList<>();
        rooms.put(id, this);
    }

    // 从数据库加载房间信息
    public static void loadRoomsFromDatabase() {
        List<Room> loadedRooms = DatabaseManager.loadRooms();
        for (Room room : loadedRooms) {
            rooms.put(room.getId(), room);
        }
    }

    public static Room getStartingRoom() {
        if (rooms.isEmpty()) {
            loadRoomsFromDatabase(); // 从数据库加载房间
        }
        return rooms.get("001"); // 获取数据库中的起始房间
    }


    // 新增方法: 设置房间出口
    public void setExit(Direction direction, Room room) {
        exits.put(direction, room);
    }

    // 新增方法: 获取房间出口
    public Optional<Room> getExit(Direction direction) {
        return Optional.ofNullable(exits.get(direction));
    }

    public void addPlayer(Player player) {
        players.add(player);
        broadcast(player.getName() + " enters the room.");
    }

    public void removePlayer(Player player) {
        players.remove(player);
        broadcast(player.getName() + " leaves the room.");
    }

    public void addItem(Items item) {
        items.add(item);
    }

    public void removeItem(Items item) {
        items.remove(item);
    }

    public List<Items> getItems() {
        return items;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(description).append("\n");
        sb.append("Exits: ");
        for (Direction direction : exits.keySet()) {
            sb.append(direction).append(" ");
        }
        sb.append("\n");
        sb.append("Players: ");
        for (Player player : players) {
            sb.append(player.getName()).append(" ");
        }
        sb.append("\n");
        sb.append("Items: ");
        for (Items item : items) {
            sb.append(item.getName()).append(" ");
        }
        return sb.toString();
    }

    public void broadcast(String message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    public HashMap<Direction, Room> getExits() {
        return exits;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
