package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.model.editor.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class EditorCanvas {
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#####");

    public final UndoRedoHelper undoHelper;
    private final float x;
    private final float top;
    private final float width;
    private final Toolbox toolbox;
    private String monsterId;
    private EditableMonsterGraphDetail graphDetail;

    private boolean selectedItemsChanged = false;

    private final Map<Toolbox.Tool, EditorCanvasTool> tools = new HashMap<>();
    private Toolbox.Tool lastTool;
    private EditorCanvasTool toolInstance;

    private Consumer<EditorCanvas> onSelectedItemChange;
    private Consumer<EditorCanvas> onSelectedItemPositionChange;

    public EditorCanvas(float x, float top, float width, Toolbox toolbox, UndoRedoHelper undoHelper) {
        this.x = x;
        this.top = top;
        this.width = width;
        this.toolbox = toolbox;
        this.undoHelper = undoHelper;
        this.tools.put(Toolbox.Tool.ARROW, new ArrowEditorCanvasTool(this));
        this.tools.put(Toolbox.Tool.DELETE, new DeleteEditorCanvasTool(this));
        this.tools.put(Toolbox.Tool.GROUP, new IconGroupEditorCanvasTool(this));
        this.tools.put(Toolbox.Tool.ICON, new IconEditorCanvasTool(this));
        this.tools.put(Toolbox.Tool.LABEL, new LabelEditorCanvasTool(this));
        this.tools.put(Toolbox.Tool.MOVE, new MoveEditorCanvasTool(this));
        this.lastTool = toolbox.getSelectedTool();
        this.toolInstance = this.tools.get(this.lastTool);
    }

    public void setGraphDetail(String monsterId, EditableMonsterGraphDetail graphDetail) {
        this.monsterId = monsterId;
        if (this.graphDetail != graphDetail) {
            this.toolInstance.onDeactivate();
            this.graphDetail = graphDetail;
            this.toolInstance.onActivate();
            this.notifySelectedItemsChanged();
        }
    }

    public void update() {
        this.selectedItemsChanged = false;
        Toolbox.Tool tool = toolbox.getSelectedTool();
        if (tool != lastTool) {
            toolInstance.onDeactivate();
            toolInstance = tools.get(tool);
            toolInstance.onActivate();
            lastTool = tool;
        }

        if (this.graphDetail != null) {
            toolInstance.update();
        }

        if (this.selectedItemsChanged) {
            this.notifySelectedItemsChanged();
        }
    }

    public void render(SpriteBatch sb) {
        float scale = Settings.scale;

        int hLine = -64;
        float hLineFloat;
        int hLineNum = 0;
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        do {
            hLineFloat = hLine * scale + this.top;
            sb.setColor(1, 1, 1, hLineNum % 2 == 0 ? 0.3f : 0.1f);
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, this.x + 32 * scale, hLineFloat - 1 * scale, this.width - 32 * scale, 2 * scale);
            hLine -= IntentGraphMod.GRID_SIZE / 2;
            hLineNum++;
        } while (hLineFloat > 0);

        int vLine = 32;
        float vLineFloat;
        int vLineNum = 0;
        float vLineEnd = this.x + this.width - IntentGraphMod.GRID_SIZE / 2f * scale;
        do {
            vLineFloat = vLine * scale + this.x;
            sb.setColor(1, 1, 1, vLineNum % 2 == 0 ? 0.3f : 0.1f);
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, vLineFloat - 1 * scale, 0, 2 * scale, this.top - 64 * scale);
            vLine += IntentGraphMod.GRID_SIZE / 2;
            vLineNum++;
        } while (vLineFloat < vLineEnd);

        if (this.graphDetail != null) {
            IntentGraphMod.visibleGraphMonsterId = this.monsterId;
            this.graphDetail.render(sb);
            IntentGraphMod.visibleGraphMonsterId = null;
        }

        int mouseX = InputHelper.mX;
        int mouseY = InputHelper.mY;
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.cardDescFont_N, "X: " + DECIMAL_FORMAT.format(getGridX(mouseX)), this.x, this.top + 64 * scale, Color.LIGHT_GRAY);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.cardDescFont_N, "Y: " + DECIMAL_FORMAT.format(getGridY(mouseY)), this.x, this.top + 32 * scale, Color.LIGHT_GRAY);

        toolInstance.render(sb);
    }

    public float getGraphRenderX() {
        return this.x + 32 * Settings.scale;
    }

    public float getGraphRenderY() {
        return this.top - 64 * Settings.scale;
    }

    public List<EditableItem> getSelectedItems() {
        return toolInstance.getSelectedItems();
    }

    public EditableMonsterGraphDetail getGraphDetail() {
        return graphDetail;
    }

    public void setOnSelectedItemChange(Consumer<EditorCanvas> onSelectedItemChange) {
        this.onSelectedItemChange = onSelectedItemChange;
    }

    public void setOnSelectedItemPositionChange(Consumer<EditorCanvas> onSelectedItemPositionChange) {
        this.onSelectedItemPositionChange = onSelectedItemPositionChange;
    }

    public void moveSelected(float x, float y) {
        if (!toolInstance.canMoveSelected()) {
            return;
        }
        List<EditableItem> selectedItems = this.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            List<EditableItem> targets = new ArrayList<>(selectedItems);
            this.undoHelper.runAndPush(
                    () -> targets.forEach(item -> item.move(x, y)),
                    () -> targets.forEach(item -> item.move(-x, -y)));
            if (this.onSelectedItemPositionChange != null) {
                this.onSelectedItemPositionChange.accept(this);
            }
        }
    }

    public void deleteSelected() {
        List<EditableItem> selectedItems = this.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            deleteItems(selectedItems);
            selectedItems.clear();
            if (this.onSelectedItemChange != null) {
                this.onSelectedItemChange.accept(this);
            }
        }
    }

    public float getGridX(float x) {
        return (x - this.x - 32 * Settings.scale) / (IntentGraphMod.GRID_SIZE * Settings.scale);
    }

    public float getGridY(float y) {
        return (this.top - y - 64 * Settings.scale) / (IntentGraphMod.GRID_SIZE * Settings.scale);
    }

    public float getScreenX(float x) {
        return this.x + 32 * Settings.scale + x * IntentGraphMod.GRID_SIZE * Settings.scale;
    }

    public float getScreenY(float y) {
        return this.top - 64 * Settings.scale - y * IntentGraphMod.GRID_SIZE * Settings.scale;
    }

    public boolean mouseInCanvas() {
        return InputHelper.mX > this.x && InputHelper.mX < this.x + this.width && InputHelper.mY > 0 && InputHelper.mY < this.top;
    }

    public void notifySelectedItemsChanged() {
        if (this.onSelectedItemChange != null) {
            this.onSelectedItemChange.accept(this);
        }
    }

    public void markSelectedItemsChanged() {
        this.selectedItemsChanged = true;
    }

    public void deleteItem(EditableItem item) {
        ArrayList<? extends EditableItem> list = getContainingList(item);
        if (list != null) {
            int index = list.indexOf(item);
            if (index >= 0) {
                this.undoHelper.runAndPush(
                        () -> list.remove(index),
                        () -> ((ArrayList<EditableItem>) list).add(index, item));
            }
        }
    }

    private void deleteItems(List<EditableItem> items) {
        if (!items.isEmpty()) {
            List<DeleteItemRecord> records = new ArrayList<>();
            for (EditableItem item : items) {
                ArrayList<? extends EditableItem> list = getContainingList(item);
                if (list != null) {
                    int index = list.indexOf(item);
                    if (index >= 0) {
                        DeleteItemRecord record = new DeleteItemRecord();
                        record.item = item;
                        record.list = list;
                        record.index = index;
                        records.add(record);
                    }
                }
            }

            records.sort(Comparator.<DeleteItemRecord>comparingInt(r -> r.index).reversed());

            this.undoHelper.runAndPush(
                    () -> records.forEach(r -> r.list.remove(r.index)),
                    () -> IntStream.range(0, records.size())
                            .mapToObj(i -> records.get(records.size() - i - 1))
                            .forEach(r -> ((ArrayList<EditableItem>) r.list).add(r.index, r.item)));
        }
    }

    private ArrayList<? extends EditableItem> getContainingList(EditableItem hoveredItem) {
        if (hoveredItem instanceof EditableIcon) {
            return this.graphDetail.icons;
        } else if (hoveredItem instanceof EditableIconGroup) {
            return this.graphDetail.iconGroups;
        } else if (hoveredItem instanceof EditableArrow) {
            return this.graphDetail.arrows;
        } else if (hoveredItem instanceof EditableLabel) {
            return this.graphDetail.labels;
        }
        return null;
    }

    private static class DeleteItemRecord {
        EditableItem item;
        ArrayList<? extends EditableItem> list;
        int index;
    }
}
