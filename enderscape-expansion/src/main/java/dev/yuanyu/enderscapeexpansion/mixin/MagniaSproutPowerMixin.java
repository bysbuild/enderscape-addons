package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.MagniaPower;
import net.bunten.enderscape.block.MagniaSproutBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MagniaSproutBlock.class)
public abstract class MagniaSproutPowerMixin {
   @Inject(method = "getNeighborSignal", at = @At("HEAD"), cancellable = true)
   private void expansion$power(SignalGetter level, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
      BlockState receiver = ((MagniaSproutBlock)this).defaultBlockState();

      for (Direction d : Direction.values()) {
         if (d != facing && MagniaPower.signal(level.getBlockState(pos.relative(d)), receiver) > 0) {
            cir.setReturnValue(true);
            return;
         }
      }
   }
}
