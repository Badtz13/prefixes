package dev.badtz.prefixes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
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
            }
        }

        removePrefixAttributeModifiers(stack);
        stack.set(Prefixes.PREFIX, prefix.id());

        if (!preserveCustomName) {
            applyName(stack, prefix);
        }

        applyTierLore(stack, prefix);
        addPrefixAttributeModifiers(stack, prefix);
        Prefixes.awardFiveStarAdvancement(player, prefix);
    }

    public static void remove(ItemStack stack, PrefixManager.PrefixDefinition prefix) {
        removePrefixAttributeModifiers(stack);
        stack.remove(Prefixes.PREFIX);
        stack.remove(DataComponents.LORE);
    }

    public static boolean applyRandom(ItemStack stack, net.minecraft.util.RandomSource random) {
        List<PrefixManager.PrefixType> types = new ArrayList<>();

        if (PrefixTags.isWeapon(stack)) {
            types.add(PrefixManager.PrefixType.WEAPON);
        }

        if (PrefixTags.isTool(stack)) {
            types.add(PrefixManager.PrefixType.TOOL);
        }

        if (types.isEmpty()) {
            return false;
        }

        PrefixManager.PrefixType type = types.get(random.nextInt(types.size()));
        PrefixManager.PrefixDefinition prefix = PrefixManager.getRandom(type, random);

        if (prefix == null) {
            return false;
        }

        apply(stack, prefix);
        return true;
    }

    public static boolean applyRandomIfNeeded(ItemStack stack,
            net.minecraft.util.RandomSource random) {
        if (stack.isEmpty() || stack.has(Prefixes.PREFIX) || !isPrefixable(stack)) {
            return false;
        }

        return applyRandom(stack, random);
    }

    public static Optional<PrefixManager.PrefixDefinition> getPrefix(ItemStack stack) {
        Identifier prefixId = stack.get(Prefixes.PREFIX);

        if (prefixId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(PrefixManager.get(prefixId));
    }

    public static Optional<PrefixManager.PrefixSound> getHitSound(ItemStack stack) {
        return getPrefix(stack).map(PrefixManager.PrefixDefinition::sound)
                .filter(sound -> sound != null);
    }

    public static float getRandomPitch(RandomSource random) {
        return 0.85F + random.nextFloat() * 0.30F;
    }

    public static void playHitSound(ServerLevel level, Entity target, ItemStack stack) {
        getHitSound(stack).ifPresent(
                sound -> BuiltInRegistries.SOUND_EVENT.get(sound.id()).ifPresent(soundEvent -> {
                    float pitch = sound.pitch() * getRandomPitch(level.getRandom());

                    level.playSound(null, target.getX(), target.getY(), target.getZ(),
                            soundEvent.value(), SoundSource.PLAYERS, sound.volume(), pitch);
                }));
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

    private static void removePrefixAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        for (ItemAttributeModifiers.Entry entry : existing.modifiers()) {
            if (!entry.modifier().id().toString().startsWith("prefixes:prefix/")) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void addPrefixAttributeModifiers(ItemStack stack,
            PrefixManager.PrefixDefinition prefix) {
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

            Identifier modifierId = Prefixes.id("prefix/" + prefix.id().getNamespace() + "/"
                    + prefix.id().getPath() + "/" + attributeId.getPath());

            entries.add(
                    new ItemAttributeModifiers.Entry(attribute.get(),
                            new AttributeModifier(modifierId, modifier.amount(),
                                    parseOperation(modifier.operation())),
                            EquipmentSlotGroup.MAINHAND));
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
