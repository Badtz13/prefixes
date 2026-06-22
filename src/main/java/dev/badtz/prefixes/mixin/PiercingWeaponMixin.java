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
import net.minecraft.world.item.component.PiercingWeapon;

@Mixin(PiercingWeapon.class)
public abstract class PiercingWeaponMixin {
    @Inject(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/component/PiercingWeapon;makeHitSound(Lnet/minecraft/world/entity/Entity;)V"))
    private void prefixes$playPrefixPiercingHitSound(LivingEntity attacker, EquipmentSlot hand,
            CallbackInfo ci) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack stack = attacker.getItemBySlot(hand);

        PrefixApplier.getPrefix(stack).ifPresent(prefix -> {
            if (prefix.type() == PrefixManager.PrefixType.WEAPON) {
                PrefixApplier.playHitSound(level, attacker, stack);
            }
        });
    }
}
