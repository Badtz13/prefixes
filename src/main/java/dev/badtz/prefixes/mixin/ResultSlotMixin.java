package dev.badtz.prefixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.Prefixes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {
    @Inject(method = "onTake", at = @At("HEAD"))
    private void prefixes$applyRandomPrefixOnCraft(Player player, ItemStack carried,
            CallbackInfo ci) {
        if (!player.level().isClientSide() && !carried.isEmpty() && !carried.has(Prefixes.PREFIX)) {
            PrefixApplier.applyRandom(carried, player.getRandom());
        }
    }
}
