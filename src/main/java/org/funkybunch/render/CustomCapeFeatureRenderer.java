package org.funkybunch.capes.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.funkybunch.capes.CapeManager;
import org.funkybunch.capes.Capes;

public class CustomCapeFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public CustomCapeFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, 
                      AbstractClientPlayerEntity player, float limbAngle, float limbDistance, 
                      float tickDelta, float animationProgress, float headYaw, float headPitch) {
        
        // Skip if player has elytra equipped
        ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
        if (itemStack.getItem() == Items.ELYTRA) {
            return;
        }
        
        // Skip if player has cape disabled in settings
        if (!player.canRenderCapeTexture() || !player.isPartVisible(PlayerModelPart.CAPE)) {
            return;
        }
        
        // Get the player's username
        String username = player.getName().getString();
        
        // Check if this player should have a custom cape
        Identifier capeTexture = CapeManager.getInstance().getCapeTexture(username);
        if (capeTexture == null) {
            return;
        }
        
        // Start rendering the cape
        matrixStack.push();
        
        // Adjust the cape position
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(6.0F + MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch()) / 2.0F));
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F - MathHelper.lerp(tickDelta, player.prevYaw, player.getYaw()) / 2.0F));
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw) / 2.0F));
        
        // Apply physics
        float g = MathHelper.lerp(tickDelta, player.prevCapeX, player.capeX) - MathHelper.lerp(tickDelta, player.prevX, player.getX());
        float h = MathHelper.lerp(tickDelta, player.prevCapeY, player.capeY) - MathHelper.lerp(tickDelta, player.prevY, player.getY());
        float j = MathHelper.lerp(tickDelta, player.prevCapeZ, player.capeZ) - MathHelper.lerp(tickDelta, player.prevZ, player.getZ());
        float k = player.prevBodyYaw + (player.bodyYaw - player.prevBodyYaw);
        
        double d = MathHelper.sin(k * 0.017453292F);
        double e = -MathHelper.cos(k * 0.017453292F);
        float l = h * 10.0F;
        l = MathHelper.clamp(l, -6.0F, 32.0F);
        float m = (g * d + j * e) * 100.0F;
        m = MathHelper.clamp(m, 0.0F, 150.0F);
        
        float n = (g * e - j * d) * 100.0F;
        n = MathHelper.clamp(n, -20.