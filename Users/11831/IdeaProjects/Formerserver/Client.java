
public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("已连接到服务器，输入命令：");
            String inputLine;
            while ((inputLine = userInput.readLine()) != null) {
                out.println(inputLine); // 发送命令到服务器
                String response = in.readLine(); // 接收服务器响应
                if (response != null && !response.isEmpty()) {
                    System.out.println("服务器返回：" + response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}