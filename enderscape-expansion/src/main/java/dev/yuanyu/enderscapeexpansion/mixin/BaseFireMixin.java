package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionBlocks;
import dev.yuanyu.enderscapeexpansion.block.VoidFireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireMixin {
   @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
   private static void expansion$voidFire(BlockGetter level, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
      if (VoidFireBlock.supports(level.getBlockState(pos.below()))) {
         cir.setReturnValue(((Block)ExpansionBlocks.VOID_FIRE.get()).defaultBlockState());
      }
   }
}
