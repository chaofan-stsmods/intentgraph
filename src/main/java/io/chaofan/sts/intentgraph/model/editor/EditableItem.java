package io.chaofan.sts.intentgraph.model.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import io.chaofan.sts.intentgraph.IntentGraphMod;

import java.util.Collection;

public interface EditableItem {
    void update();
    void render(SpriteBatch sb, EditableMonsterGraphDetail graphDetail);
    Collection<Hitbox> getHitBoxes();
    void updateHitBoxesLocation();
    void move(float x, float y);

    default void renderHitBoxes(SpriteBatch sb, Color color) {
        sb.setColor(color);
        for (Hitbox hitbox : getHitBoxes()) {
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        }
    }

    default void updateHitBoxes() {
        for (Hitbox hitbox : getHitBoxes()) {
            hitbox.update(hitbox.x, hitbox.y);
        }
    }

    default boolean isHovered() {
        return getHitBoxes().stream().anyMatch(hitbox -> hitbox.hovered);
    }

    default boolean isInRect(float x1, float y1, float x2, float y2) {
        float xMin = Math.min(x1, x2);
        float xMax = Math.max(x1, x2);
        float yMin = Math.min(y1, y2);
        float yMax = Math.max(y1, y2);

        return getHitBoxes().stream().allMatch(hitBox -> {
            float hitBoxXMin = hitBox.x;
            float hitBoxXMax = hitBox.x + hitBox.width;
            float hitBoxYMin = hitBox.y;
            float hitBoxYMax = hitBox.y + hitBox.height;
            return hitBoxXMin >= xMin && hitBoxXMax <= xMax && hitBoxYMin >= yMin && hitBoxYMax <= yMax;
        });
    }

    static float getGridX(float x, float renderX) {
        return (x - renderX) / (IntentGraphMod.GRID_SIZE * Settings.scale);
    }

    static float getGridY(float y, float renderY) {
        return (renderY - y) / (IntentGraphMod.GRID_SIZE * Settings.scale);
    }

    static float getScreenX(float x, float renderX) {
        return x * IntentGraphMod.GRID_SIZE * Settings.scale + renderX;
    }

    static float getScreenY(float y, float renderY) {
        return renderY - y * IntentGraphMod.GRID_SIZE * Settings.scale;
    }
}
