# CanopyBreaker Combat & Visual Improvements
## Implementation Plan

**Date:** 2026-10-29  
**Priority:** HIGH  
**Estimated Time:** 2-3 hours total  

---

## Summary of Changes

| # | Issue | Category | File | Fix | Time |
|----|-------|----------|------|-----|------|
| **1** | Porrete inclinado (rotação errada) | Visual | CanopyBreakerAnimations.java | Rotação `right_hand` na pose CARRY | 15 min |
| **2** | Shield break (defensável) | Combat | CanopyBreaker.java | Novo ataque SHIELD_BREAK + dano anti-escudo | 45 min |
| **3** | Mais ataques (gorila) | Combat | CanopyBreaker.java + Animations | GRAB_THROW + POUND (duplo) | 60 min |
| **4** | Climbing melhorado | AI | CanopyBreaker.java + TreeNav.java | Aumentar frequência de subida + pulos | 30 min |

---

## Issue #1: Visual — Porrete Inclinado (15 min)

### Problem
The club (Deadroot Club) is always tilted toward the Canopy Breaker. It should be held at the end and rotated to the opposite side.

### Root Cause
In `CanopyBreakerAnimations.java` line 78, the CARRY pose has:
```java
private static final Pose CARRY = Pose.of("right_upper_arm -62 0 0", "right_forearm -45 0 0", "right_hand 58 0 0", ...);
```

The `right_hand 58 0 0` rotates the club (attached to the right hand) to point toward the Canopy Breaker's body. It should point away (opposite rotation).

### Solution

**File:** `src/main/java/com/codenamecoraline/client/canopy/CanopyBreakerAnimations.java`

**Current (line 78):**
```java
private static final Pose CARRY = Pose.of("right_upper_arm -62 0 0", "right_forearm -45 0 0", "right_hand 58 0 0",
        "left_upper_arm 20 0 -6", "left_forearm -30 0 0", "chest 14 0 0", "head -12 0 0");
```

**Fixed:**
```java
private static final Pose CARRY = Pose.of("right_upper_arm -62 0 0", "right_forearm -45 0 0", "right_hand -122 0 0",  // ← Changed from 58 to -122
        "left_upper_arm 20 0 -6", "left_forearm -30 0 0", "chest 14 0 0", "head -12 0 0");
```

**Why:** The club is now rotated 180° away from the body. The hand grips at the handle end, club head points opposite.

### Validation
- [ ] Compile: `./gradlew compileJava`
- [ ] Visual check: `./gradlew runCanopyBreakerTest` (watch CARRY pose)
- [ ] Test in game: Summon boss, verify club grip and angle

**Gain:** Club now looks correctly held and rotated away from the body.

---

## Issue #2: Combat — Shield Break (45 min)

### Problem
Players can block all attacks with a shield indefinitely. The Canopy Breaker should have an attack that breaks shields (pierces Unbreakable shield property or deals massive knockback to break stamina).

### Solution

Add a new attack type `SHIELD_BREAK` that deals extra damage to shields and applies knockback/stagger effect.

**File:** `src/main/java/com/codenamecoraline/entity/canopy/CanopyBreaker.java`

#### Step 1: Add new attack enum (line 146)

**Current:**
```java
public enum Attack { NONE, TRUNK_SLAM, CANOPY_SWEEP, BRUTE_LUNGE, CANOPY_DROP, BACKHAND, INTIMIDATE }
```

**New:**
```java
public enum Attack { NONE, TRUNK_SLAM, CANOPY_SWEEP, BRUTE_LUNGE, CANOPY_DROP, BACKHAND, SHIELD_BREAK, INTIMIDATE }
```

#### Step 2: Add balance constants (after line 122)

```java
public static final int SHIELD_BREAK_PREP = 16;
public static final int SHIELD_BREAK_EXEC = 14;
public static final int SHIELD_BREAK_HIT_TICK = 8;
public static final int SHIELD_BREAK_RECOVER = 20;
public static final float SHIELD_BREAK_DAMAGE = 12.0F;  // High damage to shields
public static final double SHIELD_BREAK_REACH = 3.8D;
public static final double SHIELD_BREAK_KNOCKBACK = 1.2D;  // Strong knockback
```

#### Step 3: Add shield damage logic in `strike()` method

**File:** Look for the `strike()` method (around line 1580) and add shield-breaking logic:

