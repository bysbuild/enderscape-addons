package dev.yuanyu.enderscapeexpansion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.monster.EnderMan;

public class ImprovedEndermanModel extends HierarchicalModel<EnderMan> {
   private static final float WALK_ANIMATION_SPEED = 1.75F;
   private static final float WALK_ANIMATION_SCALE = 2.5F;
   private static final float CREEPY_JITTER_SPEED = 5.0F;
   private static final float CREEPY_JITTER_SCALE = 0.25F;
   private static final int CREEPY_JITTER_STEP_SIZE = 2;
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart jaw;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart block;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   private final AnimationDefinition creepyFace;
   private final AnimationDefinition idlePose;
   private final AnimationDefinition holdingPose;
   private final AnimationDefinition walkAnimation;
   private final AnimationDefinition walkHoldingAnimation;
   private final AnimationDefinition attackAnimation;
   private final AnimationDefinition attackHoldingAnimation;
   private final AnimationDefinition walkLeftArmAnimation;
   private final AnimationDefinition walkRightArmAnimation;

   public ModelPart root() {
      return this.root;
   }

   public ImprovedEndermanModel(ModelPart root) {
      this.root = root;
      this.body = root.getChild("body");
      this.head = root.getChild("head");
      this.jaw = this.head.getChild("jaw");
      this.rightArm = root.getChild("right_arm");
      this.leftArm = root.getChild("left_arm");
      this.block = this.leftArm.getChild("block");
      this.rightLeg = root.getChild("right_leg");
      this.leftLeg = root.getChild("left_leg");
      this.creepyFace = ImprovedEndermanAnimations.ENDERMAN_FACE_ANGRY;
      this.idlePose = ImprovedEndermanAnimations.ENDERMAN_ARM_IDLE;
      this.holdingPose = ImprovedEndermanAnimations.ENDERMAN_ARM_HOLD;
      this.walkAnimation = ImprovedEndermanAnimations.ENDERMAN_WALK;
      this.walkHoldingAnimation = ImprovedEndermanAnimations.ENDERMAN_WALK_HOLD;
      this.attackAnimation = ImprovedEndermanAnimations.ENDERMAN_ATTACK;
      this.attackHoldingAnimation = ImprovedEndermanAnimations.ENDERMAN_ATTACK_HOLD;
      this.walkLeftArmAnimation = ImprovedEndermanAnimations.ENDERMAN_ARM_SWING_L;
      this.walkRightArmAnimation = ImprovedEndermanAnimations.ENDERMAN_ARM_SWING_R;
   }

   public static LayerDefinition createLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition root = meshdefinition.getRoot();
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(32, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -3.0F, 0.0F)
      );
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -15.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "jaw",
         CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-5.0F, -13.0F, 0.0F)
      );
      PartDefinition left_arm = root.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(5.0F, -13.0F, 0.0F)
      );
      left_arm.addOrReplaceChild("block", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, 23.0F, -1.0F, 0.5236F, 0.0F, 0.0F));
      root.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, -6.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, -6.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void setupAnim(EnderMan entity, float walkAnimationPos, float walkAnimationSpeed, float ageInTicks, float yaw, float pitch) {
      this.root.getAllParts().forEach(ModelPart::resetPose);
      AnimationState attack = ((EndermanAnimation)entity).expansion$attackState();
      boolean carryingBlock = entity.getCarriedBlock() != null;
      float scaledSpeed = entity.isCreepy() ? 3.5F : 1.75F;
      this.head.xRot = pitch * (float) Math.PI / 180.0F;
      this.head.yRot = yaw * (float) Math.PI / 180.0F;
      this.jaw.y += 0.6F;
      float movementScale = 1.0F - Math.min(walkAnimationSpeed * 2.0F, 1.0F);
      if (entity.isCreepy()) {
         this.animateCreepy(ageInTicks, movementScale);
      } else {
         this.animateIdle(attack, ageInTicks, carryingBlock, movementScale);
      }

      if (!carryingBlock) {
         this.animateWalk(this.walkAnimation, walkAnimationPos, walkAnimationSpeed, scaledSpeed, 2.5F);
         this.animateWalk(this.walkLeftArmAnimation, walkAnimationPos, walkAnimationSpeed, scaledSpeed, 2.5F);
         this.animateWalk(this.walkRightArmAnimation, walkAnimationPos, walkAnimationSpeed, scaledSpeed, 2.5F);
         this.animate(attack, this.attackAnimation, ageInTicks, 0.8F);
         if (!attack.isStarted()) {
            this.applyStatic(this.idlePose);
         }
      } else {
         this.animateWalk(this.walkHoldingAnimation, walkAnimationPos, walkAnimationSpeed, scaledSpeed, 2.5F);
         this.animate(attack, this.attackHoldingAnimation, ageInTicks, 0.8F);
         if (!attack.isStarted()) {
            this.applyStatic(this.holdingPose);
         }
      }
   }

   private void animateIdle(AnimationState attack, float ageInTicks, boolean carryingBlock, float scale) {
      if (!attack.isStarted()) {
         this.head.zRot = this.head.zRot + Mth.cos(ageInTicks * 0.05F) * 0.05F * scale;
         this.head.xRot = this.head.xRot + Mth.cos(ageInTicks * 0.075F + (float) (Math.PI / 2)) * 0.05F * scale;
         if (!carryingBlock) {
            this.leftArm.zRot = this.leftArm.zRot + Mth.sin(ageInTicks * 0.05F) * 0.05F * scale;
            this.rightArm.zRot = this.rightArm.zRot + Mth.cos(ageInTicks * 0.1F) * 0.05F * scale;
            this.leftArm.xRot = this.leftArm.xRot + Mth.sin(ageInTicks * 0.05F + (float) (Math.PI / 2)) * 0.05F * scale;
            this.rightArm.xRot = this.rightArm.xRot + Mth.cos(ageInTicks * 0.1F + (float) (Math.PI / 2)) * 0.05F * scale;
         }
      }
   }

   private void animateCreepy(float ageInTicks, float movementScale) {
      this.applyStatic(this.creepyFace);
      float jitter = creepyJitterScale(ageInTicks, Math.max(0.5F, movementScale), false);
      float jitterCos = creepyJitterScale(ageInTicks, Math.max(0.5F, movementScale), true);
      float legsJitter = creepyJitterScale(ageInTicks, movementScale, false);
      float legsJitterCos = creepyJitterScale(ageInTicks, movementScale, true);
      this.head.x += jitter;
      this.head.z += jitterCos;
      this.body.x += jitter;
      this.body.z += jitterCos;
      this.leftArm.x += jitter;
      this.leftArm.z += jitterCos;
      this.rightArm.x += jitter;
      this.rightArm.z += jitterCos;
      this.leftLeg.x += legsJitter;
      this.leftLeg.z += legsJitterCos;
      this.rightLeg.x += legsJitter;
      this.rightLeg.z += legsJitterCos;
   }

   private static float creepyJitterScale(float ageInTicks, float scale, boolean cosine) {
      int sin;
      if (cosine) {
         sin = (int)(Mth.cos(ageInTicks * 5.0F) * 2.0F);
      } else {
         sin = (int)(Mth.sin(ageInTicks * 5.0F) * 2.0F);
      }

      return sin * 0.25F * scale;
   }

   public void applyCarriedBlockTransform(PoseStack pose) {
      this.root.translateAndRotate(pose);
      this.leftArm.translateAndRotate(pose);
      this.block.translateAndRotate(pose);
      pose.scale(0.5F, 0.5F, 0.5F);
      pose.translate(0.0, -0.15, -0.15);
   }
}
