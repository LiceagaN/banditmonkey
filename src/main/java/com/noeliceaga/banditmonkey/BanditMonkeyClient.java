package com.noeliceaga.banditmonkey;

import com.noeliceaga.banditmonkey.client.renderer.BanditMonkeyRenderer;
import com.noeliceaga.banditmonkey.client.screen.StealOrderScreen;
import com.noeliceaga.banditmonkey.entity.BanditMonkeyEntity;
import com.noeliceaga.banditmonkey.registry.EntityRegistry;
import com.noeliceaga.banditmonkey.registry.MenuRegistry;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = BanditMonkey.MODID, dist = Dist.CLIENT)
public class BanditMonkeyClient {

    public BanditMonkeyClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(BanditMonkeyClient::registerRenderers);
        modEventBus.addListener(BanditMonkeyClient::registerScreens);
    }

    @SuppressWarnings("unchecked")
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.MONKEY.get(),
                context -> (EntityRenderer<BanditMonkeyEntity, LivingEntityRenderState>)
                        (Object) new BanditMonkeyRenderer<>(context));
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.STEAL_ORDER.get(), StealOrderScreen::new);
    }
}
