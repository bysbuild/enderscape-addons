package dev.yuanyu.enderscapeexpansion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public final class HavenScreen {
   @SubscribeEvent
   public static void init(Post event) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (event.getScreen() instanceof DeathScreen screen
         && player != null
         && player.level().dimension() == Level.END
         && !player.level().getLevelData().isHardcore()
         && ((CompoundTag)player.getData(ExpansionEffects.HAVEN)).contains("position")) {
         event.addListener(Button.builder(Component.translatable("enderscape_expansion.haven_respawn"), button -> {
            button.active = false;
            PacketDistributor.sendToServer(new HavenNetwork.Respawn(), new CustomPacketPayload[0]);
         }).bounds(screen.width / 2 - 100, screen.height / 4 + 120, 200, 20).build());
      }
   }
}
