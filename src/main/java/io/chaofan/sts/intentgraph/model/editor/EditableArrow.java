package io.chaofan.sts.intentgraph.model.editor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.model.Arrow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EditableArrow extends Arrow implements EditableItem {
    private static final float BORDER_SIZE = 20f * Settings.scale;

    private final float renderX;
    private final float renderY;

    private final List<Hitbox> hitBoxes = new ArrayList<>();

    public EditableArrow(float renderX, float renderY) {
        this.renderX = renderX;
        this.renderY = renderY;
    }

    public EditableArrow(float renderX, float renderY, Arrow arrow) {
        this.path = arrow.path.clone();
        this.instant = arrow.instant;
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
        this.render(this.renderX, this.renderY, sb);
    }

    @Override
    public Collection<Hitbox> getHitBoxes() {
        return this.hitBoxes;
    }

    @Override
    public void updateHitBoxesLocation() {
        this.hitBoxes.clear();
        if (path.length <= 3) {
            return;
        }

        boolean isHorizontal = path[0] == 0;
        float startX = path[1];
        float startY = path[2];
        for (int i = 3; i < path.length; i++) {
            boolean isStart = i == 3;
            float endX = isHorizontal ? path[i] : startX;
            float endY = isHorizontal ? startY : path[i];

            Hitbox hitbox = new Hitbox(0, 0);
            if (isHorizontal) {
                hitbox.resize(Math.abs(endX - startX) * IntentGraphMod.GRID_SIZE * Settings.scale - (isStart ? BORDER_SIZE / 2 : 0), BORDER_SIZE);
                hitbox.move(EditableItem.getScreenX((startX + endX) / 2, renderX) - Math.signum(endX - startX) * (isStart ? BORDER_SIZE / 4 : BORDER_SIZE / 2), EditableItem.getScreenY(startY, renderY));
            } else {
                hitbox.resize(BORDER_SIZE, Math.abs(endY - startY) * IntentGraphMod.GRID_SIZE * Settings.scale - (isStart ? BORDER_SIZE / 2 : 0));
                hitbox.move(EditableItem.getScreenX(startX, renderX), EditableItem.getScreenY((startY + endY) / 2, renderY) + Math.signum(endY - startY) * (isStart ? BORDER_SIZE / 4 : BORDER_SIZE / 2));
            }

            this.hitBoxes.add(hitbox);

            isHorizontal = !isHorizontal;
            startX = endX;
            startY = endY;
        }

        isHorizontal = !isHorizontal;
        Hitbox endHitbox;
        if (isHorizontal) {
            endHitbox = new Hitbox(BORDER_SIZE, BORDER_SIZE * 1.5f);
        } else {
            endHitbox = new Hitbox(BORDER_SIZE * 1.5f, BORDER_SIZE);
        }
        endHitbox.move(EditableItem.getScreenX(startX, renderX), EditableItem.getScreenY(startY, renderY));
        this.hitBoxes.add(endHitbox);
    }

    @Override
    public void move(float x, float y) {
        boolean isHorizontal = path[0] == 0;
        path[1] += x;
        path[2] += y;
        for (int i = 3; i < path.length; i++) {
            if (isHorizontal) {
                path[i] += x;
            } else {
                path[i] += y;
            }
            isHorizontal = !isHorizontal;
        }
        updateHitBoxesLocation();
    }

    public Arrow toArrow() {
        Arrow arrow = new Arrow();
        arrow.path = this.path.clone();
        arrow.instant = this.instant;
        return arrow;
    }

    public float getLastX() {
        return path[0] == 0 ? path[path.length / 2 * 2 - 1] :
                (path.length <= 4 ? path[1] : path[(path.length - 1) / 2 * 2]);
    }

    public float getLastY() {
        return path[0] == 0 ? path[(path.length - 1) / 2 * 2] :
                (path.length <= 3 ? path[2] : path[path.length / 2 * 2 - 1]);
    }
}
