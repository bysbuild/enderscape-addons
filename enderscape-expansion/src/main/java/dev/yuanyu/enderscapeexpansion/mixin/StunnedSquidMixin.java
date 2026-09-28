package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import net.minecraft.world.entity.animal.Squid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Squid.class)
public abstract class StunnedSquidMixin {
   @Inject(method = "spawnInk", at = @At("HEAD"), cancellable = true)
   private void expansion$stop(CallbackInfo ci) {
      if (((Squid)this).hasEffect(ExpansionEffects.STUNNED)) {
         ci.cancel();
      }
   }
}
