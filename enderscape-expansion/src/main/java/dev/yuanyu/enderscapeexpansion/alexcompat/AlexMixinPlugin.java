package dev.yuanyu.enderscapeexpansion.alexcompat;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class AlexMixinPlugin implements IMixinConfigPlugin {
   public void onLoad(String pkg) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String target, String mixin) {
      LoadingModList mods = FMLLoader.getLoadingModList();
      if (mods == null) {
         return false;
      } else {
         boolean supported = mods.getMods().stream().anyMatch(m -> m.getModId().equals("alexsmobsup") && m.getVersion().toString().equals("0.2.8"));
         boolean standalone = mods.getMods().stream().anyMatch(m -> m.getModId().equals("spectre_leash_fix"));
         return supported && !standalone;
      }
   }

   public void acceptTargets(Set<String> mine, Set<String> other) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) {
   }

   public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) {
   }
}
