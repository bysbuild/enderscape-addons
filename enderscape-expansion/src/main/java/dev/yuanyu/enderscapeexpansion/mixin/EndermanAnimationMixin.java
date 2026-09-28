package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.EndermanAnimation;
import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import dev.yuanyu.enderscapeexpansion.ExpansionParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderMan.class)
public abstract class EndermanAnimationMixin implements EndermanAnimation {
   @Unique
   private final AnimationState expansion$attack = new AnimationState();

   @ModifyArg(
      method = "aiStep",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"),
      index = 0
   )
   private ParticleOptions expansion$particle(ParticleOptions original) {
      return (ParticleOptions)ExpansionParticles.VOID_ENTITY.get();
   }

   @Override
   public AnimationState expansion$attackState() {
      return this.expansion$attack;
   }

   @Inject(method = "aiStep", at = @At("TAIL"))
   private void expansion$animation(CallbackInfo ci) {
      EnderMan self = (EnderMan)this;
      int ticks = (Integer)self.getData(ExpansionEffects.ATTACK_TICKS);
      if (self.level().isClientSide) {
         this.expansion$attack.animateWhen(ticks > 0, self.tickCount);
      } else if (ticks > 0) {
         self.setData(ExpansionEffects.ATTACK_TICKS, ticks - 1);
      }
   }
}
