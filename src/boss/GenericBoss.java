package boss;

import consts.ConstTaskBadges;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.ItemService;
import services.Service;
import services.TaskService;
import task.Badges.BadgesTaskService;
import utils.Util;
import utils.Logger;

import java.util.List;
import java.util.Map;

/**
 * Boss chung đọc cấu hình hoàn toàn từ BossConfig (database).
 * Thay thế hầu hết các class boss đơn giản trong boss_manifest.
 */
public class GenericBoss extends Boss {

    protected long st;
    protected int timeLeaveMap;
    
    // State cho các logic đặc biệt
    private long lastTimeHapThu;
    private int timeHapThu;
    private boolean isSummoned;
    private long lastTimeMcChat;
    private int indexMcChat;
    private long lastTimeMcMove;

    /**
     * Constructor cho boss KHÔNG có BossType (thêm vào BossManager chính).
     */
    public GenericBoss(BossConfig config, BossData... data) throws Exception {
        super(config.getBossId(), config.isNotifyDisabled(), config.isZone01SpawnDisabled(), data);
        this.config = config;
    }

    /**
     * Constructor cho boss CÓ BossType (thêm vào manager riêng như BrolyManager, YardartManager...).
     */
    public GenericBoss(BossType bossType, BossConfig config, BossData... data) throws Exception {
        super(bossType, config.getBossId(), config.isNotifyDisabled(), config.isZone01SpawnDisabled(), data);
        this.config = config;
    }

    @Override
    public void update() {
        Map<String, Object> abilities = config.parseSpecialAbilities();
        boolean isGroupOrder = false;
        if (abilities != null && abilities.containsKey("groupOrder")) {
            isGroupOrder = (boolean) abilities.get("groupOrder");
        } else {
            int lvl = Math.max(0, this.currentLevel);
            if (this.bossAppearTogether != null && this.bossAppearTogether.length > lvl && this.bossAppearTogether[lvl] != null && this.bossAppearTogether[lvl].length > 1) {
                isGroupOrder = true; // Mặc định những con sinh ra theo bầy sẽ đánh lần lượt
            }
        }
        
        if (isGroupOrder) {
            checkGroupOrder();
        }
        
        handleMcCommentary(abilities);
        handlePetrifyPlayers(abilities);
        super.update();
    }

    private void checkGroupOrder() {
        int lvl = Math.max(0, this.currentLevel);
        if (this.bossAppearTogether == null || this.bossAppearTogether.length <= lvl || this.bossAppearTogether[lvl] == null) {
            return;
        }
        
        Boss activeBoss = null;
        for (Boss b : this.bossAppearTogether[lvl]) {
            if (b != null && !b.isDie()) {
                activeBoss = b;
                break;
            }
        }

        if (activeBoss != null) {
            if (activeBoss.equals(this)) {
                if (this.typePk != 5) {
                    services.PlayerService.gI().changeAndSendTypePK(this, 5);
                }
                if (this.bossStatus == BossStatus.AFK || this.bossStatus == BossStatus.REST) {
                    this.changeStatus(BossStatus.ACTIVE);
                }
            } else {
                if (this.typePk != 0) {
                    services.PlayerService.gI().changeAndSendTypePK(this, 0);
                }
                // Nếu không phải lượt, ép trạng thái AFK để không tấn công
                if (this.bossStatus == BossStatus.ACTIVE) {
                    this.changeStatus(BossStatus.AFK);
                }
            }
        }
    }

