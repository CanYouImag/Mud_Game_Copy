package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        // 创建主窗口
        JFrame frame = new JFrame("MUD登录界面");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300); // 修改窗口大小为500x300
        frame.setLayout(new GridLayout(3, 2));

        // 居中显示窗口
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (screenSize.width - frame.getWidth()) / 2;
        int centerY = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(centerX, centerY);

        // 添加用户名和密码输入框
        JLabel usernameLabel = new JLabel("用户名:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("密码:");
        JPasswordField passwordField = new JPasswordField();

        // 添加注册和登录按钮
        JButton registerButton = new JButton("注册");
        JButton loginButton = new JButton("登录");

        // 添加组件到窗口
        frame.add(usernameLabel);
        frame.add(usernameField);
        frame.add(passwordLabel);
        frame.add(passwordField);
        frame.add(registerButton);
        frame.add(loginButton);

        // 注册按钮事件监听器
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                // 调用注册逻辑
                if (registerUser(username, password)) {
                    JOptionPane.showMessageDialog(frame, "注册成功！");
                } else {
                    JOptionPane.showMessageDialog(frame, "注册失败，请重试！");
                }
            }
        });

        // 登录按钮事件监听器
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                // 调用登录逻辑
                if (loginUser(username, password)) {
                    JOptionPane.showMessageDialog(frame, "登录成功！");
                    // 跳转到游戏界面
                    openGameInterface();
                } else {
                    JOptionPane.showMessageDialog(frame, "登录失败，请检查用户名或密码！");
                }
            }
        });

        // 显示窗口
        frame.setVisible(true);
    }

    // 模拟注册逻辑
    private static boolean registerUser(String username, String password) {
        // 这里应该调用数据库操作
        System.out.println("注册用户: " + username);
        return true; // 假设注册成功
    }

    // 模拟登录逻辑
    private static boolean loginUser(String username, String password) {
        // 这里应该调用数据库操作
        System.out.println("登录用户: " + username);
        return true; // 假设登录成功
    }

    // 打开游戏界面
    private static void openGameInterface() {
        // 这里应该打开游戏界面
        System.out.println("打开游戏界面...");

        // 游戏客户端逻辑
        String serverAddress = "localhost"; // 后端服务器地址
        int serverPort = 3080; // 后端服务器端口

        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("连接到服务器 " + serverAddress + " 的端口 " + serverPort);
            System.out.println("输入消息后按回车发送，输入 help 查看命令，输入 'exit' 退出：");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                // 发送用户输入到后端
                out.println(userInput);
                System.out.println("发送: " + userInput);

                // 检查是否输入 'exit' 以退出
                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println("退出程序。");
                    break;
                } else if ("help".equalsIgnoreCase(userInput)) {
                    System.out.println("可用命令：");
                    System.out.println("  look - 查看当前房间");
                    System.out.println("  move [方向] - 移动到指定方向的房间（方向包括 n, s, e, w, ne, se, nw, sw, u, d）");
                    System.out.println("  get [物品] - 获取物品");
                    System.out.println("  drop [物品] - 丢弃物品");
                    System.out.println("  quit - 退出游戏");
                    System.out.println("  help - 查看帮助");
                    System.out.println("  exit - 退出程序");
                }

                // 读取服务器端的回复
                String serverResponse = in.readLine();
                if (serverResponse == null || serverResponse.isEmpty()) {
                    System.out.println("服务器无响应，请检查网络连接！");
                } else {
                    System.out.println("服务器响应: " + serverResponse);
                }
            }

        } catch (IOException e) {
            System.err.println("连接失败: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("端口号必须为数字！");
        }
    }
}