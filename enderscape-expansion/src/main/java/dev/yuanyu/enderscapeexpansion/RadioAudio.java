package dev.yuanyu.enderscapeexpansion;

import dev.yuanyu.enderscapeexpansion.block.MagniaRadio;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class RadioAudio {
   private static final Map<RadioBlockEntity, RadioAudio.Playing> ACTIVE = new WeakHashMap<>();

   public static void tick(RadioBlockEntity be) {
      if (be.current != null && (Boolean)be.getBlockState().getValue(MagniaRadio.ENABLED)) {
         RadioAudio.Playing current = ACTIVE.get(be);
         if (current == null || !current.song.equals(be.current)) {
            RadioSong song = be.song();
            if (song != null) {
               RadioAudio.Playing playing = new RadioAudio.Playing(be, song.sound());
               ACTIVE.put(be, playing);
               Minecraft.getInstance().getSoundManager().play(playing);
            }
         }
      } else {
         ACTIVE.remove(be);
      }
   }

   private static final class Playing extends AbstractTickableSoundInstance {
      private final RadioBlockEntity entity;
      private final ResourceLocation song;

      Playing(RadioBlockEntity be, ResourceLocation sound) {
         super((SoundEvent)BuiltInRegistries.SOUND_EVENT.get(sound), SoundSource.RECORDS, RandomSource.create());
         this.entity = be;
         this.song = be.current;
         this.x = be.getBlockPos().getX() + 0.5;
         this.y = be.getBlockPos().getY() + 0.5;
         this.z = be.getBlockPos().getZ() + 0.5;
         this.volume = 2.0F;
         this.pitch = 1.0F;
      }

      public void tick() {
         if (this.entity.isRemoved()
            || this.entity.getLevel() != Minecraft.getInstance().level
            || !this.song.equals(this.entity.current)
            || !(Boolean)this.entity.getBlockState().getValue(MagniaRadio.ENABLED)
            || !(Boolean)this.entity.getBlockState().getValue(MagniaRadio.PLAYING)) {
            this.stop();
         }
      }
   }
}
