package com.noeliceaga.banditmonkey.registry;

import com.noeliceaga.banditmonkey.BanditMonkey;
import com.noeliceaga.banditmonkey.menu.StealOrderMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuRegistry {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, BanditMonkey.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<StealOrderMenu>> STEAL_ORDER =
            MENU_TYPES.register("steal_order", () -> IMenuTypeExtension.create(StealOrderMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
