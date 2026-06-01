package bot;

import java.util.List;
import java.util.ArrayList;

import player.Player;
import network.Message;
import services.Service;
import services.MapService;
import services.PlayerService;
import services.func.ChangeMapService;
import utils.Util;
import map.Zone;
import mob.Mob;
import skill.Skill;
import utils.SkillUtil;
import services.SkillService;

public class Bot extends Player {

    public long lastTimeMove;
    public long lastTimeChat;
    public long lastTimeAttack;

    // Config for actions
    private int moveDelay = 2000;
    private int chatDelay = 5000;
    private int attackDelay = 1000;

    // Target lock
    private Mob targetMob;
    private Player targetPlayer;

    public Bot(int id, String name, short head, short body, short leg, int gender) {
        this.id = id;
        this.name = name;
        this.head = head;
        this.gender = (byte) gender;
        this.isBoss = false; // Not a boss
        this.isNewPet = false;

        // Initialize basic stats - Strong enough for most mobs
        this.nPoint.hpg = 10000000;
        this.nPoint.mpg = 10000000;
        this.nPoint.dameg = 6000;

        this.nPoint.hpMax = 10000000;
        this.nPoint.hp = 10000000;
        this.nPoint.mpMax = 100000000;
        this.nPoint.mp = 10000000;
        this.nPoint.dame = 6000;
        this.nPoint.def = 10000;
        this.nPoint.crit = 50;
        this.nPoint.tlNeDon = 60;
        this.nPoint.stamina = 10000;
        this.nPoint.maxStamina = 10000;

        // this.setClothes.setup();

        initSkills();
    }

