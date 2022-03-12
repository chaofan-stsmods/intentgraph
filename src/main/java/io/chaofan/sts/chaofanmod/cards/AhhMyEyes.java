package io.chaofan.sts.chaofanmod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static io.chaofan.sts.chaofanmod.cards.CardBase.getCardStrings;
import static io.chaofan.sts.chaofanmod.cards.CardBase.makeCardId;

public class AhhMyEyes extends CustomCard {
    public static final String ID = makeCardId(AhhMyEyes.class.getSimpleName());

    public AhhMyEyes() {
        super(ID, getCardStrings(ID).NAME, new RegionName("green/attack/endless_agony"), 0, getCardStrings(ID).DESCRIPTION, CardType.ATTACK, CardColor.COLORLESS, CardRarity.COMMON, CardTarget.ALL);
    }

    @Override
    public void upgrade() {

    }

    @Override
    public void use(AbstractPlayer abstractPlayer, AbstractMonster abstractMonster) {

    }
}
