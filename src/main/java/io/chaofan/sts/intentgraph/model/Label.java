package io.chaofan.sts.intentgraph.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import io.chaofan.sts.intentgraph.IntentGraphMod;

public class Label {
    public float x;
    public float y;
    public String label;
    public String align;

    public void render(float x, float y, SpriteBatch sb) {
        float scale = Settings.scale;
        float labelX = x + this.x * scale * IntentGraphMod.GRID_SIZE;
        float labelY = y - this.y * scale * IntentGraphMod.GRID_SIZE;

        BitmapFont font = FontHelper.cardDescFont_L;
        font.getData().setScale(0.8f);
        String string = this.label;
        String localizedString = IntentGraphMod.instance.intentStrings.get(string);
        if (localizedString != null) {
            string = localizedString;
        }

        if (align != null && align.equals("left")) {
            FontHelper.renderFontLeftTopAligned(sb, font, string, labelX, labelY, Color.WHITE);
        } else if (align != null && align.equals("right")){
            FontHelper.renderFontRightTopAligned(sb, font, string, labelX, labelY, Color.WHITE);
        } else {
            FontHelper.renderFontCenteredTopAligned(sb, font, string, labelX, labelY, Color.WHITE);
        }
        font.getData().setScale(1);
    }
}
