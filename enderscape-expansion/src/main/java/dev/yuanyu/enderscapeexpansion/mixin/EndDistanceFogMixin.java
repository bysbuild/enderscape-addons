package dev.yuanyu.enderscapeexpansion.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FogRenderer.class, priority = 900)
public abstract class EndDistanceFogMixin {
   @Inject(method = "setupFog", at = @At("RETURN"))
   private static void expansion$distantVisibility(Camera camera, FogMode mode, float viewDistance, boolean thickFog, float partialTick, CallbackInfo ci) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null && level.dimension() == Level.END && !thickFog && mode == FogMode.FOG_TERRAIN && camera.getFluidInCamera() == FogType.NONE) {
         if (!(camera.getEntity() instanceof LivingEntity entity && (entity.hasEffect(MobEffects.BLINDNESS) || entity.hasEffect(MobEffects.DARKNESS)))) {
            RenderSystem.setShaderFogStart(viewDistance * 0.85F);
            RenderSystem.setShaderFogEnd(viewDistance);
         }
      }
   }
}
