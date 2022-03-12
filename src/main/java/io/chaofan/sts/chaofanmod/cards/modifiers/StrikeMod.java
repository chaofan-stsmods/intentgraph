package io.chaofan.sts.chaofanmod.cards.modifiers;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import static io.chaofan.sts.chaofanmod.ChaofanMod.makeId;

public class StrikeMod extends AbstractCardModifier {
    public static final String ID = makeId("StrikeMod");

    @Override
    public String modifyName(String cardName, AbstractCard card) {
        String strikeName = CardCrawlGame.languagePack.getCardStrings(Strike_Red.ID).NAME;
        String cardNameWithoutUpgrade = cardName;
        if (card.upgraded) {
            int lastPlus = cardNameWithoutUpgrade.lastIndexOf('+');
            if (lastPlus != -1) {
                strikeName += cardNameWithoutUpgrade.substring(lastPlus);
                cardNameWithoutUpgrade = cardNameWithoutUpgrade.substring(0, lastPlus);
            }
        }

        if (Settings.language == Settings.GameLanguage.ENG) {
            int lastSpace = cardNameWithoutUpgrade.lastIndexOf(' ');
            return (lastSpace > 0 ? cardNameWithoutUpgrade.substring(0, lastSpace) : cardNameWithoutUpgrade) + ' ' + strikeName;
        } else if (Settings.language == Settings.GameLanguage.ZHS) {
            if (cardNameWithoutUpgrade.length() <= 2) {
                return cardNameWithoutUpgrade + strikeName;
            } else if (cardNameWithoutUpgrade.length() == 3) {
                return cardNameWithoutUpgrade.substring(0, 2) + strikeName;
            } else {
                return cardNameWithoutUpgrade.substring(0, cardNameWithoutUpgrade.length() - 2) + strikeName;
            }
        }

        return cardName;
    }

    @Override
    public boolean shouldApply(AbstractCard card) {
        return !card.hasTag(AbstractCard.CardTags.STRIKE);
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.tags.add(AbstractCard.CardTags.STRIKE);
        if (AbstractDungeon.player != null) {
            AbstractDungeon.player.hand.applyPowers();
        }
    }

    @Override
    public void onRemove(AbstractCard card) {
        card.tags.remove(AbstractCard.CardTags.STRIKE);
        if (AbstractDungeon.player != null) {
            AbstractDungeon.player.hand.applyPowers();
        }
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new StrikeMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
}
