package dev.badtz.prefixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.PrefixManager;
import dev.badtz.prefixes.PrefixTrackedProjectile;
import dev.badtz.prefixes.Prefixes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow implements PrefixTrackedProjectile {
    @Unique
    private static final EntityDataAccessor<String> PREFIXES_PREFIX_ID =
            SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.STRING);

    protected ThrownTridentMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    private void prefixes$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PREFIXES_PREFIX_ID, "");
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("RETURN"))
    private void prefixes$initOwner(Level level, LivingEntity owner, ItemStack tridentItem,
            CallbackInfo ci) {
        this.prefixes$setPrefixId(tridentItem.get(Prefixes.PREFIX));
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V",
            at = @At("RETURN"))
    private void prefixes$initPosition(Level level, double x, double y, double z,
            ItemStack tridentItem, CallbackInfo ci) {
        this.prefixes$setPrefixId(tridentItem.get(Prefixes.PREFIX));
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void prefixes$playPrefixHitSound(EntityHitResult hitResult, CallbackInfo ci) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        Entity target = hitResult.getEntity();
        ItemStack stack = this.getWeaponItem();

        PrefixApplier.getPrefix(stack).ifPresent(prefix -> {
            if (prefix.type() != PrefixManager.PrefixType.WEAPON) {
                return;
            }

            PrefixApplier.playHitSound(level, target, stack);
        });
    }

    @Override
    public void prefixes$setPrefixId(Identifier prefixId) {
        this.entityData.set(PREFIXES_PREFIX_ID, prefixId == null ? "" : prefixId.toString());
    }

    @Override
    public Identifier prefixes$getPrefixId() {
        String value = this.entityData.get(PREFIXES_PREFIX_ID);
        return value.isEmpty() ? null : Identifier.tryParse(value);
    }
}
