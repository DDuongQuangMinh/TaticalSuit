package com.k1ngtle.taticalsuit.registry;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.menu.TacticalEquipmentMenu;
import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TaticalSuit.MODID);

    // Workbench Menu
    public static final RegistryObject<MenuType<WorkbenchMenu>> WORKBENCH_MENU =
            registerMenuType("workbench_menu", WorkbenchMenu::new);

    // Tactical Equipment Menu
    public static final RegistryObject<MenuType<TacticalEquipmentMenu>> TACTICAL_EQUIPMENT_MENU = 
            MENUS.register("tactical_equipment_menu", () -> IForgeMenuType.create((windowId, inv, data) -> new TacticalEquipmentMenu(windowId, inv)));

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }
}