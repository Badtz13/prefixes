package dev.badtz.prefixes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public final class PrefixStats {
    public static final Identifier ITEMS_REFORGED =
            register("items_reforged", StatFormatter.DEFAULT);

    private PrefixStats() {}

    public static void initialize() {}

    private static Identifier register(String name, StatFormatter formatter) {
        Identifier id = Identifier.fromNamespaceAndPath(Prefixes.MOD_ID, name);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, name, id);
        Stats.CUSTOM.get(id, formatter);
        return id;
    }
}
