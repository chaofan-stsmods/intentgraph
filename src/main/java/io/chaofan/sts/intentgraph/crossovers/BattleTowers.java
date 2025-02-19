package io.chaofan.sts.intentgraph.crossovers;

import BattleTowers.monsters.chess.queen.customintents.IntentEnums;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.model.DamageProvider;
import io.chaofan.sts.intentgraph.model.Icon;

public class BattleTowers {
    public static final Texture queenDrain = ImageMaster.loadImage("battleTowersResources/img/ui/queenDrain.png");

    public static boolean renderIcon(DamageProvider damageProvider, SpriteBatch sb, Icon icon, float x, float y, boolean[] isAttack) {
        if (icon.type == IntentEnums.QUEEN_DRAIN_ATTACK) {
            sb.draw(queenDrain, x + Settings.scale * 4, y + Settings.scale * 4, Settings.scale * 56, Settings.scale * 56);
            isAttack[0] = true;
            return true;
        }
        if (icon.type == AbstractMonster.Intent.STRONG_DEBUFF && "battleTowers:VoodooDoll".equals(IntentGraphMod.visibleGraphMonsterId)) {
            sb.setColor(0.5f, 0, 1, 1);
            sb.draw(ImageMaster.INTENT_DEBUFF2, x, y, Settings.scale * 64, Settings.scale * 64);
            isAttack[0] = true;
            return true;
        }
        return false;
    }
}
