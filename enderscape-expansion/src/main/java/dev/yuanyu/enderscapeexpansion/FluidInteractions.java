package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.registry.EnderscapeParticles;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Post;
import net.neoforged.neoforge.fluids.FluidType;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class FluidInteractions {
   @SubscribeEvent
   public static void tick(Post event) {
      Entity entity = event.getEntity();
      if (!entity.isInFluidType((FluidType)VoidFluid.TYPE.get())) {
         entity.getPersistentData().remove("enderscape_expansion.in_void");
      } else {
         if (entity.level() instanceof ServerLevel server && !entity.getPersistentData().getBoolean("enderscape_expansion.in_void")) {
            entity.getPersistentData().putBoolean("enderscape_expansion.in_void", true);
            Vec3 movement = entity.getDeltaMovement();
            float speed = Math.min(1.0F, (float)Math.sqrt(movement.x * movement.x * 0.2 + movement.y * movement.y + movement.z * movement.z * 0.2) * 0.2F);
            entity.playSound(
               (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id(speed < 0.25 ? "entity.generic.void_splash" : "entity.generic.heavy_void_splash")),
               speed,
               1.0F
            );
            server.sendParticles(
               (SimpleParticleType)ExpansionParticles.VOID_SPLASH.get(),
               entity.getX(),
               Math.floor(entity.getY()) + 1.0,
               entity.getZ(),
               (int)(1.0F + entity.getBbWidth() * 20.0F),
               entity.getBbWidth(),
               0.0,
               entity.getBbWidth(),
               0.02
            );
         }

         expose(entity);
      }
   }

   public static void expose(Entity entity) {
      if (entity.level() instanceof ServerLevel level) {
         String var12 = "enderscape_expansion.fluid_exposure_time";
         if (!entity.getPersistentData().contains(var12) || entity.getPersistentData().getLong(var12) != level.getGameTime()) {
            entity.getPersistentData().putLong(var12, level.getGameTime());
            entity.clearFire();
            if (entity instanceof LivingEntity living) {
               VoidSystem.corrupt(living, 4);
            } else if (entity instanceof ItemEntity item) {
               ItemStack stack = item.getItem();
               if (stack.is(Items.SHULKER_SHELL)
                  || stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock
                  || stack.is(TagKey.create(Registries.ITEM, Expansion.id("void_immune")))) {
                  return;
               }

               int ticks = item.getPersistentData().getInt("enderscape_expansion.void_ticks") + 1;
               item.getPersistentData().putInt("enderscape_expansion.void_ticks", ticks);
               boolean hasRecipe = false;

               for (RecipeHolder<ConversionRecipe> holder : level.getRecipeManager().getAllRecipesFor((RecipeType)ConversionRecipe.VOID_TYPE.get())) {
                  ConversionRecipe recipe = (ConversionRecipe)holder.value();
                  if (recipe.input().test(stack)) {
                     hasRecipe = true;
                     if (Math.min(1.0, ticks / 30.0) >= recipe.minimumVoided() && level.random.nextFloat() < recipe.chance() / 20.0F) {
                        ItemStack result = recipe.result().copyWithCount(stack.getCount());
                        result.applyComponents(stack.getComponentsPatch());
                        item.setItem(result);
                        level.sendParticles(
                           (SimpleParticleType)EnderscapeParticles.VOID_POOF.get(), item.getX(), item.getY() + 0.5, item.getZ(), 4, 0.0, 0.0, 0.0, 0.0
                        );
                        return;
                     }
                  }
               }

               if (!hasRecipe && ticks >= 1230 && level.random.nextFloat() < 0.125F) {
                  level.sendParticles(
                     (SimpleParticleType)EnderscapeParticles.VOID_POOF.get(), item.getX(), item.getY() + 0.5, item.getZ(), 8, 0.0, 0.0, 0.0, 0.0
                  );
                  item.discard();
               }
            }
         }
      }
   }
}
