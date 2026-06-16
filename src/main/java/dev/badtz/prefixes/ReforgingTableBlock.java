package dev.badtz.prefixes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ReforgingTableBlock extends Block {
    VoxelShape SHAPE = Shapes.or(Block.box(1, 0, 1, 15, 12, 15), Block.box(0, 10, 0, 16, 12, 16),
            Block.box(0, 12, 0, 16, 14, 16));

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
}
