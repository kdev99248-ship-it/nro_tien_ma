package tutien;

import jdbc.DBConnecter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

// M8 Luyen Dan: registry dan phuong, load tu DB (tutien_dan_phuong + tutien_dan_phuong_nl) luc khoi dong.
// Nhom theo realm (canh gioi) -> man chon dan phuong hien DONG theo cac canh co dan (de them ve sau).
// Map duoc public ATOMIC (volatile reassign) nen reload luc runtime an toan voi thread mang.
public class DanPhuongTemplate {

    private static volatile Map<Integer, DanPhuong>       BY_ID    = new LinkedHashMap<>();
    private static volatile Map<Integer, List<DanPhuong>> BY_REALM = new TreeMap<>();

    public static void load() {
        Map<Integer, DanPhuong>       byId    = new LinkedHashMap<>();
        Map<Integer, List<DanPhuong>> byRealm = new TreeMap<>();
        try (Connection con = DBConnecter.getConnectionServer()) {
            // 1) cong thuc (chi lay dan dang bat)
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id, name, pill_item_id, realm, req_level, description, is_active " +
                    "FROM tutien_dan_phuong WHERE is_active = 1 ORDER BY realm ASC, id ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DanPhuong d = new DanPhuong();
                    d.id          = rs.getInt("id");
                    d.name        = rs.getString("name");
                    d.pillItemId  = rs.getInt("pill_item_id");
                    d.realm       = rs.getInt("realm");
                    d.reqLevel    = rs.getInt("req_level");
                    d.description = rs.getString("description");
                    d.active      = rs.getInt("is_active") == 1;
                    byId.put(d.id, d);
                    byRealm.computeIfAbsent(d.realm, k -> new ArrayList<>()).add(d);
                }
            }
            // 2) nguyen lieu cua tung dan phuong
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT recipe_id, item_id, qty FROM tutien_dan_phuong_nl ORDER BY recipe_id ASC, item_id ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DanPhuong d = byId.get(rs.getInt("recipe_id"));
                    if (d != null) d.addMaterial(rs.getInt("item_id"), rs.getInt("qty"));
                }
            }
            BY_ID    = byId;     // publish atomic
            BY_REALM = byRealm;
            Logger.getLogger(DanPhuongTemplate.class.getName())
                  .info("DanPhuongTemplate loaded: " + BY_ID.size() + " dan phuong / " + BY_REALM.size() + " canh gioi");
        } catch (Exception ex) {
            Logger.getLogger(DanPhuongTemplate.class.getName())
                  .log(Level.SEVERE, "Cannot load DanPhuongTemplate (da chay migration_m8_luyen_dan.sql chua?)", ex);
        }
    }

    public static DanPhuong                    get(int id)  { return BY_ID.get(id); }
    public static Collection<DanPhuong>        all()        { return BY_ID.values(); }
    public static Map<Integer, List<DanPhuong>> byRealm()   { return BY_REALM; }
    public static int                          size()       { return BY_ID.size(); }
}
