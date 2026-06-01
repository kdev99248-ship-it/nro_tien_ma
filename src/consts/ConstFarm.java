package consts;

import models.Farm.CropTemplate;

/**
 * Hằng số cho hệ thống Khu vườn trên mây (Cloud Garden / Farming System)
 * 
 * NOTE: Dữ liệu về loại cây trồng (crop types) đã được chuyển sang database
 * bảng crop_template. Sử dụng CropTemplate class để truy cập.
 * 
 * @author TOMAHOC
 */
public class ConstFarm {

    // ===================== MAP CONFIGURATION =====================
    // Map ID cho khu vườn trên mây (sử dụng lại map nhà theo gender)
    public static final int MAP_CLOUD_GARDEN_TD = 39; // Trái Đất (gender 0) - Map nhà
    public static final int MAP_CLOUD_GARDEN_NM = 40; // Namếc (gender 1) - Map nhà
    public static final int MAP_CLOUD_GARDEN_XD = 41; // Xayda (gender 2) - Map nhà

    // ===================== PLOT CONFIGURATION =====================
    // Số ô ruộng
    public static final int INITIAL_PLOTS = 5; // Số ô mở khóa ban đầu miễn phí
    public static final int MAX_PLOTS = 10; // Số ô tối đa
    public static final int PLOT_UNLOCK_COST_GOLD = 1000000; // Chi phí mở khóa ô mới (vàng)
    public static final int PLOT_UNLOCK_COST_GEM = 10; // Chi phí mở khóa ô mới (ngọc)

    // ===================== GROWTH STAGES =====================
    // Các giai đoạn phát triển của cây
    public static final byte STAGE_EMPTY = 0; // Đất trống
    public static final byte STAGE_SEED = 1; // Hạt giống (vừa gieo)
    public static final byte STAGE_SPROUT_1 = 2; // Mầm 1
    public static final byte STAGE_SPROUT_2 = 3; // Mầm 2
    public static final byte STAGE_YOUNG = 4; // Cây non
    public static final byte STAGE_MATURE = 5; // Cây trưởng thành (có thể thu hoạch)
    public static final byte STAGE_WITHERED = 6; // Cây héo (nếu không thu hoạch kịp)

    // ===================== ICON IDs =====================
    // Icon cho các giai đoạn (sẽ cập nhật khi có assets)
    public static final short ICON_PLOT_EMPTY = 2000; // Ô đất trống
    public static final short ICON_ARROW_DOWN = 2001; // Mũi tên trỏ xuống
    public static final short ICON_ARROW_DOWN_1 = 2007; // Mũi tên trỏ xuống 1
    public static final short ICON_ARROW_DOWN_2 = 2008; // Mũi tên trỏ xuống 2
    public static final short ICON_HOE = 2002; // Icon cuốc
    public static final short ICON_SEED = 2003; // Icon gieo hạt
    public static final short ICON_WATER = 2004; // Icon tưới nước
    public static final short ICON_HARVEST = 2005; // Icon thu hoạch
    public static final short ICON_KHUNG_RAUCU = 2006; // Khung rau củ

    // ===================== NPC MENU INDEX =====================
    // Menu index cho tương tác với ô ruộng
    public static final int MENU_FARM_PLOT_BASE = 6000;
    public static final int MENU_FARM_PLOT_ACTION = 6001; // Menu chọn hành động
    public static final int MENU_FARM_SELECT_SEED = 6002; // Menu chọn hạt giống
    public static final int MENU_FARM_CONFIRM_HARVEST = 6003; // Xác nhận thu hoạch
    public static final int MENU_FARM_UNLOCK_PLOT = 6004; // Mở khóa ô mới
    public static final int MENU_FARM_WATER = 6005; // Menu bón phân (đã đổi từ tưới nước)
    public static final int MENU_FARM_CONFIRM_DESTROY = 6006; // Xác nhận phá bỏ cây trồng
    public static final int MENU_FARM_SELECT_SEED_MASS = 6007; // Menu chọn hạt giống gieo hàng loạt
    public static final int MENU_FARM_SELECT_FERTILIZER = 6008; // Menu chọn loại phân bón
    public static final int MENU_FARM_CONFIRM_PESTICIDE = 6009; // Menu xác nhận dùng thuốc trừ sâu

