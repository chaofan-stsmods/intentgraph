package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import io.chaofan.sts.intentgraph.model.editor.EditableItem;
import io.chaofan.sts.intentgraph.model.editor.EditableMonsterGraphDetail;

import java.util.ArrayList;

public class MoveEditorCanvasTool extends EditorCanvasTool {
    private final EditorCanvasDragHandler dragHandler;
    private boolean isMultiSelecting;
    private float multiSelectX;
    private float multiSelectY;

    public MoveEditorCanvasTool(EditorCanvas canvas) {
        super(canvas);
        dragHandler = new EditorCanvasDragHandler(this, canvas);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        this.dragHandler.completeDragging(false);
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        this.isMultiSelecting = false;
    }

    @Override
    public void update() {
        super.update();
        dragHandler.update();
        if (dragHandler.isDragging()) {
            return;
        }

        EditableMonsterGraphDetail graphDetail = this.canvas.getGraphDetail();

        if (isMultiSelecting) {
            if (InputHelper.isMouseDown) {
                float currentX = InputHelper.mX;
                float currentY = InputHelper.mY;
                this.selectedItems.clear();
                this.canvas.markSelectedItemsChanged();
                addToMultiSelect(graphDetail.icons, currentX, currentY);
                addToMultiSelect(graphDetail.iconGroups, currentX, currentY);
                addToMultiSelect(graphDetail.arrows, currentX, currentY);
                addToMultiSelect(graphDetail.labels, currentX, currentY);
            } else {
                this.isMultiSelecting = false;
            }
            return;
        }

        updateEditableItems(graphDetail.icons);
        updateEditableItems(graphDetail.iconGroups);
        updateEditableItems(graphDetail.arrows);
        updateEditableItems(graphDetail.labels);
        if (InputHelper.justClickedLeft && this.canvas.mouseInCanvas()) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                if (this.hoveredItem != null) {
                    if (this.selectedItems.contains(this.hoveredItem)) {
                        this.selectedItems.remove(this.hoveredItem);
                    } else {
                        this.selectedItems.add(this.hoveredItem);
                    }
                }
                this.canvas.markSelectedItemsChanged();
            } else if (this.hoveredItem != null && this.selectedItems.contains(this.hoveredItem)) {
                this.dragHandler.startDragging();
            } else {
                this.selectedItems.clear();
                if (hoveredItem == null) {
                    this.isMultiSelecting = true;
                    this.multiSelectX = InputHelper.mX;
                    this.multiSelectY = InputHelper.mY;
                } else {
                    this.selectedItems.add(this.hoveredItem);
                    this.dragHandler.startDragging();
                }
                this.canvas.markSelectedItemsChanged();
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);

        int mouseX = InputHelper.mX;
        int mouseY = InputHelper.mY;
        if (this.isMultiSelecting) {
            sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            sb.setColor(Color.DARK_GRAY);
            float x = Math.min(this.multiSelectX, mouseX);
            float y = Math.min(this.multiSelectY, mouseY);
            float width = Math.abs(mouseX - this.multiSelectX) + 1;
            float height = Math.abs(mouseY - this.multiSelectY) + 1;
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, width, 1);
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, 1, height);
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y + height - 1, width, 1);
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, x + width - 1, y, 1, height);
            sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    @Override
    public boolean canMoveSelected() {
        return !dragHandler.isDragging();
    }

    private <T extends EditableItem> void addToMultiSelect(ArrayList<T> items, float currentX, float currentY) {
        for (T item : items) {
            if (item.isInRect(this.multiSelectX, this.multiSelectY, currentX, currentY)) {
                this.selectedItems.add(item);
                this.canvas.markSelectedItemsChanged();
            }
        }
    }
}
