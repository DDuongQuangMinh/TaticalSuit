package com.k1ngtle.taticalsuit.radar.api.events;

import com.k1ngtle.taticalsuit.radar.api.radar.IRadarEmitter;
import com.k1ngtle.taticalsuit.radar.api.radar.RadarContact;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public final class ContactDetectedEvent extends Event {
   private final IRadarEmitter emitter;
   private final RadarContact contact;

   public ContactDetectedEvent(IRadarEmitter emitter, RadarContact contact) {
      this.emitter = emitter;
      this.contact = contact;
   }

   public IRadarEmitter emitter() {
      return this.emitter;
   }

   public RadarContact contact() {
      return this.contact;
   }
}