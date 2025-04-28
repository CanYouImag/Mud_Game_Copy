package server;

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

	public static Room getStartingRoom() {
		if (rooms.isEmpty()) {
			createRooms();
		}
		return rooms.get("001"); // 修改为 "001" 以匹配 createRooms() 中的房间 ID
	}

	private static void createRooms() {
		Room startingRoom = new Room("001", "Starting Room", "You are in a small, dimly lit room.");
		Room northRoom = new Room("002", "North Room", "You are in a large, open room with a high ceiling.");
		Room eastRoom = new Room("003", "East Room", "You are in a cozy, well-lit room with a fireplace.");

		startingRoom.setExit(Direction.NORTH, northRoom);
		startingRoom.setExit(Direction.EAST, eastRoom);

		rooms.put("001", startingRoom); // 修改为 "001" 以匹配 getStartingRoom() 中的键
		rooms.put("002", northRoom);
		rooms.put("003", eastRoom);
	}

	public void setExit(Direction direction, Room room) {
		exits.put(direction, room);
	}

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