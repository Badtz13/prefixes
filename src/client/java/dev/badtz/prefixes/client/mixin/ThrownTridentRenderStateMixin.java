package dev.badtz.prefixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import dev.badtz.prefixes.client.PrefixScaledRenderState;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;

@Mixin(ThrownTridentRenderState.class)
public class ThrownTridentRenderStateMixin implements PrefixScaledRenderState {
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
}
