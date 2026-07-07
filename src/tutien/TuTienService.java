package tutien;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;

import network.Message;
import player.Player;
import services.Service;
import utils.Logger;
import utils.Util;

// Service Tu Tien — M1 loi Luyen Khi Ky (thiet ke: docs/tutien-m1-plan.md, req: docs/req/req.md)
// Nguon su that: player.tuTien (null = chua kich hoat qua NPC Quy Lao).
// Persist: cot player.data_tutien (JSON Gson; field transient khong luu).
// Wire CMD 124 server->client: [byte action 0=mo panel|1=chi refresh][UTF canhGioi][UTF tang]
//   [byte cg][byte tg][long tuVi][long tuViMax][60 int (M6: them mood truoc moodMax)][byte meditating][byte disabled][byte phaCanh]
//   [byte kvRollCharges][byte kvSlots][byte kvHeld][kvHeld×{int id,UTF name,byte rarity,UTF desc}][14 byte congPhapLv]
//   [long linhKhi][byte congPhapCap][14x {int exp, int need}]   (M4: need=0 -> chua hoc hoac da max)
// Client->server: [byte action] 0=xin data, 1=toggle da toa, 2=dot pha, 3=roll khi van
public class TuTienService {

    public long lastTimeSendRefresh = System.currentTimeMillis();
    public static final int CMD_TUTIEN = 124;
    public long lastTimeHoiTamTinh = System.currentTimeMillis();

    // ── Tunables — chinh pacing tai day ───────────────────────────────────────
    public static final long YEAR_MS = 30L * 60 * 1000;          // 30 phut thuc = 1 nam tu tien (req §2)
    public static final long TU_VI_PER_YEAR = 100L;                     // diem tu vi / nam o toc do 100%
    public static final long MS_PER_TUVI = YEAR_MS / TU_VI_PER_YEAR; // 18_000 ms / diem
    public static final int LIFESPAN_START = 150;                      // tuoi tho khoi dau (req §3)
    public static final int LIFESPAN_GIAI_DOAN = 30;  // +nam khi vao Trung Ky/Hau Ky/Vien Man (tang 4/7/10)
    public static final int LIFESPAN_PHA_CANH = 20;  // +nam moi lan Pha Canh (tang 14/15/16)
    public static final long MAX_TICK_DELTA = 60_000L;                  // clamp chong burst sau lag/relog
    // ── Dan duoc M2 (req §8) ──────────────────────────────────────────────────
    public static final long TU_KHI_BUFF_MS = YEAR_MS;     // Tu Khi Dan: +100% toc do trong 1 nam tu tien
    public static final int BOI_NGUYEN_PERCENT = 30;          // Boi Nguyen Dan: +% cap tang hien tai (chia canh gioi)
    // Item id (item_template) cua 4 vien dan — xem sql/migration_m2_dan_duoc.sql
    public static final short ITEM_TU_KHI = 1998;
    public static final short ITEM_BOI_NGUYEN = 1999;
    public static final short ITEM_NGUYEN_LINH = 2000;
    public static final short ITEM_HOAN_HON = 2001;
    // ── M3 Thien Tien Khi Van (req §12) ───────────────────────────────────────
    // trong so roll theo rarity 1..5 (cang cao cang hiem); rarity 6 (Tien) chua co data
    public static final int[] RARITY_WEIGHT = {0, 50, 30, 14, 5, 1};
    public static final int ROLL_COST_GOLD = 0; // 0 = mien phi (test). Doi gia roll khi van tai day
    // ── M4 Cong Phap & Vo Hoc (req §6-7) — 14 bi kip: idx 0-6 cong phap linh can, 7-13 vo hoc pho ─
    public static final int ROOT_PER_LV = 5; // cong phap chuyen he: +linh can / level
    public static final int APT_PER_LV = 5; // vo hoc chuyen mon: +tu chat / level
    public static final int BACH_PER_LV = 2; // Bach Phap/Bach Pho: thap hon (req)
    // Tran cap bi kip theo canh gioi: Luyen Khi=2, moi canh gioi +2 (M7 user: LK chi toi Lv2; Lv3 mo o Truc Co)
    public static final int CONG_PHAP_LV_PER_REALM = 2;
    public static final int CONG_PHAP_EXP_BASE = 2000; // exp len cap L->L+1 = BASE*L (Lv1->5 = 1000)
    // Linh khi hap thu: 100 diem/nam @x1 (= tu vi). Moi diem linh khi = +1 exp cho moi bi kip DA hoc.
    public static final long LINH_KHI_PER_YEAR = 100L;
    public static final long MS_PER_LINH_KHI = YEAR_MS / LINH_KHI_PER_YEAR;
    // 14 bi tich cong phap = item_template 2002..2015 (idx 0-13); icon 32472..32485 (migration_m4_cong_phap.sql)
    public static final short ITEM_CONG_PHAP_BASE = 2002;
    // attr id 6 cong phap linh can (idx 0-5) va 6 vo hoc pho (idx 7-12)
    private static final int[] CONG_PHAP_ROOT = {
            TuTien.ATTR_LIGHTNING_ROOT, TuTien.ATTR_FIRE_ROOT, TuTien.ATTR_WATER_ROOT,
            TuTien.ATTR_WIND_ROOT, TuTien.ATTR_WOOD_ROOT, TuTien.ATTR_EARTH_ROOT
    };
    private static final int[] VO_HOC_APT = {
            TuTien.ATTR_SWORD_APT, TuTien.ATTR_SABER_APT, TuTien.ATTR_SPEAR_APT,
            TuTien.ATTR_FIST_APT, TuTien.ATTR_FINGER_APT, TuTien.ATTR_PALM_APT
    };
    // ten 14 bi kip cho thong bao (khop client TuTienScr CONG_PHAP[0-6] + VO_HOC[7-13])
    private static final String[] CONG_PHAP_NAME = {
            "Lôi Pháp", "Hỏa Pháp", "Thủy Pháp", "Phong Pháp", "Mộc Pháp", "Thổ Pháp", "Bách Pháp",
            "Kiếm Phổ", "Đao Phổ", "Thương Phổ", "Quyền Phổ", "Chỉ Phổ", "Chưởng Phổ", "Bách Phổ"
    };

    // ── M7: Công pháp SINH tu vi & linh khí (req: HỆ THỐNG CÔNG PHÁP §III-VI) ────────────────────
    // §III GATE: chưa học công pháp linh căn nào (idx 0-5) -> tu vi & linh khí nhận = 0.
    // §IV "lực tu luyện" mỗi cấp công pháp (idx = cấp 1..10; cấp >10 nội suy 30 + 5/cấp).
    //   LƯU Ý: bảng req là số TƯƠNG ĐỐI (trọng số), KHÔNG phải tu vi/giây tuyệt đối — nếu dùng thẳng
    //   sẽ phá vỡ nhịp 30'/năm (cap tầng 1000..25 tỷ) đã dựng từ M1. Ở đây chuẩn hóa thành % tốc độ.
    private static final int[] CULT_POWER_PER_LV = {0, 1, 2, 3, 4, 6, 8, 11, 15, 20, 30};
    // §V hiệu suất tu nhiều pháp (idx = số pháp đang tu 0..6). req tới 5 (100..20); idx 6 thêm SÀN 15
    //   (vá lỗi req: công thức gốc 1-0.2*(n-1) = 0% ở 6 pháp, ÂM ở 7). 6 nguyên tố + Bách Pháp = 7 khả năng.
    private static final int[] MULTI_PHAP_PCT = {0, 100, 80, 60, 40, 20, 15};
    // Chuẩn hóa "lực tu luyện × heSoLinhCan" -> % tốc độ. 260 = 1 pháp Lv2 (lực 2, LK cap mới) × heSoLinhCan
    //   root 30 (= 2×130) -> ~100% (≈ baseline nhịp cũ cho 1 cultivator đơn căn đã max pháp ở Luyện Khí).
    //   (Trước: 780 cho Lv5 — sai sau khi LK cap = Lv2; đầu game sẽ chậm ~3 lần nếu giữ 780.)
    public static final int CULT_POWER_NORM = 260;

    // ── M5: chỉ số cơ thể ban đầu (thể lực / khỏe mạnh / linh lực / tinh lực / niệm lực) ─────────
    // Thể lực tối đa (hpMax) & khỏe mạnh (health): random — ảnh hưởng tuổi thọ + linh căn ban đầu
    public static final int THE_LUC_MIN = 80, THE_LUC_MAX = 120; // hpMax (thể lực tối đa)
    public static final int KHOE_MANH_MIN = 60, KHOE_MANH_MAX = 120; // health (khỏe mạnh)
    public static final int THE_LUC_PER_ROOT = 12; // cứ THE_LUC_PER_ROOT thể lực trên MIN -> +1 mỗi linh căn
    public static final int LIFESPAN_PER_KHOE_MANH = 2; // (health-MIN)/2  -> tối đa +50 năm
    public static final int LIFESPAN_PER_THE_LUC = 4; // (hpMax-MIN)/4   -> tối đa +30 năm
    // Tinh lực (stamina) & niệm lực (mental): pool tiêu hao khi đả tọa, hồi khi nghỉ
    public static final int TINH_NIEM_MIN = 80, TINH_NIEM_MAX = 160;
    public static final long MS_PER_SPIRIT = 5_000L; // mỗi 5s thực = 1 nhịp tiêu hao/hồi tinh & niệm lực
    public static final int SPIRIT_DRAIN = 2;      // đả tọa: -SPIRIT_DRAIN tinh & niệm lực / nhịp
    public static final int SPIRIT_REGEN = 1;      // nghỉ:   +SPIRIT_REGEN tinh & niệm lực / nhịp
    // Linh lực tối đa (mpMax) tăng theo cảnh giới tu tiên (+ mỗi tầng nhỏ)
    public static final int MP_BASE = 100, MP_PER_REALM = 100, MP_PER_TANG = 10;
    // Linh căn random theo bậc phẩm chất: CAO thì HIẾM, THẤP thì NHIỀU (tổng trọng số = 100 -> đọc thẳng ra %)
    private static final int[] LINH_CAN_W = {50, 20, 14, 6, 1};    // % rơi vào mỗi bậc
    private static final int[] LINH_CAN_LO = {1, 21, 41, 61, 81};   // cận dưới mỗi bậc
    private static final int[] LINH_CAN_HI = {20, 40, 60, 80, 100}; // cận trên mỗi bậc
    // ── M6: Tâm tình (mood) + tỷ lệ đột phá theo thiên phú (phẩm chất linh căn) ─────────────────
    public static final int MOOD_DECAY_PER_YEAR_PCT = 10; // mỗi năm tu tiên -10% tâm tình tối đa
    public static final int MOOD_REGEN_PER_YEAR_PCT = 5;  // mỗi năm +5% (net -5%/năm) — đột phá để bù
    public static final int MOOD_DOT_PHA_FAIL_PCT = 20; // đột phá thất bại -20% tâm tình
    public static final int MOOD_DOT_PHA_SUCCESS_PCT = 50; // đột phá thành công +50% tâm tình
    public static final int MOOD_LOW_PCT = 30; // tâm tình < 30% -> cảnh báo người chơi
    // Tỷ lệ đột phá: nền + bonus theo phẩm chất linh căn (ngưỡng user: phế<10, trung~30, thượng>50, cực>80)
    public static final int BASE_DOT_PHA_PCT = 50; // tỷ lệ đột phá nền (Luyện Khí cảnh giới nhỏ đầu tiên)
    public static final int DOT_PHA_DROP_PER_STAGE = 5;  // -5% tỷ lệ mỗi cảnh giới NHỎ (tầng) tăng lên
    public static final int DOT_PHA_MIN_PCT = 10; // sàn tỷ lệ cho cảnh giới nhỏ thường
    public static final int PHA_CANH_BASE_PCT = 5;  // Phá Cảnh: nền RIÊNG 5% (bức tường khó nhất)
    public static final int DOT_PHA_FAIL_TUVI_PCT = 30; // thất bại: mất 30% tu vi (+ mất HẾT linh khí)
    // Đột phá THÀNH CÔNG: tăng NHẸ thuộc tính chiến đấu (đa số scale %/điểm nên giữ nhỏ; lệch theo rate để không stat nào nổ; tunable)
    private static final int DP_GAIN_ATK = 2, DP_GAIN_DEF = 2, DP_GAIN_LUCK = 1, DP_GAIN_PERCEPTION = 1, DP_GAIN_CHARM = 1;
    private static final int DP_GAIN_COMPREHENSION = 1, DP_GAIN_REPUTATION = 1, DP_GAIN_SPEED = 1, DP_GAIN_HEART_SHIELD = 2, DP_GAIN_BACK_DMG = 1;
    public static final int ROOT_THUONG_PHAM = 50; // > 50 = thượng phẩm linh căn
    public static final int ROOT_CUC_PHAM = 80; // > 80 = cực phẩm linh căn
    public static final int TIEN_PHAM_ROOTS = 5;  // >= 5 căn đều > 80 = tiên phẩm (ngũ hành)
    public static final int TALENT_THUONG_PHAM = 20; // 1-2 căn > 50      -> +20%
    public static final int TALENT_CUC_PHAM = 25; // đơn linh căn > 80 -> +25%
    public static final int TALENT_TIEN_PHAM = 30; // ngũ hành > 80     -> +30%
    // M6: bonus % tốc độ TU VI + LINH KHÍ theo bậc linh căn (index = linhCanTier 0..3). Thiên phú cao -> tu nhanh hơn.
    private static final int[] TALENT_GAIN_PCT = {0, 20, 30, 50}; // Tạp / Thượng / Cực / Tiên
    private static final String[] LINH_CAN_TIER_NAME = {
            "Tạp Linh Căn", "Thượng Phẩm Linh Căn", "Cực Phẩm Linh Căn (Đơn)", "Tiên Phẩm Linh Căn (Ngũ Hành)"
    };

