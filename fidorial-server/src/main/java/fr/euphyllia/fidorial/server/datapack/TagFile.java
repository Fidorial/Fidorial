package fr.euphyllia.fidorial.server.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record TagFile(boolean replace, List<Entry> values) {

    public record Entry(String reference, boolean required) {

        public boolean isTag() {
            return reference.startsWith("#");
        }
    }

    public static TagFile parse(final JsonElement element) {
        final JsonObject object = element.getAsJsonObject();
        final boolean replace = object.has("replace") && object.get("replace").getAsBoolean();
        final List<Entry> entries = new ArrayList<>();
        if (object.has("values")) {
            for (final JsonElement value : object.getAsJsonArray("values")) {
                if (value.isJsonPrimitive()) {
                    entries.add(new Entry(value.getAsString(), true));
                } else if (value.isJsonObject()) {
                    final JsonObject entry = value.getAsJsonObject();
                    entries.add(new Entry(entry.get("id").getAsString(),
                            !entry.has("required") || entry.get("required").getAsBoolean()));
                }
            }
        }
        return new TagFile(replace, entries);
    }
}
