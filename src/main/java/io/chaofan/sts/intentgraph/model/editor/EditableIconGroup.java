package io.chaofan.sts.intentgraph.model.editor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.model.IconGroup;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EditableIconGroup extends IconGroup implements EditableItem {
    private static final float BORDER_SIZE = 12f * Settings.scale;

    private final float renderX;
    private final float renderY;

    private final Hitbox leftTop = new Hitbox(BORDER_SIZE, BORDER_SIZE);
    private final Hitbox rightTop = new Hitbox(BORDER_SIZE, BORDER_SIZE);
    private final Hitbox leftBottom = new Hitbox(BORDER_SIZE, BORDER_SIZE);
    private final Hitbox rightBottom = new Hitbox(BORDER_SIZE, BORDER_SIZE);
    private final Hitbox left = new Hitbox(BORDER_SIZE, 0);
    private final Hitbox right = new Hitbox(BORDER_SIZE, 0);
    private final Hitbox top = new Hitbox(0, BORDER_SIZE);
    private final Hitbox bottom = new Hitbox(0, BORDER_SIZE);

    private final List<Hitbox> hitBoxes = Arrays.asList(leftTop, rightTop, leftBottom, rightBottom, left, right, top, bottom);

    public EditableIconGroup(float renderX, float renderY) {
        this.renderX = renderX;
        this.renderY = renderY;
    }

    public EditableIconGroup(float renderX, float renderY, IconGroup iconGroup) {
        this.x = iconGroup.x;
        this.y = iconGroup.y;
        this.w = iconGroup.w;
        this.h = iconGroup.h;
        this.hide = iconGroup.hide;
        this.renderX = renderX;
        this.renderY = renderY;
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
        return this.hitBoxes;
    }

    @Override
    public void updateHitBoxesLocation() {
        leftTop.move(EditableItem.getScreenX(x, renderX), EditableItem.getScreenY(y, renderY));
        rightTop.move(EditableItem.getScreenX(x + w, renderX), EditableItem.getScreenY(y, renderY));
        leftBottom.move(EditableItem.getScreenX(x, renderX), EditableItem.getScreenY(y + h, renderY));
        rightBottom.move(EditableItem.getScreenX(x + w, renderX), EditableItem.getScreenY(y + h, renderY));
        left.resize(BORDER_SIZE, h * IntentGraphMod.GRID_SIZE * Settings.scale - BORDER_SIZE);
        left.move(EditableItem.getScreenX(x, renderX), EditableItem.getScreenY(y + h / 2, renderY));
        right.resize(BORDER_SIZE, h * IntentGraphMod.GRID_SIZE * Settings.scale - BORDER_SIZE);
        right.move(EditableItem.getScreenX(x + w, renderX), EditableItem.getScreenY(y + h / 2, renderY));
        top.resize(w * IntentGraphMod.GRID_SIZE * Settings.scale - BORDER_SIZE, BORDER_SIZE);
        top.move(EditableItem.getScreenX(x + w / 2, renderX), EditableItem.getScreenY(y, renderY));
        bottom.resize(w * IntentGraphMod.GRID_SIZE * Settings.scale - BORDER_SIZE, BORDER_SIZE);
        bottom.move(EditableItem.getScreenX(x + w / 2, renderX), EditableItem.getScreenY(y + h, renderY));
    }

    @Override
    public void move(float x, float y) {
        this.x += x;
        this.y += y;
        updateHitBoxesLocation();
    }

    public IconGroup toIconGroup() {
        IconGroup iconGroup = new IconGroup();
        iconGroup.x = x;
        iconGroup.y = y;
        iconGroup.w = w;
        iconGroup.h = h;
        iconGroup.hide = hide;
        return iconGroup;
    }
}
