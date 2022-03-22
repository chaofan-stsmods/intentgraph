package io.chaofan.sts.intentgraph;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

public class ModInitializer {

    @SuppressWarnings("unused")
    public static void initialize() {
        BaseMod.subscribe((PostInitializeSubscriber) () -> {
            IntentGraphMod.initialize();
            IntentGraphMod.instance.receivePostInitialize();
        });
    }
}
