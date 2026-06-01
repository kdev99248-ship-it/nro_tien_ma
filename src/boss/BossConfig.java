package boss;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.Map;
import utils.Logger;

/**
 * Model class ánh xạ với bảng boss_config trong database.
 */
@Data
public class BossConfig {

    private static final Gson gson = new Gson();

    private int bossId;
    private String name; // Renamed from bossName for consistency
    private byte gender;
    private String outfit;
    private long dame;
    private String hp;
    private String mapJoin;
    private String skillTemp;
    private String textS;
    private String textM;
    private String textE;
    private int secondsRest;
    private int appearType;
    private String bossesAppearTogether;
    private int levelIndex;
    private String bossType;
    private boolean notifyDisabled;
    private boolean zone01SpawnDisabled;
    private int spawnCount;
    private Long maxDamagePerHit;
    private Integer damageDivisor;
    private Long damageFlatReduction;
    private Integer dodgeRate;
    private boolean pierceReverse;
    private Long autoLeaveTimeout;
    private boolean autoLeaveResetOnPlayer;
    private Long autoLeaveRandomMin;
    private Long autoLeaveRandomMax;
    private boolean appendRandomName;
    private boolean doneChatSToAfk;
    private Integer skipNotifyAtLevel;
    private Integer skipMoveAtLevel;
    private String specialAbilities;
    private String rewardConfig;
    private String customClass;
    private boolean enabled;

    // Explicit getters to avoid Lombok build issues in some environments
    public int getBossId() { return bossId; }
    public String getName() { return name; }
    public String getCustomClass() { return customClass; }
    public int getSpawnCount() { return spawnCount; }
    public int getLevelIndex() { return levelIndex; }
    
    public BossData[] getData() {
        // This is a helper for BossManager
        return new BossData[] { toBossData() }; 
    }

    public short[] parseOutfit() {
        String[] parts = outfit.split(",");
        short[] result = new short[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Short.parseShort(parts[i].trim());
        }
        return result;
    }

    public long[] parseHp() {
        String[] parts = hp.split(",");
        long[] result = new long[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Long.parseLong(parts[i].trim());
        }
        return result;
    }

    public int[] parseMapJoin() {
        String[] parts = mapJoin.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    public int[][] parseSkills() {
        if (skillTemp == null || skillTemp.isEmpty() || skillTemp.equals("[]")) {
            return new int[0][];
        }
        try {
            Type type = new TypeToken<int[][]>() {}.getType();
            return gson.fromJson(skillTemp, type);
        } catch (Exception e) {
            // Cứ cố gắng parse nếu là định dạng đơn giản [[id, level],...]
            try {
                return gson.fromJson(skillTemp, int[][].class);
            } catch (Exception e2) {
                Logger.error("Lỗi parse skill_temp cho boss: " + name + " - " + skillTemp + "\n");
                return new int[0][];
            }
        }
    }

    public String[] parseTextS() { return parseTextArray(textS); }
    public String[] parseTextM() { return parseTextArray(textM); }
    public String[] parseTextE() { return parseTextArray(textE); }

    private String[] parseTextArray(String json) {
        if (json == null || json.isEmpty() || json.equals("[]")) {
            return new String[0];
        }
        Type type = new TypeToken<String[]>() {}.getType();
        return gson.fromJson(json, type);
    }

    public int[] parseBossesAppearTogether() {
        if (bossesAppearTogether == null || bossesAppearTogether.isEmpty()) {
            return null;
        }
        String[] parts = bossesAppearTogether.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    public AppearType parseAppearType() {
        return switch (appearType) {
            case 1 -> AppearType.APPEAR_WITH_ANOTHER;
            case 2 -> AppearType.ANOTHER_LEVEL;
            case 3 -> AppearType.CALL_BY_ANOTHER;
            default -> AppearType.DEFAULT_APPEAR;
        };
    }

    public BossType parseBossType() {
        if (bossType == null || bossType.isEmpty() || bossType.equals("DEFAULT")) {
            return null;
        }
        try {
            return BossType.valueOf(bossType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public BossData toBossData() {
        short[] outfitArr = parseOutfit();
        long[] hpArr = parseHp();
        int[] mapJoinArr = parseMapJoin();
        int[][] skillArr = parseSkills();
        String[] textSArr = parseTextS();
        String[] textMArr = parseTextM();
        String[] textEArr = parseTextE();
        AppearType at = parseAppearType();
        int[] bossesTogetherArr = parseBossesAppearTogether();

        if (bossesTogetherArr != null) {
            return new BossData(name, gender, outfitArr, dame, hpArr, mapJoinArr, skillArr, textSArr, textMArr, textEArr, secondsRest, bossesTogetherArr);
        } else if (at != AppearType.DEFAULT_APPEAR && secondsRest > 0) {
            return new BossData(name, gender, outfitArr, dame, hpArr, mapJoinArr, skillArr, textSArr, textMArr, textEArr, secondsRest, at);
        } else if (at != AppearType.DEFAULT_APPEAR) {
            return new BossData(name, gender, outfitArr, dame, hpArr, mapJoinArr, skillArr, textSArr, textMArr, textEArr, at);
        } else {
            return new BossData(name, gender, outfitArr, dame, hpArr, mapJoinArr, skillArr, textSArr, textMArr, textEArr, secondsRest);
        }
    }

    public Map<String, Object> parseSpecialAbilities() {
        if (specialAbilities == null || specialAbilities.isEmpty() || specialAbilities.equals("{}")) {
            return null;
        }
        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(specialAbilities, type);
        } catch (Exception e) {
            Logger.error("Lỗi parse special_abilities cho boss: " + name + " - " + specialAbilities + "\n");
            return null;
        }
    }

    public Map<String, Object> parseRewardConfig() {
        if (rewardConfig == null || rewardConfig.isEmpty() || rewardConfig.equals("{}")) {
            return null;
        }
        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(rewardConfig, type);
        } catch (Exception e) {
            Logger.error("Lỗi parse reward_config cho boss: " + name + " - " + rewardConfig + "\n");
            return null;
        }
    }
}

