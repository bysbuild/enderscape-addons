package dev.yuanyu.enderscapeexpansion;

import java.util.List;
import java.util.Locale;
import net.bunten.enderscape.block.BulbFlowerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;

public final class PurifyingPlants {
   public static final EnumProperty<PurifyingPlants.Phase> PHASE = EnumProperty.create("phase", PurifyingPlants.Phase.class);

   public static boolean flower(BlockState state) {
      return state.getBlock() instanceof BulbFlowerBlock;
   }

   public static PurifyingPlants.Phase dormant(Level level, BlockPos pos, BlockState state) {
      return flower(state) && !level.getBlockState(pos.below()).is(TagKey.create(Registries.BLOCK, Expansion.id("powers_bulb_flower")))
         ? PurifyingPlants.Phase.POWERLESS
         : PurifyingPlants.Phase.INACTIVE;
   }

   public static void tick(ServerLevel level, BlockPos pos, BlockState state, boolean random) {
      PurifyingPlants.Phase phase = (PurifyingPlants.Phase)state.getValue(PHASE);
      String sound = null;
      boolean flower = flower(state);
      PurifyingPlants.Phase next;
      int delay;
      if (phase == PurifyingPlants.Phase.INACTIVE && random && flower) {
         next = PurifyingPlants.Phase.CHARGING;
         delay = 40;
         sound = "charge";
      } else if (phase == PurifyingPlants.Phase.CHARGING || phase == PurifyingPlants.Phase.INACTIVE && random) {
         next = PurifyingPlants.Phase.ACTIVE;
         delay = flower ? 160 : 320;
         sound = flower ? "activate" : null;
         AABB area = new AABB(pos).inflate(flower ? 4.0 : 6.0, flower ? 2.0 : 6.0, flower ? 4.0 : 6.0);
         List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && !e.isSpectator() && !VoidSystem.tagged(e, "void"));
         if (!flower && entities.isEmpty()) {
            next = PurifyingPlants.Phase.COOLDOWN;
            delay = 120;
         } else {
            for (LivingEntity entity : entities) {
               if (flower || (Float)entity.getData(ExpansionEffects.VOIDED_HEALTH) > 0.0F) {
                  entity.addEffect(new MobEffectInstance(ExpansionEffects.PURIFICATION, delay, 0, true, false, true));
               }
            }
         }
      } else if (phase == PurifyingPlants.Phase.ACTIVE) {
         next = PurifyingPlants.Phase.COOLDOWN;
         delay = 120;
         sound = flower ? "deactivate" : null;
      } else {
         if (phase != PurifyingPlants.Phase.COOLDOWN) {
            return;
         }

         next = dormant(level, pos, state);
         delay = 0;
      }

      level.setBlock(pos, (BlockState)state.setValue(PHASE, next), 3);
      if (delay > 0) {
         level.scheduleTick(pos, state.getBlock(), delay);
      }

      if (sound != null) {
         level.playSound(null, pos, (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("block.bulb_flower." + sound)), SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   public static void animate(BlockState state, Level level, BlockPos pos, RandomSource random) {
      PurifyingPlants.Phase phase = (PurifyingPlants.Phase)state.getValue(PHASE);
      if (phase == PurifyingPlants.Phase.ACTIVE || phase == PurifyingPlants.Phase.CHARGING) {
         if (phase != PurifyingPlants.Phase.ACTIVE || random.nextInt(4) == 0) {
            double x = (random.nextDouble() - 0.5) * (phase == PurifyingPlants.Phase.CHARGING ? 1.6 : 0.4);
            double z = (random.nextDouble() - 0.5) * (phase == PurifyingPlants.Phase.CHARGING ? 1.6 : 0.4);
            level.addParticle(
               phase == PurifyingPlants.Phase.CHARGING ? GlowParticleOptions.CHARGING : GlowParticleOptions.DEFAULT,
               pos.getX() + 0.5 + x,
               pos.getY() + (flower(state) ? 1.1 : 0.3),
               pos.getZ() + 0.5 + z,
               phase == PurifyingPlants.Phase.CHARGING ? -x * 0.35 : 0.0,
               phase == PurifyingPlants.Phase.CHARGING ? -0.125 : 0.06,
               phase == PurifyingPlants.Phase.CHARGING ? -z * 0.35 : 0.0
            );
         }
      }
   }

   public static enum Phase implements StringRepresentable {
      POWERLESS,
      INACTIVE,
      CHARGING,
      ACTIVE,
      COOLDOWN;

      public String getSerializedName() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }
}
