package dev.yuanyu.enderscapeexpansion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public class ImprovedEndermanRenderer extends MobRenderer<EnderMan, ImprovedEndermanModel> {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "enderman"), "main");

   private static ResourceLocation texture(String name) {
      return ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "textures/entity/enderman/" + name + ".png");
   }

   @SubscribeEvent
   public static void layers(RegisterLayerDefinitions event) {
      event.registerLayerDefinition(LAYER, ImprovedEndermanModel::createLayer);
   }

   @SubscribeEvent
   public static void renderer(RegisterRenderers event) {
      event.registerEntityRenderer(EntityType.ENDERMAN, ImprovedEndermanRenderer::new);
   }

   public ImprovedEndermanRenderer(final Context context) {
      super(context, new ImprovedEndermanModel(context.bakeLayer(LAYER)), 0.5F);
      this.addLayer(
         new RenderLayer<EnderMan, ImprovedEndermanModel>(this) {
            public void render(
               PoseStack pose,
               MultiBufferSource buffers,
               int light,
               EnderMan entity,
               float limb,
               float amount,
               float partial,
               float age,
               float yaw,
               float pitch
            ) {
               float brightness = Math.max(LightTexture.block(light) / 15.0F, Math.max(0, LightTexture.sky(light) - entity.level().getSkyDarken()) / 15.0F);
               brightness = Mth.clamp(brightness, 0.0F, 1.0F);
               int bright = (int)(brightness * 255.0F) << 24 | 16777215;
               int dark = (int)((1.0F - brightness) * 255.0F) << 24 | 16777215;
               ((ImprovedEndermanModel)this.getParentModel())
                  .renderToBuffer(
                     pose, buffers.getBuffer(RenderType.eyes(ImprovedEndermanRenderer.texture("enderman_eyes_dark"))), light, OverlayTexture.NO_OVERLAY, dark
                  );
               ((ImprovedEndermanModel)this.getParentModel())
                  .renderToBuffer(
                     pose,
                     buffers.getBuffer(RenderType.eyes(ImprovedEndermanRenderer.texture("enderman_eyes_bright"))),
                     light,
                     OverlayTexture.NO_OVERLAY,
                     bright
                  );
               if (entity.getCarriedBlock() != null) {
                  pose.pushPose();
                  ((ImprovedEndermanModel)this.getParentModel()).applyCarriedBlockTransform(pose);
                  pose.translate(-0.5, -0.5, -0.5);
                  context.getBlockRenderDispatcher().renderSingleBlock(entity.getCarriedBlock(), pose, buffers, light, OverlayTexture.NO_OVERLAY);
                  pose.popPose();
               }
            }
         }
      );
   }

   public ResourceLocation getTextureLocation(EnderMan entity) {
      return texture("enderman");
   }
}
