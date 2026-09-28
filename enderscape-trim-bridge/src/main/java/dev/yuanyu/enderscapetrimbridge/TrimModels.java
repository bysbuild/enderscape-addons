package dev.yuanyu.enderscapetrimbridge;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class TrimModels {
   public static final String NAMESPACE = "enderscape_trim_bridge";
   public static final List<String> END_MATERIALS = List.of("enderscape:nebulite", "enderscape:shadoline");
   public static final List<String> ALL_MATERIALS = List.of(
      "minecraft:quartz",
      "minecraft:iron",
      "minecraft:netherite",
      "minecraft:redstone",
      "minecraft:copper",
      "minecraft:gold",
      "minecraft:emerald",
      "minecraft:diamond",
      "minecraft:lapis",
      "minecraft:amethyst",
      "enderscape:nebulite",
      "enderscape:shadoline"
   );
   public static final Map<ResourceLocation, List<String>> BASES = makeBases();

   private static Map<ResourceLocation, List<String>> makeBases() {
      Map<ResourceLocation, List<String>> bases = new LinkedHashMap<>();

      for (String armor : List.of("leather", "chainmail", "iron", "golden", "diamond", "netherite")) {
         for (String slot : List.of("helmet", "chestplate", "leggings", "boots")) {
            bases.put(ResourceLocation.withDefaultNamespace("item/" + armor + "_" + slot), END_MATERIALS);
         }
      }

      bases.put(ResourceLocation.withDefaultNamespace("item/turtle_helmet"), END_MATERIALS);
      bases.put(ResourceLocation.parse("enderscape:item/drift_leggings"), ALL_MATERIALS);
      return Map.copyOf(bases);
   }

   public static ResourceLocation predicate(String material) {
      return ResourceLocation.fromNamespaceAndPath("enderscape_trim_bridge", "material/" + material.replace(':', '/'));
   }

   public static ResourceLocation model(ResourceLocation base, String material) {
      return ResourceLocation.fromNamespaceAndPath(
         "enderscape_trim_bridge", "item/" + base.getNamespace() + "/" + base.getPath().substring(5) + "/" + material.replace(':', '/')
      );
   }
}
