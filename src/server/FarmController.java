package server;

import consts.ConstFarm;
import network.Message;
import player.Player;
import services.FarmService;
import services.Service;
import utils.Logger;

/**
 * Controller xử lý các farm actions từ client
 * 
 * @author TOMAHOC
 */
public class FarmController {

    private static FarmController instance;

    public static FarmController gI() {
        if (instance == null) {
            instance = new FarmController();
        }
        return instance;
    }

    /**
     * Xử lý các farm actions từ client
     * 
     * @param player     Người chơi
     * @param farmAction Loại action (0-5)
     * @param msg        Message chứa data
     */
    public void handleFarmAction(Player player, byte farmAction, Message msg) {
        try {
            switch (farmAction) {
                case 0: // Interact with plot
                    handlePlotInteraction(player, msg);
                    break;
                case 1: // Plant seed
                    handlePlantSeed(player, msg);
                    break;
                case 2: // Harvest
                    handleHarvest(player, msg);
                    break;
                case 3: // Fertilize
                    handleFertilize(player, msg);
                    break;
                case 4: // Unlock plot
                    handleUnlockPlot(player, msg);
                    break;
                case 5: // Get garden data
                    handleGetGardenData(player);
                    break;
                default:
                    Logger.warning("FarmController: Unknown farm action " + farmAction + "\n");
                    break;
            }
        } catch (Exception e) {
            Logger.logException(FarmController.class, e);
        }
    }

    /**
     * Xử lý tương tác với ô ruộng
     */
    private void handlePlotInteraction(Player player, Message msg) {
        try {
            int plotId = msg.reader().readInt();

            // Kiểm tra xem player có đang ở map nhà không
            if (!isInHomeMap(player)) {
                Service.gI().sendThongBao(player, "Bạn phải ở nhà để tương tác với khu vườn!");
                return;
            }

            FarmService.gI().openPlotMenu(player, plotId);
        } catch (Exception e) {
            Logger.logException(FarmController.class, e);
        }
    }

    /**
     * Xử lý gieo hạt
     */
    private void handlePlantSeed(Player player, Message msg) {
        try {
            int plotId = msg.reader().readInt();
            short seedItemId = msg.reader().readShort();

            if (!isInHomeMap(player)) {
                Service.gI().sendThongBao(player, "Bạn phải ở nhà để trồng cây!");
                return;
            }

            if (plotId == -1) {
                FarmService.gI().plantSeedMass(player, seedItemId);
            } else {
                FarmService.gI().plantSeed(player, plotId, seedItemId);
            }
        } catch (Exception e) {
            Logger.logException(FarmController.class, e);
        }
    }

    /**
     * Xử lý thu hoạch
     */
    private void handleHarvest(Player player, Message msg) {
        try {
            int plotId = msg.reader().readInt();

            if (!isInHomeMap(player)) {
                Service.gI().sendThongBao(player, "Bạn phải ở nhà để thu hoạch!");
                return;
            }

            FarmService.gI().harvestPlot(player, plotId);
        } catch (Exception e) {
            Logger.logException(FarmController.class, e);
        }
    }

    /**
     * Xử lý bón phân
     */
    private void handleFertilize(Player player, Message msg) {
        try {
            int plotId = msg.reader().readInt();

            if (!isInHomeMap(player)) {
                Service.gI().sendThongBao(player, "Bạn phải ở nhà để bón phân!");
                return;
            }

            FarmService.gI().sendFertilizerMenu(player, plotId);
        } catch (Exception e) {
            Logger.logException(FarmController.class, e);
        }
    }

    /**
     * Xử lý mở khóa ô mới
     */
    private void handleUnlockPlot(Player player, Message msg) {
        try {
            byte useGem = msg.reader().readByte();

            if (!isInHomeMap(player)) {
                Service.gI().sendThongBao(player, "Bạn phải ở nhà để mở khóa ô ruộng!");
                return;
            }

            FarmService.gI().unlockPlot(player, useGem == 1);
        } catch (Exception e) {
            Logger.logException(FarmController.class, e);
        }
    }

    /**
     * Xử lý lấy dữ liệu khu vườn
     */
    private void handleGetGardenData(Player player) {
        try {
            if (!isInHomeMap(player)) {
                return;
            }

            // Khởi tạo garden nếu chưa có
            if (player.cloudGarden == null) {
                FarmService.gI().initCloudGarden(player);
            }

            // Gửi toàn bộ dữ liệu garden
            FarmService.gI().sendGardenUpdate(player);
        } catch (Exception e) {
            Logger.logException(FarmController.class, e);
        }
    }

    /**
     * Kiểm tra player có đang ở map nhà không (map 39, 40, 41)
     */
    public boolean isInHomeMap(Player player) {
        if (player.zone == null || player.zone.map == null) {
            return false;
        }
        int mapId = player.zone.map.mapId;
        return mapId == ConstFarm.MAP_CLOUD_GARDEN_TD ||
                mapId == ConstFarm.MAP_CLOUD_GARDEN_NM ||
                mapId == ConstFarm.MAP_CLOUD_GARDEN_XD;
    }
}
