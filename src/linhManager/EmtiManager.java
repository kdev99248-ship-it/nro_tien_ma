/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linhManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jdbc.daos.PlayerDAO;
import network.server.EmtiSessionManager;

import server.Client;
import server.Maintenance;
import utils.Logger;
import utils.TimeUtil;

/**
 *
 * @author Lucy An Trom
 */
public class EmtiManager {

    private static EmtiManager instance = null;

    // Static method
    // Static method to create instance of Singleton class
    public static synchronized EmtiManager getInstance() {
        if (instance == null) {
            instance = new EmtiManager();
        }
        return instance;
    }

    private ScheduledExecutorService scheduler;

    public void startAutoSave() {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                handleAutoSave();
            } catch (Exception e) {
                System.out.println("[AutoSaveManager] start autosave error: " + e.getLocalizedMessage());
            }
        }, 60, 90, TimeUnit.SECONDS);
    }

    public void handleAutoSave() {
        Client.gI().getPlayers().forEach(player -> {
            // Chuyển sang lưu bất đồng bộ, đẩy task vào hàng đợi DbAsyncTask
            // Thay vì ép Server block lại để lưu JSON từng thằng
            PlayerDAO.updatePlayerAsync(player);
        });
    }

}
