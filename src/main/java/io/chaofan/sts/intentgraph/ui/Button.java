package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Button {
    private final TextureRegion img;
    private final String text;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    protected final Hitbox hb;
    private boolean pressed;
    private Consumer<Button> onClick;
    private BiConsumer<Button, SpriteBatch> onRender;
    private String title;
    private String description;
    protected final Color inactiveColor = new Color(0.6f, 0.6f, 0.6f, 1F);

    public Button(TextureRegion img, float x, float y, float width, float height) {
        this(img, null, x, y, width, height);
    }

    public Button(String text, float x, float y, float width, float height) {
        this(null, text, x, y, width, height);
    }

    public Button(TextureRegion img, String text, float x, float y, float width, float height) {
        this.img = img;
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hb = new Hitbox(x, y, width, height);
    }

    public Button(Texture img, float x, float y, float width, float height) {
        this(new TextureRegion(img), x, y, width, height);
    }

    public void setOnClick(Consumer<Button> onClick) {
        this.onClick = onClick;
    }

    public void setOnRender(BiConsumer<Button, SpriteBatch> onRender) {
        this.onRender = onRender;
    }

    public void setTooltip(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public void setInactiveColor(Color color) {
        this.inactiveColor.set(color);
    }

    public void update() {
        this.hb.update(this.x, this.y);
        if (this.hb.hovered && InputHelper.justClickedLeft) {
            InputHelper.justClickedLeft = false;
            this.pressed = true;
        }
        if (InputHelper.justReleasedClickLeft && this.pressed) {
            this.pressed = false;
            if (this.hb.hovered) {
                if (this.onClick != null) {
                    this.onClick.accept(this);
                }
            }
        }
        if (this.hb.hovered && !this.pressed && this.title != null && this.description != null) {
            TipHelper.renderGenericTip(this.x, this.y - 40 * Settings.scale, this.title, this.description);
        }
    }

    public void render(SpriteBatch sb) {
        Color color;
        if (this.hb.hovered) {
            color = Color.WHITE;
        } else {
            color = this.inactiveColor;
        }

        sb.setColor(color);

        if (this.img != null) {
            sb.draw(this.img, this.x, this.y, this.width, this.height);
        }

        if (this.text != null) {
            BitmapFont labelFont = FontHelper.cardTitleFont;
            FontHelper.renderFontCentered(sb, labelFont, text, x + width / 2, y + height / 2, color);
        }

        if (this.onRender != null) {
            this.onRender.accept(this, sb);
        }

        this.hb.render(sb);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
