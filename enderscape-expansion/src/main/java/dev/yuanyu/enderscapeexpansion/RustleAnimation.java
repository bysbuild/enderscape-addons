package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.entity.rustle.Rustle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent.Pre;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public final class RustleAnimation {
   @SubscribeEvent
   public static void swell(Pre<?, ?> event) {
      if (event.getEntity() instanceof Rustle mob) {
         int packed = (Integer)mob.getData(ExpansionEffects.RUSTLE_CONVERSION);
         int end = packed >>> 16;
         int tick = packed & 65535;
         if (end > 10 && tick >= 10 && tick <= end) {
            float swell = 1.0F + (float)Math.sin(Math.PI * (tick - 10) / (end - 10)) * 0.12F;
            event.getPoseStack().scale(swell, 1.0F / swell, swell);
         }
      }
   }
}