    // ============================================
    // INJURED - logic chung đọc từ config
    // ============================================
    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            // Né đòn
            int dodge = (config.getDodgeRate() != null) ? config.getDodgeRate() : this.nPoint.tlNeDon;
            if (!piercing && Util.isTrue(dodge, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            // Pierce reverse (kiểu Cooler: piercing thì chia damage)
            if (config.isPierceReverse() && piercing) {
                damage /= 100;
            }

            // Giảm damage flat (kiểu BlackGoku/Cumber)
            if (config.getDamageFlatReduction() != null) {
                damage = damage - Util.nextInt((int) (long) config.getDamageFlatReduction());
            }

            Map<String, Object> abilities = config.parseSpecialAbilities();
            if (abilities != null) {
                // Hấp thụ chưởng (Android 19/20 logic)
                if (abilities.containsKey("absorbSkillIds")) {
                    @SuppressWarnings("unchecked")
                    List<Number> absorbIds = (List<Number>) abilities.get("absorbSkillIds");
                    if (plAtt != null && absorbIds.contains((int) plAtt.playerSkill.skillSelect.template.id)) {
                        double rate = abilities.containsKey("absorbRate") ? ((Number) abilities.get("absorbRate")).doubleValue() : 1.0;
                        long hpHoi = (long) (damage * rate);
                        services.PlayerService.gI().hoiPhuc(this, hpHoi, 0);
                        if (Util.isTrue(1, 10)) {
                            this.chat("Hấp thụ.. các ngươi nghĩ sao vậy?");
                        }
                        return 0;
                    }
                }

                // DeTuBoss logic: chỉ đệ tử có thể gây dame
                if (abilities.containsKey("petOnlyDamage") && (boolean) abilities.get("petOnlyDamage")) {
                    if (!plAtt.isPet) {
                        Service.gI().sendThongBaoOK(plAtt, "Chỉ đệ tử mới có thể gây dame, sư phụ có nịt!!!");
                        return 0;
                    }
                }

                // Broly logic: tăng chỉ số khi bị đánh
                if (abilities.containsKey("increaseStatsOnDamage") && Util.isTrue(1, 40)) {
                    this.tangChiSoBroly();
                }

                // Bất tử khi đồng bọn còn sống (Android 13, 14, 15 logic)
                if (abilities.containsKey("undyingWhileOthersAlive") && damage >= this.nPoint.hp) {
                    @SuppressWarnings("unchecked")
                    List<Number> checkBossIds = (List<Number>) abilities.get("undyingWhileOthersAlive");
                    boolean canDie = true;

                    // Kiểm tra đồng đội trong cùng group xuất hiện
                    if (this.parentBoss != null && this.parentBoss.bossAppearTogether != null
                            && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
                        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
                            if (boss.id != this.id && checkBossIds.contains(((Number) boss.id).intValue()) && !boss.isDie()) {
                                canDie = false;
                                break;
                            }
                        }
                    }

                    // Kiểm tra cả boss cha nếu có trong danh sách
                    if (canDie && this.parentBoss != null && checkBossIds.contains(((Number) this.parentBoss.id).intValue()) && !this.parentBoss.isDie()) {
                        canDie = false;
                    }

                    if (!canDie) {
                        this.chat("Ngươi không thể giết ta khi đồng bọn của ta còn sống!");
                        return 0;
                    }
                }

                // Siêu Bọ Hung logic: triệu hồi đệ tử khi thấp máu
                if (abilities.containsKey("summonOnHpThreshold") && !isSummoned) {
                    double threshold = ((Number) abilities.get("summonOnHpThreshold")).doubleValue();
                    if (this.nPoint.hp <= (long)(this.nPoint.hpMax * threshold)) {
                        handleSieuBoHungSummon(abilities);
                        return 0; // Tránh chết ngay lập tức khi đang triệu hồi
                    }
                }
            }

            // Cộng SMTN khi bị đánh (TaoPaiPai, Training bosses logic)
            if (abilities != null && abilities.containsKey("smTnOnHit")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> smTnCfg = (Map<String, Object>) abilities.get("smTnOnHit");
                long min = smTnCfg.containsKey("min") ? ((Number) smTnCfg.get("min")).longValue() : 1000;
                long max = smTnCfg.containsKey("max") ? ((Number) smTnCfg.get("max")).longValue() : 5000;
                long cap = smTnCfg.containsKey("cap") ? ((Number) smTnCfg.get("cap")).longValue() : 1000000;
                long threshold = smTnCfg.containsKey("powerThreshold") ? ((Number) smTnCfg.get("powerThreshold")).longValue() : 120_000_000_000L;
                
                long tnSm = damage * Util.nextInt((int)min, (int)max) / 100; // Giả sử tính theo % damage hoặc số nhân
                if (tnSm > cap) tnSm = cap - Util.nextInt((int)cap/10);
                if (plAtt.nPoint.power > threshold) tnSm = Util.nextInt(1000);
                
                services.Service.gI().addSMTN(plAtt, (byte) 2, tnSm, true);
            }

            // Mabu point on hit
            if (abilities != null && abilities.containsKey("mabuPointOnHit")) {
                if (plAtt.isPl() && Util.isTrue(1, 10)) {
                    plAtt.fightMabu.changePercentPoint((byte) 1);
                }
            }

            // Giới hạn sát thương tối đa (Cooler, TaoPaiPai logic)
            if (abilities != null && abilities.containsKey("maxDamage")) {
                long maxDamage = ((Number) abilities.get("maxDamage")).longValue();
                if (damage > maxDamage) {
                    damage = maxDamage;
                }
            }

            damage = this.nPoint.subDameInjureWithDeff(damage);

            // Chia damage (kiểu XenBoHung /2, SieuBoHung /3)
            if (config.getDamageDivisor() != null && config.getDamageDivisor() > 0) {
                damage /= config.getDamageDivisor();
            }

            // Shield
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

            // Max damage per hit cap
            if (config.getMaxDamagePerHit() != null && damage > config.getMaxDamagePerHit()) {
                damage = config.getMaxDamagePerHit();
            }

            if (damage < 0) damage = 0;

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

    // ============================================
    // JOIN MAP
    // ============================================
    @Override
    public void joinMap() {
        Map<String, Object> abilities = config.parseSpecialAbilities();
        if (abilities != null) {
            // Broly logic: random stats khi xuất hiện
            if (abilities.containsKey("randomStats") && (boolean) abilities.get("randomStats")) {
                long hpMin = abilities.containsKey("hpMin") ? ((Number) abilities.get("hpMin")).longValue() : 1000;
                long hpMax = abilities.containsKey("hpMax") ? ((Number) abilities.get("hpMax")).longValue() : 100000;
                this.nPoint.hpMax = Util.nextInt((int) hpMin, (int) hpMax);
                this.nPoint.hp = this.nPoint.hpMax;
                this.nPoint.dame = this.nPoint.hpMax / 100;
                this.nPoint.crit = Util.nextInt(50);
            }
        }

        if (config.isAppendRandomName()) {
            this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        }

        if (abilities != null && abilities.containsKey("randomZoneRange")) {
            @SuppressWarnings("unchecked")
            List<Number> range = (List<Number>) abilities.get("randomZoneRange");
            int zMin = range.get(0).intValue();
            int zMax = range.size() > 1 ? range.get(1).intValue() : zMin;
            this.joinMapCustomRange(zMin, zMax);
        } else {
            super.joinMap();
        }

        st = System.currentTimeMillis();
        if (config.getAutoLeaveRandomMin() != null && config.getAutoLeaveRandomMax() != null) {
            timeLeaveMap = Util.nextInt((int) (long) config.getAutoLeaveRandomMin(),
                    (int) (long) config.getAutoLeaveRandomMax());
        }
    }

    private void joinMapCustomRange(int min, int max) {
        if (this.zone == null) {
            this.zone = getMapJoin();
        }
        if (this.zone != null) {
            try {
                int zoneid = Util.nextInt(min, max);
                if (zoneid < this.zone.map.zones.size()) {
                    this.zone = this.zone.map.zones.get(zoneid);
                }
                services.func.ChangeMapService.gI().changeMap(this, this.zone, Util.nextInt(100, 500), 
                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24));
                this.changeStatus(BossStatus.CHAT_S);
            } catch (Exception e) {
                this.changeStatus(BossStatus.REST);
            }
        }
    }

