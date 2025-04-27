package server;

import server.router.MessageHandler;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Player {
	private final ArrayList<Items> inventory;
	private String name;
	private String password;
	private Room currentRoom;
	private PrintWriter out; // 添加输出流字段
	private BufferedReader in; // 添加输入流字段
	private Socket socket;

	// 新增构造函数：仅用于数据库加载场景
	public Player(String name, String password) {
		this.name = name;
		this.password = password;
		this.inventory = new ArrayList<>();
	}

	public Player(BufferedReader in, PrintWriter out){
		this.in = in;
		this.out = out;
		this.inventory= new ArrayList<>();
	}

	public void run() {
		try {
			out.println("欢迎来到Mud修仙世界，请修士告诉我你的名字：");
			name = in.readLine();
			out.println("好的，" + name + "！你现在位于初始大厅。");
			currentRoom.addPlayer(this);

			MessageHandler messageHandler = new MessageHandler(this);
			String input;
			while ((input = in.readLine()) != null) {
				String response = messageHandler.handleMessage(input);
				if (response != null && !response.isEmpty()) {
					out.println(response);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null){
					out.close();
				}
				if(in != null){
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void processCommand(String command) {
		String[] parts = command.split(" ");
		String commandName = parts[0].toLowerCase();
		String[] args = Arrays.copyOfRange(parts, 1, parts.length);

		switch (commandName) {
			case "look":
			case "l":
				out.println(currentRoom.getDescription());
				break;
			case "quit":
				out.println("非常感谢您的游玩，" + name + "再见！");
				currentRoom.removePlayer(this);
				break;
			case "north":
			case "n":
				move(Direction.NORTH);
				break;
			case "south":
			case "s":
				move(Direction.SOUTH);
				break;
			case "east":
			case "e":
				move(Direction.EAST);
				break;
			case "west":
			case "w":
				move(Direction.WEST);
				break;
			case "get":
				if (args.length == 0) {
					out.println("请输入 'get [物品]' 来获取物品。");
				} else {
					String itemName = args[0];
					for (Items item : currentRoom.getItems()) {
						if (item.getName().equalsIgnoreCase(itemName)) {
							currentRoom.removeItem(item);
							inventory.add(item);
							out.println("你获取了物品：" + item.getName());
							return;
						}
					}
					out.println("物品 " + itemName + " 不存在于此房间。");
				}
				break;
			case "drop":
				if (args.length == 0) {
					out.println("请输入 'drop [物品]' 来丢弃物品。");
				} else {
					String itemName = args[0];
					for (Items item : inventory) {
						if (item.getName().equalsIgnoreCase(itemName)) {
							inventory.remove(item);
							currentRoom.addItem(item);
							out.println("你丢弃了物品：" + item.getName());
							return;
						}
					}
					out.println("你没有持有物品 " + itemName + "。");
				}
				break;
			case "say":
				if (args.length == 0) {
					out.println("请输入 'say [消息]' 来发送消息。");
				} else {
					String message = String.join(" ", args);
					currentRoom.broadcast(name + " says: " + message);
				}
				break;
			default:
				out.println("未知命令：" + command);
		}
	}

	private void move(Direction direction) {
		Optional<Room> nextRoomOptional = currentRoom.getExit(direction);
		if (nextRoomOptional.isPresent()) {
			Room nextRoom = nextRoomOptional.get();
			currentRoom.removePlayer(this);
			currentRoom = nextRoom;
			currentRoom.addPlayer(this);
			out.println("你去往" + direction + "。");
			out.println(currentRoom.getDescription());
			currentRoom.broadcast(name + " moves " + direction + ".");
		} else {
			out.println("那地方你过不去！");
		}
	}

	public void sendMessage(String message) {
		if (out != null) {
			out.println(message);
		}
	}

	public String getName() {
		return name;
	}

	public List<Items> getInventory() {
		return inventory;
	}

	public Room getCurrentRoom() {
		return currentRoom;
	}

	public void setCurrentRoom(Room room) {
		this.currentRoom = room;
	}

	public void addItem(Items item) {
		inventory.add(item);
	}

	public void removeItem(Items item) {
		inventory.remove(item);
	}

	public String getPassword() {
		return password;
	}

	public void setSocket(Socket clientSocket) {
		this.socket = clientSocket;
	}
	public Socket getSocket() {
		return socket;
	}

	// 新增 setOut 方法
	public void setOut(PrintWriter out) {
		this.out = out;
	}

	public void setName(String username) {
		this.name = username;
	}
}