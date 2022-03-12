package io.chaofan.sts.chaofanmod.cards;

import basemod.ReflectionHacks;
import basemod.abstracts.CustomCard;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.unlock.UnlockTracker;

import static com.megacrit.cardcrawl.core.CardCrawlGame.languagePack;
import static io.chaofan.sts.chaofanmod.ChaofanMod.*;

public abstract class CardBase extends CustomCard {
    public CardBase(String id, String img, int cost, CardType type, CardColor color, CardRarity rarity, CardTarget target) {
        super(id, getCardStrings(id).NAME, img, cost, getCardStrings(id).DESCRIPTION, type, color, rarity, target);
        isCostModified = false;
        isCostModifiedForTurn = false;
        isDamageModified = false;
        isBlockModified = false;
        isMagicNumberModified = false;
    }

    public CardBase(String id, int cost, CardType type, CardColor color, CardRarity rarity, CardTarget target) {
        this(id, getCardImagePath(id, type), cost, type, color, rarity, target);
    }

    public static String makeCardId(String id) {
        return makeId("card." + id);
    }

    public static CardStrings getCardStrings(String id) {
        return languagePack.getCardStrings(id);
    }

    private static String getCardTypeString(CardType type) {
        switch (type) {
            case ATTACK:
                return "attack";
            case POWER:
                return "power";
            default:
                return "skill";
        }
    }

    private static String getCardImagePath(String id, CardType type) {
        String imagePath = getImagePath("cards/" + invertId(id).substring("card.".length()) + ".png");
        if (CardBase.class.getResource("/" + imagePath) == null) {
            return getImagePath("cards/missing_" + getCardTypeString(type) + ".png");
        }

        return imagePath;
    }

    @Override
    public void loadCardImage(String img) {
        String betaArtName = getBetaArtName();
        if (betaArtName != null) {
            super.loadCardImage(getImagePath("cards/" + betaArtName));
            // beta art
            ReflectionHacks.setPrivate(this, AbstractCard.class, "jokePortrait",
                    ReflectionHacks.getPrivate(this, AbstractCard.class, "portrait"));
        }

        super.loadCardImage(img);
        if (betaArtName == null) {
            // We don't have beta art
            ReflectionHacks.setPrivate(this, AbstractCard.class, "jokePortrait",
                    ReflectionHacks.getPrivate(this, AbstractCard.class, "portrait"));
        }
    }

    @Override
    protected Texture getPortraitImage() {
        String textureImg = this.textureImg;
        if (UnlockTracker.betaCardPref.getBoolean(this.cardID, false) || Settings.PLAYTESTER_ART_MODE) {
            String betaArtName = getBetaArtName();
            if (betaArtName != null) {
                textureImg = getImagePath("cards/" + betaArtName);
            }
        }

        if (textureImg == null) {
            return null;
        } else {
            int endingIndex = textureImg.lastIndexOf("/");
            String newPath = textureImg.substring(0, endingIndex) + "/portrait" + textureImg.substring(endingIndex);

            Texture portraitTexture;
            try {
                portraitTexture = ImageMaster.loadImage(newPath);
            } catch (Exception var5) {
                portraitTexture = null;
            }

            return portraitTexture;
        }
    }

    protected String getBetaArtName() {
        return null;
    }
}
