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

import java.util.HashMap;
import java.util.Map;

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

    private boolean initialized = false;
    private final Map<Integer, Integer> lastAvailableAscensions = new HashMap<>();

    public float width;
    public float height;
    public boolean isCommonGraph;
    public Map<Integer, MonsterGraphDetail> graphs = new HashMap<>();
    public MonsterGraphDetail a0;
    public MonsterGraphDetail a1;
    public MonsterGraphDetail a2;

    private void initMonsterGraphDetail(AbstractMonster monster) {
        initialized = true;

        if (a0 != null) {
            graphs.put(0, a0);
        }

        AbstractMonster.EnemyType type = monster.type;
        if (type == AbstractMonster.EnemyType.NORMAL) {
            if (a1 != null) {
                graphs.put(2, a1);
            }
            if (a2 != null) {
                graphs.put(17, a2);
            }
            if (!isCommonGraph) {
                lastAvailableAscensions.put(2, 2);
                lastAvailableAscensions.put(17, 17);
            }
        } else if (type == AbstractMonster.EnemyType.ELITE) {
            if (a1 != null) {
                graphs.put(3, a1);
            }
            if (a2 != null) {
                graphs.put(18, a2);
            }
            if (!isCommonGraph) {
                lastAvailableAscensions.put(3, 3);
                lastAvailableAscensions.put(18, 18);
            }
        } else if (type == AbstractMonster.EnemyType.BOSS) {
            if (a1 != null) {
                graphs.put(4, a1);
            }
            if (a2 != null) {
                graphs.put(19, a2);
            }
            if (!isCommonGraph) {
                lastAvailableAscensions.put(4, 4);
                lastAvailableAscensions.put(19, 19);
            }
        }

        a0 = graphs.get(0);
        if (a0 == null) {
            throw new RuntimeException("intent graph of " + monster.id + " is not present for ascension 0.");
        }
        MonsterGraphDetail last = a0;
        int lastAvailableAscension = 0;
        lastAvailableAscensions.put(0, 0);
        for (int i = 1; i <= 20; i++) {
            MonsterGraphDetail detail = graphs.get(i);
            MonsterGraphDetail next = last.copyAndApply(detail);
            graphs.put(i, next);
            if (next != last) {
                lastAvailableAscension = i;
            }
            if (!lastAvailableAscensions.containsKey(i)) {
                lastAvailableAscensions.put(i, lastAvailableAscension);
            } else {
                lastAvailableAscension = i;
            }
            last = next;
        }

        for (int i = 0; i <= 20; i++) {
            graphs.get(i).init();
        }
    }

    public void render(AbstractMonster monster, SpriteBatch sb, int overwriteAscension) {
        if (!initialized) {
            initMonsterGraphDetail(monster);
        }

        int ascensionLevel = overwriteAscension >= 0 ? overwriteAscension : AbstractDungeon.ascensionLevel;
        Integer renderAscensionLevel = lastAvailableAscensions.get(ascensionLevel);
        if (renderAscensionLevel == null) {
            renderAscensionLevel = ascensionLevel >= 20 ? 20 : 0;
        }
        MonsterGraphDetail graphDetail = graphs.get(renderAscensionLevel);

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
