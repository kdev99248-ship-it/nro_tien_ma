package tutien;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TamPhapTemplate extends TuTienTemplate {

    // No description — Tam Pháp entries have no free-text description.

    // ── Static registry ───────────────────────────────────────────────────────
    private static final Map<Integer, TamPhapTemplate>          BY_ID     = new LinkedHashMap<>();
    private static final Map<Integer, List<TamPhapTemplate>>    BY_RARITY = new LinkedHashMap<>();

    public static final String       PATH    = "data/tutien/tamphap.json";
    public static final TuTienLookup EFFECTS = new TuTienLookup("data/tutien/tamphap_effects.json");

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(StatEntry.class, StatEntry.DESERIALIZER)
            .create();

    // ── Load from JSON file ───────────────────────────────────────────────────
    // JSON root: flat array [...]
    public static void load() {
        load(PATH);
    }

    public static void load(String path) {
        BY_ID.clear();
        BY_RARITY.clear();
        Type listType = new TypeToken<List<TamPhapTemplate>>() {}.getType();
        try (Reader r = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            List<TamPhapTemplate> list = GSON.fromJson(r, listType);
            if (list != null) {
                for (TamPhapTemplate t : list) {
                    BY_ID.put(t.id, t);
                    BY_RARITY.computeIfAbsent(t.rarity, k -> new ArrayList<>()).add(t);
                }
            }
            Logger.getLogger(TamPhapTemplate.class.getName())
                  .info("TamPhapTemplate loaded: " + BY_ID.size() + " entries");
        } catch (IOException ex) {
            Logger.getLogger(TamPhapTemplate.class.getName())
                  .log(Level.SEVERE, "Cannot load TamPhapTemplate from " + path, ex);
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public static TamPhapTemplate get(int id)                        { return BY_ID.get(id); }
    public static Map<Integer, TamPhapTemplate> getAll()             { return Collections.unmodifiableMap(BY_ID); }
    public static List<TamPhapTemplate> getByRarity(int rarity)      { return BY_RARITY.getOrDefault(rarity, Collections.emptyList()); }
    public static int size()                                          { return BY_ID.size(); }
}
