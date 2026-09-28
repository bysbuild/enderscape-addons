package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {
   @Inject(method = "doHurtTarget", at = @At("RETURN"))
   private void expansion$attack(Entity target, CallbackInfoReturnable<Boolean> ci) {
      if (this instanceof EnderMan self) {
         self.setData(ExpansionEffects.ATTACK_TICKS, 15);
      }
   }

   @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
   private void expansion$stunnedAI(CallbackInfo ci) {
      Mob self = (Mob)this;
      if (self.hasEffect(ExpansionEffects.STUNNED)) {
         self.getNavigation().stop();
         ci.cancel();
      }
   }
}
