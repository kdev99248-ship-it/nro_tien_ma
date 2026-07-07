package tutien;

import player.Player;
import utils.Util;

// ── M7 Hệ Thống Võ Học — hiệu ứng chiến đấu theo võ học ĐANG DÙNG (req: HỆ THỐNG VÕ HỌC) ─────────
// 1 võ học active tại 1 thời điểm (TuTien.activeVoHoc 1-6 = Kiếm/Đao/Thương/Quyền/Chỉ/Chưởng -> congPhap[6+w]).
// weaponPower = mastery + talent  (talent = tư chất vũ khí; mastery = level×3; cap 100 để các công thức %wp ổn).
// V1 (DONE): đòn tấn công Kiếm/Đao/Quyền (getDameAttack). V2: Thương/Chỉ (injured). V3: Chưởng AOE + Lv3 haste.
public final class VoHocCombat {

    private VoHocCombat() {
    }

    public static final int KIEM = 1, DAO = 2, THUONG = 3, QUYEN = 4, CHI = 5, CHUONG = 6;
    public static final int LV3 = 3;

    // ── Tunable (user duyệt: weaponPower cap 100; Kiếm hạ xuống wp/2 mỗi ảnh chống quá mạnh) ──────
    private static final int WP_CAP = 100;
    private static final int WP_PER_LV = 3;            // mastery = level × 3
    private static final int KIEM_SHADOWS = 3;         // Kiếm: 3 kiếm ảnh (Lv3 -> 6)
    private static final int KIEM_SHADOWS_LV3 = 6;
    private static final int DAO_LV3_MAX_CHANCE = 5;   // Đao Lv3: xác suất wp/20, trần 5% -> ×10
    private static final int DAO_LV3_MULT = 10;
    private static final int QUYEN_MAX_STACK = 50;     // Quyền: tối đa 50 tầng
    private static final int QUYEN_PER_STACK_PM = 5;   // 0.5%/tầng = 5/1000 (Lv3 = 10/1000)
    private static final int QUYEN_PER_STACK_PM_LV3 = 10;
    private static final long QUYEN_RESET_MS = 5000;   // rời giao tranh 5s -> mất hết tầng

    // Võ học đang dùng (1-6) nếu hợp lệ + ĐÃ học; 0 nếu không. Dùng guard chung của CongPhapCombat.
    public static int activeWeapon(Player p) {
        if (!CongPhapCombat.active(p)) {
            return 0;
        }
        int w = p.tuTien.activeVoHoc;
        if (w < 1 || w > 6 || level(p.tuTien, w) <= 0) {
            return 0;
        }
        return w;
    }

    public static int level(TuTien t, int w) {
        int idx = 6 + w;
        return (t != null && t.congPhap != null && idx >= 0 && idx < t.congPhap.length)
                ? Math.max(0, t.congPhap[idx]) : 0;
    }

    private static int aptitudeOf(TuTien t, int w) {
        if (t == null) {
            return 0;
        }
        int agg = Math.max(0, t.allCombatApt); // M3.5 Khí Vận: allCombatApt cộng vào MỌI tư chất vũ khí
        switch (w) {
            case KIEM:   return t.swordApt + agg;
            case DAO:    return t.saberApt + agg;
            case THUONG: return t.spearApt + agg;
            case QUYEN:  return t.fistApt + agg;
            case CHI:    return t.fingerApt + agg;
            case CHUONG: return t.palmApt + agg;
            default:     return 0;
        }
    }

    public static int weaponPower(TuTien t, int w) {
        return Math.min(WP_CAP, Math.max(0, aptitudeOf(t, w)) + level(t, w) * WP_PER_LV);
    }

    // ── V1: đòn tấn công — gọi CUỐI NPoint.getDameAttack (sau công pháp). Áp cả mob & player. ──────
    public static long attackerDamage(Player atk, long dame, boolean isCrit, boolean isAttackMob) {
        boolean prevCrit = (atk != null) && atk.voHocLastCrit;
        if (atk != null) {
            atk.voHocLastCrit = isCrit; // luôn cập nhật cho đòn KẾ (Đao đọc prevCrit)
            atk.voHocBaoKichFx = false; // reset cờ flytext "Bạo Kích!" mỗi đòn (relay sang injured)
        }
        int w = activeWeapon(atk);
        if (w == 0 || dame <= 0) {
            return dame;
        }
        TuTien t = atk.tuTien;
        int lvl = level(t, w);
        int wp = weaponPower(t, w);
        long bonus = 0;

        switch (w) {
            case KIEM: { // Liên kích — 3 (Lv3:6) kiếm ảnh, mỗi ảnh dame×Random(1, wp/2)%
                int shadows = (lvl >= LV3) ? KIEM_SHADOWS_LV3 : KIEM_SHADOWS;
                int range = Math.max(1, wp / 2);
                for (int i = 0; i < shadows; i++) {
                    bonus += dame * (1 + Util.nextInt(range)) / 100;
                }
                break;
            }
            case DAO: { // Bạo kích — đòn TRƯỚC chí mạng -> +dame×Random(1,wp)%; Lv3 wp/20%(≤5%) -> ×10
                if (prevCrit) {
                    bonus += dame * (1 + Util.nextInt(Math.max(1, wp))) / 100;
                }
                if (lvl >= LV3) {
                    int chance = Math.min(DAO_LV3_MAX_CHANCE, wp / 20);
                    if (chance > 0 && Util.isTrue(chance, 100)) {
                        bonus += dame * (DAO_LV3_MULT - 1); // dame + (×10-1)dame = ×10
                        atk.voHocBaoKichFx = true; // ×10 proc -> bật flytext "Bạo Kích!" (consume ở injured)
                    }
                }
                break;
            }
            case QUYEN: { // Càng đánh càng mạnh — +0.5%/tầng (Lv3 +1%), max 50, reset khi rời giao tranh
                long now = System.currentTimeMillis();
                if (now - atk.voHocLastAttackMs > QUYEN_RESET_MS) {
                    atk.voHocBattleStack = 0;
                }
                atk.voHocBattleStack = Math.min(QUYEN_MAX_STACK, atk.voHocBattleStack + 1);
                atk.voHocLastAttackMs = now;
                int perMille = (lvl >= LV3) ? QUYEN_PER_STACK_PM_LV3 : QUYEN_PER_STACK_PM;
                bonus += dame * ((long) atk.voHocBattleStack * perMille) / 1000;
                break;
            }
            case THUONG: { // Lv3 Long Thương Quán Nhật: 150% sát thương (xuyên giáp + đa mục tiêu ở injured/splash)
                if (lvl >= LV3) {
                    bonus += dame / 2;
                }
                break;
            }
            default:
                break;
        }
        return dame + Math.max(0, bonus);
    }

