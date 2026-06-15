package tutien;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;

import network.Message;
import player.Player;
import services.Service;

// Service Tu Tien — M1 loi Luyen Khi Ky (thiet ke: docs/tutien-m1-plan.md, req: docs/req/req.md)
// Nguon su that: player.tuTien (null = chua kich hoat qua NPC Quy Lao).
// Persist: cot player.data_tutien (JSON Gson; field transient khong luu).
// Wire CMD 124 server->client: [byte action 0=mo panel|1=chi refresh][UTF canhGioi][UTF tang]
//   [byte cg][byte tg][long tuVi][long tuViMax][59 int][byte meditating][byte disabled][byte phaCanh]
//   [byte kvRollCharges][byte kvSlots][byte kvHeld][kvHeld×{int id,UTF name,byte rarity,UTF desc}][14 byte congPhapLv]
//   [long linhKhi][byte congPhapCap][14x {int exp, int need}]   (M4: need=0 -> chua hoc hoac da max)
// Client->server: [byte action] 0=xin data, 1=toggle da toa, 2=dot pha, 3=roll khi van
public class TuTienService {

    public static final int CMD_TUTIEN = 124;

    // ── Tunables — chinh pacing tai day ───────────────────────────────────────
    public static final long YEAR_MS            = 30L * 60 * 1000;          // 30 phut thuc = 1 nam tu tien (req §2)
    public static final long TU_VI_PER_YEAR     = 100L;                     // diem tu vi / nam o toc do 100%
    public static final long MS_PER_TUVI        = YEAR_MS / TU_VI_PER_YEAR; // 18_000 ms / diem
    public static final int  LIFESPAN_START     = 150;                      // tuoi tho khoi dau (req §3)
    public static final int  LIFESPAN_GIAI_DOAN = 10;  // +nam khi vao Trung Ky/Hau Ky/Vien Man (tang 4/7/10)
    public static final int  LIFESPAN_PHA_CANH  = 20;  // +nam moi lan Pha Canh (tang 14/15/16)
    public static final long MAX_TICK_DELTA     = 60_000L;                  // clamp chong burst sau lag/relog
    // ── Dan duoc M2 (req §8) ──────────────────────────────────────────────────
    public static final long TU_KHI_BUFF_MS     = YEAR_MS;     // Tu Khi Dan: +100% toc do trong 1 nam tu tien
    public static final int  BOI_NGUYEN_PERCENT = 30;          // Boi Nguyen Dan: +% cap tang hien tai (chia canh gioi)
    // Item id (item_template) cua 4 vien dan — xem sql/migration_m2_dan_duoc.sql
    public static final short ITEM_TU_KHI       = 1998;
    public static final short ITEM_BOI_NGUYEN   = 1999;
    public static final short ITEM_NGUYEN_LINH  = 2000;
    public static final short ITEM_HOAN_HON     = 2001;
    // ── M3 Thien Tien Khi Van (req §12) ───────────────────────────────────────
    // trong so roll theo rarity 1..5 (cang cao cang hiem); rarity 6 (Tien) chua co data
    public static final int[] RARITY_WEIGHT     = {0, 50, 30, 14, 5, 1};
    public static final int   ROLL_COST_GOLD    = 0; // 0 = mien phi (test). Doi gia roll khi van tai day
    // ── M4 Cong Phap & Vo Hoc (req §6-7) — 14 bi kip: idx 0-6 cong phap linh can, 7-13 vo hoc pho ─
    public static final int   ROOT_PER_LV       = 5; // cong phap chuyen he: +linh can / level
    public static final int   APT_PER_LV        = 5; // vo hoc chuyen mon: +tu chat / level
    public static final int   BACH_PER_LV       = 2; // Bach Phap/Bach Pho: thap hon (req)
    // Tran cap bi kip theo canh gioi: Luyen Khi=5, moi canh gioi +5 (req: canh gioi cao nang tran)
    public static final int   CONG_PHAP_LV_PER_REALM = 5;
    public static final int   CONG_PHAP_EXP_BASE     = 100; // exp len cap L->L+1 = BASE*L (Lv1->5 = 1000)
    // Linh khi hap thu: 100 diem/nam @x1 (= tu vi). Moi diem linh khi = +1 exp cho moi bi kip DA hoc.
    public static final long  LINH_KHI_PER_YEAR  = 100L;
    public static final long  MS_PER_LINH_KHI    = YEAR_MS / LINH_KHI_PER_YEAR;
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

