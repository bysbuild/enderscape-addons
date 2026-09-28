package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.EndHaven;
import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.DimensionTransition.PostDimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class HavenRespawnMixin {
   @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
   private void expansion$havenFirst(boolean fromEndFight, PostDimensionTransition post, CallbackInfoReturnable<DimensionTransition> cir) {
      ServerPlayer player = (ServerPlayer)this;
      if (!fromEndFight && player.level().dimension() == Level.END && player.getPersistentData().getBoolean("enderscape_expansion.haven_requested")) {
         ServerLevel end = player.getServer().getLevel(Level.END);
         EndHaven.destination(end, (CompoundTag)player.getData(ExpansionEffects.HAVEN))
            .ifPresent(pos -> cir.setReturnValue(new DimensionTransition(end, pos, Vec3.ZERO, player.getYRot(), 0.0F, post)));
      }
   }
}
