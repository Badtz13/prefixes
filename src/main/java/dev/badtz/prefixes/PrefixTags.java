package dev.badtz.prefixes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PrefixTags {
        private PrefixTags() {}

        public static final TagKey<Item> WEAPONS = TagKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath("prefixes", "weapons"));

        public static final TagKey<Item> TOOLS = TagKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath("prefixes", "tools"));

        public static boolean isWeapon(ItemStack stack) {
                return stack.is(WEAPONS);
        }

        public static boolean isTool(ItemStack stack) {
                return stack.is(TOOLS);
        }
}
