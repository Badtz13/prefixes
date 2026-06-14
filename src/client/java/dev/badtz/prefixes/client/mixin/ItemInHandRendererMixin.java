package dev.badtz.prefixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.badtz.prefixes.PrefixManager;
import dev.badtz.prefixes.Prefixes;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderItem", at = @At("HEAD"))
    private void scalePrefixItem(LivingEntity mob, ItemStack itemStack, ItemDisplayContext type,
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            CallbackInfo ci) {
        Identifier prefixId = itemStack.get(Prefixes.PREFIX);

        if (prefixId == null) {
            return;
        }

        PrefixManager.PrefixDefinition prefix = PrefixManager.get(prefixId);

        if (prefix == null) {
            return;
        }

        float scale = prefix.scale();

        if (scale != 1.0f) {
            poseStack.scale(scale, scale, scale);
        }
    }
}
