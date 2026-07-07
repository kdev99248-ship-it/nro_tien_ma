package tutien;

public class TuTien {

    // ── Attribute ID constants (matches attributes.json) ──────────────────────
    public static final int ATTR_ALCHEMY_APT        = 1;
    public static final int ATTR_ALL_COMBAT_APT     = 2;
    public static final int ATTR_ALL_ROOTS          = 3;
    public static final int ATTR_ATK                = 4;
    public static final int ATTR_BACK_DAMAGE_TAKEN  = 5;
    public static final int ATTR_BODY_MASTERY_SPD   = 6;
    public static final int ATTR_CHARM              = 7;
    public static final int ATTR_COMBAT_APT         = 8;
    public static final int ATTR_COMPREHENSION      = 9;
    public static final int ATTR_CULTIVATION_GAIN   = 10;
    public static final int ATTR_DEF                = 11;
    public static final int ATTR_EARTH_ATTR         = 12;
    public static final int ATTR_EARTH_ROOT         = 13;
    public static final int ATTR_EVIL_PATH          = 14;
    public static final int ATTR_EVIL_PATH_VALUE    = 15;
    public static final int ATTR_FENG_SHUI          = 16;
    public static final int ATTR_FINGER_APT         = 17;
    public static final int ATTR_FIRE_ROOT          = 18;
    public static final int ATTR_FIST_APT           = 19;
    public static final int ATTR_GOOD_PATH          = 20;
    public static final int ATTR_GOOD_PATH_VALUE    = 21;
    public static final int ATTR_HEALTH             = 22;
    public static final int ATTR_HEALTH_MAX         = 23;
    public static final int ATTR_HEART_SHIELD       = 24;
    public static final int ATTR_HP                 = 25;
    public static final int ATTR_HP_MAX             = 26;
    public static final int ATTR_KICK_POWER         = 27;
    public static final int ATTR_KICK_RANGE         = 28;
    public static final int ATTR_LIFESPAN           = 29;
    public static final int ATTR_LIFESPAN_MAX       = 30;
    public static final int ATTR_LIGHTNING_ROOT     = 31;
    public static final int ATTR_LUCK               = 32;
    public static final int ATTR_MENTAL             = 33;
    public static final int ATTR_MENTAL_MAX         = 34;
    public static final int ATTR_MOOD_MAX           = 35;
    public static final int ATTR_MOVE_SPEED         = 36;
    public static final int ATTR_MP                 = 37;
    public static final int ATTR_MP_MAX             = 38;
    public static final int ATTR_OTHER_ROOTS        = 39;
    public static final int ATTR_PALM_APT           = 40;
    public static final int ATTR_PERCEPTION         = 41;
    public static final int ATTR_RANGED_ATK         = 42;
    public static final int ATTR_REFINING_APT       = 43;
    public static final int ATTR_REPUTATION         = 44;
    public static final int ATTR_ROOT_ATTR          = 45;
    public static final int ATTR_SABER_APT          = 46;
    public static final int ATTR_SABER_MASTERY_SPD  = 47;
    public static final int ATTR_SPEAR_APT          = 48;
    public static final int ATTR_SPEAR_MASTERY_SPD  = 49;
    public static final int ATTR_SPEED              = 50;
    public static final int ATTR_STAMINA            = 51;
    public static final int ATTR_STAMINA_MAX        = 52;
    public static final int ATTR_START_SPIRIT_STONES = 53;
    public static final int ATTR_SWORD_APT          = 54;
    public static final int ATTR_SWORD_MASTERY_SPD  = 55;
    public static final int ATTR_TALISMAN           = 56;
    public static final int ATTR_WATER_ROOT         = 57;
    public static final int ATTR_WIND_ROOT          = 58;
    public static final int ATTR_WOOD_ROOT          = 59;

    // ── Identity ──────────────────────────────────────────────────────────────
    public long   id;
    public String name;
    public byte   gender;
    public int    canhGioi;   // cultivation realm 1–10  (see CanhGioi)
    public int    tangGioi;   // sub-stage within realm  (Luyện Khí: 1–10 | others: 1=Sơ 2=Trung 3=Thượng 4=Viên Mãn)
    public long   tuVi;       // tu vi accumulated within current sub-stage

