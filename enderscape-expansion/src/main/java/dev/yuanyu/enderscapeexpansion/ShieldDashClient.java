package dev.yuanyu.enderscapeexpansion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public final class ShieldDashClient {
   private static boolean wasJumping;

   @SubscribeEvent
   public static void input(MovementInputUpdateEvent event) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && event.getEntity() == player) {
         Input input = event.getInput();
         boolean jumping = input.jumping;
         if (jumping && !wasJumping && player.onGround() && ShieldDash.charging(player) && ShieldDash.progress(player.getTicksUsingItem()) >= 0.875) {
            PacketDistributor.sendToServer(new ShieldDash.Request(input.leftImpulse, input.forwardImpulse), new CustomPacketPayload[0]);
            input.jumping = false;
         }

         wasJumping = jumping;
      }
   }

   @SubscribeEvent
   public static void bar(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null && !player.isPassenger() && ShieldDash.charging(player) && event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR)) {
         GuiGraphics gui = event.getGuiGraphics();
         int x = (mc.getWindow().getGuiScaledWidth() - 182) / 2;
         int y = mc.getWindow().getGuiScaledHeight() - 29;
         gui.blitSprite(ResourceLocation.withDefaultNamespace("hud/jump_bar_background"), x, y, 182, 5);
         int width = (int)(182.0F * ShieldDash.progress(player.getTicksUsingItem()));
         if (player.getFoodData().getFoodLevel() <= 6 && !player.getAbilities().instabuild) {
            gui.blitSprite(ResourceLocation.withDefaultNamespace("hud/jump_bar_cooldown"), x, y, 182, 5);
         } else if (width > 0) {
            gui.blitSprite(ResourceLocation.withDefaultNamespace("hud/jump_bar_progress"), 182, 5, 0, 0, x, y, width, 5);
         }

         event.setCanceled(true);
      }
   }
}
