package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.EndAmbientLight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LightTexture.class)
public abstract class EndAmbientLightMixin {
   @ModifyArg(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"), index = 2)
   private int expansion$ambientLight(int color) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.level.dimension() != Level.END || mc.player == null) {
         return color;
      } else {
         return !mc.player.hasEffect(MobEffects.DARKNESS) && !mc.player.hasEffect(MobEffects.BLINDNESS) ? EndAmbientLight.brighten(color) : color;
      }
   }
}
