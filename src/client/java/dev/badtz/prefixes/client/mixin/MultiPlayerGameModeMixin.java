//all layton
package dev.badtz.prefixes.client.mixin;

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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private ItemStack destroyingItem;

    @Inject(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"))
    private void injectBlockBreakSound(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir, @Local(name = "soundType") SoundType soundType) {
        /*this.minecraft.getSoundManager().play(
                        new SimpleSoundInstance(
                                SoundEvents.STONE_BUTTON_CLICK_ON,
                                SoundSource.BLOCKS,
                                (soundType.getVolume() + 1.0F) / 8.0F,
                                soundType.getPitch() * 0.5F,
                                SoundInstance.createUnseededRandom(),
                                pos
                        )
                );
                PrefixApplier.getPrefix(destroyingItem).ifPresent(prefix -> {
            if (prefix.type() != PrefixManager.PrefixType.TOOL) {
                return;
            }
        );*/
        //layton help
        PrefixApplier.getHitSound(destroyingItem).ifPresent(
            sound -> BuiltInRegistries.SOUND_EVENT.get(sound.id()).ifPresent(soundEvent -> {
                this.minecraft.getSoundManager().play(
                        new SimpleSoundInstance(
                                soundEvent.value(),
                                SoundSource.BLOCKS,
                                (soundType.getVolume() + 1.0F) / 6.0F,
                                soundType.getPitch(),
                                SoundInstance.createUnseededRandom(),
                                pos
                        )
                );
            }));
    }
}