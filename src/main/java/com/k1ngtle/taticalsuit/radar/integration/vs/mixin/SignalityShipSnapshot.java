package com.k1ngtle.taticalsuit.radar.integration.vs.mixin;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4dc;
import org.joml.Vector3dc;

public interface SignalityShipSnapshot {
   @Nullable
   SignalityShipSnapshot.Snapshot signality$readSnapshot();

   public static record Snapshot(Matrix4dc shipToWorld, Vector3dc linearVelocity, long captureGameTime) {
   }
}