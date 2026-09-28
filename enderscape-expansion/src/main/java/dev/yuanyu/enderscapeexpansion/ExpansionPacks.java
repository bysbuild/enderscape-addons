package dev.yuanyu.enderscapeexpansion;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack.Position;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class ExpansionPacks {
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void add(AddPackFindersEvent event) {
      event.addPackFinders(
         ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "resourcepacks/expansion_terrain"),
         PackType.SERVER_DATA,
         Component.literal("Enderscape Expansion Terrain"),
         PackSource.BUILT_IN,
         true,
         Position.TOP
      );
      event.addPackFinders(
         ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "resourcepacks/expansion_visuals"),
         PackType.CLIENT_RESOURCES,
         Component.literal("Enderscape Expansion Dragon Egg"),
         PackSource.BUILT_IN,
         true,
         Position.TOP
      );
   }
}
