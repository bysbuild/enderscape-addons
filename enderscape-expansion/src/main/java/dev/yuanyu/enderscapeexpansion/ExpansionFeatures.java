package dev.yuanyu.enderscapeexpansion;

import dev.yuanyu.enderscapeexpansion.feature.MagniaArchFeature;
import dev.yuanyu.enderscapeexpansion.feature.MagniaSpikeFeature;
import dev.yuanyu.enderscapeexpansion.feature.MagniaTowerFeature;
import dev.yuanyu.enderscapeexpansion.feature.MurublightBracketFeature;
import dev.yuanyu.enderscapeexpansion.feature.VoidLakeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ExpansionFeatures {
   private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, "enderscape_expansion");

   public static void register(IEventBus bus) {
      FEATURES.register(bus);
   }

   static {
      FEATURES.register("murublight_bracket", () -> new MurublightBracketFeature(NoneFeatureConfiguration.CODEC));
      FEATURES.register("magnia_arch", MagniaArchFeature::new);
      FEATURES.register("magnia_tower", () -> new MagniaTowerFeature(MagniaTowerFeature.Config.CODEC));
      FEATURES.register("magnia_spike", () -> new MagniaSpikeFeature(MagniaSpikeFeature.Config.CODEC));
      FEATURES.register("void_lake", () -> new VoidLakeFeature(VoidLakeFeature.Config.CODEC));
   }
}
