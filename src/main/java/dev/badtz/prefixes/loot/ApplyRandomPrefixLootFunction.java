package dev.badtz.prefixes.loot;

import java.util.List;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.badtz.prefixes.PrefixApplier;
import dev.badtz.prefixes.Prefixes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyRandomPrefixLootFunction extends LootItemConditionalFunction {
    public static final MapCodec<ApplyRandomPrefixLootFunction> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> commonFields(instance).apply(instance,
                    ApplyRandomPrefixLootFunction::new));

    private ApplyRandomPrefixLootFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        Prefixes.LOGGER.info("Loot function saw: {}", stack);

        if (!stack.isEmpty() && !stack.has(Prefixes.PREFIX)) {
            boolean applied = PrefixApplier.applyRandom(stack, context.getRandom());
            Prefixes.LOGGER.info("Applied random prefix: {}", applied);
        }

        return stack;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return MAP_CODEC;
    }

    public static Builder<?> builder() {
        return simpleBuilder(ApplyRandomPrefixLootFunction::new);
    }
}
