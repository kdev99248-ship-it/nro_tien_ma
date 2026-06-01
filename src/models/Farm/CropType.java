package models.Farm;

/**
 * Wrapper class để tương thích ngược với code cũ
 * Sử dụng CropTemplate làm nguồn dữ liệu thực tế
 * 
 * NOTE: Dữ liệu được load từ database thông qua CropTemplate
 * Để thêm loại cây mới, hãy thêm vào bảng crop_template trong database
 * 
 * @author TOMAHOC
 */
public class CropType {

    public byte id;
    public String name;
    public short seedItemId;
    public short harvestItemId;
    public long growthTimeMs;
    public int minHarvest;
    public int maxHarvest;
    public short[] stageIcons;

    // Giữ lại array CROP_TYPES để tương thích với code cũ
    // Sẽ được update khi CropTemplate load xong
    public static CropType[] CROP_TYPES = new CropType[0];

    public CropType() {
        this.stageIcons = new short[5];
    }

    public CropType(byte id, String name, short seedItemId, short harvestItemId,
            long growthTimeMs, int minHarvest, int maxHarvest) {
        this.id = id;
        this.name = name;
        this.seedItemId = seedItemId;
        this.harvestItemId = harvestItemId;
        this.growthTimeMs = growthTimeMs;
        this.minHarvest = minHarvest;
        this.maxHarvest = maxHarvest;
        this.stageIcons = new short[5];
    }

    /**
     * Lấy CropType theo ID
     * Sử dụng CropTemplate làm nguồn dữ liệu
     */
    public static CropType getById(byte id) {
        CropTemplate template = CropTemplate.getById(id);
        if (template != null) {
            return fromTemplate(template);
        }
        return null;
    }

    /**
     * Lấy CropType theo item ID của hạt giống
     * Sử dụng CropTemplate làm nguồn dữ liệu
     */
    public static CropType getBySeedItemId(short seedItemId) {
        CropTemplate template = CropTemplate.getBySeedItemId(seedItemId);
        if (template != null) {
            return fromTemplate(template);
        }
        return null;
    }

    /**
     * Kiểm tra item có phải là hạt giống không
     */
    public static boolean isSeedItem(short itemId) {
        return CropTemplate.isSeedItem(itemId);
    }

    /**
     * Chuyển đổi từ CropTemplate sang CropType
     */
    public static CropType fromTemplate(CropTemplate template) {
        CropType cropType = new CropType();
        cropType.id = template.id;
        cropType.name = template.name;
        cropType.seedItemId = template.seedItemId;
        cropType.harvestItemId = template.harvestItemId;
        cropType.growthTimeMs = template.totalGrowthTimeMs;
        cropType.minHarvest = template.minHarvest;
        cropType.maxHarvest = template.maxHarvest;
        cropType.stageIcons = template.stageIcons.clone();
        return cropType;
    }

    /**
     * Refresh CROP_TYPES array từ CropTemplate
     * Gọi sau khi load CropTemplate từ database
     */
    public static void refreshFromTemplates() {
        CROP_TYPES = new CropType[CropTemplate.CROP_TEMPLATES.size()];
        for (int i = 0; i < CropTemplate.CROP_TEMPLATES.size(); i++) {
            CropTemplate template = CropTemplate.CROP_TEMPLATES.get(i);
            CROP_TYPES[i] = fromTemplate(template);
        }
    }

    /**
     * Tính tổng thời gian từ gieo đến thu hoạch
     */
    public long getTotalGrowthTime() {
        return this.growthTimeMs * 4;
    }

    /**
     * Lấy thời gian hiển thị (phút)
     */
    public int getGrowthTimeMinutes() {
        return (int) (this.growthTimeMs / 60000);
    }

    /**
     * Lấy tổng thời gian hiển thị (phút)
     */
    public int getTotalGrowthTimeMinutes() {
        return (int) (getTotalGrowthTime() / 60000);
    }

    /**
     * Lấy tổng thời gian hiển thị dạng chuỗi
     */
    public String getTotalGrowthTimeString() {
        long totalSeconds = getTotalGrowthTime() / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes > 0) {
            return minutes + " phút " + (seconds > 0 ? seconds + " giây" : "");
        }
        return seconds + " giây";
    }

    @Override
    public String toString() {
        return name + " (ID: " + id + ", Thời gian: " + getTotalGrowthTimeMinutes() + " phút)";
    }
}
