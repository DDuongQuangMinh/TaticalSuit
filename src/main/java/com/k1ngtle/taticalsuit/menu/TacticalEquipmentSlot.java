package com.k1ngtle.taticalsuit.menu;

import com.k1ngtle.taticalsuit.capability.EquipmentSlotType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class TacticalEquipmentSlot extends SlotItemHandler {
    
    private final EquipmentSlotType slotType;

    public TacticalEquipmentSlot(IItemHandler itemHandler, EquipmentSlotType slotType, int xPosition, int yPosition) {
        super(itemHandler, slotType.getIndex(), xPosition, yPosition);
        this.slotType = slotType;
    }

    public EquipmentSlotType getSlotType() {
        return slotType;
    }
}