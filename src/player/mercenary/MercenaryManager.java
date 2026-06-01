package player.mercenary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdbc.DBConnecter;

/**
 * Manager quản lý các template lính đánh thuê
 * Load dữ liệu từ database và cache trong memory
 */
public class MercenaryManager {

    private static MercenaryManager instance;

    // Danh sách template được load từ DB
    private List<MercenaryTemplate> templates = new ArrayList<>();

    private MercenaryManager() {
    }

    public static MercenaryManager getInstance() {
        if (instance == null) {
            instance = new MercenaryManager();
        }
        return instance;
    }

    // Alias method
    public static MercenaryManager gI() {
        return getInstance();
    }

    /**
     * Load tất cả template từ database
     * Gọi method này khi server start
     */
    public void loadTemplates() {
        templates.clear();

        String sql = "SELECT * FROM mercenary_template WHERE active = 1 ORDER BY planet_type, id";

        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MercenaryTemplate template = new MercenaryTemplate();
                template.setId(rs.getInt("id"));
                template.setName(rs.getString("name"));
                template.setPlanetType(rs.getInt("planet_type"));
                template.setPrice30Min(rs.getInt("price_30min"));
                template.setPrice1Hour(rs.getInt("price_1hour"));
                template.setPrice5Hour(rs.getInt("price_5hour"));
                template.setHp(rs.getLong("hp"));
                template.setMp(rs.getLong("mp"));
                template.setDame(rs.getLong("dame"));
                template.setDef(rs.getInt("def"));
                template.setCrit(rs.getInt("crit"));
                template.setHead(rs.getInt("head"));
                template.setBody(rs.getInt("body"));
                template.setLeg(rs.getInt("leg"));
                template.setGender(rs.getInt("gender"));
                template.setCanAttackBoss(rs.getBoolean("can_attack_boss"));
                template.setActive(rs.getBoolean("active"));

                templates.add(template);
            }

            System.out.println("[MercenaryManager] Loaded " + templates.size() + " mercenary templates");

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("active")) { // Filter SQL errors related to missing columns/tables if
                                                         // possible, or just log simpler
                System.out.println("[MercenaryManager] Table 'mercenary_template' not found. Skipping mercenary load.");
            } else {
                System.out.println("[MercenaryManager] Error loading templates: " + e.getMessage());
            }
        }
    }

    /**
     * Lấy tất cả template
     */
    public List<MercenaryTemplate> getAllTemplates() {
        return new ArrayList<>(templates);
    }

    /**
     * Lấy template theo ID
     */
    public MercenaryTemplate getTemplateById(int id) {
        for (MercenaryTemplate template : templates) {
            if (template.getId() == id) {
                return template;
            }
        }
        return null;
    }

    /**
     * Lấy danh sách template theo hành tinh
     * 
     * @param planetType 0: Trái Đất, 1: Xayda, 2: Namec
     */
    public List<MercenaryTemplate> getTemplatesByPlanet(int planetType) {
        List<MercenaryTemplate> result = new ArrayList<>();
        for (MercenaryTemplate template : templates) {
            if (template.getPlanetType() == planetType) {
                result.add(template);
            }
        }
        return result;
    }

    /**
     * Lấy danh sách template có thể đánh boss
     */
    public List<MercenaryTemplate> getBossHunterTemplates() {
        List<MercenaryTemplate> result = new ArrayList<>();
        for (MercenaryTemplate template : templates) {
            if (template.isCanAttackBoss()) {
                result.add(template);
            }
        }
        return result;
    }

    /**
     * Reload template từ database (dùng khi cần cập nhật runtime)
     */
    public void reloadTemplates() {
        loadTemplates();
    }

    /**
     * Kiểm tra xem đã load template chưa
     */
    public boolean isLoaded() {
        return !templates.isEmpty();
    }

    /**
     * Lấy số lượng template
     */
    public int getTemplateCount() {
        return templates.size();
    }

    /**
     * Lấy danh sách template KHÔNG đánh boss (lính thường)
     */
    public List<MercenaryTemplate> getNormalTemplates() {
        List<MercenaryTemplate> result = new ArrayList<>();
        for (MercenaryTemplate template : templates) {
            if (!template.isCanAttackBoss()) {
                result.add(template);
            }
        }
        return result;
    }

    /**
     * Lấy danh sách template theo khả năng đánh boss
     * 
     * @param canAttackBoss true: lính đánh boss, false: lính thường
     */
    public List<MercenaryTemplate> getTemplatesByBossAbility(boolean canAttackBoss) {
        if (canAttackBoss) {
            return getBossHunterTemplates();
        } else {
            return getNormalTemplates();
        }
    }

    /**
     * Random chọn một template theo khả năng đánh boss
     * 
     * @param canAttackBoss true: random lính đánh boss, false: random lính thường
     * @return Template ngẫu nhiên hoặc null nếu không có template phù hợp
     */
    public MercenaryTemplate getRandomTemplate(boolean canAttackBoss) {
        List<MercenaryTemplate> validTemplates = getTemplatesByBossAbility(canAttackBoss);
        if (validTemplates.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * validTemplates.size());
        return validTemplates.get(randomIndex);
    }
}
