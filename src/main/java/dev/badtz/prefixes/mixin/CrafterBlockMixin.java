package dev.badtz.prefixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.Prefixes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CrafterBlock.class)
public class CrafterBlockMixin {
    @ModifyVariable(method = "dispenseFrom", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private ItemStack prefixes$applyRandomPrefix(ItemStack results, BlockState state,
            ServerLevel level, BlockPos pos) {
        if (!results.isEmpty() && !results.has(Prefixes.PREFIX)) {
            PrefixApplier.applyRandom(results, level.getRandom());
        }

        return results;
    }
}
