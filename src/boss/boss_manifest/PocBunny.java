package boss.boss_manifest;

import boss.Boss;
import boss.BossData;
import boss.BossID;
import boss.BossStatus;
import consts.ConstPlayer;
import map.ItemMap;
import map.Zone;
import player.Player;
import skill.Skill;
import utils.Util;
import services.EffectSkillService;
import services.Service;
import services.SkillService;
import services.PetService;
import services.func.ChangeMapService;

public class PocBunny extends Boss {

    public PocBunny() throws Exception {
        super(BossID.POC_BUNNY, new BossData(
                "Poc Bunny", // name
                ConstPlayer.TRAI_DAT, // gender
                new short[] { 1862, 1863, 1864, -1, -1, -1 }, // outfit {head, body, leg, bag, aura, eff}
                10000, // dame
                new long[] { 1000000L }, // hp
                new int[] { 5, 10, 15 }, // map join
                new int[][] {
                        { 0, 7, 1000 },
                        { 1, 7, 3000 },
                        { 8, 7, 180000 },
                        { 17, 7, 2000 },
                        { 7, 7, 185000 },
                        { 14, 7, 1000 }
                }, // skills
                new String[] { "|-1|Gái xinh đây chứ đâu" }, // textS
                new String[] { "|-1|aHihihi!" }, // textM
                new String[] { "|-1|Đánh con gái, quá hèn!" }, // textE
                60 // secondsRest
        ));
    }

    @Override
    public void reward(Player plKill) {
        // int[] itemDos = new int[]{555, 557, 559, 556, 558, 560, 562, 564, 566, 563,
        // 565, 567};
        // int[] NRO = new int[]{17, 18};
        // int randomDo = Util.nextInt(itemDos.length);
        // int randomNRO = Util.nextInt(NRO.length);
        // if (Util.isTrue(1, 10)) {
        // if (Util.isTrue(1, 10)) {
        // Service.gI().dropItemMap(this.zone, Util.ratiItem(zone, 16, 1,
        // this.location.x, this.location.y, plKill.id));
        // return;
        // }
        // Service.gI().dropItemMap(this.zone, Util.ratiItem(zone, NRO[randomNRO], 1,
        // this.location.x, this.location.y, plKill.id));
        // return;
        // } else {
        // Service.gI().dropItemMap(this.zone, Util.ratiItem(zone, itemDos[randomDo], 1,
        // this.location.x, this.location.y, plKill.id));
        // }
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void joinMap() {
        super.joinMap(); // Default join map
        // st = System.currentTimeMillis();
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        // ChangeMapService.gI().exitMap(this);
        // this.lastZone = null;
        // this.lastTimeRest = System.currentTimeMillis();
        // this.changeStatus(BossStatus.REST);
    }

    @Override
    public void autoLeaveMap() {
        // if (Util.canDoWithTime(st, 900000)) {
        // this.leaveMap();
        // }
        // if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
        // st = System.currentTimeMillis();
        // }
        super.autoLeaveMap();
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            return super.injured(plAtt, damage, piercing, isMobAttack);
        }
        return 0;
    }

    @Override
    public void attack() {
        // if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk ==
        // ConstPlayer.PK_ALL) {
        // this.lastTimeAttack = System.currentTimeMillis();
        // try {
        // Player pl = getPlayerAttack();
        // if (pl == null || pl.isDie()) return;
        // this.playerSkill.skillSelect =
        // this.playerSkill.skills.get(Util.nextInt(this.playerSkill.skills.size()));
        // if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
        // if (Util.isTrue(5, 20)) {
        // if (SkillUtil.isUseSkillChuong(this)) {
        // this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
        // Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0,
        // 70));
        // } else {
        // this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
        // Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0,
        // 50));
        // }
        // }
        // SkillService.gI().useSkill(this, pl, null, -1, null);
        // checkPlayerDie(pl);
        // } else {
        // if (Util.isTrue(1, 2)) this.moveToPlayer(pl);
        // }
        // } catch (Exception ex) { ex.printStackTrace(); }
        // }
        super.attack();
    }
}
