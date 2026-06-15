package tutien;

public class CanhGioi {

    // ── Realm ID constants ────────────────────────────────────────────────────
    public static final int LUYEN_KHI  = 1;   // 炼气  Qi Refining
    public static final int TRUC_CO    = 2;   // 筑基  Foundation Establishment
    public static final int KET_TINH   = 3;   // 结晶  Crystallization
    public static final int KIM_DAN    = 4;   // 金丹  Golden Core
    public static final int CU_LINH    = 5;   // 具灵  Spirit Fusion
    public static final int NGUYEN_ANH = 6;   // 元婴  Nascent Soul
    public static final int HOA_THAN   = 7;   // 化神  Soul Formation
    public static final int NGO_DAO    = 8;   // 悟道  Enlightenment
    public static final int VU_HOA     = 9;   // 羽化  Ascension
    public static final int DANG_TIEN  = 10;  // 登仙  Immortal Ascension

    public static final int COUNT = 10;

    // ── Sub-stage constants (for realms 2–10) ─────────────────────────────────
    public static final int SO       = 1;  // 初 Early
    public static final int TRUNG    = 2;  // 中 Middle
    public static final int THUONG   = 3;  // 上 Late
    public static final int VIEN_MAN = 4;  // 圓滿 Peak / Perfection

    // Luyện Khí: tầng 14/15/16 = Phá Cảnh 1/2/3 (+10% chỉ số cơ bản, +20 năm thọ mỗi lần)
    public static final int LK_PHA_CANH_START = 14;

    // ── Number of sub-stages per realm ───────────────────────────────────────
    // Luyện Khí has 16 numbered tầng (13 thường + 3 Phá Cảnh); others have 4 named stages.
    public static final int[] STAGES = {
        0,   // 0  unused
        16,  // 1  Luyện Khí  : Tầng 1 – 13, Phá Cảnh 14 – 16
        4,   // 2  Trúc Cơ    : Sơ / Trung / Thượng / Viên Mãn
        4,   // 3  Kết Tinh
        4,   // 4  Kim Đan
        4,   // 5  Cụ Linh
        4,   // 6  Nguyên Anh
        4,   // 7  Hóa Thần
        4,   // 8  Ngộ Đạo
        4,   // 9  Vũ Hóa
        4,   // 10 Đăng Tiên
    };

    // ── Realm display names ───────────────────────────────────────────────────
    public static final String[] NAMES = {
        "",
        "Luyện Khí",   // 1
        "Trúc Cơ",     // 2
        "Kết Tinh",    // 3
        "Kim Đan",     // 4
        "Cụ Linh",     // 5
        "Nguyên Anh",  // 6
        "Hóa Thần",    // 7
        "Ngộ Đạo",     // 8
        "Vũ Hóa",      // 9
        "Đăng Tiên",   // 10
    };

    // ── Sub-stage names ───────────────────────────────────────────────────────
    // Luyện Khí uses "Tầng N"; realms 2-10 use the 4-name array.
    private static final String[] STAGE_NAMES_OTHER = {
        "", "Sơ", "Trung", "Thượng", "Viên Mãn"
    };

    // Giai đoạn Luyện Khí theo tầng (req docs/req/req.md §4-§5)
    private static final String[] LK_GIAI_DOAN = { "Sơ Kỳ", "Trung Kỳ", "Hậu Kỳ" };

    // ── Max tu vi per sub-stage [realm][0-based stage index] ──────────────────
    // Reaching this value within the current stage allows advancing to the next.
    // Last stage of Đăng Tiên has no cap (Long.MAX_VALUE).
    public static final long[][] MAX_TU_VI_PER_STAGE = {
        {},                                                                          // 0  unused
        {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
         1000, 1000, 1000, 2000, 2500, 3000},                                        // 1  Luyện Khí  (tầng 1-13 = 1000/tầng — tăng độ khó | Phá Cảnh: 2000/2500/3000)
        {2_500L,          2_500L,          2_500L,          2_500L},                 // 2  Trúc Cơ
        {25_000L,         25_000L,         25_000L,         25_000L},                // 3  Kết Tinh
        {250_000L,        250_000L,        250_000L,        250_000L},               // 4  Kim Đan
        {2_500_000L,      2_500_000L,      2_500_000L,      2_500_000L},             // 5  Cụ Linh
        {25_000_000L,     25_000_000L,     25_000_000L,     25_000_000L},            // 6  Nguyên Anh
        {250_000_000L,    250_000_000L,    250_000_000L,    250_000_000L},           // 7  Hóa Thần
        {2_500_000_000L,  2_500_000_000L,  2_500_000_000L,  2_500_000_000L},        // 8  Ngộ Đạo
        {25_000_000_000L, 25_000_000_000L, 25_000_000_000L, 25_000_000_000L},       // 9  Vũ Hóa
        {25_000_000_000L, 25_000_000_000L, 25_000_000_000L, Long.MAX_VALUE},        // 10 Đăng Tiên (Viên Mãn = no cap)
    };