    // ── M8: Luyện Đan (luyện đan sư + đan phương) ───────────────────────────────
    public static final int DAN_SU_LEVEL_MIN = 1;     // ai cũng bắt đầu cấp 1
    public static final int DAN_SU_LEVEL_MAX = 99;    // trần cấp (chống overflow exp)
    public static final int DAN_BASE_RATE    = 15;    // % thành công khi cấp đan sư == req_level (req user)
    public static final int DAN_RATE_PER_LV  = 30;    // +%/cấp đan sư VƯỢT req_level (req user)
    public static final int DAN_RATE_CAP     = 100;   // trần tỷ lệ thành công
    // EXP luyện đan: thành công nhiều, thất bại ít; cả hai tỉ lệ với req_level (đan cấp cao cho nhiều exp)
    public static final int DAN_EXP_SUCCESS_FLAT    = 20;
    public static final int DAN_EXP_SUCCESS_PER_REQ = 15;
    public static final int DAN_EXP_FAIL_FLAT       = 6;
    public static final int DAN_EXP_FAIL_PER_REQ    = 4;
    // EXP cần lên cấp: tăng dần theo cấp (cấp cao grind lâu hơn). need(L) = BASE × L.
    public static final int DAN_EXP_BASE = 100;
    // Random số lượng đan khi thành công (1..10): cấp đan sư vượt req → dễ ra nhiều; cảnh giới cao → hiếm ra nhiều
    public static final int DAN_QTY_MIN = 1, DAN_QTY_MAX = 10;
    public static final int DAN_QTY_STEP_BASE    = 42;   // % qua mỗi "cửa" lên +1 viên (cần qua q-1 cửa để đạt q viên)
    public static final int DAN_QTY_PER_SURPLUS  = 12;   // +%/cấp đan sư vượt req_level
    public static final int DAN_QTY_PER_REALM    = 7;    // −%/cảnh giới (realm−1) → đan cao hiếm ra nhiều
    public static final int DAN_QTY_STEP_MIN = 3, DAN_QTY_STEP_MAX = 95;

    // ── M9: Tien Duyen (gating) + chi phi Linh Thach (dot pha / luyen dan) ───────
    // Tien Duyen = item 2039 (tien te MO KHOA), roi tu quai 1/1000 & boss 1/20, moi proc 1-3 vien.
    public static final short ITEM_TIEN_DUYEN = 2039;
    public static final int TIEN_DUYEN_HOC_TU_TIEN   = 150; // can 150 tien duyen de hoc Tu Tien
    public static final int TIEN_DUYEN_HOC_LUYEN_DAN = 100; // can 100 tien duyen de hoc Luyen Dan
    public static final int LUYEN_DAN_REQ_TANG       = 4;   // + phai dat Luyen Khi Tang 4 moi hoc Luyen Dan
    public static final int TIEN_DUYEN_MOB_DROP_1_IN  = 1000; // ty le roi tu quai = 1/1000
    public static final int TIEN_DUYEN_BOSS_DROP_1_IN = 20;   // ty le roi tu boss = 1/20
    public static final int TIEN_DUYEN_DROP_MIN = 1, TIEN_DUYEN_DROP_MAX = 3; // so luong roi moi proc
    // Linh Thach (item 2016) chi phi: dot pha = tang HIEN TAI x3 (tru khi bam, du thanh/bai); luyen dan = req_level x1.
    public static final int DOT_PHA_LINH_THACH_PER_TANG  = 3;
    public static final int LUYEN_DAN_LINH_THACH_PER_REQ = 1;

    private static TuTienService instance;
    private static final Gson GSON = new Gson();
    private static final Random ROLL_RNG = new Random();

    // ── M3.5 Khí Vận rework (Phase 0): chỉ roll trait CÓ TÁC DỤNG ──────────────
    // HOOKED_ATTR = ATTR id có reader thật trong engine (combat-attrs 15/06 + 6 linh căn / 6 tư chất M7
    // + lifecycle M5/M6 + aggregate allRoots/allCombatApt/otherRoots fan-in Phase 0).
    private static final java.util.Set<Integer> HOOKED_ATTR = new java.util.HashSet<>(java.util.Arrays.asList(
            2, 3, 39,                                  // allCombatApt, allRoots, otherRoots
            4, 5, 7, 9, 10, 11, 24, 32, 41, 44, 50,    // atk/backDmg/charm/comprehension/cultGain/def/heartShield/luck/perception/reputation/speed
            13, 18, 31, 57, 58, 59,                    // earth/fire/lightning/water/wind/wood root
            17, 19, 40, 46, 48, 54,                    // finger/fist/palm/saber/spear/sword apt
            22, 23, 29, 30, 33, 34, 35, 51, 52,        // health(Max)/lifespan(Max)/mental(Max)/moodMax/stamina(Max)
            14, 15, 20, 21,                            // Phase 2: evilPath/evilPathValue/goodPath/goodPathValue (hệ chính-tà)
            1, 43, 16, 12, 36, 27, 6));                // Khí Vận B hook: alchemyApt/refiningApt/fengShui->cultGain, earthAttr->Thổ root, moveSpeed/kickPower/bodyMasterySpd->haste
    // Trait id có effect đặc biệt ĐÃ implement (mở dần từng phase; Phase 0 rỗng).
    private static final java.util.Set<Integer> IMPLEMENTED_KHIVAN = new java.util.HashSet<>(java.util.Arrays.asList(
            700016, 700017, 700119, 700121,   // 1.1 bắn nguyên tố Hỏa/Thủy/Lôi/Phong Tứ Xạ
            700050, 700060, 700061,           // 1.3 Linh Cộng Sinh (đánh thêm chiêu)
            700107, 700108, 700109, 700110,   // 1.2 rơi phách khi giết (Thị/Liệp Linh-Tinh)
            700028, 700033,                   // 2.1 Huyết Ma hút máu, 2.2 Tiên Thụ Hậu Công (ramp)
            700001, 700002, 700003,           // 2.3 Yêu Thuật biến hình "gà con" (choáng non-tu-tiên)
            700022, 700023, 700024, 700025)); // Phase 3: Quỷ Tu phục sinh vong linh + Hoàng Cân triệu hồi Lực Sĩ
    // TẠM TẮT KHÔNG CHO ROLL (effect-only flavor chưa code — gia truyền/kỳ ngộ/thương nghiệp).
    // Giữ trong data, chỉ chặn khỏi pool. Muốn bật lại: xoá id khỏi set này.
    private static final java.util.Set<Integer> DISABLED_KHIVAN = new java.util.HashSet<>(java.util.Arrays.asList(
            1101, 1502, 1801, 1802, 1803,
            1901, 1902, 1903, 1904, 1905, 1906, 1907, 1908, 1909, 1910, 1911,
            2708, 2801, 2802));
    private List<ThienTienTemplate>[] khiVanPool; // pool roll group theo rarity (build lazy)

    public static TuTienService gI() {
        if (instance == null) {
            instance = new TuTienService();
        }
        return instance;
    }

    // ── JSON cho DAO (PlayerDAO.updatePlayer save / NDVSqlFetcher.loadPlayer) ─
    public String toJson(TuTien t) {
        if (t == null) {
            return null;
        }
        synchronized (t) { // chong snapshot rach khi autosave chay song song voi tick
            return GSON.toJson(t);
        }
    }

