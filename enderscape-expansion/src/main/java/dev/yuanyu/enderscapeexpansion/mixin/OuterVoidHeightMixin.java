package dev.yuanyu.enderscapeexpansion.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Entity.class)
public abstract class OuterVoidHeightMixin {
   @ModifyConstant(method = "checkBelowWorld", constant = @Constant(intValue = 64))
   private int expansion$height(int old) {
      return 8;
   }
}
