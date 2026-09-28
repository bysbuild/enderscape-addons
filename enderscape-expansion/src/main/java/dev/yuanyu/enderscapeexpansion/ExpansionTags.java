package dev.yuanyu.enderscapeexpansion;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ExpansionTags {
   public static final TagKey<Block> SUPPORTS_PURUBERRY_VINE = TagKey.create(Registries.BLOCK, Expansion.id("supports_puruberry_vine"));
   public static final TagKey<Block> PURUBERRY_VINE_SUPPORTS = TagKey.create(Registries.BLOCK, Expansion.id("puruberry_vine_supports"));
}
