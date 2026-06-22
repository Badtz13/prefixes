package dev.badtz.prefixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.PrefixManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;

@Mixin(KineticWeapon.class)
public abstract class KineticWeaponMixin {
    @Inject(method = "damageEntities", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
    private void prefixes$playPrefixKineticHitSound(ItemStack stack, int ticksRemaining,
            LivingEntity attacker, EquipmentSlot equipmentSlot, CallbackInfo ci) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }

        PrefixApplier.getPrefix(stack).ifPresent(prefix -> {
            if (prefix.type() == PrefixManager.PrefixType.WEAPON) {
                PrefixApplier.playHitSound(level, attacker, stack);
            }
        });
    }
}
