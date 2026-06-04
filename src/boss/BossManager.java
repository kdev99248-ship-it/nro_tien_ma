package boss;

/*
 *
 *
 *
 */

import jdbc.daos.BossDAO;

import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

import EMTI.Functions;

import player.Player;
import network.Message;
import services.MapService;

import java.util.ArrayList;
import java.util.List;

import boss.boss_manifest.The23rdMartialArtCongress.DHVT23Boss;
import map.Zone;
import server.Maintenance;
import utils.Logger;
import utils.Util;

public class BossManager implements Runnable {

    private static BossManager instance;
    public static byte ratioReward = 10;

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public BossManager() {
        this.bosses = new ArrayList<>();
    }

    protected final List<Boss> bosses;
    private final Map<Integer, List<BossConfig>> bossConfigMap = new HashMap<>();

    public List<Boss> getBosses() {
        return this.bosses;
    }

    public void addBoss(Boss boss) {
        this.bosses.add(boss);
    }

    public void removeBoss(Boss boss) {
        this.bosses.remove(boss);
    }

    public void loadBoss() {
        // Tất cả boss được load từ database (bao gồm DeTuBoss, Bossgido, ALong,
        // TaiLocQuaLon, Kongdanang)
        loadBossFromDB();
    }

    /**
     * Load boss từ database.
     */
    private void loadBossFromDB() {
        try {
            List<BossConfig> allConfigs = BossDAO.loadAllBossConfigs();
            if (allConfigs.isEmpty()) {
                Logger.warning("Boss configuration table is empty in Database!\n");
                return;
            }

            bossConfigMap.clear();
            // Nhóm theo bossId
            for (BossConfig config : allConfigs) {
                bossConfigMap.computeIfAbsent(config.getBossId(), k -> new ArrayList<>()).add(config);
            }

            // Sắp xếp mỗi group theo levelIndex và tạo boss instance ban đầu
            int bossCount = 0;
            for (Map.Entry<Integer, List<BossConfig>> entry : bossConfigMap.entrySet()) {
                int bossId = entry.getKey();
                List<BossConfig> levels = entry.getValue();
                levels.sort(Comparator.comparingInt(BossConfig::getLevelIndex));

                BossConfig primary = levels.get(0);
                int spawnCount = primary.getSpawnCount();

                // Build BossData[] from all levels
                BossData[] dataArray = new BossData[levels.size()];
                for (int i = 0; i < levels.size(); i++) {
                    dataArray[i] = levels.get(i).toBossData();
                }

                // Diagnostic log cho mỗi boss
                int[][] skills = primary.parseSkills();
                String rewardCfg = primary.getRewardConfig();
                BossType bt = primary.parseBossType();

                Logger.log("\u001b[0;36m", String.format(
                        " Boss[%d] %-25s | skills=%d | reward=%s | type=%s | levels=%d | spawn=%d\n",
                        bossId,
                        primary.getName(),
                        skills != null ? skills.length : 0,
                        (rewardCfg != null && !rewardCfg.isEmpty() && !rewardCfg.equals("{}")) ? "CÓ"
                                : "THIẾU",
                        bt != null ? bt.name() : "DEFAULT",
                        levels.size(),
                        spawnCount
                ));
                for (int i = 0; i < spawnCount; i++) {
                    try {
                        Boss boss = createBossFromConfig(primary, dataArray);
                        if (boss != null) {
                            bossCount++;
                        }
                    } catch (Exception e) {
                        Logger.error("Lỗi tạo boss từ DB, bossId=" + bossId + ": " + e.getMessage() + "\n");
                    }
                }
            }

            Logger.success(
                    "Loaded " + allConfigs.size() + " configs and spawned " + bossCount + " bosses from Database.\n");
            // Kết nối các nhóm boss (Group Linking)
            linkGroupBosses();

        } catch (Exception e) {
            Logger.error("Lỗi loadBossFromDB: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void linkGroupBosses() {
        // Gom nhóm các boss theo cùng một bộ ID xuất hiện cùng nhau
        // Các đệ tử đã được khởi tạo trong constructor của Leader và gán vào
        // bossAppearTogether
        for (Boss boss : this.bosses) {
            if (!(boss instanceof GenericBoss))
                continue;
            BossConfig cfg = ((GenericBoss) boss).config;
            int[] togetherIds = cfg.parseBossesAppearTogether();

            // Chỉ xử lý những boss đóng vai trò Leader (có config bossesAppearTogether)
            if (togetherIds != null && togetherIds.length > 0) {
                int lvl = Math.max(0, boss.currentLevel);
                if (boss.bossAppearTogether == null || boss.bossAppearTogether.length <= lvl
                        || boss.bossAppearTogether[lvl] == null) {
                    continue;
                }

                List<Boss> sharedGroup = new ArrayList<>();
                sharedGroup.add(boss);
                for (Boss follower : boss.bossAppearTogether[lvl]) {
                    if (follower != null) {
                        sharedGroup.add(follower);
                    }
                }

                // Tất cả chuỗi đánh (Ginyu, Doraemon, Android) đều hoàn hảo khớp với thứ tự ID
                // giảm dần (từ cao xuống thấp)
                sharedGroup.sort(Comparator.comparingInt(b -> -(int) b.id));

                // Gán danh sách thống nhất này cho TOÀN BỘ các thành viên trong group để
                // auto-AFK hoạt động
                Boss[] groupArr = sharedGroup.toArray(new Boss[0]);
                for (Boss member : sharedGroup) {
                    member.bossAppearTogether = new Boss[member.data.length][];
                    for (int i = 0; i < member.data.length; i++) {
                        member.bossAppearTogether[i] = groupArr;
                    }

                    if (member != boss) {
                        member.parentBoss = boss;
                    }
                }
            }
        }
    }

    /**
     * Create a boss from its ID by looking up database config.
     */
    public Boss createBoss(int bossId) {
        List<BossConfig> levels = bossConfigMap.get(bossId);
        if (levels == null || levels.isEmpty()) {
            return null;
        }

        // Sắp xếp lại để chắc chắn level 0 là đầu tiên
        levels.sort(Comparator.comparingInt(BossConfig::getLevelIndex));
        BossConfig primary = levels.get(0);

        BossData[] dataArray = new BossData[levels.size()];
        for (int i = 0; i < levels.size(); i++) {
            dataArray[i] = levels.get(i).toBossData();
        }

        return createBossFromConfig(primary, dataArray);
    }

    public void createBoss(int bossId, int total) {
        for (int i = 0; i < total; i++) {
            createBoss(bossId);
        }
    }

    /**
     * Create a boss instance from configuration and data array.
     */
    private Boss createBossFromConfig(BossConfig config, BossData[] data) {
        try {
            String customClass = config.getCustomClass();
            if (customClass != null && !customClass.isEmpty() && !"null".equals(customClass)) {
                // Reflection instantiation
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(customClass);
                } catch (ClassNotFoundException e) {
                    // Sửa lỗi mapping class cho ĐHVT 23 nếu database bị sai (do refactoring trước
                    // đó)
                    if (customClass.contains("The23rdMartialArtCongress")) {
                        clazz = resolveDHVT23Class(config);
                    }

                    if (clazz == null) {
                        throw e; // Rethrow if still not found
                    }
                }
                return (Boss) clazz.getConstructor(BossConfig.class, BossData[].class)
                        .newInstance(config, data);
            } else {
                // Default instantiation - kiểm tra BossType để thêm vào manager chuyên biệt
                BossType bt = config.parseBossType();
                if (bt != null) {
                    return new GenericBoss(bt, config, data);
                } else {
                    return new GenericBoss(config, data);
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to create boss instance: " + config.getName() + " - " + e.getMessage() + "\n");
            return null;
        }
    }

    /**
     * Tìm class chính xác cho boss ĐHVT 23 nếu customClass trong DB bị sai.
     */
    private Class<?> resolveDHVT23Class(BossConfig config) {
        String basePkg = "boss.boss_manifest.The23rdMartialArtCongress.";
        String name = Util.removeAccent(config.getName()).replaceAll("\\s+", "");
        // Map một số tên đặc biệt sang CamelCase
        if (name.equalsIgnoreCase("Chapa"))
            name = "ChaPa";
        if (name.equalsIgnoreCase("Ponput"))
            name = "PonPut";
        if (name.equalsIgnoreCase("Chanxu"))
            name = "ChanXu";
        if (name.equalsIgnoreCase("Taupaypay") || name.equalsIgnoreCase("Taupypy"))
            name = "TauPayPay";
        if (name.equalsIgnoreCase("Jackychun"))
            name = "JackyChun";
        if (name.equalsIgnoreCase("Thienxinhang"))
            name = "ThienXinHang";
        if (name.equalsIgnoreCase("Liuliu"))
            name = "LiuLiu";
        if (name.equalsIgnoreCase("Soihecquyn"))
            name = "SoiHecQuyn";
        if (name.equalsIgnoreCase("Xinbato"))
            name = "Xinbato";
        if (name.equalsIgnoreCase("Pocolo"))
            name = "Pocolo";
        if (name.equalsIgnoreCase("Odo"))
            name = "ODo";

        try {
            return Class.forName(basePkg + name);
        } catch (ClassNotFoundException e) {
            // Fallback cuối cùng là DHVT23Boss (vừa được gỡ abstract)
            return DHVT23Boss.class;
        }
    }

    /**
     * Lấy tất cả BossConfig đã load từ DB (dùng cho UI).
     */
    public List<BossConfig> getAllBossConfigs() {
        return bossConfigMap.values().stream()
                .flatMap(List::stream)
                .filter(c -> c.getLevelIndex() == 0) // Chỉ lấy level 0 để đại diện cho boss
                .collect(Collectors.toList());
    }

    public Boss getBoss(int id) {
        try {
            Boss boss = this.bosses.get(id);
            if (boss != null) {
                return boss;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public Boss getBossById(int bossId) {
        return this.bosses.stream().filter(boss -> (int) boss.id == bossId && !boss.isDie()).findFirst().orElse(null);
    }

    public Boss getBossById(int bossId, int mapId, int zoneId) {
        return this.bosses.stream().filter(boss -> (int) boss.id == bossId && boss.zone != null
                        && boss.zone.map.mapId == mapId && boss.zone.zoneId == zoneId && !boss.isDie()).findFirst()
                .orElse(null);
    }

    public boolean checkBosses(Zone zone, int targetBossId) {
        return this.bosses.stream()
                .filter(boss -> (int) boss.id == targetBossId && boss.zone != null && boss.zone.equals(zone)
                        && !boss.isDie())
                .findFirst().orElse(null) != null;
    }

    public Player findBossClone(Player player) {
        return player.zone.getBosses().stream().filter(boss -> boss.id < -100_000_000 && !boss.isDie()).findFirst()
                .orElse(null);
    }

    public void showListBoss(Player player) {
        player.iDMark.setMenuType(3);
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Boss");

            List<Boss> filteredBosses = bosses.stream()
                    .filter(boss -> boss.data != null && boss.data.length > 0 &&
                            !MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0]))
                    .collect(Collectors.toList());

            msg.writer().writeByte(filteredBosses.size());
            for (int i = 0; i < filteredBosses.size(); i++) {
                Boss boss = filteredBosses.get(i);
                msg.writer().writeInt(i);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null) {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF(
                            boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId + "");
                } else {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF("Chết rồi");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update tất cả boss và trả về true nếu có boss nào đang hoạt động.
     */
    protected boolean updateBosses() {
        boolean hasActiveBoss = false;
        for (int i = this.bosses.size() - 1; i >= 0; i--) {
            try {
                Boss boss = this.bosses.get(i);
                boss.update();
                if (boss.bossStatus != BossStatus.REST) {
                    hasActiveBoss = true;
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
        return hasActiveBoss;
    }

    /**
     * Update tất cả boss và XÓA boss khỏi danh sách nếu gặp lỗi hoặc boss đã chết
     * (dùng cho phó bản).
     */
    public boolean updateBossesWithRemove() {
        boolean hasActiveBoss = false;
        for (int i = this.bosses.size() - 1; i >= 0; i--) {
            try {
                Boss boss = this.bosses.get(i);
                boss.update();
                if (boss.bossStatus != BossStatus.REST) {
                    hasActiveBoss = true;
                }
                // Nếu boss phó bản đã rời map thì remove khỏi manager
                if (boss.bossStatus == BossStatus.LEAVE_MAP) {
                    this.bosses.remove(i);
                }
            } catch (Exception e) {
                this.bosses.remove(i);
                // e.printStackTrace();
            }
        }
        return hasActiveBoss;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long st = System.currentTimeMillis();
                boolean hasActiveBoss = updateBosses();
                // Tối ưu: Tất cả boss đang REST → sleep 1s thay vì 150ms
                int delay = hasActiveBoss ? 150 : 1000;
                Functions.sleep(Math.max(delay - (System.currentTimeMillis() - st), 10));
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }
}
