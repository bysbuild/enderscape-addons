package dev.yuanyu.enderscapeexpansion;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;

public class GlowParticle extends SimpleAnimatedParticle {
   GlowParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites, GlowParticleOptions options) {
      super(level, x, y, z, sprites, 0.0F);
      this.xd = xd;
      this.yd = yd;
      this.zd = zd;
      this.gravity = 0.02F;
      this.friction = options.friction();
      this.quadSize = Mth.nextFloat(this.random, 0.06F, 0.12F);
      this.setSpriteFromAge(sprites);
      this.setLifetime(options.lifetime().sample(this.random));
      this.setAlpha(1.0F);
      this.hasPhysics = false;
   }

   public void tick() {
      super.tick();
      if (!this.removed) {
         this.setSpriteFromAge(this.sprites);
      }
   }

   public int getLightColor(float delta) {
      return 15728880;
   }

   public static class Provider implements ParticleProvider<GlowParticleOptions> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(GlowParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         return new GlowParticle(level, x, y, z, xd, yd, zd, this.sprites, options);
      }
   }
}
