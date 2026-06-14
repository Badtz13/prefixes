package dev.badtz.prefixes;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

public final class PrefixManager extends SimpleReloadListener<PrefixManager.PreparedPrefixes> {
    private static final Gson GSON = new Gson();

    private static Map<Identifier, PrefixDefinition> weapons = Map.of();
    private static Map<Identifier, PrefixDefinition> tools = Map.of();

    @Override
    protected PreparedPrefixes prepare(PreparableReloadListener.SharedState state) {
        ResourceManager manager = state.resourceManager();

        return new PreparedPrefixes(loadPrefixFolder(manager, "weapons", PrefixType.WEAPON),
                loadPrefixFolder(manager, "tools", PrefixType.TOOL));
    }

    @Override
    protected void apply(PreparedPrefixes prepared, PreparableReloadListener.SharedState state) {
        weapons = prepared.weapons();
        tools = prepared.tools();

        Prefixes.LOGGER.info("Loaded {} weapon prefixes and {} tool prefixes", weapons.size(),
                tools.size());

        for (PrefixDefinition prefix : weapons.values()) {
            Prefixes.LOGGER.info(
                    "Weapon prefix loaded: id={}, key={}, color={}, weight={}, modifiers={}",
                    prefix.id(), prefix.translationKey(), prefix.color(), prefix.weight(),
                    prefix.modifiers().size());
        }

        for (PrefixDefinition prefix : tools.values()) {
            Prefixes.LOGGER.info(
                    "Tool prefix loaded: id={}, key={}, color={}, weight={}, modifiers={}",
                    prefix.id(), prefix.translationKey(), prefix.color(), prefix.weight(),
                    prefix.modifiers().size());
        }
    }

    public static PrefixDefinition get(Identifier id) {
        PrefixDefinition weapon = weapons.get(id);

        if (weapon != null) {
            return weapon;
        }

        return tools.get(id);
    }

    public static Map<Identifier, PrefixDefinition> weapons() {
        return weapons;
    }

    public static Map<Identifier, PrefixDefinition> tools() {
        return tools;
    }

    private static Map<Identifier, PrefixDefinition> loadPrefixFolder(ResourceManager manager,
            String folder, PrefixType type) {
        Map<Identifier, PrefixDefinition> loaded = new LinkedHashMap<>();

        manager.listResources(folder, id -> id.getPath().endsWith(".json"))
                .forEach((resourceId, resource) -> {
                    try (InputStreamReader reader =
                            new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);

                        String path = resourceId.getPath();
                        String name = path.substring(folder.length() + 1,
                                path.length() - ".json".length());
                        Identifier id =
                                Identifier.fromNamespaceAndPath(resourceId.getNamespace(), name);

                        int weight = json.has("weight") ? json.get("weight").getAsInt() : 10;
                        String color = parseColor(json);
                        float scale = parseScale(json);
                        List<PrefixModifier> modifiers = parseModifiers(json);

                        String translationKey = "%s.%s.%s".formatted(Prefixes.MOD_ID,
                                type.translationPart, name.replace('/', '.'));

                        loaded.put(id, new PrefixDefinition(id, type, weight, translationKey, color,
                                scale, modifiers));
                    } catch (Exception e) {
                        Prefixes.LOGGER.error("Failed to load prefix {}", resourceId, e);
                    }
                });

        return Map.copyOf(loaded);
    }

    private static String parseColor(JsonObject json) {
        if (!json.has("display") || !json.get("display").isJsonObject()) {
            return "white";
        }

        JsonObject display = json.getAsJsonObject("display");

        if (!display.has("color")) {
            return "white";
        }

        return display.get("color").getAsString();
    }

    private static float parseScale(JsonObject json) {
        if (!json.has("display") || !json.get("display").isJsonObject()) {
            return 1.0f;
        }

        JsonObject display = json.getAsJsonObject("display");

        if (!display.has("scale")) {
            return 1.0f;
        }

        return display.get("scale").getAsFloat();
    }

    private static List<PrefixModifier> parseModifiers(JsonObject json) {
        if (!json.has("modifiers") || !json.get("modifiers").isJsonArray()) {
            return List.of();
        }

        JsonArray array = json.getAsJsonArray("modifiers");
        List<PrefixModifier> modifiers = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject entry = array.get(i).getAsJsonObject();

            String attribute = entry.get("attribute").getAsString();
            String operation = entry.get("operation").getAsString();
            double amount = entry.get("amount").getAsDouble();

            modifiers.add(new PrefixModifier(attribute, operation, amount));
        }

        return List.copyOf(modifiers);
    }

    public enum PrefixType {
        WEAPON("weapon"), TOOL("tool");

        public final String translationPart;

        PrefixType(String translationPart) {
            this.translationPart = translationPart;
        }
    }

    public record PreparedPrefixes(Map<Identifier, PrefixDefinition> weapons,
            Map<Identifier, PrefixDefinition> tools) {
    }

    public record PrefixDefinition(Identifier id, PrefixType type, int weight,
            String translationKey, String color, float scale, List<PrefixModifier> modifiers) {
    }

    public record PrefixModifier(String attribute, String operation, double amount) {
    }
}
