package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class StunnedInventoryMixin {
   @Shadow
   public ServerPlayer player;

   @Inject(method = "handleSetCarriedItem", at = @At("HEAD"), cancellable = true)
   private void expansion$slot(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
      PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl)this, this.player.serverLevel());
      if (this.player.hasEffect(ExpansionEffects.STUNNED)) {
         this.player.connection.send(new ClientboundSetCarriedItemPacket(this.player.getInventory().selected));
         ci.cancel();
      }
   }

   @Inject(method = "handleContainerClick", at = @At("HEAD"), cancellable = true)
   private void expansion$container(ServerboundContainerClickPacket packet, CallbackInfo ci) {
      PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl)this, this.player.serverLevel());
      if (this.player.hasEffect(ExpansionEffects.STUNNED)) {
         this.player.containerMenu.sendAllDataToRemote();
         ci.cancel();
      }
   }
}
