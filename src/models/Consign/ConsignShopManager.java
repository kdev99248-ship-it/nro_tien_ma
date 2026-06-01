package models.Consign;

/*
 *
 *
 * @author EMTI
 */

import jdbc.DBConnecter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONValue;

public class ConsignShopManager {

    private static ConsignShopManager instance;

    public static ConsignShopManager gI() {
        if (instance == null) {
            instance = new ConsignShopManager();
        }
        return instance;
    }

    public long lastTimeUpdate;

    public String[] tabName = {"Trang bị", "Phụ kiện", "Hỗ trợ", "Linh tinh", ""};

    public List<ConsignItem> listItem = new ArrayList<>();

    public void save() {
        try (Connection con = DBConnecter.getConnectionServer();) {
            Statement s = con.createStatement();
            s.execute("TRUNCATE shop_ky_gui");
            for (ConsignItem it : this.listItem) {
                if (it != null) {
                    s.execute(String.format("INSERT INTO `shop_ky_gui`(`id`, `player_id`, `tab`, `item_id`, `gold`, `gem`, `quantity`, `itemOption`, `lasttime`, `isBuy`) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                            it.id, it.player_sell, it.tab, it.itemId, it.goldSell, it.gemSell, it.quantity, JSONValue.toJSONString(it.options).equals("null") ? "[]" : JSONValue.toJSONString(it.options), it.lasttime, it.isBuy ? 1 : 0));
                }
            }
        } catch (Exception e) {
        }
    }
}
