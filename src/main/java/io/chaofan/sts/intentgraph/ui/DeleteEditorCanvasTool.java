package io.chaofan.sts.intentgraph.ui;

import com.megacrit.cardcrawl.helpers.input.InputHelper;
import io.chaofan.sts.intentgraph.model.editor.EditableMonsterGraphDetail;

public class DeleteEditorCanvasTool extends EditorCanvasTool {
    public DeleteEditorCanvasTool(EditorCanvas canvas) {
        super(canvas);
    }

    @Override
    public void update() {
        super.update();

        EditableMonsterGraphDetail graphDetail = this.canvas.getGraphDetail();
        updateEditableItems(graphDetail.icons);
        updateEditableItems(graphDetail.iconGroups);
        updateEditableItems(graphDetail.arrows);
        updateEditableItems(graphDetail.labels);
        if (InputHelper.justClickedLeft && this.hoveredItem != null) {
            canvas.deleteItem(this.hoveredItem);
        }
    }
}
