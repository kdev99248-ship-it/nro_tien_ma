package player;

import java.util.ArrayList;
import java.util.List;

import mob.Mob;
import services.InventoryService;
import services.ItemTimeService;
import services.MapService;
import services.PlayerService;
import services.Service;
import services.SkillService;
import services.func.ChangeMapService;
import skill.PlayerSkill;
import skill.Skill;
import utils.SkillUtil;
import utils.Util;

public class PlayerClone extends Player {
    public Player master;
    private long lastSpawnTime = 0;
    private long lastTimeDie = 0;
    private int timeSurvive = 0;

    // Attack mode
    private boolean isAttackMode = false;
    private long lastTimeAttack = 0;
    private int attackDelay = 500;
    private Mob targetMob;
    private Player targetPlayer;

    // TU_SAT flag - để clone có thể hồi sinh sau khi tự sát
    private boolean diedByTuSat = false;

    public PlayerClone(Player master) {
        super();
        this.master = master;
        this.isClone = true;
        this.id = master.id - 10_000;
        this.gender = master.gender;
        this.name = "[Clone] " + master.name;

        Skill skill = SkillUtil.getSkillbyId(master, Skill.PHAN_THAN);
        int level = (skill != null) ? skill.point : 1;
        this.timeSurvive = (80 + (level * 10)) * 1000;
        ItemTimeService.gI().sendItemTime(master, 31725, this.timeSurvive / 1000);

        this.nPoint.power = master.nPoint.power;
        this.nPoint.tiemNang = master.nPoint.tiemNang;
        this.nPoint.stamina = master.nPoint.stamina;
        this.nPoint.maxStamina = master.nPoint.maxStamina;
        this.inventory = new Inventory();
        this.inventory.itemsBody = InventoryService.gI().copyItemsBody(master);
        this.playerSkill = new PlayerSkill(this);
        this.cloneSkill();
        this.nPoint.calPoint();
        this.nPoint.setFullHpMp();
        this.lastSpawnTime = System.currentTimeMillis();
        
        if (master != null && master.isAutoMercenary) {
            this.setAttackMode(true);
        }
    }

    // Bật/tắt chế độ tấn công
    public void setAttackMode(boolean mode) {
        this.isAttackMode = mode;
        if (!mode) {
            this.targetMob = null;
            this.targetPlayer = null;
        }
    }

    public boolean isAttackMode() {
        return this.isAttackMode;
    }

    private void cloneSkill() {
        for (Skill skill : master.playerSkill.skills) {
            Skill cloneSkill = new Skill(skill);
            this.playerSkill.skills.add(cloneSkill);
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.master != null && this.typePk == consts.ConstPlayer.PK_ALL
                && this.master.typePk != consts.ConstPlayer.PK_ALL) {
            PlayerService.gI().changeAndSendTypePK(this, this.master.typePk);
        }
        if (isDie() && canRespawn()) {
            Service.getInstance().hsChar(this, nPoint.hpMax, nPoint.mpMax);
        }
        if (master != null && (this.zone == null || this.zone != master.zone)) {
            joinMapMaster();
        }
        if (Util.canDoWithTime(lastSpawnTime, timeSurvive)
                || (lastTimeDie != 0 && Util.canDoWithTime(lastTimeDie, 3_000))) {
            dispose();
            return;
        }

        // Nếu đang ở chế độ tấn công thì attack
        if (isAttackMode && !isDie()) {
            attack();
        } else if (!isAttackMode) {
            followMaster();
        }
    }

    // Logic tấn công tự động
    private void attack() {
        if (this.playerSkill.prepareTuSat) {
            if (Util.canDoWithTime(this.playerSkill.lastTimePrepareTuSat, 1500)) {
                SkillService.gI().useSkill(this, this.targetPlayer, this.targetMob, -1, null);
                this.lastTimeAttack = System.currentTimeMillis();
            }
            return;
        }

        if (!Util.canDoWithTime(lastTimeAttack, attackDelay)) {
            return;
        }

        // Nếu cách chủ quá xa, chạy về theo chủ
        if (master != null) {
            int distanceToMaster = Util.getDistance(this, master);
            // Nếu không có mục tiêu thì giới hạn 600, nếu có mục tiêu thì cho phép xa hơn
            // (2000) để đánh nốt
            int limitDist = (this.targetPlayer != null || this.targetMob != null) ? 2000 : 600;

            if (distanceToMaster > limitDist) {
                this.targetMob = null;
                this.targetPlayer = null;
                followMaster();
                lastTimeAttack = System.currentTimeMillis();
                attackDelay = 300;
                return;
            }
        }

        // Kiểm tra target hiện tại có còn hợp lệ không
        if (this.targetPlayer != null && (this.targetPlayer.isDie() || this.targetPlayer.zone != this.zone)) {
            this.targetPlayer = null;
        }
        if (this.targetMob != null && (this.targetMob.isDie() || this.targetMob.zone != this.zone)) {
            this.targetMob = null;
        }

        // Tìm target mới nếu cần (chỉ tìm khi chưa có target)
        if (this.targetPlayer == null && this.targetMob == null) {
            findTarget();
        }

        // Ưu tiên: Boss -> Mob
        if (this.targetPlayer != null) {
            attackPlayer(this.targetPlayer);
        } else if (this.targetMob != null) {
            attackMob(this.targetMob);
        } else {
            // Không có target, đứng yên chờ quái (không chạy theo chủ khi đang auto attack)
            attackDelay = 1000;
        }
    }

