package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.registry.EnderscapeBlocks;
import net.bunten.enderscape.registry.EnderscapeParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidType.Properties;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.Items;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;

public final class VoidFluid {
   private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(Keys.FLUID_TYPES, "enderscape");
   private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, "enderscape");
   private static final Blocks BLOCKS = DeferredRegister.createBlocks("enderscape");
   private static final Items ITEMS = DeferredRegister.createItems("enderscape");
   public static final DeferredHolder<FluidType, FluidType> TYPE = TYPES.register(
      "void_lachryma",
      () -> new FluidType(Properties.create().lightLevel(8).density(1000).viscosity(1000).canExtinguish(true).canConvertToSource(false).canHydrate(false)) {
         public boolean canConvertToSource(FluidState state, LevelReader reader, BlockPos pos) {
            return reader instanceof Level level && level.getGameRules().getBoolean(ExpansionGameRules.VOID_SOURCES);
         }
      }
   );
   public static final DeferredHolder<Fluid, FlowingFluid> SOURCE = FLUIDS.register("void_lachryma", VoidFluid.Source::new);
   public static final DeferredHolder<Fluid, FlowingFluid> FLOWING = FLUIDS.register("flowing_void_lachryma", VoidFluid.Flowing::new);
   public static final DeferredBlock<LiquidBlock> BLOCK = BLOCKS.register(
      "void_lachryma",
      () -> new LiquidBlock(
         (FlowingFluid)SOURCE.get(),
         net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER).lightLevel(state -> 8)
      )
   );
   public static final DeferredItem<BucketItem> BUCKET = ITEMS.register(
      "void_lachryma_bucket",
      () -> new BucketItem(
         (Fluid)SOURCE.get(), new net.minecraft.world.item.Item.Properties().craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)
      )
   );

   private static net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties properties() {
      return new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(TYPE, SOURCE, FLOWING)
         .block(BLOCK)
         .bucket(BUCKET)
         .tickRate(10)
         .slopeFindDistance(4)
         .levelDecreasePerBlock(2)
         .explosionResistance(100.0F);
   }

   private static void react(Level level, BlockPos pos) {
      for (Direction direction : Direction.values()) {
         BlockPos other = pos.relative(direction);
         FluidState fluid = level.getFluidState(other);
         if (fluid.is(FluidTags.WATER)) {
            level.setBlockAndUpdate(other, net.minecraft.world.level.block.Blocks.PACKED_ICE.defaultBlockState());
            level.playSound(
               null, other, (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("block.void_lachryma.freeze_water")), SoundSource.BLOCKS, 0.75F, 1.0F
            );
            if (level instanceof ServerLevel server) {
               server.sendParticles(
                  (SimpleParticleType)ExpansionParticles.SNOWFLAKE.get(), other.getX() + 0.5, other.getY() + 1.2, other.getZ() + 0.5, 8, 0.5, 0.0, 0.5, 0.0
               );
            }
         } else if (fluid.is(FluidTags.LAVA)) {
            level.setBlockAndUpdate(
               other, (fluid.isSource() ? net.minecraft.world.level.block.Blocks.OBSIDIAN : net.minecraft.world.level.block.Blocks.BASALT).defaultBlockState()
            );
         }
      }

      if (level.random.nextInt(20) == 0) {
         BlockState ground = level.getBlockState(pos.below());
         if (ground.is(net.minecraft.world.level.block.Blocks.END_STONE) || ground.is((Block)EnderscapeBlocks.MIRESTONE.get())) {
            level.setBlockAndUpdate(pos.below(), ((Block)EnderscapeBlocks.VOID_SHALE.get()).defaultBlockState());
         }
      }
   }

   private static void ambient(Level level, BlockPos pos, FluidState fluid, RandomSource random) {
      if (level.getBlockState(pos.above()).isAir()) {
         if (random.nextInt(4) == 0) {
            level.addParticle(
               (ParticleOptions)EnderscapeParticles.VOID_STARS.get(),
               pos.getX() + random.nextDouble(),
               pos.getY() + fluid.getHeight(level, pos) + 0.05,
               pos.getZ() + random.nextDouble(),
               0.0,
               0.015,
               0.0
            );
         }

         if (random.nextInt(300) == 0) {
            level.playLocalSound(
               pos.getX(),
               pos.getY(),
               pos.getZ(),
               (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("block.void_lachryma.ambient")),
               SoundSource.AMBIENT,
               0.4F + random.nextFloat() * 0.4F,
               0.8F + random.nextFloat() * 0.4F,
               false
            );
         }
      }
   }

   private static void erode(ServerLevel level, BlockPos pos) {
      if (level.getBlockState(pos.below()).is(TagKey.create(Registries.BLOCK, Expansion.id("base_stone_end")))) {
         level.setBlockAndUpdate(pos.below(), ((Block)EnderscapeBlocks.VOID_SHALE.get()).defaultBlockState());
      }
   }

   public static void register(IEventBus bus) {
      TYPES.register(bus);
      FLUIDS.register(bus);
      BLOCKS.register(bus);
      ITEMS.register(bus);
   }

   private static final class Flowing extends net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing {
      Flowing() {
         super(VoidFluid.properties());
      }

      public boolean isRandomlyTicking() {
         return true;
      }

      public void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
         if (level instanceof ServerLevel server) {
            VoidFluid.erode(server, pos);
         }
      }

      public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
         VoidFluid.ambient(level, pos, state, random);
      }

      public ParticleOptions getDripParticle() {
         return (ParticleOptions)ExpansionParticles.DRIPPING_VOID.get();
      }

      public void tick(Level level, BlockPos pos, FluidState state) {
         VoidFluid.react(level, pos);
         super.tick(level, pos, state);
      }
   }

   private static final class Source extends net.neoforged.neoforge.fluids.BaseFlowingFluid.Source {
      Source() {
         super(VoidFluid.properties());
      }

      public boolean isRandomlyTicking() {
         return true;
      }

      public void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
         if (level instanceof ServerLevel server) {
            VoidFluid.erode(server, pos);
         }
      }

      public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
         VoidFluid.ambient(level, pos, state, random);
      }

      public ParticleOptions getDripParticle() {
         return (ParticleOptions)ExpansionParticles.DRIPPING_VOID.get();
      }

      public void tick(Level level, BlockPos pos, FluidState state) {
         VoidFluid.react(level, pos);
         super.tick(level, pos, state);
      }
   }
}
