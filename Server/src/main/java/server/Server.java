package server;


import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    private static final int PORT = 8080; // 确保与客户端连接的端口号一致
    private static final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private static final World world = new World();

    public static void main(String[] args) {
        initializeCommandHandlers();
        startServer();
    }

    private static void initializeCommandHandlers() {
        // 注册命令处理器
        commandHandlers.put("look", new LookCommandHandler());
        commandHandlers.put("move", new MoveCommandHandler());
        commandHandlers.put("take", new TakeCommandHandler());
        commandHandlers.put("players", new PlayersCommandHandler());
        // 添加更多命令处理器...
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器已启动，监听端口：" + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("接收到命令：" + inputLine); // 添加调试日志
                String[] parts = inputLine.split(" ");
                String command = parts[0];
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);

                CommandHandler handler = commandHandlers.get(command);
                if (handler != null) {
                    String response = handler.handle(args);
                    System.out.println("返回响应：" + response); // 添加调试日志
                    out.println(response);
                } else {
                    System.out.println("未知命令：" + command); // 添加调试日志
                    out.println("未知命令：" + command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 新增方法：获取所有玩家信息
    public static List<Map<String, String>> getPlayers() {
        return world.getPlayers();
    }
}

// 定义命令处理器接口
interface CommandHandler {
    String handle(String[] args);
}

// 示例命令处理器：LookCommandHandler
class LookCommandHandler implements CommandHandler {
    @Override
    public String handle(String[] args) {
        return "你环顾四周，看到了...";
    }
}

// 示例命令处理器：MoveCommandHandler
class MoveCommandHandler implements CommandHandler {
    @Override
    public String handle(String[] args) {
        if (args.length == 0) {
            return "请输入方向，例如：move north";
        }
        String direction = args[0];
        return "你向" + direction + "移动了...";
    }
}

// 示例命令处理器：TakeCommandHandler
class TakeCommandHandler implements CommandHandler {
    @Override
    public String handle(String[] args) {
        if (args.length == 0) {
            return "请输入要拾取的物品名称，例如：take sword";
        }
        String itemName = args[0];
        return "你尝试拾取 " + itemName + "...";
    }
}

// 新增命令处理器：PlayersCommandHandler
class PlayersCommandHandler implements CommandHandler {
    @Override
    public String handle(String[] args) {
        List<Map<String, String>> players = Server.getPlayers();
        StringBuilder response = new StringBuilder("当前在线玩家：\n");
        for (Map<String, String> player : players) {
            response.append(player.get("name")).append("\n");
        }
        return response.toString();
    }
}