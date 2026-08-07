package com.k1ngtle.taticalsuit.radar.api.rcs;

import com.k1ngtle.taticalsuit.radar.api.radar.IRadarEmitter;
import com.k1ngtle.taticalsuit.radar.api.radar.IRadarTarget;

@FunctionalInterface
public interface IRcsProvider {
   double computeRcs(IRadarTarget var1, IRadarEmitter var2, double var3);
}