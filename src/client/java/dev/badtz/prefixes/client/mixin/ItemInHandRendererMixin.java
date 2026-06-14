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

        float scale = getPrefixScale(prefix);

        if (scale != 1.0f) {
            poseStack.scale(scale, scale, scale);
        }
    }

    private float getPrefixScale(PrefixManager.PrefixDefinition prefix) {
        for (PrefixManager.PrefixModifier modifier : prefix.modifiers()) {
            if (!modifier.attribute().equals("minecraft:entity_interaction_range")
                    && !modifier.attribute().equals("minecraft:block_interaction_range")) {
                continue;
            }

            return switch (modifier.operation()) {
                case "add_multiplied_base", "add_multiplied_total" -> 1.0f
                        + (float) modifier.amount();
                case "add_value" -> 1.0f + (float) (modifier.amount() / 4.5);
                default -> 1.0f;
            };
        }

        return 1.0f;
    }
}
