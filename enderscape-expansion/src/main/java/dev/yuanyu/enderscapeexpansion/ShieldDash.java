package dev.yuanyu.enderscapeexpansion;

import java.util.List;
import net.bunten.enderscape.entity.DashJumpUser;
import net.bunten.enderscape.item.RubbleShieldItem;
import net.bunten.enderscape.registry.EnderscapeItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class ShieldDash {
   public static boolean charging(LivingEntity player) {
      return player.isUsingItem() && player.getUseItem().getItem() instanceof RubbleShieldItem;
   }

   public static float progress(int ticks) {
      double fraction = ticks / 20.0;
      if (fraction <= 1.0) {
         return (float)(1.0 - Math.pow(1.0 - fraction, 4.0));
      } else {
         double excess = Math.min(fraction - 1.0, 1.0);
         double eased = excess < 0.5 ? 8.0 * Math.pow(excess, 4.0) : 1.0 - Math.pow(-2.0 * excess + 2.0, 4.0) / 2.0;
         return (float)(1.0 - 0.2 * eased);
      }
   }

   public static boolean apply(ServerPlayer player, float strafe, float forward) {
      if (Float.isFinite(strafe)
         && Float.isFinite(forward)
         && player.isAlive()
         && !player.isSpectator()
         && !player.isPassenger()
         && charging(player)
         && player.onGround()
         && !player.isInWaterOrBubble()
         && !player.isInLava()
         && !(progress(player.getTicksUsingItem()) < 0.875)
         && (player.getAbilities().instabuild || player.getFoodData().getFoodLevel() > 6)) {
         ItemStack stack = player.getUseItem();
         if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return false;
         } else {
            InteractionHand hand = player.getUsedItemHand();
            Vec3 input = new Vec3(strafe, 0.0, forward).normalize();
            double angle = Math.toRadians(player.getYRot());
            Vec3 velocity = new Vec3(
               (input.x * Math.cos(angle) - input.z * Math.sin(angle)) * 2.35, 0.35, (input.z * Math.cos(angle) + input.x * Math.sin(angle)) * 2.35
            );
            player.stopUsingItem();
            player.setDeltaMovement(velocity);
            player.setOnGround(false);
            player.hurtMarked = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
            stack.hurtAndBreak(5, player, LivingEntity.getSlotForHand(hand));
            player.causeFoodExhaustion(4.0F);
            player.getCooldowns().addCooldown(stack.getItem(), 60);

            for (Item item : List.of(
               (Item)EnderscapeItems.END_STONE_RUBBLE_SHIELD.get(),
               (Item)EnderscapeItems.MIRESTONE_RUBBLE_SHIELD.get(),
               (Item)EnderscapeItems.VERADITE_RUBBLE_SHIELD.get(),
               (Item)EnderscapeItems.KURODITE_RUBBLE_SHIELD.get(),
               (Item)ExpansionItems.RUBBLE_SHIELD.get()
            )) {
               player.getCooldowns().addCooldown(item, 60);
            }

            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            ExpansionAdvancements.award(player, "rubble_shield_dash");
            DashJumpUser.setDashed(player, true);
            DashJumpUser.setDashTicks(player, 60);
            SoundEvent sound = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("item.rubble_shield.dash"));
            if (sound != null) {
               player.level().playSound(null, player.blockPosition(), sound, player.getSoundSource(), 1.0F, 1.0F);
            }

            player.serverLevel().sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 0.5, player.getZ(), 5, 0.0, 0.0, 0.0, 0.1);
            return true;
         }
      } else {
         return false;
      }
   }

   @SubscribeEvent
   public static void register(RegisterPayloadHandlersEvent event) {
      event.registrar("1").playToServer(ShieldDash.Request.TYPE, ShieldDash.Request.CODEC, (packet, context) -> context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer player) {
            apply(player, packet.strafe(), packet.forward());
         }
      }));
   }

   public record Request(float strafe, float forward) implements CustomPacketPayload {
      public static final Type<ShieldDash.Request> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "shield_dash"));
      public static final StreamCodec<RegistryFriendlyByteBuf, ShieldDash.Request> CODEC = StreamCodec.composite(
         ByteBufCodecs.FLOAT, ShieldDash.Request::strafe, ByteBufCodecs.FLOAT, ShieldDash.Request::forward, ShieldDash.Request::new
      );

      public Type<ShieldDash.Request> type() {
         return TYPE;
      }
   }
}
