package io.chaofan.sts.chaofanmod.relics;

import basemod.abstracts.CustomRelic;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import io.chaofan.sts.chaofanmod.cards.modifiers.StrikeMod;
import io.chaofan.sts.chaofanmod.utils.TextureLoader;

import static io.chaofan.sts.chaofanmod.ChaofanMod.getImagePath;
import static io.chaofan.sts.chaofanmod.ChaofanMod.makeId;

public class Stool extends CustomRelic {
    public static final String ID = makeId("relic.Stool");

    private static final Texture IMG = TextureLoader.getTexture(getImagePath("relics/stool.png"));
    private static final Texture OUTLINE = TextureLoader.getTexture(getImagePath("relics/outline/stool.png"));

    public Stool() {
        super(ID, IMG, OUTLINE, RelicTier.UNCOMMON, LandingSound.CLINK);
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public void atBattleStartPreDraw() {
        flash();
        for (AbstractCard card : AbstractDungeon.player.drawPile.group) {
            if (card.type == AbstractCard.CardType.ATTACK && !CardModifierManager.hasModifier(card, StrikeMod.ID)) {
                CardModifierManager.addModifier(card, new StrikeMod());
            }
        }
    }
}
