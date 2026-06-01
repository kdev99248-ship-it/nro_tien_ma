package sosumenh;

import java.util.List;
import lombok.Data;
import player.Player;
import services.Service;

/*
 * @author
 */
@Data
public class SoSuMenhPlayer {

    private int point;
    private int level;
    private boolean vip;
    public Player player;
    public List<SoSuMenhTaskMain> ssmTaskMain;
    private int coutday = 3;
    public boolean reward[] = new boolean[20];
    public boolean rewardVip[] = new boolean[20];

    public SoSuMenhPlayer(Player player) {
        this.player = player;
    }

    public void addlevel(int count) {
        level += count;
        addPoint(count * 50);
    }

    public void addPoint(int point) {
        this.point += point;
        level = this.point / 50;
    }

    public SoSuMenhTaskMain getTaskById(int id) {
        for (SoSuMenhTaskMain ssm : ssmTaskMain) {
            if (ssm.idTask == id) {
                return ssm;
            }
        }
        return null;
    }

    public void addCountTask(int id) {
        if (ssmTaskMain == null) {
            return;
        }
        boolean finish = false;
        SoSuMenhTaskMain ssm = getTaskById(id);
        if (ssm == null) {
            return;
        }
        ssm.countTask++;
        if (ssm.finish) {
            return;
        }
        switch (id) {
            case 1 -> {
                if (ssm.countTask == 200) {
                    ssm.finish = true;
                    finish = true;
                }
            }
            case 2 -> {
                if (ssm.countTask == 100) {
                    ssm.finish = true;
                    finish = true;
                }
            }
            case 3 -> {
                if (ssm.countTask == 100) {
                    ssm.finish = true;
                    finish = true;
                }
            }
            case 4 -> {
                if (ssm.countTask == 1000) {
                    ssm.finish = true;
                    finish = true;
                }
            }
            case 5 -> {
                if (ssm.countTask == 30) {
                    ssm.finish = true;
                    finish = true;
                }
            }
        }
        if (finish) {
            SoSuMenhTaskTemplate smmtem = SoSuMenhManager.getInstance().findById(id);
            addPoint(smmtem.getPoint());
            // Service.gI().sendThongBaoFromAdmin(player, "Chúc mừng bạn đã hoàn thành nhiệm
            // vụ sổ sứ mệnh: " + smmtem.getTask() + " và nhận được " + smmtem.getPoint() +
            // " điểm");
        }
    }

}
