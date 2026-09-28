package dev.yuanyu.enderscapeexpansion.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Silverfish.class)
public abstract class SilverfishSpawnMixin {
   @Inject(method = "checkSilverfishSpawnRules", at = @At("RETURN"), cancellable = true)
   private static void expansion$light(
      EntityType<Silverfish> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> ci
   ) {
      if (ci.getReturnValueZ() && level instanceof ServerLevelAccessor server) {
         ci.setReturnValue(Monster.checkMonsterSpawnRules(type, server, reason, pos, random));
      }
   }
}
