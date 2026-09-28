package dev.yuanyu.enderscapeexpansion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HavenBlockEntity extends BlockEntity {
   private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "enderscape");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HavenBlockEntity>> TYPE = TYPES.register(
      "end_haven_core", () -> Builder.of(HavenBlockEntity::new, new Block[]{(Block)ExpansionBlocks.END_HAVEN_CORE.get()}).build(null)
   );

   public HavenBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)TYPE.get(), pos, state);
   }

   public static void register(IEventBus bus) {
      TYPES.register(bus);
   }

   public static void sound(Level level, BlockPos pos, String name) {
      level.playSound(null, pos, (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("block.end_haven_core." + name)), SoundSource.BLOCKS, 1.0F, 1.0F);
   }
}
