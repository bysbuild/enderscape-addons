package dev.yuanyu.enderscapeexpansion;

import dev.yuanyu.enderscapeexpansion.block.BlisteredMagnia;
import dev.yuanyu.enderscapeexpansion.block.EndHavenCore;
import dev.yuanyu.enderscapeexpansion.block.MagniaRadio;
import dev.yuanyu.enderscapeexpansion.block.MurublightBracketBlock;
import dev.yuanyu.enderscapeexpansion.block.PolarizedMagnia;
import dev.yuanyu.enderscapeexpansion.block.PuruberryFlowerBlock;
import dev.yuanyu.enderscapeexpansion.block.PuruberryVine;
import dev.yuanyu.enderscapeexpansion.block.RipePuruberryBlock;
import dev.yuanyu.enderscapeexpansion.block.StorageShelf;
import dev.yuanyu.enderscapeexpansion.block.UnripePuruberryBlock;
import dev.yuanyu.enderscapeexpansion.block.VoidCampfireBlock;
import dev.yuanyu.enderscapeexpansion.block.VoidCauldron;
import dev.yuanyu.enderscapeexpansion.block.VoidFireBlock;
import dev.yuanyu.enderscapeexpansion.block.VoidLightBlocks;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.bunten.enderscape.block.CelestialPathBlock;
import net.bunten.enderscape.block.CorruptPathBlock;
import net.bunten.enderscape.registry.EnderscapeBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public final class ExpansionBlocks {
   private static final Blocks BLOCKS = DeferredRegister.createBlocks("enderscape");
   private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "enderscape");
   public static final Map<String, DeferredBlock<? extends Block>> ADDED = new LinkedHashMap<>();
   public static final DeferredBlock<Block> WILDFLOWERS = add(
      "wildflowers", () -> new PinkPetalsBlock(copy(net.minecraft.world.level.block.Blocks.PINK_PETALS))
   );
   public static final DeferredBlock<Block> CELESTIAL_SHELF = add(
      "celestial_shelf", () -> new StorageShelf(copy(net.minecraft.world.level.block.Blocks.CHISELED_BOOKSHELF).noOcclusion())
   );
   public static final DeferredBlock<Block> VEILED_SHELF = add(
      "veiled_shelf", () -> new StorageShelf(copy(net.minecraft.world.level.block.Blocks.CHISELED_BOOKSHELF).noOcclusion())
   );
   public static final DeferredBlock<Block> MURUBLIGHT_STORAGE_SHELF = add(
      "murublight_storage_shelf", () -> new StorageShelf(copy(net.minecraft.world.level.block.Blocks.CHISELED_BOOKSHELF).noOcclusion())
   );
   public static final DeferredBlock<Block> MAGNIA_RADIO = add(
      "magnia_radio", () -> new MagniaRadio(copy(net.minecraft.world.level.block.Blocks.JUKEBOX).noOcclusion())
   );
   public static final DeferredBlock<Block> VOID_CAULDRON = add(
      "void_lachryma_cauldron", () -> new VoidCauldron(copy(net.minecraft.world.level.block.Blocks.CAULDRON).lightLevel(s -> 8)), false
   );
   public static final DeferredBlock<Block> MURUBLIGHT_BRACKET = add(
      "murublight_bracket", () -> new MurublightBracketBlock(copy((Block)EnderscapeBlocks.MURUBLIGHT_SHELF.get()))
   );
   public static final DeferredBlock<Block> POLARIZED_MAGNIA = add(
      "polarized_magnia", () -> new PolarizedMagnia(copy((Block)EnderscapeBlocks.ALLURING_MAGNIA.get()).lightLevel(s -> 14))
   );
   public static final DeferredBlock<Block> BLISTERED_MAGNIA = add(
      "blistered_magnia",
      () -> new BlisteredMagnia(
         copy((Block)EnderscapeBlocks.ALLURING_MAGNIA.get()).lightLevel(s -> s.getValue(BlisteredMagnia.POLARITY) == BlisteredMagnia.Polarity.NONE ? 0 : 14)
      )
   );
   public static final DeferredBlock<Block> END_HAVEN_CORE = add(
      "end_haven_core",
      () -> new EndHavenCore(
         copy(net.minecraft.world.level.block.Blocks.RESPAWN_ANCHOR)
            .strength(50.0F, 150.0F)
            .noOcclusion()
            .lightLevel(s -> s.getValue(EndHavenCore.STATE) == EndHavenCore.State.ACTIVE ? 8 : 0)
      )
   );
   public static final DeferredBlock<Block> VOID_TORCH = add(
      "void_torch", () -> new VoidLightBlocks.Standing(copy(net.minecraft.world.level.block.Blocks.SOUL_TORCH).lightLevel(s -> 8)), false
   );
   public static final DeferredBlock<Block> VOID_WALL_TORCH = add(
      "void_wall_torch", () -> new VoidLightBlocks.Wall(copy(net.minecraft.world.level.block.Blocks.SOUL_WALL_TORCH).lightLevel(s -> 8)), false
   );
   public static final DeferredBlock<Block> VOID_LANTERN = add(
      "void_lantern", () -> new LanternBlock(copy(net.minecraft.world.level.block.Blocks.SOUL_LANTERN).lightLevel(s -> 8))
   );
   public static final DeferredBlock<Block> VOID_CAMPFIRE = add(
      "void_campfire",
      () -> new VoidCampfireBlock(copy(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE).lightLevel(s -> s.getValue(CampfireBlock.LIT) ? 8 : 0))
   );
   public static final DeferredBlock<Block> VOID_FIRE = add(
      "void_fire", () -> new VoidFireBlock(copy(net.minecraft.world.level.block.Blocks.SOUL_FIRE).lightLevel(s -> 8)), false
   );
   public static final DeferredBlock<Block> CRACKED_END_STONE_BRICKS = add(
      "cracked_end_stone_bricks", () -> new Block(copy(net.minecraft.world.level.block.Blocks.END_STONE_BRICKS))
   );
   public static final DeferredBlock<Block> CRACKED_MIRESTONE_BRICKS = add(
      "cracked_mirestone_bricks", () -> new Block(copy((Block)EnderscapeBlocks.MIRESTONE_BRICKS.get()))
   );
   public static final DeferredBlock<Block> OVERGROWN_END_STONE_BRICKS = add(
      "overgrown_end_stone_bricks", () -> new Block(copy(net.minecraft.world.level.block.Blocks.END_STONE_BRICKS))
   );
   public static final DeferredBlock<Block> OVERGROWN_MIRESTONE_BRICKS = add(
      "overgrown_mirestone_bricks", () -> new Block(copy((Block)EnderscapeBlocks.MIRESTONE_BRICKS.get()))
   );
   public static final DeferredBlock<Block> SHADOLINE_BARS = add(
      "shadoline_bars", () -> new IronBarsBlock(copy(net.minecraft.world.level.block.Blocks.IRON_BARS).strength(6.0F, 9.0F))
   );
   public static final DeferredBlock<Block> SHADOLINE_CHAIN = add(
      "shadoline_chain", () -> new ChainBlock(copy(net.minecraft.world.level.block.Blocks.CHAIN).strength(5.0F, 6.0F))
   );
   public static final DeferredBlock<Block> CELESTIAL_PATH = add(
      "celestial_path", () -> new CelestialPathBlock(copy((Block)EnderscapeBlocks.CELESTIAL_PATH_BLOCK.get()))
   );
   public static final DeferredBlock<Block> CORRUPT_PATH = add(
      "corrupt_path", () -> new CorruptPathBlock(copy((Block)EnderscapeBlocks.CORRUPT_PATH_BLOCK.get()))
   );
   public static final DeferredBlock<Block> PURUBERRY_VINE = add(
      "puruberry_vine", () -> new PuruberryVine(Properties.of().randomTicks().noCollission().sound(SoundType.CAVE_VINES).strength(0.2F)), false
   );
   public static final DeferredBlock<Block> PURUBERRY_FLOWER = add(
      "puruberry_flower", () -> new PuruberryFlowerBlock(Properties.of().randomTicks().noOcclusion().sound(SoundType.AZALEA).strength(0.3F))
   );
   public static final DeferredBlock<Block> UNRIPE_PURUBERRY_BLOCK = add(
      "unripe_puruberry_block", () -> new UnripePuruberryBlock(Properties.of().randomTicks().noOcclusion().sound(SoundType.WART_BLOCK).strength(0.3F))
   );
   public static final DeferredBlock<Block> RIPE_PURUBERRY_BLOCK = add(
      "ripe_puruberry_block", () -> new RipePuruberryBlock(Properties.of().sound(SoundType.WART_BLOCK).strength(0.3F))
   );

   private static Properties copy(Block block) {
      return Properties.ofFullCopy(block);
   }

   private static void family(String name, Supplier<? extends Block> base) {
      add(name + "_slab", () -> new SlabBlock(copy(base.get())));
      add(name + "_stairs", () -> new StairBlock(base.get().defaultBlockState(), copy(base.get())));
      add(name + "_wall", () -> new WallBlock(copy(base.get())));
   }

   public static DeferredBlock<Block> add(String name, Supplier<? extends Block> factory) {
      return add(name, factory, true);
   }

   public static DeferredBlock<Block> add(String name, Supplier<? extends Block> factory, boolean item) {
      DeferredBlock<Block> block = BLOCKS.register(name, factory);
      if (ADDED.putIfAbsent(name, block) != null) {
         throw new IllegalStateException("Duplicate block " + name);
      } else {
         if (item) {
            ITEMS.register(name, () -> new BlockItem((Block)block.get(), new net.minecraft.world.item.Item.Properties()));
         }

         return block;
      }
   }

   public static void register(IEventBus bus) {
      BLOCKS.register(bus);
      ITEMS.register(bus);
   }

   static {
      family("overgrown_end_stone_brick", OVERGROWN_END_STONE_BRICKS);
      family("overgrown_mirestone_brick", OVERGROWN_MIRESTONE_BRICKS);
      family("etched_alluring_magnia", EnderscapeBlocks.ETCHED_ALLURING_MAGNIA);
      family("etched_repulsive_magnia", EnderscapeBlocks.ETCHED_REPULSIVE_MAGNIA);
   }
}
