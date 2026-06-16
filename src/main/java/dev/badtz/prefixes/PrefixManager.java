package dev.badtz.prefixes;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

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
                    "Weapon prefix loaded: id={}, key={}, tier={}, weight={}, modifiers={}",
                    prefix.id(), prefix.translationKey(), prefix.tier(), prefix.weight(),
                    prefix.modifiers().size());
        }

        for (PrefixDefinition prefix : tools.values()) {
            Prefixes.LOGGER.info(
                    "Tool prefix loaded: id={}, key={}, tier={}, weight={}, modifiers={}",
                    prefix.id(), prefix.translationKey(), prefix.tier(), prefix.weight(),
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

    public static PrefixDefinition getRandom(PrefixType type, RandomSource random) {
        Map<Identifier, PrefixDefinition> pool = switch (type) {
            case WEAPON -> weapons;
            case TOOL -> tools;
        };

        return getWeightedRandom(new ArrayList<>(pool.values()), random);
    }

    public static PrefixDefinition getRandomApplicablePrefix(ItemStack stack, RandomSource random) {
        if (PrefixTags.isWeapon(stack)) {
            return getRandom(PrefixType.WEAPON, random);
        }

        if (PrefixTags.isTool(stack)) {
            return getRandom(PrefixType.TOOL, random);
        }

        return null;
    }

    private static PrefixDefinition getWeightedRandom(List<PrefixDefinition> pool,
            RandomSource random) {
        if (pool.isEmpty()) {
            return null;
        }

        int totalWeight = 0;

        for (PrefixDefinition prefix : pool) {
            totalWeight += Math.max(0, prefix.weight());
        }

        if (totalWeight <= 0) {
            return pool.get(random.nextInt(pool.size()));
        }

        int roll = random.nextInt(totalWeight);

        for (PrefixDefinition prefix : pool) {
            int weight = Math.max(0, prefix.weight());

            if (roll < weight) {
                return prefix;
            }

            roll -= weight;
        }

        return pool.getLast();
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
                        int tier = parseTier(json);
                        List<PrefixModifier> modifiers = parseModifiers(json);

                        String translationKey = "%s.%s.%s".formatted(Prefixes.MOD_ID,
                                type.translationPart, name.replace('/', '.'));

                        loaded.put(id, new PrefixDefinition(id, type, weight, translationKey, tier,
                                modifiers));
                    } catch (Exception e) {
                        Prefixes.LOGGER.error("Failed to load prefix {}", resourceId, e);
                    }
                });

        return Collections.unmodifiableMap(loaded);
    }

    private static int parseTier(JsonObject json) {
        if (!json.has("tier")) {
            return 0;
        }

        int tier = json.get("tier").getAsInt();

        if (tier < -2) {
            return -2;
        }

        if (tier > 2) {
            return 2;
        }

        return tier;
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
            String translationKey, int tier, List<PrefixModifier> modifiers) {
    }

    public record PrefixModifier(String attribute, String operation, double amount) {
    }
}
