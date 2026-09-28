package dev.yuanyu.enderscapeexpansion.feature;

import com.google.common.base.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class VoidLakeFeature extends Feature<VoidLakeFeature.Config> {
   private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

   public VoidLakeFeature(Codec<VoidLakeFeature.Config> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<VoidLakeFeature.Config> context) {
      BlockPos origin = context.origin();
      if (origin.getY() <= context.level().getMinBuildHeight() + 4) {
         return false;
      } else {
         origin = origin.offset(-8, -4, -8);
         boolean[] grid = this.makeGrid(context);
         if (this.canPlace(origin, context, grid)) {
            this.placeFluidOrAir(origin, context, grid);
            this.placeBarrier(origin, context, grid);
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean[] makeGrid(FeaturePlaceContext<VoidLakeFeature.Config> context) {
      RandomSource random = context.random();
      VoidLakeFeature.Config config = (VoidLakeFeature.Config)context.config();
      boolean[] grid = new boolean[2048];
      int size = config.size().sample(random);

      for (int i = 0; i < size; i++) {
         double xr = random.nextDouble() * 6.0 + 3.0;
         double yr = random.nextDouble() * 4.0 + 2.0;
         double zr = random.nextDouble() * 6.0 + 3.0;
         double xp = random.nextDouble() * (16.0 - xr - 2.0) + 1.0 + xr / 2.0;
         double yp = random.nextDouble() * (8.0 - yr - 4.0) + 2.0 + yr / 2.0;
         double zp = random.nextDouble() * (16.0 - zr - 2.0) + 1.0 + zr / 2.0;

         for (int x = 1; x < 15; x++) {
            for (int z = 1; z < 15; z++) {
               for (int y = 1; y < 7; y++) {
                  double xd = (x - xp) / (xr / 2.0);
                  double yd = (y - yp) / (yr / 2.0);
                  double zd = (z - zp) / (zr / 2.0);
                  double distance = xd * xd + yd * yd + zd * zd;
                  if (distance < 1.0) {
                     grid[(x * 16 + z) * 8 + y] = true;
                  }
               }
            }
         }
      }

      return grid;
   }

   private boolean canPlace(BlockPos origin, FeaturePlaceContext<VoidLakeFeature.Config> context, boolean[] grid) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      VoidLakeFeature.Config config = (VoidLakeFeature.Config)context.config();
      BlockState fluid = config.fluid().getState(random, origin);

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            for (int y = 0; y < 8; y++) {
               boolean check = !grid[(x * 16 + z) * 8 + y]
                  && (
                     x < 15 && grid[((x + 1) * 16 + z) * 8 + y]
                        || x > 0 && grid[((x - 1) * 16 + z) * 8 + y]
                        || z < 15 && grid[(x * 16 + z + 1) * 8 + y]
                        || z > 0 && grid[(x * 16 + (z - 1)) * 8 + y]
                        || y < 7 && grid[(x * 16 + z) * 8 + y + 1]
                        || y > 0 && grid[(x * 16 + z) * 8 + (y - 1)]
                  );
               if (check) {
                  BlockState state = level.getBlockState(origin.offset(x, y, z));
                  if (y >= 4 && state.liquid()) {
                     return false;
                  }

                  if (y < 4 && !state.isSolid() && level.getBlockState(origin.offset(x, y, z)) != fluid) {
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   private void placeFluidOrAir(BlockPos origin, FeaturePlaceContext<VoidLakeFeature.Config> context, boolean[] grid) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      VoidLakeFeature.Config config = (VoidLakeFeature.Config)context.config();
      BlockState fluid = config.fluid().getState(random, origin);

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            for (int y = 0; y < 8; y++) {
               if (grid[(x * 16 + z) * 8 + y]) {
                  BlockPos placePos = origin.offset(x, y, z);
                  if (this.canReplaceBlock(level.getBlockState(placePos))) {
                     boolean placeAir = y >= 4;
                     level.setBlock(placePos, placeAir ? AIR : fluid, 2);
                     if (placeAir) {
                        level.scheduleTick(placePos, AIR.getBlock(), 0);
                        this.markAboveForPostProcessing(level, placePos);
                     }
                  }
               }
            }
         }
      }
   }

   private void placeBarrier(BlockPos origin, FeaturePlaceContext<VoidLakeFeature.Config> context, boolean[] grid) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      VoidLakeFeature.Config config = (VoidLakeFeature.Config)context.config();
      List<VoidLakeFeature.BarrierPart> parts = new ArrayList<>();

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            for (int y = 0; y < 8; y++) {
               boolean check = !grid[(x * 16 + z) * 8 + y]
                  && (
                     x < 15 && grid[((x + 1) * 16 + z) * 8 + y]
                        || x > 0 && grid[((x - 1) * 16 + z) * 8 + y]
                        || z < 15 && grid[(x * 16 + z + 1) * 8 + y]
                        || z > 0 && grid[(x * 16 + (z - 1)) * 8 + y]
                        || y < 7 && grid[(x * 16 + z) * 8 + y + 1]
                        || y > 0 && grid[(x * 16 + z) * 8 + (y - 1)]
                  );
               if (check && (y < 4 || random.nextInt(2) != 0)) {
                  int radius = config.barrierRadius().sample(random);

                  for (int xr = -radius; xr <= radius; xr++) {
                     for (int zr = -radius; zr <= radius; zr++) {
                        BlockPos offset = origin.offset(x + xr, y, z + zr);
                        float distance = Mth.sqrt(xr * xr + zr * zr);
                        if (distance <= radius
                           && random.nextFloat() <= config.barrierPlacementChance().sample(random)
                           && this.placeBarrierBlock(level, offset, config.barrier().getState(random, origin), state -> state.isSolid())) {
                           parts.add(new VoidLakeFeature.BarrierPart(offset, radius, distance));
                        }
                     }
                  }
               }
            }
         }
      }

      this.placeBarrierPillars(context, parts);
   }

   private void placeBarrierPillars(FeaturePlaceContext<VoidLakeFeature.Config> context, List<VoidLakeFeature.BarrierPart> parts) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      VoidLakeFeature.Config config = (VoidLakeFeature.Config)context.config();
      parts.forEach(part -> {
         BlockPos position = part.position();
         float radius = part.radius();
         float distance = part.distance();
         double normalized = distance / (radius * Mth.sqrt(2.0F));
         double multiplier = Mth.cos((float)normalized * (float) (Math.PI / 2));
         int rimHeight = (int)(config.barrierPillarHeight().sample(random) * multiplier);

         for (int i = 1; i < rimHeight; i++) {
            BlockPos above = position.above(i);
            if (!this.placeBarrierBlock(level, above, config.barrier().getState(random, above), state -> !state.isSolid())) {
               break;
            }
         }
      });
   }

   private boolean placeBarrierBlock(WorldGenLevel level, BlockPos offset, BlockState barrier, Predicate<BlockState> predicate) {
      BlockState offsetState = level.getBlockState(offset);
      if (predicate.test(offsetState) && level.getFluidState(offset).isEmpty() && !offsetState.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
         level.setBlock(offset, barrier, 2);
         this.markAboveForPostProcessing(level, offset);
         return true;
      } else {
         return false;
      }
   }

   private boolean canReplaceBlock(BlockState state) {
      return !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
   }

   private record BarrierPart(BlockPos position, int radius, float distance) {
   }

   public record Config(
      BlockStateProvider fluid,
      BlockStateProvider barrier,
      IntProvider size,
      IntProvider barrierRadius,
      IntProvider barrierPillarHeight,
      FloatProvider barrierPlacementChance
   ) implements FeatureConfiguration {
      public static final Codec<VoidLakeFeature.Config> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               BlockStateProvider.CODEC.fieldOf("fluid").forGetter(VoidLakeFeature.Config::fluid),
               BlockStateProvider.CODEC.fieldOf("barrier").forGetter(VoidLakeFeature.Config::barrier),
               IntProvider.CODEC.fieldOf("size").forGetter(VoidLakeFeature.Config::size),
               IntProvider.CODEC.fieldOf("barrier_radius").forGetter(VoidLakeFeature.Config::barrierRadius),
               IntProvider.CODEC.fieldOf("barrier_pillar_height").forGetter(VoidLakeFeature.Config::barrierPillarHeight),
               FloatProvider.CODEC.fieldOf("barrier_placement_chance").forGetter(VoidLakeFeature.Config::barrierPlacementChance)
            )
            .apply(instance, VoidLakeFeature.Config::new)
      );
   }
}
