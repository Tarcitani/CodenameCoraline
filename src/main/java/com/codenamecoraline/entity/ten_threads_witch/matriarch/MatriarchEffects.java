package com.codenamecoraline.entity.ten_threads_witch.matriarch;

import com.codenamecoraline.entity.ten_threads_witch.AttackRunner;
import com.codenamecoraline.entity.ten_threads_witch.MatriarchWitchEntity;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * The particles and sounds of the animation events. Server side only: each call sends one burst that every tracking client
 * receives, so an event is seen once by everyone. Positions are approximate anchors of the body (hands, chest, feet) turned
 * with her body yaw; the exact joints live on the client, which only draws.
 */
public final class MatriarchEffects {
    private MatriarchEffects() {}

    /** Where an anchor is in the world, for the Matriarch facing her current body yaw. */
    public static Vec3 anchor(MatriarchWitchEntity w, MatEvent.Anchor anchor, ServerLevel level) {
        double yaw = Math.toRadians(w.yBodyRot);
        Vec3 fwd = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        Vec3 left = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
        Vec3 base = w.position();
        return switch (anchor) {
            case HAND_L -> base.add(left.scale(0.32D)).add(fwd.scale(0.15D)).add(0.0D, 0.42D, 0.0D);
            case HAND_R -> base.add(left.scale(-0.32D)).add(fwd.scale(0.15D)).add(0.0D, 0.42D, 0.0D);
            case HANDS -> base.add(fwd.scale(0.25D)).add(0.0D, 0.5D, 0.0D);
            case CHEST -> base.add(0.0D, 0.65D, 0.0D);
            case HEAD -> base.add(0.0D, 1.0D, 0.0D);
            case FEET -> base.add(0.0D, 0.05D, 0.0D);
            case FRONT -> base.add(fwd.scale(0.9D)).add(0.0D, 0.3D, 0.0D);
            case FOCUS -> {
                Entity e = w.getAnimFocus(level);
                yield e != null ? e.position().add(0.0D, e.getBbHeight() * 0.6D, 0.0D) : base.add(fwd.scale(2.0D)).add(0.0D, 0.6D, 0.0D);
            }
            case ARENA -> w.getCenter();
        };
    }