    // ── V2: Thương (bỏ giáp) + Chỉ (điểm huyệt) — gọi trong Player.injured / Mob.injured ───────────
    private static final int CHI_PHONG_MACH_MS = 5000; // Chỉ Lv3: Phong Mạch 5s
    private static final int CHI_HUYET_CAP_ATK_MULT = 3; // trần 300% công kích

    // Thương: % giáp bỏ qua (≤50). Áp lên tlGiap victim trong injured.
    public static int thuongArmorIgnorePct(Player atk) {
        int w = activeWeapon(atk);
        return (w == THUONG) ? Math.min(50, weaponPower(atk.tuTien, w) / 2) : 0;
    }

    // Thương: xác suất wp/2% xuyên TOÀN BỘ giáp (tlGiap = 0). Lv3 (Long Thương) LUÔN xuyên.
    public static boolean thuongFullPierceProc(Player atk) {
        int w = activeWeapon(atk);
        if (w != THUONG) {
            return false;
        }
        if (level(atk.tuTien, w) >= LV3) {
            return true;
        }
        return Util.isTrue(Math.max(1, weaponPower(atk.tuTien, w) / 2), 100);
    }

    // Chỉ điểm huyệt: proc wp/2% -> +victimHpMax×0.5% (trần 300% công kích). 0 nếu victim là Boss lớn (TG/sự kiện).
    public static long chiHuyetBonus(Player atk, long victimHpMax, boolean victimBigBoss) {
        int w = activeWeapon(atk);
        if (w != CHI || victimBigBoss || victimHpMax <= 0 || atk.nPoint == null) {
            return 0;
        }
        int wp = weaponPower(atk.tuTien, w);
        if (!Util.isTrue(Math.max(1, wp / 2), 100)) {
            return 0;
        }
        long bonus = victimHpMax / 200; // 0.5% HP tối đa mục tiêu
        long cap = (long) Math.max(0, atk.nPoint.dame) * CHI_HUYET_CAP_ATK_MULT;
        return Math.min(bonus, cap);
    }

    // Chỉ Lv3: điểm Phong Mạch (chặn hồi HP/Linh Khí) — tái dùng tuTienPhongMachExpire của công pháp.
    public static boolean chiLv3PhongMach(Player atk) {
        int w = activeWeapon(atk);
        return w == CHI && level(atk.tuTien, w) >= LV3;
    }

    public static int chiPhongMachMs() {
        return CHI_PHONG_MACH_MS;
    }

    // ── V3: Quyền Lv3 haste + Chưởng/Thương Lv3 AOE splash ────────────────────
    private static final int CHUONG_SPLASH_RANGE = 60;      // ~2 ô (px)
    private static final int CHUONG_SPLASH_RANGE_LV3 = 120; // ~4 ô
    private static final int THUONG_SPLASH_RANGE = 90;      // Long Thương xuyên đa mục tiêu

    // Quyền Lv3: +0.5% tốc đánh / tầng (giảm hồi chiêu) — dùng ở SkillService.effectiveCooldown
    public static int quyenHastePct(Player p) {
        int w = activeWeapon(p);
        if (w != QUYEN || level(p.tuTien, w) < LV3) {
            return 0;
        }
        return Math.max(0, p.voHocBattleStack) / 2; // 50 tầng -> 25%
    }

    // % sát thương lan: Chưởng proc wp% (50%, Lv3 100%); Thương Lv3 luôn 100%. ROLL proc bên trong -> gọi 1 LẦN.
    public static int aoeSplashPct(Player atk) {
        int w = activeWeapon(atk);
        if (w == CHUONG) {
            if (!Util.isTrue(Math.max(1, weaponPower(atk.tuTien, w)), 100)) {
                return 0;
            }
            return (level(atk.tuTien, w) >= LV3) ? 100 : 50;
        }
        if (w == THUONG && level(atk.tuTien, w) >= LV3) {
            return 100;
        }
        return 0;
    }

    public static int aoeSplashRange(Player atk) {
        int w = activeWeapon(atk);
        if (w == CHUONG) {
            return (level(atk.tuTien, w) >= LV3) ? CHUONG_SPLASH_RANGE_LV3 : CHUONG_SPLASH_RANGE;
        }
        if (w == THUONG) {
            return THUONG_SPLASH_RANGE;
        }
        return 0;
    }
}
