package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.model.editor.EditableArrow;
import io.chaofan.sts.intentgraph.model.editor.EditableMonsterGraphDetail;

import java.util.Arrays;

public class ArrowEditorCanvasTool extends EditorCanvasTool {
    private final EditorCanvasDragHandler dragHandler;
    private EditableArrow addingArrow;
    private Button addArrowButton;
    private Button cancelArrowButton;

    public ArrowEditorCanvasTool(EditorCanvas canvas) {
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
        this.completeAddingArrow(false);
    }

    @Override
    public void update() {
        super.update();

        dragHandler.update();
        if (dragHandler.isDragging()) {
            return;
        }

        if (addArrowButton != null) {
            addArrowButton.update();
        }
        if (cancelArrowButton != null) {
            cancelArrowButton.update();
        }

        if (addingArrow != null) {
            float x = MathUtils.round(canvas.getGridX(InputHelper.mX) * 4) / 4f;
            float y = MathUtils.round(canvas.getGridY(InputHelper.mY) * 4) / 4f;

            float[] path = addingArrow.path;
            boolean canAddNewPoint = false;
            float newValue = (path.length + 1) % 2 != path[0] ? y : x;
            if (path.length < 4) {
                float dx = Math.abs(x - path[1]);
                float dy = Math.abs(y - path[2]);
                if (dx >= dy && dx > 0) {
                    path[0] = 0;
                    canAddNewPoint = true;
                } else if (dy > 0) {
                    path[0] = 1;
                    canAddNewPoint = true;
                }
            } else {
                canAddNewPoint = newValue != path[path.length - 2];
            }

            if (InputHelper.justClickedLeft && canvas.mouseInCanvas()) {
                InputHelper.justClickedLeft = false;
                if (canAddNewPoint) {
                    addingArrow.path = Arrays.copyOf(addingArrow.path, addingArrow.path.length + 1);
                    addingArrow.path[addingArrow.path.length - 1] = newValue;
                    setupAddingArrowButtons();
                }
            } else if (InputHelper.justClickedRight && canvas.mouseInCanvas()) {
                if (path.length >= 4) {
                    addingArrow.path = Arrays.copyOf(addingArrow.path, addingArrow.path.length - 1);
                    setupAddingArrowButtons();
                } else {
                    completeAddingArrow(false);
                }
            }
            return;
        }

        EditableMonsterGraphDetail graphDetail = this.canvas.getGraphDetail();
        updateEditableItems(graphDetail.arrows);
        if (InputHelper.justClickedLeft && canvas.mouseInCanvas()) {
            InputHelper.justClickedLeft = false;
            if (!this.selectedItems.isEmpty() || this.hoveredItem != null) {
                setSingleSelectedItem(this.hoveredItem);
                dragHandler.startDragging();
            } else {
                float x = MathUtils.round(canvas.getGridX(InputHelper.mX) * 4) / 4f;
                float y = MathUtils.round(canvas.getGridY(InputHelper.mY) * 4) / 4f;
                addingArrow = new EditableArrow(canvas.getGraphRenderX(), canvas.getGraphRenderY());
                addingArrow.path = new float[] {0, x, y};
                graphDetail.arrows.add(addingArrow);
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
            if (addingArrow == null) {
                float x = canvas.getScreenX(MathUtils.round(canvas.getGridX(mouseX) * 4) / 4f);
                float y = canvas.getScreenY(MathUtils.round(canvas.getGridY(mouseY) * 4) / 4f);
                float width = IntentGraphMod.GRID_SIZE * scale / 2;
                float height = IntentGraphMod.GRID_SIZE * scale / 2;
                sb.draw(ImageMaster.WHITE_SQUARE_IMG, x - width / 2, y - height / 2, width, height);
            } else {
                float[] path = addingArrow.path;
                if (path.length % 2 != path[0]) { // horizontal
                    float ex = canvas.getScreenX(MathUtils.round(canvas.getGridX(mouseX) * 4) / 4f);
                    float sx = canvas.getScreenX(addingArrow.getLastX());
                    float y = canvas.getScreenY(addingArrow.getLastY());
                    float height = IntentGraphMod.GRID_SIZE * scale / 2;
                    sb.draw(ImageMaster.WHITE_SQUARE_IMG, Math.min(sx, ex), y - height / 2, Math.abs(ex - sx), height);
                } else { // vertical
                    float ey = canvas.getScreenY(MathUtils.round(canvas.getGridY(mouseY) * 4) / 4f);
                    float sy = canvas.getScreenY(addingArrow.getLastY());
                    float x = canvas.getScreenX(addingArrow.getLastX());
                    float width = IntentGraphMod.GRID_SIZE * scale / 2;
                    sb.draw(ImageMaster.WHITE_SQUARE_IMG, x - width / 2, Math.min(sy, ey), width, Math.abs(ey - sy));
                }
            }
            sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        if (addArrowButton != null) {
            addArrowButton.render(sb);
        }
        if (cancelArrowButton != null) {
            cancelArrowButton.render(sb);
        }
    }

    @Override
    public boolean canMoveSelected() {
        return !dragHandler.isDragging();
    }

    private void setupAddingArrowButtons() {
        addArrowButton = new Button(EditIntentGraphScreen.getButtonImage(13),
                canvas.getScreenX(addingArrow.getLastX()) + 10 * Settings.scale,
                canvas.getScreenY(addingArrow.getLastY()) - 52 * Settings.scale,
                42 * Settings.scale,
                42 * Settings.scale);
        cancelArrowButton = new Button(EditIntentGraphScreen.getButtonImage(2),
                canvas.getScreenX(addingArrow.getLastX()) + 60 * Settings.scale,
                canvas.getScreenY(addingArrow.getLastY()) - 52 * Settings.scale,
                42 * Settings.scale,
                42 * Settings.scale);
        addArrowButton.setOnClick(button -> completeAddingArrow(true));
        cancelArrowButton.setOnClick(button -> completeAddingArrow(false));
    }

    private void completeAddingArrow(boolean apply) {
        addArrowButton = null;
        cancelArrowButton = null;
        if (addingArrow == null) {
            return;
        }

        EditableMonsterGraphDetail graphDetail = this.canvas.getGraphDetail();
        graphDetail.arrows.remove(addingArrow);
        if (apply) {
            this.insertItem(graphDetail.arrows, (x, y) -> addingArrow);
        }
        addingArrow = null;
    }
}
