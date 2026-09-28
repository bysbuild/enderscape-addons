package dev.yuanyu.enderscapeexpansion;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class HavenNetwork {
   @SubscribeEvent
   public static void register(RegisterPayloadHandlersEvent event) {
      event.registrar("1")
         .playToServer(
            HavenNetwork.Respawn.TYPE,
            HavenNetwork.Respawn.CODEC,
            (packet, context) -> context.enqueueWork(
               () -> {
                  if (context.player() instanceof ServerPlayer player
                     && player.isDeadOrDying()
                     && player.level().dimension() == Level.END
                     && !player.getServer().isHardcore()) {
                     player.getPersistentData().putBoolean("enderscape_expansion.haven_requested", true);
                     player.connection.handleClientCommand(new ServerboundClientCommandPacket(Action.PERFORM_RESPAWN));
                  }
               }
            )
         );
   }

   public record Respawn() implements CustomPacketPayload {
      public static final Type<HavenNetwork.Respawn> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "haven_respawn"));
      public static final StreamCodec<RegistryFriendlyByteBuf, HavenNetwork.Respawn> CODEC = StreamCodec.unit(new HavenNetwork.Respawn());

      public Type<HavenNetwork.Respawn> type() {
         return TYPE;
      }
   }
}
