package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.model.editor.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class EditorCanvas {
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#####");

    private final float x;
    private final float top;
    private final float width;
    private final Toolbox toolbox;
    private final UndoRedoHelper undoHelper;
    private String monsterId;
    private EditableMonsterGraphDetail graphDetail;

    private EditableItem hoveredItem;
    private final List<EditableItem> selectedItems = new ArrayList<>();
    private boolean selectedItemsChanged = false;
    private boolean isMultiSelecting;
    private float multiSelectX;
    private float multiSelectY;
    private boolean isDragging;
    private float dragStartX;
    private float dragStartY;
    private float dragLastX;
    private float dragLastY;
    private EditableArrow addingArrow;
    private Button addArrowButton;
    private Button cancelArrowButton;
    private final Color hoverItemColor = new Color(1, 1, 1, 0.2f);
    private final Color selectedItemColor = new Color(1, 1, 0.3f, 0.5f);

    private Toolbox.Tool lastTool;

    private Consumer<EditorCanvas> onSelectedItemChange;
    private Consumer<EditorCanvas> onSelectedItemPositionChange;

    public EditorCanvas(float x, float top, float width, Toolbox toolbox, UndoRedoHelper undoHelper) {
        this.x = x;
        this.top = top;
        this.width = width;
        this.toolbox = toolbox;
        this.undoHelper = undoHelper;
    }

    public void setGraphDetail(String monsterId, EditableMonsterGraphDetail graphDetail) {
        this.monsterId = monsterId;
        if (this.graphDetail != graphDetail) {
            this.graphDetail = graphDetail;
            this.hoveredItem = null;
            this.selectedItems.clear();
            if (this.onSelectedItemChange != null) {
                this.onSelectedItemChange.accept(this);
            }
        }
    }

    public void update() {
        this.selectedItemsChanged = false;
        this.hoveredItem = null;
        Toolbox.Tool tool = toolbox.getSelectedTool();
        if (tool != lastTool) {
            this.completeDragging(false);
            this.completeAddingArrow(false);
            this.setSingleSelectedItem(null);
            this.isMultiSelecting = false;
            lastTool = tool;
        }
        if (addArrowButton != null) {
            addArrowButton.update();
        }
        if (cancelArrowButton != null) {
            cancelArrowButton.update();
        }
        if (InputHelper.justClickedRight && mouseInCanvas()) {
            this.completeDragging(false);
            this.setSingleSelectedItem(null);
            this.isMultiSelecting = false;
        }
        if (this.graphDetail != null) {
            if (isDragging) {
                updateDragging();
            } else {
                switch (tool) {
                    case MOVE: updateMoveTool(); break;
                    case ICON: updateIconTool(); break;
                    case GROUP: updateGroupTool(); break;
                    case ARROW: updateArrowTool(); break;
                    case LABEL: updateLabelTool(); break;
                    case DELETE: updateDeleteTool(); break;
                }
            }
        }
        if (this.selectedItemsChanged && this.onSelectedItemChange != null) {
            this.onSelectedItemChange.accept(this);
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

        if (this.hoveredItem != null && !this.selectedItems.contains(this.hoveredItem)) {
            this.renderItemHitBoxes(sb, this.hoveredItem, this.hoverItemColor);
        }
        for (EditableItem multiSelectedItem : this.selectedItems) {
            this.renderItemHitBoxes(sb, multiSelectedItem, this.selectedItemColor);
        }
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

        Toolbox.Tool selectedTool = toolbox.getSelectedTool();
        if (hoveredItem == null && selectedItems.isEmpty() && mouseInCanvas() &&
                (selectedTool == Toolbox.Tool.ICON || selectedTool == Toolbox.Tool.GROUP || selectedTool == Toolbox.Tool.ARROW || selectedTool == Toolbox.Tool.LABEL)) {
            sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            sb.setColor(this.hoverItemColor);
            if (selectedTool == Toolbox.Tool.ARROW) {
                if (addingArrow == null) {
                    float x = getScreenX(MathUtils.round(getGridX(mouseX) * 4) / 4f);
                    float y = getScreenY(MathUtils.round(getGridY(mouseY) * 4) / 4f);
                    float width = IntentGraphMod.GRID_SIZE * scale / 2;
                    float height = IntentGraphMod.GRID_SIZE * scale / 2;
                    sb.draw(ImageMaster.WHITE_SQUARE_IMG, x - width / 2, y - height / 2, width, height);
                } else {
                    float[] path = addingArrow.path;
                    if (path.length % 2 != path[0]) { // horizontal
                        float ex = getScreenX(MathUtils.round(getGridX(mouseX) * 4) / 4f);
                        float sx = getScreenX(addingArrow.getLastX());
                        float y = getScreenY(addingArrow.getLastY());
                        float height = IntentGraphMod.GRID_SIZE * scale / 2;
                        sb.draw(ImageMaster.WHITE_SQUARE_IMG, Math.min(sx, ex), y - height / 2, Math.abs(ex - sx), height);
                    } else { // vertical
                        float ey = getScreenY(MathUtils.round(getGridY(mouseY) * 4) / 4f);
                        float sy = getScreenY(addingArrow.getLastY());
                        float x = getScreenX(addingArrow.getLastX());
                        float width = IntentGraphMod.GRID_SIZE * scale / 2;
                        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x - width / 2, Math.min(sy, ey), width, Math.abs(ey - sy));
                    }
                }
            } else {
                float x = getScreenX(MathUtils.round(getGridX(mouseX) * 2) / 2f);
                float y = getScreenY(MathUtils.round(getGridY(mouseY) * 2) / 2f);
                float width = IntentGraphMod.GRID_SIZE * scale;
                float height = IntentGraphMod.GRID_SIZE * scale;
                sb.draw(ImageMaster.WHITE_SQUARE_IMG, x - width / 2, y - height / 2, width, height);
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

    public float getGraphRenderX() {
        return this.x + 32 * Settings.scale;
    }

    public float getGraphRenderY() {
        return this.top - 64 * Settings.scale;
    }

    public List<EditableItem> getSelectedItems() {
        return selectedItems;
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
        if (isDragging) {
            return;
        }
        if (!this.selectedItems.isEmpty()) {
            List<EditableItem> targets = new ArrayList<>(this.selectedItems);
            this.undoHelper.runAndPush(
                    () -> targets.forEach(item -> item.move(x, y)),
                    () -> targets.forEach(item -> item.move(-x, -y)));
            if (this.onSelectedItemPositionChange != null) {
                this.onSelectedItemPositionChange.accept(this);
            }
        }
    }

    public void deleteSelected() {
        if (!this.selectedItems.isEmpty()) {
            deleteItems(this.selectedItems);
            this.selectedItems.clear();
            if (this.onSelectedItemChange != null) {
                this.onSelectedItemChange.accept(this);
            }
        }
    }

    private void renderItemHitBoxes(SpriteBatch sb, EditableItem item, Color color) {
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        item.renderHitBoxes(sb, color);
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private float getGridX(float x) {
        return (x - this.x - 32 * Settings.scale) / (IntentGraphMod.GRID_SIZE * Settings.scale);
    }

    private float getGridY(float y) {
        return (this.top - y - 64 * Settings.scale) / (IntentGraphMod.GRID_SIZE * Settings.scale);
    }

    private float getScreenX(float x) {
        return this.x + 32 * Settings.scale + x * IntentGraphMod.GRID_SIZE * Settings.scale;
    }

    private float getScreenY(float y) {
        return this.top - 64 * Settings.scale - y * IntentGraphMod.GRID_SIZE * Settings.scale;
    }

    private void updateDragging() {
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
            this.selectedItems.forEach(item -> item.move(deltaX, deltaY));
        }
        this.dragLastX = currentX;
        this.dragLastY = currentY;
    }

    private void updateMoveTool() {
        if (isMultiSelecting) {
            if (InputHelper.isMouseDown) {
                float currentX = InputHelper.mX;
                float currentY = InputHelper.mY;
                this.selectedItems.clear();
                this.selectedItemsChanged = true;
                addToMultiSelect(this.graphDetail.icons, currentX, currentY);
                addToMultiSelect(this.graphDetail.iconGroups, currentX, currentY);
                addToMultiSelect(this.graphDetail.arrows, currentX, currentY);
                addToMultiSelect(this.graphDetail.labels, currentX, currentY);
            } else {
                this.isMultiSelecting = false;
            }
            return;
        }

        updateEditableItems(this.graphDetail.icons);
        updateEditableItems(this.graphDetail.iconGroups);
        updateEditableItems(this.graphDetail.arrows);
        updateEditableItems(this.graphDetail.labels);
        if (InputHelper.justClickedLeft && mouseInCanvas()) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                if (this.hoveredItem != null) {
                    if (this.selectedItems.contains(this.hoveredItem)) {
                        this.selectedItems.remove(this.hoveredItem);
                    } else {
                        this.selectedItems.add(this.hoveredItem);
                    }
                }
                this.selectedItemsChanged = true;
            } else if (this.hoveredItem != null && this.selectedItems.contains(this.hoveredItem)) {
                startDragging();
            } else {
                this.selectedItems.clear();
                if (hoveredItem == null) {
                    this.isMultiSelecting = true;
                    this.multiSelectX = InputHelper.mX;
                    this.multiSelectY = InputHelper.mY;
                } else {
                    this.selectedItems.add(this.hoveredItem);
                    startDragging();
                }
                this.selectedItemsChanged = true;
            }
        }
    }

    private void updateIconTool() {
        updateEditableItems(this.graphDetail.icons);
        if (InputHelper.justClickedLeft && mouseInCanvas()) {
            InputHelper.justClickedLeft = false;
            if (!this.selectedItems.isEmpty() || this.hoveredItem != null) {
                setSingleSelectedItem(this.hoveredItem);
                startDragging();
            } else {
                this.insertItem(this.graphDetail.icons, (x, y) -> {
                    EditableIcon icon = new EditableIcon(getGraphRenderX(), getGraphRenderY());
                    icon.type = AbstractMonster.Intent.ATTACK;
                    icon.x = x - 0.5f;
                    icon.y = y - 0.5f;
                    icon.attackCount = 1;
                    return icon;
                });
            }
        }
    }

    private void updateGroupTool() {
        updateEditableItems(this.graphDetail.iconGroups);
        if (InputHelper.justClickedLeft && mouseInCanvas()) {
            InputHelper.justClickedLeft = false;
            if (!this.selectedItems.isEmpty() || this.hoveredItem != null) {
                setSingleSelectedItem(this.hoveredItem);
                startDragging();
            } else {
                this.insertItem(this.graphDetail.iconGroups, (x, y) -> {
                    EditableIconGroup group = new EditableIconGroup(getGraphRenderX(), getGraphRenderY());
                    group.x = x - 0.5f;
                    group.y = y - 0.5f;
                    group.w = 1;
                    group.h = 1;
                    return group;
                });
            }
        }
    }

    private void updateArrowTool() {
        if (addingArrow != null) {
            float x = MathUtils.round(getGridX(InputHelper.mX) * 4) / 4f;
            float y = MathUtils.round(getGridY(InputHelper.mY) * 4) / 4f;

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

            if (InputHelper.justClickedLeft && mouseInCanvas()) {
                InputHelper.justClickedLeft = false;
                if (canAddNewPoint) {
                    addingArrow.path = Arrays.copyOf(addingArrow.path, addingArrow.path.length + 1);
                    addingArrow.path[addingArrow.path.length - 1] = newValue;
                    setupAddingArrowButtons();
                }
            } else if (InputHelper.justClickedRight && mouseInCanvas()) {
                if (path.length >= 4) {
                    addingArrow.path = Arrays.copyOf(addingArrow.path, addingArrow.path.length - 1);
                    setupAddingArrowButtons();
                } else {
                    completeAddingArrow(false);
                }
            }
            return;
        }

        updateEditableItems(this.graphDetail.arrows);
        if (InputHelper.justClickedLeft && mouseInCanvas()) {
            InputHelper.justClickedLeft = false;
            if (!this.selectedItems.isEmpty() || this.hoveredItem != null) {
                setSingleSelectedItem(this.hoveredItem);
                startDragging();
            } else {
                float x = MathUtils.round(getGridX(InputHelper.mX) * 4) / 4f;
                float y = MathUtils.round(getGridY(InputHelper.mY) * 4) / 4f;
                addingArrow = new EditableArrow(getGraphRenderX(), getGraphRenderY());
                addingArrow.path = new float[] {0, x, y};
                graphDetail.arrows.add(addingArrow);
            }
        }
    }

    private void updateLabelTool() {
        updateEditableItems(this.graphDetail.labels);
        if (InputHelper.justClickedLeft && mouseInCanvas()) {
            InputHelper.justClickedLeft = false;
            if (!this.selectedItems.isEmpty() || this.hoveredItem != null) {
                setSingleSelectedItem(this.hoveredItem);
                startDragging();
            } else {
                this.insertItem(this.graphDetail.labels, (x, y) -> {
                    EditableLabel label = new EditableLabel(getGraphRenderX(), getGraphRenderY());
                    label.x = x;
                    label.y = y;
                    label.label = "Label";
                    label.align = "middle";
                    return label;
                });
            }
        }
    }

    private void setSingleSelectedItem(EditableItem item) {
        this.selectedItems.clear();
        if (item != null) {
            this.selectedItems.add(item);
        }
        this.selectedItemsChanged = true;
    }

    private void startDragging() {
        this.isDragging = true;
        this.dragStartX = this.dragLastX = InputHelper.mX;
        this.dragStartY = this.dragLastY = InputHelper.mY;
    }

    private void completeDragging(boolean apply) {
        if (!isDragging) {
            return;
        }

        isDragging = false;
        float lastGridX = Math.round((dragLastX - dragStartX) * 2 / (IntentGraphMod.GRID_SIZE * Settings.scale)) / 2f;
        float lastGridY = -Math.round((dragLastY - dragStartY) * 2 / (IntentGraphMod.GRID_SIZE * Settings.scale)) / 2f;
        if (lastGridX != 0 || lastGridY != 0) {
            // Reset item position and move with undo-redo
            this.selectedItems.forEach(item -> item.move(-lastGridX, -lastGridY));
            if (apply) {
                this.moveSelected(lastGridX, lastGridY);
            }
        }
    }

    private void setupAddingArrowButtons() {
        addArrowButton = new Button(EditIntentGraphScreen.getButtonImage(13),
                getScreenX(addingArrow.getLastX()) + 10 * Settings.scale,
                getScreenY(addingArrow.getLastY()) - 52 * Settings.scale,
                42 * Settings.scale,
                42 * Settings.scale);
        cancelArrowButton = new Button(EditIntentGraphScreen.getButtonImage(2),
                getScreenX(addingArrow.getLastX()) + 60 * Settings.scale,
                getScreenY(addingArrow.getLastY()) - 52 * Settings.scale,
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

        graphDetail.arrows.remove(addingArrow);
        if (apply) {
            this.insertItem(this.graphDetail.arrows, (x, y) -> addingArrow);
        }
        addingArrow = null;
    }

    private <T extends EditableItem> void insertItem(ArrayList<T> list, BiFunction<Float, Float, T> constructor) {
        float x = MathUtils.round(getGridX(InputHelper.mX) * 2) / 2f;
        float y = MathUtils.round(getGridY(InputHelper.mY) * 2) / 2f;
        T item = constructor.apply(x, y);
        item.updateHitBoxesLocation();
        this.undoHelper.runAndPush(
                () -> list.add(item),
                () -> {
                    list.remove(item);
                    if (this.selectedItems.contains(item)) {
                        this.selectedItems.remove(item);
                        if (this.onSelectedItemChange != null) {
                            this.onSelectedItemChange.accept(this);
                        }
                    }
                });
        this.setSingleSelectedItem(item);
    }

    private void updateDeleteTool() {
        updateEditableItems(this.graphDetail.icons);
        updateEditableItems(this.graphDetail.iconGroups);
        updateEditableItems(this.graphDetail.arrows);
        updateEditableItems(this.graphDetail.labels);
        if (InputHelper.justClickedLeft && this.hoveredItem != null) {
            deleteItem(this.hoveredItem);
        }
    }

    private void deleteItem(EditableItem item) {
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

    private <T extends EditableItem> void updateEditableItems(ArrayList<T> items) {
        for (T item : items) {
            item.update();
            if (item.isHovered()) {
                this.hoveredItem = item;
            }
        }
    }

    private <T extends EditableItem> void addToMultiSelect(ArrayList<T> items, float currentX, float currentY) {
        for (T item : items) {
            if (item.isInRect(this.multiSelectX, this.multiSelectY, currentX, currentY)) {
                this.selectedItems.add(item);
                this.selectedItemsChanged = true;
            }
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

    private boolean mouseInCanvas() {
        return InputHelper.mX > this.x && InputHelper.mX < this.x + this.width && InputHelper.mY > 0 && InputHelper.mY < this.top;
    }

    private static class DeleteItemRecord {
        EditableItem item;
        ArrayList<? extends EditableItem> list;
        int index;
    }
}
