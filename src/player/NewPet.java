package player;

/*
 *
 *
 * @author EMTI
 */

import services.MapService;
import services.PlayerService;
import services.Service;
import services.func.ChangeMapService;
import services.func.EffectMapService;
import utils.Util;

public class NewPet extends Player {

    public Player master;
    public short body;
    public short leg;
    public static int idb = -310720020;

    public NewPet(Player master, short h, short b, short l) {
        this.master = master;
        this.isNewPet = true;
        this.isNewPet1 = true;
        this.id = idb;
        idb--;
        this.head = h;
        this.body = b;
        this.leg = l;
    }

    @Override
    public short getHead() {
        return head;
    }

    @Override
    public short getBody() {
        return body;
    }

    @Override
    public short getLeg() {
        return leg;
    }

    public void joinMapMaster() {
        if (master == null || master.zone == null) {
            return;
        }
        this.location.x = master.location.x + Util.nextInt(-10, 10);
        this.location.y = master.location.y;
        if (isPl()) {
            this.dispose();
            return;
        }
        ChangeMapService.gI().goToMap(this, master.zone);
        this.zone.load_Me_To_Another(this);
    }

    private long lastTimeMoveIdle;
    private int timeMoveIdle;
    public boolean idle;
    private long lastTimeSendEffPet; // Quản lý thời gian gửi hiệu ứng pet

    private void moveIdle() {
        if (idle && Util.canDoWithTime(lastTimeMoveIdle, timeMoveIdle)) {
            int dir = this.location.x - master.location.x <= 0 ? -1 : 1;
            PlayerService.gI().playerMove(this, master.location.x
                    + Util.nextInt(dir == -1 ? 30 : -50, dir == -1 ? 50 : 30), master.location.y);
            lastTimeMoveIdle = System.currentTimeMillis();
            timeMoveIdle = Util.nextInt(5000, 8000);
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.isDie()) {
            Service.gI().hsChar(this, nPoint.hpMax, nPoint.mpMax);
        }
        if (master != null && master.zone != null && (this.zone == null || this.zone != master.zone)
                && !MapService.gI().isMapOffline(master.zone.map.mapId)) {
            joinMapMaster();
        }
        if (master != null && master.isDie()) {
            return;
        }
        // Hiệu ứng cho Pet có đầu ID 1898
        if (this.head == 1898 && this.zone != null && Util.canDoWithTime(lastTimeSendEffPet, 500)) {
            int x_offset = 0; // Điều chỉnh tọa độ X (ví dụ: +10, -10)
            int y_offset = 15; // Điều chỉnh tọa độ Y (ví dụ: -20 để hiệu ứng bay lên cao hơn)
            int eff_id = 190; // ID hiệu ứng
            int layer = 3; // Lớp hiển thị
            int loop = 1; // Giảm xuống 1 để hiệu ứng chỉ diễn ra duy nhất 1 lần rồi mất
            int delay = 1; // Đơn vị giây thời gian để hiệu ứng biến mất

            EffectMapService.gI().sendEffectMapToAllInMap(this.zone, eff_id, layer, loop, this.location.x + x_offset,
                    this.location.y + y_offset, delay);
            lastTimeSendEffPet = System.currentTimeMillis();
        }
        moveIdle();
    }

    public void followMaster() {
        followMaster(50);
    }

    private void followMaster(int dis) {
        int mX = master.location.x;
        int mY = master.location.y;
        int disX = this.location.x - mX;
        if (Math.sqrt(Math.pow(mX - this.location.x, 2) + Math.pow(mY - this.location.y, 2)) >= dis) {
            if (disX < 0) {
                this.location.x = mX - Util.nextInt(0, dis);
            } else {
                this.location.x = mX + Util.nextInt(0, dis);
            }
            this.location.y = mY;
            PlayerService.gI().playerMove(this, this.location.x, this.location.y);
        }
    }

    @Override
    public void dispose() {
        this.master = null;
        ChangeMapService.gI().exitMap(this);
        super.dispose();
    }
}
