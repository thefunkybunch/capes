package org.funkybunch.capes.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import org.funkybunch.capes.CapeManager;
import org.funkybunch.capes.render.CustomCapeFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructor(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, boolean slim, CallbackInfo ci) {
        // Get the instance of the renderer
        PlayerEntityRenderer renderer = (PlayerEntityRenderer) (Object) this;
        
        // Add our custom cape renderer
        renderer.addFeature(new CustomCapeFeatureRenderer(renderer));
    }
    
    // This injection helps to prevent the vanilla cape from rendering
    @SuppressWarnings("unchecked")
    @Inject(method = "addFeature", at = @At("HEAD"), cancellable = true)
    private void onAddFeature(FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> feature, CallbackInfo ci) {
        // Check if this is the vanilla cape renderer being added
        if (feature instanceof CapeFeatureRenderer) {
            // We'll cancel adding the vanilla cape renderer since we have our own
            ci.cancel();
        }
    }
}