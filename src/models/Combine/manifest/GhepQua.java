package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import java.util.LinkedHashMap;
import java.util.Map;
import models.Combine.CombineService;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;

/**
 * Ghep Qua -> Nguyen lieu luyen dan (item 2017..2038).
 *
 * Luong: nguoi choi keo cac QUA thu hoach farm vao o combine (kieu Ba Hat Mit)
 * roi bam "Ghep". Server do CHUNG LOAI qua da bo vao voi bang cong thuc:
 *  - khop chung loai + du so luong -> tieu hao qua + tao 1 nguyen lieu.
 *  - khop chung loai nhung thieu  -> hien yeu cau so luong (goi y).
 *  - khong khop chung loai nao    -> bao khong hop cong thuc (tu kham pha).
 *
 * Cac bo "chung loai qua" cua moi cong thuc la DUY NHAT nen viec do la khong nhap nhang.
 * KHONG dung client/SQL: bang cong thuc hardcode duoi day.
 */
public class GhepQua {

    /** Mot cong thuc: output = nguyen lieu; fruitIds/fruitQtys = qua can (cung do dai). */
    public static class Recipe {
        public final short output;
        public final short[] fruitIds;
        public final int[] fruitQtys;

        public Recipe(short output, short[] fruitIds, int[] fruitQtys) {
            this.output = output;
            this.fruitIds = fruitIds;
            this.fruitQtys = fruitQtys;
        }
    }

    // Item qua thu hoach (tu crop_template.harvest_item_id):
    //   CaChua=1876  Chuoi=1927  DauTay=1928  CaRot=1941  DuaHau=1939  ThanhLong=1933
    //   CaTim=1937   HuongDuong=1935  Dua=1929  Bi=1877  Ngo=1879  Khe=1878
    public static final Recipe[] RECIPES = {
        // ── Bac 1 - Luyen Khi (1 loai qua thuong) ──
        new Recipe((short) 2017, new short[]{1876}, new int[]{10}),                 // Ngung Khi Thao
        new Recipe((short) 2018, new short[]{1927}, new int[]{10}),                 // Tu Linh Thao
        new Recipe((short) 2019, new short[]{1928}, new int[]{10}),                 // Huyet Thao
        new Recipe((short) 2020, new short[]{1941}, new int[]{10}),                 // Thanh Tam Thao
        // ── Bac 2 - Truc Co (combo qua thuong) ──
        new Recipe((short) 2021, new short[]{1939, 1876}, new int[]{15, 5}),        // Han Suong Thao
        new Recipe((short) 2022, new short[]{1933, 1928}, new int[]{15, 5}),        // Xich Duong Thao
        new Recipe((short) 2023, new short[]{1937, 1927}, new int[]{15, 5}),        // Tu Van Hoa
        new Recipe((short) 2024, new short[]{1935, 1941}, new int[]{15, 5}),        // Bach Nguyet Hoa
        new Recipe((short) 2025, new short[]{1929, 1939}, new int[]{15, 5}),        // Linh Chi Ngan Nam
        new Recipe((short) 2026, new short[]{1876, 1927, 1928}, new int[]{10, 10, 10}), // Cuu Diep Linh Hoa
        // ── Bac 3 - Kim Dan/Nguyen Anh (can Bi ngo) ──
        new Recipe((short) 2027, new short[]{1877, 1935}, new int[]{3, 10}),        // Kim Duong Hoa
        new Recipe((short) 2028, new short[]{1877, 1933}, new int[]{3, 10}),        // Hoa Linh Chi
        new Recipe((short) 2029, new short[]{1877, 1939}, new int[]{3, 10}),        // Bang Tam Lien
        new Recipe((short) 2030, new short[]{1877, 1928}, new int[]{5, 10}),        // Huyet Linh Qua
        new Recipe((short) 2031, new short[]{1877, 1937}, new int[]{5, 10}),        // Tu Hon Thao
        new Recipe((short) 2032, new short[]{1877, 1929, 1941}, new int[]{5, 10, 10}), // Thien Nien Huyet Sam
        // ── Bac 4 - Hoa Than tro len (can Ngo / Khe) ──
        new Recipe((short) 2033, new short[]{1879, 1877}, new int[]{3, 5}),         // Long Huyet Dang
        new Recipe((short) 2034, new short[]{1879, 1939}, new int[]{3, 15}),        // Huyen Bang Lien
        new Recipe((short) 2035, new short[]{1879, 1933}, new int[]{5, 15}),        // Tu Loi Hoa
        new Recipe((short) 2036, new short[]{1879, 1929}, new int[]{5, 15}),        // Hoa Long Qua
        new Recipe((short) 2037, new short[]{1878, 1879}, new int[]{2, 5}),         // Cuu Chuyen Kim Lien
        new Recipe((short) 2038, new short[]{1878, 1879, 1877}, new int[]{3, 8, 10}), // Hon Don Thanh Lien
    };

