package com.former.database;

import server.Room;

import java.sql.*;
import java.util.*;


public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mud_game?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "CWai@3210979";

    // 新增方法：生成唯一的 player_id
    public static String generateUniquePlayerId() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            // 查询当前最大的 player_id
            String query = "SELECT MAX(CAST(player_id AS UNSIGNED)) AS max_id FROM players";
            try (ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    int maxId = rs.getInt("max_id");
                    return String.valueOf(maxId + 1); // 返回下一个唯一 ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "1"; // 如果表为空，返回初始 ID
    }

    public static void initialize() {
        // 创建 maps 表
        String createMapsTable = "CREATE TABLE IF NOT EXISTS maps (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) UNIQUE NOT NULL, " +
                "description TEXT, " +
                "start_x INT NOT NULL, " +
                "start_y INT NOT NULL, " +
                "width INT NOT NULL, " +
                "height INT NOT NULL, " +
                "layout TEXT)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createMapsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改: 使用 PreparedStatement 避免 SQL 注入风险，并插入 player_id
    public static void savePlayer(String name, String password) {
        String playerId = generateUniquePlayerId(); // 生成唯一 player_id
        String sql = "INSERT INTO players (player_id, name, passwd) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerId); // 插入 player_id
            pstmt.setString(2, name);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改: 使用 PreparedStatement 避免 SQL 注入风险
    public static Map<String, String> getPlayer(String name) {
        String sql = "SELECT * FROM players WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> playerData = new HashMap<>();
                    playerData.put("name", rs.getString("name"));
                    playerData.put("password", rs.getString("password"));
                    return playerData;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 修改: 返回 List<Room> 而非 List<Map>
    public static List<Room> loadRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    // 创建 Room 对象并设置属性
                    Room room = new Room(rs.getString("id"), rs.getString("name"), rs.getString("description"));
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

}
