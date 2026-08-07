package com.k1ngtle.taticalsuit.radar.integration.dh;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.radar.api.occlusion.IOcclusionProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class DhHeightOcclusionProvider implements IOcclusionProvider {
   private static volatile boolean warnedOnce = false;

   @Override
   public boolean isOccluded(ServerLevel level, Vec3 from, Vec3 to) {
      if (!warnedOnce) {
         warnedOnce = true;
         TaticalSuit.LOGGER.warn("DH occlusion provider is a stub (DH 3.x API not yet ported) — beams will not be blocked by LOD terrain.");
      }

      return false;
   }

   @Override
   public boolean threadSafe() {
      return true;
   }
}