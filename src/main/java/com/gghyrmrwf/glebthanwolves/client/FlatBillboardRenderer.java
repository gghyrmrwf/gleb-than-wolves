package com.gghyrmrwf.glebthanwolves.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Phase 3.15/3.17 — generic billboard renderer used to replace a vanilla
 * entity's 3D model with a single camera-facing PNG quad.
 *
 * <p>Server-side hitboxes and behaviour are untouched: this only changes
 * what is drawn on the client.
 */
public class FlatBillboardRenderer<T extends Entity> extends EntityRenderer<T> {

    private final ResourceLocation texture;
    private final float halfSize;
    private final float yOffset;

    public FlatBillboardRenderer(EntityRendererProvider.Context context,
                                 ResourceLocation texture,
                                 float halfSize,
                                 float yOffset) {
        super(context);
        this.texture = texture;
        this.halfSize = halfSize;
        this.yOffset = yOffset;
        this.shadowRadius = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    @Override
    public void render(T entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0, yOffset, 0.0);
        Quaternionf cameraOrientation = this.entityRenderDispatcher.cameraOrientation();
        poseStack.mulPose(cameraOrientation);
        poseStack.scale(-1f, 1f, 1f);

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(texture));

        addVertex(vc, matrix, normal, -halfSize, -halfSize, 0f, 1f, packedLight);
        addVertex(vc, matrix, normal,  halfSize, -halfSize, 1f, 1f, packedLight);
        addVertex(vc, matrix, normal,  halfSize,  halfSize, 1f, 0f, packedLight);
        addVertex(vc, matrix, normal, -halfSize,  halfSize, 0f, 0f, packedLight);

        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void addVertex(VertexConsumer vc, Matrix4f matrix, Matrix3f normal,
                                  float x, float y, float u, float v, int packedLight) {
        vc.vertex(matrix, x, y, 0f)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0f, 0f, 1f)
                .endVertex();
    }
}
