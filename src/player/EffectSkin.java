package player;

/*
 *
 *
 * @author EMTI
 */
import item.Item;
import java.io.IOException;
import mob.Mob;
import services.ItemService;
import services.PlayerService;
import services.Service;
import services.InventoryService;
import services.MapService;
import utils.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.Setter;
import network.Message;
import services.EffectSkillService;
import services.func.UseItem;
import skill.Skill;

public class EffectSkin {

    private long lastSendInfoTime;
    private boolean sendInfo;

    private static final String[] textOdo = new String[]{
        "Hôi quá, tránh xa ta ra",
        "Biến đi",
        "Trời ơi đồ ở dơ",
        "Thúi quá",
        "Mùi gì hôi quá"
    };

    private static final String[] textThoBulma = new String[]{
        "Wow, sexy quá"
    };
    private static final String[] textDietQuy = new String[]{
        "Đừng để căm thù điều khiển mình.",
        "Em sẽ bảo vệ tất cả, dù trong im lặng",
        "Ngã xuống, nhưng vẫn bảo vệ mọi người",
        "Muốn sống sót, phải mạnh mẽ hơn!",
        "Đối mặt nỗi sợ, mới mạnh mẽ hơn",
        "Sức mạnh là kiểm soát cảm xúc",
        "Chấp nhận nỗi đau để bước tiếp",
        "Kẻ yếu không có quyền tồn tại",
        "Cần can đảm để biết sức mạnh mình"
    };

    private static final String[] textBuffSD = new String[]{
        "Wow!", "Đẹp quá!", "Xinh quá!"
    };

    private static final String[] textltdb = new String[]{
        "Lính Thủy Đánh Bạc", "Idol Vip Quá!", "Idol",
        "Trùm Server Đây Rồi!"
    };

    private static final String[] textXinbato = new String[]{
        "Bực bội quá",
        "Phân tâm quá",
        "Nực quá",
        "Im đi ông Xinbatô ơi",
        "Tránh ra đi Xinbatô ơi"
    };

    @Setter
    private Player player;

    public EffectSkin(Player player) {
        this.player = player;
        this.xHPKI = 1;
        this.xDame = 1;
    }

    public long lastTimeAttack;
    public long lastTimeVoHinh;
    private long lastTimeOdo;
    private long lastTimeltdb;
    private long lastTimeThoBulma;
    private long lastTimeDietQuy;
    private long lastTimeBunmaTocMau;
    private long lastTimeTiecbaiBien;
    private long lastTimeMaPhongBa;
    private long lastTimeBaby;
     private long lastTimeADR19;
    private long lastTimeXenHutHpKi;
    private long lastTimepetKhungLong;
    public long lastTimeAddTimeTrainArmor;
    public long lastTimeSubTimeTrainArmor;

    public boolean isVoHinh;

    public long lastTimeXHPKI;
    public int xHPKI;

    public long lastTimeXDame;
    public int xDame;

    public long lastTimeUpdateCTHT;

    private long lastTimeTanHinh;
    private long lastTimeLamCham;
    private long lastTimeHoaDa;
    private long lastTimeHalloween;
    public long lastTimeXChuong;
    public boolean isXChuong;
    public boolean isXDame;
    private long lastTimeTokuda;

    public void update() {
        updateVoHinh();
        if (!this.player.isDie() && this.player.zone != null && !MapService.gI().isMapOffline(this.player.zone.map.mapId)) {
            updateOdo();
            updateltdb();
            updateThoBulma();
            updateMaPhongBa();
            updateXenHutXungQuanh();
            updateTanHinh();
            updateHoaDa();
            updateLamCham();
            updateXChuong();
//            updateHalloween();
            updateCTDietQuy();
            updateBunmaTocMau();
            updateTiecbaiBien();
            updatePetKhungLong();
            updateBaby();
           
        }
        if (!this.player.isBoss && !this.player.isPet && !player.isNewPet&&this.player!=null) {
            updateTrainArmor();
        }
        if (xHPKI != 1 && Util.canDoWithTime(lastTimeXHPKI, 1800000)) {
            xHPKI = 1;
            Service.gI().point(player);
        }
        if (xDame != 1 && Util.canDoWithTime(lastTimeXDame, 1800000)) {
            xDame = 1;
            Service.gI().point(player);
        }
        updateCTHaiTac();
    }
//1488	1489	1490

