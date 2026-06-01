package boss.boss_manifest;

import boss.Boss;
import boss.BossData;
import boss.BossID;
import consts.ConstPlayer;
import skill.Skill;

public class Tester001 extends Boss {

    public Tester001() throws Exception {
        super(BossID.TESTER_001, new BossData(
                "Tester 001", // name
                ConstPlayer.TRAI_DAT, // gender
                new short[] { 288, 289, 290, -1, -1, -1 }, // outfit {head, body, leg, bag, aura, eff}
                10000, // dame
                new long[] { 1000000L }, // hp
                new int[] { 5, 10, 15 }, // map join
                new int[][] {
                        { 7, 7, 1000 },
                        { 2, 7, 1000 },
                        { 8, 7, 1000 }
                }, // skills
                new String[] { "|-1|Ta đã đến" }, // textS
                new String[] { "|-1|Haha!" }, // textM
                new String[] { "|-1|Ta sẽ quay lại" }, // textE
                60 // secondsRest
        ));
    }
}