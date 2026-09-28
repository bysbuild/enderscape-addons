package dev.yuanyu.enderscapeexpansion;

import java.util.Optional;
import net.bunten.enderscape.entity.rustle.Rustle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Post;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class RustleProcessing {
   private static final String KEY = "enderscape_expansion.processing";

   public static boolean busy(Rustle mob) {
      return mob.getPersistentData().contains("enderscape_expansion.processing");
   }

   public static boolean begin(Rustle mob, Player player, ItemStack stack) {
      if (!mob.isBaby() && !mob.isSleeping() && mob.isAlive() && !busy(mob)) {
         Optional<RecipeHolder<ConversionRecipe>> match = mob.level()
            .getRecipeManager()
            .getRecipeFor((RecipeType)ConversionRecipe.RUSTLE_TYPE.get(), new SingleRecipeInput(stack), mob.level());
         if (match.isEmpty()) {
            return false;
         } else {
            if (mob.level() instanceof ServerLevel server) {
               CompoundTag tag = new CompoundTag();
               tag.put("input", stack.copyWithCount(1).save(server.registryAccess()));
               tag.putString("recipe", match.get().id().toString());
               mob.getPersistentData().put("enderscape_expansion.processing", tag);
               stack.shrink(1);
               mob.getNavigation().stop();
               mob.lookAt(player, 180.0F, 180.0F);
               sound(mob, Expansion.id("entity.rustle.swallow"), 1.0F);
               particles(server, mob, Expansion.id("rustle_converting"));
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static ItemStack takeInput(Rustle mob) {
      CompoundTag data = mob.getPersistentData().getCompound("enderscape_expansion.processing");
      ItemStack input = data.contains("input") ? ItemStack.parseOptional(mob.registryAccess(), data.getCompound("input")) : ItemStack.EMPTY;
      mob.getPersistentData().remove("enderscape_expansion.processing");
      mob.setData(ExpansionEffects.RUSTLE_CONVERSION, 0);
      return input;
   }

   public static void interrupt(Rustle mob) {
      if (busy(mob)) {
         ItemStack input = takeInput(mob);
         if (!input.isEmpty()) {
            mob.spawnAtLocation(input);
         }
      }
   }

   @SubscribeEvent
   public static void tick(Post event) {
      if (event.getEntity() instanceof Rustle mob && mob.level() instanceof ServerLevel server && busy(mob) && mob.isAlive()) {
         CompoundTag tag = mob.getPersistentData().getCompound("enderscape_expansion.processing");
         Optional<RecipeHolder<?>> holder = server.getRecipeManager().byKey(ResourceLocation.parse(tag.getString("recipe")));
         if (!holder.isEmpty() && holder.get().value() instanceof ConversionRecipe recipe && recipe.rustle()) {
            int elapsed = tag.getInt("elapsed") + 1;
            tag.putInt("elapsed", elapsed);
            int spitAt = 10 + recipe.effects().duration();
            mob.getNavigation().stop();
            mob.setData(ExpansionEffects.RUSTLE_CONVERSION, spitAt << 16 | elapsed);
            if (elapsed == 10) {
               sound(mob, recipe.effects().swell(), recipe.effects().pitch());
            }

            if (elapsed >= spitAt && tag.contains("input")) {
               tag.remove("input");
               ItemStack output = recipe.result().copy();
               Vec3 pos = mob.getEyePosition().add(0.0, 0.4, 0.0).add(mob.getLookAngle().scale(0.125));
               ItemEntity entity = new ItemEntity(server, pos.x, pos.y, pos.z, output);
               entity.setDeltaMovement(mob.getLookAngle().multiply(0.4, 0.3, 0.4).add(0.0, 0.1, 0.0));
               entity.setDefaultPickUpDelay();
               server.addFreshEntity(entity);
               Rustle.regrowHairWithParticles(server, mob);
               sound(mob, recipe.effects().spit(), 1.0F);
               particles(server, mob, recipe.effects().particle());
               int xp = (int)recipe.experience();
               if (server.random.nextFloat() < recipe.experience() - xp) {
                  xp++;
               }

               ExperienceOrb.award(server, pos, xp);
            }

            if (elapsed >= spitAt + 14) {
               mob.getPersistentData().remove("enderscape_expansion.processing");
               mob.setData(ExpansionEffects.RUSTLE_CONVERSION, 0);
            }
         } else {
            interrupt(mob);
         }
      }
   }

   @SubscribeEvent
   public static void drops(LivingDropsEvent event) {
      if (event.getEntity() instanceof Rustle mob && busy(mob)) {
         ItemStack input = takeInput(mob);
         if (!input.isEmpty()) {
            event.getDrops().add(new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), input));
         }
      }
   }

   private static void sound(Rustle mob, ResourceLocation id, float pitch) {
      SoundEvent sound = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(id);
      if (sound != null) {
         mob.playSound(sound, 1.0F, pitch);
      }
   }

   private static void particles(ServerLevel level, Rustle mob, ResourceLocation id) {
      ParticleType<?> type = (ParticleType<?>)BuiltInRegistries.PARTICLE_TYPE.get(id);
      if (type instanceof SimpleParticleType simple) {
         level.sendParticles(simple, mob.getX(), mob.getEyeY() + 0.4, mob.getZ(), 4, 0.2, 0.2, 0.2, 0.2);
      }
   }
}
