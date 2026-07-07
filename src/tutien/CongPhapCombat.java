package tutien;

import player.Player;
import utils.Util;

// ── M7 Hệ Thống Công Pháp — hiệu ứng chiến đấu nguyên tố (req: HỆ THỐNG CÔNG PHÁP §VII-XII) ──────
// Tách riêng để hook GỌN ở NPoint/Player (1 dòng/điểm). Số đã user duyệt 16/06 (bản rescale: phẳng->%, có trần).
// idx công pháp = TuTien.congPhap (khớp CONG_PHAP_ROOT): 0 Lôi,1 Hỏa,2 Thủy,3 Phong,4 Mộc,5 Thổ.
// Phase 2 (DONE): đòn tấn công (getDameAttack) + Thổ +HP (setHpMax). Phase 3: phòng thủ/DoT/hồi máu (injured/heal/tick).
public final class CongPhapCombat {

    private CongPhapCombat() {
    }

    public static final int LOI = 0, HOA = 1, THUY = 2, PHONG = 3, MOC = 4, THO = 5;

    // ── Tunable (user duyệt) ──────────────────────────────────────────────────
    private static final int LOI_PROC_RATE_PER_LV = 2;   // Lôi: tỷ lệ kích hoạt = lv×2%
    private static final int LOI_DMG_PER_LV = 3;         // Lôi: +dmg = dmg×(lv×3 + root/10)%
    private static final int PHONG_DMG_PER_LV = 2;       // Phong: kích hoạt root% -> +dmg = dmg×(lv×2)%
    private static final int THO_HP_ROOT_DIV = 10;       // Thổ: +HP% = lv×(root/10)
    private static final int THO_DMGADD_CAP_PCT = 50;    // Thổ Lv3 DamageAdd: trần 50% dmg gốc
    public static final int LV3 = 3;                     // ngưỡng "Hiệu Ứng Cấp 3" (mở từ Trúc Cơ do cap LK=Lv2)

    // ── Accessor chung ────────────────────────────────────────────────────────
    public static boolean active(Player p) {
        return p != null && p.isPl() && p.tuTien != null && !p.tuTien.disabled && p.tuTien.congPhap != null;
    }

    public static int lv(TuTien t, int idx) {
        return (t != null && t.congPhap != null && idx >= 0 && idx < t.congPhap.length)
                ? Math.max(0, t.congPhap[idx]) : 0;
    }

    public static int root(TuTien t, int idx) {
        if (t == null) {
            return 0;
        }
        // M3.5 Khí Vận: allRoots + otherRoots cộng vào MỌI linh căn (Phase 0 hồi sinh stat aggregate)
        int agg = Math.max(0, t.allRoots) + Math.max(0, t.otherRoots);
        switch (idx) {
            case LOI:   return Math.max(0, t.lightningRoot) + agg;
            case HOA:   return Math.max(0, t.fireRoot) + agg;
            case THUY:  return Math.max(0, t.waterRoot) + agg;
            case PHONG: return Math.max(0, t.windRoot) + agg;
            case MOC:   return Math.max(0, t.woodRoot) + agg;
            case THO:   return Math.max(0, t.earthRoot) + Math.max(0, t.earthAttr) + agg; // Khí Vận B 1915: earthAttr (thổ thuộc tính) cộng linh căn Thổ
            default:    return 0;
        }
    }

    // ── Phase 2: đòn tấn công — gọi CUỐI NPoint.getDameAttack (SAU crit, để Lôi/Phong ăn crit). ──
    // Áp cho CẢ mob & player. Trả dameAttack đã cộng hiệu ứng.
    public static long attackerDamage(Player atk, long dame, boolean isCrit, boolean isAttackMob) {
        if (!active(atk) || dame <= 0) {
            return dame;
        }
        TuTien t = atk.tuTien;
        long bonus = 0;

        // Lôi (§VII): kích hoạt lv×2% -> +dmg = dmg×(lv×3 + root/10)% (ăn crit vì tính trên dame sau crit)
        int loiLv = lv(t, LOI);
        if (loiLv > 0 && Util.isTrue(loiLv * LOI_PROC_RATE_PER_LV, 100)) {
            bonus += dame * (loiLv * LOI_DMG_PER_LV + root(t, LOI) / 10) / 100;
        }

        // Phong (§X): kích hoạt root% -> +dmg = dmg×(lv×2)%
        int phongLv = lv(t, PHONG);
        if (phongLv > 0 && Util.isTrue(root(t, PHONG), 100)) {
            bonus += dame * (phongLv * PHONG_DMG_PER_LV) / 100;
        }

        // Thổ Lv3 (§XII): DamageAdd = maxHP×root/100, trần 50% dmg gốc (chống runaway HP->dmg)
        if (lv(t, THO) >= LV3) {
            long add = (long) atk.nPoint.hpMax * root(t, THO) / 100;
            bonus += Math.min(add, dame * THO_DMGADD_CAP_PCT / 100);
        }

        return dame + Math.max(0, bonus);
    }

