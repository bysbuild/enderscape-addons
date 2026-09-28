package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.Expansion;
import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 1100)
public abstract class StructureMusicMixin {
   @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
   private void expansion$music(CallbackInfoReturnable<Music> ci) {
      Minecraft mc = (Minecraft)this;
      if (mc.player != null && mc.level != null) {
         if (mc.screen == null || mc.screen.getBackgroundMusic() == null) {
            boolean inEnd = mc.level.dimension().equals(Level.END);
            if (!inEnd || !mc.gui.getBossOverlay().shouldPlayMusic()) {
               String name = (String)mc.player.getData(ExpansionEffects.STRUCTURE_MUSIC);
               if (!name.isEmpty()) {
                  BuiltInRegistries.SOUND_EVENT.getHolder(ResourceLocation.parse(name)).ifPresent(sound -> ci.setReturnValue(new Music(sound, 0, 1200, true)));
               } else if (inEnd) {
                  Holder<Biome> biome = mc.level.getBiome(mc.player.blockPosition());
                  Optional<Music> configured = ((Biome)biome.value()).getBackgroundMusic();
                  if (!configured.isPresent()) {
                     if (biome.unwrapKey()
                        .map(k -> k.location().getNamespace().equals("minecraft") || k.location().getNamespace().equals("enderscape"))
                        .orElse(false)) {
                        BuiltInRegistries.SOUND_EVENT
                           .getHolder(Expansion.id("music.enderscape.biome.default_end"))
                           .ifPresent(sound -> ci.setReturnValue(new Music(sound, 200, 1200, false)));
                     }
                  } else {
                     Music music = configured.get();
                     Optional<ResourceKey<SoundEvent>> id = music.getEvent().unwrapKey();
                     if (id.isPresent() && id.get().location().getNamespace().equals("enderscape")) {
                        ci.setReturnValue(new Music(music.getEvent(), 200, 1200, false));
                     } else {
                        ci.setReturnValue(music);
                     }
                  }
               }
            }
         }
      }
   }
}
