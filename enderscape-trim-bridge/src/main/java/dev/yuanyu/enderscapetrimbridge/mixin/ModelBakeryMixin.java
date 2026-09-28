package dev.yuanyu.enderscapetrimbridge.mixin;

import dev.yuanyu.enderscapetrimbridge.TrimModels;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverride.Predicate;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
   @Inject(method = "loadBlockModel", at = @At("RETURN"))
   private void enderscapeTrimBridge$append(ResourceLocation id, CallbackInfoReturnable<BlockModel> cir) {
      List<String> materials = TrimModels.BASES.get(id);
      if (materials != null) {
         BlockModel base = (BlockModel)cir.getReturnValue();

         for (String material : materials) {
            ResourceLocation target = TrimModels.model(id, material);
            if (base.getOverrides().stream().noneMatch(override -> override.getModel().equals(target))) {
               base.getOverrides().add(new ItemOverride(target, List.of(new Predicate(TrimModels.predicate(material), 1.0F))));
            }
         }
      }
   }
}
