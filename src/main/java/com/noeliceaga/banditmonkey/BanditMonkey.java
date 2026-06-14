package com.noeliceaga.banditmonkey;

import com.mojang.logging.LogUtils;
import com.noeliceaga.banditmonkey.entity.BanditMonkeyEntity;
import com.noeliceaga.banditmonkey.network.SetStealTargetPayload;
import com.noeliceaga.banditmonkey.registry.EntityRegistry;
import com.noeliceaga.banditmonkey.registry.MenuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;

@Mod(BanditMonkey.MODID)
public class BanditMonkey {

    public static final String MODID = "banditmonkey";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BanditMonkey(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(BanditMonkey::registerPayloads);

        EntityRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Bandit Monkey loaded!");
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.MONKEY.get(), BanditMonkeyEntity.createAttributes().build());
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SetStealTargetPayload.TYPE,
                SetStealTargetPayload.CODEC,
                BanditMonkey::handleSetStealTarget);
    }

    private static void handleSetStealTarget(SetStealTargetPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            ServerLevel level = serverPlayer.level();

            Player targetPlayer = level.getServer() != null
                    ? level.getServer().getPlayerList().getPlayer(payload.targetUUID())
                    : null;
            if (targetPlayer == null) return;

            List<BanditMonkeyEntity> monkeys = level.getEntitiesOfClass(
                    BanditMonkeyEntity.class,
                    serverPlayer.getBoundingBox().inflate(32),
                    m -> m.isTame() && !m.isOrderedToSit() && m.isOwnedBy(serverPlayer));

            if (monkeys.isEmpty()) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.banditmonkey.no_monkey_nearby"), false);
                return;
            }

            monkeys.sort(Comparator.comparingDouble(m -> m.distanceTo(serverPlayer)));
            BanditMonkeyEntity monkey = monkeys.get(0);
            monkey.setStealTargetUUID(targetPlayer.getUUID());
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.banditmonkey.target_set", targetPlayer.getName()), false);
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Bandit Monkey server starting!");
    }
}
