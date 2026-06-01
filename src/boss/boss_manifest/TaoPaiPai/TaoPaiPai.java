package boss.boss_manifest.TaoPaiPai;

/*
 *
 *
 * @author EMTI
 */

import boss.Boss;
import boss.BossesData;
import static boss.BossType.FINAL;
import player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;

public class TaoPaiPai extends Boss {

    public TaoPaiPai() throws Exception {
        super(FINAL, Util.randomBossId(), BossesData.TAU_PAY_PAY_DONG_NAM_KARIN);
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (damage >= 5000000) {
                damage = 5000000;
            }
            this.nPoint.dame = damage / Util.nextInt(500, 1000);
            this.nPoint.subHP(damage);
            long tnSm = damage * Util.nextInt(20, 50);
            if (tnSm > 1000000) {
                tnSm = 1000000 - Util.nextInt(100000);
            }
            if (plAtt.nPoint.power > 120_000_000_000L) {
                tnSm = Util.nextInt(1000);
            }
            Service.gI().addSMTN(plAtt, (byte) 2, tnSm, true);
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
