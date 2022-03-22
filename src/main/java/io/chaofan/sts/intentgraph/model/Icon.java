package io.chaofan.sts.intentgraph.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.chaofan.sts.intentgraph.IntentGraphMod;

public class Icon {
    public float x;
    public float y;
    public AbstractMonster.Intent type;
    public int damageIndex;
    public int percentage;
    public int limit;
    public int attackCount;
    public String attackCountString;

    public void render(MonsterGraphDetail graphDetail, float x, float y, SpriteBatch sb) {
        sb.setColor(Color.WHITE);

        float scale = Settings.scale;
        float iconX = x + this.x * scale * IntentGraphMod.GRID_SIZE + 8 * scale;
        float iconY = y - (this.y + 1) * scale * IntentGraphMod.GRID_SIZE + 8 * scale;
        boolean isAttack = renderIconImage(graphDetail, sb, this, iconX, iconY);

        BitmapFont font = FontHelper.cardEnergyFont_L;
        font.getData().setScale(0.5f);

        if (isAttack) {
            Damage damage = graphDetail.damages[damageIndex];
            String damageString = damage.string != null ? damage.string :
                    (damage.max <= damage.min ? String.valueOf(damage.min) : String.format("%d~%d", damage.min, damage.max));
            if (attackCount > 1) {
                damageString += "x" + (attackCountString != null ? attackCountString : attackCount);
            }
            FontHelper.renderFontLeftTopAligned(sb, font, damageString, iconX - 2 * scale, iconY + 16 * scale, Color.WHITE);
        }

        if (percentage > 0) {
            FontHelper.renderFontLeftTopAligned(sb, font, percentage + "%", iconX - 2 * scale, iconY + 62 * scale, Color.WHITE);
        }

        if (limit >= 1) {
            FontHelper.renderFontLeftTopAligned(sb, font, "-", iconX + 45 * scale, iconY + 55 * scale, Color.WHITE);
            FontHelper.renderFontLeftTopAligned(sb, font, "<" + limit, iconX + 45 * scale, iconY + 62 * scale, Color.WHITE);
        }

        font.getData().setScale(1);
    }

    private boolean renderIconImage(MonsterGraphDetail graphDetail, SpriteBatch sb, Icon icon, float iconX, float iconY) {
        float scale = Settings.scale;
        float scale64 = scale * 64;
        float scale4 = scale * 4;
        float scale56 = scale * 56;
        boolean isAttack = false;
        switch (icon.type) {
            case ATTACK:
                sb.draw(getAttackIntent(graphDetail.damages[icon.damageIndex].max * icon.attackCount), iconX + scale4, iconY + scale4, scale56, scale56);
                isAttack = true;
                break;
            case ATTACK_BUFF:
                sb.draw(ImageMaster.INTENT_BUFF, iconX, iconY, scale64, scale64);
                sb.draw(getAttackIntent(graphDetail.damages[icon.damageIndex].max * icon.attackCount), iconX + scale4, iconY + scale4, scale56, scale56);
                isAttack = true;
                break;
            case ATTACK_DEBUFF:
                sb.draw(ImageMaster.INTENT_DEBUFF, iconX, iconY, scale64, scale64);
                sb.draw(getAttackIntent(graphDetail.damages[icon.damageIndex].max * icon.attackCount), iconX + scale4, iconY + scale4, scale56, scale56);
                isAttack = true;
                break;
            case ATTACK_DEFEND:
                sb.draw(ImageMaster.INTENT_DEFEND, iconX, iconY, scale64, scale64);
                sb.draw(getAttackIntent(graphDetail.damages[icon.damageIndex].max * icon.attackCount), iconX + scale4, iconY + scale4, scale56, scale56);
                isAttack = true;
                break;
            case DEFEND:
                sb.draw(ImageMaster.INTENT_DEFEND, iconX, iconY, scale64, scale64);
                break;
            case DEFEND_BUFF:
                sb.draw(ImageMaster.INTENT_DEFEND_BUFF, iconX, iconY, scale64, scale64);
                break;
            case DEFEND_DEBUFF:
                sb.draw(ImageMaster.INTENT_DEBUFF, iconX, iconY, scale64, scale64);
                sb.draw(ImageMaster.INTENT_DEFEND, iconX, iconY, scale64, scale64);
                break;
            case DEBUFF:
                sb.draw(ImageMaster.INTENT_DEBUFF, iconX, iconY, scale64, scale64);
                break;
            case STRONG_DEBUFF:
                sb.draw(ImageMaster.INTENT_DEBUFF2, iconX, iconY, scale64, scale64);
                break;
            case BUFF:
                sb.draw(ImageMaster.INTENT_BUFF, iconX, iconY, scale64, scale64);
                break;
            case ESCAPE:
                sb.draw(ImageMaster.INTENT_ESCAPE, iconX, iconY, scale64, scale64);
                break;
            case SLEEP:
                sb.draw(ImageMaster.INTENT_SLEEP, iconX, iconY, scale64, scale64);
                break;
            case STUN:
                sb.draw(ImageMaster.INTENT_STUN, iconX, iconY, scale64, scale64);
                break;
            case UNKNOWN:
                sb.draw(ImageMaster.INTENT_UNKNOWN, iconX, iconY, scale64, scale64);
                sb.draw(ImageMaster.INTENT_UNKNOWN, iconX - 12 * scale, iconY + 6 * scale, scale * 48, scale * 48);
                sb.draw(ImageMaster.INTENT_UNKNOWN, iconX + 28 * scale, iconY + 6 * scale, scale * 48, scale * 48);
                break;
            case MAGIC:
                sb.draw(ImageMaster.INTENT_MAGIC, iconX, iconY, scale64, scale64);
                break;
        }

        return isAttack;
    }

    private Texture getAttackIntent(int damage) {
        if (damage < 5) {
            return ImageMaster.INTENT_ATK_TIP_1;
        } else if (damage < 10) {
            return ImageMaster.INTENT_ATK_TIP_2;
        } else if (damage < 15) {
            return ImageMaster.INTENT_ATK_TIP_3;
        } else if (damage < 20) {
            return ImageMaster.INTENT_ATK_TIP_4;
        } else if (damage < 25) {
            return ImageMaster.INTENT_ATK_TIP_5;
        } else {
            return damage < 30 ? ImageMaster.INTENT_ATK_TIP_6 : ImageMaster.INTENT_ATK_TIP_7;
        }
    }
}
