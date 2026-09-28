package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import net.bunten.enderscape.entity.rubblemite.Rubblemite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Rubblemite.class)
public abstract class RubblemiteStateMixin {
   @Inject(method = "isDashing", at = @At("HEAD"), cancellable = true)
   private void expansion$dash(CallbackInfoReturnable<Boolean> ci) {
      ci.setReturnValue(((Rubblemite)this).getFlags() == 2);
   }

   @Inject(method = "canHideInShell", at = @At("RETURN"), cancellable = true)
   private void expansion$hide(CallbackInfoReturnable<Boolean> ci) {
      Rubblemite mob = (Rubblemite)this;
      if (mob.hasEffect(ExpansionEffects.STUNNED) || (Integer)mob.getData(ExpansionEffects.PREPARE_DASH) > 0) {
         ci.setReturnValue(false);
      }
   }
}
