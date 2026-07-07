package tutien;

import java.util.Collections;
import java.util.List;

public class TuTienTemplate {

    public static final TuTienLookup ATTRIBUTES =
            new TuTienLookup("data/tutien/attributes.json");

    public int          id;
    public String       name;
    public int          rarity;
    public List<StatEntry>  stats;
    public List<Integer>    effects;

    // Apply all stats from this template onto a TuTien character instance.
    public void applyTo(TuTien target) {
        if (stats == null) return;
        for (StatEntry s : stats) {
            if (s.exact) {
                target.setStatExact(s.id, (int) s.value);
            } else {
                target.applyStat(s.id, (int) s.value);
            }
        }
    }

      // ── Load all templates (call once at server startup) ─────────────────────
    public static void initTemplates() {
        ATTRIBUTES.load();
        ThienTienTemplate.load();
        ThienTienTemplate.EFFECTS.load();
        ThienTienParams.load(); // M3.5 Khí Vận: bảng % proc
        HauThienTemplate.load();
        HauThienTemplate.EFFECTS.load();
        TamPhapTemplate.load();
        TamPhapTemplate.EFFECTS.load();
        ThanThongTemplate.load();
        ThanThongTemplate.EFFECTS.load();
        ThanThongTemplate.ELEMENTS.load();
        DanPhuongTemplate.load(); // M8 Luyen Dan: dan phuong tu DB (tutien_dan_phuong*)
    }

    public List<StatEntry> getStats()   { return stats   != null ? stats   : Collections.emptyList(); }
    public List<Integer>   getEffects() { return effects != null ? effects : Collections.emptyList(); }
}
