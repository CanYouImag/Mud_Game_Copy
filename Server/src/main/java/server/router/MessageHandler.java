package server.router;

import server.Room;
import server.Player;
import server.Items;
import server.Direction;
import java.util.*;

public class MessageHandler {
    private Player player; // 将 player 改为实例变量

    public MessageHandler(Player player) {
        this.player = player; // 在构造函数中初始化 player
    }

    public String handleMessage(String message) { // 保持 handleMessage 为实例方法
        if (message == null || message.isEmpty()) {
            return "无效的命令，请输入 help 查看可用命令。"; // 返回无效命令提示
        }

        String[] parts = message.split(" ");
        String command = parts[0].toLowerCase();

        switch (command) {
            case "look":
                return handleLookCommand(); // 返回当前房间的描述
            case "move":
                return handleMoveCommand(parts); // 返回移动后的房间描述
            case "get":
                return handleGetCommand(parts); // 返回获取物品的结果
            case "drop":
                return handleDropCommand(parts); // 返回丢弃物品的结果
            default:
                return "无效的命令，请输入 help 查看可用命令。"; // 返回无效命令提示
        }
    }

    private String handleLookCommand() {
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return "你似乎不在任何房间中，请尝试重新连接或联系管理员。"; // 返回错误提示
        }
        return currentRoom.getDescription(); // 返回当前房间的详细描述
    }

    private String handleMoveCommand(String[] parts) {
        if (parts.length < 2) {
            return "请指定移动方向。例如：move north 或 move n。"; // 返回提示信息
        }
        String direction = parts[1].toLowerCase();
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return "你似乎不在任何房间中，请尝试重新连接或联系管理员。"; // 返回错误提示
        }
        try {
            Optional<Room> nextRoomOptional = currentRoom.getExit(Direction.valueOf(direction.toUpperCase()));
            if (nextRoomOptional.isPresent()) {
                Room nextRoom = nextRoomOptional.get();
                currentRoom.removePlayer(player);
                player.setCurrentRoom(nextRoom);
                nextRoom.addPlayer(player);
                return "你移动到了 " + nextRoom.getName() + "。\n" + nextRoom.getDescription(); // 返回移动后的房间描述
            } else {
                return "无法向该方向移动，请检查方向是否正确。"; // 返回错误提示
            }
        } catch (IllegalArgumentException e) {
            return "无效的方向，请输入 help 查看可用命令。"; // 返回错误提示
        } catch (Exception e) {
            return "移动过程中发生错误，请稍后重试。"; // 返回错误提示
        }
    }

    private String handleGetCommand(String[] parts) {
        if (parts.length < 2) {
            return "请输入 'get [物品]' 来获取物品。"; // 返回提示信息
        }
        String itemName = parts[1];
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return "你似乎不在任何房间中，请尝试重新连接或联系管理员。"; // 返回错误提示
        }
        for (Items item : currentRoom.getItems()) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                currentRoom.removeItem(item);
                player.addItem(item);
                return "你获取了物品：" + item.getName(); // 返回获取物品的结果
            }
        }
        return "物品 " + itemName + " 不存在于此房间。"; // 返回错误提示
    }

    private String handleDropCommand(String[] parts) {
        if (parts.length < 2) {
            return "请输入 'drop [物品]' 来丢弃物品。"; // 返回提示信息
        }
        String itemName = parts[1];
        List<Items> inventory = player.getInventory();
        for (Items item : inventory) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                inventory.remove(item);
                player.getCurrentRoom().addItem(item);
                return "你丢弃了物品：" + item.getName(); // 返回丢弃物品的结果
            }
        }
        return "你没有持有物品 " + itemName + "。"; // 返回错误提示
    }
}