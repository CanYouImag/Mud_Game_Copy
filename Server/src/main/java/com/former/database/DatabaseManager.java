package com.former.database;

import server.Room;

import java.sql.*;
import java.util.*;


public class DatabaseManager {
	public static final String DB_URL = "jdbc:mysql://localhost:3306/mud_game?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
	public static final String DB_USER = "root";        //此处需要把数据库用户名和密码替换为实际的值
	public static final String DB_PASSWORD = "CWai@3210979";

	// 修改: 生成八位长的唯一数字字符串
	public static String generateUniquePlayerId() {
		String playerId;
		try (Connection conn =  getConnection(DB_URL, DB_USER, DB_PASSWORD);
			 Statement stmt = conn != null ? conn.createStatement() : null) {
			do {
				// 生成八位随机数字字符串
				playerId = String.format("%08d", new Random().nextInt(100000000));
				// 检查生成的ID是否已存在
				String query = "SELECT COUNT(*) FROM players WHERE player_id = ?";
				try (PreparedStatement pstmt = conn != null ? conn.prepareStatement(query) : null) {
					if (pstmt != null) {
						pstmt.setString(1, playerId);
						try (ResultSet rs = pstmt.executeQuery()) {
							if (rs.next() && rs.getInt(1) == 0) {
								// 如果ID不存在，则返回
								return playerId;
							}
						}
					}
				}
			} while (true); // 无限循环，直到找到唯一的ID
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("无法生成唯一的玩家ID", e);
		}
	}

