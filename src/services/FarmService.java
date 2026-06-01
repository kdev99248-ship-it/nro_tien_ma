package services;

import consts.ConstFarm;
import item.Item;
import models.Farm.CloudGarden;
import models.Farm.CropType;
import models.Farm.FarmPlot;
import network.Message;
import player.Player;
import utils.Logger;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý logic cho hệ thống Khu vườn trên mây
 * 
 * @author TOMAHOC
 */
public class FarmService {

    private static FarmService instance;

    public static FarmService gI() {
        if (instance == null) {
            instance = new FarmService();
        }
        return instance;
    }

    /**
     * Khởi tạo khu vườn cho player (gọi khi player login)
     */
    public void initCloudGarden(Player player) {
        if (player.cloudGarden == null) {
            player.cloudGarden = new CloudGarden(player);
        }
    }

    /**
     * Load dữ liệu khu vườn từ database
     */
    public void loadCloudGarden(Player player, String data) {
        if (player.cloudGarden == null) {
            player.cloudGarden = new CloudGarden(player);
        }
        player.cloudGarden.loadFromString(data);
    }

    /**
     * Lấy dữ liệu khu vườn để lưu database
     */
    public String saveCloudGarden(Player player) {
        if (player.cloudGarden == null) {
            return "";
        }
        return player.cloudGarden.saveToString();
    }

    /**
     * Cập nhật khu vườn (gọi định kỳ hoặc khi vào map)
     */
    public void updateGarden(Player player) {
        if (player.cloudGarden != null) {
            boolean anyChanged = false;
            long currentTime = System.currentTimeMillis();

            // Duyệt từng plot để update và gửi notify riêng lẻ
            // Giúp client nhận được update realtime mà không cần load lại cả map
            for (FarmPlot plot : player.cloudGarden.getPlots()) {
                if (plot.isUnlocked()) {
                    // Nếu plot thay đổi giai đoạn
                    if (plot.update()) {
                        sendPlotUpdate(player, plot);
                        anyChanged = true;
                        System.out.println("[Server] Plot " + plot.getPlotId() + " auto-updated to stage "
                                + plot.getCurrentStage());
                    }
                }
            }

            if (anyChanged) {
                player.cloudGarden.setLastUpdateTime(currentTime);
            }
        }
    }

    /**
     * Mở menu tương tác với ô ruộng
     */
    public void openPlotMenu(Player player, int plotId) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        updateGarden(player);
        FarmPlot plot = player.cloudGarden.getPlot(plotId);
        if (plot == null) {
            Service.gI().sendThongBao(player, "Ô ruộng không tồn tại!");
            return;
        }

        System.out.println("[Server] Received openPlotMenu for plot " + plotId);

        player.iDMark.setIndexMenu(ConstFarm.MENU_FARM_PLOT_ACTION);
        player.iDMark.setIdItemUpTop(plotId); // Sử dụng idItemUpTop để lưu plotId

