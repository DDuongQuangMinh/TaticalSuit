package com.k1ngtle.taticalsuit.radar.api.signal;

import java.util.UUID;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record SignalPacket(
   UUID transmitterId,
   Vec3 originWorld,
   double frequencyHz,
   double transmitPowerWatts,
   double antennaGain,
   byte[] payload,
   long timestampNanos,
   int ttlHops,
   @Nullable String polarization
) {
   public SignalPacket {
      if (transmitterId == null) {
         throw new IllegalArgumentException("transmitterId");
      } else if (originWorld == null) {
         throw new IllegalArgumentException("originWorld");
      } else if (frequencyHz <= 0.0) {
         throw new IllegalArgumentException("frequencyHz");
      } else if (transmitPowerWatts <= 0.0) {
         throw new IllegalArgumentException("transmitPower");
      } else if (antennaGain <= 0.0) {
         throw new IllegalArgumentException("antennaGain");
      }
      
      if (payload == null) {
         payload = new byte[0];
      }
   }
}