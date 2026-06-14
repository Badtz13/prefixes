package dev.badtz.prefixes.loot;

import dev.badtz.prefixes.Prefixes;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class PrefixLoot {
    private PrefixLoot() {}

    public static void initialize() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Prefixes.id("apply_random_prefix"),
                ApplyRandomPrefixLootFunction.MAP_CODEC);

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }

            tableBuilder.modifyPools(pool -> {
                pool.apply(ApplyRandomPrefixLootFunction.builder());
            });
        });

        Prefixes.LOGGER.info("Prefix loot initialized");
    }
}
