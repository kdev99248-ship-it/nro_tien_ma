package models.Farm;

import consts.ConstFarm;

/**
 * Class đại diện cho một ô ruộng trong Khu vườn trên mây
 * Mỗi ô ruộng có thể ở các trạng thái khác nhau và trồng các loại cây khác nhau
 * 
 * @author TOMAHOC
 */
public class FarmPlot {

    private int plotId; // ID của ô ruộng (0-9)
    private boolean unlocked; // Ô đã được mở khóa chưa
    private byte currentStage; // Giai đoạn hiện tại (STAGE_EMPTY, STAGE_SEED, etc.)
    private byte cropType; // Loại cây đang trồng (-1 nếu trống)
    private long plantedTime; // Thời điểm gieo hạt
    private long lastStageChangeTime; // Thời điểm chuyển giai đoạn gần nhất
    private boolean watered; // Đã tưới nước chưa (phát triển sau)
    private long matureReadyTime; // Thời điểm cây sẵn sàng thu hoạch (sau giai đoạn chín muồi)

    // Vị trí hiển thị trên map
    private int posX;
    private int posY;

    public FarmPlot() {
        this.currentStage = ConstFarm.STAGE_EMPTY;
        this.cropType = -1;
        this.unlocked = false;
        this.watered = false;
    }

    public FarmPlot(int plotId, boolean unlocked) {
        this.plotId = plotId;
        this.unlocked = unlocked;
        this.currentStage = ConstFarm.STAGE_EMPTY;
        this.cropType = -1;
        this.watered = false;
    }

    // Getters and Setters
    public int getPlotId() {
        return plotId;
    }