	public static void initialize() {
		// 创建 maps 表
		String createMapsTable = "CREATE TABLE IF NOT EXISTS maps (" +
				"id VARCHAR(255) PRIMARY KEY, " + // 修改: 将 id 类型改为 VARCHAR(255)
				"name VARCHAR(255) UNIQUE NOT NULL, " +
				"description TEXT, " +
				"start_x INT NOT NULL, " +
				"start_y INT NOT NULL, " +
				"width INT NOT NULL, " +
				"height INT NOT NULL)";

		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			Statement stmt = conn != null ? conn.createStatement() : null) {
			if (stmt != null) {
				stmt.execute(createMapsTable);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// 创建 items 表
		String createItemsTable = "CREATE TABLE IF NOT EXISTS items (" +
				"id VARCHAR(255) PRIMARY KEY, " +
				"name VARCHAR(255) NOT NULL, " +
				"description TEXT, " +
				"room_id VARCHAR(255), " + // 允许 NULL
				"player_id VARCHAR(255), " + // 允许 NULL
				"FOREIGN KEY (room_id) REFERENCES rooms(id), " +
				"FOREIGN KEY (player_id) REFERENCES players(player_id), " +
				"CHECK ((room_id IS NULL AND player_id IS NOT NULL) OR (room_id IS NOT NULL AND player_id IS NULL))" +
				")";

		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			Statement stmt = conn != null ? conn.createStatement() : null) {
			if (stmt != null) {
				stmt.execute(createItemsTable);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// 新增方法: 保存地图信息到数据库
	public static void saveMap(String id, String name, String description, int startX, int startY, int width, int height) { // 修改: 添加 id 参数
		String sql = "INSERT INTO maps (id, name, description, start_x, start_y, width, height) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, id); // 修改: 设置 id 参数
				pstmt.setString(2, name);
				pstmt.setString(3, description);
				pstmt.setInt(4, startX);
				pstmt.setInt(5, startY);
				pstmt.setInt(6, width);
				pstmt.setInt(7, height);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 新增方法: 获取地图信息
	public static Map<String, Object> getMap(String mapId) {
		String sql = "SELECT * FROM maps WHERE id = ?";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, mapId); // 修改: 使用 String 类型的 mapId
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						Map<String, Object> mapData = new HashMap<>();
						mapData.put("id", rs.getString("id"));
						mapData.put("name", rs.getString("name"));
						mapData.put("description", rs.getString("description"));
						mapData.put("startX", rs.getInt("start_x"));
						mapData.put("startY", rs.getInt("start_y"));
						mapData.put("width", rs.getInt("width"));
						mapData.put("height", rs.getInt("height"));
						return mapData;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 修改: 保存玩家信息到数据库
	public static void savePlayer(String name, String password, String currentRoomId, String currentMapId, int level, int exp, String realm) {
		String playerId = generateUniquePlayerId(); // 生成唯一 player_id
		String sql = "INSERT INTO players (player_id, name, passwd, current_room_id, current_map_id, level, exp, realm) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, playerId); // 插入 player_id
				pstmt.setString(2, name);
				pstmt.setString(3, password);
				pstmt.setString(4, currentRoomId); // 插入当前房间ID
				pstmt.setString(5, currentMapId); // 插入当前地图ID
				pstmt.setInt(6, level); // 插入等级
				pstmt.setInt(7, exp); // 插入经验值
				pstmt.setString(8, realm); // 揿境界
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 修改: 获取玩家信息
	public static Map<String, Object> getPlayer(String name) {
		String sql = "SELECT * FROM players WHERE name = ?";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, name);
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						Map<String, Object> playerData = new HashMap<>();
						playerData.put("name", rs.getString("name"));
						playerData.put("passwd", rs.getString("passwd"));
						playerData.put("currentRoomId", rs.getString("current_room_id")); // 获取当前房间ID
						playerData.put("currentMapId", rs.getString("current_map_id")); // 获取当前地图ID
						playerData.put("level", rs.getInt("level")); // 获取等级
						playerData.put("exp", rs.getInt("exp")); // 获取经验值
						playerData.put("realm", rs.getString("realm")); // 获取境界
						return playerData;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 检查用户名是否已存在
	 **/
	public static boolean isUsernameExists(String username) {
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM players WHERE name = ?")) {
			statement.setString(1, username);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static Connection getConnection(String dbUrl, String dbUser, String dbPassword) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getPlayerIdByName(String name) {
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement statement = connection.prepareStatement("SELECT player_id FROM players WHERE name = ?")) {
			statement.setString(1, name);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString("player_id");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "0";
	}

	public boolean registerUser(String username, String id, String password) {
		// 检查用户名和ID是否已存在
		if (isUserExists(username, id)) {
			System.out.println("注册失败：用户名或ID已存在");
			return false;
		}
		// 插入新用户
		String sql = "INSERT INTO players (name, player_id, passwd) VALUES (?, ?, ?)";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, username);
				pstmt.setString(2, id);
				pstmt.setString(3, password);
				pstmt.executeUpdate();
				System.out.println("注册成功");
				return true;
			}
		} catch (SQLException e) {
			System.out.println("注册失败：" + e.getMessage());
			return false;
		}
		return false;
	}



	private boolean isUserExists(String username, String id) {
		String sql = "SELECT COUNT(*) FROM players WHERE name = ? OR player_id = ?";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, username);
				pstmt.setString(2, id);
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						return rs.getInt(1) > 0;
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("检查用户是否存在时出错：" + e.getMessage());
		}
		return false;
	}

	// 新增方法: 获取初始房间的 ID
	public static String getStartingRoomId() {
		String sql = "SELECT id FROM rooms WHERE name = '中州大殿'";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			try (ResultSet rs = pstmt != null ? pstmt.executeQuery() : null) {
				if (rs != null && rs.next()) {
					return rs.getString("id");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 如果没有找到中州大殿，默认返回 "001"
		return "001";
	}

	// 新增方法: 获取初始地图的 ID
	public static String getStartingMapId() {
		String sql = "SELECT id FROM maps WHERE name = '中州大殿'";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			try (ResultSet rs = pstmt != null ? pstmt.executeQuery() : null) {
				if (rs != null && rs.next()) {
					return rs.getString("id");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 如果没有找到中州大殿，默认返回 "1"
		return "1";
	}

	// 新增方法: 保存房间信息到数据库
	public static void saveRoom(String name, String description) {
		String sql = "INSERT INTO rooms (name, description) VALUES (?, ?)";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, name);
				pstmt.setString(2, description);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 新增方法: 获取房间信息
	public static Map<String, Object> getRoom(String roomId) {
		String sql = "SELECT * FROM rooms WHERE id = ?";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, roomId);
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						Map<String, Object> roomData = new HashMap<>();
						roomData.put("id", rs.getString("id"));
						roomData.put("name", rs.getString("name"));
						roomData.put("description", rs.getString("description"));
						roomData.put("exits", rs.getString("exits"));
						return roomData;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 修改: 保存物品信息到数据库
	public static void saveItem(String name, String description, String roomId, String playerId) {
		String sql = "INSERT INTO items (name, description, room_id, player_id) VALUES (?, ?, ?, ?)";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, name);
				pstmt.setString(2, description);
				// 如果物品被玩家持有，则 room_id 为 NULL
				pstmt.setString(3, playerId == null ? null : roomId);
				// 如果物品未被玩家持有，则 player_id 为 NULL
				pstmt.setString(4, roomId == null ? null : playerId);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 修改: 获取物品信息
	public static List<Map<String, Object>> getItems(String roomId, String playerId) {
		String sql = "SELECT * FROM items WHERE room_id = ? OR player_id = ?";
		List<Map<String, Object>> itemList = new ArrayList<>();
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, roomId);
				pstmt.setString(2, playerId);
				try (ResultSet rs = pstmt.executeQuery()) {
					while (rs.next()) {
						Map<String, Object> itemData = new HashMap<>();
						itemData.put("id", rs.getString("id"));
						itemData.put("name", rs.getString("name"));
						itemData.put("description", rs.getString("description"));
						// 根据 room_id 和 player_id 判断物品归属
						itemData.put("roomId", rs.getString("room_id"));
						itemData.put("playerId", rs.getString("player_id"));
						itemList.add(itemData);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return itemList;
	}

	// 新增方法: 保存 NPC 信息到数据库
	public static void saveNPC(String name, String description, String roomId) {
		String sql = "INSERT INTO npcs (name, description, room_id) VALUES (?, ?, ?)";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, name);
				pstmt.setString(2, description);
				pstmt.setString(3, roomId);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 新增方法: 获取 NPC 信息
	public static List<Map<String, Object>> getNPCs(String roomId) {
		String sql = "SELECT * FROM npcs WHERE room_id = ?";
		List<Map<String, Object>> npcList = new ArrayList<>();
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, roomId);
				try (ResultSet rs = pstmt.executeQuery()) {
					while (rs.next()) {
						Map<String, Object> npcData = new HashMap<>();
						npcData.put("id", rs.getString("id"));
						npcData.put("name", rs.getString("name"));
						npcData.put("description", rs.getString("description"));
						npcData.put("roomId", rs.getString("room_id"));
						npcList.add(npcData);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return npcList;
	}

	// 修改: 返回 List<Room> 而非 List<Maps>
	public static List<Room> loadRooms() {
		List<Room> rooms = new ArrayList<>();
		String sql = "SELECT * FROM rooms";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			Statement stmt = conn != null ? conn.createStatement() : null) {
			if (stmt != null) {
				try (ResultSet rs = stmt.executeQuery(sql)) {
					while (rs.next()) {
						// 创建 Room 对象并设置属性
						Room room = new Room(rs.getString("id"), rs.getString("name"), rs.getString("description"));
						rooms.add(room);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rooms;
	}

	public static void updatePlayerTableSchema() {
		String[] sqlStatements = {
				"ALTER TABLE players ADD COLUMN level INT DEFAULT 1;",
				"ALTER TABLE players ADD COLUMN exp INT DEFAULT 0;",
				"ALTER TABLE players ADD COLUMN realm VARCHAR(255) DEFAULT '凡人';"
		};

		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			for (String sql : sqlStatements) {
				if (conn != null) {
					try (Statement stmt = conn.createStatement()) {
						stmt.execute(sql);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 新增方法: 保存技能信息到数据库
	public static void saveSkill(String name, String description, int requiredLevel) {
		String sql = "INSERT INTO skills (name, description, required_level) VALUES (?, ?, ?)";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, name);
				pstmt.setString(2, description);
				pstmt.setInt(3, requiredLevel);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 新增方法: 获取技能信息
	public static List<Map<String, Object>> getSkills() {
		String sql = "SELECT * FROM skills";
		List<Map<String, Object>> skillList = new ArrayList<>();
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				try (ResultSet rs = pstmt.executeQuery()) {
					while (rs.next()) {
						Map<String, Object> skillData = new HashMap<>();
						skillData.put("id", rs.getLong("id"));
						skillData.put("name", rs.getString("name"));
						skillData.put("description", rs.getString("description"));
						skillData.put("requiredLevel", rs.getInt("required_level"));
						skillList.add(skillData);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return skillList;
	}

	// 新增方法: 保存已学习技能信息到数据库 (针对 learned_skill 表)
	public static void saveLearnedSkill(long playerId, long skillId) {
		String sql = "INSERT INTO player_learned_skills (player_id, player_learned_skills.learned_skills_id) VALUES (?, ?)";
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, String.valueOf(playerId));
				pstmt.setLong(2, skillId);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 新增方法: 获取玩家已学习的技能 (针对 learned_skill 表)
	public static List<Map<String, Object>> getLearnedSkills(String playerId) {
		String sql = "SELECT pls.* FROM player_learned_skills pls WHERE pls.player_id = ?";
		List<Map<String, Object>> learnedSkills = new ArrayList<>();
		try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
			PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
			if (pstmt != null) {
				pstmt.setString(1, playerId);
				try (ResultSet rs = pstmt.executeQuery()) {
					while (rs.next()) {
						Map<String, Object> skillData = new HashMap<>();
						skillData.put("id", rs.getLong("id"));
						skillData.put("skillId", rs.getLong("skill_id"));
						learnedSkills.add(skillData);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return learnedSkills;
	}
}


