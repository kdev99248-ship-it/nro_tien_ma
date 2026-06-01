package models.The23rdMartialArtCongress;

import consts.ConstPlayer;
import boss.Boss;
import boss.BossID;
import boss.BossManager;
import boss.BossStatus;
import boss.boss_manifest.The23rdMartialArtCongress.DHVT23Boss;
import player.Player;
import services.EffectSkillService;
import services.ItemTimeService;
import services.PlayerService;
import services.Service;
import lombok.Getter;
import lombok.Setter;
import map.Zone;
import matches.pvp.DHVT;
import utils.Util;

public class The23rdMartialArtCongress {

    @Setter
    @Getter
    private Player player;

    @Setter
    @Getter
    private Boss bss;

    @Setter
    private Player npc;

    @Setter
    @Getter
    private Zone zone;

    public boolean endChallenge;

    private int time;
    @Setter
    private int round;
    private int timeWait;

    public void update() {
        if (player.zone == null || !player.zone.map.isMapDHVT23()) {
            The23rdMartialArtCongressManager.gI().remove(this);
            return;
        }
        if (bss != null && bss.isDie()) {
            round++;
            bss.leaveMap();
            bss = null;
            toTheNextRound();
        }
        if (player.isDie()) {
            The23rdMartialArtCongressManager.gI().remove(this);
            return;
        }
        if (timeWait > 0) {
            timeWait--;
            if (timeWait == 0) {
                ready();
            }
        }
    }

    public void ready() {
        if (bss != null) {
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.PK_ALL);
            PlayerService.gI().changeAndSendTypePK(bss, ConstPlayer.PK_ALL);
        }
    }

    public void toTheNextRound() {
        try {
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
            int bossId = -1;
            switch (round) {
                case 0: bossId = BossID.SOI_HEC_QUYN; break;
                case 1: bossId = BossID.O_DO; break;
                case 2: bossId = BossID.XINBATO; break;
                case 3: bossId = BossID.CHA_PA; break;
                case 4: bossId = BossID.PON_PUT; break;
                case 5: bossId = BossID.CHAN_XU; break;
                case 6: bossId = BossID.TAU_PAY_PAY; break;
                case 7: bossId = BossID.YAMCHA; break;
                case 8: bossId = BossID.JACKY_CHUN; break;
                case 9: bossId = BossID.THIEN_XIN_HANG; break;
                case 10: bossId = BossID.LIU_LIU; break;
                case 11: bossId = BossID.POCOLO; break;
                case 12: champion(); return;
                default: return;
            }
            bss = BossManager.gI().createBoss(bossId);
            if (bss != null && bss instanceof DHVT23Boss) {
                ((DHVT23Boss) bss).setPlayerAtt(player);
            }

            Service.gI().setPos(player, 335, 264);
            setTimeWait(13);
        } catch (Exception e) {
        }
    }

    public void champion() {
        Service.gI().sendThongBao(player, "Chúc mừng bạn đã giành giải nhất");
        The23rdMartialArtCongressManager.gI().remove(this);
    }

    public void leave() {
        The23rdMartialArtCongressManager.gI().remove(this);
    }

    public void die() {
        if (bss != null) {
            bss.leaveMap();
            bss = null;
        }
        leave();
    }


    public void setTimeWait(int timeWait) {
        this.timeWait = timeWait;
    }
}