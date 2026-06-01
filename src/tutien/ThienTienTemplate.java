package tutien;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThienTienTemplate extends TuTienTemplate {

    public String description;

    // ── Static registry ───────────────────────────────────────────────────────
    private static final Map<Integer, ThienTienTemplate> BY_ID = new LinkedHashMap<>();

    public static final String       PATH    = "data/tutien/thientien.json";
    public static final TuTienLookup EFFECTS = new TuTienLookup("data/tutien/thientien_effects.json");

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(StatEntry.class, StatEntry.DESERIALIZER)
            .create();

    // ── Load from JSON file ───────────────────────────────────────────────────
    // JSON root: { "tien_thien": [...], "nghich_thien": [...] }
    public static void load() {
        load(PATH);
    }

    public static void load(String path) {
        BY_ID.clear();
        Type listType = new TypeToken<List<ThienTienTemplate>>() {}.getType();
        try (Reader r = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            addAll(GSON.fromJson(root.get("tien_thien"),   listType));
            addAll(GSON.fromJson(root.get("nghich_thien"), listType));
            Logger.getLogger(ThienTienTemplate.class.getName())
                  .info("ThienTienTemplate loaded: " + BY_ID.size() + " entries");
        } catch (IOException ex) {
            Logger.getLogger(ThienTienTemplate.class.getName())
                  .log(Level.SEVERE, "Cannot load ThienTienTemplate from " + path, ex);
        }
    }

    private static void addAll(List<ThienTienTemplate> list) {
        if (list == null) return;
        for (ThienTienTemplate t : list) BY_ID.put(t.id, t);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public static ThienTienTemplate get(int id)              { return BY_ID.get(id); }
    public static Map<Integer, ThienTienTemplate> getAll()   { return Collections.unmodifiableMap(BY_ID); }
    public static int size()                                  { return BY_ID.size(); }
}
