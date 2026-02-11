package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import io.chaofan.sts.intentgraph.model.editor.EditableArrow;
import io.chaofan.sts.intentgraph.model.editor.UndoRedoHelper;

import java.util.ArrayList;

public class ArrowPropertiesControl extends PropertiesControl {
    private static final float TEXT_Y_OFFSET = 27 * Settings.scale;
    private final float x;
    private final float top;
    private final CheckBox horizontal;
    private final CheckBox instant;
    private final TextField startX;
    private final TextField startY;
    private final ArrayList<TextField> textFields = new ArrayList<>();
    private Button removeButton;
    private Button addButton;
    private EditableArrow arrow;

    public ArrowPropertiesControl(float x, float top, UndoRedoHelper undoRedoHelper) {
        super(undoRedoHelper);
        this.x = x;
        this.top = top;
        this.horizontal = new CheckBox(TEXT[16], x, top - TEXT_FIELD_HEIGHT, 450 * Settings.scale, TEXT_FIELD_HEIGHT);
        this.instant = new CheckBox(TEXT[17], x, top - 2 * TEXT_FIELD_HEIGHT, 450 * Settings.scale, TEXT_FIELD_HEIGHT);
        this.startX = new TextField(String.format(TEXT[18], 1), x, top - 3 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 100 * Settings.scale);
        this.startY = new TextField("", x, top - 3 * TEXT_FIELD_HEIGHT, 310 * Settings.scale, 100 * Settings.scale);

        this.horizontal.setOnChange(changeBoolListener(() -> arrow, (a) -> a.path[0] == 0, (a, value) -> { a.path[0] = value ? 0 : 1; updateTextFields(value); }));
        this.instant.setOnChange(changeBoolListener(() -> arrow, (a) -> a.instant, (a, value) -> a.instant = value));
        this.startX.setOnChange(changeFloatListener(() -> arrow, (a) -> a.path[1], (a, value) -> a.path[1] = value));
        this.startY.setOnChange(changeFloatListener(() -> arrow, (a) -> a.path[2], (a, value) -> a.path[2] = value));
    }

    public void setArrow(EditableArrow arrow) {
        if (this.arrow != arrow) {
            this.arrow = arrow;
            refresh();
        }
    }

    public void refresh() {
        boolean isHorizontal = arrow.path[0] == 0;
        horizontal.setChecked(isHorizontal);
        instant.setChecked(arrow.instant);
        startX.setText(String.valueOf(arrow.path[1]));
        startY.setText(String.valueOf(arrow.path[2]));
        updateTextFields(isHorizontal);
    }

    private void updateTextFields(boolean isHorizontal) {
        textFields.clear();
        for (int i = 3; i < arrow.path.length; i++) {
            int finalI = i;
            TextField textField = new TextField(String.format(TEXT[18], i - 1), x, top - (i + 1) * TEXT_FIELD_HEIGHT, (isHorizontal ? 200 : 310) * Settings.scale, 100 * Settings.scale);
            textField.setOnChange(changeFloatListener(() -> arrow, (a) -> a.path[finalI], (a, value) -> a.path[finalI] = value, () -> textFields.get(finalI - 3)));
            textField.setText(String.valueOf(arrow.path[i]));
            textFields.add(textField);
            isHorizontal = !isHorizontal;
        }

        addButton = new Button(EditIntentGraphScreen.getButtonImage(9), x + 200 * Settings.scale, top - (arrow.path.length + 1) * TEXT_FIELD_HEIGHT, TEXT_FIELD_HEIGHT, TEXT_FIELD_HEIGHT);
        addButton.setOnClick(this::onClickAddButton);

        removeButton = new Button(EditIntentGraphScreen.getButtonImage(2), x + 410 * Settings.scale, top - arrow.path.length * TEXT_FIELD_HEIGHT, TEXT_FIELD_HEIGHT, TEXT_FIELD_HEIGHT);
        removeButton.setOnClick(this::onClickRemoveButton);
    }

    private void onClickRemoveButton(Button button) {
        EditableArrow target = arrow;
        int index = target.path.length - 1;
        float oldValue = target.path[index];
        undoRedoHelper.runAndPush(() -> {
            float[] newPath = new float[target.path.length - 1];
            System.arraycopy(target.path, 0, newPath, 0, index);
            target.path = newPath;
            target.updateHitBoxesLocation();
            refresh();
        }, () -> {
            float[] newPath = new float[target.path.length + 1];
            System.arraycopy(target.path, 0, newPath, 0, index);
            newPath[index] = oldValue;
            target.path = newPath;
            target.updateHitBoxesLocation();
            refresh();
        });
    }

    private void onClickAddButton(Button button) {
        EditableArrow target = arrow;
        undoRedoHelper.runAndPush(() -> {
            float[] newPath = new float[target.path.length + 1];
            System.arraycopy(target.path, 0, newPath, 0, target.path.length);
            newPath[newPath.length - 1] = 0;
            target.path = newPath;
            target.updateHitBoxesLocation();
            refresh();
        }, () -> {
            float[] newPath = new float[target.path.length - 1];
            System.arraycopy(target.path, 0, newPath, 0, newPath.length);
            target.path = newPath;
            target.updateHitBoxesLocation();
            refresh();
        });
    }

    public void update() {
        horizontal.update();
        instant.update();
        startX.update();
        startY.update();
        for (TextField textField : textFields) {
            textField.update();
        }
        if (removeButton != null && arrow.path.length > 4) {
            removeButton.update();
        }
        if (addButton != null) {
            addButton.update();
        }
    }

    public void render(SpriteBatch sb) {
        horizontal.render(sb);
        instant.render(sb);
        startX.render(sb);
        startY.render(sb);
        boolean isHorizontal = horizontal.isChecked();
        for (int i = 0, textFieldsSize = textFields.size(); i < textFieldsSize; i++) {
            TextField textField = textFields.get(i);
            textField.render(sb);

            BitmapFont textFont = FontHelper.cardDescFont_L;
            textFont.getData().setScale(1);
            String text;
            if (i == 0 && !isHorizontal) {
                text = String.valueOf(arrow.path[1]);
            } else {
                text = String.valueOf(arrow.path[i + 3 - 1]);
            }
            FontHelper.renderFontLeftTopAligned(sb, textFont, text, x + (isHorizontal ? 310 : 200) * Settings.scale, top - (i + 4) * TEXT_FIELD_HEIGHT + TEXT_Y_OFFSET, Color.LIGHT_GRAY);
            isHorizontal = !isHorizontal;
        }
        if (removeButton != null && arrow.path.length > 4) {
            removeButton.render(sb);
        }
        if (addButton != null) {
            addButton.render(sb);
        }
    }
}
