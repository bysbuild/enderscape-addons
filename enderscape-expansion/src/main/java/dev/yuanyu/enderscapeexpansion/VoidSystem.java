package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.registry.EnderscapeParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Post;
import net.neoforged.neoforge.fluids.FluidType;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class VoidSystem {
   private static final String DELAY = "enderscape_expansion.void_delay";

   public static boolean tagged(LivingEntity entity, String tag) {
      return entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, Expansion.id(tag)));
   }

   public static DamageSource damage(LivingEntity entity, String name) {
      return new DamageSource(
         entity.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Expansion.id(name)))
      );
   }

   public static void corrupt(LivingEntity entity, int amount) {
      if (!tagged(entity, "void_immune") && !entity.isSpectator() && !(entity instanceof Player player && player.getAbilities().invulnerable)) {
         entity.setData(ExpansionEffects.VOID_TICKS, Math.min(90, (Integer)entity.getData(ExpansionEffects.VOID_TICKS) + amount));
         entity.getPersistentData().putInt("enderscape_expansion.void_delay", 90);
      }
   }

   public static void purify(LivingEntity entity, float amount) {
      entity.setData(ExpansionEffects.VOIDED_HEALTH, Math.max(0.0F, (Float)entity.getData(ExpansionEffects.VOIDED_HEALTH) - amount));
   }

   public static float healthLimit(LivingEntity entity) {
      return Math.max(0.0F, entity.getMaxHealth() - (Float)entity.getData(ExpansionEffects.VOIDED_HEALTH));
   }

   private static boolean isVoid(DamageSource source) {
      return source.is(TagKey.create(Registries.DAMAGE_TYPE, Expansion.id("is_void")));
   }

   @SubscribeEvent
   public static void incoming(LivingIncomingDamageEvent event) {
      if (isVoid(event.getSource())
         && !event.getSource().is(ResourceKey.create(Registries.DAMAGE_TYPE, Expansion.id("outer_void")))
         && (
            tagged(event.getEntity(), "void_immune")
               || event.getEntity().hasEffect(ExpansionEffects.RESISTANCE)
               || event.getEntity().getPersistentData().getInt("enderscape_expansion.void_immunity") > 0
         )) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void convert(Pre event) {
      LivingEntity entity = event.getEntity();
      if (!entity.level().isClientSide() && !tagged(entity, "void_immune")) {
         if (!(entity instanceof Player) || entity.level().getGameRules().getBoolean(ExpansionGameRules.VOID_HEALTH)) {
            if (event.getSource().is(TagKey.create(Registries.DAMAGE_TYPE, Expansion.id("voids_health"))) && event.getNewDamage() > 0.0F) {
               float total = Math.min(entity.getMaxHealth(), (Float)entity.getData(ExpansionEffects.VOIDED_HEALTH) + event.getNewDamage());
               entity.setData(ExpansionEffects.VOIDED_HEALTH, total);
               entity.removeEffect(MobEffects.ABSORPTION);
               if (total < entity.getMaxHealth()) {
                  event.setNewDamage(0.0F);
                  entity.setHealth(Math.min(entity.getHealth(), healthLimit(entity)));
               } else {
                  event.setNewDamage(entity.getHealth());
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void tick(Post event) {
      if (event.getEntity() instanceof LivingEntity entity && entity.isAlive()) {
         if (entity.level().isClientSide()) {
            if (entity.hasData(ExpansionEffects.VOID_TICKS)
               && (Integer)entity.getData(ExpansionEffects.VOID_TICKS) > 0
               && entity.getRandom().nextFloat() < ((Integer)entity.getData(ExpansionEffects.VOID_TICKS)).intValue() / 90.0F) {
               entity.level()
                  .addParticle(
                     (ParticleOptions)EnderscapeParticles.VOID_STARS.get(),
                     entity.getRandomX(0.85),
                     entity.getRandomY() - 0.25,
                     entity.getRandomZ(0.85),
                     0.0,
                     0.02,
                     0.0
                  );
            }
         } else {
            int immunity = entity.getPersistentData().getInt("enderscape_expansion.void_immunity");
            if (immunity > 0) {
               entity.getPersistentData().putInt("enderscape_expansion.void_immunity", immunity - 1);
            }

            int ticks = (Integer)entity.getData(ExpansionEffects.VOID_TICKS);
            int outerDelay = entity.getPersistentData().getInt("enderscape_expansion.outer_delay");
            if (outerDelay > 0) {
               entity.getPersistentData().putInt("enderscape_expansion.outer_delay", outerDelay - 1);
            } else if (entity.hasData(ExpansionEffects.OUTER_VOID) && (Integer)entity.getData(ExpansionEffects.OUTER_VOID) > 0) {
               entity.setData(ExpansionEffects.OUTER_VOID, 0);
            }

            int delay = entity.getPersistentData().getInt("enderscape_expansion.void_delay");
            boolean ambient = entity.level().getBiome(entity.blockPosition()).is(TagKey.create(Registries.BIOME, Expansion.id("ambient_voiding")))
               && !tagged(entity, "ambient_voiding_immune");
            if (ambient && delay <= 1) {
               corrupt(entity, 1);
               entity.getPersistentData().putInt("enderscape_expansion.void_delay", 20);
               delay = 20;
            }

            if (delay > 0) {
               entity.getPersistentData().putInt("enderscape_expansion.void_delay", delay - 1);
            } else if (ticks > 0) {
               entity.setData(ExpansionEffects.VOID_TICKS, ticks - 1);
            }

            if (ticks >= 90) {
               int amp = entity.hasEffect(ExpansionEffects.CORRUPTION) ? entity.getEffect(ExpansionEffects.CORRUPTION).getAmplifier() : 0;
               boolean fluid = entity.isInFluidType((FluidType)VoidFluid.TYPE.get())
                  || entity.level().getBlockState(entity.blockPosition()).is((Block)ExpansionBlocks.VOID_CAULDRON.get());
               int interval = fluid ? 10 : (entity.hasEffect(ExpansionEffects.CORRUPTION) ? Math.max(10, 40 >> Math.min(amp, 3)) : (ambient ? 100 : 40));
               interval = Math.max(10, interval - (int)(entity.getPercentFrozen() * 20.0F));
               if (entity.tickCount % interval == 0) {
                  entity.hurt(damage(entity, fluid ? "void_lachryma" : "void"), 1.0F);
               }
            }

            if (entity.level().dimension() == Level.OVERWORLD
               && entity.level().isDay()
               && entity.tickCount % 80 == 0
               && (Float)entity.getData(ExpansionEffects.VOIDED_HEALTH) > 0.0F
               && entity.level().canSeeSky(entity.blockPosition())
               && !entity.level().isRainingAt(entity.blockPosition())) {
               entity.addEffect(new MobEffectInstance(ExpansionEffects.PURIFICATION, 220, 0, true, false, true));
            }

            if (entity.getHealth() > healthLimit(entity)) {
               entity.setHealth(healthLimit(entity));
            }
         }
      }
   }
}