```java
private void strike(ServerLevel level, LivingEntity victim, float damage, double knock) {
    // Check if victim is holding a shield
    if (victim.isUsingItem() && victim.getUseItem().getItem() instanceof ShieldItem) {
        // Shield break: ignore blocking and add extra knockback
        damage *= 1.5F;  // 50% damage amplification on shields
        knock *= 1.5D;    // 50% more knockback
        victim.hurtServer(level, this.damageSources().mobAttack(this), damage);
        // Force shield cooldown or damage
        victim.setElytraFlying(false);  // Cancel glide if active
    } else {
        victim.hurtServer(level, this.damageSources().mobAttack(this), damage);
    }
    
    Vec3 away = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
    if (away.lengthSqr() > 1.0E-4D) {
        away = away.normalize();
        victim.knockback(knock, -away.x, -away.z);
    }
}
```

#### Step 4: Add SHIELD_BREAK to attack selection (find `selectAttack()` method)

Add this condition to favor SHIELD_BREAK when the player is blocking:

```java
// Somewhere in selectAttack() logic:
if (victim.isUsingItem() && victim.getUseItem().getItem() instanceof ShieldItem) {
    // Player is shielding: prioritize shield break
    if (this.canUseAttack(Attack.SHIELD_BREAK)) {
        this.initiateAttack(Attack.SHIELD_BREAK);
        return;
    }
}
```

#### Step 5: Add animation for SHIELD_BREAK in CanopyBreakerAnimations

**File:** `src/main/java/com/codenamecoraline/client/canopy/CanopyBreakerAnimations.java`

Add a new animation enum value `SHIELD_BREAK` and its frames. This is a **vertical slam** from the side:

```java
// In the DEFINITIONS.put(...) section, add:
DEFINITIONS.put(CanopyAnim.SHIELD_BREAK, AnimationDefinition.Builder.withLength(1.0F)
    .addAnimation("root", new AnimationChannel(
            AnimationChannel.Targets.POSITION,
            new Keyframe(0.0F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.4F, KeyframeAnimations.posVec(0, 2, 0.8), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.5F, KeyframeAnimations.posVec(0, 3, 1.2), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(1.0F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)))
    .build());
```

### Testing
- [ ] Compile: `./gradlew compileJava`
- [ ] GameTest: `./gradlew runCanopyBreakerTest`
- [ ] In-game: Summon boss with shield, verify shield break attack triggers and breaks block

---

## Issue #3: Combat — New Attacks (Gorilla Style) (60 min)

### Problem
Only melee attacks. Should have **grab & throw** and **double pound** to match gorilla behavior.

### New Attacks

#### 3A: GRAB_THROW — Grab + Projectile Throw

Grab the player and throw them away (no projectile, pure knockback).

**Balance:**
```java
public static final int GRAB_THROW_PREP = 18;
public static final int GRAB_THROW_EXEC = 16;
public static final int GRAB_HIT_TICK = 8;
public static final int GRAB_THROW_RECOVER = 24;
public static final float GRAB_DAMAGE = 8.0F;
public static final double GRAB_REACH = 4.0D;
public static final double GRAB_THROW_FORCE = 1.8D;  // Strong horizontal knockback
```

**Logic:** On hit, apply damage + massive knockback (enough to send player flying 8+ blocks).

#### 3B: POUND — Double-fisted ground slam

Two-handed pound into the ground, shaking nearby players.

**Balance:**
```java
public static final int POUND_PREP = 20;
public static final int POUND_EXEC = 12;
public static final int POUND_HIT_TICK = 8;
public static final int POUND_RECOVER = 20;
public static final float POUND_DAMAGE = 10.0F;
public static final double POUND_RADIUS = 4.0D;
public static final double POUND_KNOCKBACK = 0.8D;  // Wide area, less directional
```

**Logic:** Affects all entities in radius (similar to SLAM but with both hands).

### Implementation

1. Add to `Attack` enum
2. Add timing constants
3. Add to animation system
4. Add attack selection logic (gorilla-like pattern: single swipes, then grab, then pound)
5. Add damage/knockback logic
6. Test in GameTest

### Validation
- [ ] All new attacks compile
- [ ] Animations play smoothly
- [ ] Damage and knockback work as intended
- [ ] GameTests pass
- [ ] Boss uses new attacks in combat rotation

---

## Issue #4: AI — Climbing & Jumping (30 min)

### Problem
Boss rarely climbs trees and doesn't jump enough for a primate.

### Solution

#### 4A: Increase tree climbing frequency

**File:** `src/main/java/com/codenamecoraline/entity/canopy/CanopyBreaker.java`

Find the `considerRoute()` method and increase the chance to climb:

**Current (approx line 1591-1605):**
```java
boolean elevated = dy >= 3.5D;
boolean flank = this.getStage() >= 2 && this.cooldowns[Attack.CANOPY_DROP.ordinal()] <= 0 && dist >= 6.0D && dist <= 20.0D && dy < 3.5D && this.random.nextInt(this.getStage() >= 3 ? 40 : 70) == 0;
```

