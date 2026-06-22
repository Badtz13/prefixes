package dev.badtz.prefixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.PrefixManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void prefixes$playPrefixHitSound(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        PrefixApplier.getPrefix(stack).ifPresent(prefix -> {
            if (prefix.type() != PrefixManager.PrefixType.WEAPON) {
                return;
            }

            PrefixApplier.playHitSound(level, target, stack);
        });
    }
}
