package boss;

import consts.ConstPlayer;
import boss.Boss;
import boss.BossesData;
import boss.BossID;
import java.util.Random;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.PlayerService;
import services.Service;
import services.TaskService;
import utils.Util;
import services.func.ChangeMapService;
import skill.Skill;
import utils.Logger;

public class TaiLocQuaLon extends Boss {
 private long st;
 private int timeLeave;
    public TaiLocQuaLon() throws Exception {
        super(-99, new BossData(
                "Tài Lộc Quá Lớn",
                ConstPlayer.TRAI_DAT,
                new short[]{1386, 1387, 1388, 105, -1, -1},
                100000,
                new long[]{4_000_000_000L},
                new int[]{176},
                new int[][]{
                    {Skill.DRAGON, 7, 1000},
                    {Skill.GALICK, 7, 1000}, {Skill.LIEN_HOAN, 7, 1000},
                    {Skill.TAI_TAO_NANG_LUONG, 7, 50000},
                    {Skill.DICH_CHUYEN_TUC_THOI, 3, 50000}},
                new String[]{"|-1|Tài Lộc, Tài Lộc, Tài Lộc Quá Lớn???"}, //text chat 1
                new String[]{"|-1|Hihihaha"}, //text chat 2
                new String[]{"|-1|Ban hết acc đê!!!"}, //text chat 3
                600));
    }

    @Override
    public void reward(Player plKill) {
        if(Util.isTrue(20,100)){
            for (int i = 1; i < Util.nextInt(1,5); i++) {
                
                Service.gI().dropItemMap(
                        this.zone,
                        new ItemMap(zone, 457, 1, this.location.x + i*10, this.location.y, -1)
                );
            }
        }
        for (int i = 0; i < Util.nextInt(3, 20); i++) {
//            Service.gI().dropItemMap(this.zone, new ItemMap(zone, Util.nextInt(1488,1491), 1, this.location.x + i * 10, this.zone.map.yPhysicInTop(this.location.x,
//                    this.location.y - 24), plKill.id));
            ItemMap it = new ItemMap(this.zone, Util.nextInt(1488,1491), 1, this.location.x + i * 10, this.zone.map.yPhysicInTop(this.location.x,
                        this.location.y - 24), -1);
             Service.gI().dropItemMap(this.zone, it);
        }
        if(Util.isTrue(30,100)){
            for (int i = 0; i < Util.nextInt(1, 3); i++) {
            ItemMap it = new ItemMap(this.zone, 1187, 1, this.location.x + i * 10, this.zone.map.yPhysicInTop(this.location.x,
                        this.location.y - 24), -1);
             Service.gI().dropItemMap(this.zone, it);
        }
        }else{
            for (int i = 0; i < Util.nextInt(1, 10); i++) {
//            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 717, 1, this.location.x + i * 10, this.zone.map.yPhysicInTop(this.location.x,
//                    this.location.y - 24), plKill.id));
            ItemMap it = new ItemMap(this.zone, 717, 1, this.location.x + i * 10, this.zone.map.yPhysicInTop(this.location.x,
                        this.location.y - 24), -1);
             Service.gI().dropItemMap(this.zone, it);
        }
        }
               
        }
    

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            
            damage = this.nPoint.subDameInjureWithDeff(damage / 1);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 1;
            }
//            if (damage > 50_000_000) {
//                damage = 50_000_000;
//            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return damage;
        } else {
            return 0;
        }
    }@Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            this.wakeupAnotherBossWhenAppear();
            return;
        }
        if (this.zone == null) {
            if (this.parentBoss != null) {
                this.zone = parentBoss.zone;
            } else if (this.lastZone == null) {
                this.zone = getMapJoin();
            } else {
                this.zone = this.lastZone;
            }
        }
        if (this.zone != null) {
            try {
                int zoneid = Util.nextInt(0,5);
                // Check trong khu lớn hơn 10 người chuyển sang khu n + 1
                while (zoneid < this.zone.map.zones.size() && this.zone.map.zones.get(zoneid).getNumOfPlayers() > 10) {
                    zoneid++;
                }
                // Check trong khu có boss sẽ chuyển sang khu n + 1
                while (zoneid < this.zone.map.zones.size() && BossManager.gI().checkBosses(this.zone.map.zones.get(zoneid), BossID.ONG_GIA_NOEL)) {
                    zoneid++;
                }
                if (zoneid < this.zone.map.zones.size()) {
                    this.zone = this.zone.map.zones.get(zoneid);
                } else {
                    this.leaveMapNew();
                    return;
                }
                ChangeMapService.gI().changeMap(this, this.zone, Util.nextInt(100, 500), this.zone.map.yPhysicInTop(this.location.x,
                        this.location.y - 24));
                this.changeStatus(BossStatus.CHAT_S);
                st = System.currentTimeMillis();
                timeLeave = Util.nextInt(100000, 300000);
            } catch (Exception e) {
                Logger.error(this.data[0].getName() + ": Lỗi đang tiến hành REST\n");
                this.changeStatus(BossStatus.REST);
            }
        } else {
            Logger.error(this.data[0].getName() + ": Lỗi map đang tiến hành RESPAWN\n");
            this.changeStatus(BossStatus.RESPAWN);
        }
}
}
