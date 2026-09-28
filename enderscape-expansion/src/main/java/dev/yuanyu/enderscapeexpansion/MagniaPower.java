package dev.yuanyu.enderscapeexpansion;

import dev.yuanyu.enderscapeexpansion.block.BlisteredMagnia;
import dev.yuanyu.enderscapeexpansion.block.PolarizedMagnia;
import net.bunten.enderscape.block.MagniaBlock;
import net.bunten.enderscape.block.MagniaSproutBlock;
import net.bunten.enderscape.block.properties.MagniaType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class MagniaPower {
   public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);

   public static String polarity(BlockState s) {
      if (s.getBlock() instanceof PolarizedMagnia) {
         return ((MagniaType)s.getValue(PolarizedMagnia.POLARITY)).getSerializedName();
      } else if (s.getBlock() instanceof BlisteredMagnia) {
         return ((BlisteredMagnia.Polarity)s.getValue(BlisteredMagnia.POLARITY)).getSerializedName();
      } else {
         MagniaType type = MagniaBlock.getMagniaType(s);
         if (type == null) {
            type = MagniaSproutBlock.getMagniaType(s);
         }

         return type == null ? "none" : type.getSerializedName();
      }
   }

   public static int signal(BlockState source, BlockState receiver) {
      if (polarity(source).equals("none") || !polarity(source).equals(polarity(receiver))) {
         return 0;
      } else if (!(source.getBlock() instanceof PolarizedMagnia) && !(source.getBlock() instanceof BlisteredMagnia)) {
         return source.hasProperty(POWER) ? (Integer)source.getValue(POWER) : 0;
      } else {
         return 15;
      }
   }

   public static int strongest(BlockState receiver, BlockGetter level, BlockPos pos) {
      int max = 0;

      for (Direction d : Direction.values()) {
         BlockState s = level.getBlockState(pos.relative(d));
         int value = signal(s, receiver);
         if (s.is(receiver.getBlock())) {
            value--;
         }

         max = Math.max(max, value);
      }

      return max;
   }
}
