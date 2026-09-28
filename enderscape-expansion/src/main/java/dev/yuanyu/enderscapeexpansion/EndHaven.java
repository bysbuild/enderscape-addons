package dev.yuanyu.enderscapeexpansion;

import dev.yuanyu.enderscapeexpansion.block.EndHavenCore;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class EndHaven {
   public static final String REQUEST = "enderscape_expansion.haven_requested";

   public static void bind(ServerPlayer player, BlockPos pos) {
      CompoundTag tag = new CompoundTag();
      tag.putLong("position", pos.asLong());
      player.setData(ExpansionEffects.HAVEN, tag);
   }

   public static Optional<Vec3> destination(ServerLevel end, CompoundTag tag) {
      if (!tag.contains("position")) {
         return Optional.empty();
      } else {
         BlockPos core = BlockPos.of(tag.getLong("position"));
         if (!end.getWorldBorder().isWithinBounds(core)) {
            return Optional.empty();
         } else {
            BlockState state = end.getBlockState(core);
            if (state.getBlock() instanceof EndHavenCore && state.getValue(EndHavenCore.STATE) == EndHavenCore.State.ACTIVE) {
               for (int radius = 0; radius <= 2; radius++) {
                  for (int dx = -radius; dx <= radius; dx++) {
                     for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                           for (int dy = 1; dy >= 0; dy--) {
                              BlockPos feet = core.offset(dx, dy, dz);
                              Vec3 pos = Vec3.atBottomCenterOf(feet);
                              AABB box = new AABB(pos.x - 0.3, pos.y, pos.z - 0.3, pos.x + 0.3, pos.y + 1.8, pos.z + 0.3);
                              if (end.getBlockState(feet.below()).isFaceSturdy(end, feet.below(), Direction.UP)
                                 && end.getFluidState(feet).isEmpty()
                                 && end.getFluidState(feet.above()).isEmpty()
                                 && end.noCollision(box)) {
                                 return Optional.of(pos);
                              }
                           }
                        }
                     }
                  }
               }

               return Optional.empty();
            } else {
               return Optional.empty();
            }
         }
      }
   }

   @SubscribeEvent
   public static void respawn(PlayerRespawnPositionEvent event) {
      Player player = event.getEntity();
      boolean requested = player.getPersistentData().getBoolean("enderscape_expansion.haven_requested");
      player.getPersistentData().remove("enderscape_expansion.haven_requested");
      if (requested && !event.isFromEndFight() && player.level().dimension() == Level.END) {
         ServerLevel end = player.getServer().getLevel(Level.END);
         Optional<Vec3> destination = destination(end, (CompoundTag)player.getData(ExpansionEffects.HAVEN));
         if (!destination.isEmpty()) {
            event.setDimensionTransition(new DimensionTransition(end, destination.get(), Vec3.ZERO, player.getYRot(), 0.0F, DimensionTransition.DO_NOTHING));
            event.setCopyOriginalSpawnPosition(true);
         }
      }
   }

   @SubscribeEvent
   public static void leave(PlayerChangedDimensionEvent event) {
      if (event.getFrom() == Level.END && event.getTo() != Level.END) {
         event.getEntity().setData(ExpansionEffects.HAVEN, new CompoundTag());
      }
   }

   @SubscribeEvent
   public static void respawned(PlayerRespawnEvent event) {
      if (event.getEntity().level().dimension() != Level.END) {
         event.getEntity().setData(ExpansionEffects.HAVEN, new CompoundTag());
      }
   }
}
