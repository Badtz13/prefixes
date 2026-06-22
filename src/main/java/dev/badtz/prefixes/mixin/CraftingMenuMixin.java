package dev.badtz.prefixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import dev.badtz.prefixes.PrefixApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
    @Redirect(method = "quickMoveStack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/CraftingMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z",
            ordinal = 0))
    private boolean prefixes$applyRandomPrefixOnShiftCraft(CraftingMenu menu, ItemStack stack,
            int startIndex, int endIndex, boolean reverseDirection, Player player, int slotIndex) {
        if (slotIndex == 0 && !player.level().isClientSide()) {
            PrefixApplier.applyRandomIfNeeded(stack, player.getRandom());
        }

        return ((AbstractContainerMenuAccessor) menu).prefixes$moveItemStackTo(stack, startIndex,
                endIndex, reverseDirection);
    }
}
