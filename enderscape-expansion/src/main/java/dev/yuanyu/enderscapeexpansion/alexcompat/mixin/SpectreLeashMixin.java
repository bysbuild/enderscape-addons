package dev.yuanyu.enderscapeexpansion.alexcompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.alexsmobsup.entity.EntitySpectre", remap = false)
public abstract class SpectreLeashMixin {
   @Inject(
      method = "tick()V",
      at = @At(value = "INVOKE", target = "Lcom/alexsmobsup/entity/EntitySpectre;dropLeash(ZZ)V", shift = Shift.AFTER),
      cancellable = true,
      require = 2,
      allow = 2
   )
   private void expansion$afterSpectreDetach(CallbackInfo ci) {
      ci.cancel();
   }
}
