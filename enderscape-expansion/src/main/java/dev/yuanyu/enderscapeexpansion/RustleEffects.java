package dev.yuanyu.enderscapeexpansion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record RustleEffects(int duration, float pitch, ResourceLocation swell, ResourceLocation spit, ResourceLocation particle) {
   public static final RustleEffects DEFAULT = new RustleEffects(
      20, 1.0F, Expansion.id("entity.rustle.swell"), Expansion.id("entity.rustle.spit"), Expansion.id("rustle_converting")
   );
   public static final Codec<RustleEffects> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Codec.intRange(0, 200).optionalFieldOf("swell_duration", 20).forGetter(RustleEffects::duration),
            Codec.floatRange(0.0F, 2.0F).optionalFieldOf("swell_sound_pitch", 1.0F).forGetter(RustleEffects::pitch),
            ResourceLocation.CODEC.optionalFieldOf("swell_sound", DEFAULT.swell).forGetter(RustleEffects::swell),
            ResourceLocation.CODEC.optionalFieldOf("spit_sound", DEFAULT.spit).forGetter(RustleEffects::spit),
            ResourceLocation.CODEC.optionalFieldOf("convert_particle", DEFAULT.particle).forGetter(RustleEffects::particle)
         )
         .apply(i, RustleEffects::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, RustleEffects> STREAM = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      RustleEffects::duration,
      ByteBufCodecs.FLOAT,
      RustleEffects::pitch,
      ResourceLocation.STREAM_CODEC,
      RustleEffects::swell,
      ResourceLocation.STREAM_CODEC,
      RustleEffects::spit,
      ResourceLocation.STREAM_CODEC,
      RustleEffects::particle,
      RustleEffects::new
   );
}
