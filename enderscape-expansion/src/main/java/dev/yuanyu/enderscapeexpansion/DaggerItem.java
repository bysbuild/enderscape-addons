package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.item.NebuliteToolContext;
import net.bunten.enderscape.item.NebuliteToolItem;
import net.bunten.enderscape.registry.EnderscapeParticles;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class DaggerItem extends NebuliteToolItem {
   public DaggerItem(Properties properties) {
      super(properties);
   }

   public boolean displayHudWhen(NebuliteToolContext context) {
      return false;
   }

   public int getEnchantmentValue() {
      return 15;
   }

   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   public static int enchantment(ItemStack stack, Level level, String path) {
      return level.registryAccess()
         .lookupOrThrow(Registries.ENCHANTMENT)
         .get(ResourceKey.create(Registries.ENCHANTMENT, Expansion.id(path)))
         .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
         .orElse(0);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.getCooldowns().isOnCooldown(this) || currentFuel(stack) < 1) {
         return InteractionResultHolder.fail(stack);
      } else if (!(level instanceof ServerLevel server)) {
         return InteractionResultHolder.success(stack);
      } else {
         int burst = enchantment(stack, level, "stun_burst");
         boolean hit = false;
         if (burst > 0) {
            double rx = burst * 4.0;
            double ry = burst * 2.0;

            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(rx, ry, rx), e -> canStun(player, e))) {
               Vec3 offset = target.position().subtract(player.position());
               double distance = offset.x * offset.x / (rx * rx) + offset.y * offset.y / (ry * ry) + offset.z * offset.z / (rx * rx);
               if (distance < 1.0) {
                  hit |= stun(server, player, target, (int)(30.0 * burst * (1.0 - distance)), hand);
               }
            }

            hit = true;
            server.sendParticles(
               (SimpleParticleType)EnderscapeParticles.VOID_POOF.get(),
               player.getX(),
               player.getY() + 1.0,
               player.getZ(),
               30,
               rx / 2.0,
               ry / 2.0,
               rx / 2.0,
               0.1
            );
         } else {
            Vec3 start = player.getEyePosition();
            double range = player.entityInteractionRange();
            Vec3 direction = player.getViewVector(1.0F);
            EntityHitResult result = ProjectileUtil.getEntityHitResult(
               player,
               start,
               start.add(direction.scale(range)),
               player.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0),
               e -> e instanceof LivingEntity living && canStun(player, living),
               range * range
            );
            if (result != null && result.getEntity() instanceof LivingEntity targetx && player.hasLineOfSight(targetx)) {
               player.attack(targetx);
               stun(server, player, targetx, 60, hand);
               hit = true;
            }
         }

         if (hit) {
            int resonance = Math.max(enchantment(stack, level, "resonance"), enchantment(stack, level, "lightspeed"));
            CompoundTag data = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
            int count = data.getInt("enderscape_expansion.stuns") + 1;
            if (count >= 4 + resonance * 2) {
               setFuel(stack, currentFuel(stack) - 1);
               count = 0;
            }

            data.putInt("enderscape_expansion.stuns", count);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            player.getCooldowns().addCooldown(this, burst > 0 ? 210 * burst : 140);
         } else {
            player.getCooldowns().addCooldown(this, 35);
         }

         player.swing(hand, true);
         SoundEvent sound = (SoundEvent)BuiltInRegistries.SOUND_EVENT
            .get(Expansion.id("item.dagger." + (hit ? (burst > 0 ? "stun_burst" : "stun") : "stun_miss")));
         if (sound != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, player.getSoundSource(), 1.0F, 1.0F);
         }

         return InteractionResultHolder.consume(stack);
      }
   }

   private static boolean canStun(Player player, LivingEntity target) {
      return target != player
         && target.canBeSeenAsEnemy()
         && !player.isAlliedTo(target)
         && !(target instanceof Player other && (!player.canHarmPlayer(other) || player.getServer() != null && !player.getServer().isPvpAllowed()));
   }

   private static boolean stun(ServerLevel level, Player player, LivingEntity target, int ticks, InteractionHand hand) {
      if (target instanceof Player) {
         ticks = (int)(ticks * 0.75);
      }

      if (hand == InteractionHand.OFF_HAND) {
         ticks /= 2;
      }

      if (target.isBlocking() && target instanceof Player defender) {
         defender.getCooldowns().addCooldown(target.getUseItem().getItem(), 30);
         defender.stopUsingItem();
      }

      boolean applied = target.addEffect(new MobEffectInstance(ExpansionEffects.STUNNED, Math.max(1, ticks)), player);
      if (applied) {
         ExpansionAdvancements.award(player, "stun_attack");
      }

      if (applied) {
         level.sendParticles((SimpleParticleType)EnderscapeParticles.VOID_POOF.get(), target.getX(), target.getY() + 0.5, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
      }

      return applied;
   }
}
