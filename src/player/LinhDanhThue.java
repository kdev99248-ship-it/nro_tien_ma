package player;

import java.util.ArrayList;
import java.util.List;

import item.Item;
import mob.Mob;
import boss.Boss;
import boss.BossStatus;
import network.io.Message;

import player.mercenary.MercenaryManager;
import player.mercenary.MercenaryTemplate;
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

public class LinhDanhThue extends Player {
    public Player master;
    private long lastSpawnTime = 0;
    private long lastTimeDie = 0;
    private int timeSurvive = 0; // milliseconds
    private long expireTime = 0; // absolute timestamp when mercenary expires
    private int mercenaryType = 0; // type of mercenary for save/load (template ID)
    private boolean canAttackBoss = false; // Khả năng đánh boss từ template
    private MercenaryTemplate template; // Template từ DB
    private int durationOption = 0; // 0: 30 phút, 1: 1 giờ, 2: 5 giờ - dùng để xác định skill

    // Attack mode
    private boolean isAttackMode = false;
    private long lastTimeAttack = 0;
    private int attackDelay = 500;
    private Mob targetMob;
    private Player targetPlayer;

    /**
     * Constructor cũ - giữ lại để tương thích ngược
     * Sử dụng chỉ số từ master player
     */
    public LinhDanhThue(Player master, int durationSeconds, String name, int head, int body, int leg, int gender) {
        super();
        this.master = master;
        this.isClone = true; // Use isClone flag to be treated similar to clone/pet
        this.id = -100000 - master.id - Util.nextInt(0, 1000); // Allow multiple mercenaries
        this.gender = (byte) gender;
        this.name = name;
        this.head = (short) head;
        this.timeSurvive = durationSeconds * 1000;
        this.expireTime = System.currentTimeMillis() + (durationSeconds * 1000L);

        // Initialize inventory FIRST before any access to itemsBody
        this.inventory = new Inventory();
        // Initialize itemsBody with empty items to avoid IndexOutOfBoundsException
        // SetClothes.setup() needs at least 6 items in itemsBody, but typical players have up to 13 slots
        for (int i = 0; i < 13; i++) {
            this.inventory.itemsBody.add(new Item());
        }

        // Power scaling based on master
        this.nPoint.power = master.nPoint.power;
        this.nPoint.tiemNang = master.nPoint.tiemNang;
        this.nPoint.stamina = master.nPoint.stamina;
        this.nPoint.maxStamina = master.nPoint.maxStamina;

        this.nPoint.hpg = master.nPoint.hpg;
        this.nPoint.mpg = master.nPoint.mpg;
        this.nPoint.dameg = master.nPoint.dameg;

        // Stats
        this.nPoint.hpMax = master.nPoint.hpMax;
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.mpMax = master.nPoint.mpMax;
        this.nPoint.mp = this.nPoint.mpMax;
        this.nPoint.dame = master.nPoint.dame;
        this.nPoint.def = master.nPoint.def;
        this.nPoint.crit = master.nPoint.crit;
        this.nPoint.tlNeDon = master.nPoint.tlNeDon;

        this.playerSkill = new PlayerSkill(this);

        // Add basic skills
        addSkills();

        this.nPoint.calPoint();
        this.nPoint.setFullHpMp();
        this.lastSpawnTime = System.currentTimeMillis();

        // Send icon time to player
        sendMercenaryIcon();
        
        if (master != null && master.isAutoMercenary) {
            this.setAttackMode(true);
        }
    }

