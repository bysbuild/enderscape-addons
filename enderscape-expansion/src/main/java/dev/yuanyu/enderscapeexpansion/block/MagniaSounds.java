package dev.yuanyu.enderscapeexpansion.block;

import dev.yuanyu.enderscapeexpansion.Expansion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

final class MagniaSounds {
   static void play(Level l, BlockPos p, String name) {
      SoundEvent s = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("block." + name));
      if (s != null) {
         l.playSound(null, p, s, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }
}
