package server;

import com.former.database.DatabaseManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import server.router.MessageHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        private JTextArea textArea; // 提升 textArea 的作用域
        private JFrame frame; // 添加 JFrame 对象
        private boolean isAuthenticated = false; // 新增标志位，用于标识用户是否已验证

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

                // 初始化 World 对象并获取起始房间
                World world = new World();
                Room startingRoom = world.getRooms().values().iterator().next();
                player.setCurrentRoom(startingRoom);
                startingRoom.addPlayer(player);

                // 检查是否为 headless 模式
                boolean isHeadless = GraphicsEnvironment.isHeadless();

                if (!isHeadless) {
                    // 创建窗口化命令行界面
                    frame = new JFrame("窗口化命令行");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(400, 300);

                    textArea = new JTextArea(); // 定义 textArea
                    textArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    frame.add(scrollPane, BorderLayout.CENTER);

                    JPanel inputPanel = new JPanel();
                    JTextField inputField = new JTextField(30);
                    JButton sendButton = new JButton("发送");
                    inputPanel.add(inputField);
                    JButton loginButton = new JButton("登录");
                    inputPanel.add(loginButton);
                    frame.add(inputPanel, BorderLayout.SOUTH);

                    JTextArea finalTextArea = textArea;
                    sendButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String inputLine = inputField.getText();
                            if ("exit".equalsIgnoreCase(inputLine)) {
                                frame.dispose(); // 关闭窗口
                                return;
                            }
                            String response = processRequest(inputLine);
                            if (response != null && !response.isEmpty()) {
                                finalTextArea.append(response + "\n"); // 使用 textArea 显示响应
                            }
                            inputField.setText("");
                        }
                    });

                    loginButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String username = JOptionPane.showInputDialog(frame, "请输入用户名：");
                            String password = JOptionPane.showInputDialog(frame, "请输入密码：");
                            if (validateUser(username, password)) {
                                JOptionPane.showMessageDialog(frame, "登录成功！");
                                isAuthenticated = true;
                                finalTextArea.append("登录成功！\n");
                            } else {
                                JOptionPane.showMessageDialog(frame, "用户名或密码错误！");
                            }
                        }
                    });

                    frame.setVisible(true); // 确保窗口可见
                } else {
                    // 如果是 headless 模式，输出提示信息
                    System.out.println("当前环境不支持图形界面，跳过窗口化命令行界面的创建。");
                    // 在控制台中提示用户输入用户名和密码
                    out.println("请输入用户名：");
                    String username = in.readLine();
                    out.println("请输入密码：");
                    String password = in.readLine();

                    // 优化用户验证逻辑
                    while (!validateUser(username, password)) {
                        out.println("用户名或密码错误！");
                        out.println("请输入用户名：");
                        username = in.readLine();
                        out.println("请输入密码：");
                        password = in.readLine();
                    }
                    out.println("登录成功！");
                    isAuthenticated = true; // 设置验证标志位为 true

                    // 登录成功后，提示用户输入命令
                    out.println("请输入命令（输入 'exit' 退出）：");

                    // 主循环：处理客户端输入
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        if ("exit".equalsIgnoreCase(inputLine)) {
                            break; // 退出循环
                        }
                        String response = processRequest(inputLine);
                        if (response != null && !response.isEmpty()) {
                            out.println(response); // 发送响应到客户端
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (frame != null) {
                        frame.dispose(); // 确保窗口关闭
                    }
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean validateUser(String username, String password) {
            // 从数据库中获取玩家信息
            Map<String, String> playerData = DatabaseManager.getPlayer(username);
    
            // 检查玩家是否存在且密码匹配
            if (playerData != null && playerData.get("password").equals(password)) {
                player.setName(username); // 更新玩家名称
                return true;
            }
    
            return false;
        }

        private String processRequest(String request) {
            if (!isAuthenticated) {
                return "请先完成登录！"; // 如果未验证，提示用户登录
            }
            // 使用 MessageHandler 处理命令
            MessageHandler messageHandler = new MessageHandler(player);
            String response = messageHandler.handleMessage(request);
    
            // 检查响应是否为空或无效
            if (response == null || response.isEmpty()) {
                return "无效的命令，请输入 help 查看可用命令。"; // 统一的错误提示
            }
    
            return response; // 返回合法命令的响应
        }
    }
}