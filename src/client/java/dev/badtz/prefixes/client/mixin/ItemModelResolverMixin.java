package dev.badtz.prefixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.badtz.prefixes.Prefixes;
import dev.badtz.prefixes.client.PrefixRenderScale;
import dev.badtz.prefixes.client.PrefixScaledRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @Inject(method = "updateForTopItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;appendItemLayers(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V"))
    private void prefixes$setPrefixScale(ItemStackRenderState output, ItemStack item,
            ItemDisplayContext displayContext, Level level, ItemOwner owner, int seed,
            CallbackInfo ci) {
        Identifier prefixId = item.get(Prefixes.PREFIX);
        float scale = PrefixRenderScale.getScale(item);

        ((PrefixScaledRenderState) output).prefixes$setScale(scale);

        if (prefixId != null) {
            output.appendModelIdentityElement(prefixId);
            output.appendModelIdentityElement(scale);
        }
    }
}
