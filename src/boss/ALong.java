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

public class ALong extends Boss {

    public ALong() throws Exception {
        super(-99, new BossData(
                "Along",
                ConstPlayer.TRAI_DAT,
                new short[]{1859 , 1860, 1861, 77, 73, -1},
                100000,
                new long[]{4_000_000_000L},
                new int[]{203,205,206,204},
                new int[][]{
                   
                    {Skill.DICH_CHUYEN_TUC_THOI, 7, 10000}, {Skill.QUA_CAU_KENH_KHI, 7, 100000},
                    {Skill.LIEN_HOAN_CHUONG, 3, 50000},
                    {Skill.GALICK, 3, 5000}},
                new String[]{"|-1|ko ai nạp ta???"}, //text chat 1
                new String[]{"|-1|Hihihaha"}, //text chat 2
                new String[]{"|-1|Ban hết acc đê!!!"}, //text chat 3
                300));
    }

    @Override
    public void reward(Player plKill) {
        
        
           for (int i = 0; i < Util.nextInt(1,10); i++) {
                
                ItemMap it = new ItemMap(this.zone, 1398, (int) 1, this.location.x + i * 10, this.zone.map.yPhysicInTop(this.location.x,
                        this.location.y - 24), plKill.id);
                
                Service.gI().dropItemMap(this.zone, it);
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
    }

}
