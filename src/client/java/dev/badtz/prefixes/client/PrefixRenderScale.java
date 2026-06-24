package dev.badtz.prefixes.client;

import dev.badtz.prefixes.PrefixManager;
import dev.badtz.prefixes.Prefixes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class PrefixRenderScale {
    private PrefixRenderScale() {}

    public static float getScale(ItemStack stack) {
        return getScale(stack.get(Prefixes.PREFIX));
    }

    public static float getScale(Identifier prefixId) {
        if (prefixId == null) {
            return 1.0F;
        }

        PrefixManager.PrefixDefinition prefix = PrefixManager.get(prefixId);

        if (prefix == null) {
            return 1.0F;
        }

        for (PrefixManager.PrefixModifier modifier : prefix.modifiers()) {
            if (!modifier.attribute().equals("minecraft:entity_interaction_range")
                    && !modifier.attribute().equals("minecraft:block_interaction_range")) {
                continue;
            }

            return switch (modifier.operation()) {
                case "add_multiplied_base", "add_multiplied_total" -> 1.0F
                        + (float) modifier.amount();
                case "add_value" -> 1.0F + (float) (modifier.amount() / 4.5D);
                default -> 1.0F;
            };
        }

        return 1.0F;
    }
}
