package server.router;

import server.Room;
import server.Player;
import server.Items;
import server.Direction;
import java.util.*;

public class MessageHandler {
    private Player player;

    public MessageHandler(Player player) {
        this.player = player;
    }

    public String handleMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "无效的命令，请输入 help 查看可用命令。";
        }

        String[] parts = message.split(" ");
        String command = parts[0].toLowerCase();

        switch (command) {
            case "look":
                return handleLookCommand();
            case "move":
                return handleMoveCommand(parts);
            case "get":
                return handleGetCommand(parts);
            case "drop":
                return handleDropCommand(parts);
            case "quit":
                return "quit";
            case "help":
                return "可用命令：\n" +
                       "  look - 查看当前房间\n" +
                       "  move [方向] - 移动到指定方向的房间（方向包括 n, s, e, w, ne, se, nw, sw, u, d）\n" +
                       "  get [物品] - 获取物品\n" +
                       "  drop [物品] - 丢弃物品\n" +
                       "  quit - 退出游戏\n" +
                       "  help - 查看帮助\n" +
                       "  exit - 退出程序";
            default:
                return "无效的命令，请输入 help 查看可用命令。";
        }
    }

    private String handleLookCommand() {
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return "你似乎不在任何房间中，请尝试重新连接或联系管理员。";
        }
        return currentRoom.getDescription();
    }

    private String handleMoveCommand(String[] parts) {
        if (parts.length < 2) {
            return "请指定移动方向。例如：move north 或 move n。";
        }
        String direction = parts[1].toLowerCase();
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return "你似乎不在任何房间中，请尝试重新连接或联系管理员。";
        }
        try {
            Optional<Room> nextRoomOptional = currentRoom.getExit(Direction.valueOf(direction.toUpperCase()));
            if (nextRoomOptional.isPresent()) {
                Room nextRoom = nextRoomOptional.get();
                currentRoom.removePlayer(player);
                player.setCurrentRoom(nextRoom);
                nextRoom.addPlayer(player);
                return "你移动到了 " + nextRoom.getName() + "。\n" + nextRoom.getDescription();
            } else {
                return "无法向该方向移动，请检查方向是否正确。";
            }
        } catch (IllegalArgumentException e) {
            return "无效的方向，请输入 help 查看可用命令。";
        } catch (Exception e) {
            return "移动过程中发生错误，请稍后重试。";
        }
    }

    private String handleGetCommand(String[] parts) {
        // 实现获取物品的逻辑
        return "暂未实现获取物品功能。";
    }

    private String handleDropCommand(String[] parts) {
        // 实现丢弃物品的逻辑
        return "暂未实现丢弃物品功能。";
    }
}