    public static void fire(MatriarchWitchEntity w, ServerLevel level, MatEvent event) {
        Vec3 p = anchor(w, event.anchor, level);
        Vec3 hands = anchor(w, MatEvent.Anchor.HANDS, level);
        switch (event) {
            case THREAD_MANIFEST -> {
                burst(level, AttackRunner.THREAD_DUST, p, 16, 0.25D, 0.02D);
                sound(level, w, ModSounds.WITCH_THREAD_TENSION.get(), 0.9F, 1.4F);
            }
            case THREAD_FORM -> line(level, AttackRunner.THREAD_DUST, hands, p, 0.5D);
            case THREAD_TENSION -> {
                burst(level, AttackRunner.THREAD_DUST, p, 4, 0.08D, 0.0D);
                sound(level, w, ModSounds.WITCH_THREAD_TENSION.get(), 0.7F, 1.7F);
            }
            case THREAD_RELEASE -> burst(level, AttackRunner.THREAD_DUST, p, 8, 0.2D, 0.0D);
            case THREAD_SNAP -> {
                burst(level, AttackRunner.CRIMSON_DUST, p, 10, 0.15D, 0.05D);
                sound(level, w, ModSounds.WITCH_THREAD_SNAP.get(), 1.0F, 1.2F);
            }
            case THREAD_VANISH -> burst(level, ParticleTypes.END_ROD, p, 6, 0.3D, 0.01D);
            case ATTACK_WHOOSH -> sound(level, w, SoundEvents.PLAYER_ATTACK_SWEEP, 0.8F, 1.4F);
            case ATTACK_RELEASE -> {
                line(level, AttackRunner.THREAD_DUST, hands, p, 0.6D);
                sound(level, w, ModSounds.WITCH_THREAD_TENSION.get(), 1.0F, 1.9F);
            }
            case IMPACT -> {
                burst(level, ParticleTypes.CRIT, p, 10, 0.4D, 0.1D);
                sound(level, w, SoundEvents.MACE_SMASH_GROUND, 0.8F, 1.3F);
            }
            case BLOCK -> {
                burst(level, ParticleTypes.CRIT, p.lerp(w.position().add(0.0D, 0.6D, 0.0D), 0.6D), 6, 0.15D, 0.1D);
                sound(level, w, SoundEvents.SHIELD_BLOCK.value(), 0.8F, 1.5F);
            }
            case BARRIER -> {
                Vec3 c = w.position().lerp(p, 0.5D).add(0.0D, 0.3D, 0.0D);
                for (int i = 0; i < 12; i++) {
                    double a = Math.PI * 2.0D * i / 12.0D;
                    level.sendParticles(AttackRunner.THREAD_DUST, c.x + Math.cos(a) * 0.5D, c.y + Math.sin(a) * 0.5D, c.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                sound(level, w, ModSounds.WITCH_THREAD_TENSION.get(), 1.0F, 1.1F);
            }
            case DODGE -> {
                burst(level, ParticleTypes.CLOUD, p, 4, 0.2D, 0.01D);
                sound(level, w, SoundEvents.WOOL_STEP, 0.8F, 1.2F);
            }
            case FALL_IMPACT -> {
                burst(level, ParticleTypes.POOF, p, 8, 0.4D, 0.02D);
                sound(level, w, SoundEvents.GENERIC_SMALL_FALL, 1.0F, 0.8F);
            }
            case CLAP -> sound(level, w, SoundEvents.WOOL_HIT, 1.0F, 1.6F);
            case CACKLE -> sound(level, w, ModSounds.WITCH_VOICE.get(), 0.7F, 1.3F);
            case PHASE_SURGE -> {
                burst(level, AttackRunner.CRIMSON_DUST, p, 30, 0.6D, 0.08D);
                sound(level, w, ModSounds.WITCH_THREAD_SNAP.get(), 1.6F, 0.6F);
            }
            case INTRO_THREADS -> {
                Vec3 c = w.getCenter();
                for (int i = 0; i < 16; i++) {
                    double a = Math.PI * 2.0D * i / 16.0D;
                    for (int y = 0; y < 6; y++) {
                        level.sendParticles(AttackRunner.THREAD_DUST, c.x + Math.cos(a) * 4.0D, c.y + y * 0.6D, c.z + Math.sin(a) * 4.0D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    }
                }
                sound(level, w, ModSounds.WITCH_THREAD_TENSION.get(), 1.6F, 0.6F);
            }
            case INTRO_REVEAL -> burst(level, ParticleTypes.END_ROD, p, 10, 0.4D, 0.02D);
            case POWER_BURST -> {
                Vec3 c = w.position();
                for (int i = 0; i < 24; i++) {
                    double a = Math.PI * 2.0D * i / 24.0D;
                    level.sendParticles(AttackRunner.SPOOL_DUST, c.x + Math.cos(a) * 2.5D, c.y + 0.4D, c.z + Math.sin(a) * 2.5D, 1, 0.0D, 0.05D, 0.0D, 0.0D);
                }
                sound(level, w, ModSounds.WITCH_THREAD_TENSION.get(), 1.4F, 0.9F);
            }
            case DISINTEGRATE -> {
                burst(level, AttackRunner.THREAD_DUST, p, 14, 0.35D, 0.04D);
                burst(level, ParticleTypes.ASH, p, 10, 0.35D, 0.01D);
            }
            case VANISH -> {
                burst(level, ParticleTypes.END_ROD, p, 24, 0.5D, 0.05D);
                sound(level, w, ModSounds.WITCH_THREAD_SNAP.get(), 1.4F, 0.5F);
            }
            case PUPPET_GUARD -> line(level, AttackRunner.SPOOL_DUST, hands, p, 0.4D);
        }
    }

    private static void burst(ServerLevel level, ParticleOptions particle, Vec3 p, int count, double spread, double speed) {
        level.sendParticles(particle, p.x, p.y, p.z, count, spread, spread, spread, speed);
    }

    private static void line(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 to, double step) {
        double len = from.distanceTo(to);
        for (double k = 0.0D; k <= len; k += step) {
            Vec3 q = from.lerp(to, len < 1.0E-3D ? 0.0D : k / len);
            level.sendParticles(particle, q.x, q.y, q.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void sound(ServerLevel level, MatriarchWitchEntity w, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, w.getX(), w.getY(), w.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }
}
