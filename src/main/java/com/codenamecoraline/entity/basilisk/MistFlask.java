package com.codenamecoraline.entity.basilisk;

import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** The crafted flask leaves the same hazardous mist as a Basilisk, including danger to its thrower. */
public final class MistFlask extends ThrowableItemProjectile {
    public MistFlask(EntityType<? extends MistFlask> type, Level level) { super(type, level); }
    public MistFlask(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.MIST_FLASK.get(), owner, level, stack);
    }
    @Override protected Item getDefaultItem() { return ModItems.MIST_FLASK.get(); }
    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level() instanceof ServerLevel server) {
            server.addFreshEntity(WitherMistCloud.create(server, this.position(),
                    this.getOwner() instanceof LivingEntity owner ? owner : null));
            this.playSound(SoundEvents.SPLASH_POTION_BREAK, 0.9F, 0.7F);
            this.discard();
        }
    }
}
