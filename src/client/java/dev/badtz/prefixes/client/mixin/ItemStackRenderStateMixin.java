package dev.badtz.prefixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.badtz.prefixes.client.PrefixScaledRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;

@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin implements PrefixScaledRenderState {
    @Shadow
    ItemDisplayContext displayContext;

    @Unique
    private float prefixes$scale = 1.0F;

    @Override
    public void prefixes$setScale(float scale) {
        this.prefixes$scale = scale;
    }

    @Override
    public float prefixes$getScale() {
        return this.prefixes$scale;
    }

    @Inject(method = "clear", at = @At("RETURN"))
    private void prefixes$clearScale(CallbackInfo ci) {
        this.prefixes$scale = 1.0F;
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private void prefixes$pushScale(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        poseStack.pushPose();

        if (this.prefixes$scale != 1.0F) {
            poseStack.scale(this.prefixes$scale, this.prefixes$scale, this.prefixes$scale);
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void prefixes$popScale(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        poseStack.popPose();
    }
}
