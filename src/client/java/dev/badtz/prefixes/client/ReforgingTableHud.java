package dev.badtz.prefixes.client;

import dev.badtz.prefixes.ModBlocks;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.PrefixManager;
import dev.badtz.prefixes.Prefixes;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class ReforgingTableHud {
    private ReforgingTableHud() {}

    public static void initialize() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(Prefixes.MOD_ID, "reforging_table_hint"),
                ReforgingTableHud::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.hitResult == null) {
            return;
        }

        if (minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult hit = (BlockHitResult) minecraft.hitResult;
        BlockState state = minecraft.level.getBlockState(hit.getBlockPos());

        if (!state.is(ModBlocks.REFORGING_TABLE)) {
            return;
        }

        ItemStack stack = minecraft.player.getMainHandItem();

        int x = minecraft.getWindow().getGuiScaledWidth() / 2;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + 18;

        if (!PrefixApplier.isPrefixable(stack)) {
            graphics.centeredText(minecraft.font, "Hold a weapon or tool", x, y, 0xFFFFFFFF);
            return;
        }

        graphics.centeredText(minecraft.font, "Reforge Cost: 1 Level", x, y, 0xFFFFFFFF);

        Identifier prefixId = stack.get(Prefixes.PREFIX);
        PrefixManager.PrefixDefinition prefix =
                prefixId == null ? null : PrefixManager.get(prefixId);

        if (prefix == null) {
            graphics.centeredText(minecraft.font, "Current Prefix: None", x, y + 12, 0xFFE0E0E0);
            return;
        }

        String prefixName = Component.translatable(prefix.translationKey()).getString();

        graphics.centeredText(minecraft.font, "Current Prefix:", x, y + 12, 0xFFE0E0E0);

        graphics.centeredText(minecraft.font, prefixName, x, y + 22,
                colorForRarity(Prefixes.rarityForTier(prefix.tier())));

        graphics.centeredText(minecraft.font, Prefixes.starsForTier(prefix.tier()), x, y + 32,
                colorForRarity(Prefixes.rarityForTier(prefix.tier())));
    }

    private static int colorForRarity(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFFFFFFFF;
            case UNCOMMON -> 0xFFFFFF55;
            case RARE -> 0xFF55FFFF;
            case EPIC -> 0xFFFF55FF;
        };
    }
}
