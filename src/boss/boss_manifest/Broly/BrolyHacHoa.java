package boss.boss_manifest.Broly;

import boss.Boss;
import boss.BrolyManager;
import boss.BossData;
import boss.BossID;
import boss.BossStatus;
import static boss.BossType.BROLY;
import boss.BossesData;
import consts.ConstPlayer;
import map.ItemMap;
import map.Zone;
import player.Player;
import services.EffectSkillService;
import services.ItemService;
import services.PetService;
import services.PlayerService;
import services.Service;
import services.SkillService;
import services.func.ChangeMapService;
import skill.Skill;
import utils.SkillUtil;
import utils.Util;

public class BrolyHacHoa extends Boss {

    public BrolyHacHoa() throws Exception {
        super(BROLY, BossID.BROLYHACHOA, false, true, BossesData.BROLYHACHOA);
    }

    public BrolyHacHoa(Zone zone, int x, int y) throws Exception {
        super(BROLY, BossID.BROLYHACHOA, false, true, new BossData(
                "Broly Hắc Hóa", //name
                ConstPlayer.XAYDA, //gender
                new short[]{1592, 1593, 1594, 0, 98, 0}, //outfit {head, body, leg, bag, aura, eff}
                100000, //dame
                new long[]{10000}, //hp
                new int[]{1,2,3,4,5,15,16,17,18}, //map join
                new int[][]{
                    {Skill.KAMEJOKO, 7, 1000}, {Skill.GALICK, 7, 1000}, {Skill.KAIOKEN, 7, 1000}, 
                    {Skill.DE_TRUNG, 7, 1000}, {Skill.DICH_CHUYEN_TUC_THOI, 1, 10000}, {Skill.BIEN_KHI, 7, 40000},}, //skill
                new String[]{}, //text chat 1
                new String[]{"|-1|Haha! từ lớp 1 đến lớp 7 tao còn chưa sợ",
                    "|-1|Haha! Vào hết đây!!!",}, //text chat 2
                new String[]{"|-1|Dmm nhớ mặt tao!"}, //text chat 3
                600//type appear
        ));
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
    }

    @Override
    public void reward(Player plKill) {
        plKill.pointbossday+=2;
        byte type = (byte)(Util.nextInt(2,4));
        if (plKill.pet == null) {
            PetService.gI().createNormalPetSuperGender(plKill, Util.nextInt(0,2),type);
            Service.gI().sendThongBao(plKill, "Bạn đã nhận được đệ tử\n Vui lòng thoát game vào lại");
        } else {
            if (Util.isTrue(50, 100)) {
                ItemMap it = new ItemMap(this.zone, 1858, (int) 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                        this.location.y - 24), plKill.id);
                Service.gI().dropItemMap(this.zone, it);
            }
        }  
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void joinMap() {
        this.name = "Broly Hắc Hóa " + Util.nextInt(1, 100);
        this.nPoint.hpMax = 10000;
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.dame = 100000;
        this.nPoint.crit = Util.nextInt(100);

        if (Util.isTrue(3, 5) && this.zone != null) {
            ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
            this.changeStatus(BossStatus.CHAT_S);
            this.notifyJoinMap();
        } else {
            super.joinMap();
        }
        if (this.pet == null) {
            PetService.gI().createNormalPetSuper(this, Util.nextInt(0, 2), (byte)Util.nextInt(2,4));
        }
//        PlayerService.gI().changeAndSendTypePK(this.pet, ConstPlayer.PK_ALL);
        st = System.currentTimeMillis();
    }

    private long st;

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMap();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (piercing) {
                damage /= 100;
            }
            if (Util.isTrue(200, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            if (damage >= 100) {
                damage = 100;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
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
    private long lastTimeAttack;

    @Override
    public void attack() {

        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {

            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(7, this.playerSkill.skills.size() - 1));
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

    
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        if (this.pet != null) {
            ChangeMapService.gI().exitMap(this.pet);
        }
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        BrolyManager.gI().removeBoss(this);
        this.dispose();
    }
}
