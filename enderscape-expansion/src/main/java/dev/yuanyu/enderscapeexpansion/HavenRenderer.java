package dev.yuanyu.enderscapeexpansion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.yuanyu.enderscapeexpansion.block.EndHavenCore;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.FaceInfo.Constants;
import net.minecraft.client.renderer.FaceInfo.VertexInfo;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public final class HavenRenderer implements BlockEntityRenderer<HavenBlockEntity> {
   @SubscribeEvent
   public static void register(RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType)HavenBlockEntity.TYPE.get(), ctx -> new HavenRenderer());
      event.registerBlockEntityRenderer((BlockEntityType)ShelfBlockEntity.TYPE.get(), ShelfRenderer::new);
   }

   public void render(HavenBlockEntity entity, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      if (entity.getBlockState().getValue(EndHavenCore.STATE) == EndHavenCore.State.ACTIVE) {
         VertexConsumer consumer = buffers.getBuffer(RenderType.endGateway());

         for (Direction direction : Direction.values()) {
            for (int i = 0; i < 4; i++) {
               VertexInfo vertex = FaceInfo.fromFacing(direction).getVertexInfo(i);
               consumer.addVertex(
                  pose.last().pose(),
                  vertex.xFace == Constants.MAX_X ? 0.95F : 0.05F,
                  vertex.yFace == Constants.MAX_Y ? 0.95F : 0.05F,
                  vertex.zFace == Constants.MAX_Z ? 0.95F : 0.05F
               );
            }
         }
      }
   }

   public int getViewDistance() {
      return 256;
   }
}
