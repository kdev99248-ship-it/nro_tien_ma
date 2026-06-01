package tutien;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThanThongTemplate extends TuTienTemplate {

    public String element;      // "fire", "water", "wind", "lightning", "wood", "earth"
    public int    elementId;    // attribute ID of the primary root
    public int    requiredRoot; // minimum root value needed to equip

    // ── Static registry ───────────────────────────────────────────────────────
    private static final Map<Integer, ThanThongTemplate>          BY_ID      = new LinkedHashMap<>();
    private static final Map<String,  List<ThanThongTemplate>>    BY_ELEMENT = new LinkedHashMap<>();
    private static final Map<Integer, List<ThanThongTemplate>>    BY_RARITY  = new LinkedHashMap<>();

    public static final String       PATH     = "data/tutien/thanthuong.json";
    public static final TuTienLookup EFFECTS  = new TuTienLookup("data/tutien/thanthuong_effects.json");
    public static final TuTienLookup ELEMENTS = new TuTienLookup("data/tutien/thanthuong_elements.json");

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(StatEntry.class, StatEntry.DESERIALIZER)
            .create();

    // ── Load from JSON file ───────────────────────────────────────────────────
    public static void load() {
        load(PATH);
    }

    public static void load(String path) {
        BY_ID.clear();
        BY_ELEMENT.clear();
        BY_RARITY.clear();
        Type listType = new TypeToken<List<ThanThongTemplate>>() {}.getType();
        try (Reader r = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            List<ThanThongTemplate> list = GSON.fromJson(r, listType);
            if (list != null) {
                for (ThanThongTemplate t : list) {
                    BY_ID.put(t.id, t);
                    BY_ELEMENT.computeIfAbsent(t.element, k -> new ArrayList<>()).add(t);
                    BY_RARITY.computeIfAbsent(t.rarity,   k -> new ArrayList<>()).add(t);
                }
            }
            Logger.getLogger(ThanThongTemplate.class.getName())
                  .info("ThanThongTemplate loaded: " + BY_ID.size() + " entries");
        } catch (IOException ex) {
            Logger.getLogger(ThanThongTemplate.class.getName())
                  .log(Level.SEVERE, "Cannot load ThanThongTemplate from " + path, ex);
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public static ThanThongTemplate             get(int id)             { return BY_ID.get(id); }
    public static Map<Integer, ThanThongTemplate> getAll()              { return Collections.unmodifiableMap(BY_ID); }
    public static List<ThanThongTemplate>       getByElement(String el) { return BY_ELEMENT.getOrDefault(el, Collections.emptyList()); }
    public static List<ThanThongTemplate>       getByRarity(int rarity) { return BY_RARITY.getOrDefault(rarity, Collections.emptyList()); }
    public static int                           size()                   { return BY_ID.size(); }

    // Returns true if the given root value meets the requirement for this entry.
    public boolean canEquip(int rootValue) {
        return rootValue >= requiredRoot;
    }
}
