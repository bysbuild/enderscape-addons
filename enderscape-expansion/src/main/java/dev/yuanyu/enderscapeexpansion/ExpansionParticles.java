package dev.yuanyu.enderscapeexpansion;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ExpansionParticles {
   private static final DeferredRegister<ParticleType<?>> TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, "enderscape");
   public static final DeferredHolder<ParticleType<?>, ParticleType<GlowParticleOptions>> GLOW = glow("glow");
   public static final DeferredHolder<ParticleType<?>, ParticleType<GlowParticleOptions>> GLOW_CHARGING = glow("glow_charging");
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUSTLE_CONVERTING = TYPES.register(
      "rustle_converting", () -> new SimpleParticleType(true)
   );
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VOID_SPLASH = add("void_splash");
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DRIPPING_VOID = add("dripping_void_lachryma");
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FALLING_VOID = add("falling_void_lachryma");
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LANDING_VOID = add("landing_void_lachryma");
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VOID_ENTITY = add("void_entity");
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SNOWFLAKE = add("snowflake");

   private static DeferredHolder<ParticleType<?>, ParticleType<GlowParticleOptions>> glow(String name) {
      return TYPES.register(name, () -> new ParticleType<GlowParticleOptions>(false) {
         public MapCodec<GlowParticleOptions> codec() {
            return GlowParticleOptions.codec(this);
         }

         public StreamCodec<? super RegistryFriendlyByteBuf, GlowParticleOptions> streamCodec() {
            return GlowParticleOptions.streamCodec(this);
         }
      });
   }

   private static DeferredHolder<ParticleType<?>, SimpleParticleType> add(String name) {
      return TYPES.register(name, () -> new SimpleParticleType(false));
   }

   public static void register(IEventBus bus) {
      TYPES.register(bus);
   }
}
