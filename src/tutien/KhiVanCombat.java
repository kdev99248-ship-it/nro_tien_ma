package tutien;

import player.Player;
import utils.Util;

// ── M3.5 Thiên Tiên Khí Vận — hiệu ứng proc của trait ĐANG MANG (Phase 1) ──────
// Chỉ áp khi player ĐANG MANG trait (TuTien.khiVanIds) + là tu tiên (CongPhapCombat.active).
// Số % đọc từ ThienTienParams (data/tutien/thientien_params.json), có default fallback trong code.
// Phase 1.1 "Lực Tứ Xạ": % proc khi tấn công -> +sát thương nguyên tố (mô phỏng bắn cầu vào mục tiêu hiện tại).
public final class KhiVanCombat {

    private KhiVanCombat() {
    }

    // Trait id (elemental projectile "Lực Tứ Xạ")
    public static final int HOA_TU_XA = 700016, THUY_TU_XA = 700017, LOI_TU_XA = 700119, PHONG_TU_XA = 700121;
    public static final int SONG_LINH = 700050, TAM_LINH = 700060, TU_LINH = 700061; // Linh Cộng Sinh (đánh thêm chiêu)
    // ── Phase 2 (proc vừa) ─────────────────────────────────────────────────────
    public static final int HUYET_MA = 700028;                                    // Huyết Ma: hút máu khi đánh trúng
    public static final int TIEN_THU_HAU_CONG = 700033;                           // càng ăn dmg càng tăng dmg (ramp)
    public static final int YEU_THUAT_1 = 700001, YEU_THUAT_2 = 700002, YEU_THUAT_3 = 700003; // biến hình "gà con"
    private static final long RAMP_TIMEOUT_MS = 30000;  // rời giao tranh 30s -> reset tầng Tiên Thụ Hậu Công
    private static final int RAMP_MAX_STACKS = 100;     // trần tầng (cap = tssh × 100%)

    public static boolean has(Player p, int traitId) {
        if (!CongPhapCombat.active(p) || p.tuTien.khiVanIds == null) {
            return false;
        }
        for (int id : p.tuTien.khiVanIds) {
            if (id == traitId) {
                return true;
            }
        }
        return false;
    }

    // Gọi CUỐI NPoint.getDameAttack (sau công pháp + võ học). Trả dame + bonus nguyên tố.
    // Set relay flytext (khiVanFxText/Color) khi proc -> consume ở Player.injured / Mob để hiện trên nạn nhân.
    public static long attackerDamage(Player atk, long dame) {
        if (atk != null) {
            atk.khiVanFxText = null; // reset relay mỗi đòn (đòn trước bị né -> không carry sang đòn sau)
        }
        if (!CongPhapCombat.active(atk) || dame <= 0 || atk.tuTien.khiVanIds == null) {
            return dame;
        }
        KhiVanSummon.onAttack(atk); // Phase 3: Hoàng Cân — triệu hồi Lực Sĩ khi giao tranh (có cooldown)
        long bonus = 0;
        bonus += procElem(atk, HOA_TU_XA,   "700016_fsgl", "700016_dmg",  8, 60, dame, "Hỏa",   1); // cam
        bonus += procElem(atk, THUY_TU_XA,  "700017_fsgl", "700017_dmg",  8, 55, dame, "Thủy",  4); // lam
        bonus += procElem(atk, LOI_TU_XA,   "700119_lqgl", "700119_dmg", 10, 70, dame, "Lôi",   0); // vàng
        bonus += procElem(atk, PHONG_TU_XA, "700121_cfgl", "700121_dmg", 10, 50, dame, "Phong", 2); // lục
        // 1.3 Linh Cộng Sinh: % phóng thêm 1/2/3 lần (mô phỏng = +N× sát thương đòn hiện tại)
        bonus += procCast(atk, SONG_LINH, "700050_sfgl", 12, 1, dame);
        bonus += procCast(atk, TAM_LINH,  "700060_sfgl",  8, 2, dame);
        bonus += procCast(atk, TU_LINH,   "700061_sfgl",  5, 3, dame);
        // Phase 2 passive (KHÔNG flytext — thể hiện qua chính số sát thương): tà đạo + Tiên Thụ Hậu Công
        bonus += pathTaDamage(atk, dame);
        bonus += rampDamage(atk, dame);
        return dame + Math.max(0, bonus);
    }

    // 1 nguyên tố: nếu mang trait + roll proc% -> bonus = dame×dmg%, ghi relay flytext "<label> +X".
    private static long procElem(Player atk, int traitId, String procKey, String dmgKey,
                                 double defProc, double defDmgPct, long dame, String label, int color) {
        if (!has(atk, traitId)) {
            return 0;
        }
        int proc = (int) ThienTienParams.get(procKey, defProc);
        if (proc <= 0 || !Util.isTrue(proc, 100)) {
            return 0;
        }
        long bonus = dame * (long) ThienTienParams.get(dmgKey, defDmgPct) / 100;
        if (bonus > 0) {
            atk.khiVanFxText = label + " +" + CongPhapCombat.numShort(bonus);
            atk.khiVanFxColor = color;
        }
        return bonus;
    }

