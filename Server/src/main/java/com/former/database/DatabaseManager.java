package com.former.database;

import server.Room;

import java.sql.*;
import java.util.*;


public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mud_game?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root"; // 替换为实际的 MySQL 用户名
    private static final String DB_PASSWORD = "CWai@3210979"; // 替换为实际的 MySQL 密码

    public static void initialize() {
        // 创建 players 表
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (name VARCHAR(255) PRIMARY KEY, password VARCHAR(255))";
        // 创建 rooms 表
        String createRoomsTable = "CREATE TABLE IF NOT EXISTS rooms (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255), description TEXT)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createPlayersTable);
            stmt.execute(createRoomsTable);

            // 插入默认房间数据
            String insertRoom = "INSERT INTO rooms (id, name, description) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertRoom)) {
                pstmt.setString(1, "001");
                pstmt.setString(2, "起始房间");
                pstmt.setString(3, "这是一个空旷的起始房间。");
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改: 使用 Map 替代 Player 类
    public static void savePlayer(String name, String passwd) {
        String sql = "INSERT INTO players (name, passwd) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, passwd);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改: 返回 Map 而非 Player 类
    public static java.util.Map<String, String> getPlayer(String name) {
        String sql = "SELECT * FROM players WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    java.util.Map<String, String> playerData = new java.util.HashMap<>();
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
                    Room room = new Room("000","空屋子","一个空空荡荡的屋子");
                    room.setId(rs.getString("id"));
                    room.setName(rs.getString("name"));
                    room.setDescription(rs.getString("description"));
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

}
