package dev.badtz.prefixes.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.PrefixApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {
    @Shadow
    @Final
    private Player player;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void prefixes$applyRandomPrefixOnCraft(Player player, ItemStack stack,
            CallbackInfo ci) {
        if (!this.player.level().isClientSide()) {
            PrefixApplier.applyRandomIfNeeded(stack, this.player.getRandom());
        }
    }
}
