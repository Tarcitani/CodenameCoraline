package com.codenamecoraline.client.redcap;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.redcap.Redcap;
import com.codenamecoraline.entity.redcap.RedcapVariant;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Hunched goblin body from docs/design/redcap.md: forward-leaning torso, head jutting ahead of the
 * shoulders, big pointed ears, a crimson hood with a floppy tip, long clawed arms, bent legs in rusted
 * iron boots and a pikestaff in the left hand.
 *
 * Units are model pixels (16 per block), y down, front towards -z. Feet rest on y = 24.
 * Baked leans live on child parts. Every frame starts from the baked pose; setupAnim then layers the
 * procedural walk, breathing and secondary motion, and the keyframe animations of RedcapAnimations.
 * Part names are unique (side-prefixed) because keyframe animations look bones up by name.
 */
public final class RedcapModel extends EntityModel<RedcapRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "redcap"), "main");

    private static final float BODY_LEAN = 0.7F;
    // Resting rotations of the weapons (radians), solved against the posed hand. The weapon keyframe channels
    // in RedcapAnimations are relative to these.
    static final float PIKE_REST_X = 1.122F;
    static final float PIKE_REST_Z = 0.780F;
    static final float DAGGER_REST_X = 1.279F;
    static final float DAGGER_REST_Z = 1.393F;
    private static final long ROCK_RELEASE_MS = Redcap.ROCK_RELEASE_TICKS * 50L;

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart[] claws;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;
    private final ModelPart rightBoot;
    private final ModelPart leftBoot;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart[] capTip;
    private final ModelPart clothFront;
    private final ModelPart clothBack;

    private final ModelPart pike;
    private final ModelPart dagger;
    private final ModelPart rock;

    private final KeyframeAnimation pikeThrust;
    private final KeyframeAnimation daggerSlash;
    private final KeyframeAnimation clawSwipeRight;
    private final KeyframeAnimation clawSwipeLeft;
    private final KeyframeAnimation rockThrow;
    private final KeyframeAnimation sniff;
    private final KeyframeAnimation lookAround;
    private final KeyframeAnimation sharpen;
    private final KeyframeAnimation capCheck;

    public RedcapModel(ModelPart root) {
        super(root);
        var part = root.createPartLookup();
        this.head = part.apply("head");
        this.body = part.apply("body");
        this.rightArm = part.apply("right_arm");
        this.leftArm = part.apply("left_arm");
        this.rightForearm = part.apply("right_forearm");
        this.leftForearm = part.apply("left_forearm");
        this.claws = new ModelPart[]{part.apply("right_claw_0"), part.apply("right_claw_1"), part.apply("right_claw_2"),
                part.apply("left_claw_0"), part.apply("left_claw_1"), part.apply("left_claw_2")};
        this.rightLeg = part.apply("right_leg");
        this.leftLeg = part.apply("left_leg");
        this.rightShin = part.apply("right_shin");
        this.leftShin = part.apply("left_shin");
        this.rightBoot = part.apply("right_boot");
        this.leftBoot = part.apply("left_boot");
        this.rightEar = part.apply("right_ear");
        this.leftEar = part.apply("left_ear");
        this.capTip = new ModelPart[]{part.apply("tip_1"), part.apply("tip_2"), part.apply("tip_3"), part.apply("tip_4")};
        this.clothFront = part.apply("cloth_front");
        this.clothBack = part.apply("cloth_back");

        this.pike = part.apply("pike");
        this.dagger = part.apply("dagger");
        this.rock = part.apply("rock");

        this.pikeThrust = RedcapAnimations.PIKE_THRUST.bake(root);
        this.daggerSlash = RedcapAnimations.DAGGER_SLASH.bake(root);
        this.clawSwipeRight = RedcapAnimations.CLAW_SWIPE_RIGHT.bake(root);
        this.clawSwipeLeft = RedcapAnimations.CLAW_SWIPE_LEFT.bake(root);
        this.rockThrow = RedcapAnimations.ROCK_THROW.bake(root);
        this.sniff = RedcapAnimations.SNIFF.bake(root);
        this.lookAround = RedcapAnimations.LOOK_AROUND.bake(root);
        this.sharpen = RedcapAnimations.SHARPEN.bake(root);
        this.capCheck = RedcapAnimations.CAP_CHECK.bake(root);
    }

    private static String sidePrefix(int side) {
        return side < 0 ? "right_" : "left_";
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        addBody(root);
        addHead(root);
        // The free right arm reaches far forward; the pike arm hangs closer to the side.
        PartDefinition rightHand = addArm(root, "right_arm", -1, -0.35F, -0.6F);
        addRock(rightHand);
        PartDefinition leftHand = addArm(root, "left_arm", 1, -0.15F, -0.35F);
        addPike(leftHand);
        addDagger(leftHand);
        addLeg(root, "right_leg", -1);
        addLeg(root, "left_leg", 1);

        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void addBody(PartDefinition root) {
        // Pivot at the hips; the torso grows upwards and leans forward.
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 52).addBox(-3.0F, -7.0F, -2.0F, 6.0F, 7.0F, 4.0F)
                        .texOffs(20, 52).addBox(-2.5F, -7.2F, 1.2F, 5.0F, 3.0F, 2.0F)
                        .texOffs(34, 52).addBox(-3.5F, -2.0F, -2.5F, 7.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 14.0F, 1.5F, BODY_LEAN, 0.0F, 0.0F));

        // Tattered loincloth flaps, counter-rotated so they hang straight down.
        body.addOrReplaceChild("cloth_front",
                CubeListBuilder.create().texOffs(58, 52).addBox(-2.5F, 0.0F, -0.25F, 5.0F, 6.0F, 0.5F),
                PartPose.offsetAndRotation(0.0F, -1.0F, -2.6F, -BODY_LEAN, 0.0F, 0.0F));
        body.addOrReplaceChild("cloth_back",
                CubeListBuilder.create().texOffs(70, 52).addBox(-3.0F, 0.0F, -0.25F, 6.0F, 7.0F, 0.5F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 2.6F, -BODY_LEAN, 0.0F, 0.0F));
    }

    private static void addHead(PartDefinition root) {
        // Pivot at the neck, in front of the leaning shoulders; the face juts forward.
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -6.5F, -5.5F, 7.0F, 7.0F, 6.0F)
                        // brow ridge shading the eyes
                        .texOffs(0, 14).addBox(-3.5F, -4.5F, -6.5F, 7.0F, 1.0F, 1.0F)
                        // long hooked nose
                        .texOffs(28, 0).addBox(-0.5F, -3.5F, -7.0F, 1.0F, 3.0F, 1.5F)
                        .texOffs(28, 5).addBox(-0.5F, -1.0F, -7.5F, 1.0F, 1.0F, 1.0F)
                        // gold nose ring
                        .texOffs(34, 0).addBox(-0.5F, 0.0F, -7.2F, 1.0F, 0.8F, 0.4F)
                        // lower fangs jutting up from the mouth corners
                        .texOffs(34, 2).addBox(-2.1F, -1.5F, -5.9F, 0.6F, 1.0F, 0.6F)
                        .texOffs(34, 2).addBox(1.5F, -1.5F, -5.9F, 0.6F, 1.0F, 0.6F)
                        // grizzled beard hanging from the chin, tapering to a point
                        .texOffs(0, 17).addBox(-3.0F, -0.5F, -6.0F, 6.0F, 4.5F, 2.0F)
                        .texOffs(16, 17).addBox(-2.0F, 4.0F, -5.8F, 4.0F, 3.0F, 1.5F)
                        .texOffs(28, 9).addBox(-1.0F, 7.0F, -5.6F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 9.0F, -4.0F));

        addEar(head, "right_ear", -1);
        addEar(head, "left_ear", 1);

        // "hat" holds the crimson hood: open at the front so the face stays visible.
        PartDefinition hood = head.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-4.0F, -8.0F, -6.0F, 8.0F, 1.5F, 7.0F)
                        .texOffs(30, 26).addBox(-4.0F, -6.5F, 0.5F, 8.0F, 7.5F, 1.5F)
                        .texOffs(0, 36).addBox(-5.0F, -6.5F, -4.5F, 1.5F, 7.0F, 5.0F)
                        .texOffs(0, 36).mirror().addBox(3.5F, -6.5F, -4.5F, 1.5F, 7.0F, 5.0F),
                PartPose.ZERO);

        // Floppy tip of the cap, starting at the back of the crown and folding backwards segment by segment.
        PartDefinition tip1 = hood.addOrReplaceChild("tip_1",
                CubeListBuilder.create().texOffs(52, 0).addBox(-2.5F, -4.0F, -2.0F, 5.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, 1.5F, -1.65F, 0.0F, 0.0F));
        PartDefinition tip2 = tip1.addOrReplaceChild("tip_2",
                CubeListBuilder.create().texOffs(52, 9).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, -0.5F, 0.0F, 0.25F));
        PartDefinition tip3 = tip2.addOrReplaceChild("tip_3",
                CubeListBuilder.create().texOffs(72, 0).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, -0.6F, 0.0F, 0.2F));
        tip3.addOrReplaceChild("tip_4",
                CubeListBuilder.create().texOffs(72, 6).addBox(-1.0F, -2.0F, -1.5F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -1.5F, 0.0F, -0.8F, 0.0F, 0.0F));
    }

    /** Big pointed ear sticking out sideways, tip raised and swept back. side: -1 right, 1 left. */
    private static void addEar(PartDefinition head, String name, int side) {
        // Three steps narrowing to the tip (top edge straight), plus a gold ring under the lobe.
        CubeListBuilder cubes = CubeListBuilder.create().mirror(side > 0);
        if (side < 0) {
            cubes.texOffs(38, 0).addBox(-4.0F, -1.75F, -0.5F, 4.0F, 3.5F, 1.0F)
                    .texOffs(38, 5).addBox(-6.5F, -1.75F, -0.4F, 2.5F, 2.5F, 0.8F)
                    .texOffs(46, 5).addBox(-8.5F, -1.75F, -0.3F, 2.0F, 1.5F, 0.6F)
                    .texOffs(34, 5).addBox(-3.0F, 1.75F, -0.4F, 1.0F, 1.0F, 0.8F);
        } else {
            cubes.texOffs(38, 0).addBox(0.0F, -1.75F, -0.5F, 4.0F, 3.5F, 1.0F)
                    .texOffs(38, 5).addBox(4.0F, -1.75F, -0.4F, 2.5F, 2.5F, 0.8F)
                    .texOffs(46, 5).addBox(6.5F, -1.75F, -0.3F, 2.0F, 1.5F, 0.6F)
                    .texOffs(34, 5).addBox(2.0F, 1.75F, -0.4F, 1.0F, 1.0F, 0.8F);
        }
        head.addOrReplaceChild(name, cubes,
                PartPose.offsetAndRotation(3.5F * side, -3.0F, -2.5F, 0.0F, -0.45F * side, -0.3F * side));
    }

    /**
     * Long, thin arm bent at the elbow with a clawed hand. Returns the hand.
     * side: -1 right, 1 left. upperLean and elbowBend: forward x rotations of the upper arm and forearm.
     */
    private static PartDefinition addArm(PartDefinition root, String name, int side, float upperLean, float elbowBend) {
        String prefix = sidePrefix(side);
        PartDefinition shoulder = root.addOrReplaceChild(name, CubeListBuilder.create(),
                PartPose.offset(4.0F * side, 8.8F, -2.8F));
        PartDefinition upper = shoulder.addOrReplaceChild(prefix + "upper_arm",
                CubeListBuilder.create().texOffs(88, 0).mirror(side > 0)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.5F, 2.0F),
                PartPose.rotation(upperLean, 0.0F, -0.3F * side));
        PartDefinition forearm = upper.addOrReplaceChild(prefix + "forearm",
                CubeListBuilder.create().texOffs(96, 0).mirror(side > 0)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, elbowBend, 0.0F, 0.1F * side));
        PartDefinition hand = forearm.addOrReplaceChild(prefix + "hand",
                CubeListBuilder.create().texOffs(104, 0).mirror(side > 0)
                        .addBox(-1.25F, 0.0F, -1.25F, 2.5F, 2.0F, 2.5F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        for (int i = -1; i <= 1; i++) {
            hand.addOrReplaceChild(prefix + "claw_" + (i + 1),
                    CubeListBuilder.create().texOffs(114, 0).addBox(-0.3F, 0.0F, -0.3F, 0.6F, 2.5F, 0.6F),
                    PartPose.offsetAndRotation(0.8F * i, 1.8F, -0.6F, -0.5F, 0.0F, 0.0F));
        }
        return hand;
    }

    /** Pikestaff held in the hand, with a broad leaf blade. */
    private static void addPike(PartDefinition hand) {
        hand.addOrReplaceChild("pike",
                CubeListBuilder.create()
                        .texOffs(124, 26).addBox(-0.5F, -17.0F, -0.5F, 1.0F, 21.0F, 1.0F)
                        .texOffs(88, 28).addBox(-0.8F, -2.0F, -0.8F, 1.6F, 3.0F, 1.6F)
                        .texOffs(96, 28).addBox(-1.0F, -18.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                        .texOffs(104, 28).addBox(-1.5F, -24.0F, -0.25F, 3.0F, 6.0F, 0.5F)
                        .texOffs(112, 28).addBox(-1.0F, -26.0F, -0.25F, 2.0F, 2.0F, 0.5F),
                // Butt in the hand, blade up and forward (solved against the posed hand at rest: 64.3, 0, 44.7
                // degrees). RedcapAnimations pike channels are relative to this pose.
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, PIKE_REST_X, 0.0F, PIKE_REST_Z));
    }

    /** Short dagger gripped like the pike, blade forward and up. Part of the "dagger" variant only. */
    private static void addDagger(PartDefinition hand) {
        hand.addOrReplaceChild("dagger",
                CubeListBuilder.create()
                        .texOffs(64, 36).addBox(-0.6F, -1.5F, -0.6F, 1.2F, 3.5F, 1.2F)
                        .texOffs(70, 36).addBox(-0.8F, 2.0F, -0.8F, 1.6F, 1.0F, 1.6F)
                        .texOffs(78, 36).addBox(-1.8F, -2.5F, -0.6F, 3.6F, 1.0F, 1.2F)
                        .texOffs(64, 42).addBox(-0.9F, -9.5F, -0.25F, 1.8F, 7.0F, 0.5F)
                        .texOffs(72, 42).addBox(-0.5F, -11.0F, -0.25F, 1.0F, 1.5F, 0.5F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, DAGGER_REST_X, 0.0F, DAGGER_REST_Z));
    }

    /** Rock held in the right hand while winding up a throw (claws variant). Hidden otherwise. */
    private static void addRock(PartDefinition hand) {
        hand.addOrReplaceChild("rock",
                CubeListBuilder.create().texOffs(88, 36).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 2.5F, -0.5F));
    }

    /** Bent leg: thigh forward, shin back, leather-wrapped ankle and a rusted iron boot. side: -1 right, 1 left. */
    private static void addLeg(PartDefinition root, String name, int side) {
        String prefix = sidePrefix(side);
        PartDefinition hip = root.addOrReplaceChild(name, CubeListBuilder.create(),
                PartPose.offset(2.0F * side, 14.3F, 1.0F));
        PartDefinition thigh = hip.addOrReplaceChild(prefix + "thigh",
                CubeListBuilder.create().texOffs(88, 10).mirror(side > 0)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.5F, 3.0F),
                PartPose.rotation(-0.8F, 0.0F, -0.12F * side));
        PartDefinition shin = thigh.addOrReplaceChild(prefix + "shin",
                CubeListBuilder.create().texOffs(100, 10).mirror(side > 0)
                        .addBox(-1.25F, 0.0F, -1.25F, 2.5F, 4.0F, 2.5F)
                        .texOffs(110, 10).addBox(-1.6F, 2.0F, -1.6F, 3.2F, 2.0F, 3.2F),
                PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, 1.25F, 0.0F, 0.0F));
        shin.addOrReplaceChild(prefix + "boot",
                CubeListBuilder.create()
                        .texOffs(88, 18).addBox(-2.0F, 0.0F, -3.5F, 4.0F, 3.0F, 5.0F)
                        .texOffs(106, 18).addBox(-2.3F, -0.8F, -2.3F, 4.6F, 1.0F, 4.6F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.45F, 0.0F, 0.12F * side));
    }

    @Override
    public void setupAnim(RedcapRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks + state.idleSeed;
        float walk = state.walkAnimationPos * 0.85F;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        RedcapVariant variant = state.variant;
        // The arms belong to the attack or the throw while it plays, so the walk stops swinging them.
        boolean attacking = state.attackAnimationState.isStarted() || state.clawLeftAnimationState.isStarted()
                || state.rockThrowAnimationState.isStarted();
        float armFreedom = attacking ? 0.0F : 1.0F;

        // Weapons and the rock follow the variant; the rock only shows until it leaves the hand.
        this.pike.visible = variant == RedcapVariant.SPEAR;
        this.dagger.visible = variant == RedcapVariant.DAGGER;
        this.rock.visible = state.rockThrowAnimationState.isStarted()
                && state.rockThrowAnimationState.getTimeInMillis(state.ageInTicks) < ROCK_RELEASE_MS;

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.animateWalk(walk, speed, armFreedom, variant != RedcapVariant.CLAWS);
        this.animateBreathing(age, 1.0F - speed);
        this.animateSecondaryMotion(age, walk, speed);

        switch (variant) {
            case SPEAR -> this.pikeThrust.apply(state.attackAnimationState, state.ageInTicks);
            case DAGGER -> this.daggerSlash.apply(state.attackAnimationState, state.ageInTicks);
            case CLAWS -> {
                this.clawSwipeRight.apply(state.attackAnimationState, state.ageInTicks);
                this.clawSwipeLeft.apply(state.clawLeftAnimationState, state.ageInTicks);
                this.rockThrow.apply(state.rockThrowAnimationState, state.ageInTicks);
            }
        }
        this.sniff.apply(state.sniffAnimationState, state.ageInTicks);
        this.lookAround.apply(state.lookAroundAnimationState, state.ageInTicks);
        this.sharpen.apply(state.sharpenAnimationState, state.ageInTicks);
        this.capCheck.apply(state.capCheckAnimationState, state.ageInTicks);
    }

    /**
     * Goblin scuttle: short, quick steps with high knees, the body bobbing and rolling from foot to foot
     * and hunching lower the faster it runs. The free arm swings loose; the pike arm swings less and
     * plants the staff.
     */
    private void animateWalk(float walk, float speed, float armFreedom, boolean weaponInLeftHand) {
        float swing = Mth.cos(walk);
        float lift = Mth.sin(walk);

        this.rightLeg.xRot = swing * 0.8F * speed;
        this.leftLeg.xRot = -swing * 0.8F * speed;
        // The knee folds while its foot travels forward, so the boot clears the ground.
        this.rightShin.xRot += Math.max(0.0F, lift) * 0.9F * speed;
        this.leftShin.xRot += Math.max(0.0F, -lift) * 0.9F * speed;
        this.rightBoot.xRot -= Math.max(0.0F, lift) * 0.5F * speed;
        this.leftBoot.xRot -= Math.max(0.0F, -lift) * 0.5F * speed;

        float bob = Math.abs(lift) * 1.1F * speed;
        float roll = lift * 0.07F * speed;
        float hunch = 0.25F * speed;
        this.body.xRot += hunch;
        this.body.zRot += roll;
        this.body.y -= bob;
        // Head and arms are not children of the body: move them with the extra hunch (neck and
        // shoulders swing forward and down around the hips) and the bob.
        this.head.y += 5.3F * hunch - bob;
        this.head.z -= 4.7F * hunch;
        this.head.zRot -= roll * 1.5F;
        this.head.xRot += Mth.sin(walk * 2.0F) * 0.05F * speed;
        for (ModelPart arm : new ModelPart[]{this.rightArm, this.leftArm}) {
            arm.y += 4.4F * hunch - bob;
            arm.z -= 5.5F * hunch;
        }

        float armSwing = 0.75F * speed * armFreedom;
        this.rightArm.xRot = -swing * armSwing;
        // A hand holding a weapon swings less; a bare claw swings as freely as the other arm.
        this.leftArm.xRot = swing * armSwing * (weaponInLeftHand ? 0.45F : 0.9F);
        this.rightArm.zRot = 0.15F * speed * armFreedom;
        this.leftArm.zRot = -0.08F * speed * armFreedom;
        // Dangling forearms lag behind the swing.
        this.rightForearm.xRot += Mth.sin(walk + 0.9F) * 0.35F * speed * armFreedom;
        this.leftForearm.xRot += Mth.sin(walk + Mth.PI + 0.9F) * (weaponInLeftHand ? 0.15F : 0.35F) * speed * armFreedom;

        this.clothFront.xRot -= hunch + Math.abs(lift) * 0.35F * speed;
        this.clothBack.xRot -= hunch - Math.abs(swing) * 0.25F * speed;
    }

    /** Breathing, a slow sway of the long arms and flexing claws; calm is 1 when standing still. */
    private void animateBreathing(float age, float calm) {
        float breath = Mth.sin(age * 0.09F);
        this.body.xRot += breath * 0.025F;
        this.head.y += breath * 0.3F;
        this.head.xRot += breath * 0.03F;
        this.rightArm.y += breath * 0.25F;
        this.leftArm.y += breath * 0.25F;

        float sway = Mth.sin(age * 0.067F) * 0.05F * calm;
        this.rightArm.zRot += sway;
        this.leftArm.zRot -= sway;
        this.rightForearm.xRot += Mth.sin(age * 0.05F) * 0.06F * calm;

        for (int i = 0; i < this.claws.length; i++) {
            this.claws[i].xRot += Mth.sin(age * 0.15F + i * 1.3F) * 0.12F;
        }
    }

    /** Ears flick now and then and bounce with each step; the floppy cap tip trails behind the head. */
    private void animateSecondaryMotion(float age, float walk, float speed) {
        float bounce = Mth.sin(walk * 2.0F) * 0.12F * speed;
        this.rightEar.zRot += twitch(age, 87.0F, 0.0F) * 0.35F + bounce;
        this.leftEar.zRot -= twitch(age, 113.0F, 40.0F) * 0.35F + bounce;

        float flop = Mth.sin(walk * 2.0F - 0.8F) * speed;
        float drift = Mth.sin(age * 0.05F);
        for (int i = 0; i < this.capTip.length; i++) {
            ModelPart tip = this.capTip[i];
            tip.xRot += flop * (0.08F + 0.07F * i) + drift * 0.02F * (i + 1);
            tip.zRot += Mth.sin(age * 0.05F - i * 0.6F) * 0.03F * (i + 1);
        }
    }

    /** A quick 0-1-0 pulse lasting 5 ticks, once every period ticks. */
    private static float twitch(float age, float period, float offset) {
        float t = (age + offset) % period;
        return t < 5.0F ? Mth.sin(t / 5.0F * Mth.PI) : 0.0F;
    }
}
