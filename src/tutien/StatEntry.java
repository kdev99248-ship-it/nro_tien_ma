package tutien;

import com.google.gson.*;
import java.lang.reflect.Type;

public class StatEntry {

    public int    id;
    public double value;  // numeric magnitude
    public boolean exact; // true = set field to value; false = add delta

    public static final JsonDeserializer<StatEntry> DESERIALIZER = new JsonDeserializer<>() {
        @Override
        public StatEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject o  = json.getAsJsonObject();
            StatEntry  se = new StatEntry();
            se.id = o.get("id").getAsInt();
            JsonPrimitive v = o.get("value").getAsJsonPrimitive();
            if (v.isString()) {
                se.exact = true;
                se.value = Double.parseDouble(v.getAsString().substring(1)); // strip "="
            } else {
                se.exact = false;
                se.value = v.getAsDouble();
            }
            return se;
        }
    };
}
