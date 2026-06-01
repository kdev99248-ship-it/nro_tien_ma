package boss;

import boss.BossID;
import boss.BossManager;
import boss.BossData;

/*
 *
 *
 * @author zl NRO_MOD
 */
import boss.boss_manifest.Commeson.PhanThan;
import consts.ConstPlayer;
import boss.iboss.IBossOutfit;
import network.Message;
import java.util.List;
import map.Zone;
import mob.Mob;
import player.Pet;
import player.Player;
import player.PlayerClone;
import player.LinhDanhThue;
import skill.Skill;
import server.ServerNotify;
import services.EffectSkillService;
import services.MapService;
import services.PlayerService;
import services.Service;
import services.SkillService;
import services.TaskService;
import services.func.ChangeMapService;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;
import boss.iboss.IBoss;
import java.io.IOException;

public class Boss extends Player implements IBoss, IBossOutfit {

    public int currentLevel = -1;
    protected final BossData[] data;
    protected BossConfig config;

    public BossStatus bossStatus;

    protected Zone lastZone;

    protected long lastTimeRest;
    protected int secondsRest;

    protected long lastTimeChatS;
    protected int timeChatS;
    protected byte indexChatS;

    protected long lastTimeChatE;
    protected int timeChatE;
    protected byte indexChatE;

    public int idSkillPlayer = -1;
    public Player playertarget;
    public Mob mobTarget;

    protected long lastTimeChatM;
    protected int timeChatM;

    protected long lastTimeTargetPlayer;
    protected int timeTargetPlayer;
    protected Player playerTarger;

    protected Boss parentBoss;
    public Boss[][] bossAppearTogether;

    public Zone zoneFinal = null;

    public Player playerReward;

    public int lv;

    public int error;

    public boolean prepareBom;

    public boolean isNotifyDisabled;
    public boolean isZone01SpawnDisabled;

    private long lastTimeCheck;

