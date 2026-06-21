package dev.badtz.prefixes.client.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.sugar.Local;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.PrefixManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;

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
            CallbackInfoReturnable<Boolean> cir, @Local(name = "soundType") SoundType soundType) {
        PrefixApplier.getPrefix(destroyingItem).ifPresent(prefix -> {
            if (prefix.type() != PrefixManager.PrefixType.TOOL) {
                return;
            }

            PrefixApplier.getHitSound(destroyingItem)
                    .ifPresent(sound -> BuiltInRegistries.SOUND_EVENT.get(sound.id())
                            .ifPresent(soundEvent -> minecraft.getSoundManager()
                                    .play(new SimpleSoundInstance(soundEvent.value(),
                                            SoundSource.PLAYERS,
                                            ((soundType.getVolume() + 1.0F) / 6.0F)
                                                    * sound.volume(),
                                            soundType.getPitch() * sound.pitch(),
                                            SoundInstance.createUnseededRandom(), pos))));
        });
    }
}
