package tutien;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HauThienTemplate extends TuTienTemplate {

    public String description;

    // ── Static registry ───────────────────────────────────────────────────────
    private static final Map<Integer, HauThienTemplate> BY_ID = new LinkedHashMap<>();

    public static final String       PATH    = "data/tutien/hauthien.json";
    public static final TuTienLookup EFFECTS = new TuTienLookup("data/tutien/hauthien_effects.json");

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
        Type listType = new TypeToken<List<HauThienTemplate>>() {}.getType();
        try (Reader r = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            List<HauThienTemplate> list = GSON.fromJson(r, listType);
            if (list != null) {
                for (HauThienTemplate t : list) BY_ID.put(t.id, t);
            }
            Logger.getLogger(HauThienTemplate.class.getName())
                  .info("HauThienTemplate loaded: " + BY_ID.size() + " entries");
        } catch (IOException ex) {
            Logger.getLogger(HauThienTemplate.class.getName())
                  .log(Level.SEVERE, "Cannot load HauThienTemplate from " + path, ex);
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public static HauThienTemplate get(int id)              { return BY_ID.get(id); }
    public static Map<Integer, HauThienTemplate> getAll()   { return Collections.unmodifiableMap(BY_ID); }
    public static int size()                                 { return BY_ID.size(); }
}