    /**
     * Constructor MỚI - Sử dụng MercenaryTemplate từ database
     * Chỉ số lính được lấy từ template thay vì copy từ master
     * 
     * @param master         Player chủ thuê lính
     * @param template       Template từ database
     * @param durationOption 0: 30 phút, 1: 1 giờ, 2: 5 giờ
     */
    public LinhDanhThue(Player master, MercenaryTemplate template, int durationOption) {
        super();
        this.master = master;
        this.template = template;
        this.mercenaryType = template.getId();
        this.canAttackBoss = template.isCanAttackBoss();
        this.isClone = true;
        this.id = -100000 - master.id - Util.nextInt(0, 1000);
        this.gender = (byte) template.getGender();
        this.name = template.getName();
        this.head = (short) template.getHead();
        this.durationOption = durationOption; // Lưu để xác định skill

        int durationSeconds = MercenaryTemplate.getDurationSeconds(durationOption);
        this.timeSurvive = durationSeconds * 1000;
        this.expireTime = System.currentTimeMillis() + (durationSeconds * 1000L);

        // Initialize inventory
        this.inventory = new Inventory();
        for (int i = 0; i < 6; i++) {
            this.inventory.itemsBody.add(new Item());
        }

        // QUAN TRỌNG: Sử dụng chỉ số từ template thay vì master
        this.nPoint.power = master.nPoint.power; // Sức mạnh vẫn theo master để hiển thị
        this.nPoint.tiemNang = 0;
        this.nPoint.stamina = 1000;
        this.nPoint.maxStamina = 1000;

        // Chỉ số từ template
        this.nPoint.hpMax = template.getHp();
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.mpMax = template.getMp();
        this.nPoint.mp = this.nPoint.mpMax;
        this.nPoint.dame = template.getDame();
        this.nPoint.def = template.getDef();
        this.nPoint.crit = template.getCrit();
        this.nPoint.tlNeDon = 5; // Default

        // Set visual
        this.headVal = (short) template.getHead();
        this.bodyVal = (short) template.getBody();
        this.legVal = (short) template.getLeg();

        this.playerSkill = new PlayerSkill(this);
        addSkills();

        this.nPoint.calPoint();
        this.nPoint.setFullHpMp();
        this.lastSpawnTime = System.currentTimeMillis();

        sendMercenaryIcon();
        
        if (master != null && master.isAutoMercenary) {
            this.setAttackMode(true);
        }
    }

    // Constructor for restoring from database
    public LinhDanhThue(Player master, long expireTime, int mercenaryType, String name, int head, int body, int leg,
            int gender) {
        super();
        this.master = master;
        this.isClone = true;
        this.id = -100000 - master.id - Util.nextInt(0, 1000);
        this.gender = (byte) gender;
        this.name = name;
        this.head = (short) head;
        this.expireTime = expireTime;
        this.mercenaryType = mercenaryType;
        this.timeSurvive = (int) (expireTime - System.currentTimeMillis());

        // Thử load template từ DB bằng mercenaryType (là template ID)
        this.template = MercenaryManager.gI().getTemplateById(mercenaryType);
        if (this.template != null) {
            this.canAttackBoss = this.template.isCanAttackBoss();
        }

        // Initialize inventory FIRST
        this.inventory = new Inventory();
        for (int i = 0; i < 6; i++) {
            this.inventory.itemsBody.add(new Item());
        }

        // Nếu có template, sử dụng chỉ số từ template
        if (this.template != null) {
            this.nPoint.power = master.nPoint.power; // Sức mạnh vẫn theo master để hiển thị
            this.nPoint.tiemNang = 0;
            this.nPoint.stamina = 1000;
            this.nPoint.maxStamina = 1000;

            this.nPoint.hpMax = this.template.getHp();
            this.nPoint.hp = this.nPoint.hpMax;
            this.nPoint.mpMax = this.template.getMp();
            this.nPoint.mp = this.nPoint.mpMax;
            this.nPoint.dame = this.template.getDame();
            this.nPoint.def = this.template.getDef();
            this.nPoint.crit = this.template.getCrit();
            this.nPoint.tlNeDon = 5;
        } else {
            // Fallback: sử dụng chỉ số từ master (tương thích ngược)
            this.nPoint.power = master.nPoint.power;
            this.nPoint.tiemNang = master.nPoint.tiemNang;
            this.nPoint.stamina = master.nPoint.stamina;
            this.nPoint.maxStamina = master.nPoint.maxStamina;

            this.nPoint.hpg = master.nPoint.hpg;
            this.nPoint.mpg = master.nPoint.mpg;
            this.nPoint.dameg = master.nPoint.dameg;

            this.nPoint.hpMax = master.nPoint.hpMax;
            this.nPoint.hp = this.nPoint.hpMax;
            this.nPoint.mpMax = master.nPoint.mpMax;
            this.nPoint.mp = this.nPoint.mpMax;
            this.nPoint.dame = master.nPoint.dame;
            this.nPoint.def = master.nPoint.def;
            this.nPoint.crit = master.nPoint.crit;
            this.nPoint.tlNeDon = master.nPoint.tlNeDon;
        }

        this.playerSkill = new PlayerSkill(this);
        addSkills();
        this.nPoint.calPoint();
        this.nPoint.setFullHpMp();
        this.lastSpawnTime = System.currentTimeMillis();

        // Visual - store clothes for override methods
        this.headVal = (short) head;
        this.bodyVal = (short) body;
        this.legVal = (short) leg;

        // Send icon time to player (for restored mercenary)
        sendMercenaryIcon();
        
        if (master != null && master.isAutoMercenary) {
            this.setAttackMode(true);
        }
    }