    public Boss(int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        this(null, id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(BossType bossType, int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data)
            throws Exception {
        this(bossType, id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(BossConfig config, BossData[] data) throws Exception {
        this(null, config.getBossId(), data);
        this.config = config;
        this.isNotifyDisabled = config.isNotifyDisabled();
        this.isZone01SpawnDisabled = config.isZone01SpawnDisabled();
    }

    public Boss(int id, BossData... data) throws Exception {
        this(null, id, data);
    }

    public Boss(BossType bossType, int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ");
        }
        this.data = data;
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;

        if (bossType == null) {
            BossManager.gI().addBoss(this);
        } else {
            switch (bossType) {
                case YARDART, FINAL, BROLY, ANTROM -> BossManager.gI().addBoss(this);
                case SKILLSUMMONED -> SkillSummonedManager.gI().addBoss(this);
                case PHOBAN -> OtherBossManager.gI().addBoss(this);
                case PHOBANDT -> RedRibbonHQManager.gI().addBoss(this);
                case PHOBANBDKB -> TreasureUnderSeaManager.gI().addBoss(this);
                case PHOBANCDRD -> SnakeWayManager.gI().addBoss(this);
                case PHOBANKGHD -> GasDestroyManager.gI().addBoss(this);
                case TRUNGTHU_EVENT -> TrungThuEventManager.gI().addBoss(this);
                case HALLOWEEN_EVENT -> HalloweenEventManager.gI().addBoss(this);
                case CHRISTMAS_EVENT -> ChristmasEventManager.gI().addBoss(this);
                case HUNGVUONG_EVENT -> HungVuongEventManager.gI().addBoss(this);
                case TET_EVENT -> LunarNewYearEventManager.gI().addBoss(this);
            }
        }

        this.bossAppearTogether = new Boss[this.data.length][];
        for (int i = 0; i < this.bossAppearTogether.length; i++) {
            if (this.data[i].getBossesAppearTogether() != null) {
                this.bossAppearTogether[i] = new Boss[this.data[i].getBossesAppearTogether().length];
                for (int j = 0; j < this.data[i].getBossesAppearTogether().length; j++) {
                    Boss boss = BossManager.gI().createBoss(this.data[i].getBossesAppearTogether()[j]);
                    if (boss != null) {
                        boss.parentBoss = this;
                        boss.lv = j;
                        this.bossAppearTogether[i][j] = boss;
                    }
                }
            }
        }
    }

    @Override
    public void initBase() {
        BossData data = this.data[this.currentLevel];
        this.name = String.format(data.getName(), Util.nextInt(0, 100));
        this.gender = data.getGender();
        this.nPoint.mpg = 31_07_2002;
        this.nPoint.dameg = data.getDame();
        this.nPoint.hpg = data.getHp()[Util.nextInt(0, data.getHp().length - 1)];
        this.nPoint.hp = nPoint.hpg;
        this.nPoint.calPoint();
        this.initSkill();
        this.resetBase();
    }

    protected void initSkill() {
        for (Skill skill : this.playerSkill.skills) {
            skill.dispose();
        }
        this.playerSkill.skills.clear();
        this.playerSkill.skillSelect = null;
        int[][] skillTemps = data[this.currentLevel].getSkillTemp();
        if (skillTemps == null || skillTemps.length == 0) {
            // Logger.warning("Boss " + this.name + " (id=" + this.id + ", level=" +
            // this.currentLevel + ") không có skill từ DB → fallback theo gender\n");
            // Fallback: gán skill theo chủng tộc (gender) khi DB chưa có dữ liệu
            skillTemps = getDefaultSkillsByGender(this.gender);
        }
        for (int[] skillTemp : skillTemps) {
            Skill skill = SkillUtil.createSkill(skillTemp[0], skillTemp[1]);
            if (skillTemp.length == 3) {
                skill.coolDown = skillTemp[2];
            }
            this.playerSkill.skills.add(skill);
        }

        // Nếu vẫn không có skill, ép học skill Đấm (ID 0) để không đứng yên
        if (this.playerSkill.skills.isEmpty()) {
            this.playerSkill.skills.add(SkillUtil.createSkill(0, 7));
        }
    }

    /**
     * Trả về bộ skill mặc định theo chủng tộc khi DB chưa có dữ liệu skill.
     * Trái Đất(0): Dragon + Kamejoko + Kaioken
     * Namek(1): Demon + Masenko + Makankosappo
     * Xayda(2): Galick + Antomic + Lien Hoan
     */
    private int[][] getDefaultSkillsByGender(byte gender) {
        return switch (gender) {
            case 0 -> new int[][] { { Skill.DRAGON, 7 }, { Skill.KAMEJOKO, 7 }, { Skill.KAIOKEN, 7 } };
            case 1 -> new int[][] { { Skill.DEMON, 7 }, { Skill.MASENKO, 7 }, { Skill.MAKANKOSAPPO, 7 } };
            case 2 -> new int[][] { { Skill.GALICK, 7 }, { Skill.ANTOMIC, 7 }, { Skill.LIEN_HOAN, 7 } };
            default -> new int[][] { { Skill.DRAGON, 7 }, { Skill.KAMEJOKO, 7 } };
        };
    }

    protected void resetBase() {
        this.lastTimeChatS = 0;
        this.lastTimeChatE = 0;
        this.timeChatS = 0;
        this.timeChatE = 0;
        this.indexChatS = 0;
        this.indexChatE = 0;
    }

    // .outfit.
    @Override
    public short getHead() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][0];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        }
        return this.data[this.currentLevel].getOutfit()[0];
    }

