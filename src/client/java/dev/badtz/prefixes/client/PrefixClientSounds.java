package dev.badtz.prefixes.client;

import dev.badtz.prefixes.PrefixApplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public final class PrefixClientSounds {
    private PrefixClientSounds() {}

    public static void playMiningSound(Minecraft minecraft, BlockPos pos, ItemStack stack) {
        PrefixApplier.getHitSound(stack)
                .ifPresent(sound -> BuiltInRegistries.SOUND_EVENT.get(sound.id())
                        .ifPresent(soundEvent -> minecraft.getSoundManager()
                                .play(new SimpleSoundInstance(soundEvent.value(),
                                        SoundSource.PLAYERS, sound.volume(), sound.pitch(),
                                        SoundInstance.createUnseededRandom(), pos))));
    }
}