    // ===================== FERTILIZER ITEMS =====================
    // Item ID phân bón
    public static final short FERTILIZER_5M = 1922; // Phân bón 5 phút
    public static final short FERTILIZER_10M = 1923; // Phân bón 10 phút
    public static final short FERTILIZER_20M = 1924; // Phân bón 20 phút
    public static final short FERTILIZER_30M = 1925; // Phân bón 30 phút

    // ===================== PESTICIDE ITEM =====================
    // Item ID thuốc trừ sâu (bình thuốc trừ sâu)
    public static final short PESTICIDE_ITEM_ID = 1926;

    // ===================== WITHER CONFIGURATION =====================
    // Thời gian (ms) sau khi cây có thể thu hoạch (STAGE_MATURE + ready) để tính tỷ
    // lệ héo
    // Mốc thời gian và tỷ lệ héo tương ứng
    public static final long WITHER_TIME_5M = 5 * 60 * 1000L; // 5 phút -> 40%
    public static final long WITHER_TIME_20M = 20 * 60 * 1000L; // 20 phút -> 60%
    public static final long WITHER_TIME_30M = 30 * 60 * 1000L; // 30 phút -> 70%
    public static final long WITHER_TIME_1H = 60 * 60 * 1000L; // 1 giờ -> 100%

    public static final int WITHER_CHANCE_5M = 40; // 40%
    public static final int WITHER_CHANCE_20M = 60; // 60%
    public static final int WITHER_CHANCE_30M = 70; // 70%
    public static final int WITHER_CHANCE_1H = 100; // 100%

    // Hệ số giảm sản lượng khi cây bị héo (giảm 20%)
    public static final float WITHER_HARVEST_PENALTY = 0.80f;

    /**
     * Lấy thời gian tăng tốc của phân bón (milliseconds)
     */
    public static long getFertilizerBoostTimeMs(short itemId) {
        switch (itemId) {
            case FERTILIZER_5M:
                return 5 * 60 * 1000L;
            case FERTILIZER_10M:
                return 10 * 60 * 1000L;
            case FERTILIZER_20M:
                return 20 * 60 * 1000L;
            case FERTILIZER_30M:
                return 30 * 60 * 1000L;
            default:
                return 0;
        }
    }

    /**
     * Lấy tên phân bón
     */
    public static String getFertilizerName(short itemId) {
        switch (itemId) {
            case FERTILIZER_5M:
                return "Phân bón 5p";
            case FERTILIZER_10M:
                return "Phân bón 10p";
            case FERTILIZER_20M:
                return "Phân bón 20p";
            case FERTILIZER_30M:
                return "Phân bón 30p";
            default:
                return "Phân bón";
        }
    }

    /**
     * Kiểm tra item có phải phân bón không
     */
    public static boolean isFertilizerItem(short itemId) {
        return itemId == FERTILIZER_5M || itemId == FERTILIZER_10M
                || itemId == FERTILIZER_20M || itemId == FERTILIZER_30M;
    }

    /**
     * Kiểm tra item có phải thuốc trừ sâu không
     */
    public static boolean isPesticideItem(short itemId) {
        return itemId == PESTICIDE_ITEM_ID;
    }

    /**
     * Danh sách tất cả item ID phân bón
     */
    public static final short[] ALL_FERTILIZERS = {
            FERTILIZER_5M, FERTILIZER_10M, FERTILIZER_20M, FERTILIZER_30M
    };

