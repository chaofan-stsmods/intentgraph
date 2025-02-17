package io.chaofan.sts.intentgraph.model.editor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.model.Icon;

import java.util.Collection;
import java.util.Collections;

public class EditableIcon extends Icon implements EditableItem {
    private final float renderX;
    private final float renderY;

    private final Hitbox hitbox = new Hitbox(IntentGraphMod.GRID_SIZE * Settings.scale, IntentGraphMod.GRID_SIZE * Settings.scale);

    private boolean isAttack = false;

    public EditableIcon(float renderX, float renderY) {
        this.renderX = renderX;
        this.renderY = renderY;
    }

    public EditableIcon(float renderX, float renderY, Icon icon) {
        this.x = icon.x;
        this.y = icon.y;
        this.type = icon.type;
        this.damageIndex = icon.damageIndex;
        this.percentage = icon.percentage;
        this.limit = icon.limit;
        this.limitType = icon.limitType;
        this.attackCount = icon.attackCount;
        this.attackCountString = icon.attackCountString;
        this.renderX = renderX;
        this.renderY = renderY;
        updateHitBoxesLocation();
    }

    @Override
    public void update() {
        updateHitBoxes();
    }

    @Override
    public void updateSelected() {

    }

    @Override
    public void render(SpriteBatch sb, EditableMonsterGraphDetail graphDetail) {
        this.isAttack = this.render(graphDetail, renderX, renderY, sb);
    }

    @Override
    public Collection<Hitbox> getHitBoxes() {
        return Collections.singleton(hitbox);
    }

    @Override
    public void updateHitBoxesLocation() {
        this.hitbox.move(EditableItem.getScreenX(x + 0.5f, renderX), EditableItem.getScreenY(y + 0.5f, renderY));
    }

    @Override
    public void move(float x, float y) {
        this.x += x;
        this.y += y;
        updateHitBoxesLocation();
    }

    public boolean isAttack() {
        return isAttack;
    }

    public Icon toIcon() {
        Icon icon = new Icon();
        icon.x = x;
        icon.y = y;
        icon.type = type;
        icon.damageIndex = damageIndex;
        icon.percentage = percentage;
        icon.limit = limit;
        icon.limitType = limitType;
        icon.attackCount = attackCount;
        icon.attackCountString = attackCountString;
        return icon;
    }
}
