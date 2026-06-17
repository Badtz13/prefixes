package dev.badtz.prefixes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;

public final class PrefixApplier {
    private PrefixApplier() {}

    public static boolean isPrefixable(ItemStack stack) {
        return PrefixTags.isWeapon(stack) || PrefixTags.isTool(stack);
    }

    public static boolean canApply(ItemStack stack, PrefixManager.PrefixDefinition prefix) {
        return switch (prefix.type()) {
            case WEAPON -> PrefixTags.isWeapon(stack);
            case TOOL -> PrefixTags.isTool(stack);
        };
    }

    public static void apply(ItemStack stack, PrefixManager.PrefixDefinition prefix) {
        apply(stack, prefix, null);
    }

    public static void apply(ItemStack stack, PrefixManager.PrefixDefinition prefix,
            ServerPlayer player) {
        Identifier oldPrefixId = stack.get(Prefixes.PREFIX);
        boolean preserveCustomName = false;

        if (oldPrefixId != null) {
            PrefixManager.PrefixDefinition oldPrefix = PrefixManager.get(oldPrefixId);

            if (oldPrefix != null) {
                preserveCustomName = hasPlayerCustomName(stack, oldPrefix);
                applyAttributeModifiers(stack, oldPrefix, -1.0);
            }
        }

        stack.set(Prefixes.PREFIX, prefix.id());

        if (!preserveCustomName) {
            applyName(stack, prefix);
        }

        applyTierLore(stack, prefix);
        applyAttributeModifiers(stack, prefix, 1.0);
        Prefixes.awardFiveStarAdvancement(player, prefix);
    }

    public static void remove(ItemStack stack, PrefixManager.PrefixDefinition prefix) {
        applyAttributeModifiers(stack, prefix, -1.0);
        stack.remove(DataComponents.LORE);
    }

    public static boolean applyRandom(ItemStack stack, net.minecraft.util.RandomSource random) {
        PrefixManager.PrefixType type;

        if (PrefixTags.isWeapon(stack)) {
            type = PrefixManager.PrefixType.WEAPON;
        } else if (PrefixTags.isTool(stack)) {
            type = PrefixManager.PrefixType.TOOL;
        } else {
            return false;
        }

        PrefixManager.PrefixDefinition prefix = PrefixManager.getRandom(type, random);

        if (prefix == null) {
            return false;
        }

        apply(stack, prefix);
        return true;
    }

    private static boolean hasPlayerCustomName(ItemStack stack,
            PrefixManager.PrefixDefinition oldPrefix) {
        Component customName = stack.get(DataComponents.CUSTOM_NAME);

        if (customName == null) {
            return false;
        }

        Component expectedName = createPrefixName(stack, oldPrefix);
        return !customName.getString().equals(expectedName.getString());
    }

    private static void applyName(ItemStack stack, PrefixManager.PrefixDefinition prefix) {
        stack.set(DataComponents.CUSTOM_NAME, createPrefixName(stack, prefix));
    }

    private static Component createPrefixName(ItemStack stack,
            PrefixManager.PrefixDefinition prefix) {
        Component baseName = stack.getItemName();
        Component prefixName = Component.translatable(prefix.translationKey());

        return Component.empty().append(prefixName).append(Component.literal(" ")).append(baseName)
                .setStyle(net.minecraft.network.chat.Style.EMPTY
                        .withColor(Prefixes.rarityForTier(prefix.tier()).color())
                        .withItalic(false));
    }

    private static void applyTierLore(ItemStack stack, PrefixManager.PrefixDefinition prefix) {
        stack.set(DataComponents.LORE,
                new ItemLore(List.of(Component.literal(Prefixes.starsForTier(prefix.tier()))
                        .withStyle(style -> style
                                .withColor(Prefixes.rarityForTier(prefix.tier()).color())
                                .withItalic(false)))));
    }

    private static void applyAttributeModifiers(ItemStack stack,
            PrefixManager.PrefixDefinition prefix, double multiplier) {
        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(existing.modifiers());

        for (PrefixManager.PrefixModifier modifier : prefix.modifiers()) {
            Identifier attributeId = Identifier.tryParse(modifier.attribute());

            if (attributeId == null) {
                Prefixes.LOGGER.error("Invalid attribute id in prefix {}: {}", prefix.id(),
                        modifier.attribute());
                continue;
            }

            Optional<Holder.Reference<Attribute>> attribute =
                    BuiltInRegistries.ATTRIBUTE.get(attributeId);

            if (attribute.isEmpty()) {
                Prefixes.LOGGER.error("Unknown attribute in prefix {}: {}", prefix.id(),
                        modifier.attribute());
                continue;
            }

            boolean merged = false;

            for (int i = 0; i < entries.size(); i++) {
                ItemAttributeModifiers.Entry entry = entries.get(i);

                if (!entry.attribute().equals(attribute.get())) {
                    continue;
                }

                if (entry.slot() != EquipmentSlotGroup.MAINHAND) {
                    continue;
                }

                AttributeModifier old = entry.modifier();

                if (old.operation() != parseOperation(modifier.operation())) {
                    continue;
                }

                AttributeModifier replacement = new AttributeModifier(old.id(),
                        old.amount() + modifier.amount() * multiplier, old.operation());

                entries.set(i, new ItemAttributeModifiers.Entry(entry.attribute(), replacement,
                        entry.slot()));
                merged = true;
                break;
            }

            if (!merged && multiplier > 0.0) {
                Identifier modifierId = Prefixes.id("prefix/" + prefix.id().getNamespace() + "/"
                        + prefix.id().getPath() + "/" + attributeId.getPath());

                entries.add(new ItemAttributeModifiers.Entry(attribute.get(),
                        new AttributeModifier(modifierId, modifier.amount(),
                                parseOperation(modifier.operation())),
                        EquipmentSlotGroup.MAINHAND));
            }
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        for (ItemAttributeModifiers.Entry entry : entries) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static AttributeModifier.Operation parseOperation(String operation) {
        return switch (operation) {
            case "add_value" -> AttributeModifier.Operation.ADD_VALUE;
            case "add_multiplied_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "add_multiplied_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };
    }


}