    // 1 trait Cộng Sinh: roll proc% -> bonus = dame×extra (mô phỏng phóng thêm 'extra' lần).
    private static long procCast(Player atk, int traitId, String procKey, double defProc, int extra, long dame) {
        if (!has(atk, traitId)) {
            return 0;
        }
        int proc = (int) ThienTienParams.get(procKey, defProc);
        if (proc <= 0 || !Util.isTrue(proc, 100)) {
            return 0;
        }
        atk.khiVanFxText = "Cộng Sinh x" + (extra + 1);
        atk.khiVanFxColor = 3; // tím
        return dame * extra;
    }

    // ── Phase 2 ────────────────────────────────────────────────────────────────

    // Tà đạo (ma đạo > chính đạo): +dmg% theo chênh lệch path, trần path_ta_cap. Passive.
    private static long pathTaDamage(Player atk, long dame) {
        TuTien t = atk.tuTien; // attackerDamage đã chốt active + khiVanIds != null
        int net = (t.evilPath + t.evilPathValue) - (t.goodPath + t.goodPathValue);
        if (net <= 0) {
            return 0;
        }
        int div = (int) ThienTienParams.get("path_ta_div", 20);
        int cap = (int) ThienTienParams.get("path_ta_cap", 30);
        if (div <= 0) {
            return 0;
        }
        return dame * Math.min(cap, net / div) / 100;
    }

    // 700033 Tiên Thụ Hậu Công: +dmg% theo số tầng đã tích (tích ở onDamageTaken). Hết tầng nếu rời giao tranh.
    private static long rampDamage(Player atk, long dame) {
        if (!has(atk, TIEN_THU_HAU_CONG)) {
            return 0;
        }
        if (System.currentTimeMillis() - atk.khiVanRampLastHit > RAMP_TIMEOUT_MS) {
            atk.khiVanRampStacks = 0;
            atk.khiVanRampAccum = 0;
            return 0;
        }
        int per = (int) ThienTienParams.get("700033_tssh", 1);
        return dame * (long) atk.khiVanRampStacks * per / 100;
    }

    // Tổng hồi máu cho atk khi đánh TRÚNG (Huyết Ma hút máu + chính-đạo sustain). Passive — caller áp setHp.
    public static long healOnHit(Player atk, long dame) {
        if (!CongPhapCombat.active(atk) || dame <= 0 || atk.tuTien.khiVanIds == null) {
            return 0;
        }
        long heal = 0;
        if (has(atk, HUYET_MA)) {
            heal += dame * (long) ThienTienParams.get("700028_xxbl", 8) / 100;
        }
        TuTien t = atk.tuTien;
        int net = (t.goodPath + t.goodPathValue) - (t.evilPath + t.evilPathValue);
        if (net > 0) {
            int div = (int) ThienTienParams.get("path_chinh_div", 40);
            int cap = (int) ThienTienParams.get("path_chinh_cap", 15);
            if (div > 0) {
                heal += dame * Math.min(cap, net / div) / 100;
            }
        }
        return heal;
    }

    // 700033: gọi khi VICTIM ăn dmg -> tích dmg, đủ ngưỡng (ljsh% maxHP) thì +1 tầng (cap RAMP_MAX_STACKS).
    public static void onDamageTaken(Player victim, long dame) {
        if (dame <= 0 || !has(victim, TIEN_THU_HAU_CONG) || victim.nPoint == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - victim.khiVanRampLastHit > RAMP_TIMEOUT_MS) {
            victim.khiVanRampStacks = 0; // trận mới
            victim.khiVanRampAccum = 0;
        }
        victim.khiVanRampLastHit = now;
        int ljsh = (int) ThienTienParams.get("700033_ljsh", 10);
        long threshold = (long) victim.nPoint.hpMax * ljsh / 100;
        if (threshold <= 0) {
            return;
        }
        victim.khiVanRampAccum += dame;
        while (victim.khiVanRampAccum >= threshold && victim.khiVanRampStacks < RAMP_MAX_STACKS) {
            victim.khiVanRampAccum -= threshold;
            victim.khiVanRampStacks++;
        }
    }

    // Yêu Thuật biến hình "gà con" -> mô phỏng CHOÁNG. Vô hiệu với tu tiên giả (victim active). Trả ms hoặc 0.
    // 700003 > 700002 > 700001 (cấp cao thay cấp thấp). Caller (Player) áp startStun + flytext.
    public static int bienHinhStunMs(Player atk, Player victim) {
        if (!CongPhapCombat.active(atk) || atk.tuTien.khiVanIds == null) {
            return 0;
        }
        if (victim == null || CongPhapCombat.active(victim)) {
            return 0; // tu tiên giả miễn nhiễm
        }
        int ms = bienHinhTry(atk, YEU_THUAT_3, "700003_bxgl", 12, "700003_cxsj", 3000);
        if (ms == 0) {
            ms = bienHinhTry(atk, YEU_THUAT_2, "700002_bxgl", 10, "700002_cxsj", 2500);
        }
        if (ms == 0) {
            ms = bienHinhTry(atk, YEU_THUAT_1, "700001_bxgl", 8, "700001_cxsj", 2000);
        }
        return ms;
    }

    private static int bienHinhTry(Player atk, int traitId, String procKey, double defProc, String msKey, int defMs) {
        if (!has(atk, traitId)) {
            return 0;
        }
        int proc = (int) ThienTienParams.get(procKey, defProc);
        if (proc <= 0 || !Util.isTrue(proc, 100)) {
            return 0;
        }
        return (int) ThienTienParams.get(msKey, defMs);
    }
}
