package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.Expansion;
import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui.HeartType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class VoidedHeartsMixin {
   @Unique
   private final List<int[]> expansion$hearts = new ArrayList<>();

   @Inject(method = "renderHearts", at = @At("HEAD"))
   private void expansion$clear(CallbackInfo ci) {
      this.expansion$hearts.clear();
   }

   @Inject(method = "renderHeart", at = @At("TAIL"))
   private void expansion$record(GuiGraphics gui, HeartType type, int x, int y, boolean hardcore, boolean blink, boolean half, CallbackInfo ci) {
      if (type == HeartType.CONTAINER) {
         this.expansion$hearts.add(new int[]{x, y});
      }
   }

   @Inject(method = "renderHearts", at = @At("TAIL"))
   private void expansion$draw(
      GuiGraphics gui,
      Player player,
      int x,
      int y,
      int rows,
      int offset,
      float maximum,
      int health,
      int previous,
      int absorption,
      boolean blink,
      CallbackInfo ci
   ) {
      float voided = (Float)player.getData(ExpansionEffects.VOIDED_HEALTH);
      boolean hardcore = player.level().getLevelData().isHardcore();
      int warning = (Integer)player.getData(ExpansionEffects.OUTER_VOID);

      for (int[] p : this.expansion$hearts) {
         if (voided > 0.0F) {
            String suffix = voided <= 1.0F ? "half" : "full";
            gui.blitSprite(Expansion.id("hud/heart/voided/" + (hardcore ? "hardcore/" : "") + suffix), p[0], p[1], 9, 9);
            if (player.hasEffect(ExpansionEffects.PURIFICATION) && !blink) {
               gui.blitSprite(Expansion.id("hud/heart/void_purification_outline/" + suffix), p[0], p[1], 9, 9);
            }

            voided -= 2.0F;
         }

         if (warning > 0 && player.tickCount % 10 < 5) {
            gui.blitSprite(Expansion.id("hud/heart/outer_void_warning/full"), p[0], p[1], 9, 9);
         }
      }
   }
}
