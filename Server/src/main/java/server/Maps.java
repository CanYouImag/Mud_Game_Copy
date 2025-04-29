package server;

import javax.persistence.*;

import static server.Room.*;

@Entity
@Table(name = "maps")
public class Maps {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String name;

	private String description;

	// 新增注释：表示地图的起始位置的X坐标，用于定义玩家进入地图时的初始位置
	private int startX;

	// 新增注释：表示地图的起始位置的Y坐标，用于定义玩家进入地图时的初始位置
	private int startY;

	// 新增注释：表示地图的宽度和高度，用于定义地图的二维空间大小
	private int width;
	private int height;

	@Column(columnDefinition = "TEXT")

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getStartX() {
		return startX;
	}

	public void setStartX(int startX) {
		this.startX = startX;
	}

	public int getStartY() {
		return startY;
	}

	public void setStartY(int startY) {
		this.startY = startY;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isEmpty() {
		return rooms.isEmpty();
	}
}