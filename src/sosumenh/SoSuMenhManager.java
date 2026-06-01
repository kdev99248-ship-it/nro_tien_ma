package sosumenh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdbc.DBConnecter;
import lombok.Getter;
import npc.npc_manifest.SoSuMenh;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import services.ItemService;

/*
 * @author BTH Cute Phô Mai Que
 */
public class SoSuMenhManager {

    public List<SoSuMenhTaskTemplate> list = new ArrayList<>();

    public void loading() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try ( Connection con2 = DBConnecter.getConnectionServer()) {
            ps = con2.prepareStatement("SELECT * FROM so_su_menh_task");
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new SoSuMenhTaskTemplate(rs.getInt("id"), rs.getString("task"), rs.getInt("point")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(SoSuMenhManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public SoSuMenhTaskTemplate findById(int id) {
        for (SoSuMenhTaskTemplate ssm : list) {
            if (ssm.getId() == id) {
                return ssm;
            }
        }
        return null;
    }

    @Getter
    private static final SoSuMenhManager instance = new SoSuMenhManager();

}
