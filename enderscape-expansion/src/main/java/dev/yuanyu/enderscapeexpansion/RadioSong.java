package dev.yuanyu.enderscapeexpansion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent.NewRegistry;

@EventBusSubscriber(modid = "enderscape_expansion", bus = Bus.MOD)
public record RadioSong(ResourceLocation sound, Component description, float seconds, Optional<Integer> signal, Optional<TagKey<Biome>> biomes) {
   public static final ResourceKey<Registry<RadioSong>> KEY = ResourceKey.createRegistryKey(Expansion.id("magnia_radio_song"));
   public static final Codec<RadioSong> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            ResourceLocation.CODEC.fieldOf("sound_event").forGetter(RadioSong::sound),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(RadioSong::description),
            Codec.floatRange(0.01F, 86400.0F).fieldOf("length_in_seconds").forGetter(RadioSong::seconds),
            Codec.intRange(0, 15).optionalFieldOf("exclusive_signal").forGetter(RadioSong::signal),
            TagKey.codec(Registries.BIOME).optionalFieldOf("permitted_biomes").forGetter(RadioSong::biomes)
         )
         .apply(i, RadioSong::new)
   );

   @SubscribeEvent
   public static void registry(NewRegistry e) {
      e.dataPackRegistry(KEY, CODEC, CODEC);
   }
}