**Improved:**
```java
boolean elevated = dy >= 3.5D;
// Increased chances: Stage 1: 1/40, Stage 2: 1/30, Stage 3: 1/20 (more aggressive climbing in later stages)
boolean flank = this.getStage() >= 2 && this.cooldowns[Attack.CANOPY_DROP.ordinal()] <= 0 && dist >= 6.0D && dist <= 20.0D && dy < 3.5D && this.random.nextInt(this.getStage() >= 3 ? 20 : 30) == 0;
boolean climb = this.getStage() >= 2 && this.random.nextInt(this.getStage() >= 3 ? 30 : 50) == 0;  // NEW: General climbing chance
```

#### 4B: Increase jump frequency while airborne

**File:** `src/main/java/com/codenamecoraline/entity/canopy/CanopyBreaker.java`

Find jump/airborne logic and reduce cooldown:

**Current:**
```java
if (this.jumpTicks <= 0 && random.nextInt(60) == 0) {
    // Jump
}
```

**Improved:**
```java
if (this.jumpTicks <= 0 && random.nextInt(40) == 0) {  // 1/40 chance per tick instead of 1/60
    // Jump
}
```

#### 4C: Add stray jumping when idle

Add a behavior: if the boss is stuck or idle for too long, jump up to reposition:

```java
if (this.stuckTicks > 40 && this.random.nextInt(20) == 0) {
    this.setDeltaMovement(this.getDeltaMovement().add(0, 0.5D, 0));  // Jump up
    this.stuckTicks = 0;  // Reset stuck counter
}
```

### Testing
- [ ] Compile
- [ ] GameTest: Verify boss climbs on-demand
- [ ] In-game: Watch boss climb trees more frequently
- [ ] Verify jumping happens naturally, not spammily

---

## Implementation Order

### Phase 1: Visual Fix (Immediate)
1. Fix club rotation in CanopyBreakerAnimations (15 min)
2. Compile + validate
3. **Commit:** `feat: fix canopy breaker club rotation (visual)`

### Phase 2: Combat - Shield Break (Next)
1. Add SHIELD_BREAK attack enum
2. Add balance constants
3. Implement shield detection logic
4. Add animation
5. **Commit:** `feat: add shield-breaking attack to canopy breaker`

### Phase 3: Combat - New Attacks (Next)
1. Add GRAB_THROW + POUND enums
2. Add all balance constants
3. Add animation frames
4. Implement attack logic + selection
5. **Commit:** `feat: add grab/throw and pound attacks (gorilla-style)`

### Phase 4: AI Improvements (Last)
1. Increase climbing frequency
2. Increase jump frequency
3. Add stuck-escape jumping
4. **Commit:** `feat: improve canopy breaker ai - more climbing and jumping`

### Phase 5: Validation
1. Full GameTest suite: `./gradlew runCanopyBreakerTest`
2. In-game testing
3. Balance adjustments as needed

---

## Testing Checklist

### Unit Level
- [ ] CanopyBreakerAnimations compiles (pose rotations valid)
- [ ] CanopyBreaker.java compiles (new attacks, enums valid)
- [ ] No missing animation frames

### GameTest Level
- [ ] 18/18 tests pass (existing + new shield-break/grab tests)
- [ ] Club rotation verified in animation trace
- [ ] Shield break triggers on shielded players
- [ ] New attacks play animations without glitches
- [ ] AI climbing and jumping work as intended

### In-Game Level
- [ ] Club held correctly at correct angle
- [ ] Shield-breaking attack visibly breaks shields
- [ ] Grab/throw has clear knockback
- [ ] Pound attack affects area targets
- [ ] Boss actively climbs trees (every 30-50 ticks)
- [ ] Boss jumps frequently (every 40 ticks or less)
- [ ] No animation blending issues
- [ ] No physics glitches (stuck in walls, infinite jumps)

---

## References

- **Main boss file:** `src/main/java/com/codenamecoraline/entity/canopy/CanopyBreaker.java` (2192 lines)
- **Animation file:** `src/main/java/com/codenamecoraline/client/canopy/CanopyBreakerAnimations.java` (355 lines)
- **Model file:** `src/main/java/com/codenamecoraline/client/canopy/CanopyBreakerModel.java` (94 lines)
- **TreeNav (climbing):** `src/main/java/com/codenamecoraline/entity/canopy/TreeNav.java` (280 lines)

---

## Questions Before Implementation?

1. **Shield break damage scaling?** (1.5x current or flat 12 damage?)
2. **Grab throw distance?** (8+ blocks or 6 blocks?)
3. **Pound animation?** (Both fists together or one at a time?)
4. **Climb chance per stage?** (Stage 1: rare, Stage 2: medium, Stage 3: very frequent?)

Otherwise, ready to implement Phase 1 now! 🚀
