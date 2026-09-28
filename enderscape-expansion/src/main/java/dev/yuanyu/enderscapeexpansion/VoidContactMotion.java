package dev.yuanyu.enderscapeexpansion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.neoforge.fluids.FluidType;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class VoidContactMotion {
   public static final int RECOVERY = 110;
   private static final String TIMER = "enderscape_expansion.void_contact_recovery";
   private static final ResourceLocation SLOW = ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "void_contact_slow");

   public static boolean touching(Player player) {
      return player.isInFluidType((FluidType)VoidFluid.TYPE.get());
   }

   @SubscribeEvent
   public static void tick(Post event) {
      Player player = event.getEntity();
      if (!player.level().isClientSide()) {
         int remaining = player.getPersistentData().getInt("enderscape_expansion.void_contact_recovery");
         if (player.isSpectator() || !player.isAlive()) {
            remaining = 0;
         } else if (touching(player)) {
            remaining = 110;
         } else {
            remaining = Math.max(0, remaining - 1);
         }

         player.getPersistentData().putInt("enderscape_expansion.void_contact_recovery", remaining);
         AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
         if (speed != null) {
            double amount = -0.4 * remaining / 110.0;
            AttributeModifier old = speed.getModifier(SLOW);
            if (old == null || old.amount() != amount) {
               speed.removeModifier(SLOW);
               if (remaining > 0) {
                  speed.addTransientModifier(new AttributeModifier(SLOW, amount, Operation.ADD_MULTIPLIED_TOTAL));
               }
            }
         }
      }
   }
}
