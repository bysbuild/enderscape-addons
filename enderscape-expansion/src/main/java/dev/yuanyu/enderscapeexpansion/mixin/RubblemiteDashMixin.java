package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.RubblemiteBehavior;
import net.bunten.enderscape.entity.ai.behavior.RubblemiteDashDuringCombat;
import net.bunten.enderscape.entity.rubblemite.Rubblemite;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RubblemiteDashDuringCombat.class)
public abstract class RubblemiteDashMixin {
   @Inject(
      method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/bunten/enderscape/entity/rubblemite/Rubblemite;J)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void expansion$prepare(ServerLevel level, Rubblemite mob, long time, CallbackInfo ci) {
      RubblemiteBehavior.prepare(mob);
      ci.cancel();
   }
}
