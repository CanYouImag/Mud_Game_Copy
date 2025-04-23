package server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import server.router.MessageHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

@SpringBootApplication
public class ServerApplication implements CommandLineRunner {
    private static World world; // 添加 World 对象作为静态变量

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        int port = 3080; // 默认端口号
        ServerSocket serverSocket = null;

        try {
            // 初始化 World 对象
            world = new World();
            // 检查房间是否加载成功，如果没有加载到任何房间，则添加默认房间
            if (world.getRooms().isEmpty()) {
                Room defaultRoom = new Room("000", "空屋子", "一个空空荡荡的屋子");
                world.getRooms().put(defaultRoom.getId(), defaultRoom);
            }
            // 尝试绑定到指定端口
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected");

                // 启动 ClientHandler 线程处理客户端连接
                new ClientHandler(clientSocket).start();
            }
        } catch (BindException e) {
            // 处理端口已被占用的情况
            System.err.println("Error: Port " + port + " is already in use. Please check and free the port.");
            System.exit(1); // 终止程序
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 整合 com.former.ServerApplication 的 ClientHandler 功能
    public static class ClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private Player player; // 添加玩家对象

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // 创建玩家对象并设置输出流
                player = new Player("Guest" + System.currentTimeMillis(), "password");
                player.setOut(out); // 设置玩家的输出流

                World world = new World();
                Room startingRoom = world.getRooms().values().iterator().next();
                player.setCurrentRoom(startingRoom);
                startingRoom.addPlayer(player);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // 处理客户端请求
                    String response = processRequest(inputLine);
                    if (response != null && !response.isEmpty()) { // 过滤空响应
                        out.println(response); // 确保每次只发送一次有效响应
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String processRequest(String request) {
            // 使用 MessageHandler 处理命令
            MessageHandler messageHandler = new MessageHandler(player);
            String response = messageHandler.handleMessage(request);
            
            // 检查响应是否为空或无效
            if (response == null || response.isEmpty()) {
                return "无效的命令，请输入 help 查看可用命令。";
            }
            
            return response;
        }
    }
}