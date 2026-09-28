package dev.yuanyu.enderscapeexpansion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;

public record GlowParticleOptions(ParticleType<GlowParticleOptions> particle, IntProvider lifetime, float friction) implements ParticleOptions {
   public static final GlowParticleOptions CHARGING = new GlowParticleOptions(
      (ParticleType<GlowParticleOptions>)ExpansionParticles.GLOW_CHARGING.get(), UniformInt.of(5, 10), 0.6F
   );
   public static final GlowParticleOptions DEFAULT = new GlowParticleOptions(
      (ParticleType<GlowParticleOptions>)ExpansionParticles.GLOW.get(), UniformInt.of(15, 30), 0.9F
   );

   public static MapCodec<GlowParticleOptions> codec(ParticleType<GlowParticleOptions> type) {
      return RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               IntProvider.CODEC.fieldOf("lifetime").forGetter(options -> options.lifetime),
               Codec.FLOAT.fieldOf("friction").forGetter(options -> options.friction)
            )
            .apply(instance, (lifetime, friction) -> new GlowParticleOptions(type, lifetime, friction))
      );
   }

   public static StreamCodec<? super ByteBuf, GlowParticleOptions> streamCodec(ParticleType<GlowParticleOptions> type) {
      return StreamCodec.composite(
         ByteBufCodecs.fromCodec(IntProvider.CODEC),
         options -> options.lifetime,
         ByteBufCodecs.FLOAT,
         options -> options.friction,
         (lifetime, friction) -> new GlowParticleOptions(type, lifetime, friction)
      );
   }

   public ParticleType<GlowParticleOptions> getType() {
      return this.particle;
   }
}