    // ── Combat ────────────────────────────────────────────────────────────────
    public int atk;               // Lực công kích           (#4)
    public int def;               // Lực phòng ngự           (#11)
    public int luck;              // May mắn                 (#32)
    public int perception;        // Hiểu ý                  (#41)
    public int charm;             // Mị lực                  (#7)
    public int comprehension;     // Ngộ tính                (#9)
    public int reputation;        // Danh vọng               (#44)
    public int kickPower;         // Cước lực                (#27)
    public int kickRange;         // Cước trình              (#28)
    public int rangedAtk;         // Công kích khoảng cách   (#42)
    public int moveSpeed;         // Tốc độ di chuyển        (#36)
    public int speed;             // Tốc độ                  (#50)
    public int heartShield;       // Hộ tâm                  (#24)
    public int backDamageTaken;   // Nhận tổn thương mặt sau (#5)  (%)

    // ── Path alignment ────────────────────────────────────────────────────────
    public int goodPath;          // Chính đạo               (#20)
    public int goodPathValue;     // Chính đạo giá trị       (#21)
    public int evilPath;          // Ma đạo                  (#14)
    public int evilPathValue;     // Ma đạo giá trị          (#15)

    // ── Resources (base & cap) ────────────────────────────────────────────────
    public int hp;                // Thể lực                 (#25)
    public int hpMax;             // Thể lực hạn mức cao nhất (#26)
    public int mp;                // Linh lực                (#37)
    public int mpMax;             // Linh lực hạn mức cao nhất (#38)
    public int stamina;           // Tinh lực                (#51)
    public int staminaMax;        // Tinh lực hạn mức cao nhất (#52)
    public int mental;            // Niệm lực                (#33)
    public int mentalMax;         // Niệm lực hạn mức cao nhất (#34)
    public int mood;              // M6: Tâm tình hiện tại (không có attr id; ảnh hưởng tốc độ tu luyện)
    public int moodMax;           // Tâm tình hạn mức cao nhất (#35)
    public int health;            // Khỏe mạnh               (#22)
    public int healthMax;         // Khỏe mạnh hạn mức cao nhất (#23)
    public int lifespan;          // Tuổi thọ                (#29)
    public int lifespanMax;       // Tuổi thọ hạn mức cao nhất (#30)

    // ── Weapon aptitudes ──────────────────────────────────────────────────────
    public int swordApt;          // Kiếm pháp tư chất       (#54)
    public int saberApt;          // Đao pháp tư chất        (#46)
    public int spearApt;          // Thương pháp tư chất     (#48)
    public int fistApt;           // Quyền pháp tư chất      (#19)
    public int fingerApt;         // Chỉ pháp tư chất        (#17)
    public int palmApt;           // Chưởng pháp tư chất     (#40)
    public int combatApt;         // Công pháp tư chất       (#8)
    public int allCombatApt;      // Tất cả công pháp tư chất (#2)

    // ── Weapon mastery speeds (%) ─────────────────────────────────────────────
    public int swordMasterySpd;   // Kiếm pháp thuần thục    (#55)
    public int saberMasterySpd;   // Đao pháp thuần thục     (#47)
    public int spearMasterySpd;   // Thương pháp thuần thục  (#49)
    public int bodyMasterySpd;    // Thân pháp thuần thục    (#6)

    // ── Spirit roots (linh căn) ───────────────────────────────────────────────
    public int lightningRoot;     // Lôi linh căn            (#31)
    public int fireRoot;          // Hỏa linh căn            (#18)
    public int waterRoot;         // Thủy linh căn           (#57)
    public int windRoot;          // Phong linh căn          (#58)
    public int woodRoot;          // Mộc linh căn            (#59)
    public int earthRoot;         // Thổ linh căn            (#13)
    public int earthAttr;         // Thổ thuộc tính          (#12)
    public int otherRoots;        // Cái khác linh căn tư chất (#39)
    public int rootAttr;          // Linh căn thuộc tính     (#45)
    public int allRoots;          // Tất cả linh căn tư chất (#3)

