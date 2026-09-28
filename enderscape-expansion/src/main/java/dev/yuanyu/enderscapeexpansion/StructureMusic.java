package dev.yuanyu.enderscapeexpansion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.neoforge.registries.DataPackRegistryEvent.NewRegistry;

@EventBusSubscriber(modid = "enderscape_expansion")
public record StructureMusic(ResourceLocation sound, List<ResourceLocation> structures) {
   public static final ResourceKey<Registry<StructureMusic>> KEY = ResourceKey.createRegistryKey(Expansion.id("structure_music"));
   public static final Codec<StructureMusic> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            ResourceLocation.CODEC.fieldOf("sound").forGetter(StructureMusic::sound),
            ResourceLocation.CODEC.listOf().fieldOf("permitted_structures").forGetter(StructureMusic::structures)
         )
         .apply(i, StructureMusic::new)
   );

   @SubscribeEvent
   public static void registry(NewRegistry event) {
      event.dataPackRegistry(KEY, CODEC, CODEC);
   }

   @SubscribeEvent
   public static void tick(Post event) {
      if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
         ServerLevel level = player.serverLevel();
         String track = "";
         if (level.hasChunkAt(player.blockPosition())) {
            label32:
            for (StructureMusic music : level.registryAccess().registryOrThrow(KEY)) {
               for (ResourceLocation id : music.structures) {
                  Structure structure = (Structure)level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(id);
                  if (structure != null && level.structureManager().getStructureAt(player.blockPosition(), structure).isValid()) {
                     track = music.sound.toString();
                     break label32;
                  }
               }
            }
         }

         if (!track.equals(player.getData(ExpansionEffects.STRUCTURE_MUSIC))) {
            player.setData(ExpansionEffects.STRUCTURE_MUSIC, track);
         }
      }
   }
}
