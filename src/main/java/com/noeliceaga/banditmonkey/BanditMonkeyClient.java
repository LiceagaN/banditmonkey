package com.noeliceaga.banditmonkey;

import com.noeliceaga.banditmonkey.client.renderer.BanditMonkeyRenderer;
import com.noeliceaga.banditmonkey.client.screen.StealOrderScreen;
import com.noeliceaga.banditmonkey.entity.BanditMonkeyEntity;
import com.noeliceaga.banditmonkey.network.SetStealTargetPayload;
import com.noeliceaga.banditmonkey.registry.EntityRegistry;
import com.noeliceaga.banditmonkey.registry.MenuRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import software.bernie.geckolib.renderer.base.GeoRenderState;

@Mod(value = BanditMonkey.MODID, dist = Dist.CLIENT)
public class BanditMonkeyClient {

    public BanditMonkeyClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(BanditMonkeyClient::registerRenderers);
        modEventBus.addListener(BanditMonkeyClient::registerScreens);
        NeoForge.EVENT_BUS.addListener(BanditMonkeyClient::onEntityInteract);
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

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!StealOrderScreen.targetingMode) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity) || target instanceof BanditMonkeyEntity) return;

        StealOrderScreen.targetingMode = false;
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(
                    new ServerboundCustomPayloadPacket(new SetStealTargetPayload(target.getId())));
        }
        event.setCanceled(true);
    }
}