    // ============================================
    // AUTO LEAVE MAP
    // ============================================
    @Override
    public void autoLeaveMap() {
        if (config.getAutoLeaveTimeout() == null && config.getAutoLeaveRandomMin() == null) {
            return; // Không có auto leave
        }

        long timeout;
        if (config.getAutoLeaveRandomMin() != null) {
            timeout = timeLeaveMap;
        } else {
            timeout = config.getAutoLeaveTimeout();
        }

        if (Util.canDoWithTime(st, timeout)) {
            this.leaveMapNew();
        }

        if (config.isAutoLeaveResetOnPlayer() && this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
            if (config.getAutoLeaveRandomMin() != null && config.getAutoLeaveRandomMax() != null) {
                timeLeaveMap = Util.nextInt((int) (long) config.getAutoLeaveRandomMin(),
                        (int) (long) config.getAutoLeaveRandomMax());
            }
        }
    }

    // ============================================
    // DONE CHAT S
    // ============================================
    @Override
    public void doneChatS() {
        if (config.isDoneChatSToAfk()) {
            this.changeStatus(BossStatus.AFK);
        } else {
            super.doneChatS();
        }
    }

    // ============================================
    // NOTIFY JOIN MAP - skip ở level cụ thể
    // ============================================
    @Override
    protected void notifyJoinMap() {
        if (config.getSkipNotifyAtLevel() != null && this.currentLevel == config.getSkipNotifyAtLevel()) {
            return;
        }
        super.notifyJoinMap();
    }

    // ============================================
    // MOVE TO - skip ở level cụ thể
    // ============================================
    @Override
    public void moveTo(int x, int y) {
        if (config.getSkipMoveAtLevel() != null && this.currentLevel == config.getSkipMoveAtLevel()) {
            return;
        }
        super.moveTo(x, y);
    }

