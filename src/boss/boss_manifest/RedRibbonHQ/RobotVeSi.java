package boss.boss_manifest.RedRibbonHQ;
import boss.BossID;
import boss.BossData;

/*
 *
 *
 * @author EMTI
 */

import consts.ConstPlayer;
import boss.*;
import static boss.BossType.PHOBANDT;
import map.ItemMap;
import map.Zone;
import player.Player;
import skill.Skill;
import services.EffectSkillService;
import services.Service;
import services.func.ChangeMapService;
import utils.Util;

public class RobotVeSi extends Boss {

    public RobotVeSi(Zone zone, int id, long dame, long hp) throws Exception {
        super(PHOBANDT, BossID.ROBOT_VE_SI - id, new BossData(
                "Rôbốt Vệ Sĩ 0" + id, //name
                ConstPlayer.TRAI_DAT, //gender
                new short[]{138, 139, 140, -1, -1, -1}, //outfit {head, body, leg, bag, aura, eff}
                ((dame)), //dame
                new long[]{((hp))}, //hp
                new int[]{57}, //map join
                new int[][]{
                    {Skill.DEMON, 3, 1}, {Skill.DEMON, 6, 2}, {Skill.DRAGON, 7, 3}, {Skill.DRAGON, 1, 4}, {Skill.GALICK, 5, 5},
                    {Skill.KAMEJOKO, 7, 6}, {Skill.KAMEJOKO, 6, 7}, {Skill.KAMEJOKO, 5, 8}, {Skill.KAMEJOKO, 4, 9}, {Skill.KAMEJOKO, 3, 10}, {Skill.KAMEJOKO, 2, 11}, {Skill.KAMEJOKO, 1, 12},
                    {Skill.ANTOMIC, 1, 13}, {Skill.ANTOMIC, 2, 14}, {Skill.ANTOMIC, 3, 15}, {Skill.ANTOMIC, 4, 16}, {Skill.ANTOMIC, 5, 17}, {Skill.ANTOMIC, 6, 19}, {Skill.ANTOMIC, 7, 20},
                    {Skill.MASENKO, 1, 21}, {Skill.MASENKO, 5, 22}, {Skill.MASENKO, 6, 23},
                    {Skill.KAMEJOKO, 7, 1000},},
                new String[]{}, //text chat 1
                new String[]{}, //text chat 2
                new String[]{}, //text chat 3
                60
        ));

        this.zone = zone;
    }

    @Override
    public void reward(Player plKill) {
        if (Util.isTrue(100, 100)) {
            ItemMap it = new ItemMap(this.zone, Util.nextInt(16, 20), 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
    }

    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 300, 312);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
        Service.gI().setPos(this, 300, 312);
    }

    @Override
    public void afk() {
        Player pl = getPlayerAttack();
        if (pl == null || pl.isDie()) {
            return;
        }
        Service.gI().setPos(this, pl.location.x + Util.nextInt(-100, 100), 0);
        this.changeStatus(BossStatus.ACTIVE);
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
                damage = damage / 2;
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
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        RedRibbonHQManager.gI().removeBoss(this);
        this.dispose();
    }
}