    private void updateCTHaiTac() {
        if (this.player.setClothes != null && this.player.setClothes.ctHaiTac != -1
                && this.player.zone != null
                && Util.canDoWithTime(lastTimeUpdateCTHT, 5000)) {
            int count = 0;
            int[] cts = new int[9];
            cts[this.player.setClothes.ctHaiTac - 618] = this.player.setClothes.ctHaiTac;
            List<Player> players = new ArrayList<>();
            players.add(player);
            try {
                for (Player pl : player.zone.getNotBosses()) {
                    if (!player.equals(pl) && pl.setClothes.ctHaiTac != -1 && Util.getDistance(player, pl) <= 300) {
                        cts[pl.setClothes.ctHaiTac - 618] = pl.setClothes.ctHaiTac;
                        players.add(pl);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < cts.length; i++) {
                if (cts[i] != 0) {
                    count++;
                }
            }
            for (Player pl : players) {
                Item ct = pl.inventory.itemsBody.get(5);
                if (ct.isNotNullItem() && ct.template.id >= 618 && ct.template.id <= 626) {
                    for (Item.ItemOption io : ct.itemOptions) {
                        if (io.optionTemplate.id == 147
                                || io.optionTemplate.id == 77
                                || io.optionTemplate.id == 103) {
                            io.param = count * 3;
                        }
                    }
                }
                if (!pl.isPet && !pl.isNewPet && Util.canDoWithTime(lastTimeUpdateCTHT, 5000)) {
                    InventoryService.gI().sendItemBody(pl);
                }
                pl.effectSkin.lastTimeUpdateCTHT = System.currentTimeMillis();
            }
        }
    }

    private void updateXenHutXungQuanh() {
        try {
            if (this.player.nPoint != null && (player.nPoint.hp < player.nPoint.hpMax || player.nPoint.mp < player.nPoint.mpMax)) {
                int param = this.player.nPoint.tlHutHpMpXQ;
                if (param > 0) {
                    if (!this.player.isDie() && Util.canDoWithTime(lastTimeXenHutHpKi, 5000)) {
                        long hpHut = 0;
                        long mpHut = 0;
                        List<Player> players = new ArrayList<>();
                        List<Player> playersMap = this.player.zone.getNotBosses();
                        for (Player pl : playersMap) {
                            if (!this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                    && Util.getDistance(this.player, pl) <= 200) {
                                players.add(pl);
                            }

                        }
                        for (Mob mob : this.player.zone.mobs) {
                            if (mob.point.gethp() > 1) {
                                if (Util.getDistance(this.player, mob) <= 200) {
                                    long subHp = Util.maxIntValue(mob.point.getHpFull() * param / 100);
                                    if (subHp >= mob.point.gethp()) {
                                        subHp = mob.point.gethp() - 1;
                                    }
                                    hpHut += subHp;
                                    mob.injured(null, subHp, false);
                                }
                            }
                        }
                        for (Player pl : players) {
                            long subHp = Util.maxIntValue((long) pl.nPoint.hpMax * param / 100);
                            long subMp = Util.maxIntValue((long) pl.nPoint.mpMax * param / 100);
                            if (subHp >= pl.nPoint.hp) {
                                subHp = pl.nPoint.hp - 1;
                            }
                            if (subMp >= pl.nPoint.mp) {
                                subMp = pl.nPoint.mp - 1;
                            }
                            hpHut += subHp;
                            mpHut += subMp;
                            PlayerService.gI().sendInfoHpMpMoney(pl);
                            Service.gI().Send_Info_NV(pl);
                            pl.injured(null, subHp, true, false);
                        }
                        this.player.nPoint.addHp(hpHut);
                        this.player.nPoint.addMp(mpHut);
                        PlayerService.gI().sendInfoHpMpMoney(this.player);
                        Service.gI().Send_Info_NV(this.player);
                        this.lastTimeXenHutHpKi = System.currentTimeMillis();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOdo() {
        try {
            if (this.player.nPoint != null) {
                int param = this.player.nPoint.tlHpGiamODo;
                if (param > 0) {
                    if (Util.canDoWithTime(lastTimeOdo, 10000)) {
                        List<Player> playersMap = this.player.zone.getNotBosses();
                        for (int i = playersMap.size() - 1; i >= 0; i--) {
                            Player pl = playersMap.get(i);
                            if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                    && Util.getDistance(this.player, pl) <= 200) {
                                long subHp = Util.maxIntValue((long) pl.nPoint.hpMax * param / 100);
                                if (subHp >= pl.nPoint.hp) {
                                    subHp = pl.nPoint.hp - 1;
                                }
                                Service.gI().chat(pl, textOdo[Util.nextInt(0, textOdo.length - 1)]);
                                PlayerService.gI().sendInfoHpMpMoney(pl);
                                Service.gI().Send_Info_NV(pl);
                                pl.injured(null, subHp, true, false);
                            }

                        }
                        this.lastTimeOdo = System.currentTimeMillis();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateltdb() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.islinhthuydanhbac) {
                if (Util.canDoWithTime(lastTimeltdb, 10000)) {
                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 200) {
                            Service.gI().chat(pl, textltdb[Util.nextInt(0, textltdb.length - 1)]);
                        }

                    }
                    this.lastTimeltdb = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateThoBulma() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.tlSexyDame > 0) {
                if (Util.canDoWithTime(lastTimeThoBulma, 10000)) {

                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 120) {
                            if (player.nPoint.isThoBulma) {
                                Service.gI().chat(pl, textThoBulma[Util.nextInt(0, textThoBulma.length - 1)]);
                            } else {
                                Service.gI().chat(pl, textBuffSD[Util.nextInt(0, textBuffSD.length - 1)]);
                            }
                            EffectSkillService.gI().setDameBuff(player, 11000, player.nPoint.tlSexyDame);
                        }
                    }
                    this.lastTimeThoBulma = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//     private void updateDietQuy() {
//        try {
//            if (this.player.nPoint != null && this.player.nPoint.tlSexyDame > 0) {
//                if (Util.canDoWithTime(lastTimeDietQuy, 10000)) {
//
//                    List<Player> playersMap = this.player.zone.getNotBosses();
//                    for (int i = playersMap.size() - 1; i >= 0; i--) {
//                        Player pl = playersMap.get(i);
//                        if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
//                                && Util.getDistance(this.player, pl) <= 120) {
//                            if (player.nPoint.isDietQuy) {
//                                Service.gI().chat(pl, textDietQuy[Util.nextInt(0, textDietQuy.length - 1)]);
//                            } else {
//                                Service.gI().chat(pl, textBuffSD[Util.nextInt(0, textBuffSD.length - 1)]);
//                            }
//                            EffectSkillService.gI().setDameBuff(player, 11000, player.nPoint.tlSexyDame);
//                        }
//                    }
//                    this.lastTimeThoBulma = System.currentTimeMillis();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void updateCTDietQuy() {
        if (this.player.setClothes != null && this.player.setClothes.ctDietQuy != -1
                && this.player.zone != null
                && Util.canDoWithTime(lastTimeDietQuy, 5000)) {
            int count = 0;
            int[] cts = new int[5];
            cts[this.player.setClothes.ctDietQuy - 1087] = this.player.setClothes.ctDietQuy;
            List<Player> players = new ArrayList<>();
            players.add(player);
            try {
                for (Player pl : player.zone.getNotBosses()) {
                    if (!player.equals(pl) && pl.setClothes.ctDietQuy != -1 && Util.getDistance(player, pl) <= 300) {
                        cts[pl.setClothes.ctDietQuy - 1087] = pl.setClothes.ctDietQuy;
                        players.add(pl);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < cts.length; i++) {
                if (cts[i] != 0) {
                    count++;
                }
            }
            for (Player pl : players) {
                Item ct = pl.inventory.itemsBody.get(5);
                if (ct.isNotNullItem() && ct.template.id >= 1087 && ct.template.id <= 1091) {
                    for (Item.ItemOption io : ct.itemOptions) {
                        if (io.optionTemplate.id == 147) {
                            io.param = Math.min(count * 3, 18);
                        }
                    }
                }
                if (!pl.isPet && !pl.isNewPet && Util.canDoWithTime(lastTimeDietQuy, 5000)) {
                    InventoryService.gI().sendItemBody(pl);
                }
                pl.effectSkin.lastTimeDietQuy = System.currentTimeMillis();
            }
        }
    }

    private void updateBunmaTocMau() {
        if (this.player.setClothes != null && this.player.setClothes.ctBunmaTocMau != -1
                && this.player.zone != null
                && Util.canDoWithTime(lastTimeBunmaTocMau, 5000)) {
            int count = 0;
            int[] cts = new int[3];
            cts[this.player.setClothes.ctBunmaTocMau - 1208] = this.player.setClothes.ctBunmaTocMau;
            List<Player> players = new ArrayList<>();
            players.add(player);
            try {
                for (Player pl : player.zone.getNotBosses()) {
                    if (!player.equals(pl) && pl.setClothes.ctBunmaTocMau != -1 && Util.getDistance(player, pl) <= 300) {
                        cts[pl.setClothes.ctBunmaTocMau - 1208] = pl.setClothes.ctBunmaTocMau;
                        players.add(pl);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < cts.length; i++) {
                if (cts[i] != 0) {
                    count++;
                }
            }
            for (Player pl : players) {
                Item ct = pl.inventory.itemsBody.get(5);
                if (ct.isNotNullItem() && ct.template.id >= 1208 && ct.template.id <= 1210) {
                    for (Item.ItemOption io : ct.itemOptions) {
                        if (io.optionTemplate.id == 147) {
                            io.param = Math.min(count * 3, 10);
                        }
                    }
                }
                if (!pl.isPet && !pl.isNewPet && Util.canDoWithTime(lastTimeBunmaTocMau, 5000)) {
                    InventoryService.gI().sendItemBody(pl);
                }
                pl.effectSkin.lastTimeBunmaTocMau = System.currentTimeMillis();
            }
        }
    }

   private void updateTiecbaiBien() {
    if (this.player.setClothes == null || this.player.setClothes.ctTiecbaiBien == -1
            || this.player.zone == null || !Util.canDoWithTime(lastTimeTiecbaiBien, 5000)) {
        return;
    }

    int count = 1;
    int[] cts = new int[3];

    if (this.player.setClothes.ctTiecbaiBien >= 1234 && this.player.setClothes.ctTiecbaiBien <= 1236) {
        cts[this.player.setClothes.ctTiecbaiBien - 1234] = this.player.setClothes.ctTiecbaiBien;
    }

    List<Player> players = new ArrayList<>();
    players.add(player);

    try {
        for (Player pl : player.zone.getNotBosses()) {
            if (pl.equals(player) || pl.setClothes.ctTiecbaiBien == -1 || Util.getDistance(player, pl) > 300) {
                continue; // Bỏ qua nếu là chính mình hoặc ngoài phạm vi 300
            }

            if (pl.setClothes.ctTiecbaiBien >= 1234 && pl.setClothes.ctTiecbaiBien <= 1236) {
                continue; // Bỏ qua người có cải trang từ 1234 đến 1236
            }

            cts[pl.setClothes.ctTiecbaiBien - 1234] = pl.setClothes.ctTiecbaiBien;
            players.add(pl);
            count++;

            Item ct = pl.inventory.itemsBody.get(5);
            if (ct.isNotNullItem() && ct.template.id >= 1234 && ct.template.id <= 1236) {
                for (Item.ItemOption io : ct.itemOptions) {
                    if (io.optionTemplate.id == 147) {
                        io.param = io.param - Math.min(count * 5, 15); // giảm tối đa 15%
                    }
                }
            }

            // Cập nhật hiệu ứng và gửi lại trạng thái item
            if (!pl.isPet && !pl.isNewPet) {
                InventoryService.gI().sendItemBody(pl);
            }
            pl.effectSkin.lastTimeTiecbaiBien = System.currentTimeMillis();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
   
   private void updatePetKhungLong() {
    if (this.player.setClothes == null || this.player.setClothes.petKhungLong == -1
            || this.player.zone == null) {
        return;
    }

    List<Player> players = new ArrayList<>();
    players.add(player);

    try {
        for (Player pl : player.zone.getNotBosses()) {
            if (pl.equals(player) || pl.setClothes.petKhungLong == 1118 || Util.getDistance(player, pl) > 300) {
                continue; // Bỏ qua nếu là chính mình, đã có ID 1118 hoặc ngoài phạm vi 300
            }

            boolean hasItem1118 = false;
            
                Item item = pl.inventory.itemsBody.get(7); // Lấy item ở vị trí thứ 7
                if (item.isNotNullItem() && item.template.id == 1118) {
                    hasItem1118 = true;
                }
            

            // Nếu không có item 1118 ở slot 7, giảm 5% sát thương
            if (!hasItem1118) {
                pl.nPoint.dame *= 0.9; // Giảm sát thương còn 90%
            }

            // Cập nhật hiệu ứng và gửi lại trạng thái item
            if (!pl.isPet && !pl.isNewPet) {
                InventoryService.gI().sendItemBody(pl);
            }
            pl.effectSkin.lastTimepetKhungLong = System.currentTimeMillis();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void updateBaby() {
    try {
       
        // Nếu là lần đầu tiên hoặc đã đủ 60 giây kể từ lần cuối
         if (Util.canDoWithTime(lastTimeBaby, 30000)) {
            if (player.nPoint.isBaby) {
                if (player.effectSkill.isMonkey) {
                    // Chọn ngẫu nhiên trạng thái
                    player.luachon = Util.nextInt(1, 3);
                    
                    switch (player.luachon) {
                        case 1:
                            Service.gI().sendTitle(player, 1024);
                            Service.gI().chat(player, "Đã chuyển Trạng Thái Năng Lượng Tăng 20%HP và 10% sát thương bom");
                            break;
                        case 2:
                            Service.gI().sendTitle(player, 1020);
                            Service.gI().chat(player, "Đã chuyển Trạng Thái Năng Lượng Tăng 20%Ki và 10% sát thương laze");
                            break;
                        case 3:
                            Service.gI().sendTitle(player, 1025);
                            Service.gI().chat(player, "Đã chuyển Trạng Thái Cuồng sát Tăng 20%SĐ và 10% tốc đánh");
                            break;
                        
                        default:
                            Service.gI().chat(player, "Chưa vào trạng thái!!!");
                            break;
                    }
                } else {
                    player.luachon = 0;
                }
            }

            // Cập nhật thời gian làm mới
            player.effectSkin.lastTimeBaby = System.currentTimeMillis();
            
            // Gửi lại trạng thái nếu không phải pet
            if (!player.isPet && !player.isNewPet) {
                InventoryService.gI().sendItemBody(player);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


    private void updateTanHinh() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.isTanHinh) {
                if (Util.canDoWithTime(lastTimeTanHinh, 5000)) {
                    EffectSkillService.gI().setIsTanHinh(player, 1500);
                    this.lastTimeTanHinh = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHoaDa() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.isHoaDa) {
                if (Util.canDoWithTime(lastTimeHoaDa, 30000)) {
                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 200) {
                            EffectSkillService.gI().setIsStone(pl, 6000);
                        }

                    }
                    this.lastTimeHoaDa = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLamCham() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.isLamCham) {
                if (Util.canDoWithTime(lastTimeLamCham, 10000)) {

                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 200) {
                            Service.gI().chat(pl, "Nặng quá!");
                            EffectSkillService.gI().setIsLamCham(pl, 5000);
                        }

                    }
                    this.lastTimeLamCham = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateXChuong() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.xChuong != 0) {
                if (Util.canDoWithTime(lastTimeXChuong, 60000)) {
                    this.isXChuong = true;
                    this.lastTimeXChuong = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMaPhongBa() {
        try {
            if (this.player.effectSkill != null && this.player.effectSkill.isBinh && this.player.effectSkill.playerUseMafuba != null) {
                if (Util.canDoWithTime(lastTimeMaPhongBa, 500) && this.player.effectSkill.playerUseMafuba.playerSkill != null) {
                    double param = this.player.effectSkill.playerUseMafuba.playerSkill.getSkillbyId(Skill.MA_PHONG_BA).point * (this.player.effectSkill.typeBinh == 0 ? 1 : 2);
                    long subHp = Util.maxIntValue((long) (this.player.effectSkill.playerUseMafuba.nPoint.hpMax * param / 100));
                    if (subHp >= this.player.nPoint.hp) {
                        subHp = Math.abs(this.player.nPoint.hp - 100);
                    }
                    PlayerService.gI().sendInfoHpMpMoney(this.player);
                    Service.gI().Send_Info_NV(this.player);
                    this.player.injured(this.player.effectSkill.playerUseMafuba, subHp, true, false);
                    this.lastTimeMaPhongBa = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHalloween() {
        try {
            if (this.player.effectSkill != null && this.player.effectSkill.isHalloween) {
                if (Util.canDoWithTime(lastTimeHalloween, 10000)) {
                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (pl != null && pl.nPoint != null && !this.player.equals(pl) && pl.effectSkill != null && !pl.effectSkill.isHalloween && !pl.isPet && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 200) {
                            EffectSkillService.gI().setIsHalloween(pl, -1, 1800000);
                        }

                    }
                    this.lastTimeHalloween = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //giáp tập luyện
    private void updateTrainArmor() {
        if (Util.canDoWithTime(lastTimeAddTimeTrainArmor, 60000) && !Util.canDoWithTime(lastTimeAttack, 30000)) {
            if (this.player.nPoint.wearingTrainArmor) {
                for (Item.ItemOption io : this.player.inventory.trainArmor.itemOptions) {
                    if (io.optionTemplate.id == 9) {
                        if (io.param < 1000) {
                            io.param++;
                            InventoryService.gI().sendItemBody(player);
                        }
                        break;
                    }
                }
            }
            this.lastTimeAddTimeTrainArmor = System.currentTimeMillis();
        }
        if (Util.canDoWithTime(lastTimeSubTimeTrainArmor, 60000)) {
            for (Item item : this.player.inventory.itemsBag) {
                if (item.isNotNullItem()) {
                    if (ItemService.gI().isTrainArmor(item)) {
                        for (Item.ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 9) {
                                if (io.param > 0) {
                                    io.param--;
                                }
                            }
                        }
                    }
                } else {
                    break;
                }
            }
            for (Item item : this.player.inventory.itemsBox) {
                if (item.isNotNullItem()) {
                    if (ItemService.gI().isTrainArmor(item)) {
                        for (Item.ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 9) {
                                if (io.param > 0) {
                                    io.param--;
                                }
                            }
                        }
                    }
                } else {
                    break;
                }
            }
            this.lastTimeSubTimeTrainArmor = System.currentTimeMillis();
            InventoryService.gI().sendItemBag(player);
            Service.gI().point(this.player);
        }
    }

    private void updateVoHinh() {
        if (this.player.nPoint != null && this.player.nPoint.wearingVoHinh) {
            if (Util.canDoWithTime(lastTimeAttack, 5000)) {
                isVoHinh = Util.canDoWithTime(lastTimeVoHinh, 5000);
            } else {
                isVoHinh = false;
            }
        } else {
            isVoHinh = false;
        }
    }

    public void dispose() {
        this.player = null;
    }
}
