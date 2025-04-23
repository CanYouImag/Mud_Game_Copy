
public class Room {
    private String id;
    private String name;
    private String description;
    private List<Items> items;
    private Map<Direction, Room> exits;
    private List<Player> players;

    public Room(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.items = new ArrayList<>();
        this.exits = new HashMap<>();
        this.players = new ArrayList<>();
    }

    public void broadcast(String message) {
        for (Player player : players) {
            // 确保消息只发送一次
            if (!player.getName().equals(message.split(" ")[0])) {
                player.sendMessage(message);
            }
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
        broadcast(player.getName() + " 进入了房间。");
    }

    public void removePlayer(Player player) {
        players.remove(player);
        broadcast(player.getName() + " 离开了房间。");
    }

}