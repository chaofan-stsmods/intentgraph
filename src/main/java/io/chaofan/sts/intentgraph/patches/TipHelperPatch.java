package io.chaofan.sts.intentgraph.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.TipHelper;

@SpirePatch(clz = TipHelper.class, method = "renderPowerTips")
public class TipHelperPatch {
    public static boolean rendered = false;
    public static float xMin;
    public static float xMax;
    public static float y;

    @SpireInsertPatch(rloc = 222 - 171, localvars = {"originalY", "x"})
    public static void postfix(float originalY, float x) {
        rendered = true;
        float x1 = ReflectionHacks.getPrivateStatic(TipHelper.class, "drawX");
        y = originalY;
        xMin = Math.min(x, x1);
        xMax = Math.max(x, x1) + 324.0F * Settings.scale;
    }
}
