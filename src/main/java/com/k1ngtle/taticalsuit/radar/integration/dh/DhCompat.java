package com.k1ngtle.taticalsuit.radar.integration.dh;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.radar.api.occlusion.IOcclusionProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

public final class DhCompat {
   public static final String DH_MOD_ID = "distanthorizons";
   private static final IOcclusionProvider NOOP = new IOcclusionProvider() {
      @Override
      public boolean isOccluded(ServerLevel l, Vec3 a, Vec3 b) {
         return false;
      }

      @Override
      public boolean threadSafe() {
         return true;
      }
   };
   private static final boolean LOADED;
   private static final IOcclusionProvider PROVIDER;

   private DhCompat() {
   }

   public static boolean isLoaded() {
      return LOADED;
   }

   public static IOcclusionProvider provider() {
      return PROVIDER;
   }

   private static IOcclusionProvider loadProvider() {
      try {
         Class<?> implCls = Class.forName("com.k1ngtle.taticalsuit.radar.integration.dh.DhHeightOcclusionProvider");
         return (IOcclusionProvider)implCls.getDeclaredConstructor().newInstance();
      } catch (Throwable var1) {
         TaticalSuit.LOGGER.error("DH detected but DhHeightOcclusionProvider failed to load — disabling DH occlusion.", var1);
         return NOOP;
      }
   }

   static {
      boolean loaded = false;

      try {
         loaded = ModList.get() != null && ModList.get().isLoaded("distanthorizons");
      } catch (Throwable var2) {
      }

      LOADED = loaded;
      PROVIDER = LOADED ? loadProvider() : NOOP;
      if (LOADED) {
         TaticalSuit.LOGGER.info("Distant Horizons detected — LOD-aware beam occlusion active.");
      }
   }
}