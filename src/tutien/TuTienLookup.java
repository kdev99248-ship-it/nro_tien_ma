package tutien;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// Generic id→text lookup for effects and attribute name tables.
// JSON format: [{id: N, description: "..."}, ...] or [{id: N, name: "..."}]
public class TuTienLookup {

    private static final Gson GSON = new Gson();

    private final Map<Integer, String> map = new HashMap<>();
    private final String               sourcePath;

    public TuTienLookup(String path) {
        this.sourcePath = path;
    }

    public void load() {
        map.clear();
        Type listType = new TypeToken<List<JsonObject>>() {}.getType();
        try (Reader r = new InputStreamReader(new FileInputStream(sourcePath), StandardCharsets.UTF_8)) {
            List<JsonObject> rows = GSON.fromJson(r, listType);
            if (rows == null) return;
            for (JsonObject row : rows) {
                int    id   = row.get("id").getAsInt();
                String text = row.has("description")
                        ? row.get("description").getAsString()
                        : row.get("name").getAsString();
                map.put(id, text);
            }
            Logger.getLogger(TuTienLookup.class.getName())
                  .info("TuTienLookup loaded " + map.size() + " entries from " + sourcePath);
        } catch (IOException ex) {
            Logger.getLogger(TuTienLookup.class.getName())
                  .log(Level.SEVERE, "Cannot load TuTienLookup from " + sourcePath, ex);
        }
    }

    public String get(int id)              { return map.getOrDefault(id, ""); }
    public Map<Integer, String> getAll()   { return Collections.unmodifiableMap(map); }
    public int size()                      { return map.size(); }
}
