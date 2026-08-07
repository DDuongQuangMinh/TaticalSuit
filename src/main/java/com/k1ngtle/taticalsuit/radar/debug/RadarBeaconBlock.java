package com.k1ngtle.taticalsuit.radar.debug;

import com.k1ngtle.taticalsuit.radar.registry.RadarBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class RadarBeaconBlock extends Block implements EntityBlock {
   public RadarBeaconBlock(Properties properties) {
      super(properties);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new RadarBeaconBlockEntity(pos, state);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof RadarBeaconBlockEntity be) {
         double next = be.cycleRcs();
         player.displayClientMessage(Component.literal("Beacon RCS: " + next + " m²"), true);
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   static BlockEntityType<?> beType() {
      return (BlockEntityType<?>)RadarBlocks.RADAR_BEACON_BE.get();
   }
}