package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class StunnedEndermanMixin {
   @Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
   private void expansion$stop(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
      if (((EnderMan)this).hasEffect(ExpansionEffects.STUNNED)) {
         cir.setReturnValue(false);
      }
   }
}