    // Base icon ID for mercenary time display
    // Icon mapping by gender (race):
    // - Trái Đất (gender 0) → 32456
    // - Xayda (gender 1) → 32457
    // - Namec (gender 2) → 32458
    private static final int MERCENARY_BASE_ICON_ID = 32456;

    // Get unique icon ID for this mercenary based on gender (race)
    private int getMercenaryIconId() {
        return MERCENARY_BASE_ICON_ID + this.gender;
    }

    private void sendMercenaryIcon() {
        if (master != null) {
            long timeLeft = (expireTime - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                ItemTimeService.gI().sendItemTime(master, getMercenaryIconId(), (int) timeLeft);
            }
        }
    }

    private void removeMercenaryIcon() {
        if (master != null) {
            ItemTimeService.gI().removeItemTime(master, getMercenaryIconId());
        }
    }

    private void setDefaultClothes(int head, int body, int leg) {
        // This is a bit hacky, usually clothes are in itemsBody
        // But for Bot/Clone often getHead/Body/Leg are overridden or set directly.
        // Since we extend Player, we should override getHead, getBody, getLeg or ensure
        // itemsBody has items.
        // Let's store them in fields and override getters like Bot.java does, or just
        // rely on overriding.
    }

    // Quick stash for visual parts
    private short headVal;
    private short bodyVal;
    private short legVal;

    public void setOutfit(int head, int body, int leg) {
        this.headVal = (short) head;
        this.bodyVal = (short) body;
        this.legVal = (short) leg;
    }

    @Override
    public short getHead() {
        return headVal > 0 ? headVal : super.getHead();
    }

    @Override
    public short getBody() {
        return bodyVal > 0 ? bodyVal : super.getBody();
    }

    @Override
    public short getLeg() {
        return legVal > 0 ? legVal : super.getLeg();
    }