    private static TuTienService instance;
    private static final Gson GSON = new Gson();
    private static final Random ROLL_RNG = new Random();
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
        pl.tuTien = newbie(pl);
        Service.gI().sendThongBao(pl, "Ngươi đã bước lên con đường tu tiên! Cảnh giới hiện tại: Luyện Khí Kỳ Tầng 1");
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

        // linh can va tu chat sinh ngau nhien nhung on dinh theo nhan vat
        Random r = new Random((long) pl.id);
        t.lightningRoot = 10 + r.nextInt(51);
        t.fireRoot = 10 + r.nextInt(51);
        t.waterRoot = 10 + r.nextInt(51);
        t.windRoot = 10 + r.nextInt(51);
        t.woodRoot = 10 + r.nextInt(51);
        t.earthRoot = 10 + r.nextInt(51);

        t.atk = 10;
        t.def = 10;
        t.rangedAtk = 5;
        t.kickPower = 10;
        t.kickRange = 1;
        t.speed = 10;
        t.moveSpeed = 10;
        t.luck = 10;
        t.perception = 10;
        t.charm = 10;
        t.comprehension = 10 + r.nextInt(31);

        t.hp = 100;
        t.hpMax = 100;
        t.mp = 100;
        t.mpMax = 100;
        t.stamina = 100;
        t.staminaMax = 100;
        t.mental = 100;
        t.mentalMax = 100;
        t.moodMax = 100;
        t.health = 100;
        t.healthMax = 100;
        t.lifespan = LIFESPAN_START;
        t.lifespanMax = LIFESPAN_START;

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

