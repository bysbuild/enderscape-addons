package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.EndCityShipRule;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Structure.class)
public abstract class StructureMixin {
   @Inject(method = "generate", at = @At("RETURN"), cancellable = true)
   private void expansion$oneShip(
      RegistryAccess registries,
      ChunkGenerator generator,
      BiomeSource biomes,
      RandomState randomState,
      StructureTemplateManager templates,
      long seed,
      ChunkPos chunk,
      int references,
      LevelHeightAccessor height,
      Predicate<Holder<Biome>> allowedBiome,
      CallbackInfoReturnable<StructureStart> cir
   ) {
      StructureStart start = EndCityShipRule.apply((StructureStart)cir.getReturnValue(), registries, templates, height);
      cir.setReturnValue(start);
   }
}