    private void addSkills() {
        // Thêm skill dựa trên durationOption
        // 0: 30 phút - Skill cơ bản (tấn công cấp 7, ngẫu nhiên từ 3 hành tinh)
        // 1: 1 giờ - Thêm skill đặc biệt (choáng, hồi máu, biến socola, hóa khỉ, thôi
        // miên) - cooldown x1.5
        // 2: 5 giờ - Tất cả skill - cooldown bình thường

        this.playerSkill.skills.clear();

        // ========== SKILL CƠ BẢN (TẤT CẢ CÁC MỐC THỜI GIAN) ==========
        // Skill tấn công cơ bản từ 3 hành tinh

        // Trái Đất
        addSkillWithCooldownMultiplier(Skill.DRAGON, 7); // Rồng xăm
        addSkillWithCooldownMultiplier(Skill.KAMEJOKO, 7); // Sóng kame
        addSkillWithCooldownMultiplier(Skill.KAIOKEN, 7); // Kaioken (buff)

        // Xayda
        addSkillWithCooldownMultiplier(Skill.GALICK, 7); // Galick
        addSkillWithCooldownMultiplier(Skill.ANTOMIC, 7); // Antomic

        // Namec
        addSkillWithCooldownMultiplier(Skill.MASENKO, 7); // Masenko
        addSkillWithCooldownMultiplier(Skill.MAKANKOSAPPO, 7);// Makankosappo

        // Skill hồi năng lượng (để không hết ki)
        addSkillWithCooldownMultiplier(Skill.TAI_TAO_NANG_LUONG, 7);

        // ========== SKILL ĐẶC BIỆT (CHỈ THÊM CHO 1H VÀ 5H) ==========
        if (durationOption >= 1) {
            // Thái Dương Hạ Sơn (choáng)
            addSkillWithCooldownMultiplier(Skill.THAI_DUONG_HA_SAN, 7);

            // Trị Thương (hồi máu)
            addSkillWithCooldownMultiplier(Skill.TRI_THUONG, 7);

            // Socola (biến đối thủ thành kẹo)
            addSkillWithCooldownMultiplier(Skill.SOCOLA, 7);

            // Biến Khí (hóa khỉ đột)
            addSkillWithCooldownMultiplier(Skill.BIEN_KHI, 7);

            // Thôi miên
            addSkillWithCooldownMultiplier(Skill.THOI_MIEN, 7);

            // Trói
            addSkillWithCooldownMultiplier(Skill.TROI, 7);

            // Liên hoàn
            addSkillWithCooldownMultiplier(Skill.LIEN_HOAN, 7);
        }
    }

    /**
     * Thêm skill với cooldown multiplier dựa trên durationOption
     * - 30p (option 0): cooldown x1.0 (bình thường)
     * - 1h (option 1): cooldown x1.5 cho skill đặc biệt
     * - 5h (option 2): cooldown x1.0 (bình thường)
     */
    private void addSkillWithCooldownMultiplier(byte skillId, int level) {
        Skill skill = SkillUtil.createSkill(skillId, level);
        if (skill != null) {
            // Áp dụng cooldown multiplier cho skill đặc biệt nếu là lính 1h
            if (durationOption == 1 && isSpecialSkill(skillId)) {
                // Skill đặc biệt của lính 1h có cooldown x1.5
                skill.coolDown = (int) (skill.coolDown * 1.5);
            }
            this.playerSkill.skills.add(skill);
        }
    }

    /**
     * Kiểm tra xem skill có phải là skill đặc biệt (chỉ có ở lính 1h trở lên) không
     */
    private boolean isSpecialSkill(byte skillId) {
        return skillId == Skill.THAI_DUONG_HA_SAN // Choáng
                || skillId == Skill.TRI_THUONG // Hồi máu
                || skillId == Skill.SOCOLA // Biến socola
                || skillId == Skill.BIEN_KHI // Hóa khỉ
                || skillId == Skill.THOI_MIEN // Thôi miên
                || skillId == Skill.TROI // Trói
                || skillId == Skill.LIEN_HOAN; // Liên hoàn
    }

    /**
     * Reload danh sách skill sau khi thay đổi durationOption
     * Gọi method này sau khi setDurationOption() để cập nhật skill đúng
     */
    public void reloadSkills() {
        addSkills();
    }

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

    // Getters for persistence
    public long getExpireTime() {
        return this.expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
        this.timeSurvive = (int) (expireTime - System.currentTimeMillis());
    }

    public int getMercenaryType() {
        return this.mercenaryType;
    }

    public void setMercenaryType(int type) {
        this.mercenaryType = type;
    }

    public short getHeadVal() {
        return this.headVal;
    }

    public short getBodyVal() {
        return this.bodyVal;
    }

    public short getLegVal() {
        return this.legVal;
    }

    public boolean isCanAttackBoss() {
        return this.canAttackBoss;
    }

    public void setCanAttackBoss(boolean canAttackBoss) {
        this.canAttackBoss = canAttackBoss;
    }

    public MercenaryTemplate getTemplate() {
        return this.template;
    }