    // ── Tick moi ~1s tu Player.update() block 1 (chi player that) ─────────────
    public void tick(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null || t.disabled) {
            return;
        }
        boolean justDisabled = false;
        List<String> levelUps = null;
        synchronized (t) {
            long now = System.currentTimeMillis();
            long delta = (t.lastTickMs == 0) ? 0 : (now - t.lastTickMs);
            t.lastTickMs = now;
            if (delta <= 0 || delta > MAX_TICK_DELTA) {
                return;
            }
            // nam tu tien troi + tru tuoi tho (req §2, §3)
            t.msAccumYear += delta;
            while (t.msAccumYear >= YEAR_MS) {
                t.msAccumYear -= YEAR_MS;
                t.namTuTien++;
                t.lifespan--;
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
                long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
                if (t.tuVi < cap) {
                    long mult = (t.meditating ? 2 : 1) * (tuKhi ? 2 : 1); // da toa x2, Tu Khi x2 (cong don)
                    t.msAccumTuVi += delta * mult * Math.max(0, t.cultivationGain) / 100; // khi van xau co the lam cg<0
                    if (t.msAccumTuVi < 0) {
                        t.msAccumTuVi = 0;
                    }
                    long gain = t.msAccumTuVi / MS_PER_TUVI;
                    if (gain > 0) {
                        t.msAccumTuVi %= MS_PER_TUVI;
                        t.tuVi = Math.min(cap, t.tuVi + gain);
                    }
                }
                // M4: hap thu linh khi (da toa x2, Tu Khi x2) -> nuoi exp moi bi kip DA hoc.
                // Linh khi van tang ke ca khi tu vi day cap (luyen cong phap trong luc cho dot pha).
                long lkMult = (t.meditating ? 2L : 1L) * (tuKhi ? 2L : 1L);
                t.msAccumLinhKhi += delta * lkMult * Math.max(0, t.cultivationGain) / 100;
                if (t.msAccumLinhKhi < 0) {
                    t.msAccumLinhKhi = 0;
                }
                long lkGain = t.msAccumLinhKhi / MS_PER_LINH_KHI;
                if (lkGain > 0) {
                    t.msAccumLinhKhi %= MS_PER_LINH_KHI;
                    t.linhKhi += lkGain;
                    levelUps = addCongPhapExp(t, lkGain);
                }
                if (tuKhi) {
                    t.tuKhiBuffMs = Math.max(0, t.tuKhiBuffMs - delta);
                }
            }
        }
        if (justDisabled) {
            onDisabled(pl);
        } else if (levelUps != null && !levelUps.isEmpty()) {
            for (String s : levelUps) {
                Service.gI().sendThongBao(pl, s);
            }
            sendRefresh(pl);
        }
    }

    // het tuoi tho (req §3): go buff Pha Canh, dung tick, cho hoan hon
    private void onDisabled(Player pl) {
        pl.nPoint.calPoint();
        Service.gI().point(pl);
        Service.gI().sendThongBao(pl, "Tuổi thọ đã cạn kiệt! Toàn bộ hiệu lực tu tiên bị vô hiệu hóa, cần Âm Dương Hoàn Hồn Đan để khôi phục");
        sendRefresh(pl);
    }

    // ── Da toa (action 1): toc do x2, dung yen; huy khi di chuyen/bi danh/chet ─
    public void toggleMeditate(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null) {
            return;
        }
        if (t.disabled) {
            Service.gI().sendThongBao(pl, "Tuổi thọ đã cạn, không thể tu luyện");
            return;
        }
        if (pl.isDie()) {
            return;
        }
        t.meditating = !t.meditating;
        Service.gI().sendThongBao(pl, t.meditating
                ? "Bắt đầu đả tọa, tốc độ tu luyện x2. Di chuyển hoặc bị tấn công sẽ gián đoạn"
                : "Đã dừng đả tọa");
        sendRefresh(pl);
    }

    // goi tu PlayerService.playerMove / Player.injured / Player.setDie
    public void cancelMeditate(Player pl, String reason) {
        TuTien t = pl.tuTien;
        if (t == null || !t.meditating) {
            return;
        }
        t.meditating = false;
        Service.gI().sendThongBao(pl, reason);
        sendRefresh(pl);
    }

    // ── Dot pha (action 2) — M1: thanh cong 100% khi du tu vi ─────────────────
    public void dotPha(Player pl) {
        TuTien t = pl.tuTien;
        if (t == null) {
            return;
        }
        String notify;
        boolean recalc = false;
        synchronized (t) {
            if (t.disabled) {
                notify = "Tuổi thọ đã cạn, không thể đột phá";
            } else if (t.canhGioi == CanhGioi.LUYEN_KHI && t.tangGioi >= CanhGioi.stageCount(CanhGioi.LUYEN_KHI)) {
                notify = "Đã đủ điều kiện đột phá Trúc Cơ — sắp ra mắt";
            } else {
                long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
                if (t.tuVi < cap) {
                    notify = "Tu vi chưa đủ để đột phá (" + t.tuVi + "/" + cap + ")";
                } else {
                    t.tangGioi++;
                    t.tuVi = 0;
                    t.msAccumTuVi = 0;
                    t.khiVanRollCharges++; // moi cảnh giới nhỏ (đột phá) = +1 lượt roll khí vận
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
                    notify = "Đột phá thành công! " + CanhGioi.fullNameOf(t.canhGioi, t.tangGioi) + extra + " (+1 lượt roll khí vận)";
                }
            }
        }
        if (recalc) { // ngoai lock: calPoint nang va tu doc phaCanh/disabled
            pl.nPoint.calPoint();
            Service.gI().point(pl);
        }
        Service.gI().sendThongBao(pl, notify);
        sendRefresh(pl);
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
        Service.gI().sendThongBao(pl, "Sinh cơ đã khôi phục, trạng thái tu tiên trở lại!");
        sendRefresh(pl);
    }

    public void reset(Player pl) {
        if (pl.tuTien == null) {
            return;
        }
        pl.tuTien = null; // DAO se ghi NULL -> ve trang thai chua kich hoat
        pl.nPoint.calPoint();
        Service.gI().point(pl);
        Service.gI().sendThongBao(pl, "Đã xóa toàn bộ dữ liệu tu tiên");
    }

    // ── Dan duoc M2 (req §8) — moi ham tra ve true neu DA dung => UseItem tru 1 ─
    private boolean checkActivated(Player pl, TuTien t) {
        if (t == null) {
            Service.gI().sendThongBao(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên trước");
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
        Service.gI().sendThongBao(pl, "Tụ Khí Đan: +100% tốc độ tu luyện trong 1 năm tu tiên");
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
                Service.gI().sendThongBao(pl, "Tuổi thọ đã cạn, không thể bồi nguyên");
                return false;
            }
            long cap = CanhGioi.maxTuViPerStage(t.canhGioi, t.tangGioi);
            gain = Math.max(5L, cap * BOI_NGUYEN_PERCENT / 100 / t.canhGioi);
            t.tuVi = Math.min(cap, t.tuVi + gain);
        }
        Service.gI().sendThongBao(pl, "Bồi Nguyên Đan: +" + gain + " tu vi");
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
            Service.gI().sendThongBao(pl, "Nguyên Linh Đan chỉ có hiệu lực trong Luyện Khí Kỳ");
            return false;
        }
        synchronized (t) {
            t.mp = t.mpMax;
        }
        pl.nPoint.setMp(pl.nPoint.mpMax); // hoi 100% KI (linh luc) that
        services.PlayerService.gI().sendInfoHpMp(pl);
        Service.gI().sendThongBao(pl, "Nguyên Linh Đan: hồi 100% linh lực");
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
            Service.gI().sendThongBao(pl, "Âm Dương Hoàn Hồn Đan chỉ dùng khi tuổi thọ đã cạn");
            return false;
        }
        hoanHon(pl);
        return true;
    }

    // ── M3 Thien Tien Khi Van: roll trait tu thientien.json, ap stat vao TuTien ─
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
                if (!hasExact) {
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
            Service.gI().sendThongBao(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên trước");
            return;
        }
        if (t.disabled) {
            Service.gI().sendThongBao(pl, "Tuổi thọ đã cạn, không thể chuyển khí vận");
            return;
        }
        ensureKhiVan(t);
        if (t.khiVanRollCharges <= 0) {
            Service.gI().sendThongBao(pl, "Hết lượt roll — đột phá cảnh giới (mỗi tầng +1 lượt) để có thêm");
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
            Service.gI().sendThongBao(pl, "Dữ liệu khí vận chưa sẵn sàng");
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
        Service.gI().sendThongBao(pl, "Khí Vận x" + slots + ": " + names);
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
            if (addExpOne(t, i, gain, cap)) {
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
            Service.gI().sendThongBao(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên trước");
            return false;
        }
        if (t.disabled) {
            Service.gI().sendThongBao(pl, "Tuổi thọ đã cạn, không thể tu luyện công pháp");
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
                Service.gI().sendThongBao(pl, CONG_PHAP_NAME[idx]
                        + " đã đạt cấp tối đa của cảnh giới hiện tại (" + cap + ")");
                return false; // khong tieu item
            } else {
                addExpOne(t, idx, expForLevel(t.congPhap[idx]), cap); // nhoi ~1 cap
                msg = CONG_PHAP_NAME[idx] + " hấp thu bí tịch — Cấp " + t.congPhap[idx];
            }
        }
        Service.gI().sendThongBao(pl, msg);
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

    // ── Gui data: action 0 = client MO panel, 1 = chi cap nhat (khong mo) ─────
    public void sendAll(Player pl) {
        send(pl, 0);
    }

    public void sendRefresh(Player pl) {
        send(pl, 1);
    }

    private void send(Player pl, int action) {
        TuTien t = pl.tuTien;
        if (t == null) {
            Service.gI().sendThongBao(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên");
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
                // 59 int theo DUNG thu tu khai bao field trong TuTien.java — chi duoc APPEND phia sau
                int[] vals = {
                    t.atk, t.def, t.luck, t.perception, t.charm,
                    t.comprehension, t.reputation, t.kickPower, t.kickRange, t.rangedAtk,
                    t.moveSpeed, t.speed, t.heartShield, t.backDamageTaken,
                    t.goodPath, t.goodPathValue, t.evilPath, t.evilPathValue,
                    t.hp, t.hpMax, t.mp, t.mpMax, t.stamina, t.staminaMax,
                    t.mental, t.mentalMax, t.moodMax, t.health, t.healthMax,
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
            }
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
            Service.gI().sendThongBao(pl, "Hãy gặp Quy Lão ở Đảo Kame để học Tu Tiên");
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
        }
    }
}
