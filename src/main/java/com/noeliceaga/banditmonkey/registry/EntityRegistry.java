package com.noeliceaga.banditmonkey.registry;

import com.noeliceaga.banditmonkey.BanditMonkey;
import com.noeliceaga.banditmonkey.entity.BanditMonkeyEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, BanditMonkey.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BanditMonkeyEntity>> MONKEY =
            ENTITY_TYPES.register("banditmonkey",
                    () -> EntityType.Builder.<BanditMonkeyEntity>of(
                                    BanditMonkeyEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.6f, 0.9f)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(BanditMonkey.MODID, "banditmonkey")))
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
