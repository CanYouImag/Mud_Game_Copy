package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	public static void main(String[] args) {
		String serverAddress = "localhost";
		int serverPort = 12345;

		try (Socket socket = new Socket(serverAddress, serverPort);
			 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 Scanner scanner = new Scanner(System.in)) {

			System.out.println("Connected to server: " + serverAddress + ":" + serverPort);


			// 接收服务器响应
			String response = in.readLine();
			System.out.println("Received: " + response);

			// 测试用户输入
			System.out.print("Enter a message to send to the server: ");
			String userInput = scanner.nextLine().trim();
			if(userInput.isEmpty()){
				System.out.println("干啥呢，喝高了？");
			}else{
				out.println(userInput);
				System.out.println("Sent: " + userInput);
			}

			// 接收服务器对用户输入的响应
			response = in.readLine();
			System.out.println("Received: " + response);

		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}catch (Exception e){
			System.err.println("未知错误: " + e.getMessage());
		}
	}
}