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

    public String processCommand(String command) {
        if (command == null || command.isEmpty()) {
            return "无效的命令，请输入 help 查看可用命令。";
        }

        String[] parts = command.split(" ");
        String commandName = parts[0].toLowerCase();
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        switch (commandName) {
            case "look":
                return handleLookCommand();
            case "move":
                return handleMoveCommand(args);
            case "get":
                return handleGetCommand(Arrays.toString(args));
            case "drop":
                return handleDropCommand(args);
            case "levelup":
                return handleLevelUpCommand();
            case "learn": // 新增命令：学习技能
                return handleLearnCommand(args);
            default:
                return "无效的命令，请输入 help 查看可用命令。";
        }
    }

    // 新增方法：处理等级和境界查询
    private String handleLevelUpCommand() {
        return String.format("当前等级: %d, 当前境界: %s, 当前经验值: %d/%d",
                player.getLevel(), player.getRealm(), player.getExp(), player.getExpThreshold());
    }

    private String handleLookCommand() {
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return "你似乎不在任何房间中，请尝试重新连接或联系管理员。"; // 返回错误提示
        }
        return currentRoom.getDescription(); // 返回当前房间的详细描述
    }


    private String handleMoveCommand(String[] parts) {
        // 去除多余空格并重新分割命令
        String command = String.join(" ", parts).trim();
        parts = command.split("\\s+");

        // 打印调试信息
        System.out.println("Parsed parts: " + Arrays.toString(parts));

        if (parts == null || parts.length < 2) {
            return "请指定移动方向。例如：move north 或 move n。"; // 返回提示信息
        }

        String direction = parts[1].toLowerCase();
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return "你似乎不在任何房间中，请尝试重新连接或联系管理员。"; // 返回错误提示
        }
        try {
            // 确保方向值与 Direction 枚举一致
            Direction dir = Direction.valueOf(direction.toUpperCase());
            Optional<Room> nextRoomOptional = currentRoom.getExit(dir);
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
            // 提供更详细的错误信息
            return "无效的方向，请输入 help 查看可用命令。支持的方向包括：north (n), south (s), east (e), west (w) 等。"; // 返回错误提示
        } catch (Exception e) {
            // 捕获其他异常并记录日志
            e.printStackTrace();
            return "移动过程中发生错误，请稍后重试。"; // 返回错误提示
        }
    }

    private String handleLearnCommand(String[] args) {
        if (args.length < 1) {
            return "请输入 'learn [技能ID]' 来学习新技能。";
        }
        try {
            long skillId = Long.parseLong(args[0]);
            player.learnSkill(skillId);
            return "你成功学习了技能 ID: " + skillId;
        } catch (NumberFormatException e) {
            return "无效的技能ID，请输入数字。";
        }
    }

    private String handleGetCommand(String command) {
        if (command == null || command.isEmpty()) {
            return "请输入 'get [物品]' 来获取物品。"; // 返回提示信息
        }
        
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return "你似乎不在任何房间中，请尝试重新连接或联系管理员。"; // 返回错误提示
        }
        
        String itemName = command.trim();
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