    private void attackPlayer(Player target) {
        int dis = Util.getDistance(this, target);
        Skill skill = pickSkill(dis);
        if (skill == null) {
            return;
        }
        this.playerSkill.skillSelect = skill;

        int range = getSkillRange(skill);

        if (dis <= range) {
            boolean success = SkillService.gI().useSkill(this, target, null, -1, null);
            if (success) {
                lastTimeAttack = System.currentTimeMillis();
                attackDelay = 500;
            }
        } else {
            moveTo(target.location.x, target.location.y);
            lastTimeAttack = System.currentTimeMillis();
            attackDelay = 400;
        }
    }

    private void attackMob(Mob target) {
        int dis = Util.getDistance(this, target);
        Skill skill = pickSkill(dis);
        if (skill == null) {
            return;
        }
        this.playerSkill.skillSelect = skill;

        int range = getSkillRange(skill);

        if (dis <= range) {
            boolean success = SkillService.gI().useSkill(this, null, target, -1, null);
            if (success) {
                lastTimeAttack = System.currentTimeMillis();
                attackDelay = 500;
            }
        } else {
            moveTo(target.location.x, target.location.y);
            lastTimeAttack = System.currentTimeMillis();
            attackDelay = 400;
        }
    }

    private int getSkillRange(Skill skill) {
        switch (skill.template.id) {
            case Skill.KAMEJOKO:
            case Skill.MASENKO:
            case Skill.ANTOMIC:
            case Skill.MAKANKOSAPPO:
                return 300;
            default:
                return 40;
        }
    }

    private void findTarget() {
        this.targetPlayer = null;
        this.targetMob = null;
        if (this.master == null || this.zone == null) {
            return;
        }

        // Ưu tiên: Boss gần sư phụ nhất
        Player closestBoss = null;
        int minDistanceBoss = 1000; // Khoảng cách tìm boss tối đa

        if (!this.zone.getBosses().isEmpty()) {
            for (Player boss : this.zone.getBosses()) {
                if (boss != null && !boss.isDie() && boss.id != this.id && !boss.isNewPet) {
                    int dis = Util.getDistance(this.master, boss);
                    if (dis < minDistanceBoss) {
                        minDistanceBoss = dis;
                        closestBoss = boss;
                    }
                }
            }
        }

        if (closestBoss != null) {
            this.targetPlayer = closestBoss;
            return;
        }

        // Tiếp theo: Quái gần sư phụ nhất
        Mob closestMob = null;
        int minDistanceMob = 1000; // Khoảng cách tìm quái tối đa

        if (!this.zone.mobs.isEmpty()) {
            for (Mob mob : this.zone.mobs) {
                if (mob != null && !mob.isDie()) {
                    int dis = Util.getDistance(this.master, mob);
                    if (dis < minDistanceMob) {
                        minDistanceMob = dis;
                        closestMob = mob;
                    }
                }
            }
        }

        if (closestMob != null) {
            this.targetMob = closestMob;
        }
    }

