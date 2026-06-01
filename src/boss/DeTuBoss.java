package boss;

import static boss.BossType.ANTROM;
import consts.ConstPlayer;
import item.Item;
import java.util.Random;
import map.ItemMap;
import player.Pet;
import player.Player;
import services.Service;
import services.SkillService;
import skill.Skill;
import utils.Util;

/**
 *
 * @author chill
 */
public class DeTuBoss extends Boss {

    public DeTuBoss() throws Exception {
        super(100, new BossData(
                "Gozilaa!!",
                ConstPlayer.TRAI_DAT,
                new short[] { 1428, 1429, 1430, 141, -1, -1 },
                1000000,
                new long[] { 500_000_000 },
                new int[] { 181 },
                new int[][] {
                        { Skill.DRAGON, 7, 1000 },
                        { Skill.GALICK, 7, 1000 }, { Skill.LIEN_HOAN, 7, 1000 },
                        { Skill.THOI_MIEN, 3, 50000 },
                        { Skill.DICH_CHUYEN_TUC_THOI, 3, 50000 } },
                new String[] { "|-1|ko ai nạp ta???" }, // text chat 1
                new String[] { "|-1|Hihihaha" }, // text chat 2
                new String[] { "|-1|Ban hết acc đê!!!" }, // text chat 3
                300));
    }

    @Override
    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!plAtt.isPet) {
                Service.gI().sendThongBaoOK(plAtt, "Chỉ đệ tử mới có thể gây dame, sư phụ có nịt!!!");
                return 0;
            }

            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            this.playerSkill.skillSelect = this.playerSkill.skills
                    .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
            SkillService.gI().useSkill(this, plAtt, null, -1, null);
            return damage;
        } else {
            return 0;
        }
    }

    @Override
    public void reward(Player plKill) {
        int[] listitem = { 1800, 1808, 1860, 1823, 1739, 457 };
        for (int i = 1; i < Util.nextInt(1, 5); i++) {

            Service.gI().dropItemMap(
                    this.zone,
                    new ItemMap(zone, listitem[Util.nextInt(0, 5)], 1, this.location.x + i * 10, this.location.y, -1));
        }
        // int[] itemDos = new int[]{233, 237, 241, 245, 249, 253, 257, 261, 265, 269,
        // 273, 277, 281};
        // int[] itemtime = new int[]{1151, 1152, 1153, 1154, 1150};
        // int randomDo = new Random().nextInt(itemDos.length);
        // int randomitem = new Random().nextInt(itemtime.length);

    }

}
