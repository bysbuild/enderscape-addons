package dev.yuanyu.enderscapeexpansion.mixin;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.monster.Silverfish$SilverfishMergeWithStoneGoal")
public abstract class SilverfishDelayMixin extends RandomStrollGoal {
   protected SilverfishDelayMixin(PathfinderMob mob, double speed) {
      super(mob, speed);
   }

   @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
   private void expansion$delay(CallbackInfoReturnable<Boolean> ci) {
      if (this.mob.tickCount < 60) {
         ci.setReturnValue(false);
      }
   }
}
