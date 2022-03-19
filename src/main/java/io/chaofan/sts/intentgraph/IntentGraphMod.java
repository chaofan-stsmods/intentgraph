package io.chaofan.sts.intentgraph;

import basemod.BaseMod;
import basemod.devcommands.ConsoleCommand;
import basemod.interfaces.PostRenderSubscriber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import io.chaofan.sts.chaofanmod.ChaofanMod;
import io.chaofan.sts.intentgraph.model.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class IntentGraphMod implements PostRenderSubscriber /*, PostInitializeSubscriber*/ {

    private static final Texture GROUP_BORDER = ImageMaster.loadImage(ChaofanMod.getImagePath("ui/groupborder.png"));
    private static final TextureRegion GROUP_BORDER_TL = new TextureRegion(GROUP_BORDER, 3, 3);
    private static final TextureRegion GROUP_BORDER_TM = new TextureRegion(GROUP_BORDER, 3, 0, 26, 3);
    private static final TextureRegion GROUP_BORDER_TR = new TextureRegion(GROUP_BORDER, 29, 0, 3, 3);
    private static final TextureRegion GROUP_BORDER_ML = new TextureRegion(GROUP_BORDER, 0, 3, 3, 26);
    private static final TextureRegion GROUP_BORDER_MR = new TextureRegion(GROUP_BORDER, 29, 3, 3, 26);
    private static final TextureRegion GROUP_BORDER_BL = new TextureRegion(GROUP_BORDER, 0, 29, 3, 3);
    private static final TextureRegion GROUP_BORDER_BM = new TextureRegion(GROUP_BORDER, 3, 29, 26, 3);
    private static final TextureRegion GROUP_BORDER_BR = new TextureRegion(GROUP_BORDER, 29, 29, 3, 3);

    private static final Texture ARROW = ImageMaster.loadImage(ChaofanMod.getImagePath("ui/arrow.png"));
    private static final TextureRegion ARROW_HORIZONTAL = new TextureRegion(ARROW, 64, 10);
    private static final TextureRegion ARROW_VERTICAL = new TextureRegion(ARROW, 65, 0, 10, 64);
    private static final TextureRegion ARROW_DR = new TextureRegion(ARROW, 0, 11, 10, 10);
    private static final TextureRegion ARROW_DL = new TextureRegion(ARROW, 10, 11, 10, 10);
    private static final TextureRegion ARROW_UR = new TextureRegion(ARROW, 0, 21, 10, 10);
    private static final TextureRegion ARROW_UL = new TextureRegion(ARROW, 10, 21, 10, 10);
    private static final TextureRegion ARROW_U = new TextureRegion(ARROW, 21, 11, 20, 15);
    private static final TextureRegion ARROW_D = new TextureRegion(ARROW, 42, 11, 20, 15);
    private static final TextureRegion ARROW_R = new TextureRegion(ARROW, 21, 27, 15, 20);
    private static final TextureRegion ARROW_L = new TextureRegion(ARROW, 37, 27, 15, 20);
    public static final Color INSTANT_COLOR = new Color(1, 0.2f, 0.2f, 1);

    private final TextureRegion BOX_TL = new TextureRegion(ImageMaster.KEYWORD_TOP, 32, 32);
    private final TextureRegion BOX_TM = new TextureRegion(ImageMaster.KEYWORD_TOP, 32, 0, 256, 32);
    private final TextureRegion BOX_TR = new TextureRegion(ImageMaster.KEYWORD_TOP, 288, 0, 32, 32);
    private final TextureRegion BOX_ML = new TextureRegion(ImageMaster.KEYWORD_BODY, 32, 32);
    private final TextureRegion BOX_MM = new TextureRegion(ImageMaster.KEYWORD_BODY, 32, 0, 256, 32);
    private final TextureRegion BOX_MR = new TextureRegion(ImageMaster.KEYWORD_BODY, 288, 0, 32, 32);
    private final TextureRegion BOX_BL = new TextureRegion(ImageMaster.KEYWORD_BOT, 32, 32);
    private final TextureRegion BOX_BM = new TextureRegion(ImageMaster.KEYWORD_BOT, 32, 0, 256, 32);
    private final TextureRegion BOX_BR = new TextureRegion(ImageMaster.KEYWORD_BOT, 288, 0, 32, 32);

    private final String intentStringsPath;
    private final Map<String, String> intentStrings = new HashMap<>();
    private final Map<String, MonsterIntentGraph> intents = new HashMap<>();

    private static final int GRID_SIZE = 80;

    public static IntentGraphMod instance;
    public int overwriteAscension = -1;

    public IntentGraphMod(String intentStringsPath) {
        this.intentStringsPath = intentStringsPath;
    }

    @Override
    public void receivePostRender(SpriteBatch spriteBatch) {
        if (AbstractDungeon.getCurrMapNode() == null) {
            return;
        }

        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.monsters == null) {
            return;
        }

        for (AbstractMonster monster : room.monsters.monsters) {
            if (monster.hb.hovered && !monster.isDeadOrEscaped()) {
                renderIntentGraphForMonster(monster, spriteBatch);
                break;
            }
        }
    }

    public void receivePostInitialize() {
        loadIntents();
        ConsoleCommand.addCommand("reloadintents", ReloadIntentsCommand.class);
    }

    public void loadIntents() {
        intentStrings.clear();
        intents.clear();

        Gson gson = new Gson();
        String json = Gdx.files.internal("chaofanmod/intents/intents.json").readString(String.valueOf(StandardCharsets.UTF_8));
        Type intentType = (new TypeToken<Map<String, MonsterIntentGraph>>() {}).getType();
        intents.putAll(gson.fromJson(json, intentType));
        for (MonsterIntentGraph graph : intents.values()) {
            graph.initMonsterGraphDetail();
        }

        json = Gdx.files.internal(intentStringsPath).readString(String.valueOf(StandardCharsets.UTF_8));
        intentType = (new TypeToken<Map<String, String>>() {}).getType();
        intentStrings.putAll(gson.fromJson(json, intentType));
    }

    private void renderIntentGraphForMonster(AbstractMonster monster, SpriteBatch sb) {
        MonsterIntentGraph graph = intents.get(monster.id);
        if (graph == null) {
            return;
        }

        MonsterGraphDetail graphDetail;
        int ascensionLevel = overwriteAscension >= 0 ? overwriteAscension : AbstractDungeon.ascensionLevel;
        int renderAscensionLevel = 0;
        if (monster.type == AbstractMonster.EnemyType.NORMAL) {
            graphDetail = ascensionLevel < 2 ? graph.a0 : ascensionLevel < 17 ? graph.a1 : graph.a2;
            renderAscensionLevel = ascensionLevel < 2 ? 0 : ascensionLevel < 17 ? 2 : 17;
        } else if (monster.type == AbstractMonster.EnemyType.ELITE) {
            graphDetail = ascensionLevel < 3 ? graph.a0 : ascensionLevel < 18 ? graph.a1 : graph.a2;
            renderAscensionLevel = ascensionLevel < 3 ? 0 : ascensionLevel < 18 ? 3 : 18;
        } else if (monster.type == AbstractMonster.EnemyType.BOSS) {
            graphDetail = ascensionLevel < 4 ? graph.a0 : ascensionLevel < 19 ? graph.a1 : graph.a2;
            renderAscensionLevel = ascensionLevel < 4 ? 0 : ascensionLevel < 19 ? 4 : 19;
        } else {
            return;
        }

        float scale = Settings.scale;
        float scale32 = 32 * scale;

        float width = graphDetail.width > 0 ? graphDetail.width : graph.width;
        float height = graphDetail.height > 0 ? graphDetail.height : graph.height;
        float x = monster.hb.cX - scale32 - width * GRID_SIZE * scale / 2;
        float y = Settings.HEIGHT - 80 * scale;

        renderBox(Color.WHITE, x, y, width * GRID_SIZE * scale, (height * GRID_SIZE + 32) * scale, sb);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipHeaderFont, monster.name, x + 20 * scale, y - 20 * scale, Color.WHITE);

        if (renderAscensionLevel > 0) {
            this.renderAscensionLevel(renderAscensionLevel, x + scale32 + (width * GRID_SIZE + 12) * scale, y - 20 * scale, sb);
        }

        renderGraphDetail(graphDetail, x + scale32, y - scale32 * 2, sb);
    }

    private void renderAscensionLevel(int renderAscensionLevel, float x, float y, SpriteBatch sb) {
        float scale = Settings.scale;
        float scale64 = 64 * scale;
        FontHelper.renderFontRightTopAligned(sb, FontHelper.tipHeaderFont, Integer.toString(renderAscensionLevel), x, y, Settings.RED_TEXT_COLOR);
        sb.draw(ImageMaster.TP_ASCENSION, x - 54 * scale - FontHelper.layout.width, y - 42 * scale, scale64, scale64);
    }

    private void renderGraphDetail(MonsterGraphDetail graphDetail, float x, float y, SpriteBatch sb) {
        renderIcons(graphDetail, x, y, sb);
        renderIconGroups(graphDetail.iconGroups, x, y, sb);
        renderArrows(graphDetail.arrows, x, y, sb);
        renderLabels(graphDetail.labels, x, y, sb);
    }

    private void renderLabels(Label[] labels, float x, float y, SpriteBatch sb) {
        if (labels == null) {
            return;
        }

        for (Label label : labels) {
            renderLabel(label, x, y, sb);
        }
    }

    private void renderLabel(Label label, float x, float y, SpriteBatch sb) {
        float scale = Settings.scale;
        float labelX = x + label.x * scale * GRID_SIZE;
        float labelY = y - label.y * scale * GRID_SIZE;

        BitmapFont font = FontHelper.cardDescFont_L;
        font.getData().setScale(0.8f);
        String string = label.label;
        String localizedString = intentStrings.get(string);
        if (localizedString != null) {
            string = localizedString;
        }

        if (label.align != null && label.align.equals("left")) {
            FontHelper.renderFontLeftTopAligned(sb, font, string, labelX, labelY, Color.WHITE);
        } else if (label.align != null && label.align.equals("right")){
            FontHelper.renderFontRightTopAligned(sb, font, string, labelX, labelY, Color.WHITE);
        } else {
            FontHelper.renderFontCenteredTopAligned(sb, font, string, labelX, labelY, Color.WHITE);
        }
        font.getData().setScale(1);
    }

    private void renderArrows(Arrow[] arrows, float x, float y, SpriteBatch sb) {
        if (arrows == null) {
            return;
        }

        for (Arrow arrow : arrows) {
            renderArrow(arrow, x, y, sb);
        }
    }

    private void renderArrow(Arrow arrow, float x, float y, SpriteBatch sb) {
        if (arrow.path.length <= 3) {
            return;
        }

        sb.setColor(arrow.instant ? INSTANT_COLOR : Color.WHITE);
        float scale = Settings.scale;
        float[] path = arrow.path;
        boolean isHorizontal = path[0] == 0;
        float arrowX = path[1];
        float arrowY = path[2];
        int direction = -1; // U, R, D, L
        for (int i = 3; i < path.length; i++) {
            boolean isStart = i == 3;
            boolean isEnd = i == path.length - 1;
            float nextArrowX = isHorizontal ? path[i] : arrowX;
            float nextArrowY = isHorizontal ? arrowY : path[i];
            int nextDirection = -1;
            float dy = y - arrowY * GRID_SIZE * scale - 5 * scale;
            float dx = x + arrowX * GRID_SIZE * scale - 5 * scale;
            if (isHorizontal) {
                boolean isRight = nextArrowX > arrowX;
                nextDirection = isRight ? 1 : 3;
                float dxs = x + arrowX * GRID_SIZE * scale + (isStart ? 0 : (isRight ? 1 : -1)) * 5 * scale;
                float dxe = x + nextArrowX * GRID_SIZE * scale + ((isEnd ? 15 : 5) * (isRight ? -1 : 1) * scale);
                sb.draw(ARROW_HORIZONTAL, Math.min(dxs, dxe), dy, Math.abs(dxs - dxe), ARROW_HORIZONTAL.getRegionHeight() * scale);
            } else {
                boolean isDown = nextArrowY > arrowY;
                nextDirection = isDown ? 2 : 0;
                float dys = y - arrowY * GRID_SIZE * scale - (isStart ? 0 : (isDown ? 1 : -1)) * 5 * scale;
                float dye = y - nextArrowY * GRID_SIZE * scale - ((isEnd ? 15 : 5) * (isDown ? -1 : 1) * scale);
                sb.draw(ARROW_VERTICAL, dx, Math.min(dys, dye), ARROW_VERTICAL.getRegionWidth() * scale, Math.abs(dys - dye));
            }

            if (!isStart) {
                if ((direction == 2 && nextDirection == 1) || (direction == 3 && nextDirection == 0)) {
                    sb.draw(ARROW_UR, dx, dy, 10 * scale, 10 * scale);
                } else if ((direction == 2 && nextDirection == 3) || (direction == 1 && nextDirection == 0)) {
                    sb.draw(ARROW_UL, dx, dy, 10 * scale, 10 * scale);
                } else if ((direction == 0 && nextDirection == 3) || (direction == 1 && nextDirection == 2)) {
                    sb.draw(ARROW_DL, dx, dy, 10 * scale, 10 * scale);
                } else if ((direction == 0 && nextDirection == 1) || (direction == 3 && nextDirection == 2)) {
                    sb.draw(ARROW_DR, dx, dy, 10 * scale, 10 * scale);
                }
            }

            isHorizontal = !isHorizontal;
            arrowX = nextArrowX;
            arrowY = nextArrowY;
            direction = nextDirection;
        }

        float dy = y - arrowY * GRID_SIZE * scale;
        float dx = x + arrowX * GRID_SIZE * scale;
        switch (direction) {
            case 0:
                sb.draw(ARROW_U, dx - 10 * scale, dy - 15 * scale, 20 * scale, 15 * scale);
                break;
            case 1:
                sb.draw(ARROW_R, dx - 15 * scale, dy - 10 * scale, 15 * scale, 20 * scale);
                break;
            case 2:
                sb.draw(ARROW_D, dx - 10 * scale, dy, 20 * scale, 15 * scale);
                break;
            case 3:
                sb.draw(ARROW_L, dx, dy - 10 * scale, 15 * scale, 20 * scale);
                break;
        }
    }

    private void renderIconGroups(IconGroup[] iconGroups, float x, float y, SpriteBatch sb) {
        if (iconGroups == null) {
            return;
        }

        float scale = Settings.scale;
        for (IconGroup iconGroup : iconGroups) {
            renderIconGroupBox(
                    x + iconGroup.x * scale * GRID_SIZE,
                    y - iconGroup.y * scale * GRID_SIZE,
                    iconGroup.w * scale * GRID_SIZE,
                    iconGroup.h * scale * GRID_SIZE,
                    sb);
        }
    }

    private void renderIcons(MonsterGraphDetail graphDetail, float x, float y, SpriteBatch sb) {
        if (graphDetail.icons == null) {
            return;
        }

        for (Icon icon : graphDetail.icons) {
            renderIcon(graphDetail, x, y, sb, icon);
        }
    }

    private void renderIcon(MonsterGraphDetail graphDetail, float x, float y, SpriteBatch sb, Icon icon) {
        sb.setColor(Color.WHITE);
        
        float scale = Settings.scale;
        float iconX = x + icon.x * scale * GRID_SIZE + 8 * scale;
        float iconY = y - (icon.y + 1) * scale * GRID_SIZE + 8 * scale;
        boolean isAttack = renderIconImage(graphDetail, sb, icon, iconX, iconY);

        BitmapFont font = FontHelper.cardEnergyFont_L;
        font.getData().setScale(0.5f);

        if (isAttack) {
            Damage damage = graphDetail.damages[icon.damageIndex];
            String damageString = damage.string != null ? damage.string :
                    (damage.max <= damage.min ? String.valueOf(damage.min) : String.format("%d~%d", damage.min, damage.max));
            if (icon.attackCount > 1) {
                damageString += "x" + (icon.attackCountString != null ? icon.attackCountString : icon.attackCount);
            }
            FontHelper.renderFontLeftTopAligned(sb, font, damageString, iconX - 2 * scale, iconY + 16 * scale, Color.WHITE);
        }

        if (icon.percentage > 0) {
            FontHelper.renderFontLeftTopAligned(sb, font, icon.percentage + "%", iconX - 2 * scale, iconY + 62 * scale, Color.WHITE);
        }

        if (icon.limit >= 1) {
            FontHelper.renderFontLeftTopAligned(sb, font, "-", iconX + 45 * scale, iconY + 55 * scale, Color.WHITE);
            FontHelper.renderFontLeftTopAligned(sb, font, "<" + icon.limit, iconX + 45 * scale, iconY + 62 * scale, Color.WHITE);
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

    private void renderBox(Color color, float x, float y, float w, float h, SpriteBatch sb) {
        sb.setColor(color);
        float scale = Settings.scale;
        float scale32 = 32 * scale;
        sb.draw(this.BOX_TL, x, y - scale32, scale32, scale32);
        sb.draw(this.BOX_TM, x + scale32, y - scale32, w, scale32);
        sb.draw(this.BOX_TR, x + scale32 + w, y - scale32, scale32, scale32);

        sb.draw(this.BOX_ML, x, y - h - scale32, scale32, h);
        sb.draw(this.BOX_MM, x + scale32, y - h - scale32, w, h);
        sb.draw(this.BOX_MR, x + scale32 + w, y - h - scale32, scale32, h);

        sb.draw(this.BOX_BL, x, y - scale32 * 2 - h, scale32, scale32);
        sb.draw(this.BOX_BM, x + scale32, y - scale32 * 2 - h, w, scale32);
        sb.draw(this.BOX_BR, x + scale32 + w, y - scale32 * 2 - h, scale32, scale32);
    }

    private void renderIconGroupBox(float x, float y, float w, float h, SpriteBatch sb) {
        sb.setColor(Color.WHITE);
        float scale = Settings.scale;
        float scale3 = 3 * scale;
        w -= scale3 * 2;
        h -= scale3 * 2;
        sb.draw(GROUP_BORDER_TL, x, y - scale3, scale3, scale3);
        sb.draw(GROUP_BORDER_TM, x + scale3, y - scale3, w, scale3);
        sb.draw(GROUP_BORDER_TR, x + scale3 + w, y - scale3, scale3, scale3);

        sb.draw(GROUP_BORDER_ML, x, y - h - scale3, scale3, h);
        sb.draw(GROUP_BORDER_MR, x + scale3 + w, y - h - scale3, scale3, h);

        sb.draw(GROUP_BORDER_BL, x, y - scale3 * 2 - h, scale3, scale3);
        sb.draw(GROUP_BORDER_BM, x + scale3, y - scale3 * 2 - h, w, scale3);
        sb.draw(GROUP_BORDER_BR, x + scale3 + w, y - scale3 * 2 - h, scale3, scale3);
    }

    protected Texture getAttackIntent(int damage) {
        if (damage < 5) {// 691
            return ImageMaster.INTENT_ATK_TIP_1;// 692
        } else if (damage < 10) {// 693
            return ImageMaster.INTENT_ATK_TIP_2;// 694
        } else if (damage < 15) {// 695
            return ImageMaster.INTENT_ATK_TIP_3;// 696
        } else if (damage < 20) {// 697
            return ImageMaster.INTENT_ATK_TIP_4;// 698
        } else if (damage < 25) {// 699
            return ImageMaster.INTENT_ATK_TIP_5;// 700
        } else {
            return damage < 30 ? ImageMaster.INTENT_ATK_TIP_6 : ImageMaster.INTENT_ATK_TIP_7;// 701 702 704
        }
    }
}
