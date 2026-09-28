package dev.yuanyu.enderscapeexpansion;

import dev.yuanyu.enderscapeexpansion.block.MagniaRadio;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RadioBlockEntity extends BlockEntity {
   private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "enderscape");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadioBlockEntity>> TYPE = TYPES.register(
      "magnia_radio", () -> Builder.of(RadioBlockEntity::new, new Block[]{(Block)ExpansionBlocks.MAGNIA_RADIO.get()}).build(null)
   );
   public ResourceLocation current;
   public ResourceLocation last;
   public int elapsed;
   public int delay = -1;
   public static Consumer<RadioBlockEntity> CLIENT_TICK = be -> {};

   public RadioBlockEntity(BlockPos p, BlockState s) {
      super((BlockEntityType)TYPE.get(), p, s);
   }

   public static void register(IEventBus bus) {
      TYPES.register(bus);
   }

   protected void saveAdditional(CompoundTag n, Provider r) {
      super.saveAdditional(n, r);
      if (this.current != null) {
         n.putString("current_song", this.current.toString());
      }

      if (this.last != null) {
         n.putString("last_song", this.last.toString());
      }

      n.putInt("ticks_since_song_started", this.elapsed);
      n.putInt("ticks_until_song", this.delay);
   }

   protected void loadAdditional(CompoundTag n, Provider r) {
      super.loadAdditional(n, r);
      this.current = n.contains("current_song") ? ResourceLocation.tryParse(n.getString("current_song")) : null;
      this.last = n.contains("last_song") ? ResourceLocation.tryParse(n.getString("last_song")) : null;
      this.elapsed = n.getInt("ticks_since_song_started");
      this.delay = n.getInt("ticks_until_song");
   }

   public CompoundTag getUpdateTag(Provider r) {
      return this.saveWithoutMetadata(r);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public RadioSong song() {
      return this.current != null && this.level != null ? (RadioSong)this.level.registryAccess().registryOrThrow(RadioSong.KEY).get(this.current) : null;
   }

   public void stop() {
      this.current = null;
      this.elapsed = 0;
      this.delay = 0;
      this.sync(false);
   }

   private void sync(boolean playing) {
      if (this.level != null) {
         BlockState s = (BlockState)this.getBlockState().setValue(MagniaRadio.PLAYING, playing);
         this.level.setBlockAndUpdate(this.worldPosition, s);
         this.level.sendBlockUpdated(this.worldPosition, s, s, 3);
         this.level.updateNeighborsAt(this.worldPosition, s.getBlock());
         this.setChanged();
      }
   }

   public static void tick(Level l, BlockPos p, BlockState s, RadioBlockEntity be) {
      if (l.isClientSide) {
         CLIENT_TICK.accept(be);
      } else if (!(Boolean)s.getValue(MagniaRadio.ENABLED)) {
         if (be.current != null) {
            be.stop();
         }
      } else if (be.current == null) {
         if (be.delay < 0) {
            be.delay = (3 + l.random.nextInt(4)) * 20;
         }

         if (be.delay-- <= 0) {
            if (nearbyMusic((ServerLevel)l, p)) {
               be.delay = 100;
            } else {
               Registry<RadioSong> registry = l.registryAccess().registryOrThrow(RadioSong.KEY);
               int signal = l.getBestNeighborSignal(p);
               List<Entry<ResourceKey<RadioSong>, RadioSong>> candidates = registry.entrySet()
                  .stream()
                  .filter(
                     e -> {
                        RadioSong song = e.getValue();
                        return (song.biomes().isEmpty() || l.getBiome(p).is(song.biomes().get()))
                           && (signal > 0 ? song.signal().isEmpty() || song.signal().get() == signal : !e.getKey().location().equals(be.last));
                     }
                  )
                  .toList();
               be.current = candidates.isEmpty() ? Expansion.id("fallback") : candidates.get(l.random.nextInt(candidates.size())).getKey().location();
               if (!be.current.equals(Expansion.id("fallback"))) {
                  be.last = be.current;
               }

               be.elapsed = 0;
               be.sync(true);
            }
         }
      } else {
         RadioSong song = be.song();
         if (song == null || ++be.elapsed >= Math.ceil(song.seconds() * 20.0F) + 20.0) {
            be.stop();
            be.delay = (30 + l.random.nextInt(211)) * 20;
         } else if (be.elapsed % 20 == 0) {
            ((ServerLevel)l).sendParticles(ParticleTypes.NOTE, p.getX() + 0.5, p.getY() + 1.2, p.getZ() + 0.5, 0, l.random.nextInt(4) / 24.0, 0.0, 0.0, 1.0);
            l.gameEvent(null, GameEvent.JUKEBOX_PLAY, p);
         }
      }
   }

   private static boolean nearbyMusic(ServerLevel level, BlockPos pos) {
      for (int x = pos.getX() - 36 >> 4; x <= pos.getX() + 36 >> 4; x++) {
         for (int z = pos.getZ() - 36 >> 4; z <= pos.getZ() + 36 >> 4; z++) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
            if (chunk != null) {
               for (BlockEntity be : chunk.getBlockEntities().values()) {
                  if (!be.getBlockPos().equals(pos) && be.getBlockPos().distSqr(pos) <= 1296.0) {
                     if (be instanceof RadioBlockEntity radio && radio.current != null) {
                        return true;
                     }

                     if (be instanceof JukeboxBlockEntity jukebox && jukebox.getSongPlayer().isPlaying()) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }
}
