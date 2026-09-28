package dev.yuanyu.enderscapeexpansion;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public final class RustleParticle extends TextureSheetParticle {
   private RustleParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
      super(level, x, y, z, dx, dy, dz);
      this.pickSprite(sprites);
      this.setColor(1.0F, 1.0F, 1.0F);
      this.setSize(0.02F, 0.02F);
      this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.5F);
      this.xd *= 0.02;
      this.yd *= 0.02;
      this.zd *= 0.02;
      this.lifetime = (int)(20.0 / (this.random.nextDouble() * 0.8 + 0.2));
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double x, double y, double z) {
      this.setBoundingBox(this.getBoundingBox().move(x, y, z));
      this.setLocationFromBoundingbox();
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.lifetime-- <= 0) {
         this.remove();
      } else {
         this.move(this.xd, this.yd, this.zd);
         this.xd *= 0.99;
         this.yd *= 0.99;
         this.zd *= 0.99;
      }
   }

   @SubscribeEvent
   public static void register(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet(
         (ParticleType)ExpansionParticles.RUSTLE_CONVERTING.get(),
         sprites -> (type, level, x, y, z, dx, dy, dz) -> new RustleParticle(level, x, y, z, dx, dy, dz, sprites)
      );
   }
}
