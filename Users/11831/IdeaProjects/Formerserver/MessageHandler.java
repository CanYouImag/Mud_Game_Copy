public String handleMessage(String message) {
    String[] parts = message.split(" ");
    String command = parts[0].toLowerCase();
    String[] args = Arrays.copyOfRange(parts, 1, parts.length);

    switch (command) {
        case "look":
            return player.getCurrentRoom().getDescription();
        case "move":
            if (args.length == 0) {
                return "请输入方向，例如：move north";
            }
            String direction = args[0];
            player.move(Direction.valueOf(direction.toUpperCase()));
            return "你向 " + direction + " 移动了。";
        case "get":
            if (args.length == 0) {
                return "请输入要拾取的物品名称，例如：get sword";
            }
            String itemName = args[0];
            for (Items item : player.getCurrentRoom().getItems()) {
                if (item.getName().equalsIgnoreCase(itemName)) {
                    player.getCurrentRoom().removeItem(item);
                    player.addItem(item);
                    return "你获取了物品：" + item.getName();
                }
            }
            return "物品 " + itemName + " 不存在于此房间。";
        case "drop":
            if (args.length == 0) {
                return "请输入要丢弃的物品名称，例如：drop sword";
            }
            itemName = args[0];
            for (Items item : player.getInventory()) {
                if (item.getName().equalsIgnoreCase(itemName)) {
                    player.removeItem(item);
                    player.getCurrentRoom().addItem(item);
                    return "你丢弃了物品：" + item.getName();
                }
            }
            return "你没有持有物品 " + itemName + "。";
        case "say":
            if (args.length == 0) {
                return "聊天命令缺少消息内容，请输入 'say [消息]'。";
            }
            String chatMessage = String.join(" ", args);
            player.getCurrentRoom().broadcast(player.getName() + " says: " + chatMessage);
            return ""; // 不返回任何内容，避免重复显示
        default:
            return "未知命令：" + command;
    }
}