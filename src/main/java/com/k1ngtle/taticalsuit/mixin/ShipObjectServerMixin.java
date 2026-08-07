package com.k1ngtle.taticalsuit.mixin;

import com.k1ngtle.taticalsuit.radar.integration.vs.mixin.SignalityShipSnapshot;
import org.joml.Matrix4dc;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServer;

@Mixin(ShipObjectServer.class)
public abstract class ShipObjectServerMixin implements SignalityShipSnapshot {

    @Shadow(remap = false)
    public abstract ShipTransform getShipTransform();

    @Shadow(remap = false)
    public abstract Vector3dc getVelocity();

    @Override
    public SignalityShipSnapshot.Snapshot signality$readSnapshot() {
        ShipTransform transform = this.getShipTransform();
        if (transform == null) {
            return null;
        }

        Matrix4dc shipToWorld = transform.getShipToWorld();
        Vector3dc velocity = this.getVelocity();

        return new SignalityShipSnapshot.Snapshot(shipToWorld, velocity, 0L); // Note: GameTime tracking might require additional hooks depending on VS2's tick cycle.
    }
}