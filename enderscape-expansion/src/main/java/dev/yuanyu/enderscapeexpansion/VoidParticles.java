package dev.yuanyu.enderscapeexpansion;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.SplashParticle.Provider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public class VoidParticles extends TextureSheetParticle {
   private final int mode;
   private final double startX;
   private final double startY;
   private final double startZ;

   private VoidParticles(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites, int mode) {
      super(level, x, y, z);
      this.mode = mode;
      this.startX = x;
      this.startY = y;
      this.startZ = z;
      this.pickSprite(sprites);
      this.xd = dx;
      this.yd = dy;
      this.zd = dz;
      this.quadSize = mode == 3 ? 0.1F * (this.random.nextFloat() * 0.2F + 0.5F) : 0.06F;
      this.lifetime = mode == 0 ? 40 : (mode == 1 ? 100 : (mode == 2 ? 20 : 40 + this.random.nextInt(10)));
      if (mode == 3) {
         this.rCol = this.rCol * (1.0F - level.getRandom().nextFloat() * 0.6F);
      }

      if (mode < 3) {
         this.setColor(0.25F, 0.04F, 0.5F);
      }

      this.hasPhysics = mode != 3;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public int getLightColor(float partial) {
      return Math.max(this.mode == 3 ? 15728784 : 15728800, super.getLightColor(partial));
   }

   public float getQuadSize(float partial) {
      if (this.mode != 3) {
         return super.getQuadSize(partial);
      } else {
         float progress = Mth.clamp((this.age + partial) / this.lifetime, 0.0F, 1.0F);
         return this.quadSize * (1.0F - (1.0F - progress) * (1.0F - progress)) * 0.3F;
      }
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
         if (this.mode == 0) {
            this.level.addParticle((ParticleOptions)ExpansionParticles.FALLING_VOID.get(), this.x, this.y, this.z, 0.0, 0.0, 0.0);
         }
      } else {
         if (this.mode == 3) {
            float p = (float)this.age / this.lifetime;
            double t = 1.0F + p - 2.0F * p * p;
            this.setPos(this.startX + this.xd * t, this.startY + this.yd * t + 1.0 - p, this.startZ + this.zd * t);
            this.alpha = 1.0F - p * p * p;
         } else if (this.mode == 1) {
            this.yd -= 0.06;
            this.move(this.xd, this.yd, this.zd);
            if (this.onGround) {
               this.remove();
               this.level.addParticle((ParticleOptions)ExpansionParticles.LANDING_VOID.get(), this.x, this.y, this.z, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   @SubscribeEvent
   public static void register(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet((ParticleType)ExpansionParticles.GLOW.get(), GlowParticle.Provider::new);
      event.registerSpriteSet((ParticleType)ExpansionParticles.GLOW_CHARGING.get(), GlowParticle.Provider::new);
      event.registerSpriteSet((ParticleType)ExpansionParticles.VOID_SPLASH.get(), Provider::new);
      event.registerSpriteSet((ParticleType)ExpansionParticles.SNOWFLAKE.get(), net.minecraft.client.particle.SnowflakeParticle.Provider::new);
      event.registerSpriteSet(
         (ParticleType)ExpansionParticles.DRIPPING_VOID.get(), s -> (t, l, x, y, z, dx, dy, dz) -> new VoidParticles(l, x, y, z, dx, dy, dz, s, 0)
      );
      event.registerSpriteSet(
         (ParticleType)ExpansionParticles.FALLING_VOID.get(), s -> (t, l, x, y, z, dx, dy, dz) -> new VoidParticles(l, x, y, z, dx, dy, dz, s, 1)
      );
      event.registerSpriteSet(
         (ParticleType)ExpansionParticles.LANDING_VOID.get(), s -> (t, l, x, y, z, dx, dy, dz) -> new VoidParticles(l, x, y, z, dx, dy, dz, s, 2)
      );
      event.registerSpriteSet(
         (ParticleType)ExpansionParticles.VOID_ENTITY.get(), s -> (t, l, x, y, z, dx, dy, dz) -> new VoidParticles(l, x, y, z, dx, dy, dz, s, 3)
      );
   }
}
