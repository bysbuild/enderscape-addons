package dev.yuanyu.enderscapeexpansion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.yuanyu.enderscapeexpansion.block.StorageShelf;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class ShelfRenderer implements BlockEntityRenderer<ShelfBlockEntity> {
   private final ItemRenderer items;

   public ShelfRenderer(Context c) {
      this.items = c.getItemRenderer();
   }

   public void render(ShelfBlockEntity be, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      for (int slot = 0; slot < 3; slot++) {
         ItemStack stack = be.getItem(slot);
         if (!stack.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5, 0.5, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(-((Direction)be.getBlockState().getValue(StorageShelf.FACING)).toYRot()));
            pose.translate((slot - 1) * 0.3125, 0.0, -0.25);
            pose.scale(0.25F, 0.25F, 0.25F);
            this.items.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, be.getLevel(), slot);
            pose.popPose();
         }
      }
   }
}
