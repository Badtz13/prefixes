package dev.badtz.prefixes.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.PrefixManager;
import dev.badtz.prefixes.Prefixes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    private int repairItemCountCost;

    private AnvilMenuMixin(MenuType<?> menuType, int containerId, Inventory inventory,
            ContainerLevelAccess access, ItemCombinerMenuSlotDefinition definition) {
        super(menuType, containerId, inventory, access, definition);
    }

    @Shadow
    protected abstract boolean isValidBlock(BlockState state);

    @Shadow
    public abstract void createResult();

    @Shadow
    protected abstract void onTake(Player player, ItemStack stack);

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void prefixes$emeraldRerollPreview(CallbackInfo ci) {
        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);

        if (left.isEmpty() || right.isEmpty() || !right.is(Items.EMERALD)) {
            return;
        }

        if (!PrefixApplier.isPrefixable(left)) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            this.broadcastChanges();
            ci.cancel();
            return;
        }

        ItemStack result = left.copy();
        result.setCount(1);

        Identifier oldPrefixId = result.get(Prefixes.PREFIX);

        if (oldPrefixId != null) {
            PrefixManager.PrefixDefinition oldPrefix = PrefixManager.get(oldPrefixId);

            if (oldPrefix != null) {
                PrefixApplier.remove(result, oldPrefix);
            }

            result.remove(Prefixes.PREFIX);
        }

        Component previewName = getPlayerCustomName(left);

        result.set(DataComponents.CUSTOM_NAME,
                (previewName != null ? previewName.copy() : left.getItemName().copy())
                        .withStyle(style -> style.withItalic(false)));

        result.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("(Random Prefix!)")
                .withStyle(style -> style.withColor(ChatFormatting.RED).withItalic(false)))));

        this.repairItemCountCost = 1;
        this.resultSlots.setItem(0, result);
        this.cost.set(1);
        this.broadcastChanges();
        ci.cancel();
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void prefixes$emeraldRerollOnTake(Player player, ItemStack carried, CallbackInfo ci) {
        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);

        if (carried.isEmpty() || right.isEmpty() || !right.is(Items.EMERALD)) {
            return;
        }

        Component playerName = getPlayerCustomName(left);

        carried.remove(DataComponents.CUSTOM_NAME);
        carried.remove(DataComponents.LORE);

        PrefixApplier.applyRandom(carried, player.getRandom());

        if (playerName != null) {
            carried.set(DataComponents.CUSTOM_NAME, playerName);
        }

        this.repairItemCountCost = 1;
    }

    private Component getPlayerCustomName(ItemStack stack) {
        Component customName = stack.get(DataComponents.CUSTOM_NAME);

        if (customName == null) {
            return null;
        }

        Identifier prefixId = stack.get(Prefixes.PREFIX);

        if (prefixId == null) {
            return customName;
        }

        PrefixManager.PrefixDefinition prefix = PrefixManager.get(prefixId);

        if (prefix == null) {
            return customName;
        }

        String expectedPrefixName =
                Component.translatable(prefix.translationKey()).getString() + " ";

        if (customName.getString().startsWith(expectedPrefixName)) {
            return null;
        }

        return customName;
    }
}