    private void initSkills() {
        this.playerSkill.skills.clear();

        // Race specific skills
        switch (this.gender) {
            case 0: // Earth
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.DRAGON, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.KAMEJOKO, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.THAI_DUONG_HA_SAN, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.QUA_CAU_KENH_KHI, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.DICH_CHUYEN_TUC_THOI, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.KHIEN_NANG_LUONG, 7));
                break;
            case 1: // Namek
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.DEMON, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.MASENKO, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.MAKANKOSAPPO, 7)); // Laze
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.DE_TRUNG, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.TRI_THUONG, 7));
                break;
            case 2: // Saiyan
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.GALICK, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.ANTOMIC, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.BIEN_KHI, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.TU_SAT, 7));
                this.playerSkill.skills.add(SkillUtil.createSkill(Skill.HUYT_SAO, 7));
                break;
        }
    }

    // Override outfit getters to use fixed values
    private short headVal;
    private short bodyVal;
    private short legVal;

    public void setOutfit(short head, short body, short leg) {
        this.headVal = head;
        this.bodyVal = body;
        this.legVal = leg;
    }

    @Override
    public short getHead() {
        return headVal;
    }

    @Override
    public short getBody() {
        return bodyVal;
    }

    @Override
    public short getLeg() {
        return legVal;
    }

    public void joinMap(int mapId, int zoneId) {
        Zone zone = MapService.gI().getMapCanJoin(this, mapId, zoneId);
        if (zone != null) {
            int x = Util.nextInt(50, zone.map.mapWidth - 50);
            int y = zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, zone, x, y);
            Service.gI().sendFlagBag(this);
            // System.out.println("Bot " + this.name + " joined map " + mapId);
        }
    }

    @Override
    public void update() {
        super.update();

        if (this.zone == null)
            return;

        if (this.isDie()) {
            Service.gI().hsChar(this, this.nPoint.hpMax, this.nPoint.mpMax);
            this.nPoint.setFullHpMp();
            Service.gI().Send_Info_NV(this);
            Service.gI().point(this);
            // System.out.println("Bot " + this.name + " revived!");
            return;
        }

        // Auto recover
        if (this.nPoint.stamina < this.nPoint.maxStamina) {
            this.nPoint.stamina = this.nPoint.maxStamina;
        }
        if (this.nPoint.hp < this.nPoint.hpMax) {
            this.nPoint.setFullHpMp();
        }

        // Chat
        if (Util.canDoWithTime(lastTimeChat, chatDelay)) {
            chatRandomly();
            lastTimeChat = System.currentTimeMillis();
            chatDelay = Util.nextInt(5000, 15000);
        }

        useUtilitySkill();

        // Attack or Move
        if (!attack()) {
            if (Util.canDoWithTime(lastTimeMove, moveDelay)) {
                moveRandomly();
                lastTimeMove = System.currentTimeMillis();
                moveDelay = Util.nextInt(2000, 5000);
            }
        }
    }

    private boolean attack() {
        if (Util.canDoWithTime(lastTimeAttack, attackDelay)) {
            // Check current target validity
            if (this.targetPlayer != null && (this.targetPlayer.isDie() || this.targetPlayer.zone != this.zone)) {
                this.targetPlayer = null;
            }
            if (this.targetMob != null && (this.targetMob.isDie() || this.targetMob.zone != this.zone)) {
                this.targetMob = null;
            }

            // Find new target if needed
            if (this.targetPlayer == null && this.targetMob == null) {
                findTarget();
            }

            // Priority: Player (Boss) -> Mob
            if (this.targetPlayer != null) {
                Player target = this.targetPlayer;
                int dis = Util.getDistance(this, target);
                Skill skill = pickSkill(dis);
                if (skill == null) {
                    return false;
                }
                this.playerSkill.skillSelect = skill;

                int range = 0;
                if (skill.template.id == Skill.KAMEJOKO || skill.template.id == Skill.MASENKO
                        || skill.template.id == Skill.ANTOMIC) {
                    range = 300; // Ranged attack distance
                } else {
                    range = 100; // Melee attack distance
                }

                if (dis <= range) {
                    boolean success = SkillService.gI().useSkill(this, target, null, -1, null);
                    if (success) {
                        lastTimeAttack = System.currentTimeMillis();
                        attackDelay = 500;
                        return true;
                    }
                    return false;
                } else {
                    moveTo(target.location.x, target.location.y);
                    lastTimeAttack = System.currentTimeMillis();
                    attackDelay = 400; // Update position faster (0.4s)
                    return true;
                }
            } else if (this.targetMob != null) {
                Mob target = this.targetMob;
                int dis = Util.getDistance(this, target);

                Skill skill = pickSkill(dis);
                if (skill == null) {
                    return false;
                }
                this.playerSkill.skillSelect = skill;

                int range = 0;
                if (skill.template.id == Skill.KAMEJOKO || skill.template.id == Skill.MASENKO
                        || skill.template.id == Skill.ANTOMIC) {
                    range = 300; // Ranged attack distance
                } else {
                    range = 40; // Melee attack distance (must get close)
                }

                if (dis <= range) {
                    boolean success = SkillService.gI().useSkill(this, null, target, -1, null);
                    if (success) {
                        lastTimeAttack = System.currentTimeMillis();
                        attackDelay = 500;
                        return true;
                    }
                    return false;
                } else {
                    moveTo(target.location.x, target.location.y);
                    lastTimeAttack = System.currentTimeMillis();
                    attackDelay = 400; // Update position faster (0.4s)
                    return true;
                }
            } else {
                attackDelay = 1000;
            }
        }
        return false;
    }

    private void findTarget() {
        this.targetPlayer = null;
        this.targetMob = null;

        // 1. Find Boss
        if (this.zone != null && !this.zone.getBosses().isEmpty()) {
            for (Player boss : this.zone.getBosses()) {
                if (boss != null && !boss.isDie() && boss.id != this.id) {
                    this.targetPlayer = boss;
                    // System.out.println("Bot " + this.name + ": Found Boss " + boss.name);
                    return;
                }
            }
        }

        // 2. Find Mob
        if (this.zone != null && !this.zone.mobs.isEmpty()) {
            for (Mob mob : this.zone.mobs) {
                if (mob != null && !mob.isDie()) {
                    this.targetMob = mob;
                    // System.out.println("Bot " + this.name + ": Found Mob " + mob.tempId);
                    return;
                }
            }
        }
    }

    private Skill pickSkill(int distance) {
        if (this.playerSkill.skills.isEmpty()) {
            return null;
        }

        List<Skill> validSkills = new java.util.ArrayList<>();

        for (Skill skill : this.playerSkill.skills) {
            byte id = skill.template.id;
            // Check cooldown
            long timeDiff = System.currentTimeMillis() - skill.lastTimeUseThisSkill;
            if (timeDiff < skill.coolDown) {
                continue;
            }

            if (id == Skill.DRAGON || id == Skill.DEMON || id == Skill.GALICK) {
                if (distance <= 40)
                    validSkills.add(skill);
            } else if (id == Skill.KAMEJOKO || id == Skill.MASENKO || id == Skill.ANTOMIC) {
                if (distance <= 300)
                    validSkills.add(skill);
            } else if (id == Skill.DICH_CHUYEN_TUC_THOI) {
                if (distance > 50 && distance <= 300)
                    validSkills.add(skill);
            }
        }

        if (validSkills.isEmpty()) {
            // If no skill is ready/in-range, try to find *any* attack skill that is ready
            // (even if out of range, so we can chase)
            // or just return the first attack skill to force chase logic
            for (Skill skill : this.playerSkill.skills) {
                byte id = skill.template.id;
                if (id == Skill.DRAGON || id == Skill.DEMON || id == Skill.GALICK
                        || id == Skill.KAMEJOKO || id == Skill.MASENKO || id == Skill.ANTOMIC) {
                    return skill;
                }
            }
            return null;
        }

        // Randomly pick one
        return validSkills.get(Util.nextInt(0, validSkills.size() - 1));
    }

    private void useUtilitySkill() {
        if (this.playerSkill.skills.isEmpty() || this.zone == null) {
            return;
        }

        for (Skill skill : this.playerSkill.skills) {
            // Check cooldown
            long timeDiff = System.currentTimeMillis() - skill.lastTimeUseThisSkill;
            if (timeDiff < skill.coolDown) {
                continue;
            }

            switch (skill.template.id) {
                case Skill.THAI_DUONG_HA_SAN:
                    // Use if enemies are nearby
                    if (countEnemiesInRange(200) >= 1) {
                        this.playerSkill.skillSelect = skill;
                        SkillService.gI().useSkill(this, null, null, -1, null);
                    }
                    break;
                case Skill.BIEN_KHI:
                case Skill.KHIEN_NANG_LUONG:
                case Skill.HUYT_SAO:
                    // Use buff if not already active (basics) - SkillService usually handles
                    // toggle/duration checks,
                    // but we can just spam it if it's off cooldown for simplicity, or check effect.
                    // For simplicity, just use if off cooldown.
                    this.playerSkill.skillSelect = skill;
                    SkillService.gI().useSkill(this, null, null, -1, null);
                    break;
                case Skill.TAI_TAO_NANG_LUONG:
                    if (this.nPoint.hp < this.nPoint.hpMax * 0.5) {
                        this.playerSkill.skillSelect = skill;
                        SkillService.gI().useSkill(this, null, null, -1, null);
                    }
                    break;
            }
        }
    }

    private int countEnemiesInRange(int range) {
        int count = 0;
        if (this.zone != null) {
            for (Player pl : this.zone.getHumanoids()) {
                if (pl != null && !pl.equals(this) && !pl.isDie() && Util.getDistance(this, pl) <= range) {
                    count++;
                }
            }
            for (Mob mob : this.zone.mobs) {
                if (mob != null && !mob.isDie() && Util.getDistance(this, mob) <= range) {
                    count++;
                }
            }
        }
        return count;
    }

    private void moveRandomly() {
        if (this.zone == null)
            return;

        int currentX = this.location.x;
        int currentY = this.location.y;

        int dist = Util.nextInt(50, 150);
        int dir = Util.nextInt(0, 2) == 0 ? 1 : -1;
        int nextX = currentX + (dist * dir);

        if (nextX < 50)
            nextX = 50;
        if (nextX > this.zone.map.mapWidth - 50)
            nextX = this.zone.map.mapWidth - 50;

        int nextY = this.zone.map.yPhysicInTop(nextX, currentY);
        if (Util.isTrue(1, 5)) {
            nextY -= Util.nextInt(50, 100);
        }

        PlayerService.gI().playerMove(this, nextX, nextY);
    }

    public void moveTo(int x, int y) {
        int dist = x - this.location.x;
        int step = 150; // Faster speed
        if (Math.abs(dist) < step) {
            step = Math.abs(dist);
        }
        int nextX = this.location.x + (dist > 0 ? step : -step);

        // Ground checking logic
        int nextY = this.zone.map.yPhysicInTop(nextX, y);
        if (nextY == 0) { // If no ground found (flying mob?), fly to target Y
            nextY = y;
        }

        PlayerService.gI().playerMove(this, nextX, nextY);
    }

    private void chatRandomly() {
        String[] chats = { "Xin chào!", "Có ai ở đây không?", "Cày cuốc vất vả quá", "Server vui ghê",
                "Nhìn cái đ*o gì?", "Lag vl!", "Cho tui vào bang với!", "Này, cô em xinh đẹp ơi!" };
        String text = chats[Util.nextInt(0, chats.length - 1)];
        Service.gI().chat(this, text);
    }

    @Override
    public void sendMessage(Message msg) {
        // Bot does not have a session, so we ignore messages sent to it.
        // This prevents NPE and saves resources (no network I/O).
        if (msg != null) {
            msg.cleanup();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