    public void setPlotId(int plotId) {
        this.plotId = plotId;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public byte getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(byte currentStage) {
        this.currentStage = currentStage;
    }

    public byte getCropType() {
        return cropType;
    }

    public void setCropType(byte cropType) {
        this.cropType = cropType;
    }

    public long getPlantedTime() {
        return plantedTime;
    }

    public void setPlantedTime(long plantedTime) {
        this.plantedTime = plantedTime;
    }

    public long getLastStageChangeTime() {
        return lastStageChangeTime;
    }

    public void setLastStageChangeTime(long lastStageChangeTime) {
        this.lastStageChangeTime = lastStageChangeTime;
    }

    public boolean isWatered() {
        return watered;
    }

    public void setWatered(boolean watered) {
        this.watered = watered;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    /**
     * Kiểm tra và cập nhật giai đoạn phát triển của cây
     * Được gọi định kỳ hoặc khi player vào map
     * Thời gian mỗi giai đoạn theo tỷ lệ 1:2:2:3:2
     */
    public boolean update() {
        if (!unlocked || currentStage == ConstFarm.STAGE_EMPTY ||
                currentStage == ConstFarm.STAGE_WITHERED) {
            return false;
        }

        CropTemplate crop = CropTemplate.getById(cropType);
        if (crop == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastChange = currentTime - lastStageChangeTime;
        boolean changed = false;

        // Nếu đang ở STAGE_MATURE, kiểm tra héo
        if (currentStage == ConstFarm.STAGE_MATURE) {
            if (checkWither()) {
                return true;
            }
            return false;
        }

        // Kiểm tra có cần chuyển giai đoạn không
        // Mỗi giai đoạn có thời gian riêng theo tỷ lệ 1:2:2:3:2
        while (currentStage < ConstFarm.STAGE_MATURE) {
            long stageGrowthTime = crop.getStageTimeMs(currentStage);
            if (timeSinceLastChange >= stageGrowthTime) {
                currentStage++;
                lastStageChangeTime += stageGrowthTime;
                timeSinceLastChange -= stageGrowthTime;
                changed = true;
            } else {
                break;
            }
        }
        return changed;
    }

    /**
     * Kiểm tra và xử lý cây bị héo (STAGE_WITHERED)
     * Khi cây ở STAGE_MATURE và đã sẵn sàng thu hoạch, tính xác suất héo:
     * - Sau 5p: 40%
     * - Sau 20p: 60%
     * - Sau 30p: 70%
     * - Sau 1h: 100% (chắc chắn héo)
     * 
     * @return true nếu cây bị héo
     */
    public boolean checkWither() {
        if (currentStage != ConstFarm.STAGE_MATURE) {
            return false;
        }

        // Cần có matureReadyTime (được set khi cây sẵn sàng thu hoạch lần đầu)
        if (matureReadyTime <= 0) {
            // Kiểm tra xem cây đã sẵn sàng thu hoạch chưa
            CropTemplate crop = CropTemplate.getById(cropType);
            if (crop == null)
                return false;
            long matureTime = crop.getStageTimeMs(ConstFarm.STAGE_MATURE);
            long timeSinceMature = System.currentTimeMillis() - lastStageChangeTime;
            if (timeSinceMature >= matureTime) {
                // Cây đã sẵn sàng thu hoạch, ghi lại thời điểm
                matureReadyTime = lastStageChangeTime + matureTime;
            } else {
                return false; // chưa sẵn sàng thu hoạch, chưa có nguy cơ héo
            }
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceHarvestReady = currentTime - matureReadyTime;

        int witherChance = ConstFarm.getWitherChance(timeSinceHarvestReady);
        if (witherChance <= 0) {
            return false;
        }

        // Xác suất 100% = chắc chắn héo
        if (witherChance >= 100) {
            currentStage = ConstFarm.STAGE_WITHERED;
            return true;
        }

        // Random xác suất héo
        int roll = (int) (Math.random() * 100);
        if (roll < witherChance) {
            currentStage = ConstFarm.STAGE_WITHERED;
            return true;
        }

        return false;
    }

    /**
     * Sử dụng thuốc trừ sâu để chữa héo
     * Chuyển từ STAGE_WITHERED về STAGE_MATURE và reset bộ đếm héo
     * 
     * @return true nếu thành công
     */
    public boolean curePesticide() {
        if (currentStage != ConstFarm.STAGE_WITHERED) {
            return false;
        }

        // Chuyển về STAGE_MATURE
        currentStage = ConstFarm.STAGE_MATURE;
        // Reset bộ đếm héo - đặt matureReadyTime về hiện tại để bắt đầu lại
        matureReadyTime = System.currentTimeMillis();
        return true;
    }

    /**
     * Cuốc đất - chuẩn bị ô đất để gieo hạt
     * 
     * @return true nếu thành công
     */
    public boolean plow() {
        if (!unlocked) {
            return false;
        }
        // Nếu đất đang trống hoặc cây đã héo, có thể cuốc
        if (currentStage == ConstFarm.STAGE_EMPTY || currentStage == ConstFarm.STAGE_WITHERED) {
            reset();
            return true;
        }
        return false;
    }

    /**
     * Gieo hạt
     * 
     * @param cropType Loại cây muốn gieo
     * @return true nếu thành công
     */
    public boolean plant(byte cropType) {
        if (!unlocked || currentStage != ConstFarm.STAGE_EMPTY) {
            return false;
        }

        CropTemplate crop = CropTemplate.getById(cropType);
        if (crop == null) {
            return false;
        }

        this.cropType = cropType;
        this.currentStage = ConstFarm.STAGE_SEED;
        this.plantedTime = System.currentTimeMillis();
        this.lastStageChangeTime = this.plantedTime;
        this.watered = false;

        return true;
    }

    /**
     * Tưới nước (phát triển sau - có thể tăng tốc độ phát triển)
     * 
     * @return true nếu thành công
     */
    public boolean water() {
        if (!unlocked || currentStage == ConstFarm.STAGE_EMPTY ||
                currentStage == ConstFarm.STAGE_MATURE || watered) {
            return false;
        }

        this.watered = true;
        // TODO: Có thể giảm thời gian phát triển khi tưới nước
        return true;
    }

    /**
     * Thu hoạch cây
     * Cây có thể thu hoạch ở cả STAGE_MATURE và STAGE_WITHERED
     * Nếu cây bị héo (WITHERED), sản lượng giảm 20%
     * 
     * @return Số lượng sản phẩm thu được, -1 nếu không thể thu hoạch
     */
    public int harvest() {
        if (!isReadyToHarvest() && currentStage != ConstFarm.STAGE_WITHERED) {
            return -1;
        }

        CropTemplate crop = CropTemplate.getById(cropType);
        if (crop == null) {
            return -1;
        }

        boolean isWithered = (currentStage == ConstFarm.STAGE_WITHERED);

        // Tính số lượng thu hoạch ngẫu nhiên
        int harvestAmount = crop.minHarvest +
                (int) (Math.random() * (crop.maxHarvest - crop.minHarvest + 1));

        // Nếu cây bị héo, giảm sản lượng 20%
        if (isWithered) {
            harvestAmount = Math.max(1, (int) (harvestAmount * ConstFarm.WITHER_HARVEST_PENALTY));
        }

        // Reset ô ruộng
        reset();

        return harvestAmount;
    }

    /**
     * Reset ô ruộng về trạng thái ban đầu
     */
    public void reset() {
        this.currentStage = ConstFarm.STAGE_EMPTY;
        this.cropType = -1;
        this.plantedTime = 0;
        this.lastStageChangeTime = 0;
        this.watered = false;
        this.matureReadyTime = 0;
    }

    /**
     * Kiểm tra cây đã sẵn sàng thu hoạch chưa
     * Cây phải ở stage MATURE và đã qua thời gian chín muồi (phần 2/10 cuối)
     */
    public boolean isReadyToHarvest() {
        if (!unlocked || currentStage != ConstFarm.STAGE_MATURE) {
            return false;
        }

        CropTemplate crop = CropTemplate.getById(cropType);
        if (crop == null) {
            return true; // Fallback: cho thu hoạch nếu không tìm thấy crop
        }

        // Kiểm tra đã hết thời gian chín muồi chưa (phần 2/10 cuối của tổng thời gian)
        long currentTime = System.currentTimeMillis();
        long matureTime = crop.getStageTimeMs(ConstFarm.STAGE_MATURE);
        long timeSinceMature = currentTime - lastStageChangeTime;

        return timeSinceMature >= matureTime;
    }

    /**
     * Kiểm tra ô đất có trống không
     */
    public boolean isEmpty() {
        return currentStage == ConstFarm.STAGE_EMPTY;
    }

    /**
     * Kiểm tra cây có đang phát triển không
     */
    public boolean isGrowing() {
        return currentStage >= ConstFarm.STAGE_SEED && currentStage < ConstFarm.STAGE_MATURE;
    }

    /**
     * Lấy thời gian còn lại đến giai đoạn tiếp theo (giây)
     */
    public int getTimeToNextStage() {
        if (currentStage == ConstFarm.STAGE_EMPTY ||
                currentStage == ConstFarm.STAGE_MATURE ||
                currentStage == ConstFarm.STAGE_WITHERED) {
            return 0;
        }

        CropTemplate crop = CropTemplate.getById(cropType);
        if (crop == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long stageTime = crop.getStageTimeMs(currentStage);
        long nextStageTime = lastStageChangeTime + stageTime;
        long remainingTime = nextStageTime - currentTime;

        return (int) Math.max(0, remainingTime / 1000);
    }

    /**
     * Lấy thời gian còn lại đến khi thu hoạch (giây)
     * Bao gồm cả thời gian chín muồi ở stage MATURE (phần 2/10 cuối)
     */
    public int getTimeToHarvest() {
        if (currentStage == ConstFarm.STAGE_EMPTY) {
            return 0;
        }

        CropTemplate crop = CropTemplate.getById(cropType);
        if (crop == null) {
            return 0;
        }

        // Nếu đang ở stage MATURE, tính thời gian còn lại của giai đoạn chín muồi
        if (currentStage == ConstFarm.STAGE_MATURE) {
            long currentTime = System.currentTimeMillis();
            long matureTime = crop.getStageTimeMs(ConstFarm.STAGE_MATURE);
            long timeSinceMature = currentTime - lastStageChangeTime;
            long remainingTime = matureTime - timeSinceMature;
            return (int) Math.max(0, remainingTime / 1000);
        }

        // Tính thời gian còn lại của giai đoạn hiện tại
        int timeToNextStage = getTimeToNextStage();

        // Cộng thêm thời gian các giai đoạn còn lại (bao gồm cả MATURE)
        long remainingStagesTime = 0;
        for (byte stage = (byte) (currentStage + 1); stage <= ConstFarm.STAGE_MATURE; stage++) {
            remainingStagesTime += crop.getStageTimeMs(stage);
        }

        return timeToNextStage + (int) (remainingStagesTime / 1000);
    }

    /**
     * Lấy phần trăm hoàn thành của giai đoạn hiện tại
     */
    public float getStageProgress() {
        if (currentStage == ConstFarm.STAGE_EMPTY ||
                currentStage == ConstFarm.STAGE_MATURE ||
                currentStage == ConstFarm.STAGE_WITHERED) {
            return 0;
        }

        CropTemplate crop = CropTemplate.getById(cropType);
        if (crop == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceChange = currentTime - lastStageChangeTime;
        long stageTime = crop.getStageTimeMs(currentStage);
        return Math.min(1.0f, (float) timeSinceChange / stageTime);
    }

    /**
     * Lấy thông tin hiển thị
     */
    public String getDisplayInfo() {
        if (!unlocked) {
            return "Ô khóa";
        }

        if (currentStage == ConstFarm.STAGE_EMPTY) {
            return "Đất trống";
        }

        CropTemplate crop = CropTemplate.getById(cropType);
        String cropName = crop != null ? crop.name : "Không xác định";
        String stageName = ConstFarm.getStageName(currentStage);

        if (currentStage == ConstFarm.STAGE_WITHERED) {
            return cropName + " - Bị héo!\nSử dụng thuốc trừ sâu để chữa\nhoặc thu hoạch (giảm 20% sản lượng)";
        }

        if (isReadyToHarvest()) {
            return cropName + " - Có thể thu hoạch!";
        }

        int timeRemaining = getTimeToHarvest();
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;

        return cropName + " - " + stageName + "\nCòn " + minutes + " phút " + seconds + " giây";
    }

    /**
     * Clone FarmPlot để lưu trữ
     */
    public FarmPlot clonePlot() {
        FarmPlot clonedPlot = new FarmPlot();
        clonedPlot.plotId = this.plotId;
        clonedPlot.unlocked = this.unlocked;
        clonedPlot.currentStage = this.currentStage;
        clonedPlot.cropType = this.cropType;
        clonedPlot.plantedTime = this.plantedTime;
        clonedPlot.lastStageChangeTime = this.lastStageChangeTime;
        clonedPlot.watered = this.watered;
        clonedPlot.matureReadyTime = this.matureReadyTime;
        clonedPlot.posX = this.posX;
        clonedPlot.posY = this.posY;
        return clonedPlot;
    }

    // Getters/Setters cho matureReadyTime
    public long getMatureReadyTime() {
        return matureReadyTime;
    }

    public void setMatureReadyTime(long matureReadyTime) {
        this.matureReadyTime = matureReadyTime;
    }

    /**
     * Kiểm tra cây có đang bị héo không
     */
    public boolean isWithered() {
        return currentStage == ConstFarm.STAGE_WITHERED;
    }
}
