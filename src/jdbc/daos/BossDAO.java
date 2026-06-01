package jdbc.daos;
import boss.BossConfig;


import jdbc.DBConnecter;
import jdbc.NDVResultSet;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO class để load cấu hình boss từ database.
 */
public class BossDAO {

    private static final String QUERY_ALL = "SELECT * FROM boss_config WHERE enabled = 1 ORDER BY boss_id, level_index";

    /**
     * Load tất cả boss config từ database.
     */
    public static List<BossConfig> loadAllBossConfigs() {
        List<BossConfig> configs = new ArrayList<>();
        try {
            NDVResultSet rs = DBConnecter.executeQuery(QUERY_ALL);
            while (rs.next()) {
                BossConfig config = new BossConfig();

                // Thông tin cơ bản
                config.setBossId(getInt(rs, "boss_id", 0));
                config.setName(getStr(rs, "boss_name", ""));

                config.setGender((byte) getInt(rs, "gender", 0));
                config.setOutfit(getStr(rs, "outfit", "0,0,0,-1,-1,-1"));
                config.setDame(getLong(rs, "dame", 0));
                config.setHp(getStr(rs, "hp", "1000"));
                config.setMapJoin(getStr(rs, "map_join", "0"));
                config.setSkillTemp(getStr(rs, "skill_temp", "[]"));
                config.setTextS(getStr(rs, "text_s", "[]"));
                config.setTextM(getStr(rs, "text_m", "[]"));
                config.setTextE(getStr(rs, "text_e", "[]"));
                config.setSecondsRest(getInt(rs, "seconds_rest", 600));
                config.setAppearType(getInt(rs, "appear_type", 0));
                config.setBossesAppearTogether(getStr(rs, "bosses_appear_together", null));
                config.setLevelIndex(getInt(rs, "level_index", 0));

                // Boss Type & Spawn Config
                config.setBossType(getStr(rs, "boss_type", "DEFAULT"));
                config.setNotifyDisabled(getInt(rs, "is_notify_disabled", 0) == 1);
                config.setZone01SpawnDisabled(getInt(rs, "is_zone01_spawn_disabled", 0) == 1);
                config.setSpawnCount(getInt(rs, "spawn_count", 1));

                // Injured behavior
                config.setMaxDamagePerHit(getLongNullable(rs, "max_damage_per_hit"));
                config.setDamageDivisor(getIntNullable(rs, "damage_divisor"));
                config.setDamageFlatReduction(getLongNullable(rs, "damage_flat_reduction"));
                config.setDodgeRate(getIntNullable(rs, "dodge_rate"));
                config.setPierceReverse(getInt(rs, "pierce_reverse", 0) == 1);

                // Auto leave map
                config.setAutoLeaveTimeout(getLongNullable(rs, "auto_leave_timeout"));
                config.setAutoLeaveResetOnPlayer(getInt(rs, "auto_leave_reset_on_player", 0) == 1);
                config.setAutoLeaveRandomMin(getLongNullable(rs, "auto_leave_random_min"));
                config.setAutoLeaveRandomMax(getLongNullable(rs, "auto_leave_random_max"));

                // Join map
                config.setAppendRandomName(getInt(rs, "append_random_name", 0) == 1);

                // Chat behavior
                config.setDoneChatSToAfk(getInt(rs, "done_chat_s_to_afk", 0) == 1);
                config.setSkipNotifyAtLevel(getIntNullable(rs, "skip_notify_at_level"));
                config.setSkipMoveAtLevel(getIntNullable(rs, "skip_move_at_level"));

                // Special abilities & Reward
                config.setSpecialAbilities(getStr(rs, "special_abilities", null));
                config.setRewardConfig(getStr(rs, "reward_config", null));

                // Custom class
                config.setCustomClass(getStr(rs, "custom_class", null));
                config.setEnabled(true);

                configs.add(config);
            }
            rs.dispose();
            Logger.log("\u001b[0;32m", "Loaded " + configs.size() + " boss configs from database\n");
        } catch (Exception e) {
            Logger.error("Lỗi load boss config từ database: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        return configs;
    }

    // === Helper methods an toàn cho NDVResultSet (không có wasNull) ===

    private static String getStr(NDVResultSet rs, String column, String defaultVal) {
        try {
            Object obj = rs.getObject(column);
            if (obj == null) return defaultVal;
            String val = String.valueOf(obj);
            if ("null".equals(val)) return defaultVal;
            return val;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static int getInt(NDVResultSet rs, String column, int defaultVal) {
        try {
            Object obj = rs.getObject(column);
            if (obj == null) return defaultVal;
            return ((Number) obj).intValue();
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static long getLong(NDVResultSet rs, String column, long defaultVal) {
        try {
            Object obj = rs.getObject(column);
            if (obj == null) return defaultVal;
            return ((Number) obj).longValue();
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static Integer getIntNullable(NDVResultSet rs, String column) {
        try {
            Object obj = rs.getObject(column);
            if (obj == null) return null;
            return ((Number) obj).intValue();
        } catch (Exception e) {
            return null;
        }
    }

    private static Long getLongNullable(NDVResultSet rs, String column) {
        try {
            Object obj = rs.getObject(column);
            if (obj == null) return null;
            return ((Number) obj).longValue();
        } catch (Exception e) {
            return null;
        }
    }
}