    public int getDurationOption() {
        return this.durationOption;
    }

    public void setDurationOption(int durationOption) {
        this.durationOption = durationOption;
    }

    @Override
    public void update() {
        super.update();
        if (this.master != null && this.typePk == consts.ConstPlayer.PK_ALL
                && this.master.typePk != consts.ConstPlayer.PK_ALL) {
            PlayerService.gI().changeAndSendTypePK(this, this.master.typePk);
        }
        if (this.nPoint.mp <= this.nPoint.mpMax / 2) {
            this.nPoint.setMp(this.nPoint.mpMax);
            Service.gI().sendInfoPlayerEatPea(this);
        }
        if (isDie() && canRespawn()) {
            Service.getInstance().hsChar(this, nPoint.hpMax, nPoint.mpMax);
        }
        if (master != null && (this.zone == null || this.zone != master.zone)) {
            joinMapMaster();
        }
        if (System.currentTimeMillis() >= expireTime) {
            services.Service.gI().chat(this.master, "Lính đánh thuê " + this.name + " đã hết thời gian phục vụ!");
            dispose();
            return;
        }

        if (isAttackMode && !isDie()) {
            attack();
        } else if (!isAttackMode) {
            followMaster();
        }
    }

    private void attack() {
        if (!Util.canDoWithTime(lastTimeAttack, attackDelay)) {
            return;
        }

        // If too far from master, return to master
        if (master != null) {
            int distanceToMaster = Util.getDistance(this, master);
            int limitDist = (this.targetMob != null) ? 2000 : 600;

            if (distanceToMaster > limitDist) {
                this.targetMob = null;
                this.targetPlayer = null;
                followMaster();
                lastTimeAttack = System.currentTimeMillis();
                attackDelay = 300;
                return;
            }
        }

        if (this.targetPlayer != null && (this.targetPlayer.isDie() || this.targetPlayer.zone != this.zone)) {
            this.targetPlayer = null;
        }

        if (this.targetMob != null && (this.targetMob.isDie() || this.targetMob.zone != this.zone)) {
            this.targetMob = null;
        }

        if (this.targetPlayer == null && this.targetMob == null) {
            findTarget();
        }

        if (this.targetPlayer != null) {
            attackPlayer(this.targetPlayer);
        } else if (this.targetMob != null) {
            attackMob(this.targetMob);
        } else {
            attackDelay = 1000;
        }
    }