    /**
     * Lấy tỷ lệ héo (%) dựa trên thời gian đã qua kể từ khi cây có thể thu hoạch
     * 
     * @param timeSinceHarvestReady thời gian đã qua (ms)
     * @return tỷ lệ héo (0-100), trả về 0 nếu chưa đủ thời gian
     */
    public static int getWitherChance(long timeSinceHarvestReady) {
        if (timeSinceHarvestReady >= WITHER_TIME_1H) {
            return WITHER_CHANCE_1H;
        } else if (timeSinceHarvestReady >= WITHER_TIME_30M) {
            return WITHER_CHANCE_30M;
        } else if (timeSinceHarvestReady >= WITHER_TIME_20M) {
            return WITHER_CHANCE_20M;
        } else if (timeSinceHarvestReady >= WITHER_TIME_5M) {
            return WITHER_CHANCE_5M;
        }
        return 0; // chưa đủ 5 phút, chưa có nguy cơ héo
    }

    // ===================== HARVEST QUANTITY =====================
    // Số lượng sản phẩm thu hoạch
    public static final int MIN_HARVEST_QUANTITY = 1;
    public static final int MAX_HARVEST_QUANTITY = 5;

    public static final byte DATA_HARVEST_SUCCESS = 4; // Data type cho hiệu ứng thu hoạch

    // ===================== ASSETS PATH =====================
    // Đường dẫn assets
    public static final String FARM_ASSETS_PATH = "data/famer/";

    // Tên file assets cho các giai đoạn
    public static final String ASSET_PLOT_EMPTY = "plot_empty.png";
    public static final String ASSET_STAGE_SEED = "seed.png";
    public static final String ASSET_STAGE_SPROUT_1 = "sprout_1.png";
    public static final String ASSET_STAGE_SPROUT_2 = "sprout_2.png";
    public static final String ASSET_STAGE_YOUNG = "young_plant.png";
    public static final String ASSET_STAGE_MATURE = "mature_plant.png";
    public static final String ASSET_STAGE_WITHERED = "withered.png";

    /**
     * Lấy map ID khu vườn theo gender
     */
    public static int getCloudGardenMapId(int gender) {
        return MAP_CLOUD_GARDEN_TD + gender;
    }

    /**
     * Lấy item ID hạt giống theo loại cây
     * Dữ liệu lấy từ database thông qua CropTemplate
     */
    public static short getSeedItemId(byte cropType) {
        CropTemplate crop = CropTemplate.getById(cropType);
        return crop != null ? crop.seedItemId : -1;
    }

    /**
     * Lấy item ID sản phẩm thu hoạch theo loại cây
     * Dữ liệu lấy từ database thông qua CropTemplate
     */
    public static short getHarvestItemId(byte cropType) {
        CropTemplate crop = CropTemplate.getById(cropType);
        return crop != null ? crop.harvestItemId : -1;
    }

    /**
     * Lấy tên loại cây
     * Dữ liệu lấy từ database thông qua CropTemplate
     */
    public static String getCropName(byte cropType) {
        CropTemplate crop = CropTemplate.getById(cropType);
        return crop != null ? crop.name : "Không xác định";
    }

    /**
     * Lấy tên giai đoạn phát triển
     */
    public static String getStageName(byte stage) {
        switch (stage) {
            case STAGE_EMPTY:
                return "Đất trống";
            case STAGE_SEED:
                return "Hạt giống";
            case STAGE_SPROUT_1:
                return "Mầm 1";
            case STAGE_SPROUT_2:
                return "Mầm 2";
            case STAGE_YOUNG:
                return "Cây non";
            case STAGE_MATURE:
                return "Trưởng thành";
            case STAGE_WITHERED:
                return "Héo";
            default:
                return "Không xác định";
        }
    }

    // ===================== ZOOM-BASED ASSET HELPERS =====================

    /**
     * Lấy đường dẫn thư mục assets theo zoom level
     * 
     * @param zoomLevel Mức zoom (1-4)
     * @return Đường dẫn thư mục VD: "data/famer/x2/"
     */
    public static String getAssetPath(byte zoomLevel) {
        int zoom = Math.max(1, Math.min(4, zoomLevel)); // Giới hạn 1-4
        return FARM_ASSETS_PATH + "x" + zoom + "/";
    }

