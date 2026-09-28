package dev.yuanyu.enderscapetrimbridge;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = "trimmed", bus = Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
   @SubscribeEvent
   public static void setup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         for (String name : TrimModels.ALL_MATERIALS) {
            ResourceLocation material = ResourceLocation.parse(name);
            ItemProperties.registerGeneric(TrimModels.predicate(name), (stack, level, entity, seed) -> {
               ArmorTrim trim = (ArmorTrim)stack.get(DataComponents.TRIM);
               return trim != null && trim.material().is(material) ? 1.0F : 0.0F;
            });
         }
      });
   }
}
