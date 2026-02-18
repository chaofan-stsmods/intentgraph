package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import io.chaofan.sts.intentgraph.model.editor.EditableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public abstract class EditorCanvasTool {
    protected final Color hoverItemColor = new Color(1, 1, 1, 0.2f);
    private final Color selectedItemColor = new Color(1, 1, 0.3f, 0.5f);

    protected final EditorCanvas canvas;
    protected EditableItem hoveredItem;
    protected List<EditableItem> selectedItems = new ArrayList<>();

    public EditorCanvasTool(EditorCanvas canvas) {
        this.canvas = canvas;
    }

    public void onActivate() {
        this.setSingleSelectedItem(null);
    }

    public void update() {
        // hoveredItem will be updated in derived classes.
        this.hoveredItem = null;
    }

    public void render(SpriteBatch sb) {
        if (this.hoveredItem != null && !this.selectedItems.contains(this.hoveredItem)) {
            this.renderItemHitBoxes(sb, this.hoveredItem, this.hoverItemColor);
        }
        for (EditableItem multiSelectedItem : this.selectedItems) {
            this.renderItemHitBoxes(sb, multiSelectedItem, this.selectedItemColor);
        }
    }

    public List<EditableItem> getSelectedItems() {
        return selectedItems;
    }

    protected <T extends EditableItem> void updateEditableItems(ArrayList<T> items) {
        for (T item : items) {
            item.update();
            if (item.isHovered()) {
                this.hoveredItem = item;
            }
        }
    }

    protected void setSingleSelectedItem(EditableItem item) {
        this.selectedItems.clear();
        if (item != null) {
            this.selectedItems.add(item);
        }
        canvas.markSelectedItemsChanged();
    }

    protected <T extends EditableItem> void insertItem(ArrayList<T> list, BiFunction<Float, Float, T> constructor) {
        float x = MathUtils.round(canvas.getGridX(InputHelper.mX) * 2) / 2f;
        float y = MathUtils.round(canvas.getGridY(InputHelper.mY) * 2) / 2f;
        T item = constructor.apply(x, y);
        item.updateHitBoxesLocation();
        canvas.undoHelper.runAndPush(
                () -> list.add(item),
                () -> {
                    list.remove(item);
                    if (this.selectedItems.contains(item)) {
                        this.selectedItems.remove(item);
                        canvas.notifySelectedItemsChanged();
                    }
                });
        this.setSingleSelectedItem(item);
    }

    private void renderItemHitBoxes(SpriteBatch sb, EditableItem item, Color color) {
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        item.renderHitBoxes(sb, color);
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public boolean canMoveSelected() {
        return true; // !isDragging
    }
}
