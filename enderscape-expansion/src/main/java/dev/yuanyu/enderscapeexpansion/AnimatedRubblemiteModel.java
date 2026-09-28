package dev.yuanyu.enderscapeexpansion;

import java.util.Map;
import java.util.WeakHashMap;
import net.bunten.enderscape.client.entity.rubblemite.RubblemiteModel;
import net.bunten.enderscape.entity.rubblemite.Rubblemite;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

public class AnimatedRubblemiteModel extends RubblemiteModel {
   private final ModelPart root;
   private final ModelPart shell;
   private final ModelPart head;
   private final Map<Rubblemite, AnimationState[]> animations = new WeakHashMap<>();

   public AnimatedRubblemiteModel(ModelPart root) {
      super(root);
      this.root = root;
      this.shell = root.getChild("shell");
      this.head = this.shell.getChild("head");
   }

   public ModelPart root() {
      return this.root;
   }

   public static LayerDefinition createLayer() {
      MeshDefinition data = new MeshDefinition();
      PartDefinition rootData = data.getRoot();
      PartDefinition shell = rootData.addOrReplaceChild(
         "shell",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.5F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 21.5F, 0.0F)
      );
      shell.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(0, 14).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.5F, -4.0F)
      );
      return LayerDefinition.create(data, 32, 32);
   }

   public void setupAnim(Rubblemite mob, float pos, float speed, float age, float yaw, float pitch) {
      this.root.getAllParts().forEach(ModelPart::resetPose);
      AnimationState[] states = this.animations
         .computeIfAbsent(mob, key -> new AnimationState[]{new AnimationState(), new AnimationState(), new AnimationState()});
      states[0].animateWhen(mob.isDashing(), mob.tickCount);
      states[1].animateWhen((Integer)mob.getData(ExpansionEffects.PREPARE_DASH) > 0, mob.tickCount);
      states[2].animateWhen(mob.isInsideShell(), mob.tickCount);
      if (mob.deathTime == 0) {
         this.head.zRot = Mth.sin(age + pos / 3.0F * 0.06F) * speed * 0.1F;
         this.shell.xRot += pitch * (float) Math.PI / 360.0F;
         this.shell.yRot += yaw * (float) Math.PI / 360.0F;
         this.shell.zRot = Mth.sin(age + pos / 3.0F * 0.03F + (float) (Math.PI / 2)) * speed * 0.15F;
      }

      this.animate(states[0], RubblemiteAnimations.DASH, age);
      this.animate(states[1], RubblemiteAnimations.PREPARE_DASH, age);
      this.animate(states[2], RubblemiteAnimations.INSIDE_SHELL, age);
   }
}
