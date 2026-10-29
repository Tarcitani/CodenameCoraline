package com.codenamecoraline.client.collector;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.collector.LastGiftCollector;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Articulated colossal renegade-Santa model for the Collector of the Last Gift (~4.5 blocks tall,
 * ~2.5 blocks wide across the fur-trimmed mantle shoulders):
 * - Cracked porcelain mask, winter hood, twisted wood-and-ice antler crown, and frozen root beard
 * - Heavy dark-wine mantle with ash-white fur shoulder collar
 * - Dorsal iron-and-wood reliquary cage with hinged door (`cage_door`) and suspended cracked bell (`cage_bell`)
 * - Ceremonial chest chains and bells (`chains`)
 * - Long curved Winter Scythe (`scythe`) carried in the right hand
 */
public final class LastGiftCollectorModel extends EntityModel<LastGiftCollectorRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "last_gift_collector"), "main");

    private final ModelPart pelvis;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart beard;
    private final ModelPart mantle;
    private final ModelPart reliquaryCage;
    private final ModelPart cageDoor;
    private final ModelPart cageBell;
    private final ModelPart chains;
    private final ModelPart rightArm;
    private final ModelPart rightForearm;
    private final ModelPart scythe;
    private final ModelPart leftArm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart rightBoot;
    private final ModelPart leftLeg;
    private final ModelPart leftBoot;

    private final KeyframeAnimation idleAnim;
    private final KeyframeAnimation idleAlternateAnim;
    private final KeyframeAnimation walkAnim;
    private final KeyframeAnimation appearAnim;
    private final KeyframeAnimation jumpFromSleighAnim;
    private final KeyframeAnimation fallAnim;
    private final KeyframeAnimation landAnim;
    private final KeyframeAnimation intimidateAnim;
    private final KeyframeAnimation attackScythePrepAnim;
    private final KeyframeAnimation attackScytheExecAnim;
    private final KeyframeAnimation attackHollowBellPrepAnim;
    private final KeyframeAnimation attackHollowBellImpactAnim;
    private final KeyframeAnimation attackPawnsAnim;
    private final KeyframeAnimation attackSolsticeJumpAnim;
    private final KeyframeAnimation attackCastAnim;
    private final KeyframeAnimation attackSpinAnim;
    private final KeyframeAnimation attackSlamAnim;
    private final KeyframeAnimation rageAnim;
    private final KeyframeAnimation recoveryAnim;
    private final KeyframeAnimation phaseTransitionAnim;
    private final KeyframeAnimation hurtAnim;
    private final KeyframeAnimation deathAnim;

    public LastGiftCollectorModel(ModelPart root) {
        super(root);
        var lookup = root.createPartLookup();
        this.pelvis = lookup.apply("pelvis");
        this.torso = lookup.apply("torso");
        this.head = lookup.apply("head");
        this.beard = lookup.apply("beard");
        this.mantle = lookup.apply("mantle");
        this.reliquaryCage = lookup.apply("reliquary_cage");
        this.cageDoor = lookup.apply("cage_door");
        this.cageBell = lookup.apply("cage_bell");
        this.chains = lookup.apply("chains");
        this.rightArm = lookup.apply("right_arm");
        this.rightForearm = lookup.apply("right_forearm");
        this.scythe = lookup.apply("scythe");
        this.leftArm = lookup.apply("left_arm");
        this.leftForearm = lookup.apply("left_forearm");
        this.rightLeg = lookup.apply("right_leg");
        this.rightBoot = lookup.apply("right_boot");
        this.leftLeg = lookup.apply("left_leg");
        this.leftBoot = lookup.apply("left_boot");

        this.idleAnim = LastGiftCollectorAnimations.IDLE.bake(root);
        this.idleAlternateAnim = LastGiftCollectorAnimations.IDLE_ALTERNATE.bake(root);
        this.walkAnim = LastGiftCollectorAnimations.WALK.bake(root);
        this.appearAnim = LastGiftCollectorAnimations.APPEAR.bake(root);
        this.jumpFromSleighAnim = LastGiftCollectorAnimations.JUMP_FROM_SLEIGH.bake(root);
        this.fallAnim = LastGiftCollectorAnimations.FALL.bake(root);
        this.landAnim = LastGiftCollectorAnimations.LAND.bake(root);
        this.intimidateAnim = LastGiftCollectorAnimations.INTIMIDATE.bake(root);
        this.attackScythePrepAnim = LastGiftCollectorAnimations.ATTACK_SCYTHE_PREP.bake(root);
        this.attackScytheExecAnim = LastGiftCollectorAnimations.ATTACK_SCYTHE_EXEC.bake(root);
        this.attackHollowBellPrepAnim = LastGiftCollectorAnimations.ATTACK_HOLLOW_BELL_PREP.bake(root);
        this.attackHollowBellImpactAnim = LastGiftCollectorAnimations.ATTACK_HOLLOW_BELL_IMPACT.bake(root);
        this.attackPawnsAnim = LastGiftCollectorAnimations.ATTACK_PAWNS.bake(root);
        this.attackSolsticeJumpAnim = LastGiftCollectorAnimations.ATTACK_SOLSTICE_JUMP.bake(root);
        this.attackCastAnim = LastGiftCollectorAnimations.ATTACK_CAST.bake(root);
        this.attackSpinAnim = LastGiftCollectorAnimations.ATTACK_SPIN.bake(root);
        this.attackSlamAnim = LastGiftCollectorAnimations.ATTACK_SLAM.bake(root);
        this.rageAnim = LastGiftCollectorAnimations.RAGE.bake(root);
        this.recoveryAnim = LastGiftCollectorAnimations.RECOVERY.bake(root);
        this.phaseTransitionAnim = LastGiftCollectorAnimations.PHASE_TRANSITION.bake(root);
        this.hurtAnim = LastGiftCollectorAnimations.HURT.bake(root);
        this.deathAnim = LastGiftCollectorAnimations.DEATH.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Pelvis pivot at y = 0.0 (24 model units above the ground at y = 24.0).
        PartDefinition pelvis = root.addOrReplaceChild("pelvis",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -4.0F, -5.0F, 14.0F, 8.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Colossal hunched upper torso extending up to y = -22.0.
        PartDefinition torso = pelvis.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-10.0F, -22.0F, -6.0F, 20.0F, 16.0F, 12.0F)
                        .texOffs(68, 20).addBox(-8.0F, -6.0F, -5.0F, 16.0F, 6.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Head: long-haired skull, shouting blue-grey face, and a burgundy Santa hat with fur brim, drooping tip and pompom.
        PartDefinition head = torso.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 56).addBox(-5.0F, -12.0F, -5.0F, 10.0F, 12.0F, 10.0F)
                        .texOffs(44, 56).addBox(-5.0F, -11.5F, -6.5F, 10.0F, 11.0F, 2.0F)
                        .texOffs(72, 56).addBox(-5.0F, -20.0F, -5.0F, 10.0F, 7.0F, 10.0F)
                        .texOffs(198, 56).addBox(-6.5F, -14.0F, -6.5F, 13.0F, 3.0F, 13.0F)
                        .texOffs(112, 56).addBox(-2.5F, -25.0F, 0.0F, 5.0F, 8.0F, 5.0F)
                        .texOffs(112, 70).addBox(-2.5F, -27.0F, 4.0F, 5.0F, 5.0F, 5.0F)
                        .texOffs(176, 73).addBox(-1.5F, -7.5F, -11.5F, 3.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, -22.0F, -1.5F));

        // Enormous cream beard spilling over the chest.
        head.addOrReplaceChild("beard",
                CubeListBuilder.create()
                        .texOffs(134, 56).addBox(-6.0F, 0.0F, -3.0F, 12.0F, 18.0F, 6.0F)
                        .texOffs(172, 56).addBox(-4.0F, 18.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, -1.0F, -4.5F));

        // Ceremonial dark-wine mantle with ash-white fur shoulder collar (40 units = 2.5 blocks wide across shoulders).
        torso.addOrReplaceChild("mantle",
                CubeListBuilder.create()
                        // Broad fur-trimmed shoulder mantle (40 x 7 x 14)
                        .texOffs(0, 84).addBox(-20.0F, -3.0F, -7.0F, 40.0F, 7.0F, 14.0F)
                        // Heavy trailing wine-and-charcoal back coat (24 x 28 x 4)
                        .texOffs(112, 84).addBox(-12.0F, 4.0F, 4.0F, 24.0F, 28.0F, 4.0F)
                        // Side coat drapes
                        .texOffs(172, 84).addBox(-13.0F, 4.0F, -6.0F, 4.0F, 24.0F, 10.0F)
                        .texOffs(204, 84).addBox(9.0F, 4.0F, -6.0F, 4.0F, 24.0F, 10.0F),
                PartPose.offset(0.0F, -21.0F, 0.0F));

        // Dorsal iron-and-wood reliquary cage mounted on the Collector's upper back.
        PartDefinition reliquaryCage = torso.addOrReplaceChild("reliquary_cage",
                CubeListBuilder.create()
                        .texOffs(0, 124).addBox(-8.0F, -14.0F, 0.0F, 16.0F, 22.0F, 12.0F),
                PartPose.offset(0.0F, -16.0F, 6.0F));

        // Hinged cage door that swings open during PHASE_TRANSITION and remains open in Phase II.
        reliquaryCage.addOrReplaceChild("cage_door",
                CubeListBuilder.create()
                        .texOffs(60, 124).addBox(0.0F, -12.0F, 0.0F, 14.0F, 18.0F, 2.0F),
                PartPose.offset(-7.0F, 0.0F, 12.0F));

        // Cracked bronze bell suspended inside the reliquary cage with glowing solstice core.
        reliquaryCage.addOrReplaceChild("cage_bell",
                CubeListBuilder.create()
                        .texOffs(96, 124).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 10.0F, 8.0F)
                        .texOffs(132, 124).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, -11.0F, 6.0F));

        // Ceremonial iron chains and small bronze bells draped across the chest.
        torso.addOrReplaceChild("chains",
                CubeListBuilder.create()
                        .texOffs(156, 124).addBox(-9.0F, -6.0F, -2.0F, 18.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, -10.0F, -5.5F));

        // Right arm and forearm wielding the long curved Winter Scythe.
        PartDefinition rightArm = torso.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 164).addBox(-5.5F, -2.0F, -3.5F, 7.0F, 14.0F, 7.0F),
                PartPose.offset(-12.5F, -19.0F, 0.0F));

        PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm",
                CubeListBuilder.create()
                        .texOffs(32, 164).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        // Greataxe (part kept as "scythe" for the shared animations): haft, iron collar, broad blade and back spike.
        rightForearm.addOrReplaceChild("scythe",
                CubeListBuilder.create()
                        .texOffs(124, 164).addBox(-1.0F, -34.0F, -1.0F, 2.0F, 54.0F, 2.0F)
                        .texOffs(136, 164).addBox(-2.0F, -36.0F, -2.5F, 4.0F, 6.0F, 5.0F)
                        .texOffs(156, 164).addBox(-1.0F, -40.0F, -17.0F, 2.0F, 16.0F, 14.0F)
                        .texOffs(156, 204).addBox(-1.0F, -37.0F, 1.0F, 2.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 11.0F, -1.0F));

        // Left arm and forearm for bell-tolling and pawn-invocation gestures.
        PartDefinition leftArm = torso.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(60, 164).addBox(-1.5F, -2.0F, -3.5F, 7.0F, 14.0F, 7.0F),
                PartPose.offset(12.5F, -19.0F, 0.0F));

        leftArm.addOrReplaceChild("left_forearm",
                CubeListBuilder.create()
                        .texOffs(92, 164).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        // Heavy legs and iron-shod winter boots reaching down to y = 24.0.
        PartDefinition rightLeg = pelvis.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 200).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F),
                PartPose.offset(-4.5F, 0.0F, 0.0F));

        rightLeg.addOrReplaceChild("right_boot",
                CubeListBuilder.create()
                        .texOffs(28, 200).addBox(-3.5F, 0.0F, -4.5F, 7.0F, 12.0F, 8.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition leftLeg = pelvis.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(60, 200).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F),
                PartPose.offset(4.5F, 0.0F, 0.0F));

        leftLeg.addOrReplaceChild("left_boot",
                CubeListBuilder.create()
                        .texOffs(88, 200).addBox(-3.5F, 0.0F, -4.5F, 7.0F, 12.0F, 8.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(LastGiftCollectorRenderState state) {
        super.setupAnim(state);

        // Apply baked keyframe animations
        this.idleAnim.apply(state.idleAnimationState, state.ageInTicks);
        this.idleAlternateAnim.apply(state.idleAlternateAnimationState, state.ageInTicks);
        this.walkAnim.apply(state.walkAnimationState, state.ageInTicks);
        this.appearAnim.apply(state.appearAnimationState, state.ageInTicks);
        this.jumpFromSleighAnim.apply(state.jumpFromSleighAnimationState, state.ageInTicks);
        this.fallAnim.apply(state.fallAnimationState, state.ageInTicks);
        this.landAnim.apply(state.landAnimationState, state.ageInTicks);
        this.intimidateAnim.apply(state.intimidateAnimationState, state.ageInTicks);
        this.attackScythePrepAnim.apply(state.attackScythePrepAnimationState, state.ageInTicks);
        this.attackScytheExecAnim.apply(state.attackScytheExecAnimationState, state.ageInTicks);
        this.attackHollowBellPrepAnim.apply(state.attackHollowBellPrepAnimationState, state.ageInTicks);
        this.attackHollowBellImpactAnim.apply(state.attackHollowBellImpactAnimationState, state.ageInTicks);
        this.attackPawnsAnim.apply(state.attackPawnsAnimationState, state.ageInTicks);
        this.attackSolsticeJumpAnim.apply(state.attackSolsticeJumpAnimationState, state.ageInTicks);
        this.attackCastAnim.apply(state.attackCastAnimationState, state.ageInTicks);
        this.attackSpinAnim.apply(state.attackSpinAnimationState, state.ageInTicks);
        this.attackSlamAnim.apply(state.attackSlamAnimationState, state.ageInTicks);
        this.rageAnim.apply(state.rageAnimationState, state.ageInTicks);
        this.recoveryAnim.apply(state.recoveryAnimationState, state.ageInTicks);
        this.phaseTransitionAnim.apply(state.phaseTransitionAnimationState, state.ageInTicks);
        this.hurtAnim.apply(state.hurtAnimationState, state.ageInTicks);
        this.deathAnim.apply(state.deathAnimationState, state.ageInTicks);

        // Keep the dorsal reliquary cage door wide open in Phase II
        if (state.cageOpen && state.bossState != LastGiftCollector.BossState.PHASE_TRANSITION) {
            this.cageDoor.yRot -= 105.0F * Mth.DEG_TO_RAD;
        }

        if (state.bossState == LastGiftCollector.BossState.DEATH_CINEMATIC
                || state.bossState == LastGiftCollector.BossState.DEAD) {
            return;
        }

        // Procedural stride and inertia when walking in combat
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float pos = state.walkAnimationPos;
        if (speed > 0.01F
                && (state.bossState == LastGiftCollector.BossState.PHASE_ONE
                    || state.bossState == LastGiftCollector.BossState.PHASE_TWO)) {
            float legSwing = Mth.cos(pos * 0.55F) * 0.65F * speed;
            this.rightLeg.xRot += legSwing;
            this.leftLeg.xRot -= legSwing;
            this.rightBoot.xRot += Math.max(0.0F, -legSwing * 0.45F);
            this.leftBoot.xRot += Math.max(0.0F, legSwing * 0.45F);
            this.rightArm.xRot -= legSwing * 0.25F;
            this.leftArm.xRot += legSwing * 0.35F;
            this.mantle.xRot += speed * 0.14F;
            this.cageBell.zRot += Mth.sin(pos * 0.55F) * 0.22F * speed;
        }

        // Secondary inertia on beard, chains, and bell in Phase II
        float phaseMultiplier = state.phaseTwo ? 1.85F : 1.0F;
        float t = state.ageInTicks + state.idleSeed;
        this.cageBell.xRot += Mth.sin(t * 0.12F) * 0.06F * phaseMultiplier;
        this.chains.xRot += Mth.cos(t * 0.10F) * 0.04F * phaseMultiplier;
        this.beard.xRot += Mth.sin(t * 0.08F) * 0.03F;

        // Subtle head tracking when in active combat
        if (state.bossState != LastGiftCollector.BossState.INTRO_CINEMATIC) {
            float yawRad = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
            float pitchRad = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
            this.head.yRot += yawRad * 0.55F;
            this.head.xRot += pitchRad * 0.45F;
        }
    }
}

