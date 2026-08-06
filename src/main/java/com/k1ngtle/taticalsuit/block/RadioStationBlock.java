package com.k1ngtle.taticalsuit.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RadioStationBlock extends HorizontalDirectionalBlock {
    
    // Tracks whether the radio has been upgraded with the external attachment
    public static final BooleanProperty FULL = BooleanProperty.create("full");
    
    // Accurate collision bounding boxes based on the Blockbench models
    private static final VoxelShape SHAPE_BASE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D);
    private static final VoxelShape SHAPE_FULL = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public RadioStationBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FULL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FULL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Face the block towards the player placing it
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FULL) ? SHAPE_FULL : SHAPE_BASE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Upgrade sequence: Right clicking base model with another block upgrades it to full
        if (!state.getValue(FULL) && stack.getItem() == this.asItem()) {
            if (!level.isClientSide) {
                // Update the block state to full
                level.setBlock(pos, state.setValue(FULL, true), 3);
                // Play a heavy metal building sound
                level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                
                // Consume the item if the player is in survival mode
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        // Usage sequence: Right clicking a full model opens the Tactical Radio Screen
        if (state.getValue(FULL)) {
            if (level.isClientSide) {
                // Create a temporary item stack so the GUI knows what name to display
                ItemStack dummyRadio = new ItemStack(this.asItem());
                dummyRadio.setHoverName(net.minecraft.network.chat.Component.literal("PRC-150 BASE STATION"));
                
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                    com.k1ngtle.taticalsuit.client.screen.RadioScreen.open(hand, dummyRadio);
                });
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        return InteractionResult.PASS;
    }
}