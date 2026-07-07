package tutien;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// ── M3.5 Thiên Tiên Khí Vận — bảng tham số proc ───────────────────────────────
// Resolve placeholder &<id>_<key>& trong thientien.json (vd 700016_fsgl = xác suất, 700016_dmg = %sát thương).
// Data GỐC không có giá trị số -> server tự định nghĩa ở data/tutien/thientien_params.json {"<key>": number}.
// File là TÙY CHỌN: thiếu key -> dùng default truyền trong code. Sửa số chỉ cần restart (reload lúc load template).
public final class ThienTienParams {

    private ThienTienParams() {
    }

    private static final Map<String, Double> P = new HashMap<>();
    public static final String PATH = "data/tutien/thientien_params.json";

    public static void load() {
        load(PATH);
    }

    public static void load(String path) {
        P.clear();
        try (Reader r = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            JsonObject o = new Gson().fromJson(r, JsonObject.class);
            if (o != null) {
                for (Map.Entry<String, JsonElement> e : o.entrySet()) {
                    try {
                        P.put(e.getKey(), e.getValue().getAsDouble());
                    } catch (Exception ignore) {
                        // bỏ qua key không phải số (vd _comment)
                    }
                }
            }
            Logger.getLogger(ThienTienParams.class.getName()).info("ThienTienParams loaded: " + P.size() + " keys");
        } catch (IOException ex) {
            Logger.getLogger(ThienTienParams.class.getName())
                  .log(Level.WARNING, "Khong load duoc " + path + " (dung default)", ex);
        }
    }

    public static double get(String key, double def) {
        Double v = P.get(key);
        return v != null ? v : def;
    }
}