    /** Goi moi khi nguoi choi bo / doi qua trong o combine. */
    public static void showInfoCombine(Player player) {
        Npc npc = getNpc(player);
        Map<Short, Item> dragged = collectDragged(player);

        if (dragged.isEmpty()) {
            npc.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Bo cac loai Qua thu hoach vao o\nroi bam Ghep de tao Nguyen Lieu Luyen Dan.", "Dong");
            return;
        }

        Recipe r = matchByType(dragged);
        if (r == null) {
            npc.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    ConstFont.BOLD_RED + "May loai qua nay khong ghep thanh cong thuc nao.\n"
                            + ConstFont.BOLD_BLUE + "Thu doi loai / so luong qua khac xem...", "Dong");
            return;
        }

        // Da dung chung loai -> hien yeu cau so luong, xanh = du, do = thieu
        String outName = ItemService.gI().getTemplate(r.output).name;
        StringBuilder sb = new StringBuilder();
        sb.append(ConstFont.BOLD_BLUE).append("Ghep thanh: ").append(outName).append("\n");
        boolean enough = true;
        for (int i = 0; i < r.fruitIds.length; i++) {
            Item it = dragged.get(r.fruitIds[i]);
            int have = (it != null) ? it.quantity : 0;
            int need = r.fruitQtys[i];
            if (have < need) {
                enough = false;
            }
            String fname = ItemService.gI().getTemplate(r.fruitIds[i]).name;
            sb.append(have >= need ? ConstFont.BOLD_GREEN : ConstFont.BOLD_RED)
                    .append("Can ").append(fname).append(" x").append(need)
                    .append(" (co ").append(have).append(")\n");
        }

        if (enough) {
            npc.createOtherMenu(player, CombineService.MENU_GHEP_QUA_CONFIRM, sb.toString(), "Ghep", "Tu choi");
        } else {
            sb.append(ConstFont.BOLD_RED).append("Chua du so luong qua.");
            npc.createOtherMenu(player, ConstNpc.IGNORE_MENU, sb.toString(), "Dong");
        }
    }

    /** Goi khi bam "Ghep" o menu xac nhan. */
    public static void ghepQua(Player player) {
        Map<Short, Item> dragged = collectDragged(player);
        Recipe r = matchByType(dragged);
        if (r == null) {
            Service.gI().sendThongBao(player, "Cong thuc khong dung!");
            return;
        }

        // Kiem tra du so luong
        for (int i = 0; i < r.fruitIds.length; i++) {
            Item it = dragged.get(r.fruitIds[i]);
            if (it == null || !it.isNotNullItem() || it.quantity < r.fruitQtys[i]) {
                Service.gI().sendThongBao(player, "Chua du qua de ghep!");
                return;
            }
        }

        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hanh trang day, can 1 o trong!");
            return;
        }

        // Tieu hao qua
        for (int i = 0; i < r.fruitIds.length; i++) {
            Item it = dragged.get(r.fruitIds[i]);
            InventoryService.gI().subQuantityItemsBag(player, it, r.fruitQtys[i]);
        }

        // Tao nguyen lieu
        Item out = ItemService.gI().createNewItem(r.output, 1);
        InventoryService.gI().addItemBag(player, out);
        InventoryService.gI().sendItemBag(player);

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Ghep thanh cong: " + out.template.name + " x1");

        // Reset o combine
        player.combine.clearItemCombine();
        CombineService.gI().reOpenItemCombine(player);
    }

    /** Gom cac qua da bo vao o, theo template id (lay stack dau tien moi loai). */
    private static Map<Short, Item> collectDragged(Player player) {
        Map<Short, Item> m = new LinkedHashMap<>();
        if (player.combine == null || player.combine.itemsCombine == null) {
            return m;
        }
        for (Item it : player.combine.itemsCombine) {
            if (it != null && it.isNotNullItem() && !m.containsKey(it.template.id)) {
                m.put(it.template.id, it);
            }
        }
        return m;
    }

    /** Tim cong thuc co BO CHUNG LOAI qua trung khop chinh xac (chua xet so luong). */
    private static Recipe matchByType(Map<Short, Item> dragged) {
        for (Recipe r : RECIPES) {
            if (r.fruitIds.length != dragged.size()) {
                continue;
            }
            boolean ok = true;
            for (short fid : r.fruitIds) {
                if (!dragged.containsKey(fid)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                return r;
            }
        }
        return null;
    }

    private static Npc getNpc(Player player) {
        Npc npc = player.iDMark.getNpcChose();
        if (npc == null) {
            npc = CombineService.gI().baHatMit;
        }
        return npc;
    }
}
