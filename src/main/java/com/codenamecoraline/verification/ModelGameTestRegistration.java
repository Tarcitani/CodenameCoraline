package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Keeps opt-in baked-model suites unloaded on a normal dedicated server. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class ModelGameTestRegistration {
    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        if (Boolean.getBoolean("coraline.babyDragonTests")) DragonModelGameTests.functions(event);
        if (Boolean.getBoolean("coraline.canopyTests")) CanopyBreakerGameTests.functions(event);
        if (Boolean.getBoolean("coraline.crimsonTests")) CrimsonCommanderGameTests.functions(event);
        if (Boolean.getBoolean("coraline.tenThreadsTests")) TenThreadsWitchGameTests.functions(event);
        if (Boolean.getBoolean("coraline.matriarchAnimTests")) MatriarchAnimationGameTests.functions(event);
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        if (Boolean.getBoolean("coraline.babyDragonTests")) DragonModelGameTests.tests(event);
        if (Boolean.getBoolean("coraline.canopyTests")) CanopyBreakerGameTests.tests(event);
        if (Boolean.getBoolean("coraline.crimsonTests")) CrimsonCommanderGameTests.tests(event);
        if (Boolean.getBoolean("coraline.tenThreadsTests")) TenThreadsWitchGameTests.tests(event);
        if (Boolean.getBoolean("coraline.matriarchAnimTests")) MatriarchAnimationGameTests.tests(event);
    }

    private ModelGameTestRegistration() {}
}
