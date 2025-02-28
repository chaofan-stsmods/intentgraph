package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.chaofan.sts.intentgraph.model.Damage;
import io.chaofan.sts.intentgraph.model.DamageProvider;
import io.chaofan.sts.intentgraph.model.Icon;
import io.chaofan.sts.intentgraph.model.editor.EditableIcon;
import io.chaofan.sts.intentgraph.model.editor.EditableMonsterGraphDetail;
import io.chaofan.sts.intentgraph.model.editor.UndoRedoHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IconPropertiesControl extends PropertiesControl implements DamageProvider {
    private static final Matrix4 oldProjectionMatrix = new Matrix4();
    private static final Matrix4 tmpMatrix = new Matrix4();

    private final float x;
    private final TextField iconX;
    private final TextField iconY;
    private final TextField percentage;
    private final TextField limit;
    private final ComboBox limitType;
    private final TextField attackCount;
    private final TextField attackCountString;
    private final ComboBox damage;
    private final TextField damageMin;
    private final TextField damageMax;
    private final TextField damageString;
    private final Button[] intentButtons = new Button[AbstractMonster.Intent.values().length];
    private final Button addDamage;
    private final Button removeDamage;

    private EditableMonsterGraphDetail graphDetail;
    private EditableIcon icon;

    private final Icon renderingIcon = new Icon();
    private final Damage renderingDamage = new Damage();

    private final float maxScroll;
    private float currentScroll = 0;
    private float targetScroll = 0;

    public IconPropertiesControl(float x, float top, UndoRedoHelper undoRedoHelper) {
        super(undoRedoHelper);
        this.x = x;
        this.iconX = new TextField("X", x, top - TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.iconY = new TextField("Y", x, top - 2 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.percentage = new TextField(TEXT[2], x, top - 3 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.limit = new TextField(TEXT[3], x, top - 4 * TEXT_FIELD_HEIGHT, 350 * Settings.scale, 100 * Settings.scale);
        this.limitType = new ComboBox("", x, top - 4 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 145 * Settings.scale);
        this.attackCount = new TextField(TEXT[4], x, top - 5 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.attackCountString = new TextField(TEXT[5], x, top - 6 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.damage = new ComboBox(TEXT[6], x, top - 7 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale - 2 * TEXT_FIELD_HEIGHT);
        this.damageMin = new TextField(TEXT[7], x, top - 8 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.damageMax = new TextField(TEXT[8], x, top - 9 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.damageString = new TextField(TEXT[9], x, top - 10 * TEXT_FIELD_HEIGHT, 200 * Settings.scale, 250 * Settings.scale);
        this.addDamage = new Button(EditIntentGraphScreen.getButtonImage(9), x + 450 * Settings.scale - 2 * TEXT_FIELD_HEIGHT, top - 7 * TEXT_FIELD_HEIGHT, TEXT_FIELD_HEIGHT, TEXT_FIELD_HEIGHT);
        this.removeDamage = new Button(EditIntentGraphScreen.getButtonImage(2), x + 450 * Settings.scale - TEXT_FIELD_HEIGHT, top - 7 * TEXT_FIELD_HEIGHT, TEXT_FIELD_HEIGHT, TEXT_FIELD_HEIGHT);

        this.limitType.setOptions(Arrays.asList(TEXT[22], TEXT[23]));

        this.iconX.setOnChange(changeFloatListener(() -> icon, (i) -> i.x, (i, value) -> i.x = value));
        this.iconY.setOnChange(changeFloatListener(() -> icon, (i) -> i.y, (i, value) -> i.y = value));
        this.percentage.setOnChange(changeIntListener(() -> icon, (i) -> i.percentage, (i, value) -> i.percentage = value));
        this.limit.setOnChange(changeIntListener(() -> icon, (i) -> i.limit, (i, value) -> i.limit = value));
        this.limitType.setOnChange(this::onChangeLimitType);
        this.attackCount.setOnChange(changeIntListener(() -> icon, (i) -> i.attackCount, (i, value) -> i.attackCount = value));
        this.attackCountString.setOnChange(changeStringListener(() -> icon, (i) -> i.attackCountString, (i, value) -> i.attackCountString = value.isEmpty() ? null : value));
        this.damage.setOnChange(this::onChangeDamage);
        this.damageMin.setOnChange(changeIntListener(() -> graphDetail.damages.get(icon.damageIndex), (d) -> d.min, (d, value) -> { d.min = value; updateDamageList(); }));
        this.damageMax.setOnChange(changeIntListener(() -> graphDetail.damages.get(icon.damageIndex), (d) -> d.max, (d, value) -> { d.max = value; updateDamageList(); }));
        this.damageString.setOnChange(changeStringListener(() -> graphDetail.damages.get(icon.damageIndex), (d) -> d.string, (d, value) -> { d.string = value.isEmpty() ? null : value; updateDamageList(); }));
        this.addDamage.setOnClick(this::onAddDamage);
        this.removeDamage.setOnClick(this::onRemoveDamage);

        int intentsPerRow = 5;
        for (int i = 0; i < AbstractMonster.Intent.values().length; i++) {
            int intentX = i % intentsPerRow;
            int intentY = i / intentsPerRow;
            intentButtons[i] = new Button((TextureRegion) null, x + (25 + intentX * 80) * Settings.scale, top - (80 + intentY * 80) * Settings.scale - 10 * TEXT_FIELD_HEIGHT, 80 * Settings.scale, 80 * Settings.scale);
            int intentIndex = i;
            intentButtons[i].setOnClick((button) -> {
                EditableIcon target = icon;
                AbstractMonster.Intent oldType = target.type;
                AbstractMonster.Intent newType = AbstractMonster.Intent.values()[intentIndex];
                undoRedoHelper.runAndPush(
                        () -> target.type = newType,
                        () -> target.type = oldType);
            });
            intentButtons[i].setOnRender((button, sb) -> {
                renderingIcon.type = AbstractMonster.Intent.values()[intentIndex];
                Icon.renderIconImage(this, sb, renderingIcon, button.getX() + 8 * Settings.scale, button.getY() + 8 * Settings.scale);
            });
        }

        float maxY = intentButtons[intentButtons.length - 1].getY();
        this.maxScroll = Math.max(0, -maxY + 50 * Settings.scale);
    }

    public void setIcon(EditableMonsterGraphDetail graphDetail, EditableIcon icon) {
        if (this.graphDetail != graphDetail || this.icon != icon) {
            this.graphDetail = graphDetail;
            this.icon = icon;
            refresh();
        }
    }

    public void refresh() {
        this.iconX.setText(String.valueOf(icon.x));
        this.iconY.setText(String.valueOf(icon.y));
        this.percentage.setText(String.valueOf(icon.percentage));
        this.limit.setText(String.valueOf(icon.limit));
        this.limitType.setSelection(icon.limitType == null ? 0 : icon.limitType.ordinal());
        this.attackCount.setText(String.valueOf(icon.attackCount));
        this.attackCountString.setText(icon.attackCountString != null ? icon.attackCountString : "");
        this.updateDamageIfCurrentIconIs(icon);
    }

    public void update() {
        currentScroll = MathHelper.scrollSnapLerpSpeed(currentScroll, targetScroll);
        int oldMouseY = InputHelper.mY;
        InputHelper.mY -= (int) currentScroll;

        iconX.update();
        iconY.update();
        percentage.update();
        limit.update();
        limitType.update();
        if (icon.isAttack()) {
            attackCount.update();
            attackCountString.update();
            damage.update();
            damageMin.update();
            damageMax.update();
            damageString.update();
            addDamage.update();
            removeDamage.update();
        }
        for (Button intentButton : intentButtons) {
            intentButton.update();
        }

        InputHelper.mY = oldMouseY;
    }

    public void render(SpriteBatch sb) {
        oldProjectionMatrix.set(sb.getProjectionMatrix());
        tmpMatrix.set(oldProjectionMatrix);
        tmpMatrix.translate(0, currentScroll, 0);
        sb.setProjectionMatrix(tmpMatrix);

        iconX.render(sb);
        iconY.render(sb);
        percentage.render(sb);
        limit.render(sb);
        limitType.render(sb);
        if (icon.isAttack()) {
            attackCount.render(sb);
            attackCountString.render(sb);
            damage.render(sb);
            damageMin.render(sb);
            damageMax.render(sb);
            damageString.render(sb);
            addDamage.render(sb);
            removeDamage.render(sb);
        }
        for (int i = 0, intentButtonsLength = intentButtons.length; i < intentButtonsLength; i++) {
            Button intentButton = intentButtons[i];
            if (icon.type != null && icon.type.ordinal() == i) {
                sb.setColor(1, 1, 1, 0.5f);
                sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                        intentButton.getX() + 8 * Settings.scale, intentButton.getY() + 8 * Settings.scale,
                        64 * Settings.scale, 64 * Settings.scale);
            }
            intentButton.render(sb);
        }

        sb.setProjectionMatrix(oldProjectionMatrix);
    }

    @Override
    public Damage getDamage(int index) {
        return this.renderingDamage;
    }

    @Override
    public boolean scrolled(int amount) {
        if (InputHelper.mX > this.x) {
            targetScroll = MathUtils.clamp(targetScroll + amount * 40, 0, maxScroll);
            return true;
        }
        return false;
    }

    private void onChangeLimitType(ComboBox comboBox) {
        EditableIcon target = icon;
        Icon.LimitType oldType = target.limitType;
        int selection = comboBox.getSelection();
        Icon.LimitType newType = selection == 0 ? null : Icon.LimitType.values()[selection];
        undoRedoHelper.runAndPush(() -> {
            target.limitType = newType;
            if (icon == target) {
                comboBox.setSelection(selection);
            }
        }, () -> {
            target.limitType = oldType;
            if (icon == target) {
                comboBox.setSelection(oldType == null ? 0 : oldType.ordinal());
            }
        });
    }

    private void updateDamageList() {
        this.damage.setOptions(graphDetail.damages.stream().map(this::damageToString).collect(Collectors.toList()));
    }

    private String damageToString(Damage d) {
        if (d.string != null) {
            return d.string;
        }
        if (d.min == d.max) {
            return String.valueOf(d.min);
        }
        return d.min + " ~ " + d.max;
    }

    private void onChangeDamage(ComboBox comboBox) {
        EditableIcon target = icon;
        int oldIndex = target.damageIndex;
        int newIndex = comboBox.getSelection();
        undoRedoHelper.runAndPush(() -> {
            target.damageIndex = newIndex;
            if (icon == target) {
                comboBox.setSelection(newIndex);
                updateDamageTextFields(newIndex);
            }
        }, () -> {
            target.damageIndex = oldIndex;
            if (icon == target) {
                comboBox.setSelection(oldIndex);
                updateDamageTextFields(oldIndex);
            }
        });
    }

    private void updateDamageTextFields(int index) {
        damageMin.setText(String.valueOf(graphDetail.damages.get(index).min));
        damageMax.setText(String.valueOf(graphDetail.damages.get(index).max));
        damageString.setText(graphDetail.damages.get(index).string != null ? graphDetail.damages.get(index).string : "");
    }

    private void onAddDamage(Button button) {
        EditableMonsterGraphDetail graph = graphDetail;
        EditableIcon target = icon;
        int oldIndex = target.damageIndex;
        int newIndex = graph.damages.size();
        undoRedoHelper.runAndPush(() -> {
            graph.damages.add(new Damage());
            target.damageIndex = newIndex;
            updateDamageIfCurrentIconIs(target);
        }, () -> {
            graph.damages.remove(newIndex);
            target.damageIndex = oldIndex;
            updateDamageIfCurrentIconIs(target);
        });
    }

    private void onRemoveDamage(Button button) {
        if (graphDetail.damages.size() <= 1) {
            return;
        }

        EditableMonsterGraphDetail graph = graphDetail;
        Map<EditableIcon, Integer> oldDamageIndices = new HashMap<>();
        int oldDamageIndex = icon.damageIndex;
        Damage oldDamage = graph.damages.get(oldDamageIndex);
        undoRedoHelper.runAndPush(() -> {
            graph.damages.remove(oldDamageIndex);
            oldDamageIndices.clear();
            for (EditableIcon icon : graph.icons) {
                if (icon.damageIndex >= oldDamageIndex) {
                    oldDamageIndices.put(icon, icon.damageIndex);
                    icon.damageIndex--;
                    if (icon.damageIndex < 0) {
                        icon.damageIndex = 0;
                    }
                    updateDamageIfCurrentIconIs(icon);
                }
            }
        }, () -> {
            graph.damages.add(oldDamageIndex, oldDamage);
            for (Map.Entry<EditableIcon, Integer> entry : oldDamageIndices.entrySet()) {
                EditableIcon icon = entry.getKey();
                icon.damageIndex = entry.getValue();
                updateDamageIfCurrentIconIs(icon);
            }
        });
    }

    private void updateDamageIfCurrentIconIs(EditableIcon icon) {
        if (icon == this.icon) {
            updateDamageList();
            damage.setSelection(icon.damageIndex);
            updateDamageTextFields(icon.damageIndex);
        }
    }
}
