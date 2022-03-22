package io.chaofan.sts.intentgraph.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.chaofan.sts.intentgraph.IntentGraphMod;

public class MonsterIntentGraph {

    private static final TextureRegion BOX_TL = new TextureRegion(ImageMaster.KEYWORD_TOP, 32, 32);
    private static final TextureRegion BOX_TM = new TextureRegion(ImageMaster.KEYWORD_TOP, 32, 0, 256, 32);
    private static final TextureRegion BOX_TR = new TextureRegion(ImageMaster.KEYWORD_TOP, 288, 0, 32, 32);
    private static final TextureRegion BOX_ML = new TextureRegion(ImageMaster.KEYWORD_BODY, 32, 32);
    private static final TextureRegion BOX_MM = new TextureRegion(ImageMaster.KEYWORD_BODY, 32, 0, 256, 32);
    private static final TextureRegion BOX_MR = new TextureRegion(ImageMaster.KEYWORD_BODY, 288, 0, 32, 32);
    private static final TextureRegion BOX_BL = new TextureRegion(ImageMaster.KEYWORD_BOT, 32, 32);
    private static final TextureRegion BOX_BM = new TextureRegion(ImageMaster.KEYWORD_BOT, 32, 0, 256, 32);
    private static final TextureRegion BOX_BR = new TextureRegion(ImageMaster.KEYWORD_BOT, 288, 0, 32, 32);

    public float width;
    public float height;
    public MonsterGraphDetail a0;
    public MonsterGraphDetail a1;
    public MonsterGraphDetail a2;

    public void initMonsterGraphDetail() {
        a1 = a0.copyAndApply(a1);
        a2 = a1.copyAndApply(a2);
        a0.init();
        a1.init();
        a2.init();
    }

    public void render(AbstractMonster monster, SpriteBatch sb, int overwriteAscension) {
        MonsterGraphDetail graphDetail;
        int ascensionLevel = overwriteAscension >= 0 ? overwriteAscension : AbstractDungeon.ascensionLevel;
        int renderAscensionLevel;
        if (monster.type == AbstractMonster.EnemyType.NORMAL) {
            graphDetail = ascensionLevel < 2 ? a0 : ascensionLevel < 17 ? a1 : a2;
            renderAscensionLevel = ascensionLevel < 2 ? 0 : ascensionLevel < 17 ? 2 : 17;
        } else if (monster.type == AbstractMonster.EnemyType.ELITE) {
            graphDetail = ascensionLevel < 3 ? a0 : ascensionLevel < 18 ? a1 : a2;
            renderAscensionLevel = ascensionLevel < 3 ? 0 : ascensionLevel < 18 ? 3 : 18;
        } else if (monster.type == AbstractMonster.EnemyType.BOSS) {
            graphDetail = ascensionLevel < 4 ? a0 : ascensionLevel < 19 ? a1 : a2;
            renderAscensionLevel = ascensionLevel < 4 ? 0 : ascensionLevel < 19 ? 4 : 19;
        } else {
            return;
        }

        float scale = Settings.scale;
        float scale32 = 32 * scale;

        float width = graphDetail.width > 0 ? graphDetail.width : this.width;
        float height = graphDetail.height > 0 ? graphDetail.height : this.height;
        float x = monster.hb.cX - scale32 - width * IntentGraphMod.GRID_SIZE * scale / 2;
        float y = Settings.HEIGHT - 80 * scale;

        renderBox(Color.WHITE, x, y, width * IntentGraphMod.GRID_SIZE * scale, (height * IntentGraphMod.GRID_SIZE + 32) * scale, sb);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipHeaderFont, monster.name, x + 20 * scale, y - 20 * scale, Color.WHITE);

        if (renderAscensionLevel > 0) {
            this.renderAscensionLevel(renderAscensionLevel, x + scale32 + (width * IntentGraphMod.GRID_SIZE + 12) * scale, y - 20 * scale, sb);
        }

        graphDetail.render(x + scale32, y - scale32 * 2, sb);
    }

    private void renderAscensionLevel(int renderAscensionLevel, float x, float y, SpriteBatch sb) {
        float scale = Settings.scale;
        float scale64 = 64 * scale;
        FontHelper.renderFontRightTopAligned(sb, FontHelper.tipHeaderFont, Integer.toString(renderAscensionLevel), x, y, Settings.RED_TEXT_COLOR);
        sb.draw(ImageMaster.TP_ASCENSION, x - 54 * scale - FontHelper.layout.width, y - 42 * scale, scale64, scale64);
    }

    private void renderBox(Color color, float x, float y, float w, float h, SpriteBatch sb) {
        sb.setColor(color);
        float scale = Settings.scale;
        float scale32 = 32 * scale;
        sb.draw(BOX_TL, x, y - scale32, scale32, scale32);
        sb.draw(BOX_TM, x + scale32, y - scale32, w, scale32);
        sb.draw(BOX_TR, x + scale32 + w, y - scale32, scale32, scale32);

        sb.draw(BOX_ML, x, y - h - scale32, scale32, h);
        sb.draw(BOX_MM, x + scale32, y - h - scale32, w, h);
        sb.draw(BOX_MR, x + scale32 + w, y - h - scale32, scale32, h);

        sb.draw(BOX_BL, x, y - scale32 * 2 - h, scale32, scale32);
        sb.draw(BOX_BM, x + scale32, y - scale32 * 2 - h, w, scale32);
        sb.draw(BOX_BR, x + scale32 + w, y - scale32 * 2 - h, scale32, scale32);
    }
}
