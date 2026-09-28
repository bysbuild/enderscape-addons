package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Armadillo.class)
public abstract class StunnedArmadilloMixin {
   @Inject(method = {"rollUp", "rollOut"}, at = @At("HEAD"), cancellable = true)
   private void expansion$stop(CallbackInfo ci) {
      if (((Armadillo)this).hasEffect(ExpansionEffects.STUNNED)) {
         ci.cancel();
      }
   }
}
