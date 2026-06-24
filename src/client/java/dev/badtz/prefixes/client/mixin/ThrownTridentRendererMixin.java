package dev.badtz.prefixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.badtz.prefixes.PrefixTrackedProjectile;
import dev.badtz.prefixes.client.PrefixRenderScale;
import dev.badtz.prefixes.client.PrefixScaledRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;

@Mixin(ThrownTridentRenderer.class)
public class ThrownTridentRendererMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void prefixes$extractScale(ThrownTrident entity, ThrownTridentRenderState state,
            float partialTicks, CallbackInfo ci) {
        Identifier prefixId = ((PrefixTrackedProjectile) entity).prefixes$getPrefixId();
        ((PrefixScaledRenderState) state).prefixes$setScale(PrefixRenderScale.getScale(prefixId));
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private void prefixes$pushScale(ThrownTridentRenderState state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        float scale = ((PrefixScaledRenderState) state).prefixes$getScale();

        poseStack.pushPose();

        if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void prefixes$popScale(ThrownTridentRenderState state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        poseStack.popPose();
    }
}
