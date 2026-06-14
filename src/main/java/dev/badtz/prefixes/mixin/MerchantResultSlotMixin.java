package dev.badtz.prefixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.Prefixes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {
    @Inject(method = "onTake", at = @At("HEAD"))
    private void prefixes$applyRandomPrefixOnTrade(Player player, ItemStack stack,
            CallbackInfo ci) {
        if (!player.level().isClientSide() && !stack.isEmpty() && !stack.has(Prefixes.PREFIX)) {
            PrefixApplier.applyRandom(stack, player.getRandom());
        }
    }
}
