package dev.badtz.prefixes.client.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.PrefixManager;
import dev.badtz.prefixes.client.PrefixClientSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ClientLevel.class)
public class MultiPlayerGameModeMixin {
        @Shadow
        @Final
        private Minecraft minecraft;

        @Inject(method = "playBreakingSound", at = @At("HEAD"))
        private void injectBlockBreakSound(BlockPos pos, BlockState state, CallbackInfo ci) {
                var destroyingItem = minecraft.player.getMainHandItem();
                PrefixApplier.getPrefix(destroyingItem).ifPresent(prefix -> {
                        if (prefix.type() != PrefixManager.PrefixType.TOOL) {
                                return;
                        }

                        PrefixClientSounds.playMiningSound(minecraft, pos, destroyingItem);
                });
        }
}


