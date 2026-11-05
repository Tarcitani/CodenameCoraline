package com.codenamecoraline.client.ten_threads_witch;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatRig;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatriarchAnimator;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatriarchModel;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatriarchRenderState;
import com.codenamecoraline.client.ten_threads_witch.matriarch.MatriarchThreadLayer;
import com.codenamecoraline.entity.ten_threads_witch.EncounterPhase;
import com.codenamecoraline.entity.ten_threads_witch.MatriarchWitchEntity;
import com.codenamecoraline.entity.ten_threads_witch.PuppetEntity;
import com.codenamecoraline.entity.ten_threads_witch.PuppetState;
import com.codenamecoraline.entity.ten_threads_witch.matriarch.MatriarchAnimDirector;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Client renderer of the Matriarch. Every frame it feeds her {@link MatriarchAnimator} (one per entity, kept while the
 * entity lives) with what the server synched and what her motion shows, and hands the composed pose to her
 * {@link MatriarchModel}; the eyes layer adds the glow and {@link MatriarchThreadLayer} the threads from her fingertips.
 * Rendering never decides anything: no target, no attack, no event.
 */
public final class MatriarchRenderer extends MobRenderer<MatriarchWitchEntity, MatriarchRenderState, MatriarchModel> {
    public static final String ID = MatRig.ID;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/entity/" + ID + ".png");

    private final Map<MatriarchWitchEntity, MatriarchAnimator> animators = new WeakHashMap<>();
    private final Map<MatriarchWitchEntity, float[]> hangingCache = new WeakHashMap<>();
    private final MatriarchAnimator.Input input = new MatriarchAnimator.Input();

    public MatriarchRenderer(EntityRendererProvider.Context context) {
        super(context, new MatriarchModel(context.bakeLayer(PuppetModel.layer(ID))), 0.4F);
        this.addLayer(new TenThreadsEyesLayer<>(this, ID));
        this.addLayer(new MatriarchThreadLayer(this));
    }

    @Override
    public MatriarchRenderState createRenderState() {
        return new MatriarchRenderState();
    }

    @Override
    public Identifier getTextureLocation(MatriarchRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F; // the death is her own cinematic, never the vanilla fall onto the side
    }

    @Override
    public void extractRenderState(MatriarchWitchEntity entity, MatriarchRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        EncounterPhase phase = entity.getPhase();
        state.brokenSpools = entity.getBrokenSpoolsBitmask();
        state.blindfoldOff = phase == EncounterPhase.STAGE_11_FURY || phase == EncounterPhase.DEATH_CINEMATIC;
        state.unravel = entity.getUnravel();
        state.bodyYawDegrees = state.bodyRot;
        if (phase == EncounterPhase.DEATH_CINEMATIC) {
            state.hasRedOverlay = false; // a seven-second cinematic, not a red flash
        }
        MatriarchAnimator animator = this.animators.computeIfAbsent(entity, e -> new MatriarchAnimator(e.getId()));
        MatriarchAnimator.Input in = this.input;
        in.now = entity.tickCount + partialTick;
        in.sync = entity.animSync;
        in.vx = entity.getX() - entity.xo;
        in.vy = entity.getY() - entity.yo;
        in.vz = entity.getZ() - entity.zo;
        in.bodyYaw = state.bodyRot;
        in.yawRate = entity.yBodyRot - entity.yBodyRotO;
        in.floating = entity.isAnimFloating();
        in.style = entity.getAnimGait() & 15;
        in.unravel = state.unravel;
        in.concealed = phase == EncounterPhase.DORMANT;
        in.look = this.lookVector(entity, partialTick);
        float[] pose = animator.animate(in);
        System.arraycopy(pose, 0, state.pose, 0, pose.length);
        // where the threads lead
        Entity focus = entity.level().getEntity(entity.getAnimFocusId());
        state.hasFocus = focus != null && focus.isAlive();
        if (state.hasFocus) {
            Vec3 f = focus.getPosition(partialTick).add(0.0D, focus.getBbHeight() * 0.7D, 0.0D).subtract(entity.getPosition(partialTick));
            state.focusX = (float) f.x;
            state.focusY = (float) f.y;
            state.focusZ = (float) f.z;
        }
        this.collectHanging(entity, state, partialTick);
    }

    /** From her eyes to what she watches (the player, a puppet, her own raised hand), in world axes; null when nothing. */
    private Vec3 lookVector(MatriarchWitchEntity entity, float partialTick) {
        int mode = entity.getAnimLookMode();
        Vec3 eye = entity.getPosition(partialTick).add(0.0D, entity.getEyeHeight(), 0.0D);
        if (mode == MatriarchAnimDirector.LookMode.THREADS.ordinal()) {
            double yaw = Math.toRadians(entity.yBodyRot);
            Vec3 hand = entity.getPosition(partialTick).add(Math.cos(yaw) * 0.3D - Math.sin(yaw) * 0.25D, 0.55D, Math.sin(yaw) * 0.3D + Math.cos(yaw) * 0.25D);
            return hand.subtract(eye);
        }
        Entity e = entity.level().getEntity(entity.getAnimLookId());
        if (e == null || mode == MatriarchAnimDirector.LookMode.NONE.ordinal()) {
            return null;
        }
        return e.getPosition(partialTick).add(0.0D, e.getEyeHeight(), 0.0D).subtract(eye);
    }

    /** The puppets still hanging over the arena, refreshed twice a second (their threads come from her right hand). */
    private void collectHanging(MatriarchWitchEntity entity, MatriarchRenderState state, float partialTick) {
        float[] cache = this.hangingCache.computeIfAbsent(entity, e -> new float[31]);
        if (entity.tickCount % 10 == 0 || cache[30] == 0.0F) {
            int n = 0;
            for (PuppetEntity p : entity.level().getEntitiesOfClass(PuppetEntity.class, entity.getBoundingBox().inflate(40.0D),
                    q -> q.getOwnerEntityId() == entity.getId() && q.getPuppetState() == PuppetState.SLEEPING_SUSPENDED)) {
                if (n >= 10) {
                    break;
                }
                cache[n * 3] = (float) (p.getX() - entity.getX());
                cache[n * 3 + 1] = (float) (p.getY() + p.getBbHeight() - entity.getY());
                cache[n * 3 + 2] = (float) (p.getZ() - entity.getZ());
                n++;
            }
            cache[30] = n + 0.5F;
        }
        state.hangingCount = (int) cache[30];
        System.arraycopy(cache, 0, state.hanging, 0, 30);
    }
}
