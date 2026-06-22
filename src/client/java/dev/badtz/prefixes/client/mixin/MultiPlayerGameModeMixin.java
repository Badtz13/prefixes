package dev.badtz.prefixes.client.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.PrefixManager;
import dev.badtz.prefixes.client.PrefixClientSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
        @Shadow
        @Final
        private Minecraft minecraft;

        @Shadow
        private ItemStack destroyingItem;

        @Inject(method = "continueDestroyBlock", at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/sounds/SoundManager;play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"))
        private void injectBlockBreakSound(BlockPos pos, Direction direction,
                        CallbackInfoReturnable<Boolean> cir) {
                PrefixApplier.getPrefix(destroyingItem).ifPresent(prefix -> {
                        if (prefix.type() != PrefixManager.PrefixType.TOOL) {
                                return;
                        }

                        PrefixClientSounds.playMiningSound(minecraft, pos, destroyingItem);
                });
        }
}
