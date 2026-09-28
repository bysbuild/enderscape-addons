package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.client.entity.rubblemite.RubblemiteRenderer;
import net.bunten.enderscape.registry.EnderscapeEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public class AnimatedRubblemiteRenderer extends RubblemiteRenderer {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "rubblemite"), "main");

   public AnimatedRubblemiteRenderer(Context context) {
      super(context);
      this.model = new AnimatedRubblemiteModel(context.bakeLayer(LAYER));
   }

   @SubscribeEvent
   public static void layer(RegisterLayerDefinitions event) {
      event.registerLayerDefinition(LAYER, AnimatedRubblemiteModel::createLayer);
   }

   @SubscribeEvent
   public static void renderer(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)EnderscapeEntities.RUBBLEMITE.get(), AnimatedRubblemiteRenderer::new);
   }
}
