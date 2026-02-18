package io.chaofan.sts.intentgraph.ui;

import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import io.chaofan.sts.intentgraph.IntentGraphMod;

public class EditorCanvasDragHandler {
    private final EditorCanvasTool tool;
    private final EditorCanvas canvas;
    private boolean isDragging;
    private float dragStartX;
    private float dragStartY;
    private float dragLastX;
    private float dragLastY;

    public EditorCanvasDragHandler(EditorCanvasTool tool, EditorCanvas canvas) {
        this.tool = tool;
        this.canvas = canvas;
    }

    public void update() {
        if (!this.isDragging) {
            return;
        }

        if (!InputHelper.isMouseDown) {
            this.completeDragging(true);
            return;
        }

        int currentX = InputHelper.mX;
        int currentY = InputHelper.mY;
        float currentGridX = Math.round((currentX - dragStartX) * 2 / (IntentGraphMod.GRID_SIZE * Settings.scale)) / 2f;
        float currentGridY = -Math.round((currentY - dragStartY) * 2 / (IntentGraphMod.GRID_SIZE * Settings.scale)) / 2f;
        float lastGridX = Math.round((dragLastX - dragStartX) * 2 / (IntentGraphMod.GRID_SIZE * Settings.scale)) / 2f;
        float lastGridY = -Math.round((dragLastY - dragStartY) * 2 / (IntentGraphMod.GRID_SIZE * Settings.scale)) / 2f;
        float deltaX = currentGridX - lastGridX;
        float deltaY = currentGridY - lastGridY;
        if (deltaX != 0 || deltaY != 0) {
            // Move without undo-redo
            this.tool.getSelectedItems().forEach(item -> item.move(deltaX, deltaY));
        }
        this.dragLastX = currentX;
        this.dragLastY = currentY;
    }

    public void startDragging() {
        this.isDragging = true;
        this.dragStartX = this.dragLastX = InputHelper.mX;
        this.dragStartY = this.dragLastY = InputHelper.mY;
    }

    public void completeDragging(boolean apply) {
        if (!isDragging) {
            return;
        }

        isDragging = false;
        float lastGridX = Math.round((dragLastX - dragStartX) * 2 / (IntentGraphMod.GRID_SIZE * Settings.scale)) / 2f;
        float lastGridY = -Math.round((dragLastY - dragStartY) * 2 / (IntentGraphMod.GRID_SIZE * Settings.scale)) / 2f;
        if (lastGridX != 0 || lastGridY != 0) {
            // Reset item position and move with undo-redo
            this.tool.getSelectedItems().forEach(item -> item.move(-lastGridX, -lastGridY));
            if (apply) {
                this.canvas.moveSelected(lastGridX, lastGridY);
            }
        }
    }

    public boolean isDragging() {
        return isDragging;
    }
}
