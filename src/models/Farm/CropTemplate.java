package models.Farm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import consts.ConstFarm;

/**
 * Template cho các loại cây trồng trong hệ thống Khu vườn trên mây
 * Dữ liệu được load từ database bảng crop_template
 * 
 * @author TOMAHOC
 */
public class CropTemplate {

    // ID loại cây
    public byte id;

    // Tên cây
    public String name;

    // Item ID của hạt giống
    public short seedItemId;

    // Item ID của sản phẩm thu hoạch
    public short harvestItemId;

    // Tổng thời gian phát triển (ms) - từ database
    public long totalGrowthTimeMs;

    // Thời gian cho từng giai đoạn (ms) - tính theo tỷ lệ
    // Index: 0=SEED, 1=SPROUT_1, 2=SPROUT_2, 3=YOUNG, 4=MATURE->WITHERED
    public long[] stageTimesMs;

    // Số lượng thu hoạch tối thiểu
    public int minHarvest;

    // Số lượng thu hoạch tối đa
    public int maxHarvest;

    // Icon cho từng giai đoạn [SEED, SPROUT_1, SPROUT_2, YOUNG, MATURE]
    public short[] stageIcons;

    // Tên file ảnh cho các giai đoạn riêng theo loại cây (từ DB)
    // VD: "crop_tomato_young.png", "crop_tomato_mature.png",
    // "crop_tomato_withered.png"
    public String imgYoung; // Ảnh giai đoạn cây non (STAGE_YOUNG)
    public String imgMature; // Ảnh giai đoạn trưởng thành (STAGE_MATURE)
    public String imgWithered; // Ảnh giai đoạn héo (STAGE_WITHERED)

    // ===================== STAGE RATIO CONFIGURATION =====================
    // Tỷ lệ thời gian: 1:2:2:3:2 (SEED:SPROUT1:SPROUT2:YOUNG:MATURE->WITHERED)
    public static final int[] STAGE_RATIOS = { 1, 2, 2, 3, 2 };
    public static final int TOTAL_RATIO = 1 + 2 + 2 + 3 + 2; // = 10

    // ===================== STATIC DATA =====================
    // Danh sách các loại cây từ database
    public static final List<CropTemplate> CROP_TEMPLATES = new ArrayList<>();

    // Map để truy cập nhanh theo ID
    public static final Map<Byte, CropTemplate> CROP_TEMPLATE_MAP = new HashMap<>();

    // Map để truy cập nhanh theo seed item ID
    public static final Map<Short, CropTemplate> CROP_BY_SEED_MAP = new HashMap<>();

    public CropTemplate() {
        this.stageIcons = new short[5];
        this.stageTimesMs = new long[5];
    }

    public CropTemplate(byte id, String name, short seedItemId, short harvestItemId,
            long totalGrowthTimeMs, int minHarvest, int maxHarvest) {
        this.id = id;
        this.name = name;
        this.seedItemId = seedItemId;
        this.harvestItemId = harvestItemId;
        this.totalGrowthTimeMs = totalGrowthTimeMs;
        this.minHarvest = minHarvest;
        this.maxHarvest = maxHarvest;
        this.stageIcons = new short[5];
        this.stageTimesMs = new long[5];
        calculateStageTimes();
    }

    /**
     * Tính thời gian cho từng giai đoạn theo tỷ lệ 1:2:2:3:2
     */
    public void calculateStageTimes() {
        for (int i = 0; i < STAGE_RATIOS.length; i++) {
            stageTimesMs[i] = (totalGrowthTimeMs * STAGE_RATIOS[i]) / TOTAL_RATIO;
        }
    }

    /**
     * Lấy thời gian cho giai đoạn cụ thể (ms)
     * 
     * @param stage Giai đoạn (STAGE_SEED=1, STAGE_SPROUT_1=2, ...)
     * @return Thời gian (ms) để chuyển sang giai đoạn tiếp theo
     */
    public long getStageTimeMs(byte stage) {
        // stage 1-5 tương ứng với index 0-4
        int index = stage - ConstFarm.STAGE_SEED;
        if (index >= 0 && index < stageTimesMs.length) {
            return stageTimesMs[index];
        }
        return 0;
    }

