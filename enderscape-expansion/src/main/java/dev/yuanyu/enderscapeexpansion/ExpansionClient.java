package dev.yuanyu.enderscapeexpansion;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Map.Entry;
import net.bunten.enderscape.registry.EnderscapeItems;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.joml.Vector3f;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public final class ExpansionClient {
   @SubscribeEvent
   public static void fluidExtensions(RegisterClientExtensionsEvent event) {
      event.registerFluidType(new IClientFluidTypeExtensions() {
         public ResourceLocation getStillTexture() {
            return Expansion.id("block/void_lachryma_still");
         }

         public ResourceLocation getFlowingTexture() {
            return Expansion.id("block/void_lachryma_flow");
         }

         public Vector3f modifyFogColor(Camera camera, float partial, ClientLevel level, int distance, float darken, Vector3f color) {
            return new Vector3f(0.023529412F, 0.0F, 0.047058824F);
         }

         public void modifyFogRender(Camera camera, FogMode mode, float distance, float partial, float near, float far, FogShape shape) {
            RenderSystem.setShaderFogStart(camera.getEntity().isSpectator() ? -8.0F : 0.25F);
            RenderSystem.setShaderFogEnd(camera.getEntity().isSpectator() ? distance * 0.5F : 1.0F);
         }
      }, new FluidType[]{(FluidType)VoidFluid.TYPE.get()});
   }

   @SubscribeEvent
   public static void setup(FMLClientSetupEvent event) {
      event.enqueueWork(
         () -> {
            if (ModList.get().isLoaded("colorfulhearts")) {
               ColorfulHeartsCompat.register();
            }

            RadioBlockEntity.CLIENT_TICK = RadioAudio::tick;
            ItemBlockRenderTypes.setRenderLayer(Blocks.DRAGON_EGG, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer((Block)ExpansionBlocks.MAGNIA_RADIO.get(), RenderType.cutout());
            ItemProperties.register(
               (Item)EnderscapeItems.MIRROR.get(),
               ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "mirror_color"),
               (stack, level, entity, seed) -> stack.has(UnifiedShield.DYE_COLOR) ? ((DyeColor)stack.get(UnifiedShield.DYE_COLOR)).getId() + 1 : 0.0F
            );
            ItemBlockRenderTypes.setRenderLayer((Block)ExpansionBlocks.WILDFLOWERS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer((Block)ExpansionBlocks.MURUBLIGHT_BRACKET.get(), RenderType.cutout());
            ItemProperties.register(
               (Item)ExpansionItems.RUBBLE_SHIELD.get(),
               ResourceLocation.withDefaultNamespace("blocking"),
               (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
            );
            ItemProperties.register(
               (Item)ExpansionItems.RUBBLE_SHIELD.get(),
               ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "shield_variant"),
               (stack, level, entity, seed) -> Math.max(
                  0,
                  List.of("end_stone", "mirestone", "veradite", "kurodite")
                     .indexOf(((ResourceLocation)stack.getOrDefault(UnifiedShield.VARIANT, Expansion.id("end_stone"))).getPath())
               )
            );

            for (String material : List.of(
               "minecraft:quartz",
               "minecraft:iron",
               "minecraft:netherite",
               "minecraft:redstone",
               "minecraft:copper",
               "minecraft:gold",
               "minecraft:emerald",
               "minecraft:diamond",
               "minecraft:lapis",
               "minecraft:amethyst",
               "enderscape:nebulite",
               "enderscape:shadoline"
            )) {
               ResourceLocation id = ResourceLocation.parse(material);
               ItemProperties.registerGeneric(
                  ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "trim/" + material.replace(':', '/')), (stack, level, entity, seed) -> {
                     ArmorTrim trim = (ArmorTrim)stack.get(DataComponents.TRIM);
                     return trim != null && trim.material().is(id) ? 1.0F : 0.0F;
                  }
               );
            }

            ItemBlockRenderTypes.setRenderLayer((Fluid)VoidFluid.SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer((Fluid)VoidFluid.FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer((Block)ExpansionBlocks.SHADOLINE_BARS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer((Block)ExpansionBlocks.SHADOLINE_CHAIN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer((Block)ExpansionBlocks.END_HAVEN_CORE.get(), RenderType.cutout());

            for (Entry<String, DeferredBlock<? extends Block>> entry : ExpansionBlocks.ADDED.entrySet()) {
               if (entry.getKey().contains("puruberry") || entry.getKey().startsWith("void_") || entry.getKey().startsWith("overgrown_")) {
                  ItemBlockRenderTypes.setRenderLayer((Block)entry.getValue().get(), RenderType.cutout());
               }
            }
         }
      );
   }
}
