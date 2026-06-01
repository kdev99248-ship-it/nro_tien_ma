package server;

/*
 *
 *
 * @author EMTI
 */
import EMTI.Functions;
import services.Service;
import utils.Logger;
import jdbc.daos.PlayerDAO;
import player.Player;

public class Maintenance extends Thread {

    public static boolean isRunning = false;

    private static Maintenance i;

    private int time;

    private Maintenance() {

    }

    public static Maintenance gI() {
        if (i == null) {
            i = new Maintenance();
        }
        return i;
    }

    public void start(int min) {
        if (!isRunning) {
            isRunning = true;
            this.time = min;
            this.start();
        }
    }

    public void startNew(int min) {
        if (!isRunning) {
            isRunning = true;
            this.time = min;
            new Thread(Maintenance.gI(), "Thread Bảo Trì").start();
        }
    }

    public void startImmediately() {
        if (!isRunning) {
            isRunning = true;
            Logger.log(Logger.YELLOW, "BEGIN MAINTENANCE\n");
            ServerManager.gI().close();
        }
    }

    @Override
    public void run() {
        while (this.time > 0) {
            if (this.time == 60) {
                Service.gI().sendThongBaoAllPlayer(
                        "Hệ thống sẽ bảo trì sau 1 phút nữa hãy thoát game ngay để tránh mất mát vật phẩm.");
                try {
                    Functions.sleep(1000);
                } catch (Exception e) {
                }
                this.time--;
            } else if (time < 60) {
                Service.gI().sendThongBaoAllPlayer("Hệ thống sẽ bảo trì sau " + time + " giây nữa");

                if (this.time == 5) {
                    new Thread(() -> {
                        Logger.log(Logger.YELLOW, "Auto Saving Data before shutdown...\n");
                        try {
                            for (Player pl : Client.gI().getPlayers()) {
                                if (pl != null) {
                                    PlayerDAO.updatePlayer(pl);
                                }
                            }
                            Logger.success("Auto Saved Data.\n");
                        } catch (Exception e) {
                            Logger.error("Error Auto Save: " + e.getMessage() + "\n");
                        }
                    }).start();
                }

                try {
                    Functions.sleep(1000);
                } catch (Exception e) {
                }
                this.time--;
            } else {
                int hour = this.time / 3600;
                int min = (this.time - hour * 3600) / 60;
                int sec = this.time % 60;

                String hourStr = (hour > 0) ? hour + " giờ " : "";
                String minStr = (min > 0) ? min + " phút " : "";
                String secStr = (sec > 0) ? sec + " giây " : "";

                Service.gI().sendThongBaoAllPlayer("Hệ thống sẽ bảo trì sau " + hourStr + minStr + secStr
                        + "nữa");
                Logger.log(Logger.YELLOW, "Hệ thống sẽ bảo trì sau " + hourStr + minStr + secStr
                        + "nữa\n");
                if (sec == 0 && this.time > 60) {
                    sec = 60;
                } else if (sec == 0) {
                    sec = 1;
                }
                this.time -= sec;
                try {
                    Functions.sleep(sec * 1000);
                } catch (Exception e) {
                }
            }
        }
        Logger.log(Logger.YELLOW, "BEGIN MAINTENANCE\n");
        ServerManager.gI().close();
    }

}
