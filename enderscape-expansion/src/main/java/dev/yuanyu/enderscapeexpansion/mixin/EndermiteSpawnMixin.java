package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Endermite.class)
public abstract class EndermiteSpawnMixin {
   @Inject(method = "checkEndermiteSpawnRules", at = @At("RETURN"), cancellable = true)
   private static void expansion$light(
      EntityType<Endermite> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> ci
   ) {
      if (ci.getReturnValueZ() && level instanceof ServerLevelAccessor server) {
         ci.setReturnValue(Monster.checkMonsterSpawnRules(type, server, reason, pos, random));
      }
   }

   @ModifyArg(
      method = "aiStep",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"),
      index = 0
   )
   private ParticleOptions expansion$particle(ParticleOptions original) {
      return (ParticleOptions)ExpansionParticles.VOID_ENTITY.get();
   }
}