    @Override
    public short getBody() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][1];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        }
        return this.data[this.currentLevel].getOutfit()[1];
    }

    @Override
    public short getLeg() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][2];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        }
        return this.data[this.currentLevel].getOutfit()[2];

    }

    @Override
    public short getFlagBag() {
        return this.data[this.currentLevel].getOutfit()[3];
    }

    @Override
    public byte getAura() {
        return (byte) this.data[this.currentLevel].getOutfit()[4];
    }

    @Override
    public byte getEffFront() {
        return (byte) this.data[this.currentLevel].getOutfit()[5];
    }

    public PlayerClone getClone() {
        return clone;
    }

    public void setClone(PlayerClone clone) {
        this.clone = clone;
    }

    public boolean isIsClone() {
        return isClone;
    }

    public void setIsClone(boolean isClone) {
        this.isClone = isClone;
    }

    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0,
                this.data[this.currentLevel].getMapJoin().length - 1)];
        Zone map = MapService.gI().getMapWithRandZone(mapId);
        return map;
    }

    @Override
    public void changeStatus(BossStatus status) {
        this.bossStatus = status;
    }

    @Override
    public Player getPlayerAttack() {
        if (this.zone == null) {
            return null;
        }
        if (this.playerTarger != null && (this.playerTarger.isDie() || !this.zone.equals(this.playerTarger.zone)
                || (this.playerTarger.effectSkill != null && this.playerTarger.effectSkill.isTanHinh))) {
            this.playerTarger = null;
        }
        if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
            this.playerTarger = this.zone.getRandomPlayerInMap();
            this.lastTimeTargetPlayer = System.currentTimeMillis();
            this.timeTargetPlayer = Util.nextInt(5000, 7000);
        }
        if (this.playerTarger != null && this.playerTarger.isPet && ((Pet) this.playerTarger).master != null
                && ((Pet) this.playerTarger).master.equals(this)) {
            this.playerTarger = null;
        }
        return this.playerTarger;
    }

    @Override
    public void changeToTypePK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
    }

    @Override
    public void changeToTypeNonPK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
    }

    @Override
    public void updateInfo() {
        super.update();
    }

    @Override
    public void update() {
        if (prepareBom) {
            return;
        }
        super.update();
        if (this.nPoint == null) {
            return;
        }
        this.nPoint.mp = this.nPoint.mpg;
        if (this.effectSkill == null || this.effectSkill.isHaveEffectSkill()
                || (this.newSkill != null && this.newSkill.isStartSkillSpecial)) {
            return;
        }
        // Tối ưu: Khi boss đang ACTIVE/CHAT_S/AFK mà zone không có player thật
        // → skip logic tấn công/di chuyển/chat để giảm CPU
        // Các trạng thái REST/RESPAWN/JOIN_MAP/DIE/CHAT_E/LEAVE_MAP vẫn xử lý bình
        // thường
        if (this.zone != null && !this.zone.hasRealPlayer()) {
            switch (this.bossStatus) {
                case CHAT_S, AFK, ACTIVE -> {
                    return; // Sleep: không cần xử lý khi không có player
                }
                default -> {
                } // Các trạng thái khác vẫn xử lý bình thường
            }
        }
        switch (this.bossStatus) {
            case CHAT_S, AFK, ACTIVE ->
                this.autoLeaveMap();
        }
        switch (this.bossStatus) {
            case REST ->
                this.rest();
            case RESPAWN -> {
                this.respawn();
                this.changeStatus(BossStatus.JOIN_MAP);
            }
            case JOIN_MAP ->
                this.joinMap();
            case CHAT_S -> {
                if (chatS()) {
                    this.doneChatS();
                    this.lastTimeChatM = System.currentTimeMillis();
                    this.timeChatM = 5000;
                    if (this.bossStatus != BossStatus.AFK) {
                        this.changeStatus(BossStatus.ACTIVE);
                    }
                }
            }
            case AFK ->
                this.afk();
            case ACTIVE -> {
                this.chatM();
                if (this.effectSkill.isCharging && !Util.isTrue(1, 20) || this.effectSkill.useTroi) {
                    return;
                }
                this.active();
            }
            case DIE ->
                this.changeStatus(BossStatus.CHAT_E);
            case CHAT_E -> {
                if (chatE()) {
                    this.doneChatE();
                    this.changeStatus(BossStatus.LEAVE_MAP);
                }
            }
            case LEAVE_MAP ->
                this.leaveMap();
        }
    }

    @Override
    public void rest() {
        int nextLevel = this.currentLevel + 1;
        if (nextLevel >= this.data.length) {
            nextLevel = 0;
        }
        if (this.data[nextLevel].getTypeAppear() == AppearType.DEFAULT_APPEAR
                && Util.canDoWithTime(lastTimeRest, secondsRest * 1000)) {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void afk() {

    }

    @Override
    public void respawn() {
        this.currentLevel++;
        if (this.currentLevel >= this.data.length) {
            this.currentLevel = 0;
        }
        this.initBase();
        this.changeToTypeNonPK();
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            this.wakeupAnotherBossWhenAppear();
            return;
        }
        if (this.zone == null) {
            if (this.parentBoss != null && this.parentBoss.zone != null) {
                this.zone = parentBoss.zone;
            } else if (this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null) {
                // Nếu không có leader hoặc leader chưa vào map, tìm con đồng đội nào đã vào map
                // rồi để bay theo
                for (Boss b : this.bossAppearTogether[this.currentLevel]) {
                    if (b != null && b.zone != null) {
                        this.zone = b.zone;
                        break;
                    }
                }
            }

            if (this.zone == null) {
                if (this.parentBoss != null) {
                    return; // Vẫn phải đợi nếu là boss đệ mà chưa ai vào map
                }
                if (this.lastZone == null) {
                    this.zone = getMapJoin();
                } else {
                    this.zone = this.lastZone;
                }
            }
        }
        if (this.zone == null) {
            this.zone = getMapJoin();
        }
        if (this.zone != null) {
            try {
                if (this.currentLevel == 0) {
                    if (this.parentBoss == null) {
                        int zoneid = this.zone.zoneId;
                        // this.zone.map.mapId == 80 || this.zone.map.mapId == 103 ||
                        // this.zone.map.mapId == 97 || this.zone.map.mapId == 102
                        // Chỉ cho boss xuất hiện từ khu 2 trở lên ở map thường
                        if (this.isZone01SpawnDisabled && this.zone.map.zones.size() > 2) {
                            zoneid = Util.nextInt(2, this.zone.map.zones.size() - 1);
                            while (zoneid < this.zone.map.zones.size()
                                    && !this.zone.map.zones.get(zoneid).getBosses().isEmpty()) {
                                zoneid++;
                            }

                            if (zoneid < this.zone.map.zones.size()) {
                                this.zone = this.zone.map.zones.get(zoneid);
                            } else {
                                this.changeStatus(BossStatus.REST);
                                this.zone = null;
                                this.lastZone = null;
                                return;
                            }
                        } else {
                            // Check trong khu lớn hơn 10 người chuyển sang khu n + 1
                            while (zoneid < this.zone.map.zones.size()
                                    && this.zone.map.zones.get(zoneid).getNumOfPlayers() > 10) {
                                zoneid++;
                            }
                            // Check trong khu có boss sẽ chuyển sang khu n + 1
                            while (zoneid < this.zone.map.zones.size()
                                    && !this.zone.map.zones.get(zoneid).getBosses().isEmpty()) {
                                zoneid++;
                            }
                            if (zoneid < this.zone.map.zones.size()) {
                                this.zone = this.zone.map.zones.get(zoneid);
                            }
                        }
                        int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100)
                                : Util.nextInt(100);
                        int y = this.zone.map.yPhysicInTop(x, 100);
                        ChangeMapService.gI().changeMap(this, this.zone, x, y);
                    } else {
                        int x = this.parentBoss.location.x - (this.lv + 1) * 30;
                        int y = this.zone.map.yPhysicInTop(x, 100);
                        ChangeMapService.gI().changeMap(this, this.zone, x, y);
                    }
                    this.wakeupAnotherBossWhenAppear();
                } else {
                    ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
                }
                Service.gI().sendFlagBag(this);
                this.notifyJoinMap();
                this.changeStatus(BossStatus.CHAT_S);
            } catch (Exception e) {
                this.changeStatus(BossStatus.REST);
                if (error < 5) {
                    Logger.error("Lỗi : " + e + "\n");
                    error++;
                }
            }
        } else {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    public void joinMapByZone(Zone zone) {
        if (zone != null) {
            this.zone = zone;
            int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100) : Util.nextInt(100);
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
        }
    }

    protected void notifyJoinMap() {
        if (canSendNotify()) {
            ServerNotify.gI().notify(
                    "BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName + " khu: " + this.zone.zoneId);
            Logger.warning("BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName + " khu: "
                    + this.zone.zoneId + "\n");
        }
    }

    private boolean canSendNotify() {
        return !(this.isNotifyDisabled
                || this.zone.map.mapId == 140
                || MapService.gI().isMapPhoBan(this.zone.map.mapId)
                || MapService.gI().isMapMaBu(this.zone.map.mapId)
                || MapService.gI().isMapBlackBallWar(this.zone.map.mapId)
                || MapService.gI().isMapOffline(this.zone.map.mapId)
                || MapService.gI().isMapYardart(this.zone.map.mapId)
                || MapService.gI().isMapDHVT23(this.zone.map.mapId)
                || this.zone.map.mapId == 111 // || this.id != BossID.TAU_PAY_PAY_DONG_NAM_KARIN
        );
    }

    @Override
    public boolean chatS() {
        if (Util.canDoWithTime(lastTimeChatS, timeChatS)) {
            if (this.indexChatS == this.data[this.currentLevel].getTextS().length) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextS()[this.indexChatS];
            int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            if (!this.chat(prefix, textChat)) {
                return false;
            }
            this.lastTimeChatS = System.currentTimeMillis();
            this.timeChatS = textChat.length() * 100;
            if (this.timeChatS > 2000) {
                this.timeChatS = 2000;
            }
            this.indexChatS++;
        }
        return false;
    }

    @Override
    public void doneChatS() {

    }

    @Override
    public void chatM() {
        if (this.typePk == ConstPlayer.NON_PK) {
            return;
        }
        if (this.data[this.currentLevel].getTextM().length == 0) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) {
            return;
        }
        String textChat = this.data[this.currentLevel].getTextM()[Util.nextInt(0,
                this.data[this.currentLevel].getTextM().length - 1)];
        int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
        textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
        this.chat(prefix, textChat);
        this.lastTimeChatM = System.currentTimeMillis();
        this.timeChatM = Util.nextInt(3000, 20000);
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    protected long lastTimeAttack;

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                if (this.playerSkill.skills.isEmpty()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills
                        .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void checkPlayerDie(Player player) {
        if (player.isDie()) {

        }
    }

    protected int getRangeCanAttackWithSkillSelect() {
        int skillId = this.playerSkill.skillSelect.template.id;
        if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC) {
            return Skill.RANGE_ATTACK_CHIEU_CHUONG;
        } else if (skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK
                || skillId == Skill.LIEN_HOAN || skillId == Skill.KAIOKEN) {
            return Skill.RANGE_ATTACK_CHIEU_DAM;
        }
        return 500;
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            if (plKill instanceof Pet) {
                plKill = ((Pet) plKill).master;
            } else if (plKill instanceof PlayerClone) {
                plKill = ((PlayerClone) plKill).master;
            } else if (plKill instanceof LinhDanhThue) {
                plKill = ((LinhDanhThue) plKill).master;
            }
            if (plKill.playerAtt != null) {
                plKill = plKill.playerAtt;
            }
            reward(plKill);
            plKill.sosumenhplayer.addCountTask(2);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void reward(Player plKill) {
        if (config == null) {
            defaultReward(plKill);
            return;
        }
        java.util.Map<String, Object> rewardCfg = config.parseRewardConfig();
        if (rewardCfg == null) {
            defaultReward(plKill);
            return;
        }

        // pointBossDay
        if (rewardCfg.containsKey("pointBossDay")) {
            plKill.pointbossday += ((Number) rewardCfg.get("pointBossDay")).intValue();
        }

        // mabuPoint
        if (rewardCfg.containsKey("mabuPoint")) {
            if (plKill.fightMabu != null) {
                plKill.fightMabu.changePoint(((Number) rewardCfg.get("mabuPoint")).byteValue());
            }
        }

        // goHome
        if (rewardCfg.containsKey("goHome")) {
            if (plKill.isPl()) {
                plKill.goHome = true;
                plKill.timeGohome = ((Number) rewardCfg.get("goHome")).intValue();
            }
        }

        // Điểm sự kiện
        if (plKill.event != null) {
            if (rewardCfg.containsKey("pointBHM")) {
                plKill.event.addEventPointBHM(((Number) rewardCfg.get("pointBHM")).intValue());
            }
            if (rewardCfg.containsKey("pointNHS")) {
                plKill.event.addEventPointNHS(((Number) rewardCfg.get("pointNHS")).intValue());
            }
        }

        // checkTask
        if (rewardCfg.containsKey("checkTask") && (boolean) rewardCfg.get("checkTask")) {
            services.TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        }

        // drops - mảng các item drop [{"itemId": 16, "chance": 10, "count": [1, 1]}]
        if (rewardCfg.containsKey("drops")) {
            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, Object>> drops = (java.util.List<java.util.Map<String, Object>>) rewardCfg
                    .get("drops");
            for (java.util.Map<String, Object> drop : drops) {
                int itemId = ((Number) drop.get("itemId")).intValue();
                int qty = drop.containsKey("qty") ? ((Number) drop.get("qty")).intValue() : 1;
                int countMin = 1, countMax = 1;
                if (drop.containsKey("count")) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Number> countRange = (java.util.List<Number>) drop.get("count");
                    countMin = countRange.get(0).intValue();
                    countMax = countRange.size() > 1 ? countRange.get(1).intValue() : countMin;
                }
                int chance = drop.containsKey("chance") ? ((Number) drop.get("chance")).intValue() : 100;

                if (utils.Util.isTrue(chance, 100)) {
                    for (int i = 0; i < utils.Util.nextInt(countMin, countMax); i++) {
                        map.ItemMap it = new map.ItemMap(this.zone, itemId, qty,
                                this.location.x + utils.Util.nextInt(-20, 20),
                                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                                plKill.id);
                        services.Service.gI().dropItemMap(this.zone, it);
                    }
                }
            }
        }

        // dropDoTL - drop đồ thời lượng random
        if (rewardCfg.containsKey("dropDoTL")) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> doTL = (java.util.Map<String, Object>) rewardCfg.get("dropDoTL");
            int chance = doTL.containsKey("chance") ? ((Number) doTL.get("chance")).intValue() : 20;
            if (utils.Util.isTrue(chance, 100)) {
                map.ItemMap it = services.ItemService.gI().randDoTL(this.zone, 1, this.location.x,
                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
                services.Service.gI().dropItemMap(this.zone, it);
            }
        }

        if (!rewardCfg.containsKey("skipBaseReward") || !(boolean) rewardCfg.get("skipBaseReward")) {
            defaultReward(plKill);
        }
    }

    private void defaultReward(Player plKill) {
        if (plKill.playerAtt != null) {
            plKill = plKill.playerAtt;
        }
        services.TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (plKill.event != null) {
            plKill.event.addEventPointBHM(1);
        }
        services.Service.gI().sendThongBao(plKill, "Bạn đã tiêu diệt được " + this.name + " và nhận 1 điểm Bà Hạt Mít");
    }

    @Override
    public boolean chatE() {
        if (Util.canDoWithTime(lastTimeChatE, timeChatE)) {
            if (this.indexChatE == this.data[this.currentLevel].getTextE().length) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextE()[this.indexChatE];
            int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            if (!this.chat(prefix, textChat)) {
                return false;
            }
            this.lastTimeChatE = System.currentTimeMillis();
            this.timeChatE = textChat.length() * 100;
            if (this.timeChatE > 2000) {
                this.timeChatE = 2000;
            }
            this.indexChatE++;
        }
        return false;
    }

    @Override
    public void doneChatE() {

    }

    @Override
    public void leaveMap() {
        if (this.currentLevel < this.data.length - 1) {
            this.lastZone = this.zone;
            this.changeStatus(BossStatus.RESPAWN);
        } else {
            ChangeMapService.gI().exitMap(this);
            this.lastZone = null;
            this.lastTimeRest = System.currentTimeMillis();
            this.changeStatus(BossStatus.REST);
        }
        this.wakeupAnotherBossWhenDisappear();
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            if (plAtt != null && plAtt.idNRNM != -1) {
                return 1;
            }

            damage = this.nPoint.subDameInjureWithDeff(damage);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

            this.nPoint.subHP(damage);

            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }

            return damage;
        } else {
            return 0;
        }
    }

    @Override
    public void moveToPlayer(Player pl) {
        if (pl.location != null) {
            moveTo(pl.location.x, pl.location.y);
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(40, 60);
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move),
                y + (Util.isTrue(3, 10) ? -50 : 0));
    }

    public void chat(String text) {
        Service.gI().chat(this, text);
    }

    protected boolean chat(int prefix, String textChat) {
        if (prefix == -1) {
            this.chat(textChat);
        } else if (prefix == -2) {
            if (this.zone != null) {
                Player plMap = this.zone.getRandomPlayerInMap();
                if (plMap != null && !plMap.isDie() && Util.getDistance(this, plMap) <= 600) {
                    Service.gI().chat(plMap, textChat);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (prefix == -3) {
            if (this.parentBoss != null && !this.parentBoss.isDie()) {
                this.parentBoss.chat(textChat);
            }
        } else if (prefix >= 0) {
            if (this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null) {
                Boss boss = this.bossAppearTogether[this.currentLevel][prefix];
                if (!boss.isDie()) {
                    boss.chat(textChat);
                }
            } else if (this.parentBoss != null && this.parentBoss.bossAppearTogether != null
                    && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
                Boss boss = this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][prefix];
                if (!boss.isDie()) {
                    boss.chat(textChat);
                }
            }
        }
        return true;
    }

    @Override
    public void wakeupAnotherBossWhenAppear() {
        if (this.parentBoss != null) {
            return; // Chỉ Leader mới được phép gọi đệ tử
        }
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            if (boss == null || boss == this)
                continue;
            int nextLevelBoss = boss.currentLevel + 1;
            if (nextLevelBoss >= boss.data.length) {
                nextLevelBoss = 0;
            }
            if (boss.data[nextLevelBoss].getTypeAppear() == AppearType.CALL_BY_ANOTHER) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
            }
            if (boss.data[nextLevelBoss].getTypeAppear() == AppearType.APPEAR_WITH_ANOTHER) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
                boss.changeStatus(BossStatus.RESPAWN);
            }
        }
    }

    @Override
    public void wakeupAnotherBossWhenDisappear() {
    }

    @Override
    public void autoLeaveMap() {

    }

    public void leaveMapNew() {
        if (this.data != null) {
            this.currentLevel = this.data.length;
        }
        this.changeStatus(BossStatus.LEAVE_MAP);
    }

    @Override
    public void setBom(Player plAtt) {
        try {
            if (!prepareBom) {
                prepareBom = true;
                this.nPoint.hp = 1;
                long lastTime = System.currentTimeMillis();
                // gồng tự sát
                Service.gI().chat(Boss.this, "Rồi, rồi, mày xong rồi!");
                Message msg;
                try {
                    msg = new Message(-45);
                    msg.writer().writeByte(7);
                    msg.writer().writeInt((int) Boss.this.id);
                    msg.writer().writeShort(104);
                    msg.writer().writeShort(2000);
                    Service.gI().sendMessAllPlayerInMap(Boss.this, msg);
                    msg.cleanup();
                } catch (IOException e) {
                }
                while (prepareBom) {
                    if (Util.canDoWithTime(lastTime, 2500)) {
                        setDie(this);
                        die(plAtt);
                        long dame = Util.maxIntValue(Boss.this.nPoint.hpMax);
                        for (int i = Boss.this.zone.mobs.size() - 1; i >= 0; i--) {
                            Mob mob = Boss.this.zone.mobs.get(i);
                            if (mob != null) {
                                mob.injured(Boss.this, dame, true);
                            }
                        }
                        List<Player> playersMap = Boss.this.zone.getNotBosses();
                        if (!MapService.gI().isMapOffline(Boss.this.zone.map.mapId)) {
                            // Sử dụng vòng for lặp ngược để hạn chế lỗi đồng bộ
                            for (int i = playersMap.size() - 1; i >= 0; i--) {
                                Player pl = playersMap.get(i);
                                if (!Boss.this.equals(pl)) {
                                    pl.injured(Boss.this, dame, false, false);
                                    PlayerService.gI().sendInfoHpMpMoney(pl);
                                    Service.gI().Send_Info_NV(pl);
                                }
                            }
                        }
                        prepareBom = false;
                    }
                }
            }
        } catch (Exception e) {
            if (prepareBom) {
                prepareBom = false;
            }
            setDie(this);
            die(plAtt);
        }
    }

}