    public TuTien fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, TuTien.class);
    }

    // ── Kich hoat he thong (NPC Quy Lao chon "Hoc Tu Tien") ───────────────────
    public void activate(Player pl) {
        if (pl.tuTien != null) { // da hoc roi -> chi mo bang
            sendAll(pl);
            return;
        }
        // M9 Gate: can TIEN_DUYEN_HOC_TU_TIEN Tien Duyen (item 2039, roi tu quai/boss) de hoc tu tien
        int tdHave = countMaterial(pl, ITEM_TIEN_DUYEN);
        if (tdHave < TIEN_DUYEN_HOC_TU_TIEN) {
            Service.gI().sendThongBao(pl, "Can " + TIEN_DUYEN_HOC_TU_TIEN + " Tien Duyen de hoc Tu Tien (hien co "
                    + tdHave + "). Tien Duyen roi ngau nhien khi danh quai/boss.");
            return;
        }
        consumeMaterial(pl, ITEM_TIEN_DUYEN, TIEN_DUYEN_HOC_TU_TIEN);
        services.InventoryService.gI().sendItemBag(pl);
        pl.tuTien = newbie(pl);
        Service.gI().point(pl);
        sendTuTienFly(pl, "Ngươi đã bước lên con đường tu tiên! Cảnh giới hiện tại: Luyện Khí Kỳ Tầng 1");
        sendAll(pl);
    }

    private TuTien newbie(Player pl) {
        TuTien t = new TuTien();
        t.id = pl.id;
        t.name = pl.name;
        t.gender = pl.gender;
        t.canhGioi = CanhGioi.LUYEN_KHI;
        t.tangGioi = 1;
        t.tuVi = 0;

        // ── Chỉ số cơ thể ban đầu (random mới mỗi lần mở/reset tu tiên) ─────────
        Random r = new Random();
        // Thể lực tối đa & khỏe mạnh — random; ảnh hưởng tuổi thọ + linh căn ban đầu
        t.hpMax = THE_LUC_MIN + r.nextInt(THE_LUC_MAX - THE_LUC_MIN + 1);
        t.hp = t.hpMax;
        t.health = KHOE_MANH_MIN + r.nextInt(KHOE_MANH_MAX - KHOE_MANH_MIN + 1);
        t.healthMax = t.health;
        // Linh căn: random theo phẩm chất (cao hiếm, thấp nhiều) + bonus do thể lực tối đa cao
        int rootBonus = Math.max(0, (t.hpMax - THE_LUC_MIN) / THE_LUC_PER_ROOT);
        t.lightningRoot = rollLinhCan(r) + rootBonus;
        t.fireRoot = rollLinhCan(r) + rootBonus;
        t.waterRoot = rollLinhCan(r) + rootBonus;
        t.windRoot = rollLinhCan(r) + rootBonus;
        t.woodRoot = rollLinhCan(r) + rootBonus;
        t.earthRoot = rollLinhCan(r) + rootBonus;

        t.atk = 10 + r.nextInt(5);           // 10 - 40
        t.def = 10 + r.nextInt(5);           // 10 - 40
        t.rangedAtk = 5 + r.nextInt(5);      // 5 - 20
        t.kickPower = 10 + r.nextInt(5);     // 10 - 40
        t.kickRange = 1 + r.nextInt(5);       // 1 - 3
        t.speed = 10 + r.nextInt(5);         // 10 - 40
        t.moveSpeed = 10 + r.nextInt(5);     // 10 - 40
        t.luck = 10 + r.nextInt(5);          // 10 - 40
        t.perception = 10 + r.nextInt(5);    // 10 - 40
        t.charm = 10 + r.nextInt(5);         // 10 - 40
        t.heartShield = 10 + r.nextInt(5);         // 10 - 40
        t.backDamageTaken = 10 + r.nextInt(5);         // 10 - 40
        t.comprehension = 10 + r.nextInt(31); // 10 - 40

        // Linh lực tối đa theo cảnh giới (Luyện Khí Tầng 1 = MP_BASE)
        t.mpMax = mpMaxFor(t.canhGioi, t.tangGioi);
        t.mp = t.mpMax;
        // Tinh lực & niệm lực: pool tiêu hao khi đả tọa
        t.staminaMax = TINH_NIEM_MIN + r.nextInt(TINH_NIEM_MAX - TINH_NIEM_MIN + 1);
        t.stamina = t.staminaMax;
        t.mentalMax = TINH_NIEM_MIN + r.nextInt(TINH_NIEM_MAX - TINH_NIEM_MIN + 1);
        t.mental = t.mentalMax;
        t.moodMax = 100;
        t.mood = t.moodMax;   // tâm tình bắt đầu đầy
        t.moodInit = true;
        // Tuổi thọ ban đầu = base + bonus từ khỏe mạnh & thể lực (cao -> thọ nhiều hơn)
        t.lifespanMax = LIFESPAN_START + lifespanBonus(t.health, t.hpMax);
        t.lifespan = t.lifespanMax;

        t.swordApt = 10 + r.nextInt(41);
        t.saberApt = 10 + r.nextInt(41);
        t.spearApt = 10 + r.nextInt(41);
        t.fistApt = 10 + r.nextInt(41);
        t.fingerApt = 10 + r.nextInt(41);
        t.palmApt = 10 + r.nextInt(41);
        t.combatApt = 10 + r.nextInt(41);

        t.alchemyApt = 10 + r.nextInt(31);
        t.refiningApt = 10 + r.nextInt(31);
        t.fengShui = 10 + r.nextInt(31);
        t.talisman = 10 + r.nextInt(31);
        t.cultivationGain = 100;
        t.congPhap = new int[14];
        t.khiVanIds = new int[0];
        t.khiVanRollCharges = 1; // 1 luot roll khoi dau o Luyen Khi Tang 1
        return t;
    }

    // ── M5 helpers: linh lực theo cảnh giới, roll linh căn, bonus tuổi thọ ─────
    // Linh lực tối đa tăng theo cảnh giới (+ mỗi tầng nhỏ) — gọi ở newbie & sau đột phá
    private int mpMaxFor(int canhGioi, int tangGioi) {
        return MP_BASE + MP_PER_REALM * (Math.max(1, canhGioi) - 1)
                + MP_PER_TANG * (Math.max(1, tangGioi) - 1);
    }

    // Roll 1 linh căn: chọn bậc phẩm chất theo trọng số (cao hiếm, thấp nhiều) rồi random trong bậc
    private int rollLinhCan(Random r) {
        int total = 0;
        for (int w : LINH_CAN_W) {
            total += w;
        }
        int roll = r.nextInt(total);
        int acc = 0;
        for (int i = 0; i < LINH_CAN_W.length; i++) {
            acc += LINH_CAN_W[i];
            if (roll < acc) {
                return LINH_CAN_LO[i] + r.nextInt(LINH_CAN_HI[i] - LINH_CAN_LO[i] + 1);
            }
        }
        return LINH_CAN_LO[0];
    }

    // Bonus tuổi thọ ban đầu từ khỏe mạnh & thể lực tối đa (cao -> thọ nhiều hơn)
    private int lifespanBonus(int health, int hpMax) {
        int b = (health - KHOE_MANH_MIN) / LIFESPAN_PER_KHOE_MANH
                + (hpMax - THE_LUC_MIN) / LIFESPAN_PER_THE_LUC;
        return Math.max(0, b);
    }

    // M6: migrate tâm tình cho save cũ (field mood/moodInit mới -> Gson default 0/false)
    private void ensureMood(TuTien t) {
        if (t.moodMax <= 0) {
            t.moodMax = 100;
        }
        if (!t.moodInit) {
            t.mood = t.moodMax;
            t.moodInit = true;
        }
        if (t.mood > t.moodMax) {
            t.mood = t.moodMax;
        }
        if (t.mood < 0) {
            t.mood = 0;
        }
    }

    // Phẩm chất linh căn (thiên phú): 0=Tạp, 1=Thượng phẩm, 2=Cực phẩm (đơn), 3=Tiên phẩm (ngũ hành)
    // Ngưỡng user: phế<10, trung~30, thượng>50, cực>80; tiên = ngũ hành (>=5 căn) đều > 80
    private int linhCanTier(TuTien t) {
        int[] roots = {t.lightningRoot, t.fireRoot, t.waterRoot, t.windRoot, t.woodRoot, t.earthRoot};
        int n80 = 0, n50 = 0;
        for (int v : roots) {
            if (v > ROOT_CUC_PHAM) {
                n80++;
            }
            if (v > ROOT_THUONG_PHAM) {
                n50++;
            }
        }
        if (n80 >= TIEN_PHAM_ROOTS) {
            return 3;                  // ngũ hành đều > 80
        }
        if (n80 == 1 && n50 == 1) {
            return 2;                  // đơn linh căn > 80 (chỉ 1 căn nổi trội)
        }
        if (n50 == 1 || n50 == 2) {
            return 1;                  // tu thuần 1-2 căn > 50
        }
        return 0;                      // 3-4 căn (tạp) hoặc toàn thấp
    }

    // Bonus % đột phá theo thiên phú
    private int talentBonus(TuTien t) {
        switch (linhCanTier(t)) {
            case 3:
                return TALENT_TIEN_PHAM;
            case 2:
                return TALENT_CUC_PHAM;
            case 1:
                return TALENT_THUONG_PHAM;
            default:
                return 0;
        }
    }

    // Bonus % tốc độ tu vi + linh khí theo thiên phú (dùng chung bậc với talentBonus)
    private int talentGainPct(TuTien t) {
        return TALENT_GAIN_PCT[linhCanTier(t)];
    }

    private String linhCanSummary(TuTien t) {
        return LINH_CAN_TIER_NAME[linhCanTier(t)] + " (đột phá +" + talentBonus(t) + "%, tu luyện +" + talentGainPct(t) + "%)";
    }

    // M7 §IV: lực tu luyện 1 công pháp theo cấp (cấp >10 nội suy 30 + 5/cấp)
    private int cultPowerForLv(int lv) {
        if (lv <= 0) {
            return 0;
        }
        if (lv < CULT_POWER_PER_LV.length) {
            return CULT_POWER_PER_LV[lv];
        }
        return 30 + (lv - 10) * 5;
    }

    // M7 §III-VI: % tốc độ tu vi/linh khí từ công pháp linh căn (idx 0-5 = Lôi/Hỏa/Thủy/Phong/Mộc/Thổ).
    //   = Σ( cultPower(cấp_i) × heSoLinhCan_i ) × hiệu suất tu nhiều pháp / chuẩn.
    //   heSoLinhCan_i = 1 + diemLinhCan_i/100 (req §VI) -> nhân ×100 ra (100 + root).
    //   Trả 0 nếu CHƯA học pháp nào (req §III GATE: tu vi & linh khí = 0). Bách Pháp (idx6) KHÔNG sinh
    //   (đã cộng +linh căn mọi hệ -> nuôi gián tiếp qua heSoLinhCan của 6 pháp nguyên tố).
    private long cultivationMult100(TuTien t) {
        if (t.congPhap == null) {
            return 0;
        }
        int[] roots = {t.lightningRoot, t.fireRoot, t.waterRoot, t.windRoot, t.woodRoot, t.earthRoot};
        long power = 0;
        int nPhap = 0;
        for (int i = 0; i < 6 && i < t.congPhap.length; i++) {
            if (t.congPhap[i] > 0) {
                nPhap++;
                power += (long) cultPowerForLv(t.congPhap[i]) * (100 + Math.max(0, roots[i]));
            }
        }
        if (nPhap == 0) {
            return 0; // §III: chưa học công pháp -> không nhận tu vi & linh khí
        }
        int multiPct = MULTI_PHAP_PCT[Math.min(nPhap, MULTI_PHAP_PCT.length - 1)];
        return power * multiPct / CULT_POWER_NORM; // ~percent (100 = baseline 1 pháp)
    }

    // Đột phá thành công -> tăng nhẹ thuộc tính chiến đấu (áp vào NPoint qua calPoint sau đột phá; persist trong data_tutien)
    private void grantDotPhaCombatStats(TuTien t) {
        t.atk += DP_GAIN_ATK;
        t.def += DP_GAIN_DEF;
        t.luck += DP_GAIN_LUCK;
        t.perception += DP_GAIN_PERCEPTION;
        t.charm += DP_GAIN_CHARM;
        t.comprehension += DP_GAIN_COMPREHENSION;
        t.reputation += DP_GAIN_REPUTATION;
        t.speed += DP_GAIN_SPEED;
        t.heartShield += DP_GAIN_HEART_SHIELD;
        t.backDamageTaken += DP_GAIN_BACK_DMG;
    }

    // M7 header "nhận được": tốc độ nhận tu vi/phút theo trạng thái HIỆN TẠI (đả tọa, tâm tình, công pháp, Tu Khí, cap).
    // Nhân bản công thức tu vi trong tick() với cửa sổ 60000ms (= MAX_TICK_DELTA -> cùng biên an toàn long).
    public long tuViGainPerMin(TuTien t) {
        if (t == null || t.disabled) {
            return 0;
        }
        long cultMult100 = cultivationMult100(t);
        if (cultMult100 <= 0) {
            return 0; // chưa học công pháp -> gate §III, không sinh tu vi
        }
        long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
        if (t.tuVi >= cap) {
            return 0; // đầy cấp -> chờ đột phá, tu vi không tăng
        }
        boolean tuKhi = t.tuKhiBuffMs > 0;
        long mult = (t.meditating ? 2 : 1) * (tuKhi ? 2 : 1);
        long moodPct = (t.moodMax > 0) ? (long) t.mood * 100 / t.moodMax : 100;
        long moodFactorPct = Math.min(100, 50 + moodPct);
        long perMin = 60000L * mult * cultGainEff(t) * moodFactorPct * (100 + talentGainPct(t)) * cultMult100 / 100_000_000L / MS_PER_TUVI;
        return Math.max(0, perMin);
    }

    // M7 header "nhận được": tốc độ nhận linh khí/phút theo trạng thái HIỆN TẠI. Nhân bản công thức linh khí trong tick().
    public long linhKhiGainPerMin(TuTien t) {
        if (t == null || t.disabled) {
            return 0;
        }
        long cultMult100 = cultivationMult100(t);
        if (cultMult100 <= 0) {
            return 0;
        }
        boolean tuKhi = t.tuKhiBuffMs > 0;
        boolean channeling = t.meditating;
        long reservePct = 0;
        if (channeling) {
            long sPct = (t.staminaMax > 0) ? (long) t.stamina * 100 / t.staminaMax : 0;
            long mPct = (t.mentalMax > 0) ? (long) t.mental * 100 / t.mentalMax : 0;
            reservePct = Math.max(0, Math.min(100, (sPct + mPct) / 2));
        }
        long lkMult100 = channeling ? (100 + reservePct) : 100;
        if (tuKhi) {
            lkMult100 *= 2;
        }
        long moodPct = (t.moodMax > 0) ? (long) t.mood * 100 / t.moodMax : 100;
        long moodFactorPct = Math.min(100, 50 + moodPct);
        long perMin = 60000L * cultGainEff(t) * lkMult100 * moodFactorPct * (100 + talentGainPct(t)) * (100 + Math.max(0, t.comprehension)) * cultMult100 / 1_000_000_000_000L / MS_PER_LINH_KHI;
        return Math.max(0, perMin);
    }

    // ── Tick moi ~1s tu Player.update() block 1 (chi player that) ─────────────
    public void tick(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null || t.disabled) {
            return;
        }
        boolean justDisabled = false;
        boolean spiritEmptied = false;
        boolean moodWarn = false;
        List<String> levelUps = null;
        synchronized (t) {
            ensureMood(t);
            long now = System.currentTimeMillis();
            long delta = (t.lastTickMs == 0) ? 0 : (now - t.lastTickMs);
            t.lastTickMs = now;
            if (delta <= 0 || delta > MAX_TICK_DELTA) {
                return;
            }
            // nam tu tien troi + tru tuoi tho (req §2, §3) + bao mon tam tinh (M6)
            int moodLowThresh = t.moodMax * MOOD_LOW_PCT / 100;
            t.msAccumYear += delta;
            while (t.msAccumYear >= YEAR_MS) {
                t.msAccumYear -= YEAR_MS;
                t.namTuTien++;
                t.lifespan--;
                // tâm tình mỗi năm: -10% +5% = net -5% moodMax (clamp 0..moodMax)
                boolean wasAboveLow = t.mood >= moodLowThresh;
                t.mood += t.moodMax * (MOOD_REGEN_PER_YEAR_PCT - MOOD_DECAY_PER_YEAR_PCT) / 100;
                if (t.mood < 0) {
                    t.mood = 0;
                }
                if (t.mood > t.moodMax) {
                    t.mood = t.moodMax;
                }
                if (wasAboveLow && t.mood < moodLowThresh) {
                    moodWarn = true;
                }
                if (t.lifespan <= 0) {
                    t.lifespan = 0;
                    t.disabled = true;
                    t.meditating = false;
                    justDisabled = true;
                    break;
                }
            }
            // tu vi tang dan theo toc do, cap theo tang hien tai
            if (!justDisabled) {
                boolean tuKhi = t.tuKhiBuffMs > 0; // Tu Khi Dan x2
                // M6: tâm tình thấp -> giảm tốc độ tu luyện (tu vi + linh khí). mood>=50% -> 100%, mood=0 -> 50%
                long moodPct = (t.moodMax > 0) ? (long) t.mood * 100 / t.moodMax : 100;
                long moodFactorPct = Math.min(100, 50 + moodPct);
                // M7: hệ số % tốc độ từ công pháp linh căn (0 = chưa học pháp -> GATE tu vi & linh khí, req §III)
                long cultMult100 = cultivationMult100(t);
                long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
                if (t.tuVi < cap && cultMult100 > 0) {
                    long mult = (t.meditating ? 2 : 1) * (tuKhi ? 2 : 1); // da toa x2, Tu Khi x2 (cong don)
                    t.msAccumTuVi += (long) delta * mult * cultGainEff(t) * moodFactorPct * (100 + talentGainPct(t)) * cultMult100 / 100_000_000L; // tâm tình + thiên phú + công pháp (M7)
                    if (t.msAccumTuVi < 0) {
                        t.msAccumTuVi = 0;
                    }
                    long gain = t.msAccumTuVi / MS_PER_TUVI;
                    if (gain > 0) {
                        t.msAccumTuVi %= MS_PER_TUVI;
                        t.tuVi = Math.min(cap, t.tuVi + gain);
                    }
                }
                // M4/M5: hap thu linh khi -> nuoi exp moi bi kip DA hoc.
                // Linh khi van tang ke ca khi tu vi day cap (luyen cong phap trong luc cho dot pha).
                // Da toa: hap thu manh hon theo TINH LUC + NIEM LUC (can -> mat hieu suat) & tieu hao 2 chi so do.
                boolean channeling = t.meditating;
                long reservePct = 0;
                if (channeling) {
                    long sPct = (t.staminaMax > 0) ? (long) t.stamina * 100 / t.staminaMax : 0;
                    long mPct = (t.mentalMax > 0) ? (long) t.mental * 100 / t.mentalMax : 0;
                    reservePct = Math.max(0, Math.min(100, (sPct + mPct) / 2)); // 0..100
                }
                // mult linh khi (%): nen 100; da toa = 100 + reservePct (x1..x2 theo tinh/niem luc); Tu Khi x2
                long lkMult100 = channeling ? (100 + reservePct) : 100;
                if (tuKhi) {
                    lkMult100 *= 2;
                }
                // Ngộ Tính (comprehension): +1% lượng linh khí nhận / điểm. M7: ×công pháp (cultMult100; 0 -> gate §III)
                long lkAdd = (long) delta * cultGainEff(t) * lkMult100 * moodFactorPct * (100 + talentGainPct(t)) * (100 + Math.max(0, t.comprehension)) * cultMult100 / 1_000_000_000_000L;
                t.msAccumLinhKhi += Math.max(0, lkAdd);
                long lkGain = t.msAccumLinhKhi / MS_PER_LINH_KHI;
                if (lkGain > 0) {
                    t.msAccumLinhKhi %= MS_PER_LINH_KHI;
                    t.linhKhi += lkGain;
                    levelUps = addCongPhapExp(t, lkGain);
                }
                // Nhip tieu hao (da toa) / hoi phuc (nghi) tinh luc & niem luc
                boolean spiritWasPositive = (t.stamina > 0 || t.mental > 0);
                t.msAccumSpirit += delta;
                while (t.msAccumSpirit >= MS_PER_SPIRIT) {
                    t.msAccumSpirit -= MS_PER_SPIRIT;
                    if (channeling) {
                        t.stamina = Math.max(0, t.stamina - SPIRIT_DRAIN);
                        t.mental = Math.max(0, t.mental - SPIRIT_DRAIN);
                    } else {
                        t.stamina = Math.min(t.staminaMax, t.stamina + SPIRIT_REGEN);
                        t.mental = Math.min(t.mentalMax, t.mental + SPIRIT_REGEN);
                    }
                }
                if (channeling && spiritWasPositive && t.stamina == 0 && t.mental == 0) {
                    spiritEmptied = true; // vua can -> thong bao 1 lan
                }
                if (tuKhi) {
                    t.tuKhiBuffMs = Math.max(0, t.tuKhiBuffMs - delta);
                }
                ///  hoi tam tinh
                if (Util.canDoWithTime(lastTimeHoiTamTinh, 2 * 60 * 1000)) {
                    t.mood += ROLL_RNG.nextInt(2);
                }
            }
        }
        if (justDisabled) {
            onDisabled(pl);
        } else if ((levelUps != null && !levelUps.isEmpty()) || spiritEmptied) {
            if (levelUps != null) {
                for (String s : levelUps) {
                    sendTuTienFly(pl, s);
                }
            }
            if (spiritEmptied) {
                sendTuTienFly(pl, "Tinh lực & niệm lực đã cạn — hiệu suất đả tọa giảm mạnh, hãy ngừng đả tọa để hồi phục");
            }
            if (moodWarn) {
                sendTuTienFly(pl, "Tâm tình đang xuống thấp — tốc độ tu luyện giảm, đột phá cảnh giới để khôi phục tâm tình");
            }
        }
        if (Util.canDoWithTime(lastTimeSendRefresh, 2000)) {
            lastTimeSendRefresh = System.currentTimeMillis();
            sendRefresh(pl);
        }
    }

    // het tuoi tho (req §3): go buff Pha Canh, dung tick, cho hoan hon
    private void onDisabled(Player pl) {
        pl.nPoint.calPoint();
        Service.gI().point(pl);
        sendTuTienFly(pl, "Tuổi thọ đã cạn kiệt! Toàn bộ hiệu lực tu tiên bị vô hiệu hóa, cần Âm Dương Hoàn Hồn Đan để khôi phục");
        sendRefresh(pl);
    }

    // ── Da toa (action 1): toc do x2, dung yen; huy khi di chuyen/bi danh/chet ─
    public void toggleMeditate(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null) {
            return;
        }
        if (t.disabled) {
            sendTuTienFly(pl, "Tuổi thọ đã cạn, không thể tu luyện");
            return;
        }
        if (pl.isDie()) {
            return;
        }
        t.meditating = !t.meditating;
        sendTuTienFly(pl, t.meditating
                ? "Bắt đầu đả tọa, tốc độ tu luyện x2. Di chuyển hoặc bị tấn công sẽ gián đoạn"
                : "Đã dừng đả tọa");
        // M7 §III: đả tọa mà chưa học công pháp linh căn nào -> không nhận tu vi/linh khí
        if (t.meditating && cultivationMult100(t) == 0) {
            sendTuTienFly(pl, "Chưa học công pháp linh căn — không hấp thu được tu vi/linh khí. Hãy dùng bí tịch (Lôi/Hỏa/Thủy/Phong/Mộc/Thổ Pháp) để khai mở");
        }
        sendRefresh(pl);
    }

    // goi tu PlayerService.playerMove / Player.injured / Player.setDie
    public void cancelMeditate(Player pl, String reason) {
        TuTien t = pl.tuTien;
        if (t == null || !t.meditating) {
            return;
        }
        t.meditating = false;
        sendTuTienFly(pl, reason);
        sendRefresh(pl);
    }

    // ── Dot pha (action 2) — M1: thanh cong 100% khi du tu vi ─────────────────
    public void dotPha(Player pl) {
        dotPha(pl, false);
    }

    // M7: Phá Cảnh — leo RIÊNG các tầng Phá Cảnh (14/15/16) của Luyện Khí (đường tăng lực TÙY CHỌN).
    // Đột Phá thường KHÔNG vào phá cảnh: tới Viên Mãn (tầng 13) Đột Phá = lên cảnh giới lớn kế (Trúc Cơ).
    public void phaCanh(Player pl) {
        dotPha(pl, true);
    }

    private void dotPha(Player pl, boolean phaCanhMode) {
        TuTien t = pl.tuTien;
        if (t == null) {
            return;
        }
        String notify;
        boolean recalc = false;
        synchronized (t) {
            boolean isLuyenKhi = (t.canhGioi == CanhGioi.LUYEN_KHI);
            // Viên Mãn đỉnh phong (tầng 13 = ngay trước Phá Cảnh) trở đi: Đột Phá = lên cảnh giới lớn kế (Trúc Cơ).
            boolean dotPhaToNextRealm = isLuyenKhi && t.tangGioi >= CanhGioi.LK_PHA_CANH_START - 1;
            // Còn tầng Phá Cảnh để leo: 13→14, 14→15, 15→16 (next tầng <= 16).
            boolean canPhaCanh = isLuyenKhi && t.tangGioi >= CanhGioi.LK_PHA_CANH_START - 1
                    && t.tangGioi + 1 <= CanhGioi.stageCount(CanhGioi.LUYEN_KHI);
            if (t.disabled) {
                notify = "Tuổi thọ đã cạn, không thể đột phá";
            } else if (!phaCanhMode && dotPhaToNextRealm) {
                // Đột Phá ở Viên Mãn/Phá Cảnh = lên Trúc Cơ (chưa implement); KHÔNG leo tầng phá cảnh.
                notify = "Đã đủ điều kiện đột phá Trúc Cơ — sắp ra mắt";
            } else if (phaCanhMode && !canPhaCanh) {
                if (!isLuyenKhi) {
                    notify = "Phá Cảnh chỉ dành cho Luyện Khí";
                } else if (t.tangGioi >= CanhGioi.stageCount(CanhGioi.LUYEN_KHI)) {
                    notify = "Đã tới đỉnh Phá Cảnh — dùng Đột Phá để lên Trúc Cơ";
                } else {
                    notify = "Chưa tới Viên Mãn — hãy tu luyện & dùng nút Đột Phá để lên cảnh giới thường trước";
                }
            } else {
                long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
                if (t.tuVi < cap) {
                    notify = "Tu vi chưa đủ để " + (phaCanhMode ? "phá cảnh" : "đột phá") + " (" + t.tuVi + "/" + cap + ")";
                } else {
                    // Tỷ lệ đột phá: nền 50% (Luyện Khí tầng 1), -5% mỗi cảnh giới NHỎ; Phá Cảnh nền RIÊNG 5%. + thiên phú.
                    // M9: dot pha tieu Linh Thach = tang HIEN TAI x3 (tru khi bam, du thanh/bai)
                    int ltCost = t.tangGioi * DOT_PHA_LINH_THACH_PER_TANG;
                    int ltHave = countMaterial(pl, ITEM_LINH_THACH);
                    if (ltHave < ltCost) {
                        notify = "Can " + ltCost + " Linh Thach de "
                                + (phaCanhMode ? "pha canh" : "dot pha") + " (hien co " + ltHave + ")";
                    } else {
                    consumeMaterial(pl, ITEM_LINH_THACH, ltCost);
                    services.InventoryService.gI().sendItemBag(pl);
                    int rate = getRate(t);
                    if (ROLL_RNG.nextInt(100) >= rate) {
                        t.mood = Math.max(0, t.mood - t.moodMax * MOOD_DOT_PHA_FAIL_PCT / 100);
                        long tuViLost = t.tuVi * DOT_PHA_FAIL_TUVI_PCT / 100;
                        t.tuVi = Math.max(0, t.tuVi - tuViLost);
                        t.msAccumTuVi = 0;
                        long lkLost = t.linhKhi;
                        t.linhKhi = 0;
                        t.msAccumLinhKhi = 0;
                        notify = "Đột phá THẤT BẠI! (tỷ lệ " + rate + "%) Mất " + lkLost + " linh khí, -"
                                + DOT_PHA_FAIL_TUVI_PCT + "% tu vi (" + tuViLost + "), tâm tình -" + MOOD_DOT_PHA_FAIL_PCT + "%";
                    } else {
                        t.tangGioi++;
                        t.tuVi = 0;
                        t.msAccumTuVi = 0;
                        t.khiVanRollCharges++; // moi cảnh giới nhỏ (đột phá) = +1 lượt roll khí vận
                        t.mpMax = mpMaxFor(t.canhGioi, t.tangGioi); // linh lực tối đa tăng theo cảnh giới
                        t.mp = t.mpMax;                              // đột phá hồi đầy linh lực
                        t.mood = Math.min(t.moodMax, t.mood + t.moodMax * MOOD_DOT_PHA_SUCCESS_PCT / 100); // +50% tâm tình
                        grantDotPhaCombatStats(t); // tăng nhẹ thuộc tính chiến đấu
                        recalc = true;             // calPoint để áp thuộc tính mới vào NPoint (mọi đột phá, không chỉ phá cảnh)
                        String extra = "";
                        if (t.canhGioi == CanhGioi.LUYEN_KHI) {
                            if (t.tangGioi == 4 || t.tangGioi == 7 || t.tangGioi == 10) {
                                // buoc vao Trung Ky / Hau Ky / Vien Man (req §3: +10 nam)
                                t.lifespan += LIFESPAN_GIAI_DOAN;
                                t.lifespanMax += LIFESPAN_GIAI_DOAN;
                                extra = ", tuổi thọ +" + LIFESPAN_GIAI_DOAN + " năm";
                            } else if (CanhGioi.isPhaCanhStage(t.canhGioi, t.tangGioi)) {
                                // Pha Canh (req §5: +20 nam, +10% chi so co ban moi lan)
                                t.phaCanh++;
                                t.lifespan += LIFESPAN_PHA_CANH;
                                t.lifespanMax += LIFESPAN_PHA_CANH;
                                recalc = true;
                                extra = ", tuổi thọ +" + LIFESPAN_PHA_CANH + " năm, chỉ số cơ bản +" + (t.phaCanh * 10) + "%";
                            }
                        }
                        notify = "Đột phá thành công! " + CanhGioi.fullNameOf(t.canhGioi, t.tangGioi) + extra
                                + " (tỷ lệ " + rate + "%, +1 lượt roll khí vận, tâm tình +" + MOOD_DOT_PHA_SUCCESS_PCT + "%, chỉ số chiến đấu tăng)";
                    }
                    } // M9: dong else "du Linh Thach"
                }
            }
        }
        if (recalc) { // ngoai lock: calPoint nang va tu doc phaCanh/disabled
            pl.nPoint.calPoint();
            Service.gI().point(pl);
        }
        sendTuTienFly(pl, notify);
        sendRefresh(pl);
    }

    private int getRate(TuTien t) {
        int base;
        if (CanhGioi.isPhaCanhStage(t.canhGioi, t.tangGioi + 1)) {
            base = PHA_CANH_BASE_PCT; // đột phá VÀO tầng Phá Cảnh
        } else {
            base = Math.max(DOT_PHA_MIN_PCT, BASE_DOT_PHA_PCT - DOT_PHA_DROP_PER_STAGE * (t.tangGioi - 1));
        }
        // Ngộ Tính (comprehension): +0.2% tỷ lệ đột phá / điểm (50 điểm = +10%)
        int rate = Math.min(100, base + talentBonus(t) + Math.max(0, t.comprehension) / 5);
        return rate;
    }

    // ── Admin/test (Command.java: "tutien tv|nam|hoanhon|reset") ──────────────
    public void addTuVi(Player pl, long v) {
        TuTien t = pl.tuTien;
        if (t == null) {
            return;
        }
        synchronized (t) {
            long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
            t.tuVi = Math.max(0L, Math.min(cap, t.tuVi + v));
        }
        sendRefresh(pl);
    }

    public void addNam(Player pl, int nam) {
        TuTien t = pl.tuTien;
        if (t == null) {
            return;
        }
        boolean justDisabled = false;
        synchronized (t) {
            t.lifespan += nam;
            if (t.lifespan > t.lifespanMax) {
                t.lifespan = t.lifespanMax;
            }
            if (t.lifespan <= 0) {
                t.lifespan = 0;
                if (!t.disabled) {
                    t.disabled = true;
                    t.meditating = false;
                    justDisabled = true;
                }
            }
        }
        if (justDisabled) {
            onDisabled(pl);
        } else {
            sendRefresh(pl);
        }
    }

    // hoi sinh co khi het tho (M1: lenh admin; M2: Am Duong Hoan Hon Dan goi ham nay)
    public void hoanHon(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null) {
            return;
        }
        synchronized (t) {
            t.disabled = false;
            if (t.lifespan <= 0) {
                t.lifespan = t.lifespanMax;
            }
            t.lastTickMs = 0;
        }
        pl.nPoint.calPoint(); // buff Pha Canh tro lai
        Service.gI().point(pl);
        sendTuTienFly(pl, "Sinh cơ đã khôi phục, trạng thái tu tiên trở lại!");
        sendRefresh(pl);
    }

    public void reset(Player pl) {
        if (pl.tuTien == null) {
            return;
        }
        pl.tuTien = null; // DAO se ghi NULL -> ve trang thai chua kich hoat
        pl.nPoint.calPoint();
        Service.gI().point(pl);
        sendTuTienFly(pl, "Đã xóa toàn bộ dữ liệu tu tiên");
    }

    // Reset ve Luyen Khi Tang 1 + roll lai TOAN BO chi so ban dau (giu kich hoat) — test linh can/chi so
    public void resetReroll(Player pl) {
        pl.tuTien = newbie(pl);
        pl.nPoint.calPoint();
        Service.gI().point(pl);
        sendTuTienFly(pl, "Đã reset tu tiên về Luyện Khí Tầng 1 — " + linhCanSummary(pl.tuTien));
        sendAll(pl);
    }

    // ── Dan duoc M2 (req §8) — moi ham tra ve true neu DA dung => UseItem tru 1 ─
    private boolean checkActivated(Player pl, TuTien t) {
        if (t == null) {
            sendTuTienFly(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên trước");
            return false;
        }
        return true;
    }

    // Tu Khi Dan: +100% toc do tu luyen trong 1 nam tu tien (cong don neu dung nhieu)
    public boolean useTuKhiDan(Player pl) {
        TuTien t = pl.tuTien;
        if (!checkActivated(pl, t)) {
            return false;
        }
        synchronized (t) {
            t.tuKhiBuffMs += TU_KHI_BUFF_MS;
        }
        sendTuTienFly(pl, "Tụ Khí Đan: +100% tốc độ tu luyện trong 1 năm tu tiên");
        sendRefresh(pl);
        return true;
    }

    // Boi Nguyen Dan: tang truc tiep tu vi, cang len canh gioi cao hieu qua cang giam
    public boolean useBoiNguyenDan(Player pl) {
        TuTien t = pl.tuTien;
        if (!checkActivated(pl, t)) {
            return false;
        }
        long gain;
        synchronized (t) {
            if (t.disabled) {
                sendTuTienFly(pl, "Tuổi thọ đã cạn, không thể bồi nguyên");
                return false;
            }
            long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
            gain = Math.max(5L, cap * BOI_NGUYEN_PERCENT / 100 / t.canhGioi);
            t.tuVi = Math.min(cap, t.tuVi + gain);
        }
        sendTuTienFly(pl, "Bồi Nguyên Đan: +" + gain + " tu vi");
        sendRefresh(pl);
        return true;
    }

    // Nguyen Linh Dan: hoi 100% linh luc — chi hieu luc trong Luyen Khi Ky (req §8)
    public boolean useNguyenLinhDan(Player pl) {
        TuTien t = pl.tuTien;
        if (!checkActivated(pl, t)) {
            return false;
        }
        if (t.canhGioi != CanhGioi.LUYEN_KHI) {
            sendTuTienFly(pl, "Nguyên Linh Đan chỉ có hiệu lực trong Luyện Khí Kỳ");
            return false;
        }
        synchronized (t) {
            t.mp = t.mpMax;
        }
        pl.nPoint.setMp(pl.nPoint.mpMax); // hoi 100% KI (linh luc) that
        services.PlayerService.gI().sendInfoHpMp(pl);
        sendTuTienFly(pl, "Nguyên Linh Đan: hồi 100% linh lực");
        sendRefresh(pl);
        return true;
    }

    // Am Duong Hoan Hon Dan: khoi phuc trang thai tu tien khi het tho (req §8)
    public boolean useHoanHonDan(Player pl) {
        TuTien t = pl.tuTien;
        if (!checkActivated(pl, t)) {
            return false;
        }
        if (!t.disabled) {
            sendTuTienFly(pl, "Âm Dương Hoàn Hồn Đan chỉ dùng khi tuổi thọ đã cạn");
            return false;
        }
        hoanHon(pl);
        return true;
    }

    // ── Linh Thạch (item 2016): rơi từ boss, là TIỀN TỆ mua công pháp ở Quy Lão; dùng -> +linh khí + 1 chút tu vi ─
    public static final short ITEM_LINH_THACH = 2016;
    public static final int LINH_THACH_LINH_KHI = 500; // +linh khí / viên (cũng nuôi exp công pháp như tick)
    public static final int LINH_THACH_TU_VI = 50;     // +tu vi / viên (1 chút, cap theo tầng)

    public boolean useLinhThachItem(Player pl) {
        TuTien t = pl.tuTien;
        if (!checkActivated(pl, t)) {
            return false;
        }
        if (t.disabled) {
            sendTuTienFly(pl, "Tuổi thọ đã cạn, không thể hấp thu linh thạch");
            return false;
        }
        long lkGain, tvGain;
        List<String> ups;
        synchronized (t) {
            t.linhKhi += LINH_THACH_LINH_KHI;
            lkGain = LINH_THACH_LINH_KHI;
            long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
            long before = t.tuVi;
            t.tuVi = Math.min(cap, t.tuVi + LINH_THACH_TU_VI);
            tvGain = t.tuVi - before;
            ups = addCongPhapExp(t, lkGain); // linh khí hấp thụ -> nuôi exp bí kíp đã học (như tick)
        }
        sendTuTienFly(pl, "Hấp thu Linh Thạch: +" + lkGain + " linh khí, +" + tvGain + " tu vi");
        if (ups != null) {
            for (String s : ups) {
                sendTuTienFly(pl, s);
            }
        }
        sendRefresh(pl);
        return true;
    }

    // ── M3 Thien Tien Khi Van: roll trait tu thientien.json, ap stat vao TuTien ─
    // Phase 0: trait "có nghĩa" mới vào pool — ≥1 stat hooked HOẶC effect đã implement.
    private boolean isMeaningfulKhiVan(ThienTienTemplate t) {
        if (DISABLED_KHIVAN.contains(t.id)) {
            return false; // tạm tắt không cho roll (ưu tiên trên mọi điều kiện)
        }
        if (IMPLEMENTED_KHIVAN.contains(t.id)) {
            return true;
        }
        for (StatEntry s : t.getStats()) {
            if (HOOKED_ATTR.contains(s.id)) {
                return true;
            }
        }
        return false;
    }

    // ── Khí Vận B hook: tư chất nghề (luyện đan/luyện khí/phong thủy) chưa có hệ riêng ──
    // → tạm quy vào tốc độ tu luyện (thiên phú cao tu nhanh hơn). Đọc field ở consumer,
    // KHÔNG đụng applyStat nên reroll/persist vẫn đúng (cùng triết lý Phase 0).
    private int craftCultGainBonus(TuTien t) {
        if (t == null) {
            return 0;
        }
        int sum = Math.max(0, t.alchemyApt) + Math.max(0, t.refiningApt) + Math.max(0, t.fengShui);
        int div = (int) ThienTienParams.get("craft_cultgain_div", 3);
        return div > 0 ? sum / div : 0;
    }

    // cultivationGain hiệu dụng = base (chặn âm) + thưởng tư chất nghề.
    private int cultGainEff(TuTien t) {
        int base = (t != null && t.cultivationGain > 0) ? t.cultivationGain : 0;
        return base + craftCultGainBonus(t);
    }

    @SuppressWarnings("unchecked")
    private synchronized List<ThienTienTemplate>[] khiVanPool() {
        if (khiVanPool == null) {
            List<ThienTienTemplate>[] p = new List[6]; // index = rarity 1..5
            for (int i = 0; i < p.length; i++) {
                p[i] = new ArrayList<>();
            }
            for (ThienTienTemplate t : ThienTienTemplate.getAll().values()) {
                if (t.rarity < 1 || t.rarity > 5) {
                    continue;
                }
                boolean hasExact = false; // loai trait exact-mode: doi khi van se khong reverse sach
                for (StatEntry s : t.getStats()) {
                    if (s.exact) {
                        hasExact = true;
                        break;
                    }
                }
                if (!hasExact && isMeaningfulKhiVan(t)) {
                    p[t.rarity].add(t);
                }
            }
            khiVanPool = p;
        }
        return khiVanPool;
    }

    public void rollKhiVan(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null) {
            sendTuTienFly(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên trước");
            return;
        }
        if (t.disabled) {
            sendTuTienFly(pl, "Tuổi thọ đã cạn, không thể chuyển khí vận");
            return;
        }
        ensureKhiVan(t);
        if (t.khiVanRollCharges <= 0) {
            sendTuTienFly(pl, "Hết lượt roll — đột phá cảnh giới (mỗi tầng +1 lượt) để có thêm");
            return;
        }
        List<ThienTienTemplate>[] p = khiVanPool();
        int totalW = 0;
        for (int r = 1; r <= 5; r++) {
            if (!p[r].isEmpty()) {
                totalW += RARITY_WEIGHT[r];
            }
        }
        if (totalW <= 0) {
            sendTuTienFly(pl, "Dữ liệu khí vận chưa sẵn sàng");
            return;
        }
        int slots = khiVanSlots(t.canhGioi);
        ThienTienTemplate[] rolled = new ThienTienTemplate[slots];
        for (int i = 0; i < slots; i++) {
            rolled[i] = rollOneTrait(p, totalW);
        }
        StringBuilder names = new StringBuilder();
        synchronized (t) {
            // go TAT CA khi van cu (pool toan delta-mode nen reverse = applyStat am)
            for (int kid : t.khiVanIds) {
                if (kid == 0) {
                    continue;
                }
                ThienTienTemplate old = ThienTienTemplate.get(kid);
                if (old != null) {
                    for (StatEntry s : old.getStats()) {
                        t.applyStat(s.id, -(int) s.value);
                    }
                }
            }
            int[] ids = new int[slots];
            for (int i = 0; i < slots; i++) {
                ThienTienTemplate nx = rolled[i];
                for (StatEntry s : nx.getStats()) {
                    t.applyStat(s.id, (int) s.value);
                }
                ids[i] = nx.id;
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(nx.name);
            }
            t.khiVanIds = ids;
            t.khiVanRollCharges--;
            if (t.lifespanMax < 1) {
                t.lifespanMax = 1;
            }
            if (t.lifespan > t.lifespanMax) {
                t.lifespan = t.lifespanMax;
            }
            if (t.lifespan < 0) {
                t.lifespan = 0;
            }
        }
        sendTuTienFly(pl, "Khí Vận x" + slots + ": " + names);
        sendRefresh(pl);
    }

    // So luong khi van giu dong thoi theo canh gioi (LK=1, Truc Co=2, Kim Dan=3, Nguyen Anh=4 = 1 + cg/2)
    public int khiVanSlots(int canhGioi) {
        return 1 + Math.max(1, canhGioi) / 2;
    }

    private void ensureKhiVan(TuTien t) {
        if (t.khiVanIds == null) {
            // migrate field cu khiVanId (single) -> mang; stats da bake nen KHONG apply lai
            t.khiVanIds = (t.khiVanId != 0) ? new int[]{t.khiVanId} : new int[0];
        }
    }

    private ThienTienTemplate rollOneTrait(List<ThienTienTemplate>[] p, int totalW) {
        int roll = ROLL_RNG.nextInt(totalW);
        int chosen = 1;
        for (int r = 1; r <= 5; r++) {
            if (p[r].isEmpty()) {
                continue;
            }
            roll -= RARITY_WEIGHT[r];
            if (roll < 0) {
                chosen = r;
                break;
            }
        }
        List<ThienTienTemplate> tier = p[chosen];
        return tier.get(ROLL_RNG.nextInt(tier.size()));
    }

    // ── M4 Cong Phap & Vo Hoc: hoc bi kip tang linh can / tu chat ─────────────
    private void ensureCongPhap(TuTien t) {
        if (t.congPhap == null || t.congPhap.length != 14) {
            t.congPhap = new int[14];
        }
        if (t.congPhapExp == null || t.congPhapExp.length != 14) {
            t.congPhapExp = new int[14];
        }
    }

    // Tran cap bi kip o canh gioi hien tai (Luyen Khi=5; canh gioi cao nang tran)
    public int maxCongPhapLv(int canhGioi) {
        return CONG_PHAP_LV_PER_REALM * Math.max(1, canhGioi);
    }

    // Exp can de len tu cap L -> L+1
    private long expForLevel(int level) {
        return (long) CONG_PHAP_EXP_BASE * Math.max(1, level);
    }

    // Cong exp cho 1 bi kip DA hoc, xu ly len nhieu cap; tra ve true neu co len cap (caller giu lock t)
    private boolean addExpOne(TuTien t, int idx, long amount, int cap) {
        if (idx < 0 || idx >= 14) {
            return false;
        }
        if (t.congPhap[idx] <= 0 || t.congPhap[idx] >= cap) {
            return false; // chua hoc hoac da max tran canh gioi
        }
        boolean leveled = false;
        long exp = (long) t.congPhapExp[idx] + amount;
        long need = expForLevel(t.congPhap[idx]);
        while (t.congPhap[idx] < cap && exp >= need) {
            exp -= need;
            t.congPhap[idx]++;
            applyCongPhap(t, idx, 1);
            leveled = true;
            need = expForLevel(t.congPhap[idx]);
        }
        if (t.congPhap[idx] >= cap) {
            exp = 0; // da max -> khoa exp
        }
        t.congPhapExp[idx] = (int) Math.max(0, exp);
        return leveled;
    }

    // Moi bi kip DA hoc +gain exp; tra ve danh sach thong bao len cap (null neu khong co). Caller giu lock t.
    private List<String> addCongPhapExp(TuTien t, long gain) {
        ensureCongPhap(t);
        int cap = maxCongPhapLv(t.canhGioi);
        List<String> ups = null;
        for (int i = 0; i < 14; i++) {
            long g = (i == 7) ? gain / 3 : gain; // M7: Kiếm Phổ (idx7) exp ×3 (nhược điểm liên kích) -> nhận 1/3
            if (addExpOne(t, i, g, cap)) {
                if (ups == null) {
                    ups = new ArrayList<>();
                }
                ups.add(CONG_PHAP_NAME[i] + " đạt cấp " + t.congPhap[i]
                        + (t.congPhap[i] >= cap ? " (tối đa)" : ""));
            }
        }
        return ups;
    }

    private void applyCongPhap(TuTien t, int idx, int levels) {
        if (idx >= 0 && idx <= 5) {
            t.applyStat(CONG_PHAP_ROOT[idx], ROOT_PER_LV * levels);
        } else if (idx == 6) { // Bach Phap: tang ca 6 linh can, thap hon
            for (int a : CONG_PHAP_ROOT) {
                t.applyStat(a, BACH_PER_LV * levels);
            }
        } else if (idx >= 7 && idx <= 12) {
            t.applyStat(VO_HOC_APT[idx - 7], APT_PER_LV * levels);
        } else if (idx == 13) { // Bach Pho: tang ca 6 tu chat, thap hon
            for (int a : VO_HOC_APT) {
                t.applyStat(a, BACH_PER_LV * levels);
            }
        }
    }

    // Dung 1 bi tich cong phap (item 2002..2015). Chua hoc -> hoc Cap 1; da hoc -> nhoi exp ~1 cap.
    // Tra ve true neu DA dung (UseItem se tru 1 item); false -> khong tieu item.
    public boolean useCongPhapItem(Player pl, int idx) {
        TuTien t = pl.tuTien;
        if (t == null) {
            sendTuTienFly(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên trước");
            return false;
        }
        if (t.disabled) {
            sendTuTienFly(pl, "Tuổi thọ đã cạn, không thể tu luyện công pháp");
            return false;
        }
        if (idx < 0 || idx >= 14) {
            return false;
        }
        ensureCongPhap(t);
        String msg;
        synchronized (t) {
            int cap = maxCongPhapLv(t.canhGioi);
            if (t.congPhap[idx] <= 0) {
                t.congPhap[idx] = 1;
                t.congPhapExp[idx] = 0;
                applyCongPhap(t, idx, 1);
                msg = "Đã học " + CONG_PHAP_NAME[idx] + " — Cấp 1";
            } else if (t.congPhap[idx] >= cap) {
                sendTuTienFly(pl, CONG_PHAP_NAME[idx]
                        + " đã đạt cấp tối đa của cảnh giới hiện tại (" + cap + ")");
                return false; // khong tieu item
            } else {
                addExpOne(t, idx, expForLevel(t.congPhap[idx]), cap); // nhoi ~1 cap
                msg = CONG_PHAP_NAME[idx] + " hấp thu bí tịch — Cấp " + t.congPhap[idx];
            }
        }
        sendTuTienFly(pl, msg);
        sendRefresh(pl);
        return true;
    }

    // Admin test (Command.java "tutien cpexp <1-14> <n>"): cong thang exp vao 1 bi kip (tu hoc neu chua)
    public void addCongPhapExpDirect(Player pl, int idx, long amount) {
        TuTien t = pl.tuTien;
        if (t == null || idx < 0 || idx >= 14) {
            return;
        }
        ensureCongPhap(t);
        synchronized (t) {
            if (t.congPhap[idx] <= 0) {
                t.congPhap[idx] = 1;
                applyCongPhap(t, idx, 1);
            }
            addExpOne(t, idx, amount, maxCongPhapLv(t.canhGioi));
        }
        sendRefresh(pl);
    }

    // Admin test (Command.java "tutien lk <n>"): cong linh khi truc tiep de kiem tra header
    public void addLinhKhi(Player pl, long amount) {
        TuTien t = pl.tuTien;
        if (t == null) {
            return;
        }
        synchronized (t) {
            t.linhKhi = Math.max(0, t.linhKhi + amount);
        }
        sendRefresh(pl);
    }

    // ── M3.5 Khí Vận (Phase 1.2): rơi phách khi GIẾT QUÁI (PvE) — Linh -> +linh khí, Tinh -> +tu vi ──
    // Chỉ hook mob-kill (Mob.java) để tránh exploit farm bằng giết player. Liệp (upgrade) ưu tiên hơn Thị.
    public void khiVanOnKillMob(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null || t.disabled || t.khiVanIds == null) {
            return;
        }
        KhiVanSummon.onKillMob(pl); // Phase 3: Quỷ Tu — % phục sinh quái thành vong linh đánh cho ngươi
        long lk = 0;
        if (KhiVanCombat.has(pl, 700108) && ROLL_RNG.nextInt(100) < (int) ThienTienParams.get("700108_dlgl", 15)) {
            lk = (long) ThienTienParams.get("700108_lk", 120);
        } else if (KhiVanCombat.has(pl, 700107) && ROLL_RNG.nextInt(100) < (int) ThienTienParams.get("700107_dlgl", 15)) {
            lk = (long) ThienTienParams.get("700107_lk", 60);
        }
        long tv = 0;
        if (KhiVanCombat.has(pl, 700110) && ROLL_RNG.nextInt(100) < (int) ThienTienParams.get("700110_dlgl", 15)) {
            tv = (long) ThienTienParams.get("700110_tv", 30);
        } else if (KhiVanCombat.has(pl, 700109) && ROLL_RNG.nextInt(100) < (int) ThienTienParams.get("700109_dlgl", 15)) {
            tv = (long) ThienTienParams.get("700109_tv", 15);
        }
        if (lk <= 0 && tv <= 0) {
            return;
        }
        long lkGain = 0, tvGain = 0;
        synchronized (t) {
            if (lk > 0) {
                t.linhKhi += lk;
                lkGain = lk;
            }
            if (tv > 0) {
                long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
                long before = t.tuVi;
                t.tuVi = Math.min(cap, t.tuVi + tv);
                tvGain = t.tuVi - before;
            }
        }
        if (lkGain > 0) {
            Service.gI().tuTienFlyEffect(pl, "Linh Phách +" + CongPhapCombat.numShort(lkGain), 4);
        }
        if (tvGain > 0) {
            Service.gI().tuTienFlyEffect(pl, "Tinh Phách +" + CongPhapCombat.numShort(tvGain), 7);
        }
        sendRefresh(pl);
    }

    // ── Gui data: action 0 = client MO panel, 1 = chi cap nhat (khong mo) ─────
    // M9: roll & roi Tien Duyen (item 2039) duoi dat khi giet quai/boss. chanceOneIn = 1000 (quai) | 20 (boss).
    // Drop duoi dat (ItemMap) giong Linh Thach boss; nhat len duoc 1-3 vien. Owner = killer.
    public void rollDropTienDuyen(map.Zone zone, int x, int y, long ownerId, int chanceOneIn) {
        if (zone == null || chanceOneIn <= 0) {
            return;
        }
        if (ROLL_RNG.nextInt(chanceOneIn) != 0) {
            return;
        }
        int qty = TIEN_DUYEN_DROP_MIN + ROLL_RNG.nextInt(TIEN_DUYEN_DROP_MAX - TIEN_DUYEN_DROP_MIN + 1);
        try {
            int dropY = zone.map.yPhysicInTop(x, y - 24);
            map.ItemMap im = new map.ItemMap(zone, ITEM_TIEN_DUYEN, qty,
                    x + utils.Util.nextInt(-20, 20), dropY, ownerId);
            services.Service.gI().dropItemMap(zone, im);
        } catch (Exception e) {
        }
    }

    public void sendAll(Player pl) {
        send(pl, 0);
    }

    public void sendRefresh(Player pl) {
        send(pl, 1);
    }

    private void send(Player pl, int action) {
        TuTien t = pl.tuTien;
        if (t == null) {
            sendTuTienFly(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên");
            return;
        }
        try {
            Message m = new Message(CMD_TUTIEN);
            synchronized (t) {
                m.writer().writeByte(action);
                m.writer().writeUTF(CanhGioi.nameOf(t.canhGioi));
                m.writer().writeUTF(CanhGioi.stageNameOf(t.canhGioi, t.tangGioi));
                m.writer().writeByte(t.canhGioi);
                m.writer().writeByte(t.tangGioi);
                m.writer().writeLong(t.tuVi);
                m.writer().writeLong(CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi));
                ensureMood(t); // M6: migrate tâm tình cho save cũ
                // 60 int theo DUNG thu tu khai bao field trong TuTien.java — chi duoc APPEND phia sau
                int[] vals = {
                        t.atk, t.def, t.luck, t.perception, t.charm,
                        t.comprehension, t.reputation, t.kickPower, t.kickRange, t.rangedAtk,
                        t.moveSpeed, t.speed, t.heartShield, t.backDamageTaken,
                        t.goodPath, t.goodPathValue, t.evilPath, t.evilPathValue,
                        t.hp, t.hpMax, t.mp, t.mpMax, t.stamina, t.staminaMax,
                        t.mental, t.mentalMax, t.mood, t.moodMax, t.health, t.healthMax,
                        t.lifespan, t.lifespanMax,
                        t.swordApt, t.saberApt, t.spearApt, t.fistApt, t.fingerApt,
                        t.palmApt, t.combatApt, t.allCombatApt,
                        t.swordMasterySpd, t.saberMasterySpd, t.spearMasterySpd, t.bodyMasterySpd,
                        t.lightningRoot, t.fireRoot, t.waterRoot, t.windRoot, t.woodRoot,
                        t.earthRoot, t.earthAttr, t.otherRoots, t.rootAttr, t.allRoots,
                        t.alchemyApt, t.refiningApt, t.fengShui, t.talisman,
                        t.cultivationGain, t.startSpiritStones
                };
                for (int v : vals) {
                    m.writer().writeInt(v);
                }
                m.writer().writeByte(t.meditating ? 1 : 0);
                m.writer().writeByte(t.disabled ? 1 : 0);
                m.writer().writeByte(t.phaCanh);
                // M3.5 khi van: luot roll con lai + so slot theo canh gioi + danh sach trait dang mang
                ensureKhiVan(t);
                int kvSlots = khiVanSlots(t.canhGioi);
                m.writer().writeByte(Math.min(127, Math.max(0, t.khiVanRollCharges)));
                m.writer().writeByte(kvSlots);
                m.writer().writeByte(t.khiVanIds.length);
                for (int kid : t.khiVanIds) {
                    ThienTienTemplate kv = (kid != 0) ? ThienTienTemplate.get(kid) : null;
                    m.writer().writeInt(kid);
                    m.writer().writeUTF(kv != null ? kv.name : "");
                    m.writer().writeByte(kv != null ? kv.rarity : 0);
                    m.writer().writeUTF(kv != null && kv.description != null ? kv.description : "");
                }
                // M4 cong phap: 14 level (idx 0-6 cong phap linh can, 7-13 vo hoc pho)
                ensureCongPhap(t);
                for (int i = 0; i < 14; i++) {
                    m.writer().writeByte(t.congPhap[i]);
                }
                // M4 them: linh khi (header) + tran cap canh gioi + exp/need tung bi kip (need=0 -> chua hoc/da max)
                int cpCap = maxCongPhapLv(t.canhGioi);
                m.writer().writeLong(t.linhKhi);
                m.writer().writeByte(cpCap);
                for (int i = 0; i < 14; i++) {
                    int lv = t.congPhap[i];
                    int need = (lv <= 0 || lv >= cpCap) ? 0 : (int) expForLevel(lv);
                    m.writer().writeInt(t.congPhapExp[i]);
                    m.writer().writeInt(need);
                }
                m.writer().writeByte(t.activeVoHoc); // M7: võ học đang dùng (0=không, 1-6)
                // M7 header: tốc độ nhận tu vi & linh khí HIỆN TẠI (mỗi phút) -> hiển thị "nhận được"
                m.writer().writeLong(tuViGainPerMin(t));
                m.writer().writeLong(linhKhiGainPerMin(t));
            }
            m.writer().flush();
            pl.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // M7: chọn võ học đang dùng (action 4; param 1-6 = Kiếm/Đao/Thương/Quyền/Chỉ/Chưởng; 0 hoặc chọn lại = bỏ chọn).
    // Hiệu ứng chiến đấu võ học áp theo võ học đang dùng (VoHocCombat).
    public void setActiveVoHoc(Player pl, int n) {
        TuTien t = pl.tuTien;
        if (t == null || n < 0 || n > 6) {
            return;
        }
        ensureCongPhap(t);
        synchronized (t) {
            if (n == 0 || n == t.activeVoHoc) {
                t.activeVoHoc = 0; // bỏ chọn / chạm lại cái đang dùng -> tắt
                sendTuTienFly(pl, "Đã ngừng dùng võ học");
            } else if (t.congPhap[6 + n] <= 0) {
                sendTuTienFly(pl, "Chưa học " + CONG_PHAP_NAME[6 + n] + " — dùng Bí Tịch để học trước");
                return;
            } else {
                t.activeVoHoc = n;
                sendTuTienFly(pl, "Đang dùng " + CONG_PHAP_NAME[6 + n]);
            }
        }
        sendRefresh(pl);
    }

    // ════════════════════════════════════════════════════════════════════════
    // M8 — LUYỆN ĐAN (luyện đan sư + đan phương). req user §luyện đan.
    // ════════════════════════════════════════════════════════════════════════

    // danSuLevel: 0 = CHƯA HỌC luyện đan (phải học ở NPC Quy Lão); >=1 = đã là Luyện Đan Sư.
    private void ensureDanSu(TuTien t) {
        if (t.danSuLevel < 0) t.danSuLevel = 0;
        if (t.danSuExp < 0) t.danSuExp = 0;
    }

    // Học Luyện Đan ở Quy Lão: chưa học -> thành Luyện Đan Sư cấp 1. Yêu cầu đã học Tu Tiên trước.
    public void learnDanSu(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null) {
            sendTuTienFly(pl, "Hãy học Tu Tiên trước rồi mới học được Luyện Đan");
            return;
        }
        synchronized (t) {
            ensureDanSu(t);
            if (t.danSuLevel >= DAN_SU_LEVEL_MIN) {
                sendTuTienFly(pl, "Con đã là Luyện Đan Sư cấp " + t.danSuLevel + " rồi");
                return;
            }
            // M9 Gate 1: phai dat Luyen Khi Tang LUYEN_DAN_REQ_TANG tro len
            boolean reachTang = t.canhGioi > CanhGioi.LUYEN_KHI
                    || (t.canhGioi == CanhGioi.LUYEN_KHI && t.tangGioi >= LUYEN_DAN_REQ_TANG);
            if (!reachTang) {
                Service.gI().sendThongBao(pl, "Can dat Luyen Khi Tang " + LUYEN_DAN_REQ_TANG
                        + " moi hoc duoc Luyen Dan (hien tai Tang " + t.tangGioi + ")");
                return;
            }
            // M9 Gate 2: can TIEN_DUYEN_HOC_LUYEN_DAN Tien Duyen
            int tdHave = countMaterial(pl, ITEM_TIEN_DUYEN);
            if (tdHave < TIEN_DUYEN_HOC_LUYEN_DAN) {
                Service.gI().sendThongBao(pl, "Can " + TIEN_DUYEN_HOC_LUYEN_DAN
                        + " Tien Duyen de hoc Luyen Dan (hien co " + tdHave + ")");
                return;
            }
            consumeMaterial(pl, ITEM_TIEN_DUYEN, TIEN_DUYEN_HOC_LUYEN_DAN);
            services.InventoryService.gI().sendItemBag(pl);
            t.danSuLevel = DAN_SU_LEVEL_MIN; // = 1
            t.danSuExp = 0;
        }
        sendTuTienFly(pl, "Chúc mừng! Con đã trở thành Luyện Đan Sư cấp 1.\n"
                + "Mở bảng Tu Tiên → tab Luyện Đan để luyện chế đan dược.");
        sendAlchemyData(pl);
    }

    // EXP cần để lên từ `level` -> `level+1` (0 nếu đã max cấp).
    public long danSuExpNeed(int level) {
        if (level >= DAN_SU_LEVEL_MAX) return 0L;
        return (long) DAN_EXP_BASE * Math.max(1, level);
    }

    // Cộng exp luyện đan, auto lên cấp khi đầy. Trả về số cấp tăng.
    private int addDanSuExp(TuTien t, long amount) {
        if (amount <= 0) return 0;
        t.danSuExp += amount;
        int gained = 0;
        long need;
        while (t.danSuLevel < DAN_SU_LEVEL_MAX && (need = danSuExpNeed(t.danSuLevel)) > 0 && t.danSuExp >= need) {
            t.danSuExp -= need;
            t.danSuLevel++;
            gained++;
        }
        if (t.danSuLevel >= DAN_SU_LEVEL_MAX) t.danSuExp = 0;
        return gained;
    }

    // Tỷ lệ thành công cho đan phương d với cấp đan sư hiện tại (0 = chưa đủ cấp -> không luyện được).
    public int danSuccessRate(TuTien t, DanPhuong d) {
        if (t.danSuLevel < d.reqLevel) return 0;
        return Math.min(DAN_RATE_CAP, DAN_BASE_RATE + DAN_RATE_PER_LV * (t.danSuLevel - d.reqLevel));
    }

    // Random số lượng đan (1..10). surplus = danSuLevel − reqLevel (>=0); realm = cảnh giới đan.
    private int rollDanQuantity(int surplus, int realm) {
        int step = DAN_QTY_STEP_BASE + DAN_QTY_PER_SURPLUS * Math.max(0, surplus)
                 - DAN_QTY_PER_REALM * Math.max(0, realm - 1);
        if (step < DAN_QTY_STEP_MIN) step = DAN_QTY_STEP_MIN;
        if (step > DAN_QTY_STEP_MAX) step = DAN_QTY_STEP_MAX;
        int qty = DAN_QTY_MIN;
        for (int q = DAN_QTY_MIN + 1; q <= DAN_QTY_MAX; q++) {
            if (ROLL_RNG.nextInt(100) < step) qty = q; else break;
        }
        return qty;
    }

    // Đếm tổng số lượng 1 nguyên liệu trong túi (cộng dồn nhiều stack).
    private int countMaterial(Player pl, int itemId) {
        int total = 0;
        try {
            for (item.Item it : pl.inventory.itemsBag) {
                if (it != null && it.isNotNullItem() && it.template.id == itemId) total += it.quantity;
            }
        } catch (Exception e) {}
        return total;
    }

    // Trừ `qty` nguyên liệu khỏi túi (cộng dồn nhiều stack). Gọi SAU khi đã check đủ.
    private void consumeMaterial(Player pl, int itemId, int qty) {
        int remain = qty;
        while (remain > 0) {
            item.Item it = services.InventoryService.gI().findItem(pl.inventory.itemsBag, itemId);
            if (it == null) break;
            int take = Math.min(remain, it.quantity);
            if (take <= 0) break; // an toàn chống loop vô hạn
            services.InventoryService.gI().subQuantityItemsBag(pl, it, take);
            remain -= take;
        }
    }

    // Tên viên đan từ item_template (fallback nếu đan chưa cấu hình).
    private String danName(DanPhuong d) {
        if (d.name != null && !d.name.isEmpty()) return d.name;
        try {
            models.Template.ItemTemplate tmp = services.ItemService.gI().getTemplate(d.pillItemId);
            if (tmp != null && tmp.name != null) return tmp.name;
        } catch (Exception e) {}
        return "Đan Dược #" + d.pillItemId;
    }

    // Trao `qty` item vào túi. Trả false nếu không tạo được (chưa có template) hoặc túi đầy.
    private boolean giveItem(Player pl, int itemId, int qty) {
        try {
            item.Item it = services.ItemService.gI().createNewItem((short) itemId, qty);
            if (it == null || it.template == null) return false;
            return services.InventoryService.gI().addItemBag(pl, it);
        } catch (Exception e) {
            return false;
        }
    }

    // Gửi catalog đan phương + thông tin đan sư (sub-action 2). Client cache -> màn chính & màn chọn.
    public void sendAlchemyData(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null) {
            sendTuTienFly(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên");
            return;
        }
        try {
            Message m = new Message(CMD_TUTIEN);
            synchronized (t) {
                ensureDanSu(t);
                m.writer().writeByte(2); // sub-action = alchemy data
                m.writer().writeByte(t.danSuLevel);
                m.writer().writeLong(t.danSuExp);
                m.writer().writeLong(danSuExpNeed(t.danSuLevel));
                java.util.Map<Integer, java.util.List<DanPhuong>> byRealm = DanPhuongTemplate.byRealm();
                m.writer().writeByte(byRealm.size());
                for (java.util.Map.Entry<Integer, java.util.List<DanPhuong>> e : byRealm.entrySet()) {
                    int realm = e.getKey();
                    java.util.List<DanPhuong> list = e.getValue();
                    m.writer().writeByte(realm);
                    m.writer().writeUTF(CanhGioi.nameOf(realm));
                    m.writer().writeByte(Math.min(127, list.size()));
                    int sent = 0;
                    for (DanPhuong d : list) {
                        if (sent++ >= 127) break;
                        m.writer().writeInt(d.id);
                        m.writer().writeInt(d.pillItemId);
                        m.writer().writeUTF(danName(d));
                        m.writer().writeUTF(d.description != null ? d.description : "");
                        m.writer().writeByte(d.reqLevel);
                        m.writer().writeByte(danSuccessRate(t, d));
                        m.writer().writeByte(t.danSuLevel >= d.reqLevel ? 1 : 0);
                        // M9: hien Linh Thach nhu 1 nguyen lieu them (req_level x1) -> client cu render duoc, khong can build
                        int ltCost = Math.max(1, d.reqLevel * LUYEN_DAN_LINH_THACH_PER_REQ);
                        m.writer().writeByte(Math.min(127, d.materials.size() + 1));
                        for (int[] mat : d.materials) {
                            m.writer().writeInt(mat[0]);
                            m.writer().writeShort(Math.min(30000, mat[1]));
                            m.writer().writeShort(Math.min(30000, countMaterial(pl, mat[0])));
                        }
                        m.writer().writeInt(ITEM_LINH_THACH);
                        m.writer().writeShort(Math.min(30000, ltCost));
                        m.writer().writeShort(Math.min(30000, countMaterial(pl, ITEM_LINH_THACH)));
                    }
                }
            }
            m.writer().flush();
            pl.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Luyện 1 viên đan theo id đan phương. Check cấp + nguyên liệu -> trừ -> roll -> trao đan + exp.
    public void craftDan(Player pl, int danPhuongId) {
        TuTien t = pl.tuTien;
        if (t == null) {
            sendTuTienFly(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên");
            return;
        }
        DanPhuong d = DanPhuongTemplate.get(danPhuongId);
        if (d == null || !d.active) {
            sendTuTienFly(pl, "Đan phương không tồn tại");
            return;
        }
        synchronized (t) {
            ensureDanSu(t);
            // 0) đã học Luyện Đan chưa? (học ở Quy Lão)
            if (t.danSuLevel < DAN_SU_LEVEL_MIN) {
                sendTuTienFly(pl, "Con chưa học Luyện Đan — hãy gặp Quy Lão ở Đảo Kame để học");
                return;
            }
            // 1) đủ cấp đan sư?
            if (t.danSuLevel < d.reqLevel) {
                sendTuTienFly(pl, "Cần Luyện Đan Sư cấp " + d.reqLevel + " mới luyện được " + danName(d));
                return;
            }
            // 1b) viên đan đã có item_template chưa? (tránh ăn mất nguyên liệu khi đan phương cấu hình thiếu)
            if (services.ItemService.gI().getTemplate(d.pillItemId) == null) {
                sendTuTienFly(pl, "Đan dược chưa cấu hình vật phẩm (pill_item_id=" + d.pillItemId + ")");
                return;
            }
            // 2) đủ nguyên liệu?
            for (int[] mat : d.materials) {
                if (countMaterial(pl, mat[0]) < mat[1]) {
                    sendTuTienFly(pl, "Không đủ nguyên liệu để luyện đan");
                    return;
                }
            }
            // 3) trừ nguyên liệu
            // M9: luyen dan tieu THEM Linh Thach = req_level x1 (check truoc khi tru, tranh an mat nguyen lieu)
            int ltCost = Math.max(1, d.reqLevel * LUYEN_DAN_LINH_THACH_PER_REQ);
            if (countMaterial(pl, ITEM_LINH_THACH) < ltCost) {
                sendTuTienFly(pl, "Can " + ltCost + " Linh Thach de luyen " + danName(d)
                        + " (hien co " + countMaterial(pl, ITEM_LINH_THACH) + ")");
                return;
            }
            for (int[] mat : d.materials) consumeMaterial(pl, mat[0], mat[1]);
            consumeMaterial(pl, ITEM_LINH_THACH, ltCost);
            services.InventoryService.gI().sendItemBag(pl);
            // 4) roll thành công + thưởng
            boolean success = ROLL_RNG.nextInt(100) < danSuccessRate(t, d);
            int levelsUp;
            if (success) {
                int qty = rollDanQuantity(t.danSuLevel - d.reqLevel, d.realm);
                boolean given = giveItem(pl, d.pillItemId, qty);
                levelsUp = addDanSuExp(t, DAN_EXP_SUCCESS_FLAT + (long) DAN_EXP_SUCCESS_PER_REQ * d.reqLevel);
                sendTuTienFly(pl, given
                        ? ("Luyện đan thành công! Nhận " + qty + " " + danName(d))
                        : "Luyện thành công nhưng túi đầy — đan bị thất lạc!");
            } else {
                levelsUp = addDanSuExp(t, DAN_EXP_FAIL_FLAT + (long) DAN_EXP_FAIL_PER_REQ * d.reqLevel);
                sendTuTienFly(pl, "Luyện đan thất bại, nguyên liệu hóa thành tro!");
            }
            if (levelsUp > 0) {
                sendTuTienFly(pl, "Luyện Đan Sư thăng cấp! Hiện tại: cấp " + t.danSuLevel);
            }
        }
        // 5) gửi lại data (cập nhật owned counts + đan sư + tỷ lệ)
        sendAlchemyData(pl);
    }

    // Flytext hien thi NGAY tren bang Tu Tien (sub-action 3) - thay sendThongBao cho thong bao luyen dan.
    public void sendTuTienFly(Player pl, String text) {
        if (pl == null) return;
        try {
            Message m = new Message(CMD_TUTIEN);
            m.writer().writeByte(3);
            m.writer().writeUTF(text != null ? text : "");
            m.writer().flush();
            pl.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Router CMD 124 client->server ─────────────────────────────────────────
    public void handleAction(Player pl, int action) {
        handleAction(pl, action, 0);
    }

    public void handleAction(Player pl, int action, int param) {
        if (pl.tuTien == null) {
            sendTuTienFly(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên");
            return;
        }
        switch (action) {
            case 0:
                sendAll(pl);
                break;
            case 1:
                toggleMeditate(pl);
                break;
            case 2:
                dotPha(pl);
                break;
            case 3:
                rollKhiVan(pl);
                break;
            case 4:
                setActiveVoHoc(pl, param); // M7: chọn võ học đang dùng
                break;
            case 5:
                phaCanh(pl); // M7: nút Phá Cảnh riêng — leo tầng Phá Cảnh Luyện Khí (14/15/16)
                break;
            case 6:
                sendAlchemyData(pl); // M8: client xin data luyện đan (catalog đan phương + đan sư)
                break;
        }
    }
}
