package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.block.VoidCampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {
   @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
   private void expansion$campfire(BlockState state, CallbackInfoReturnable<Boolean> cir) {
      if (this == BlockEntityType.CAMPFIRE && state.getBlock() instanceof VoidCampfireBlock) {
         cir.setReturnValue(true);
      }
   }
}
