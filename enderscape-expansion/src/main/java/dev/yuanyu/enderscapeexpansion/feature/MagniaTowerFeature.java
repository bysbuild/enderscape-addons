package dev.yuanyu.enderscapeexpansion.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.yuanyu.enderscapeexpansion.ExpansionBlocks;
import dev.yuanyu.enderscapeexpansion.block.BlisteredMagnia;
import java.util.List;
import java.util.Optional;
import net.bunten.enderscape.block.MagniaBlock;
import net.bunten.enderscape.block.MagniaSproutBlock;
import net.bunten.enderscape.block.properties.MagniaType;
import net.bunten.enderscape.registry.EnderscapeBlocks;
import net.bunten.enderscape.registry.tag.EnderscapeBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class MagniaTowerFeature extends Feature<MagniaTowerFeature.Config> {
   public MagniaTowerFeature(Codec<MagniaTowerFeature.Config> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<MagniaTowerFeature.Config> context) {
      MagniaTowerFeature.Config config = (MagniaTowerFeature.Config)context.config();
      WorldGenLevel level = context.level();
      BlockPos origin = context.origin();
      RandomSource random = context.random();
      if (level.getBlockState(origin).isAir() && this.hasSolidBase(level, origin, 3)) {
         List<BlockPos> placedBlocks = Lists.newArrayList();
         this.replaceFloor(level, origin, random, placedBlocks, config);
         this.generateTower(level, origin, random, placedBlocks, config);
         this.generateSprouts(level, origin, random, placedBlocks, config);
         return true;
      } else {
         return false;
      }
   }

   private void replaceFloor(WorldGenLevel level, BlockPos origin, RandomSource random, List<BlockPos> placedBlocks, MagniaTowerFeature.Config config) {
      float radius = config.floor_replacement_radius().sample(random);

      for (float y = -radius; y <= radius; y++) {
         for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
               BlockPos offset = origin.offset((int)x, (int)y, (int)z);
               float distance = (float)Math.sqrt(x * x + y * y + z * z);
               if (distance <= radius + Mth.nextFloat(random, -2.5F, 0.5F) && level.getBlockState(offset).is(EnderscapeBlockTags.MAGNIA_TOWER_REPLACEABLE)) {
                  level.setBlock(offset, ((Block)EnderscapeBlocks.REPULSIVE_MAGNIA.get()).defaultBlockState(), 2);
                  placedBlocks.add(offset);
               }
            }
         }
      }
   }

   private void generateTower(WorldGenLevel level, BlockPos origin, RandomSource random, List<BlockPos> placedBlocks, MagniaTowerFeature.Config config) {
      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            boolean atEdgeX = x == -1 || x == 1;
            boolean atEdgeZ = z == -1 || z == 1;
            int height = this.getPillarHeight(random, x, z, atEdgeX, atEdgeZ, config);

            for (int y = 0; y < height; y++) {
               BlockPos currentPos = origin.offset(x, y, z);
               if (level.isStateAtPosition(currentPos, DripstoneUtils::isEmptyOrWater)) {
                  level.setBlock(currentPos, ((Block)EnderscapeBlocks.REPULSIVE_MAGNIA.get()).defaultBlockState(), 2);
                  placedBlocks.add(currentPos);
               }

               if (x == 0 && z == 0) {
                  this.generateSphere(level, origin.above(height), placedBlocks, random, config);
               }
            }
         }
      }
   }

   private void generateSphere(WorldGenLevel level, BlockPos origin, List<BlockPos> placedBlocks, RandomSource random, MagniaTowerFeature.Config config) {
      int radius = config.sphere_radius().sample(random);

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            for (int y = -radius; y <= radius; y++) {
               BlockPos spherePos = origin.offset(x, y, z);
               double sizeReduction = y == 2 ? 1.75 : 1.5;
               double threshold = radius * radius / sizeReduction;
               if (x * x + y * y + z * z <= threshold) {
                  level.setBlock(spherePos, ((Block)EnderscapeBlocks.ALLURING_MAGNIA.get()).defaultBlockState(), 2);
                  placedBlocks.add(spherePos);
                  if (y == 0) {
                     int ringRadius = config.ring_radius().sample(random);
                     this.generateRing(level, origin, placedBlocks, ringRadius, 1.5);
                     this.generateRing(level, origin, placedBlocks, (int)(ringRadius * 0.65F), 1.7);
                  }
               }
            }
         }
      }
   }

   private void generateRing(WorldGenLevel level, BlockPos origin, List<BlockPos> placedBlocks, int radius, double thickness) {
      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            BlockPos offset = origin.offset(x, 0, z);
            if (x * x + z * z <= radius * radius
               && !(x * x + z * z < radius * radius / thickness)
               && level.isStateAtPosition(offset, DripstoneUtils::isEmptyOrWater)) {
               level.setBlock(offset, ((Block)EnderscapeBlocks.ALLURING_MAGNIA.get()).defaultBlockState(), 2);
               placedBlocks.add(offset);
            }
         }
      }
   }

   private void generateSprouts(WorldGenLevel level, BlockPos origin, RandomSource random, List<BlockPos> placedBlocks, MagniaTowerFeature.Config config) {
      for (BlockPos pos : placedBlocks) {
         for (Direction direction : Direction.values()) {
            BlockState floor = level.getBlockState(pos);
            if (floor.getBlock() instanceof MagniaBlock) {
               Optional<MagniaType> type = Optional.ofNullable(MagniaBlock.getMagniaType(floor));
               if (!type.isEmpty()) {
                  float chance = type.get() == MagniaType.ALLURING
                     ? config.alluring_magnia_sprout_placement_chance().sample(random)
                     : config.repulsive_magnia_sprout_placement_chance().sample(random);
                  if (random.nextFloat() <= chance) {
                     BlockPos relative = pos.relative(direction);
                     if (level.getBlockState(relative).canBeReplaced() && level.getBlockState(pos.relative(direction, 2)).canBeReplaced()) {
                        double dx = pos.getX() - origin.getX();
                        double dz = pos.getZ() - origin.getZ();
                        boolean nearCenter = dx * dx + dz * dz <= 36.0;
                        if (random.nextInt(200) == 0 && nearCenter) {
                           BlockState state = (BlockState)((Block)ExpansionBlocks.BLISTERED_MAGNIA.get())
                              .defaultBlockState()
                              .setValue(BlisteredMagnia.POLARITY, BlisteredMagnia.select(level, relative));
                           level.setBlock(relative, state, 2);
                           level.scheduleTick(relative, state.getBlock(), 1);
                        } else {
                           Block sprout = type.get() == MagniaType.ALLURING
                              ? (Block)EnderscapeBlocks.ALLURING_MAGNIA_SPROUT.get()
                              : (Block)EnderscapeBlocks.REPULSIVE_MAGNIA_SPROUT.get();
                           level.setBlock(relative, (BlockState)sprout.defaultBlockState().setValue(MagniaSproutBlock.FACING, direction), 2);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean hasSolidBase(WorldGenLevel level, BlockPos origin, int radius) {
      boolean hasSolidGround = false;

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            if (level.getBlockState(origin.offset(x, -1, z)).isAir()) {
               return false;
            }

            hasSolidGround = true;
         }
      }

      return hasSolidGround;
   }

   private int getPillarHeight(RandomSource random, int x, int z, boolean corner, boolean edge, MagniaTowerFeature.Config config) {
      int height = config.pillar_height().sample(random);
      if (height <= config.pillar_height().getMaxValue() - 2 && random.nextInt(10) == 0) {
         height *= 2;
      }

      if (corner && edge) {
         height /= Mth.nextInt(random, 4, 6);
      } else if (x != 0 || z != 0) {
         height /= Mth.nextInt(random, 2, 4);
         if (random.nextBoolean()) {
            height += 3;
         }
      }

      return height;
   }

   public record Config(
      FloatProvider floor_replacement_radius,
      IntProvider pillar_height,
      IntProvider sphere_radius,
      IntProvider ring_radius,
      FloatProvider alluring_magnia_sprout_placement_chance,
      FloatProvider repulsive_magnia_sprout_placement_chance
   ) implements FeatureConfiguration {
      public static final Codec<MagniaTowerFeature.Config> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               FloatProvider.codec(1.0F, 64.0F).fieldOf("floor_replacement_radius").forGetter(config -> config.floor_replacement_radius),
               IntProvider.codec(1, 64).fieldOf("height").forGetter(config -> config.pillar_height),
               IntProvider.codec(0, 64).fieldOf("sphere_radius").forGetter(config -> config.sphere_radius),
               IntProvider.codec(0, 64).fieldOf("ring_radius").forGetter(config -> config.ring_radius),
               FloatProvider.codec(0.0F, 1.0F)
                  .fieldOf("alluring_magnia_sprout_placement_chance")
                  .forGetter(config -> config.alluring_magnia_sprout_placement_chance),
               FloatProvider.codec(0.0F, 1.0F)
                  .fieldOf("repulsive_magnia_sprout_placement_chance")
                  .forGetter(config -> config.repulsive_magnia_sprout_placement_chance)
            )
            .apply(instance, MagniaTowerFeature.Config::new)
      );
   }
}
