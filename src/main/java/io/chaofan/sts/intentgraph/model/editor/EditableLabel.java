package io.chaofan.sts.intentgraph.model.editor;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import io.chaofan.sts.intentgraph.model.Label;
import io.chaofan.sts.intentgraph.ui.LabelPropertiesControl;

import java.util.Collection;
import java.util.Collections;

public class EditableLabel extends Label implements EditableItem {
    private final float renderX;
    private final float renderY;

    public String localizedText;

    private final Hitbox hitbox = new Hitbox(0, 0);

    public EditableLabel(float renderX, float renderY) {
        this.renderX = renderX;
        this.renderY = renderY;
    }

    public EditableLabel(float renderX, float renderY, Label label) {
        this.x = label.x;
        this.y = label.y;
        this.label = label.label;
        this.align = label.align;
        this.renderX = renderX;
        this.renderY = renderY;
        if (label instanceof EditableLabel) {
            this.localizedText = ((EditableLabel) label).localizedText;
        }
        updateHitBoxesLocation();
    }

    @Override
    public void update() {
        updateHitBoxes();
    }

    @Override
    public void render(SpriteBatch sb, EditableMonsterGraphDetail graphDetail) {
        this.render(renderX, renderY, sb);
    }

    @Override
    public Collection<Hitbox> getHitBoxes() {
        return Collections.singleton(hitbox);
    }

    @Override
    public String getLocalizedString(String string) {
        if (!LabelPropertiesControl.showLocalizedText) {
            return label;
        }
        if (localizedText != null) {
            return localizedText;
        }
        return localizedText = super.getLocalizedString(string);
    }

    public void updateHitBoxesLocation() {
        BitmapFont font = FontHelper.cardDescFont_L;
        float width = FontHelper.getWidth(font, getLocalizedString(label), 0.8f);
        hitbox.resize(width + 12 * Settings.scale, 28 * Settings.scale);
        if ("left".equals(align)) {
            hitbox.move(EditableItem.getScreenX(x, renderX) + width / 2, EditableItem.getScreenY(y + 0.1f, renderY));
        } else if ("right".equals(align)) {
            hitbox.move(EditableItem.getScreenX(x, renderX) - width / 2, EditableItem.getScreenY(y + 0.1f, renderY));
        } else {
            hitbox.move(EditableItem.getScreenX(x, renderX), EditableItem.getScreenY(y + 0.1f, renderY));
        }
    }

    @Override
    public void move(float x, float y) {
        this.x += x;
        this.y += y;
        updateHitBoxesLocation();
    }

    public Label toLabel() {
        Label label = new Label();
        label.x = x;
        label.y = y;
        label.label = this.label;
        label.align = align;
        return label;
    }
}
