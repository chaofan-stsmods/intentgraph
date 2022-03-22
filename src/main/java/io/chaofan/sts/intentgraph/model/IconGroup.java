package io.chaofan.sts.intentgraph.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import io.chaofan.sts.intentgraph.IntentGraphMod;

import static io.chaofan.sts.intentgraph.IntentGraphMod.getImagePath;

public class IconGroup {

    private static final Texture GROUP_BORDER = ImageMaster.loadImage(getImagePath("ui/groupborder.png"));
    private static final TextureRegion GROUP_BORDER_TL = new TextureRegion(GROUP_BORDER, 3, 3);
    private static final TextureRegion GROUP_BORDER_TM = new TextureRegion(GROUP_BORDER, 3, 0, 26, 3);
    private static final TextureRegion GROUP_BORDER_TR = new TextureRegion(GROUP_BORDER, 29, 0, 3, 3);
    private static final TextureRegion GROUP_BORDER_ML = new TextureRegion(GROUP_BORDER, 0, 3, 3, 26);
    private static final TextureRegion GROUP_BORDER_MR = new TextureRegion(GROUP_BORDER, 29, 3, 3, 26);
    private static final TextureRegion GROUP_BORDER_BL = new TextureRegion(GROUP_BORDER, 0, 29, 3, 3);
    private static final TextureRegion GROUP_BORDER_BM = new TextureRegion(GROUP_BORDER, 3, 29, 26, 3);
    private static final TextureRegion GROUP_BORDER_BR = new TextureRegion(GROUP_BORDER, 29, 29, 3, 3);

    public float x;
    public float y;
    public float w;
    public float h;
    public boolean hide;

    public void render(float x, float y, SpriteBatch sb) {
        float scale = Settings.scale;
        renderIconGroupBox(
                x + this.x * scale * IntentGraphMod.GRID_SIZE,
                y - this.y * scale * IntentGraphMod.GRID_SIZE,
                w * scale * IntentGraphMod.GRID_SIZE,
                h * scale * IntentGraphMod.GRID_SIZE,
                sb);
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
}
