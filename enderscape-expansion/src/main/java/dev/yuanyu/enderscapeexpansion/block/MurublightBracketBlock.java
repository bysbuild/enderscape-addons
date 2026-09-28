package dev.yuanyu.enderscapeexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.yuanyu.enderscapeexpansion.ExpansionBlocks;
import java.util.ArrayList;
import java.util.List;
import net.bunten.enderscape.block.DirectionalPlantBlock;
import net.bunten.enderscape.block.properties.DirectionProperties;
import net.bunten.enderscape.util.BlockUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BonemealableBlock.Type;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector2i;

public class MurublightBracketBlock extends DirectionalPlantBlock implements BonemealableBlock {
   public static final MapCodec<MurublightBracketBlock> CODEC = simpleCodec(MurublightBracketBlock::new);
   protected final Vector2i bonemealArea = new Vector2i(1, 1);
   protected final IntProvider bonemealMaxCount = UniformInt.of(1, 2);
   protected final FloatProvider bonemealChance = ConstantFloat.of(1.0F);

   public MurublightBracketBlock(Properties settings) {
      super(DirectionProperties.create().horizontal(), settings);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public MapCodec<MurublightBracketBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public boolean canPlantOn(BlockState state, BlockState floor, BlockGetter level, BlockPos pos, Direction facing) {
      return floor.isFaceSturdy(level, pos, facing);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return BlockUtil.createRotatedShape(1.0, 0.0, 1.0, 15.0, 6.0, 15.0, (Direction)state.getValue(FACING));
   }

   public Type getType() {
      return Type.NEIGHBOR_SPREADER;
   }

   public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
      return !this.findGenerationPositions(level, pos).isEmpty();
   }

   public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
      float chance = this.bonemealChance.sample(random);
      return chance >= 1.0F || random.nextFloat() < chance;
   }

   public void performBonemeal(ServerLevel level, RandomSource random, BlockPos origin, BlockState state) {
      List<BlockPos> positions = this.findGenerationPositions(level, origin);
      Util.shuffle(positions, random);
      int maxCount = this.bonemealMaxCount.sample(random);
      int i = 0;

      for (BlockPos pos : positions) {
         if (i >= maxCount) {
            break;
         }

         if (generate(level, pos, random)) {
            i++;
         }
      }
   }

   private List<BlockPos> findGenerationPositions(LevelReader level, BlockPos origin) {
      List<BlockPos> positions = new ArrayList<>();
      int radius = this.bonemealArea.x;
      int height = this.bonemealArea.y;
      BlockPos.betweenClosed(origin.offset(-radius, -height, -radius), origin.offset(radius, height, radius)).forEach(pos -> {
         if (canGenerate(level, pos, false)) {
            positions.add(pos.immutable());
         }
      });
      return positions;
   }

   private static BlockState stateForGeneration(Direction direction) {
      return (BlockState)((Block)ExpansionBlocks.MURUBLIGHT_BRACKET.get()).defaultBlockState().setValue(FACING, direction);
   }

   public static boolean canGenerate(LevelReader level, BlockPos pos, boolean checkSpace) {
      if (level.isEmptyBlock(pos) && (!checkSpace || hasSpace(level, pos))) {
         for (Direction direction : Plane.HORIZONTAL) {
            if (canGenerateFacing(level, pos, direction)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean hasSpace(LevelReader level, BlockPos pos) {
      return level.isEmptyBlock(pos.above()) && level.isEmptyBlock(pos.below());
   }

   private static boolean canGenerateFacing(LevelReader level, BlockPos pos, Direction direction) {
      return stateForGeneration(direction).canSurvive(level, pos);
   }

   public static boolean generate(LevelAccessor level, BlockPos pos, RandomSource random) {
      if (canGenerate(level, pos, false)) {
         for (Direction direction : Plane.HORIZONTAL.shuffledCopy(random)) {
            if (canGenerateFacing(level, pos, direction)) {
               level.setBlock(pos, stateForGeneration(direction), 2);
               return true;
            }
         }
      }

      return false;
   }
}
