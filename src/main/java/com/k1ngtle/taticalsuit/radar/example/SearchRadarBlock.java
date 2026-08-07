package com.k1ngtle.taticalsuit.radar.example;

import com.k1ngtle.taticalsuit.radar.registry.RadarBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class SearchRadarBlock extends AbstractRadarBlock {
   public SearchRadarBlock(Properties properties) {
      super(properties, RadarBlocks.SEARCH_RADAR_BE);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new SearchRadarBlockEntity(pos, state);
   }
}