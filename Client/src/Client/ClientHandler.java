package Client;

import server.router.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;

public class ClientHandler extends Thread implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

	private Socket socket; // 客户端套接字
	private BufferedReader in; // 输入流
	private PrintWriter out; // 输出流
	private final MessageHandler messageHandler; // 消息处理器

	public ClientHandler(Socket socket, MessageHandler messageHandler) {
		this.socket = socket;
		this.messageHandler = messageHandler;
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			System.out.println("新用户连接：" + socket.getInetAddress());
			String message;
			while ((message = in.readLine()) != null) {
				try {
					LOGGER.info("收到玩家命令：" + message);
					String response = messageHandler.handleMessage(message); // 使用 messageHandler 实例处理消息
					System.out.println("返回命令：" + response);

					if (response.equals("quit")) {
						System.out.println("玩家退出游戏！");
						break;
					}
				} catch (Exception e) {
					LOGGER.severe("处理命令时发生错误：" + e.getMessage()); // 记录错误日志
					out.println("错误：命令处理失败，请重试。");
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "客户端连接异常！", e);
			e.printStackTrace();
		} finally {
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "关闭客户端连接异常！", e);
			}
		}
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}
}