package dev.yuanyu.enderscapeexpansion;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("enderscape_expansion")
public final class Expansion {
   public static final String MOD_ID = "enderscape_expansion";

   public Expansion(IEventBus bus) {
      CheeseOreCompat.register(bus);
      ExpansionGameRules.init();
      ExpansionEffects.register(bus);
      ExpansionBlocks.register(bus);
      ExpansionItems.register(bus);
      ConversionRecipe.register(bus);
      VoidFluid.register(bus);
      HavenBlockEntity.register(bus);
      ExpansionSounds.register(bus);
      ExpansionParticles.register(bus);
      UnifiedShield.register(bus);
      ExpansionFeatures.register(bus);
      RadioBlockEntity.register(bus);
      ShelfBlockEntity.register(bus);
      ExpansionPotions.register(bus);
      ToolFuelingRecipe.register(bus);
   }

   public static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("enderscape", path);
   }
}
