package dev.yuanyu.enderscapeexpansion;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bunten.enderscape.registry.EnderscapeParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Pre;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeFogColor;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeFov;
import net.neoforged.neoforge.client.event.ViewportEvent.RenderFog;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public final class VoidScreenEffect {
   private static float alpha;
   private static float previous;
   private static int contactRecovery;
   private static float zoom;
   private static float previousZoom;
   private static LocalPlayer trackedPlayer;

   @SubscribeEvent
   public static void fov(ComputeFov event) {
      if (event.usedConfiguredFov()) {
         float amount = Mth.lerp((float)event.getPartialTick(), previousZoom, zoom);
         event.setFOV(event.getFOV() * (1.0F - 0.22F * amount));
      }
   }

   private static boolean submerged(Camera camera) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         return false;
      } else {
         Vec3 point = camera.getPosition();
         BlockPos pos = BlockPos.containing(point);
         FluidState fluid = level.getFluidState(pos);
         return fluid.getFluidType() == VoidFluid.TYPE.get() && point.y < pos.getY() + fluid.getHeight(level, pos);
      }
   }

   @SubscribeEvent
   public static void fog(RenderFog event) {
      if (submerged(event.getCamera())) {
         boolean spectator = event.getCamera().getEntity().isSpectator();
         event.setNearPlaneDistance(spectator ? -8.0F : 0.25F);
         event.setFarPlaneDistance(spectator ? event.getFarPlaneDistance() * 0.5F : 1.0F);
         event.setFogShape(FogShape.SPHERE);
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void fogColor(ComputeFogColor event) {
      if (submerged(event.getCamera())) {
         event.setRed(0.023529412F);
         event.setGreen(0.0F);
         event.setBlue(0.047058824F);
      }
   }

   @SubscribeEvent
   public static void tick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      previous = alpha;
      if (mc.player != trackedPlayer) {
         previousZoom = 0.0F;
         zoom = 0.0F;
         previous = 0.0F;
         alpha = 0.0F;
         contactRecovery = 0;
         trackedPlayer = mc.player;
      }

      if (mc.player != null) {
         if (!mc.isPaused()) {
            previousZoom = zoom;
            if (mc.player.isSpectator() || !mc.player.isAlive()) {
               contactRecovery = 0;
            } else if (VoidContactMotion.touching(mc.player)) {
               contactRecovery = 110;
            } else {
               contactRecovery = Math.max(0, contactRecovery - 1);
            }

            zoom = Mth.lerp(0.35F, zoom, contactRecovery / 110.0F);
            float target = ((Integer)mc.player.getData(ExpansionEffects.VOID_TICKS)).intValue() / 90.0F;
            float contactFade = Mth.clamp(contactRecovery / 20.0F, 0.0F, 1.0F);
            target = Math.max(target, 0.8F * contactFade);
            if (mc.player.isSpectator() || !mc.player.isAlive()) {
               target = 0.0F;
            }

            alpha = Mth.lerp(0.5F, alpha, Mth.clamp(target, 0.0F, 1.0F));
            if (contactRecovery > 0 && mc.level != null) {
               RandomSource random = mc.player.getRandom();

               for (int i = 0; i < 2; i++) {
                  if (random.nextFloat() < contactFade) {
                     Vec3 ahead = mc.player.getLookAngle().scale(0.8);
                     mc.level
                        .addParticle(
                           (ParticleOptions)EnderscapeParticles.VOID_STARS.get(),
                           mc.player.getX() + ahead.x + (random.nextDouble() - 0.5) * 2.4,
                           mc.player.getY() + random.nextDouble() * 1.8,
                           mc.player.getZ() + ahead.z + (random.nextDouble() - 0.5) * 2.4,
                           0.0,
                           0.02,
                           0.0
                        );
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void render(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && !mc.options.hideGui && !mc.player.isSpectator() && !(alpha < 0.001F)) {
         GuiGraphics graphics = event.getGuiGraphics();
         int w = graphics.guiWidth();
         int h = graphics.guiHeight();
         float amount = Mth.lerp(event.getPartialTick().getGameTimeDeltaPartialTick(false), previous, alpha);
         graphics.flush();
         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.blendFuncSeparate(SourceFactor.ZERO, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO);
         RenderSystem.setShaderColor(amount, amount, amount, 1.0F);

         try {
            graphics.blit(
               ResourceLocation.fromNamespaceAndPath("enderscape", "textures/misc/void_vignette_inverted.png"), 0, 0, w, h, 0.0F, 0.0F, 64, 64, 64, 64
            );
            graphics.blitSprite(ResourceLocation.fromNamespaceAndPath("enderscape", "overlay/void_noise_overlay_inverted"), 0, 0, w, h);
            graphics.flush();
         } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
         }
      }
   }
}
