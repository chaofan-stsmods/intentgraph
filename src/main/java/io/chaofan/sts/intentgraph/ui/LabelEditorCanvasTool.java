package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.model.editor.EditableLabel;
import io.chaofan.sts.intentgraph.model.editor.EditableMonsterGraphDetail;

public class LabelEditorCanvasTool extends EditorCanvasTool {
    private final EditorCanvasDragHandler dragHandler;

    public LabelEditorCanvasTool(EditorCanvas canvas) {
        super(canvas);
        dragHandler = new EditorCanvasDragHandler(this, canvas);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        this.dragHandler.completeDragging(false);
    }

    @Override
    public void update() {
        super.update();

        dragHandler.update();
        if (dragHandler.isDragging()) {
            return;
        }

        EditableMonsterGraphDetail graphDetail = this.canvas.getGraphDetail();
        updateEditableItems(graphDetail.icons);
        if (InputHelper.justClickedLeft && canvas.mouseInCanvas()) {
            InputHelper.justClickedLeft = false;
            if (!this.selectedItems.isEmpty() || this.hoveredItem != null) {
                setSingleSelectedItem(this.hoveredItem);
                dragHandler.startDragging();
            } else {
                this.insertItem(graphDetail.labels, (x, y) -> {
                    EditableLabel label = new EditableLabel(canvas.getGraphRenderX(), canvas.getGraphRenderY());
                    label.x = x;
                    label.y = y;
                    label.label = "Label";
                    label.align = "middle";
                    return label;
                });
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);

        int mouseX = InputHelper.mX;
        int mouseY = InputHelper.mY;
        float scale = Settings.scale;
        if (hoveredItem == null && selectedItems.isEmpty() && canvas.mouseInCanvas()) {
            sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            sb.setColor(this.hoverItemColor);
            float x = canvas.getScreenX(MathUtils.round(canvas.getGridX(mouseX) * 2) / 2f);
            float y = canvas.getScreenY(MathUtils.round(canvas.getGridY(mouseY) * 2) / 2f);
            float width = IntentGraphMod.GRID_SIZE * scale;
            float height = IntentGraphMod.GRID_SIZE * scale;
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, x - width / 2, y - height / 2, width, height);
            sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    @Override
    public boolean canMoveSelected() {
        return !dragHandler.isDragging();
    }
}
