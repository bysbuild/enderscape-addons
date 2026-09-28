package dev.yuanyu.enderscapeexpansion;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.Items;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public final class CheeseOreCompat {
   public static void register(IEventBus bus) {
      if (ModList.get().isLoaded("alexsmobsdelight")) {
         if (((ModContainer)ModList.get().getModContainerById("alexsmobsdelight").orElseThrow())
               .getModInfo()
               .getVersion()
               .compareTo(new DefaultArtifactVersion("1.1.0"))
            < 0) {
            Blocks blocks = DeferredRegister.createBlocks("alexsmobsdelight");
            Items items = DeferredRegister.createItems("alexsmobsdelight");
            DeferredBlock<Block> ore = blocks.register(
               "end_cheese_ore", () -> new Block(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.END_STONE).requiresCorrectToolForDrops())
            );
            items.registerSimpleBlockItem(ore);
            blocks.register(bus);
            items.register(bus);
         }
      }
   }
}
