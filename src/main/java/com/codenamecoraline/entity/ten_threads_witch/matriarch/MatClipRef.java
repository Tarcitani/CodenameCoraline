package com.codenamecoraline.entity.ten_threads_witch.matriarch;

import org.jspecify.annotations.Nullable;

/**
 * The packed value of one synched animation slot: which clip, which variant, and a sequence number so the same clip can be
 * restarted. Bits 0-7: ordinal + 1 (0 = nothing), bits 8-11: variant, bits 12-30: sequence.
 */
public final class MatClipRef {
    private MatClipRef() {}

    public static int pack(@Nullable MatAnim anim, int variant, int sequence) {
        if (anim == null) {
            return (sequence & 0x7FFFF) << 12;
        }
        return ((sequence & 0x7FFFF) << 12) | ((variant & 0xF) << 8) | (anim.ordinal() + 1);
    }

    public static @Nullable MatAnim anim(int packed) {
        int o = packed & 0xFF;
        return o == 0 ? null : MatAnim.byOrdinal(o - 1);
    }

    public static int variant(int packed) {
        return (packed >> 8) & 0xF;
    }

    public static int sequence(int packed) {
        return (packed >> 12) & 0x7FFFF;
    }
}