    private Skill pickSkill(int distance) {
        if (this.playerSkill.skills.isEmpty()) {
            return null;
        }

        List<Skill> validSkills = new ArrayList<>();

        for (Skill skill : this.playerSkill.skills) {
            byte id = skill.template.id;

            // Bỏ qua các skill không tấn công
            if (id == Skill.PHAN_THAN || id == Skill.KHIEN_NANG_LUONG || id == Skill.TRI_THUONG
                    || id == Skill.TAI_TAO_NANG_LUONG || id == Skill.HUYT_SAO || id == Skill.BIEN_KHI
                    || id == Skill.DE_TRUNG || id == Skill.TROI || id == Skill.THOI_MIEN
                    || id >= Skill.SUPER_KAME) {
                continue;
            }

            // Kiểm tra cooldown
            long timeDiff = System.currentTimeMillis() - skill.lastTimeUseThisSkill;
            if (timeDiff < skill.coolDown) {
                continue;
            }

            // Kiểm tra mana
            if (this.nPoint.mp < skill.manaUse) {
                continue;
            }

            // Skill đánh gần
            if (id == Skill.DRAGON || id == Skill.DEMON || id == Skill.GALICK
                    || id == Skill.KAIOKEN || id == Skill.LIEN_HOAN) {
                if (distance <= 50) {
                    validSkills.add(skill);
                }
            }
            // Skill đánh xa
            else if (id == Skill.KAMEJOKO || id == Skill.MASENKO || id == Skill.ANTOMIC
                    || id == Skill.MAKANKOSAPPO) {
                if (distance <= 300) {
                    validSkills.add(skill);
                }
            }
            // Skill diện rộng
            else if (id == Skill.THAI_DUONG_HA_SAN || id == Skill.QUA_CAU_KENH_KHI || id == Skill.TU_SAT) {
                if (distance <= 200) {
                    validSkills.add(skill);
                }
            }
            // DCTT
            else if (id == Skill.DICH_CHUYEN_TUC_THOI) {
                if (distance > 50 && distance <= 300) {
                    validSkills.add(skill);
                }
            }
        }

        if (validSkills.isEmpty()) {
            // Nếu không có skill phù hợp, chọn bất kỳ skill tấn công nào để đuổi theo
            for (Skill skill : this.playerSkill.skills) {
                byte id = skill.template.id;
                if (id == Skill.DRAGON || id == Skill.DEMON || id == Skill.GALICK
                        || id == Skill.KAMEJOKO || id == Skill.MASENKO || id == Skill.ANTOMIC
                        || id == Skill.KAIOKEN || id == Skill.LIEN_HOAN) {
                    return skill;
                }
            }
            return null;
        }

        return validSkills.get(Util.nextInt(0, validSkills.size() - 1));
    }

    private void moveTo(int x, int y) {
        if (this.zone == null)
            return;

        int dist = x - this.location.x;
        int step = 100;
        if (Math.abs(dist) < step) {
            step = Math.abs(dist);
        }
        int nextX = this.location.x + (dist > 0 ? step : -step);
        int nextY = this.zone.map.yPhysicInTop(nextX, y);
        if (nextY == 0) {
            nextY = y;
        }

        PlayerService.gI().playerMove(this, nextX, nextY);
    }

    @Override
    public void setDie(Player plAtt) {
        super.setDie(plAtt);

        // Nếu chết bởi TU_SAT thì không dispose và giữ attack mode, sẽ hồi sinh lại
        if (diedByTuSat) {
            diedByTuSat = false;
            return;
        }

        // Chỉ tắt attack mode khi chết thật sự (không phải TU_SAT)
        this.isAttackMode = false;
        lastTimeDie = System.currentTimeMillis();
        if (this.master != null) {
            ItemTimeService.gI().removeItemTime(this.master, 31725);
            this.master.clone = null;
        }
    }

    // Gọi method này trước khi setDie nếu Clone dùng TU_SAT
    public void setDieByTuSat() {
        this.diedByTuSat = true;
    }

    public boolean isDieByTuSat() {
        return this.diedByTuSat;
    }

    private boolean canRespawn() {
        return lastTimeDie == 0;
    }

    public void joinMapMaster() {
        this.location.x = master.location.x + Util.nextInt(-10, 10);
        this.location.y = master.location.y;
        MapService.gI().goToMap(this, master.zone);
        this.zone.load_Me_To_Another(this);
    }

    public void followMaster() {
        int mX = master.location.x;
        int mY = master.location.y;
        int disX = this.location.x - mX;
        if (Math.sqrt(Math.pow(mX - this.location.x, 2) + Math.pow(mY - this.location.y, 2)) >= 40) {
            if (disX < 0) {
                this.location.x = mX - Util.nextInt(0, 40);
            } else {
                this.location.x = mX + Util.nextInt(0, 40);
            }
            this.location.y = mY;
            PlayerService.gI().playerMove(this, this.location.x, this.location.y);
        }
    }

    @Override
    public short getHead() {
        return master != null ? master.getHead() : this.head;
    }

    @Override
    public short getBody() {
        return master != null ? master.getBody() : super.getBody();
    }

    @Override
    public short getLeg() {
        return master != null ? master.getLeg() : super.getLeg();
    }

    @Override
    public byte getAura() {
        return master != null ? master.getAura() : -1;
    }

    @Override
    public short getMount() {
        return master != null ? master.getMount() : -1;
    }

    @Override
    public void dispose() {
        ChangeMapService.gI().exitMap(this);
        if (this.master != null) {
            ItemTimeService.gI().removeItemTime(this.master, 31725);
            this.master.clone = null;
        }
        this.master = null;
    }
}