    // Cumulative max tu vi to reach each realm (sum of all stages below).
    public static final long[] MAX_TU_VI = {
        0L,
        20_500L,              // 1  Luyện Khí       (Σ tầng 1-13 = 13 000 + Phá Cảnh 7 500)
        10_000L,              // 2  Trúc Cơ         (4 × 2500)
        100_000L,             // 3  Kết Tinh        (4 × 25000)
        1_000_000L,           // 4  Kim Đan         (4 × 250000)
        10_000_000L,          // 5  Cụ Linh
        100_000_000L,         // 6  Nguyên Anh
        1_000_000_000L,       // 7  Hóa Thần
        10_000_000_000L,      // 8  Ngộ Đạo
        100_000_000_000L,     // 9  Vũ Hóa
        Long.MAX_VALUE,       // 10 Đăng Tiên
    };

    // ── Helpers ───────────────────────────────────────────────────────────────
    public static String nameOf(int realm) {
        if (realm < 1 || realm > COUNT) return "";
        return NAMES[realm];
    }

    // Returns "Luyện Khí Tầng 3", "Kim Đan Trung", "Đăng Tiên Viên Mãn", etc.
    public static String fullNameOf(int realm, int stage) {
        if (realm < 1 || realm > COUNT) return "";
        String stageName = stageNameOf(realm, stage);
        return stageName.isEmpty() ? NAMES[realm] : NAMES[realm] + " " + stageName;
    }

    public static String stageNameOf(int realm, int stage) {
        if (realm == LUYEN_KHI) {
            if (stage < 1 || stage > 16) return "";
            return "Tầng " + stage + " - " + giaiDoanOf(realm, stage);
        }
        if (stage < 1 || stage > 4) return "";
        return STAGE_NAMES_OTHER[stage];
    }

    // "Sơ Kỳ" (1-3), "Trung Kỳ" (4-6), "Hậu Kỳ" (7-9), "Viên Mãn" (10-13), "Phá Cảnh 1-3" (14-16)
    public static String giaiDoanOf(int realm, int stage) {
        if (realm != LUYEN_KHI) return stageNameOf(realm, stage);
        if (stage < 1 || stage > 16) return "";
        if (stage >= LK_PHA_CANH_START) return "Phá Cảnh " + (stage - LK_PHA_CANH_START + 1);
        if (stage >= 10) return "Viên Mãn";
        return LK_GIAI_DOAN[(stage - 1) / 3];
    }

    public static boolean isPhaCanhStage(int realm, int stage) {
        return realm == LUYEN_KHI && stage >= LK_PHA_CANH_START;
    }

    public static int stageCount(int realm) {
        if (realm < 1 || realm > COUNT) return 0;
        return STAGES[realm];
    }

    public static long maxTuViPerStage(int realm, int stage) {
        if (realm < 1 || realm > COUNT) return 0L;
        int idx = stage - 1;
        if (idx < 0 || idx >= MAX_TU_VI_PER_STAGE[realm].length) return 0L;
        return MAX_TU_VI_PER_STAGE[realm][idx];
    }

    public static boolean isFinalRealm(int realm)  { return realm >= DANG_TIEN; }
    public static boolean isFinalStage(int realm, int stage) {
        return isFinalRealm(realm) && stage >= VIEN_MAN;
    }

    public static boolean canBreakthrough(int realm, int stage, long tuVi) {
        if (isFinalStage(realm, stage)) return false;
        return tuVi >= maxTuViPerStage(realm, stage);
    }

    // Advance to next stage; returns {newRealm, newStage}.
    public static int[] advance(int realm, int stage) {
        if (isFinalStage(realm, stage)) return new int[]{realm, stage};
        if (stage < STAGES[realm]) return new int[]{realm, stage + 1};
        return new int[]{Math.min(realm + 1, DANG_TIEN), 1};
    }
}