    // ── Crafting & misc ───────────────────────────────────────────────────────
    public int alchemyApt;        // Luyện đan tư chất       (#1)
    public int refiningApt;       // Luyện khí tư chất       (#43)
    public int fengShui;          // Phong thuỷ              (#16)
    public int talisman;          // Vẽ bùa                  (#56)
    public int cultivationGain;   // Thu hoạch được tu vi    (#10)  (%)
    public int startSpiritStones; // Bắt đầu linh thạch      (#53)

    // ── Trạng thái vòng lặp tu luyện M1 (KHÔNG rename — Gson persist theo tên) ─
    public volatile boolean disabled;  // hết tuổi thọ → vô hiệu hóa tu tiên (persist)
    public int  phaCanh;               // số lần Phá Cảnh đã qua 0–3 → NPoint +10%/lần (persist)
    public int  namTuTien;             // số năm tu tiên đã trôi (persist)
    public long msAccumYear;           // ms cộng dồn tới năm kế tiếp (persist)
    public long msAccumTuVi;           // ms hiệu dụng cộng dồn tới điểm tu vi kế tiếp (persist)
    public long tuKhiBuffMs;           // Tụ Khí Đan: real-ms còn lại x2 tốc độ tu luyện (persist)
    public int  khiVanId;              // M3 (legacy — chỉ để migrate sang khiVanIds; không dùng cho logic mới) (persist)
    public int[] khiVanIds;            // M3.5 Thiên Tiên Khí Vận: danh sách trait đang mang (số lượng theo cảnh giới) (persist)
    public int  khiVanRollCharges;     // M3.5: số lượt roll còn lại — mỗi đột phá cảnh giới nhỏ +1 (persist)
    public int[] congPhap;             // M4: 14 cấp bí kíp đã học (0 = chưa học; 0-6 linh căn, 7-13 võ học) (persist)
    public int[] congPhapExp;          // M4: 14 exp trong cấp hiện tại của từng bí kíp (persist)
    public long linhKhi;               // M4: linh khí tích lũy — tài nguyên header + nuôi exp công pháp (persist)
    public long msAccumLinhKhi;        // M4: ms hiệu dụng cộng dồn tới điểm linh khí kế tiếp (persist)
    public boolean moodInit;           // M6: đã khởi tạo tâm tình hiện tại chưa (migrate save cũ: false -> mood=moodMax) (persist)
    public int activeVoHoc;            // M7: võ học đang dùng (0=không; 1-6=Kiếm/Đao/Thương/Quyền/Chỉ/Chưởng -> congPhap[6+n]) (persist)
    public int  danSuLevel;            // M8: cấp Luyện Đan Sư (0 = save cũ -> migrate thành 1) (persist)
    public long danSuExp;              // M8: kinh nghiệm luyện đan trong cấp hiện tại (persist)
    public transient volatile boolean meditating; // đả tọa — transient, Gson không lưu
    public transient long lastTickMs;             // mốc tick trước — transient
    public transient long msAccumSpirit;          // M5: ms cộng dồn cho nhịp tiêu hao/hồi tinh lực & niệm lực — transient


