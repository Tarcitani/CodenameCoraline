package com.codenamecoraline.entity.ten_threads_witch.matriarch;

/**
 * Client clocks of the synched slots: when each slot received its current clip, and what it played before (kept for the
 * cross-fade, so a change of clip never jumps). Plain data on the entity, filled from {@code onSyncedDataUpdated}; the
 * client animator reads it every frame. No client class is referenced here.
 */
public final class MatriarchAnimSync {
    public final int[] value = new int[MatSlot.SYNCHED.length];
    public final int[] start = new int[MatSlot.SYNCHED.length];
    public final int[] prevValue = new int[MatSlot.SYNCHED.length];
    public final int[] prevStart = new int[MatSlot.SYNCHED.length];
    /** Client tick at which the slot switched from {@code prevValue} to {@code value}. */
    public final int[] switched = new int[MatSlot.SYNCHED.length];
    /** Client tick at which the floating flag last changed, and its previous value. */
    public int gaitSwitched = -1000;
    public int prevGait = 0;

    public void receive(int slotIndex, int packed, int tick) {
        if (this.value[slotIndex] == packed) {
            return;
        }
        this.prevValue[slotIndex] = this.value[slotIndex];
        this.prevStart[slotIndex] = this.start[slotIndex];
        this.value[slotIndex] = packed;
        this.start[slotIndex] = tick;
        this.switched[slotIndex] = tick;
    }

    public void receiveGait(int oldGait, int tick) {
        this.prevGait = oldGait;
        this.gaitSwitched = tick;
    }
}