    /**
     * Lấy đường dẫn file asset ô ruộng trống theo zoom
     * 
     * @param zoomLevel Mức zoom (1-4)
     * @return Đường dẫn file VD: "data/famer/x2/plot_empty.png"
     */
    public static String getPlotEmptyAsset(byte zoomLevel) {
        return getAssetPath(zoomLevel) + ASSET_PLOT_EMPTY;
    }

    /**
     * Lấy đường dẫn file asset cây theo loại cây và giai đoạn
     * Lấy cấu hình từ database (nếu có) thông qua CropTemplate
     * 
     * @param zoomLevel Mức zoom (1-4)
     * @param cropType  Loại cây (ID từ database)
     * @param stage     Giai đoạn (STAGE_SEED ... STAGE_WITHERED)
     * @return Đường dẫn file
     */
    public static String getCropStageAsset(byte zoomLevel, byte cropType, byte stage) {
        String basePath = getAssetPath(zoomLevel);

        CropTemplate cropTemplate = CropTemplate.getById(cropType);
        if (cropTemplate != null) {
            String fileName = cropTemplate.getStageAssetFilename(stage);
            if (fileName != null) {
                return basePath + fileName;
            }
        }

        // Fallback default naming just in case
        String stageName = getStageAssetName(stage);
        if (isEarlyStage(stage)) {
            return basePath + "crop_common_" + stageName + ".png";
        }
        String cropName = getCropAssetName(cropType);
        return basePath + "crop_" + cropName + "_" + stageName + ".png";
    }

    /**
     * Kiểm tra xem giai đoạn có phải là giai đoạn đầu (dùng chung asset) không
     * Seed, Sprout1, Sprout2 = dùng chung
     * Young, Mature, Withered = riêng từng loại cây
     */
    public static boolean isEarlyStage(byte stage) {
        return stage == STAGE_SEED || stage == STAGE_SPROUT_1 || stage == STAGE_SPROUT_2;
    }

    /**
     * Lấy đường dẫn file asset icon theo zoom
     * 
     * @param zoomLevel Mức zoom (1-4)
     * @param iconName  Tên icon (VD: "hoe", "water", "harvest")
     * @return Đường dẫn file VD: "data/famer/x2/icon_hoe.png"
     */
    public static String getIconAsset(byte zoomLevel, String iconName) {
        return getAssetPath(zoomLevel) + "icon_" + iconName + ".png";
    }

    /**
     * Lấy tên asset theo loại cây (dùng trong filename)
     * Mapping cứng vì file asset được đặt tên theo tiếng Anh
     */
    public static String getCropAssetName(byte cropType) {
        // Mapping theo ID crop type
        switch (cropType) {
            case 0:
                return "tomato";
            case 1:
                return "starfruit";
            case 2:
                return "corn";
            case 3:
                return "pumpkin";
            default:
                return "unknown";
        }
    }

    /**
     * Lấy tên asset theo giai đoạn (dùng trong filename)
     */
    public static String getStageAssetName(byte stage) {
        switch (stage) {
            case STAGE_EMPTY:
                return "empty";
            case STAGE_SEED:
                return "seed";
            case STAGE_SPROUT_1:
                return "sprout1";
            case STAGE_SPROUT_2:
                return "sprout2";
            case STAGE_YOUNG:
                return "young";
            case STAGE_MATURE:
                return "mature";
            case STAGE_WITHERED:
                return "withered";
            default:
                return "unknown";
        }
    }

    /**
     * Lấy danh sách tất cả zoom levels được hỗ trợ
     */
    public static int[] getSupportedZoomLevels() {
        return new int[] { 1, 2, 3, 4 };
    }

    /**
     * Kiểm tra zoom level có hợp lệ không
     */
    public static boolean isValidZoomLevel(byte zoomLevel) {
        return zoomLevel >= 1 && zoomLevel <= 4;
    }
}