    // ============================================
    // DONE CHAT E - kiểm tra boss parent
    // ============================================
    @Override
    public void doneChatE() {
        Map<String, Object> abilities = config.parseSpecialAbilities();
        if (abilities != null && abilities.containsKey("checkParentActive")) {
            // Kiểu SO1: khi tất cả boss đi kèm đã chết, cho parent boss active
            if (this.parentBoss != null && this.parentBoss.bossAppearTogether != null
                    && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
                String checkBossIdStr = String.valueOf(abilities.get("checkParentActiveBossId"));
                if (checkBossIdStr != null && !checkBossIdStr.equals("null")) {
                    int checkBossId = ((Number) abilities.get("checkParentActiveBossId")).intValue();
                    for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
                        if (boss.id == checkBossId && !boss.isDie()) {
                            return;
                        }
                    }
                }
                this.parentBoss.changeStatus(BossStatus.ACTIVE);
            }
        }
    }

    // Logic reward đã được di chuyển lên class cha Boss để dùng chung cho tất cả boss.
    private void bodyChangePlayerInMap() {
        if (this.zone != null) {
            List<Player> players = this.zone.getPlayers();
            for (int i = players.size() - 1; i >= 0; i--) {
                Player pl = players.get(i);
                if (pl != null && pl.isPl() && Util.isTrue(5, 10) && pl.effectSkill != null
                        && !pl.effectSkill.isBodyChangeTechnique) {
                    services.EffectSkillService.gI().setIsBodyChangeTechnique(pl);
                }
            }
        }
    }

    private void tangChiSoBroly() {
        long currentHpMax = this.nPoint.hpMax;
        int rand = Util.nextInt(80, 100);
        long newHpMax = currentHpMax + currentHpMax / rand;
        if (newHpMax > 16_070_777) newHpMax = 16_070_777;
        this.nPoint.hpMax = newHpMax;
        this.nPoint.dame = newHpMax / 10;
        Service.gI().point(this);
    }

    // ============================================
    // ATTACK - có thể dùng special abilities
    // ============================================
    @Override
    public void attack() {
        Map<String, Object> abilities = config.parseSpecialAbilities();

        // Hấp thụ player (kiểu XenBoHung)
        if (abilities != null && abilities.containsKey("absorbPlayer")) {
            handleXenAbsorb(abilities);
        }

        super.attack();
    }

    /**
     * Hấp thụ player (logic XenBoHung).
     */
    private void handleXenAbsorb(Map<String, Object> abilities) {
        if (!Util.canDoWithTime(this.lastTimeHapThu, this.timeHapThu)) {
            return;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cfg = (Map<String, Object>) abilities.get("absorbPlayer");
        int chance = cfg.containsKey("chance") ? ((Number) cfg.get("chance")).intValue() : 1;
        
        if (!Util.isTrue(chance, 100)) {
            return;
        }

        Player pl = this.zone.getRandomPlayerInMap();
        if (pl == null || pl.isDie() || pl.isBoss) {
            return;
        }
        
        services.func.ChangeMapService.gI().changeMapYardrat(this, this.zone, pl.location.x, pl.location.y);
        
        int dameBonus = cfg.containsKey("dameBonusRate") ? ((Number) cfg.get("dameBonusRate")).intValue() : 5;
        int hpBonus = cfg.containsKey("hpBonusRate") ? ((Number) cfg.get("hpBonusRate")).intValue() : 2;
        
        this.nPoint.dameg += (pl.nPoint.dame * dameBonus / 100);
        this.nPoint.hpg += (pl.nPoint.hp * hpBonus / 100);
        this.nPoint.critg++;
        this.nPoint.calPoint();
        
        services.PlayerService.gI().hoiPhuc(this, pl.nPoint.hp, 0);
        pl.injured(null, pl.nPoint.hpMax, true, false);
        
        Service.gI().sendThongBao(pl, "Bạn vừa bị " + this.name + " hấp thu!");
        this.chat(2, "Ui cha cha, kinh dị quá. " + pl.name + " vừa bị tên " + this.name + " nuốt chửng kìa!!!");
        this.chat("Haha, ngọt lắm đấy " + pl.name + "..");
        
        this.lastTimeHapThu = System.currentTimeMillis();
        int minTime = cfg.containsKey("minTime") ? ((Number) cfg.get("minTime")).intValue() : 10000;
        int maxTime = cfg.containsKey("maxTime") ? ((Number) cfg.get("maxTime")).intValue() : 20000;
        this.timeHapThu = Util.nextInt(minTime, maxTime);
    }

    /**
     * Triệu hồi đệ tử (logic SieuBoHung).
     */
    private void handleSieuBoHungSummon(Map<String, Object> abilities) {
        this.isSummoned = true;
        new Thread(() -> {
            try {
                this.changeStatus(BossStatus.AFK);
                this.changeToTypeNonPK();
                services.PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
                
                String msg1 = abilities.containsKey("summonMsg1") ? (String) abilities.get("summonMsg1") : "Hãy đấu với các con của ta!";
                this.chat(msg1);
                EMTI.Functions.sleep(2000);
                
                String msg2 = abilities.containsKey("summonMsg2") ? (String) abilities.get("summonMsg2") : "Cứ chưởng tiếp đi haha";
                this.chat(msg2);
                EMTI.Functions.sleep(2000);
                
                int lvl = Math.max(0, this.currentLevel);
                if (this.bossAppearTogether != null && this.bossAppearTogether.length > lvl && this.bossAppearTogether[lvl] != null) {
                    for (Boss boss : this.bossAppearTogether[lvl]) {
                        if (boss != null && boss != this) {
                            boss.changeStatus(BossStatus.RESPAWN);
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("Lỗi summon boss: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Bình luận viên NPC (logic SieuBoHung).
     */
    private void handleMcCommentary(Map<String, Object> abilities) {
        if (abilities == null || !abilities.containsKey("mcCommentary") || this.zone == null) {
            return;
        }
        
        Player mc = this.zone.getNpc();
        if (mc == null) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> cfg = (Map<String, Object>) abilities.get("mcCommentary");
        
        // Chat logic
        int chatInterval = cfg.containsKey("chatInterval") ? ((Number) cfg.get("chatInterval")).intValue() : 3000;
        if (Util.canDoWithTime(this.lastTimeMcChat, chatInterval)) {
            @SuppressWarnings("unchecked")
            List<String> texts = (List<String>) cfg.get("texts");
            if (texts != null && !texts.isEmpty()) {
                if (indexMcChat >= texts.size()) indexMcChat = 0;
                Service.gI().chat(mc, texts.get(indexMcChat));
                indexMcChat++;
                this.lastTimeMcChat = System.currentTimeMillis();
            }
        }

        // Move logic
        int moveInterval = cfg.containsKey("moveInterval") ? ((Number) cfg.get("moveInterval")).intValue() : 15000;
        if (Util.canDoWithTime(this.lastTimeMcMove, moveInterval)) {
            if (Util.isTrue(2, 3)) {
                int x = mc.location.x + Util.nextInt(-100, 100);
                int y = this.zone.map.yPhysicInTop(x, mc.location.y);
                services.PlayerService.gI().playerMove(mc, x, y);
            }
            this.lastTimeMcMove = System.currentTimeMillis();
        }
    }

    @Override
    public void rest() {
        super.rest();
        Map<String, Object> abilities = config.parseSpecialAbilities();
        if (abilities != null && abilities.containsKey("restProgressMabu")) {
            long currentTimeMillis = System.currentTimeMillis();
            long elapsedTime = currentTimeMillis - lastTimeRest;
            int percent = (int) (elapsedTime * 100 / ((secondsRest - 3) * 1000));
            if (percent <= 100 && this.zoneFinal != null) {
                Service.gI().SendMabu(this.zoneFinal, percent);
            }
        }
    }

    /**
     * Hóa đá và Socola players trong map (logic Mabu).
     */
    private void handlePetrifyPlayers(Map<String, Object> abilities) {
        if (abilities == null || !abilities.containsKey("petrifyPlayers") || this.zone == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> cfg = (Map<String, Object>) abilities.get("petrifyPlayers");
        int interval = cfg.containsKey("interval") ? ((Number) cfg.get("interval")).intValue() : 30000;

        if (Util.canDoWithTime(this.lastTimeHapThu, interval)) { // reuse lastTimeHapThu for petrify timer
            List<Player> players = this.zone.getNotBosses();
            for (int i = players.size() - 1; i >= 0; i--) {
                Player pl = players.get(i);
                if (pl == null || pl.isDie()) continue;

                if (Util.isTrue(1, 10)) {
                    EffectSkillService.gI().setIsStone(pl, 22000);
                } else if (Util.isTrue(1, 5)) {
                    this.chat("Úm ba la xì bùa");
                    EffectSkillService.gI().setSocola(pl, System.currentTimeMillis(), 30000);
                    services.ItemTimeService.gI().sendItemTime(pl, 4133, 30);
                }
            }
            this.lastTimeHapThu = System.currentTimeMillis();
        }
    }

    @Override
    protected void resetBase() {
        super.resetBase();
        this.isSummoned = false;
    }
}