    private void attackMob(Mob target) {
        // Double-check: Nếu target là boss và lính không có quyền đánh boss -> bỏ qua
        if (target.isBigBoss() && !this.canAttackBoss) {
            this.targetMob = null;
            findTarget(); // Tìm target khác
            return;
        }

        int dis = Util.getDistance(this, target);
        Skill skill = pickSkill(dis);
        if (skill == null) {
            skill = pickSkill(40);
            if (skill != null) {
                moveTo(target.location.x, target.location.y);
                lastTimeAttack = System.currentTimeMillis();
                attackDelay = 500;
            }
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

    private void attackPlayer(Player target) {
        if (!this.canAttackBoss) {
            this.targetPlayer = null;
            return;
        }

        int dis = Util.getDistance(this, target);
        Skill skill = pickSkill(dis);
        if (skill == null) {
            skill = pickSkill(40);
            if (skill != null) {
                moveTo(target.location.x, target.location.y);
                lastTimeAttack = System.currentTimeMillis();
                attackDelay = 500;
            }
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
        this.targetMob = null;
        this.targetPlayer = null;
        if (this.master == null || this.zone == null) {
            return;
        }

        if (this.canAttackBoss) {
            List<Player> bosses = this.zone.getBosses();
            if (!bosses.isEmpty()) {
                Player closestBoss = null;
                int minDistanceBoss = Integer.MAX_VALUE;
                for (Player p : bosses) {
                    if (p != null && p instanceof Boss) {
                        Boss boss = (Boss) p;
                        if (!boss.isDie() && boss.bossStatus == BossStatus.ACTIVE) {
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
                    return; // Ưu tiên đánh boss
                }
            }
        }

        // 2. Tìm Mob thường
        Mob closestMob = null;
        int minDistanceMob = Integer.MAX_VALUE;

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

            // 1. Global Ban: Các skill không phù hợp cho lính hoặc gây lỗi
            if (id == Skill.PHAN_THAN || id == Skill.KHIEN_NANG_LUONG || id == Skill.HUYT_SAO
                    || id == Skill.DE_TRUNG || id == Skill.TROI || id == Skill.THOI_MIEN
                    || id >= Skill.SUPER_KAME) {
                continue;
            }

            boolean isAllowed = false;
            long effectiveCooldown = skill.coolDown;

            boolean isBasic = (id == Skill.DRAGON || id == Skill.DEMON || id == Skill.GALICK ||
                    id == Skill.KAMEJOKO || id == Skill.MASENKO || id == Skill.ANTOMIC ||
                    id == Skill.KAIOKEN || id == Skill.LIEN_HOAN);

            boolean isSpecial1h = (id == Skill.THAI_DUONG_HA_SAN || id == Skill.BIEN_KHI || id == Skill.TRI_THUONG);

            // 2. Phân quyền theo gói thời gian
            if (this.durationOption == 0) { // 30 phút: Chỉ Skill cơ bản
                if (isBasic)
                    isAllowed = true;
            } else if (this.durationOption == 1) { // 1 giờ: Cơ bản + Special (Cooldown x1.5)
                if (isBasic)
                    isAllowed = true;
                if (isSpecial1h) {
                    isAllowed = true;
                    effectiveCooldown = (long) (skill.coolDown * 1.5);
                }
            } else if (this.durationOption >= 2) { // 5 giờ: Full Skill (trừ Global Ban)
                isAllowed = true;
            }

            if (!isAllowed) {
                continue;
            }

            // 3. Check Cooldown
            long timeDiff = System.currentTimeMillis() - skill.lastTimeUseThisSkill;
            if (timeDiff < effectiveCooldown) {
                continue;
            }

            // 4. Check Mana
            if (this.nPoint.mp < skill.manaUse) {
                continue;
            }

            // 5. Check Range & Add valid skill
            if (id == Skill.DRAGON || id == Skill.DEMON || id == Skill.GALICK
                    || id == Skill.KAIOKEN || id == Skill.LIEN_HOAN) {
                if (distance <= 50) {
                    validSkills.add(skill);
                }
            } else if (id == Skill.KAMEJOKO || id == Skill.MASENKO || id == Skill.ANTOMIC
                    || id == Skill.MAKANKOSAPPO) {
                if (distance <= 300) {
                    validSkills.add(skill);
                }
            } else if (id == Skill.DICH_CHUYEN_TUC_THOI) {
                if (distance > 50 && distance <= 300) {
                    validSkills.add(skill);
                }
            } else {
                // Các skill buff, aoe, special khác (TDHS, BienKhi, TriThuong, QCKK...)
                // Không yêu cầu range quá gắt hoặc logic range xử lý sau
                validSkills.add(skill);
            }
        }

        if (validSkills.isEmpty()) {
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
        // Keep attack mode state - mercenary will continue attacking after respawn
        // this.isAttackMode = false; // Removed to preserve state
        lastTimeDie = System.currentTimeMillis();
    }

    private boolean canRespawn() {
        return lastTimeDie != 0 && Util.canDoWithTime(lastTimeDie, 5000);
    }

    public void joinMapMaster() {
        this.location.x = master.location.x + Util.nextInt(-10, 10);
        this.location.y = master.location.y;
        MapService.gI().goToMap(this, master.zone);
        this.zone.load_Me_To_Another(this);

        // Send/refresh icon when joining map (important for restored mercenaries after
        // login)
        sendMercenaryIcon();
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
    public void dispose() {
        removeMercenaryIcon();
        ChangeMapService.gI().exitMap(this);
        if (this.master != null) {
            this.master.linhDanhThueList.remove(this);
        }
        this.master = null;
    }
}
