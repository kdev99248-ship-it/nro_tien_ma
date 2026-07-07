package tutien;

import player.LinhDanhThue;
import player.Player;
import utils.Util;

// ── M3.5 Thiên Tiên Khí Vận Phase 3 — triệu hồi minion ──────────────────────────
// Tái dùng nguyên hệ LinhDanhThue (lính đánh thuê): tự tìm địch đánh (setAttackMode),
// tự despawn khi hết hạn (update -> dispose), client render sẵn.
// => KHÔNG cần build client, KHÔNG SQL (minion ephemeral, không lưu DB -> tan khi logout).
// Chỉ áp cho tu tiên đang mang trait (KhiVanCombat.has đã gate active + khiVanIds).
public final class KhiVanSummon {

    private KhiVanSummon() {
    }

    public static final int QUY_TU_1 = 700022, QUY_TU_2 = 700023, QUY_TU_3 = 700024; // giết địch -> phục sinh vong linh
    public static final int HOANG_CAN = 700025;                                       // dùng thần thông -> triệu hồi Lực Sĩ

    private static final String NAME_VONG_LINH = "Vong Linh";
    private static final String NAME_HOANG_CAN = "Hoàng Cân Lực Sĩ";

    // Hoàng Cân (700025): gọi mỗi đòn tấn công. Mang trait + chưa có Lực Sĩ sống + hết cooldown -> triệu hồi 1.
    // (Mô phỏng "lần đầu dùng thần thông trong trận" = khi đang giao tranh mà chưa có Lực Sĩ bên cạnh.)
    public static void onAttack(Player atk) {
        if (!KhiVanCombat.has(atk, HOANG_CAN)) {
            return;
        }
        long now = System.currentTimeMillis();
        long cdMs = (long) ThienTienParams.get("700025_cd", 30) * 1000L;
        if (now - atk.khiVanHoangCanLast < cdMs) {
            return;
        }
        if (countAlive(atk, NAME_HOANG_CAN) >= 1) {
            return;
        }
        atk.khiVanHoangCanLast = now;
        spawn(atk, (int) ThienTienParams.get("700025_sec", 120), NAME_HOANG_CAN);
        services.Service.gI().tuTienFlyEffect(atk, "Triệu Hồi Lực Sĩ", 6);
    }

    // Quỷ Tu (700022-24): gọi khi GIẾT QUÁI (mob-kill, tránh exploit giết player). % proc -> phục sinh 1 vong linh, cap zhsx.
    public static void onKillMob(Player atk) {
        int tier = quyTuTier(atk);
        if (tier == 0) {
            return;
        }
        int traitId = 700021 + tier; // 1->700022, 2->700023, 3->700024
        int proc = (int) ThienTienParams.get(traitId + "_zhgl", tier >= 3 ? 15 : 12);
        if (proc <= 0 || !Util.isTrue(proc, 100)) {
            return;
        }
        int max = (int) ThienTienParams.get(traitId + "_zhsx", tier);
        if (countAlive(atk, NAME_VONG_LINH) >= max) {
            return;
        }
        spawn(atk, (int) ThienTienParams.get("quy_tu_sec", 60), NAME_VONG_LINH);
        services.Service.gI().tuTienFlyEffect(atk, "Phục Sinh Vong Linh", 5);
    }

    // Cấp Quỷ Tu cao nhất đang mang (700024 > 700023 > 700022); 0 = không mang.
    private static int quyTuTier(Player atk) {
        if (KhiVanCombat.has(atk, QUY_TU_3)) {
            return 3;
        }
        if (KhiVanCombat.has(atk, QUY_TU_2)) {
            return 2;
        }
        if (KhiVanCombat.has(atk, QUY_TU_1)) {
            return 1;
        }
        return 0;
    }

    // Tạo 1 minion scale theo master (constructor copy 100% -> hạ về summon_*_pct), tự đánh, tự despawn.
    private static void spawn(Player master, int durationSec, String name) {
        // body/leg bị constructor#1 bỏ qua (chỉ dùng head+gender) -> truyền 0
        LinhDanhThue m = new LinhDanhThue(master, durationSec, name, master.head, 0, 0, master.gender);
        m.khiVanMinion = true; // tách khỏi hệ lính thuê trả phí (NPC QuyLaoKame bỏ qua)
        int dmgPct = (int) ThienTienParams.get("summon_dmg_pct", 35);
        int hpPct = (int) ThienTienParams.get("summon_hp_pct", 40);
        m.nPoint.dame = master.nPoint.dame * dmgPct / 100;
        m.nPoint.hpMax = master.nPoint.hpMax * hpPct / 100;
        m.nPoint.hp = m.nPoint.hpMax;
        master.linhDanhThueList.add(m);
        m.joinMapMaster();
        m.setAttackMode(true); // tự tìm địch tấn công
    }

    // Đếm minion CÒN SỐNG của khí vận (phân biệt với lính thuê trả phí qua tên).
    private static int countAlive(Player master, String name) {
        int n = 0;
        for (LinhDanhThue m : master.linhDanhThueList) {
            if (m != null && !m.isDie() && name.equals(m.name)) {
                n++;
            }
        }
        return n;
    }
}
