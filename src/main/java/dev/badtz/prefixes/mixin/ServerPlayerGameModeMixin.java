package dev.badtz.prefixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.PrefixApplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Shadow
    protected ServerPlayer player;

    @Unique
    private int prefixes$lastMiningSoundTick = 0;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void prefixes$playMiningPrefixSound(BlockPos pos,
            ServerboundPlayerActionPacket.Action action, Direction direction, int maxY,
            int sequence, CallbackInfo ci) {
        if (action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
                && action != ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        int tick = player.tickCount;

        if (tick - prefixes$lastMiningSoundTick < 2) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        PrefixApplier.getHitSound(stack).ifPresent(
                sound -> BuiltInRegistries.SOUND_EVENT.get(sound.id()).ifPresent(soundEvent -> {
                    prefixes$lastMiningSoundTick = tick;
                    level.playSound(null, pos, soundEvent.value(), SoundSource.PLAYERS,
                            sound.volume(), sound.pitch());
                }));
    }
}
