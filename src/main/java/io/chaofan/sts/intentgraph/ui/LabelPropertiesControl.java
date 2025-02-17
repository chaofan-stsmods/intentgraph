package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import io.chaofan.sts.intentgraph.model.editor.EditableLabel;
import io.chaofan.sts.intentgraph.model.editor.EditableMonsterGraphDetail;
import io.chaofan.sts.intentgraph.model.editor.UndoRedoHelper;

import java.util.Arrays;
import java.util.List;

public class LabelPropertiesControl extends PropertiesControl {
    public static boolean showLocalizedText = true;
    private static final List<String> ALIGN = Arrays.asList("left", "middle", "right");

    private final TextField labelX;
    private final TextField labelY;
    private final ComboBox align;
    private final TextField text;
    private final TextField localizedText;
    private final CheckBox showLocalized;

    private EditableMonsterGraphDetail graphDetail;
    private EditableLabel label;

    public LabelPropertiesControl(float x, float top, UndoRedoHelper undoRedoHelper) {
        super(undoRedoHelper);
        this.labelX = new TextField("X", x, top - TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.labelY = new TextField("Y", x, top - 2 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.align = new ComboBox(TEXT[12], x, top - 3 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.text = new TextField(TEXT[13], x, top - 4 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.localizedText = new TextField(TEXT[14], x, top - 5 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.showLocalized = new CheckBox(TEXT[15], x, top - 6 * TEXT_FIELD_HEIGHT, 450 * Settings.scale, TEXT_FIELD_HEIGHT);

        this.align.setOptions(Arrays.asList(TEXT[19], TEXT[20], TEXT[21]));
        this.showLocalized.setChecked(true);

        this.labelX.setOnChange(changeFloatListener(() -> label, (l) -> l.x, (l, value) -> l.x = value));
        this.labelY.setOnChange(changeFloatListener(() -> label, (l) -> l.y, (l, value) -> l.y = value));
        this.align.setOnChange(this::onAlignChange);
        this.text.setOnChange(changeStringListener(() -> label, (l) -> l.label, (l, value) -> l.label = value));
        this.localizedText.setOnChange(changeStringListener(() -> label, (l) -> l.localizedText, (l, value) -> l.localizedText = value));
        this.showLocalized.setOnChange((checkBox) -> {
            showLocalizedText = checkBox.isChecked();
            for (EditableLabel label : graphDetail.labels) {
                label.updateHitBoxesLocation();
            }
        });
    }

    public void setLabel(EditableMonsterGraphDetail graphDetail, EditableLabel label) {
        this.graphDetail = graphDetail;
        if (this.label != label) {
            this.label = label;
            refresh();
        }
    }

    public void refresh() {
        this.labelX.setText(String.valueOf(label.x));
        this.labelY.setText(String.valueOf(label.y));
        int index = ALIGN.indexOf(label.align);
        this.align.setSelection(index == -1 ? 1 : index);
        this.text.setText(label.label);
        this.localizedText.setText(label.localizedText == null ? label.getLocalizedString(label.label) : label.localizedText);
    }

    public void update() {
        labelX.update();
        labelY.update();
        align.update();
        text.update();
        localizedText.update();
        showLocalized.update();
    }

    public void render(SpriteBatch sb) {
        labelX.render(sb);
        labelY.render(sb);
        align.render(sb);
        text.render(sb);
        localizedText.render(sb);
        showLocalized.render(sb);
    }

    private void onAlignChange(ComboBox comboBox) {
        int index = comboBox.getSelection() < 0 ? 1 : comboBox.getSelection();
        EditableLabel target = label;
        String oldValue = target.align;
        String newValue = ALIGN.get(index);
        undoRedoHelper.runAndPush(() -> {
            target.align = newValue;
            target.updateHitBoxesLocation();
            if (target == label) {
                comboBox.setSelection(index);
            }
        }, () -> {
            target.align = oldValue;
            target.updateHitBoxesLocation();
            if (target == label) {
                int i = ALIGN.indexOf(oldValue);
                comboBox.setSelection(i == -1 ? 1 : i);
            }
        });
    }
}
