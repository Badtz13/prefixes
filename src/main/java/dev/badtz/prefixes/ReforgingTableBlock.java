package dev.badtz.prefixes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ReforgingTableBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1, 0, 1, 15, 12, 15),
            Block.box(0, 10, 0, 16, 12, 16), Block.box(0, 12, 0, 16, 14, 16));

    public ReforgingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos,
            CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos,
            CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!PrefixApplier.isPrefixable(stack)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!player.isCreative() && player.experienceLevel < 1) {
            player.sendSystemMessage(Component.literal("You need at least 1 level to reforge."));
            return InteractionResult.FAIL;
        }

        PrefixManager.PrefixDefinition prefix =
                PrefixManager.getRandomApplicablePrefix(stack, level.getRandom());

        if (prefix == null) {
            return InteractionResult.FAIL;
        }

        PrefixApplier.apply(stack, prefix, (ServerPlayer) player);
        Prefixes.playReforgeSound(level, pos, prefix);
        player.awardStat(PrefixStats.ITEMS_REFORGED);

        if (!player.isCreative()) {
            player.giveExperienceLevels(-1);
        }

        return InteractionResult.SUCCESS;
    }


}