    // ── Apply a stat delta by attribute ID ───────────────────────────────────
    public void applyStat(int attrId, int delta) {
        switch (attrId) {
            case ATTR_ALCHEMY_APT:         alchemyApt        += delta; break;
            case ATTR_ALL_COMBAT_APT:      allCombatApt      += delta; break;
            case ATTR_ALL_ROOTS:           allRoots          += delta; break;
            case ATTR_ATK:                 atk               += delta; break;
            case ATTR_BACK_DAMAGE_TAKEN:   backDamageTaken   += delta; break;
            case ATTR_BODY_MASTERY_SPD:    bodyMasterySpd    += delta; break;
            case ATTR_CHARM:               charm             += delta; break;
            case ATTR_COMBAT_APT:          combatApt         += delta; break;
            case ATTR_COMPREHENSION:       comprehension     += delta; break;
            case ATTR_CULTIVATION_GAIN:    cultivationGain   += delta; break;
            case ATTR_DEF:                 def               += delta; break;
            case ATTR_EARTH_ATTR:          earthAttr         += delta; break;
            case ATTR_EARTH_ROOT:          earthRoot         += delta; break;
            case ATTR_EVIL_PATH:           evilPath          += delta; break;
            case ATTR_EVIL_PATH_VALUE:     evilPathValue     += delta; break;
            case ATTR_FENG_SHUI:           fengShui          += delta; break;
            case ATTR_FINGER_APT:          fingerApt         += delta; break;
            case ATTR_FIRE_ROOT:           fireRoot          += delta; break;
            case ATTR_FIST_APT:            fistApt           += delta; break;
            case ATTR_GOOD_PATH:           goodPath          += delta; break;
            case ATTR_GOOD_PATH_VALUE:     goodPathValue     += delta; break;
            case ATTR_HEALTH:              health            += delta; break;
            case ATTR_HEALTH_MAX:          healthMax         += delta; break;
            case ATTR_HEART_SHIELD:        heartShield       += delta; break;
            case ATTR_HP:                  hp                += delta; break;
            case ATTR_HP_MAX:              hpMax             += delta; break;
            case ATTR_KICK_POWER:          kickPower         += delta; break;
            case ATTR_KICK_RANGE:          kickRange         += delta; break;
            case ATTR_LIFESPAN:            lifespan          += delta; break;
            case ATTR_LIFESPAN_MAX:        lifespanMax       += delta; break;
            case ATTR_LIGHTNING_ROOT:      lightningRoot     += delta; break;
            case ATTR_LUCK:                luck              += delta; break;
            case ATTR_MENTAL:              mental            += delta; break;
            case ATTR_MENTAL_MAX:          mentalMax         += delta; break;
            case ATTR_MOOD_MAX:            moodMax           += delta; break;
            case ATTR_MOVE_SPEED:          moveSpeed         += delta; break;
            case ATTR_MP:                  mp                += delta; break;
            case ATTR_MP_MAX:              mpMax             += delta; break;
            case ATTR_OTHER_ROOTS:         otherRoots        += delta; break;
            case ATTR_PALM_APT:            palmApt           += delta; break;
            case ATTR_PERCEPTION:          perception        += delta; break;
            case ATTR_RANGED_ATK:          rangedAtk         += delta; break;
            case ATTR_REFINING_APT:        refiningApt       += delta; break;
            case ATTR_REPUTATION:          reputation        += delta; break;
            case ATTR_ROOT_ATTR:           rootAttr          += delta; break;
            case ATTR_SABER_APT:           saberApt          += delta; break;
            case ATTR_SABER_MASTERY_SPD:   saberMasterySpd   += delta; break;
            case ATTR_SPEAR_APT:           spearApt          += delta; break;
            case ATTR_SPEAR_MASTERY_SPD:   spearMasterySpd   += delta; break;
            case ATTR_SPEED:               speed             += delta; break;
            case ATTR_STAMINA:             stamina           += delta; break;
            case ATTR_STAMINA_MAX:         staminaMax        += delta; break;
            case ATTR_START_SPIRIT_STONES: startSpiritStones += delta; break;
            case ATTR_SWORD_APT:           swordApt          += delta; break;
            case ATTR_SWORD_MASTERY_SPD:   swordMasterySpd   += delta; break;
            case ATTR_TALISMAN:            talisman          += delta; break;
            case ATTR_WATER_ROOT:          waterRoot         += delta; break;
            case ATTR_WIND_ROOT:           windRoot          += delta; break;
            case ATTR_WOOD_ROOT:           woodRoot          += delta; break;
        }
    }

