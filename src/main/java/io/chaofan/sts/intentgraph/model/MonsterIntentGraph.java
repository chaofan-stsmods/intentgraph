package io.chaofan.sts.intentgraph.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.chaofan.sts.intentgraph.GraphLibrary;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.patches.TipHelperPatch;

import java.util.*;

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
    private transient GraphLibrary graphLibrary;

    public float width;
    public float height;
    public Map<Integer, MonsterGraphDetail> graphs = new HashMap<>();
    public List<MonsterGraphDetail> graphList = new ArrayList<>();
    public MonsterGraphDetail a0;
    public MonsterGraphDetail a1;
    public MonsterGraphDetail a2;

    public void initMonsterGraphDetail(AbstractMonster.EnemyType type) {
        if (initialized) {
            return;
        }
        initialized = true;

        if (a0 != null) {
            graphs.put(0, a0);
        }

        if (type == AbstractMonster.EnemyType.NORMAL) {
            if (a1 != null) {
                graphs.put(2, a1);
            }
            if (a2 != null) {
                graphs.put(17, a2);
            }
        } else if (type == AbstractMonster.EnemyType.ELITE) {
            if (a1 != null) {
                graphs.put(3, a1);
            }
            if (a2 != null) {
                graphs.put(18, a2);
            }
        } else if (type == AbstractMonster.EnemyType.BOSS) {
            if (a1 != null) {
                graphs.put(4, a1);
            }
            if (a2 != null) {
                graphs.put(19, a2);
            }
        }

        Collections.reverse(graphList);
        int last = -1;
        int insertLocation = graphList.size();
        for (int i = 0; i <= 20; i++) {
            MonsterGraphDetail detail = graphs.get(i);
            if (detail != null) {
                if (detail.extend == null && !detail.overwrite && last >= 0) {
                    detail.extend = String.valueOf(last);
                }
                detail.ascensionLevel = i;
                detail.id = String.valueOf(i);
                detail.condition = i == 0 ? "true" : "ascension >= " + i;
                last = i;
                graphList.add(insertLocation, detail);
            }
        }

        graphLibrary = new GraphLibrary(graphList);
    }

    @Override
    public MonsterIntentGraph clone() {
        Gson gson = new Gson();
        MonsterIntentGraph newInstance = gson.fromJson(gson.toJson(this), MonsterIntentGraph.class);
        if (this.initialized) {
            newInstance.initialized = true;
            newInstance.graphLibrary = new GraphLibrary(newInstance.graphList);
        }
        return newInstance;
    }

    public GraphLibrary getGraphLibrary() {
        return graphLibrary;
    }

    public void render(AbstractMonster monster, SpriteBatch sb) {
        if (!initialized) {
            initMonsterGraphDetail(monster.type);
        }

        String name = monster.name;
        float y = Settings.HEIGHT - 80 * Settings.scale;
        float x = monster.hb.cX - 32 * Settings.scale - width * IntentGraphMod.GRID_SIZE * Settings.scale / 2;
        float bottom = y - 64 * Settings.scale - height * IntentGraphMod.GRID_SIZE * Settings.scale;
        float boxWidth = width * IntentGraphMod.GRID_SIZE * Settings.scale + 64 * Settings.scale;
        float right = x + boxWidth;
        if (TipHelperPatch.rendered && TipHelperPatch.y > bottom) {
            if (monster.hb.cX * 2 < TipHelperPatch.xMin + TipHelperPatch.xMax && x + boxWidth > TipHelperPatch.xMin) {
                x = TipHelperPatch.xMin - boxWidth;
                if (x < 0) {
                    x = TipHelperPatch.xMax;
                }
            } else if (monster.hb.cX * 2 >= TipHelperPatch.xMin + TipHelperPatch.xMax && x < TipHelperPatch.xMax) {
                x += TipHelperPatch.xMax - x;
                if (x + boxWidth > Settings.WIDTH) {
                    x = TipHelperPatch.xMin - boxWidth;
                }
            }
        }
        if (x < 0) {
            x = 0;
        }
        if (right > Settings.WIDTH) {
            x = Settings.WIDTH - width * IntentGraphMod.GRID_SIZE * Settings.scale - 64 * Settings.scale;
        }

        MonsterGraphDetail graphDetail = graphLibrary.get(monster);
        if (graphDetail == null) {
            return;
        }

        render(sb, Color.WHITE, graphDetail, x, y, name);
    }

    public void render(SpriteBatch sb, Color boxColor, MonsterGraphDetail graphDetail, float x, float y, String name) {
        float scale = Settings.scale;
        float scale32 = 32 * scale;

        float width = graphDetail.width > 0 ? graphDetail.width : this.width;
        float height = graphDetail.height > 0 ? graphDetail.height : this.height;

        renderBox(Settings.TOP_PANEL_SHADOW_COLOR, x + 9 * Settings.scale, y - 14 * Settings.scale, width * IntentGraphMod.GRID_SIZE * scale, (height * IntentGraphMod.GRID_SIZE + 32) * scale, sb);
        renderBox(boxColor, x, y, width * IntentGraphMod.GRID_SIZE * scale, (height * IntentGraphMod.GRID_SIZE + 32) * scale, sb);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipHeaderFont, name, x + 20 * scale, y - 20 * scale, Color.WHITE);

        sb.setColor(Color.WHITE);
        int renderAscensionLevel = graphDetail.ascensionLevel;
        if (renderAscensionLevel > 0) {
            renderAscensionLevel(renderAscensionLevel, x + scale32 + (width * IntentGraphMod.GRID_SIZE + 12) * scale, y - 20 * scale, sb);
        }

        graphDetail.render(x + scale32, y - scale32 * 2, sb);
    }

    public static void renderAscensionLevel(int renderAscensionLevel, float x, float y, SpriteBatch sb) {
        float scale = Settings.scale;
        float scale64 = 64 * scale;
        FontHelper.renderFontRightTopAligned(sb, FontHelper.tipHeaderFont, Integer.toString(renderAscensionLevel), x, y, Settings.RED_TEXT_COLOR);
        sb.draw(ImageMaster.TP_ASCENSION, x - 54 * scale - FontHelper.layout.width, y - 42 * scale, scale64, scale64);
    }

    public static void renderBox(Color color, float x, float y, float w, float h, SpriteBatch sb) {
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
