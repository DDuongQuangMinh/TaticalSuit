package com.k1ngtle.taticalsuit.capability;

public enum EquipmentSlotType {
    SHIRT(0, "Shirt"),
    PANTS(1, "Pants"),
    GLOVES(2, "Gloves"),
    BOOTS(3, "Boots"),
    BELT(4, "Belt"),
    TATTOO(5, "Tattoo"),
    EYEWEAR(6, "Eyewear"),
    WATCH(7, "Watch");

    private final int index;
    private final String displayName;

    EquipmentSlotType(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EquipmentSlotType fromIndex(int index) {
        for (EquipmentSlotType slot : values()) {
            if (slot.getIndex() == index) return slot;
        }
        return SHIRT;
    }
}