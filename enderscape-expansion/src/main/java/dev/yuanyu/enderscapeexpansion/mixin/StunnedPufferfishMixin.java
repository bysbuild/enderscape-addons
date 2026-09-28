package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import net.minecraft.world.entity.animal.Pufferfish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pufferfish.class)
public abstract class StunnedPufferfishMixin {
   @Inject(method = "setPuffState", at = @At("HEAD"), cancellable = true)
   private void expansion$stop(int puff, CallbackInfo ci) {
      if (((Pufferfish)this).hasEffect(ExpansionEffects.STUNNED)) {
         ci.cancel();
      }
   }
}