    // ── Phase 2: Thổ basic (§XII) — +HP% = lv×(root/10). Gọi CUỐI NPoint.setHpMax (sau tlTuTien). ──
    public static int thoHpBonusPct(Player p) {
        if (!active(p)) {
            return 0;
        }
        TuTien t = p.tuTien;
        int lv = lv(t, THO);
        if (lv <= 0) {
            return 0;
        }
        return lv * (root(t, THO) / THO_HP_ROOT_DIV); // vd Lv10 root50 -> 10×5 = +50% HP tối đa
    }

    // ── Phase 3 tunable (user duyệt) ──────────────────────────────────────────
    private static final int THUY_REDUCE_PER_LV = 3;      // Thủy (§IX) giảm sát thương = lv×3%
    private static final int THUY_REFLECT_ROOT_DIV = 10;  // Thủy phản đòn = (lv + root/10)%
    private static final int THUY_REFLECT_LV3_MULT = 3;   // Lv3: phản đòn ×3
    private static final int NE_LV3_ROOT_DIV = 3;         // Phong né Lv3 = min(50, root/3 + lv×2)%
    private static final int NE_LV3_PER_LV = 2;
    private static final int NE_CAP_PCT = 50;
    private static final int MOC_HEAL_BONUS_PCT = 250;    // Mộc (§XI) +250% hồi phục
    private static final int MOC_TRUE_CAP_PCT = 25;       // Mộc Lv3 true dmg trần 25% maxHP nạn nhân
    private static final int MOC_HEAL_ON_HIT_DIV = 100;   // Mộc Lv3 hồi 1% maxHP / đòn
    private static final int HOA_BURN_CAP_PCT = 30;       // Hỏa (§VIII) trần tổng burn 30% maxHP / nhịp
    private static final int PHONG_MACH_MS = 3000;        // Phong Mạch chặn hồi máu 3s (refresh mỗi đòn)
    public static final long BURN_TICK_MS = 1000;         // burn đánh mỗi 1s
    public static final long BURN_DURATION_MS = 4000;     // burn kéo dài 4s (refresh mỗi đòn)
    private static final int LOI_STUN_RATE_PER_LV = 2;    // Lôi Lv3 choáng = lv×2%
    private static final int LOI_STUN_MS = 1500;          // choáng 1.5s

    // Phong né (§X Lv3) — % né, trần 50
    public static int neRatePct(Player victim) {
        if (!active(victim)) {
            return 0;
        }
        TuTien t = victim.tuTien;
        int lv = lv(t, PHONG);
        if (lv < LV3) {
            return 0;
        }
        return Math.min(NE_CAP_PCT, root(t, PHONG) / NE_LV3_ROOT_DIV + lv * NE_LV3_PER_LV);
    }

    // Thủy giảm sát thương (§IX) — trả damage sau giảm
    public static long thuyReduce(Player victim, long damage) {
        if (!active(victim) || damage <= 0) {
            return damage;
        }
        int lv = lv(victim.tuTien, THUY);
        if (lv <= 0) {
            return damage;
        }
        return Math.max(0, damage - damage * (lv * THUY_REDUCE_PER_LV) / 100);
    }

    // Thủy phản đòn (§IX) — sát thương phản về kẻ tấn công (= % damage đã nhận)
    public static long thuyReflect(Player victim, long damageTaken) {
        if (!active(victim) || damageTaken <= 0) {
            return 0;
        }
        TuTien t = victim.tuTien;
        int lv = lv(t, THUY);
        if (lv <= 0) {
            return 0;
        }
        int pct = lv + root(t, THUY) / THUY_REFLECT_ROOT_DIV;
        if (lv >= LV3) {
            pct *= THUY_REFLECT_LV3_MULT;
        }
        return damageTaken * pct / 100;
    }

    // Mộc Lv3 (§XI) — true dmg cộng thêm mỗi đòn = maxHP_attacker × lv/100, trần 25% maxHP nạn nhân;
    //   không tác dụng lên nạn nhân thuộc tính Lôi/Hỏa (căn Lôi hoặc Hỏa cao nhất).
    public static long mocTrueDamage(Player atk, Player victim) {
        if (!active(atk) || victim == null || victim.nPoint == null) {
            return 0;
        }
        int lv = lv(atk.tuTien, MOC);
        if (lv < LV3 || isFireOrLightningAttuned(victim)) {
            return 0;
        }
        long dmg = (long) atk.nPoint.hpMax * lv / 100;
        long cap = (long) victim.nPoint.hpMax * MOC_TRUE_CAP_PCT / 100;
        return Math.max(0, Math.min(dmg, cap));
    }

