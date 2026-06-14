package com.noeliceaga.banditmonkey.client.renderer;

import com.noeliceaga.banditmonkey.client.model.BanditMonkeyModel;
import com.noeliceaga.banditmonkey.entity.BanditMonkeyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class BanditMonkeyRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<BanditMonkeyEntity, R> {

    public BanditMonkeyRenderer(EntityRendererProvider.Context context) {
        super(context, new BanditMonkeyModel());
    }
}
