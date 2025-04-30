package server;

import com.former.database.DatabaseManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class World {
    private Map<String, Room> rooms;
    private Map<String, Player> players;

    public World() {
        rooms = new HashMap<>();
        players = new HashMap<>();
        loadRooms();
    }

    // 新增 getRooms 方法
    public Map<String, Room> getRooms() {
        return rooms;
    }

    public void registerPlayer(Player player) {
        players.put(player.getName(), player);
        DatabaseManager.savePlayer(
            player.getName(),
            player.getPassword(),
            player.getCurrentRoomId(),
            player.getCurrentMapId(),
            player.getLevel(),
            player.getExp(),
            player.getRealm()
        );
    }

    public Player getPlayer(String name) {
        // 首先从内存中的 players 映射中查找玩家
        Player player = players.get(name);
        if (player == null) {
            // 如果内存中没有找到，则从数据库加载
            Map<String, Object> playerData = DatabaseManager.getPlayer(name);
            if (playerData != null) {
                // 创建 Player 对象并添加到内存中
                String getName = (String) playerData.get("name");
                String getPasswd = (String) playerData.get("password");
                player = new Player(getName, getPasswd);
                players.put(name, player);
            }
        }
        return player;
    }

    public List<Map<String, String>> getPlayers() {
        List<Map<String, String>> playerList = new ArrayList<>();
        for (Player player : players.values()) {
            Map<String, String> playerData = new HashMap<>();
            playerData.put("name", player.getName());
            playerData.put("password", player.getPassword());
            playerList.add(playerData);
        }
        return playerList;
    }

    private void loadRooms() {
        // 从数据库加载房间
        List<Room> loadedRooms = DatabaseManager.loadRooms();
        if (loadedRooms.isEmpty()) {
            // 如果数据库中没有房间数据，则添加默认房间
            Room defaultRoom = new Room("000", "空屋子", "一个空空荡荡的屋子");
            rooms.put(defaultRoom.getId(), defaultRoom);
        } else {
            for (Room room : loadedRooms) {
                rooms.put(room.getId(), room);
                // 为每个房间生成初始物品
                room.addItem(new Items("10000001", "Rusty Sword", "一把生锈的剑。", room.getId(),null));
                room.addItem(new Items("10000002", "Health Potion", "一瓶可以恢复生命值的药水。",  room.getId(),null));
            }
        }
    }

    // 新增方法: 添加房间
    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    // 新增方法: 移除房间
    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    // 新增方法：向所有玩家广播消息
    public void broadcastMessage(String message) {
        for (Player player : players.values()) {
            player.sendMessage(message);
        }
    }
}