        if (!plot.isUnlocked()) {
            // Ô chưa mở khóa - hiển thị menu mở khóa
            sendOpenPlotUnlockMenu(player, plot);
        } else if (plot.isEmpty()) {
            // Ô trống - hiển thị menu cuốc + gieo hạt
            sendOpenPlotEmptyMenu(player, plot);
        } else if (plot.isWithered()) {
            // Cây bị héo - hiển thị menu thuốc trừ sâu/thu hoạch
            sendOpenPlotWitheredMenu(player, plot);
        } else if (plot.isReadyToHarvest()) {
            // Sẵn sàng thu hoạch
            sendOpenPlotHarvestMenu(player, plot);
        } else {
            // Đang trồng - hiển thị thông tin
            sendOpenPlotGrowingMenu(player, plot);
        }
    }

    /**
     * Gửi menu mở khóa ô
     */
    private void sendOpenPlotUnlockMenu(Player player, FarmPlot plot) {
        try {
            long goldCost = player.cloudGarden.getNextUnlockCostGold();
            int gemCost = player.cloudGarden.getNextUnlockCostGem();

            String info = "Ô ruộng bị khóa!\n\nMở khóa để có thể trồng cây.\n\n" +
                    "Chi phí:\n- " + Util.numberToMoney(goldCost) + " vàng\n- hoặc " + gemCost + " ngọc";

            Message msg = new Message(32);
            msg.writer().writeShort(-1); // NPC ID placeholder
            msg.writer().writeUTF(info);
            msg.writer().writeByte(3);
            msg.writer().writeUTF("Mở bằng vàng");
            msg.writer().writeUTF("Mở bằng ngọc");
            msg.writer().writeUTF("Đóng");
            player.sendMessage(msg);
            msg.cleanup();

            player.iDMark.setIndexMenu(ConstFarm.MENU_FARM_UNLOCK_PLOT);
        } catch (Exception e) {
            Logger.logException(FarmService.class, e);
        }
    }

    /**
     * Gửi action mở panel chọn hạt giống cho ô trống
     * Client sẽ tự lọc hạt giống từ inventory
     */
    private void sendOpenPlotEmptyMenu(Player player, FarmPlot plot) {
        try {
            Message msg = new Message(-34); // MSG_FARM_DATA
            msg.writer().writeByte(10); // SUBTYPE_PLOT_UPDATE
            msg.writer().writeByte(2); // DATA_OPEN_SEED_PANEL
            msg.writer().writeInt(plot.getPlotId());
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(FarmService.class, e);
        }
    }

    /**
     * Gửi menu thu hoạch
     */
    private void sendOpenPlotHarvestMenu(Player player, FarmPlot plot) {
        try {
            CropType crop = CropType.getById(plot.getCropType());
            String cropName = crop != null ? crop.name : "Cây";

            String info = cropName + " đã chín!\n\nThu hoạch ngay thôi!";

            Message msg = new Message(32);
            msg.writer().writeShort(-1);
            msg.writer().writeUTF(info);
            msg.writer().writeByte(2);
            msg.writer().writeUTF("Thu hoạch");
            msg.writer().writeUTF("Đóng");
            player.sendMessage(msg);
            msg.cleanup();

            player.iDMark.setIndexMenu(ConstFarm.MENU_FARM_CONFIRM_HARVEST);
        } catch (Exception e) {
            Logger.logException(FarmService.class, e);
        }
    }

    /**
     * Gửi menu đang trồng
     */
    private void sendOpenPlotGrowingMenu(Player player, FarmPlot plot) {
        try {
            String info = plot.getDisplayInfo();

            Message msg = new Message(32);
            msg.writer().writeShort(-1);
            msg.writer().writeUTF(info);
            msg.writer().writeByte(3);
            msg.writer().writeUTF("Bón phân");
            msg.writer().writeUTF("Phá bỏ");
            msg.writer().writeUTF("Đóng");
            player.sendMessage(msg);
            msg.cleanup();

            player.iDMark.setIndexMenu(ConstFarm.MENU_FARM_WATER);
        } catch (Exception e) {
            Logger.logException(FarmService.class, e);
        }
    }

    /**
     * Gửi menu khi cây bị héo (STAGE_WITHERED)
     * Hiển thị 3 lựa chọn: Dùng thuốc trừ sâu / Thu hoạch (giảm 20%) / Đóng
     */
    private void sendOpenPlotWitheredMenu(Player player, FarmPlot plot) {
        try {
            CropType crop = CropType.getById(plot.getCropType());
            String cropName = crop != null ? crop.name : "Cây";

            int pesticideCount = countItemInInventory(player, ConstFarm.PESTICIDE_ITEM_ID);

            String info = cropName + " bị héo!\n\n" +
                    "Sử dụng Bình thuốc trừ sâu\nđể chữa héo và bắt đầu lại.\n\n" +
                    "Hoặc thu hoạch ngay\n(giảm 20% sản lượng).";

            Message msg = new Message(32);
            msg.writer().writeShort(-1);
            msg.writer().writeUTF(info);
            msg.writer().writeByte(4);
            msg.writer().writeUTF("Thuốc trừ sâu (" + pesticideCount + ")");
            msg.writer().writeUTF("Thu hoạch");
            msg.writer().writeUTF("Phá bỏ");
            msg.writer().writeUTF("Đóng");
            player.sendMessage(msg);
            msg.cleanup();

            player.iDMark.setIndexMenu(ConstFarm.MENU_FARM_CONFIRM_PESTICIDE);
        } catch (Exception e) {
            Logger.logException(FarmService.class, e);
        }
    }

    /**
     * Gửi menu xác nhận phá bỏ cây trồng
     */
    private void sendConfirmDestroyMenu(Player player, FarmPlot plot) {
        try {
            CropType crop = CropType.getById(plot.getCropType());
            String cropName = crop != null ? crop.name : "Cây";

            String info = "Bạn có chắc chắn muốn phá bỏ \n" + cropName + " không?\n\n" +
                    "Hành động này sẽ xóa cây trồng\nvà không thể hoàn tác!";

            System.out.println("[Server] sendConfirmDestroyMenu for plot " + plot.getPlotId());

            Message msg = new Message(32);
            msg.writer().writeShort(-1);
            msg.writer().writeUTF(info);
            msg.writer().writeByte(2);
            msg.writer().writeUTF("OK");
            msg.writer().writeUTF("Đóng");
            player.sendMessage(msg);
            msg.cleanup();

            player.iDMark.setIndexMenu(ConstFarm.MENU_FARM_CONFIRM_DESTROY);
            System.out.println(
                    "[Server] Sent confirm destroy menu, indexMenu=" + ConstFarm.MENU_FARM_CONFIRM_DESTROY);
        } catch (Exception e) {
            Logger.logException(FarmService.class, e);
        }
    }

    /**
     * Phá bỏ cây trồng - reset ô đất về trống
     */
    public void destroyPlot(Player player, int plotId) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        FarmPlot plot = player.cloudGarden.getPlot(plotId);
        if (plot == null || !plot.isUnlocked()) {
            Service.gI().sendThongBao(player, "Ô ruộng không hợp lệ!");
            return;
        }

        if (plot.isEmpty()) {
            Service.gI().sendThongBao(player, "Ô ruộng đã trống!");
            return;
        }

        CropType crop = CropType.getById(plot.getCropType());
        String cropName = crop != null ? crop.name : "Cây";

        // Reset ô đất
        plot.reset();

        System.out.println("[Server] destroyPlot plotId=" + plotId +
                " stage=" + plot.getCurrentStage() + " cropType=" + plot.getCropType());

        // Gửi cập nhật UI trước (quan trọng)
        sendPlotUpdate(player, plot);

        // Đóng menu và ChatPopup sau
        closeNpcDialog(player);

        Service.gI().sendThongBao(player, "Đã phá bỏ " + cropName + "!");
    }

    /**
     * Mở menu chọn hạt giống từ hành trang
     */
    public void openSeedSelectionMenu(Player player, int plotId) {
        try {
            // Tìm các hạt giống trong hành trang
            List<Item> seeds = findSeedsInInventory(player);

            if (seeds.isEmpty()) {
                Service.gI().sendThongBao(player, "Bạn không có hạt giống nào trong hành trang!");
                return;
            }

            player.iDMark.setIdItemUpTop(plotId);
            player.iDMark.setIndexMenu(ConstFarm.MENU_FARM_SELECT_SEED);

            // Gửi danh sách hạt giống
            StringBuilder sb = new StringBuilder("Chọn hạt giống để trồng:\n\n");
            String[] menuItems = new String[seeds.size() + 1];

            for (int i = 0; i < seeds.size(); i++) {
                Item seed = seeds.get(i);
                CropType crop = CropType.getBySeedItemId(seed.template.id);
                String cropName = crop != null ? crop.name : seed.template.name;
                String timeInfo = crop != null ? " (" + crop.getTotalGrowthTimeMinutes() + " phút)" : "";
                menuItems[i] = cropName + " x" + seed.quantity + timeInfo;
            }
            menuItems[seeds.size()] = "Đóng";

            Message msg = new Message(32);
            msg.writer().writeShort(-1);
            msg.writer().writeUTF(sb.toString());
            msg.writer().writeByte(menuItems.length);
            for (String menu : menuItems) {
                msg.writer().writeUTF(menu);
            }
            player.sendMessage(msg);
            msg.cleanup();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tìm các hạt giống trong hành trang
     */
    public List<Item> findSeedsInInventory(Player player) {
        List<Item> seeds = new ArrayList<>();
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem() && CropType.isSeedItem(item.template.id)) {
                seeds.add(item);
            }
        }
        return seeds;
    }

    /**
     * Gieo hạt vào ô ruộng
     */
    public void plantSeed(Player player, int plotId, short seedItemId) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        FarmPlot plot = player.cloudGarden.getPlot(plotId);
        if (plot == null || !plot.isUnlocked()) {
            Service.gI().sendThongBao(player, "Không thể gieo hạt vào ô này!");
            return;
        }

        if (!plot.isEmpty()) {
            // Check if just planted (prevent spam error on double click)
            if (plot.getCurrentStage() == ConstFarm.STAGE_SEED &&
                    System.currentTimeMillis() - plot.getPlantedTime() < 2000) {
                return;
            }
            Service.gI().sendThongBao(player, "Ô này đã có cây rồi!");
            return;
        }

        CropType crop = CropType.getBySeedItemId(seedItemId);
        if (crop == null) {
            Service.gI().sendThongBao(player, "Hạt giống không hợp lệ!");
            return;
        }

        // Tìm và trừ hạt giống trong hành trang
        Item seedItem = null;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem() && item.template.id == seedItemId) {
                seedItem = item;
                break;
            }
        }

        if (seedItem == null || seedItem.quantity <= 0) {
            Service.gI().sendThongBao(player, "Bạn không có hạt giống này!");
            return;
        }

        // Gieo hạt
        if (plot.plant(crop.id)) {
            System.out.println("[Server] Plant seed success for plot " + plotId);
            // Trừ hạt giống
            seedItem.quantity--;
            if (seedItem.quantity <= 0) {
                InventoryService.gI().removeItemBag(player, seedItem);
            }
            InventoryService.gI().sendItemBag(player);

            Service.gI().sendThongBao(player, "Đã gieo " + crop.name + " thành công!\n" +
                    "Thời gian thu hoạch: " + crop.getTotalGrowthTimeMinutes() + " phút");

            // Gửi cập nhật UI
            sendPlotUpdate(player, plot);
        } else {
            Service.gI().sendThongBao(player, "Gieo hạt thất bại!");
        }
    }

    /**
     * Thu hoạch cây từ ô ruộng
     */
    public void harvestPlot(Player player, int plotId) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        FarmPlot plot = player.cloudGarden.getPlot(plotId);
        if (plot == null || (!plot.isReadyToHarvest() && !plot.isWithered())) {
            Service.gI().sendThongBao(player, "Ô này không thể thu hoạch!");
            return;
        }

        byte cropType = plot.getCropType();
        CropType crop = CropType.getById(cropType);
        if (crop == null) {
            Service.gI().sendThongBao(player, "Lỗi: Loại cây không xác định!");
            return;
        }

        boolean wasWithered = plot.isWithered();
        int quantity = plot.harvest();
        if (quantity > 0) {
            System.out.println("[Server] Harvest success for plot " + plotId + " quantity=" + quantity);
            // Tạo item sản phẩm thu hoạch
            Item harvestItem = ItemService.gI().createNewItem(crop.harvestItemId, quantity);
            if (harvestItem != null) {
                InventoryService.gI().addItemBag(player, harvestItem);
                if (harvestItem.quantity > 0) {
                    InventoryService.gI().addItemBox(player, harvestItem);
                }
                InventoryService.gI().sendItemBag(player);

                String harvestMsg = "Thu hoạch thành công!\n" +
                        "Nhận được: " + crop.name + " x" + quantity;
                if (wasWithered) {
                    harvestMsg += "\n(Sản lượng giảm 20% do cây bị héo)";
                }
                Service.gI().sendThongBao(player, harvestMsg);
            }

            // Đóng menu và ChatPopup
            closeNpcDialog(player);

            // Gửi cập nhật UI
            sendPlotUpdate(player, plot);
            sendHarvestEffect(player, plotId, cropType, quantity);
        } else {
            Service.gI().sendThongBao(player, "Thu hoạch thất bại!");
        }
    }

    /**
     * Mở menu chọn hạt giống để gieo hàng loạt
     * Sử dụng panel chọn hạt giống của client giống như khi click vào ô đất trống
     */
    public void openSeedSelectionMenuMass(Player player) {
        try {
            Message msg = new Message(-34); // MSG_FARM_DATA
            msg.writer().writeByte(10); // SUBTYPE_PLOT_UPDATE
            msg.writer().writeByte(2); // DATA_OPEN_SEED_PANEL
            msg.writer().writeInt(-1); // -1 để đánh dấu là gieo hàng loạt
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(FarmService.class, e);
        }
    }

    /**
     * Gieo hạt hàng loạt vào tất cả các ô trống
     */
    public void plantSeedMass(Player player, short seedItemId) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        CropType crop = CropType.getBySeedItemId(seedItemId);
        if (crop == null) {
            Service.gI().sendThongBao(player, "Hạt giống không hợp lệ!");
            return;
        }

        // Tìm hạt giống trong hành trang
        Item seedItem = null;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem() && item.template.id == seedItemId) {
                seedItem = item;
                break;
            }
        }

        if (seedItem == null || seedItem.quantity <= 0) {
            Service.gI().sendThongBao(player, "Bạn không có hạt giống này!");
            return;
        }

        int plantedCount = 0;
        for (FarmPlot plot : player.cloudGarden.getPlots()) {
            if (plot.isUnlocked() && plot.isEmpty()) {
                if (seedItem.quantity > 0) {
                    if (plot.plant(crop.id)) {
                        seedItem.quantity--;
                        plantedCount++;
                        sendPlotUpdate(player, plot);
                    }
                } else {
                    break;
                }
            }
        }

        if (plantedCount > 0) {
            if (seedItem.quantity <= 0) {
                InventoryService.gI().removeItemBag(player, seedItem);
            }
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Đã gieo " + crop.name + " thành công vào " + plantedCount + " ô trống!");
        } else {
            Service.gI().sendThongBao(player, "Không có ô trống nào để gieo hạt!");
        }
    }

    /**
     * Thu hoạch nhanh tất cả các ô đã chín
     */
    public void harvestAllPlots(Player player) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        updateGarden(player);
        List<FarmPlot> readyPlots = player.cloudGarden.getReadyToHarvestPlots();

        if (readyPlots.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có cây nào chín để thu hoạch!");
            return;
        }

        int totalHarvested = 0;
        for (FarmPlot plot : readyPlots) {
            int plotId = plot.getPlotId();
            byte cropType = plot.getCropType();
            CropType crop = CropType.getById(cropType);

            if (crop == null)
                continue;

            int quantity = plot.harvest();
            if (quantity > 0) {
                Item harvestItem = ItemService.gI().createNewItem(crop.harvestItemId, quantity);
                if (harvestItem != null) {
                    InventoryService.gI().addItemBag(player, harvestItem);
                    if (harvestItem.quantity > 0) {
                        InventoryService.gI().addItemBox(player, harvestItem);
                    }
                }
                totalHarvested++;

                // Gửi cập nhật UI cho từng ô
                sendPlotUpdate(player, plot);
                sendHarvestEffect(player, plotId, cropType, quantity);
            }
        }

        if (totalHarvested > 0) {
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Đã thu hoạch nhanh " + totalHarvested + " ô ruộng!");
        } else {
            Service.gI().sendThongBao(player, "Thu hoạch thất bại hoặc hành trang đầy!");
        }
    }

    /**
     * Mở khóa ô ruộng
     */
    public void unlockPlot(Player player, boolean useGem) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        if (!player.cloudGarden.canUnlockMore()) {
            Service.gI().sendThongBao(player, "Bạn đã mở khóa tất cả ô ruộng!");
            return;
        }

        if (player.cloudGarden.unlockNextPlot(useGem)) {
            PlayerService.gI().sendInfoHpMpMoney(player);
            Service.gI().sendThongBao(player, "Mở khóa ô ruộng thành công!");

            // Đóng menu và ChatPopup
            closeNpcDialog(player);

            // Gửi cập nhật UI
            sendGardenUpdate(player);
        } else {
            if (useGem) {
                Service.gI().sendThongBao(player, "Bạn không đủ ngọc để mở khóa!");
            } else {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng để mở khóa!");
            }
        }
    }

    /**
     * Mở menu chọn phân bón cho ô ruộng
     */
    public void sendFertilizerMenu(Player player, int plotId) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        FarmPlot plot = player.cloudGarden.getPlot(plotId);
        if (plot == null || !plot.isUnlocked()) {
            Service.gI().sendThongBao(player, "Ô ruộng không hợp lệ!");
            return;
        }

        if (plot.isEmpty()) {
            Service.gI().sendThongBao(player, "Ô ruộng chưa có cây trồng!");
            return;
        }

        if (plot.isReadyToHarvest()) {
            Service.gI().sendThongBao(player, "Cây đã chín, không cần bón phân!");
            return;
        }

        try {
            // Đếm số lượng từng loại phân bón trong hành trang
            int[] fertilizerCounts = new int[ConstFarm.ALL_FERTILIZERS.length];
            for (int i = 0; i < ConstFarm.ALL_FERTILIZERS.length; i++) {
                fertilizerCounts[i] = countItemInInventory(player, ConstFarm.ALL_FERTILIZERS[i]);
            }

            String info = "Chọn loại phân bón:\n\n" +
                    "Phân bón sẽ giúp cây trồng\nphát triển nhanh hơn.";

            Message msg = new Message(32);
            msg.writer().writeShort(-1);
            msg.writer().writeUTF(info);
            msg.writer().writeByte(5); // 4 loại phân + Đóng
            msg.writer().writeUTF("Phân bón 5p (" + fertilizerCounts[0] + ")");
            msg.writer().writeUTF("Phân bón 10p (" + fertilizerCounts[1] + ")");
            msg.writer().writeUTF("Phân bón 20p (" + fertilizerCounts[2] + ")");
            msg.writer().writeUTF("Phân bón 30p (" + fertilizerCounts[3] + ")");
            msg.writer().writeUTF("Đóng");
            player.sendMessage(msg);
            msg.cleanup();

            player.iDMark.setIdItemUpTop(plotId);
            player.iDMark.setIndexMenu(ConstFarm.MENU_FARM_SELECT_FERTILIZER);
        } catch (Exception e) {
            Logger.logException(FarmService.class, e);
        }
    }

    /**
     * Đếm số lượng item trong hành trang
     */
    private int countItemInInventory(Player player, short itemId) {
        int count = 0;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem() && item.template.id == itemId) {
                count += item.quantity;
            }
        }
        return count;
    }

    /**
     * Bón phân cho ô ruộng - tăng tốc độ phát triển
     * Cách hoạt động: dịch lastStageChangeTime về quá khứ, rồi gọi update
     */
    public void fertilizePlot(Player player, int plotId, short fertilizerItemId) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        FarmPlot plot = player.cloudGarden.getPlot(plotId);
        if (plot == null || !plot.isUnlocked()) {
            Service.gI().sendThongBao(player, "Ô ruộng không hợp lệ!");
            return;
        }

        if (plot.isEmpty()) {
            Service.gI().sendThongBao(player, "Ô ruộng chưa có cây trồng!");
            return;
        }

        if (plot.isReadyToHarvest()) {
            Service.gI().sendThongBao(player, "Cây đã chín, không cần bón phân!");
            return;
        }

        if (!ConstFarm.isFertilizerItem(fertilizerItemId)) {
            Service.gI().sendThongBao(player, "Vật phẩm không hợp lệ!");
            return;
        }

        // Tìm phân bón trong hành trang
        Item fertilizerItem = null;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem() && item.template.id == fertilizerItemId) {
                fertilizerItem = item;
                break;
            }
        }

        if (fertilizerItem == null || fertilizerItem.quantity <= 0) {
            Service.gI().sendThongBao(player, "Bạn không có loại phân bón này!");
            return;
        }

        long boostTimeMs = ConstFarm.getFertilizerBoostTimeMs(fertilizerItemId);
        if (boostTimeMs <= 0) {
            Service.gI().sendThongBao(player, "Phân bón không hợp lệ!");
            return;
        }

        // Dịch lastStageChangeTime về quá khứ để tăng tốc phát triển
        long currentLastChange = plot.getLastStageChangeTime();
        plot.setLastStageChangeTime(currentLastChange - boostTimeMs);

        // Cũng dịch plantedTime về quá khứ để đồng bộ
        long currentPlantedTime = plot.getPlantedTime();
        plot.setPlantedTime(currentPlantedTime - boostTimeMs);

        // Cập nhật lại giai đoạn phát triển
        plot.update();

        // Nếu phân bón đã đẩy cây lên STAGE_MATURE, reset lastStageChangeTime về hiện
        // tại
        // để thời gian thừa từ phân bón không bị tính vào bộ đếm héo
        if (plot.getCurrentStage() == ConstFarm.STAGE_MATURE) {
            plot.setLastStageChangeTime(System.currentTimeMillis());
            plot.setMatureReadyTime(0); // Reset để tính lại từ đầu
        }

        // Trừ phân bón
        fertilizerItem.quantity--;
        if (fertilizerItem.quantity <= 0) {
            InventoryService.gI().removeItemBag(player, fertilizerItem);
        }
        InventoryService.gI().sendItemBag(player);

        String fertName = ConstFarm.getFertilizerName(fertilizerItemId);

        // Đóng menu
        closeNpcDialog(player);

        // Gửi cập nhật UI
        sendPlotUpdate(player, plot);

        if (plot.isReadyToHarvest()) {
            Service.gI().sendThongBao(player, "Đã bón " + fertName + "!\nCây đã chín, thu hoạch ngay!");
        } else {
            int remaining = plot.getTimeToHarvest();
            int minutes = remaining / 60;
            int seconds = remaining % 60;
            Service.gI().sendThongBao(player,
                    "Đã bón " + fertName + "!\nCòn " + minutes + " phút " + seconds + " giây để thu hoạch.");
        }
    }

    /**
     * Sử dụng thuốc trừ sâu để chữa cây héo
     * Chuyển từ STAGE_WITHERED về STAGE_MATURE và reset bộ đếm héo
     */
    public void usePesticide(Player player, int plotId) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        FarmPlot plot = player.cloudGarden.getPlot(plotId);
        if (plot == null || !plot.isUnlocked()) {
            Service.gI().sendThongBao(player, "Ô ruộng không hợp lệ!");
            return;
        }

        if (!plot.isWithered()) {
            Service.gI().sendThongBao(player, "Cây không bị héo!");
            return;
        }

        // Tìm thuốc trừ sâu trong hành trang
        Item pesticideItem = null;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem() && item.template.id == ConstFarm.PESTICIDE_ITEM_ID) {
                pesticideItem = item;
                break;
            }
        }

        if (pesticideItem == null || pesticideItem.quantity <= 0) {
            Service.gI().sendThongBao(player, "Bạn không có Bình thuốc trừ sâu!");
            return;
        }

        // Sử dụng thuốc trừ sâu
        if (plot.curePesticide()) {
            // Trừ thuốc trừ sâu
            pesticideItem.quantity--;
            if (pesticideItem.quantity <= 0) {
                InventoryService.gI().removeItemBag(player, pesticideItem);
            }
            InventoryService.gI().sendItemBag(player);

            CropType crop = CropType.getById(plot.getCropType());
            String cropName = crop != null ? crop.name : "Cây";

            // Đóng menu
            closeNpcDialog(player);

            // Gửi cập nhật UI
            sendPlotUpdate(player, plot);

            Service.gI().sendThongBao(player, "Đã dùng thuốc trừ sâu!\n" +
                    cropName + " đã hồi phục.\nHãy thu hoạch sớm trước khi bị héo lại!");
        } else {
            Service.gI().sendThongBao(player, "Không thể sử dụng thuốc trừ sâu!");
        }
    }

    /**
     * Phun thuốc trừ sâu nhanh cho tất cả cây bị héo trong khu vườn
     * Tự động sử dụng thuốc trừ sâu cho từng ô có cây WITHERED
     */
    public void usePesticideAll(Player player) {
        if (player.cloudGarden == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có khu vườn!");
            return;
        }

        // Cập nhật khu vườn trước để đảm bảo trạng thái mới nhất
        updateGarden(player);

        // Lấy danh sách các ô có cây bị héo
        List<FarmPlot> witheredPlots = player.cloudGarden.getWitheredPlots();

        if (witheredPlots.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có cây nào bị bệnh (héo) để phun thuốc!");
            return;
        }

        // Tìm thuốc trừ sâu trong hành trang
        Item pesticideItem = null;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem() && item.template.id == ConstFarm.PESTICIDE_ITEM_ID) {
                pesticideItem = item;
                break;
            }
        }

        if (pesticideItem == null || pesticideItem.quantity <= 0) {
            Service.gI().sendThongBao(player, "Bạn không có Bình thuốc trừ sâu!\n" +
                    "Có " + witheredPlots.size() + " cây đang bị héo cần phun thuốc.");
            return;
        }

        int curedCount = 0;
        int totalWithered = witheredPlots.size();

        for (FarmPlot plot : witheredPlots) {
            if (pesticideItem.quantity <= 0) {
                break; // Hết thuốc
            }

            if (plot.curePesticide()) {
                pesticideItem.quantity--;
                curedCount++;

                // Gửi cập nhật UI cho từng ô
                sendPlotUpdate(player, plot);
            }
        }

        // Xử lý item sau khi phun xong
        if (pesticideItem.quantity <= 0) {
            InventoryService.gI().removeItemBag(player, pesticideItem);
        }
        InventoryService.gI().sendItemBag(player);

        // Thông báo kết quả
        if (curedCount > 0) {
            String msg = "Đã phun thuốc trừ sâu cho " + curedCount + " cây!\n";
            if (curedCount < totalWithered) {
                msg += "Còn " + (totalWithered - curedCount) + " cây chưa được phun (hết thuốc).\n";
            }
            msg += "Hãy thu hoạch sớm trước khi bị héo lại!";
            Service.gI().sendThongBao(player, msg);
        } else {
            Service.gI().sendThongBao(player, "Phun thuốc thất bại!");
        }
    }

    /**
     * Gửi cập nhật UI cho một ô ruộng
     */
    public void sendPlotUpdate(Player player, FarmPlot plot) {
        try {
            Message msg = new Message(-34); // Sử dụng message type tương tự MagicTree
            msg.writer().writeByte(10); // Sub-type cho Farm
            msg.writer().writeByte(0); // Update single plot
            msg.writer().writeInt(plot.getPlotId());
            System.out.println("[Server] Sending plot update for plot " + plot.getPlotId() + " stage="
                    + plot.getCurrentStage());
            msg.writer().writeByte(plot.getCurrentStage());
            msg.writer().writeByte(plot.getCropType());
            msg.writer().writeInt(plot.getTimeToHarvest());
            msg.writer().writeBoolean(plot.isWatered());
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi cập nhật toàn bộ khu vườn
     */
    public void sendGardenUpdate(Player player) {
        if (player.cloudGarden == null) {
            return;
        }

        // Auto upgrade for old data (Update memory state)
        if (player.cloudGarden.getUnlockedPlots() < ConstFarm.INITIAL_PLOTS) {
            int newInitial = ConstFarm.INITIAL_PLOTS;
            player.cloudGarden.setUnlockedPlots(newInitial);
            for (int i = 0; i < newInitial; i++) {
                FarmPlot plot = player.cloudGarden.getPlot(i);
                if (plot != null) {
                    plot.setUnlocked(true);
                }
            }
        }

        try {
            Message msg = new Message(-34);
            msg.writer().writeByte(10); // Sub-type cho Farm
            msg.writer().writeByte(1); // Update full garden
            msg.writer().writeInt(player.cloudGarden.getUnlockedPlots());

            List<FarmPlot> plots = player.cloudGarden.getPlots();
            for (int i = 0; i < ConstFarm.MAX_PLOTS; i++) {
                FarmPlot plot = (i < plots.size()) ? plots.get(i) : new FarmPlot(i, false);
                msg.writer().writeInt(plot.getPlotId());
                msg.writer().writeBoolean(plot.isUnlocked());
                msg.writer().writeByte(plot.getCurrentStage());
                msg.writer().writeByte(plot.getCropType());
                msg.writer().writeInt(plot.getTimeToHarvest());
                msg.writer().writeBoolean(plot.isWatered());
                msg.writer().writeShort(plot.getPosX());
                msg.writer().writeShort(plot.getPosY());
            }

            player.sendMessage(msg);
            msg.cleanup();
            System.out.println("[Farm] Sent garden update to " + player.name + " (" + plots.size() + " plots, " + player.cloudGarden.getUnlockedPlots() + " unlocked)");
        } catch (Exception e) {
            System.err.println("[Farm] Error sending garden update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý lựa chọn menu
     */
    public void handleMenuSelection(Player player, int menuIndex, int selection) {
        int plotId = player.iDMark.getIdItemUpTop();
        System.out.println("[Server] logic menu selection: menu=" + menuIndex + " plot=" + plotId
                + " selection=" + selection);

        switch (menuIndex) {
            case ConstFarm.MENU_FARM_PLOT_ACTION:
                if (selection == 0) {
                    // Gieo hạt
                    openSeedSelectionMenu(player, plotId);
                }
                break;

            case ConstFarm.MENU_FARM_SELECT_SEED:
                List<Item> seeds = findSeedsInInventory(player);
                if (selection >= 0 && selection < seeds.size()) {
                    Item selectedSeed = seeds.get(selection);
                    plantSeed(player, plotId, selectedSeed.template.id);
                }
                break;

            case ConstFarm.MENU_FARM_SELECT_SEED_MASS:
                // Sẽ không đi vào đây nữa vì đã chuyển sang dùng panel client
                break;

            case ConstFarm.MENU_FARM_CONFIRM_HARVEST:
                if (selection == 0) {
                    harvestPlot(player, plotId);
                }
                break;

            case ConstFarm.MENU_FARM_UNLOCK_PLOT:
                if (selection == 0) {
                    unlockPlot(player, false); // Mở bằng vàng
                } else if (selection == 1) {
                    unlockPlot(player, true); // Mở bằng ngọc
                }
                break;

            case ConstFarm.MENU_FARM_WATER:
                if (selection == 0) {
                    // Bón phân - mở menu chọn loại phân bón
                    sendFertilizerMenu(player, plotId);
                } else if (selection == 1) {
                    // Mở menu xác nhận phá bỏ
                    FarmPlot plot = player.cloudGarden.getPlot(plotId);
                    if (plot != null) {
                        sendConfirmDestroyMenu(player, plot);
                    }
                }
                break;

            case ConstFarm.MENU_FARM_SELECT_FERTILIZER:
                // Chọn loại phân bón: 0=5p, 1=10p, 2=20p, 3=30p, 4=Đóng
                if (selection >= 0 && selection < ConstFarm.ALL_FERTILIZERS.length) {
                    short selectedFertilizer = ConstFarm.ALL_FERTILIZERS[selection];
                    fertilizePlot(player, plotId, selectedFertilizer);
                }
                break;

            case ConstFarm.MENU_FARM_CONFIRM_DESTROY:
                if (selection == 0) {
                    destroyPlot(player, plotId);
                }
                break;

            case ConstFarm.MENU_FARM_CONFIRM_PESTICIDE:
                if (selection == 0) {
                    // Dùng thuốc trừ sâu
                    usePesticide(player, plotId);
                } else if (selection == 1) {
                    // Thu hoạch cây héo
                    harvestPlot(player, plotId);
                } else if (selection == 2) {
                    // Phá bỏ
                    FarmPlot pPlot = player.cloudGarden.getPlot(plotId);
                    if (pPlot != null) {
                        sendConfirmDestroyMenu(player, pPlot);
                    }
                }
                break;
        }
    }

    /**
     * Kiểm tra player có đang ở map khu vườn không
     */
    public boolean isInCloudGardenMap(Player player) {
        if (player.zone == null)
            return false;
        int mapId = player.zone.map.mapId;
        return mapId >= ConstFarm.MAP_CLOUD_GARDEN_TD && mapId <= ConstFarm.MAP_CLOUD_GARDEN_XD;
    }

    /**
     * Dispose khu vườn khi player logout
     */
    public void disposeGarden(Player player) {
        if (player.cloudGarden != null) {
            player.cloudGarden.dispose();
            player.cloudGarden = null;
        }
    }

    /**
     * Đóng menu và ChatPopup NPC trên client
     * Gửi message để client xóa menu và ChatPopup hiện tại
     */
    private void closeNpcDialog(Player player) {
        try {
            // Reset indexMenu
            player.iDMark.setIndexMenu(-1);

            // Gửi hideWaitDialog để ẩn dialog chờ nếu có
            Service.gI().hideWaitDialog(player);

            // Gửi message để client đóng ChatPopup và menu NPC
            Message msg = new Message(-34); // MSG_FARM_DATA
            msg.writer().writeByte(10); // SUBTYPE_PLOT_UPDATE
            msg.writer().writeByte(3); // DATA_CLOSE_DIALOG
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(FarmService.class, e);
        }
    }

    /**
     * Gửi hiệu ứng thu hoạch
     */
    public void sendHarvestEffect(Player player, int plotId, int cropType, int quantity) {
        try {
            Message msg = new Message(-34);
            msg.writer().writeByte(10); // SUBTYPE_PLOT_UPDATE
            msg.writer().writeByte(ConstFarm.DATA_HARVEST_SUCCESS); // 4
            msg.writer().writeInt(plotId);
            msg.writer().writeByte(cropType);
            msg.writer().writeInt(quantity);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
