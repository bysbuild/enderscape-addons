package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import dev.yuanyu.enderscapeexpansion.VoidSystem;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class OuterVoidMixin {
   @Inject(method = "onBelowWorld", at = @At("HEAD"), cancellable = true)
   private void expansion$warning(CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)this;
      if (!entity.level().isClientSide && entity.isAlive()) {
         int ticks = (Integer)entity.getData(ExpansionEffects.OUTER_VOID);
         int limit = Math.max(1, (int)(entity.getMaxHealth() * 2.5F));
         if (ticks >= limit) {
            entity.hurt(VoidSystem.damage(entity, "outer_void"), entity.getMaxHealth());
         } else {
            entity.setData(ExpansionEffects.OUTER_VOID, ticks + 1);
         }

         entity.getPersistentData().putInt("enderscape_expansion.outer_delay", 20);
         ci.cancel();
      }
   }
}
