package dev.yuanyu.enderscapeexpansion;

import com.mojang.logging.LogUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class GuaranteedCityMimicube {
   @SubscribeEvent
   public static void tick(Post event) {
      if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 40 == 0) {
         ServerLevel level = player.serverLevel();
         if (level.dimension().equals(Level.END) && level.getDifficulty() != Difficulty.PEACEFUL) {
            Structure structure = (Structure)level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.parse("enderscape:end_city"));
            if (structure != null && level.hasChunkAt(player.blockPosition())) {
               StructureStart start = level.structureManager().getStructureAt(player.blockPosition(), structure);
               if (start.isValid()) {
                  ensureCity(level, start);
               }
            }
         }
      }
   }

   static void ensureCity(ServerLevel level, StructureStart start) {
      if (level.getDifficulty() != Difficulty.PEACEFUL && start.isValid()) {
         EntityType<?> type = BuiltInRegistries.ENTITY_TYPE
            .getOptional(ResourceLocation.parse("alexsmobs:mimicube"))
            .or(() -> BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse("alexsmobsup:mimicube")))
            .orElse(null);
         if (type != null) {
            GuaranteedCityMimicube.CityRecord record = (GuaranteedCityMimicube.CityRecord)level.getDataStorage()
               .computeIfAbsent(GuaranteedCityMimicube.CityRecord.FACTORY, "enderscape_expansion_city_mimicubes");
            long key = start.getChunkPos().toLong();
            if (!record.cities.contains(key)) {
               if (type.create(level) instanceof Mob mob) {
                  for (StructurePiece piece : start.getPieces()) {
                     BoundingBox box = piece.getBoundingBox();

                     for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                        if (level.hasChunkAt(pos)
                           && level.hasChunkAt(pos.offset(-1, 0, -1))
                           && level.hasChunkAt(pos.offset(1, 0, 1))
                           && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                           && level.getFluidState(pos).isEmpty()
                           && level.getFluidState(pos.above()).isEmpty()) {
                           mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                           if (level.noCollision(mob) && level.isUnobstructed(mob)) {
                              mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
                              mob.setPersistenceRequired();
                              if (level.addFreshEntity(mob)) {
                                 record.cities.add(key);
                                 record.setDirty();
                                 LogUtils.getLogger().info("Guaranteed city Mimicube spawned at {} for city {}", pos, start.getChunkPos());
                              }

                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static final class CityRecord extends SavedData {
      static final Factory<GuaranteedCityMimicube.CityRecord> FACTORY = new Factory(
         GuaranteedCityMimicube.CityRecord::new, GuaranteedCityMimicube.CityRecord::load
      );
      final Set<Long> cities = new HashSet<>();

      static GuaranteedCityMimicube.CityRecord load(CompoundTag tag, Provider provider) {
         GuaranteedCityMimicube.CityRecord record = new GuaranteedCityMimicube.CityRecord();

         for (long key : tag.getLongArray("Cities")) {
            record.cities.add(key);
         }

         return record;
      }

      public CompoundTag save(CompoundTag tag, Provider provider) {
         tag.putLongArray("Cities", this.cities.stream().mapToLong(Long::longValue).toArray());
         return tag;
      }
   }
}
