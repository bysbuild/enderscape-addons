package dev.yuanyu.enderscapeexpansion;

import dev.yuanyu.enderscapeexpansion.mixin.SinglePoolElementAccessor;
import dev.yuanyu.enderscapeexpansion.mixin.TemplatePieceAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces.EndCityPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class EndCityShipRule {
   private static final ResourceLocation SHIP = Expansion.id("end_city/ship");

   public static boolean isShip(StructurePiece piece) {
      if (piece instanceof PoolElementStructurePiece pool && pool.getElement() instanceof SinglePoolElement single) {
         return ((SinglePoolElementAccessor)single).expansion$template().left().filter(SHIP::equals).isPresent();
      } else if (!(piece instanceof EndCityPiece)) {
         return false;
      } else {
         String name = ((TemplatePieceAccessor)piece).expansion$templateName();
         return name.equals("end_city/ship") || name.equals("minecraft:end_city/ship") || name.equals("ship");
      }
   }

   public static StructureStart apply(StructureStart start, RegistryAccess registries, StructureTemplateManager templates, LevelHeightAccessor height) {
      if (!start.isValid()) {
         return start;
      } else {
         ResourceLocation id = registries.registryOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
         boolean modded = Expansion.id("end_city").equals(id);
         if (!modded && !ResourceLocation.withDefaultNamespace("end_city").equals(id)) {
            return start;
         } else {
            List<StructurePiece> pieces = new ArrayList<>();
            boolean found = false;

            for (StructurePiece piece : start.getPieces()) {
               if (isShip(piece)) {
                  if (found) {
                     continue;
                  }

                  found = true;
               }

               pieces.add(piece);
            }

            if (!found) {
               Rotation rotation = Rotation.values()[Math.floorMod(start.getChunkPos().hashCode(), 4)];
               Function<BlockPos, StructurePiece> create;
               if (modded) {
                  Reference<StructureProcessorList> processors = registries.registryOrThrow(Registries.PROCESSOR_LIST)
                     .getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, Expansion.id("end_city_flower_pots")));
                  SinglePoolElement element = (SinglePoolElement)StructurePoolElement.single(SHIP.toString(), processors).apply(Projection.RIGID);
                  create = posx -> new PoolElementStructurePiece(
                     templates,
                     element,
                     posx,
                     element.getGroundLevelDelta(),
                     rotation,
                     element.getBoundingBox(templates, posx, rotation),
                     LiquidSettings.IGNORE_WATERLOGGING
                  );
               } else {
                  create = posx -> new EndCityPiece(templates, "ship", posx, rotation, true);
               }

               BoundingBox shape = create.apply(BlockPos.ZERO).getBoundingBox();
               int cx = start.getChunkPos().getMiddleBlockX();
               int cz = start.getChunkPos().getMiddleBlockZ();
               int preferredY = Math.min(
                  height.getMaxBuildHeight() - shape.getYSpan() - 1, Math.max(height.getMinBuildHeight() + 32, pieces.getFirst().getBoundingBox().minY() + 24)
               );
               StructurePiece ship = null;

               for (int dy = 0; dy < height.getHeight() && ship == null; dy += 8) {
                  for (int sign : new int[]{1, -1}) {
                     int y = preferredY + sign * dy;
                     if (y >= height.getMinBuildHeight() + 16 && y + shape.getYSpan() < height.getMaxBuildHeight()) {
                        for (int radius = 48; radius <= 96 && ship == null; radius += 16) {
                           for (int x = -radius; x <= radius && ship == null; x += 16) {
                              for (int z = -radius; z <= radius && ship == null; z += 16) {
                                 if (Math.abs(x) == radius || Math.abs(z) == radius) {
                                    BlockPos pos = new BlockPos(
                                       cx + x - shape.minX() - shape.getXSpan() / 2, y - shape.minY(), cz + z - shape.minZ() - shape.getZSpan() / 2
                                    );
                                    BoundingBox box = new BoundingBox(
                                       shape.minX() + pos.getX(),
                                       shape.minY() + pos.getY(),
                                       shape.minZ() + pos.getZ(),
                                       shape.maxX() + pos.getX(),
                                       shape.maxY() + pos.getY(),
                                       shape.maxZ() + pos.getZ()
                                    );
                                    if (box.minX() >= cx - 112 && box.maxX() <= cx + 112 && box.minZ() >= cz - 112 && box.maxZ() <= cz + 112) {
                                       BoundingBox clearance = box.inflatedBy(3);
                                       boolean blocked = false;

                                       for (StructurePiece piece : pieces) {
                                          if (piece.getBoundingBox().intersects(clearance)) {
                                             blocked = true;
                                             break;
                                          }
                                       }

                                       if (!blocked) {
                                          ship = create.apply(pos);
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        if (ship != null) {
                           break;
                        }
                     }
                  }
               }

               if (ship == null) {
                  return StructureStart.INVALID_START;
               }

               pieces.add(ship);
            }

            return found && pieces.size() == start.getPieces().size()
               ? start
               : new StructureStart(start.getStructure(), start.getChunkPos(), start.getReferences(), new PiecesContainer(pieces));
         }
      }
   }
}
