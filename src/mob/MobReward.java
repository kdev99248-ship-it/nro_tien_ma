package mob;

import java.util.ArrayList;
import java.util.List;
import item.Item.ItemOption;

/**
 * Model đại diện cho một cấu hình drop item từ Database
 * Tương ứng với bảng `mob_reward`
 */
public class MobReward {

    public int id;
    public int mobId = -1; // -1 = tất cả quái
    public int mapId = -1; // -1 = tất cả map
    public int itemTemplateId; // ID item sẽ rơi
    public int rate = 100; // Tỉ lệ 1/rate
    public int quantityMin = 1; // Số lượng min
    public int quantityMax = 1; // Số lượng max
    public int gender = -1; // -1 = all, 0/1/2 = TDS/NM/XD
    public String eventKey; // Tên sự kiện (CHRISTMAS, HALLOWEEN...)
    public String mapType; // Loại map (MAP_COLD, MAP_SKH...)
    public String conditionType; // Điều kiện đặc biệt
    public boolean isRandomRange; // Random từ itemTemplateId đến id + randomRange
    public int randomRange; // Phạm vi random
    public boolean notifyGlobal; // Thông báo toàn server
    public String description; // Mô tả
    public boolean isActive = true; // Đang hoạt động

    // Options được parse từ JSON
    public List<ItemOption> options = new ArrayList<>();

    /**
     * Parse options từ chuỗi JSON đơn giản
     * Format: [{"id":30,"param":0},{"id":31,"param":5}]
     * Sử dụng parse thủ công để không cần thư viện JSON
     */
    public void parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty()) {
            return;
        }
        try {
            // Loại bỏ dấu ngoặc vuông đầu và cuối
            String content = optionsJson.trim();
            if (content.startsWith("[")) {
                content = content.substring(1);
            }
            if (content.endsWith("]")) {
                content = content.substring(0, content.length() - 1);
            }

            // Tách các object
            // Format: {"id":30,"param":0},{"id":31,"param":5}
            String[] objects = content.split("\\},\\{");

            for (String obj : objects) {
                // Loại bỏ dấu ngoặc nhọn còn sót
                obj = obj.replace("{", "").replace("}", "");

                int id = 0;
                int param = 0;

                // Tách các cặp key:value
                String[] pairs = obj.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split(":");
                    if (kv.length == 2) {
                        String key = kv[0].replace("\"", "").trim();
                        String value = kv[1].replace("\"", "").trim();

                        if ("id".equals(key)) {
                            id = Integer.parseInt(value);
                        } else if ("param".equals(key)) {
                            param = Integer.parseInt(value);
                        }
                    }
                }

                if (id > 0 || param > 0) {
                    options.add(new ItemOption(id, param));
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing options JSON: " + optionsJson + " - " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "MobReward{" +
                "id=" + id +
                ", itemTemplateId=" + itemTemplateId +
                ", rate=" + rate +
                ", eventKey='" + eventKey + '\'' +
                ", mapType='" + mapType + '\'' +
                '}';
    }
}
