package com.codenamecoraline.entity.ten_threads_witch;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Anything that belongs to a Witch of Ten Threads encounter: the Matriarch herself, her puppets and the thread hitboxes.
 * Members never attack each other and their blows never hurt each other (zero friendly fire).
 */
public interface TenThreadsEncounterMember {
    /** UUID of the Matriarch this member belongs to ({@code null} for the Matriarch herself or an orphan). */
    @Nullable UUID getOwnerWitchUuid();
}
