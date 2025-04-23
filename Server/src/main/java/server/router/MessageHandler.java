package server.router;

import server.Room;
import server.Player;
import server.Items;
import server.Direction;
import java.util.*;

public class MessageHandler {
    private static Player player;
    private static Room currentRoom;

    public MessageHandler(Player player) {
        this.player = player;
        this.currentRoom = player.getCurrentRoom();
    }

    public String handleMessage(String message) {
        String[] parts = message.split(" ");
        String commandName = parts[0].toLowerCase();
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        switch (commandName) {
            case "look":
                return lookCommand();
            case "move":
                return moveCommand(args);
            case "get":
                return getCommand(args);
            case "drop":
                return dropCommand(args);
            case "say":
                return sayCommand(args);
            case "help":
                return helpCommand();
            case "quit":
                return quitCommand();
            default:
                return "未知命令，请输入 'help' 查看可用命令。";
        }
    }

    private String lookCommand() {
        StringBuilder description = new StringBuilder();
        description.append("当前房间：").append(currentRoom.getDescription()).append("\n");

        // 显示房间中的物品
        if (currentRoom.getItems().isEmpty()) {
            description.append("房间里没有任何物品。\n");
        } else {
            description.append("物品：");
            for (Items item : currentRoom.getItems()) {
                description.append(item.getName()).append(", ");
            }
            description.deleteCharAt(description.length() - 2); // 删除最后一个逗号和空格
            description.append("\n");
        }

        // 显示房间的出口
        Map<Direction, Room> exits = currentRoom.getExits();
        if (exits.isEmpty()) {
            description.append("没有出口。\n");
        } else {
            description.append("出口：");
            for (Direction exit : exits.keySet()) {
                description.append(exit).append(", ");
            }
            description.deleteCharAt(description.length() - 2); // 删除最后一个逗号和空格
            description.append("\n");
        }

        return description.toString();
    }

    private String moveCommand(String[] args) {
        if (args.length == 0) {
            return "移动命令缺少方向参数，请输入 'move [方向]'。";
        }

        String directionStr = args[0].toUpperCase();
        Direction direction;
        try {
            direction = Direction.valueOf(directionStr);
        } catch (IllegalArgumentException e) {
            return "无效的方向，请输入有效的方向。";
        }

        Room nextRoom = currentRoom.getExit(direction);
        if (nextRoom == null) {
            return "无法向 " + direction + " 移动，没有该方向的出口。";
        }

        currentRoom.removePlayer(player);
        currentRoom = nextRoom;
        currentRoom.addPlayer(player);
        player.setCurrentRoom(currentRoom);
        return "你移动到了新的房间：" + currentRoom.getDescription();
    }

    private static String getCommand(String[] args) {
        if (args.length == 0) {
            return "获取命令缺少物品参数，请输入 'get [物品]'。";
        }

        String itemName = args[0];
        List<Items> items = currentRoom.getItems();

        for (Items item : items) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                currentRoom.removeItem(item);
                player.addItem(item);
                return "你获取了物品：" + item.getName();
            }
        }

        return "物品 " + itemName + " 不存在于此房间。";
    }

    private static String dropCommand(String[] args) {
        if (args.length == 0) {
            return "丢弃命令缺少物品参数，请输入 'drop [物品]'。";
        }

        String itemName = args[0];
        List<Items> inventory = player.getInventory();

        for (Items item : inventory) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                player.removeItem(item);
                currentRoom.addItem(item);
                return "你丢弃了物品：" + item.getName();
            }
        }

        return "你没有持有物品 " + itemName + "。";
    }

    private static String sayCommand(String[] args) {
        if (args.length == 0) {
            return "聊天命令缺少消息内容，请输入 'say [消息]'。";
        }

        String message = String.join(" ", args);
        currentRoom.broadcast(player.getName() + " says: " + message);
        return "";
    }

    private static String helpCommand() {
        return "可用命令：look, move [方向], get [物品], drop [物品], say [消息], help, quit";
    }

    private static String quitCommand() {
        currentRoom.removePlayer(player);
        return "quit";
    }
}