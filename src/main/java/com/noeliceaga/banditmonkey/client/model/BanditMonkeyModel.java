package com.noeliceaga.banditmonkey.client.model;

import com.noeliceaga.banditmonkey.BanditMonkey;
import com.noeliceaga.banditmonkey.entity.BanditMonkeyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class BanditMonkeyModel extends GeoModel<BanditMonkeyEntity> {

    // GeckoLib 5 scans assets/{ns}/geckolib/models/ and stores keys as {ns}:{basename}
    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BanditMonkey.MODID, "banditmonkey");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BanditMonkey.MODID, "textures/entity/mono.png");
    }

    // GeckoLib 5 scans assets/{ns}/geckolib/animations/ and stores keys as {ns}:{basename}
    @Override
    public ResourceLocation getAnimationResource(BanditMonkeyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(BanditMonkey.MODID, "gibon_idle");
    }
}