    // ── Set a stat to an exact value (biến thành / khóa chặt vì) ─────────────
    public void setStatExact(int attrId, int value) {
        switch (attrId) {
            case ATTR_ALCHEMY_APT:         alchemyApt        = value; break;
            case ATTR_ALL_COMBAT_APT:      allCombatApt      = value; break;
            case ATTR_ALL_ROOTS:           allRoots          = value; break;
            case ATTR_ATK:                 atk               = value; break;
            case ATTR_BACK_DAMAGE_TAKEN:   backDamageTaken   = value; break;
            case ATTR_BODY_MASTERY_SPD:    bodyMasterySpd    = value; break;
            case ATTR_CHARM:               charm             = value; break;
            case ATTR_COMBAT_APT:          combatApt         = value; break;
            case ATTR_COMPREHENSION:       comprehension     = value; break;
            case ATTR_CULTIVATION_GAIN:    cultivationGain   = value; break;
            case ATTR_DEF:                 def               = value; break;
            case ATTR_EARTH_ATTR:          earthAttr         = value; break;
            case ATTR_EARTH_ROOT:          earthRoot         = value; break;
            case ATTR_EVIL_PATH:           evilPath          = value; break;
            case ATTR_EVIL_PATH_VALUE:     evilPathValue     = value; break;
            case ATTR_FENG_SHUI:           fengShui          = value; break;
            case ATTR_FINGER_APT:          fingerApt         = value; break;
            case ATTR_FIRE_ROOT:           fireRoot          = value; break;
            case ATTR_FIST_APT:            fistApt           = value; break;
            case ATTR_GOOD_PATH:           goodPath          = value; break;
            case ATTR_GOOD_PATH_VALUE:     goodPathValue     = value; break;
            case ATTR_HEALTH:              health            = value; break;
            case ATTR_HEALTH_MAX:          healthMax         = value; break;
            case ATTR_HEART_SHIELD:        heartShield       = value; break;
            case ATTR_HP:                  hp                = value; break;
            case ATTR_HP_MAX:              hpMax             = value; break;
            case ATTR_KICK_POWER:          kickPower         = value; break;
            case ATTR_KICK_RANGE:          kickRange         = value; break;
            case ATTR_LIFESPAN:            lifespan          = value; break;
            case ATTR_LIFESPAN_MAX:        lifespanMax       = value; break;
            case ATTR_LIGHTNING_ROOT:      lightningRoot     = value; break;
            case ATTR_LUCK:                luck              = value; break;
            case ATTR_MENTAL:              mental            = value; break;
            case ATTR_MENTAL_MAX:          mentalMax         = value; break;
            case ATTR_MOOD_MAX:            moodMax           = value; break;
            case ATTR_MOVE_SPEED:          moveSpeed         = value; break;
            case ATTR_MP:                  mp                = value; break;
            case ATTR_MP_MAX:              mpMax             = value; break;
            case ATTR_OTHER_ROOTS:         otherRoots        = value; break;
            case ATTR_PALM_APT:            palmApt           = value; break;
            case ATTR_PERCEPTION:          perception        = value; break;
            case ATTR_RANGED_ATK:          rangedAtk         = value; break;
            case ATTR_REFINING_APT:        refiningApt       = value; break;
            case ATTR_REPUTATION:          reputation        = value; break;
            case ATTR_ROOT_ATTR:           rootAttr          = value; break;
            case ATTR_SABER_APT:           saberApt          = value; break;
            case ATTR_SABER_MASTERY_SPD:   saberMasterySpd   = value; break;
            case ATTR_SPEAR_APT:           spearApt          = value; break;
            case ATTR_SPEAR_MASTERY_SPD:   spearMasterySpd   = value; break;
            case ATTR_SPEED:               speed             = value; break;
            case ATTR_STAMINA:             stamina           = value; break;
            case ATTR_STAMINA_MAX:         staminaMax        = value; break;
            case ATTR_START_SPIRIT_STONES: startSpiritStones = value; break;
            case ATTR_SWORD_APT:           swordApt          = value; break;
            case ATTR_SWORD_MASTERY_SPD:   swordMasterySpd   = value; break;
            case ATTR_TALISMAN:            talisman          = value; break;
            case ATTR_WATER_ROOT:          waterRoot         = value; break;
            case ATTR_WIND_ROOT:           windRoot          = value; break;
            case ATTR_WOOD_ROOT:           woodRoot          = value; break;
        }
    }
}