    /**
     * Lấy tên file ảnh cho giai đoạn cụ thể (lấy từ DB, fallback về naming
     * convention cũ)
     * Giai đoạn đầu (SEED, SPROUT_1, SPROUT_2) dùng chung: crop_common_<stage>.png
     * Giai đoạn sau (YOUNG, MATURE, WITHERED) dùng riêng theo loại cây từ DB
     * 
     * @param stage Giai đoạn (STAGE_SEED=1 ... STAGE_WITHERED=6)
     * @return Tên file ảnh (VD: "crop_tomato_mature.png")
     */
    public String getStageAssetFilename(byte stage) {
        switch (stage) {
            case ConstFarm.STAGE_SEED:
                return "crop_common_seed.png";
            case ConstFarm.STAGE_SPROUT_1:
                return "crop_common_sprout1.png";
            case ConstFarm.STAGE_SPROUT_2:
                return "crop_common_sprout2.png";
            case ConstFarm.STAGE_YOUNG:
                return formatFilename(imgYoung);
            case ConstFarm.STAGE_MATURE:
                return formatFilename(imgMature);
            case ConstFarm.STAGE_WITHERED:
                return formatFilename(imgWithered);
            default:
                return null;
        }
    }

    /**
     * Tự động thêm đuôi .png nếu trong DB chỉ lưu id/tên
     */
    private String formatFilename(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (!name.endsWith(".png")) {
            return name + ".png";
        }
        return name;
    }

    // ===================== STATIC METHODS =====================

    /**
     * Lấy CropTemplate theo ID
     */
    public static CropTemplate getById(byte id) {
        return CROP_TEMPLATE_MAP.get(id);
    }

    /**
     * Lấy CropTemplate theo item ID của hạt giống
     */
    public static CropTemplate getBySeedItemId(short seedItemId) {
        return CROP_BY_SEED_MAP.get(seedItemId);
    }

    /**
     * Kiểm tra item có phải là hạt giống không
     */
    public static boolean isSeedItem(short itemId) {
        return CROP_BY_SEED_MAP.containsKey(itemId);
    }

    /**
     * Lấy số lượng loại cây
     */
    public static int getCropTypeCount() {
        return CROP_TEMPLATES.size();
    }

    /**
     * Tính tổng thời gian từ gieo đến thu hoạch được (bao gồm cả thời gian chín
     * muồi)
     */
    public long getTotalGrowthTime() {
        // Tổng 5 giai đoạn: SEED + SPROUT_1 + SPROUT_2 + YOUNG + MATURE (chín muồi)
        return stageTimesMs[0] + stageTimesMs[1] + stageTimesMs[2] + stageTimesMs[3] + stageTimesMs[4];
    }

    /**
     * Lấy tổng thời gian hiển thị (phút)
     */
    public int getTotalGrowthTimeMinutes() {
        return (int) (getTotalGrowthTime() / 60000);
    }

    /**
     * Lấy tổng thời gian hiển thị dạng chuỗi (phút:giây)
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
        return name + " (ID: " + id + ", Thời gian: " + getTotalGrowthTimeString() + ")";
    }

    /**
     * Clear all crop templates (dùng khi reload)
     */
    public static void clear() {
        CROP_TEMPLATES.clear();
        CROP_TEMPLATE_MAP.clear();
        CROP_BY_SEED_MAP.clear();
    }

    /**
     * Thêm crop template vào danh sách
     */
    public static void addCropTemplate(CropTemplate template) {
        CROP_TEMPLATES.add(template);
        CROP_TEMPLATE_MAP.put(template.id, template);
        CROP_BY_SEED_MAP.put(template.seedItemId, template);
    }
}