    // Mộc Lv3 — kẻ tấn công hồi 1% maxHP mỗi đòn
    public static long mocHealOnHit(Player atk) {
        if (!active(atk) || lv(atk.tuTien, MOC) < LV3) {
            return 0;
        }
        return (long) atk.nPoint.hpMax / MOC_HEAL_ON_HIT_DIV;
    }

    private static boolean isFireOrLightningAttuned(Player victim) {
        if (!active(victim)) {
            return false;
        }
        TuTien t = victim.tuTien;
        int max = 0;
        int[] all = {root(t, LOI), root(t, HOA), root(t, THUY), root(t, PHONG), root(t, MOC), root(t, THO)};
        for (int v : all) {
            max = Math.max(max, v);
        }
        return max > 0 && (root(t, LOI) == max || root(t, HOA) == max);
    }

    // Phong Mạch (§X) chặn 100% hồi máu / Mộc (§XI) +250% hồi máu + miễn Phong Mạch. Gọi trong NPoint.addHp.
    public static long modifyHeal(Player target, long hp) {
        if (!active(target) || hp <= 0) {
            return hp;
        }
        boolean hasMoc = lv(target.tuTien, MOC) > 0;
        if (!hasMoc && target.tuTienPhongMachExpire > System.currentTimeMillis()) {
            return 0; // Phong Mạch chặn hồi máu (Mộc miễn nhiễm)
        }
        if (hasMoc) {
            hp += hp * MOC_HEAL_BONUS_PCT / 100;
        }
        return hp;
    }

    // ── Hỏa thiêu đốt (§VIII) — apply trong Player, tick trong Player.update ──
    public static long hoaBurnPerStack(Player atk) {     // sát thương / stack / nhịp (snapshot lúc trúng)
        if (!active(atk)) {
            return 0;
        }
        int lv = lv(atk.tuTien, HOA);
        return lv <= 0 ? 0 : (long) atk.nPoint.hpMax * lv / 100;
    }

    public static int hoaBurnMaxStacks(Player atk) {     // trần stack = lv/2 (tối thiểu 1 khi có Hỏa)
        if (!active(atk)) {
            return 0;
        }
        int lv = lv(atk.tuTien, HOA);
        return lv <= 0 ? 0 : Math.max(1, lv / 2);
    }

    public static long burnTickCap(Player victim) {      // trần tổng 30% maxHP / nhịp
        return (victim == null || victim.nPoint == null) ? 0 : (long) victim.nPoint.hpMax * HOA_BURN_CAP_PCT / 100;
    }

    // Lôi Lv3 (§VII) choáng — % kích hoạt
    public static int loiStunRatePct(Player atk) {
        if (!active(atk) || lv(atk.tuTien, LOI) < LV3) {
            return 0;
        }
        return lv(atk.tuTien, LOI) * LOI_STUN_RATE_PER_LV;
    }

    public static boolean hasPhong(Player atk) {
        return active(atk) && lv(atk.tuTien, PHONG) > 0;
    }

    public static int phongMachMs() {
        return PHONG_MACH_MS;
    }

    public static int loiStunMs() {
        return LOI_STUN_MS;
    }

    // ── Hỗ trợ hiển thị flytext (server -> client) ─────────────────────────────
    // Nhãn thời gian: 1500ms -> "1.5", 4000ms -> "4", 3000ms -> "3".
    public static String secLabel(long ms) {
        if (ms % 1000 == 0) {
            return Long.toString(ms / 1000);
        }
        return (ms / 1000) + "." + (ms % 1000 / 100);
    }

    // Rút gọn số lớn cho flytext: 1234 -> "1.2K", 3_000_000 -> "3M", 1_500_000_000 -> "1.5B".
    public static String numShort(long v) {
        long a = Math.abs(v);
        if (a >= 1_000_000_000L) {
            return shortDiv(v, 1_000_000_000L) + "B";
        }
        if (a >= 1_000_000L) {
            return shortDiv(v, 1_000_000L) + "M";
        }
        if (a >= 1_000L) {
            return shortDiv(v, 1_000L) + "K";
        }
        return Long.toString(v);
    }

    private static String shortDiv(long v, long div) {
        long whole = v / div;
        long frac = Math.abs(v % div) * 10 / div;
        return (frac == 0) ? Long.toString(whole) : (whole + "." + frac);
    }
}
