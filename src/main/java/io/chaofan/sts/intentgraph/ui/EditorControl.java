package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;

import java.util.function.IntConsumer;

public class EditorControl {
    private final Button save;
    private final Button exit;
    private final Button add;
    private final Button remove;
    private final Button ascensionDown;
    private final Button ascensionUp;
    private final Button undo;
    private final Button redo;
    private final float x;
    private final float y;
    private int ascension = 0;
    private IntConsumer onAscensionChange;
    private boolean showAdd = true;

    public EditorControl(float x, float y) {
        this.save = new Button(EditIntentGraphScreen.getButtonImage(1), x, y + 146 * Settings.scale, 64 * Settings.scale, 64 * Settings.scale);
        this.exit = new Button(EditIntentGraphScreen.getButtonImage(2), x + 210 * Settings.scale, y + 146 * Settings.scale, 64 * Settings.scale, 64 * Settings.scale);
        this.add = new Button(EditIntentGraphScreen.getButtonImage(9), x + 70 * Settings.scale, y + 146 * Settings.scale, 64 * Settings.scale, 64 * Settings.scale);
        this.remove = new Button(EditIntentGraphScreen.getButtonImage(10), x + 70 * Settings.scale, y + 146 * Settings.scale, 64 * Settings.scale, 64 * Settings.scale);
        this.undo = new Button(EditIntentGraphScreen.getButtonImage(11), x, y + 76 * Settings.scale, 64 * Settings.scale, 64 * Settings.scale);
        this.redo = new Button(EditIntentGraphScreen.getButtonImage(12), x + 70 * Settings.scale, y + 76 * Settings.scale, 64 * Settings.scale, 64 * Settings.scale);
        this.ascensionDown = new Button(ImageMaster.CF_LEFT_ARROW, x + 8 * Settings.scale, y + 14 * Settings.scale, 48 * Settings.scale, 48 * Settings.scale);
        this.ascensionUp = new Button(ImageMaster.CF_RIGHT_ARROW, x + 218 * Settings.scale, y + 14 * Settings.scale, 48 * Settings.scale, 48 * Settings.scale);
        this.x = x;
        this.y = y;

        this.save.setTooltip(EditIntentGraphScreen.TEXT[0], EditIntentGraphScreen.TEXT[1]);
        this.exit.setTooltip(EditIntentGraphScreen.TEXT[2], EditIntentGraphScreen.TEXT[3]);
        this.add.setTooltip(EditIntentGraphScreen.TEXT[4], EditIntentGraphScreen.TEXT[5]);
        this.remove.setTooltip(EditIntentGraphScreen.TEXT[6], EditIntentGraphScreen.TEXT[7]);
        this.undo.setTooltip(EditIntentGraphScreen.TEXT[8], EditIntentGraphScreen.TEXT[9]);
        this.redo.setTooltip(EditIntentGraphScreen.TEXT[10], EditIntentGraphScreen.TEXT[11]);

        this.ascensionUp.setOnClick(this::onClickAscension);
        this.ascensionDown.setOnClick(this::onClickAscension);
    }

    private void onClickAscension(Button button) {
        if (button == this.ascensionUp) {
            ascension++;
        } else {
            ascension--;
        }
        if (ascension < 0) {
            ascension = 0;
        }
        if (ascension > 20) {
            ascension = 20;
        }
        if (onAscensionChange != null) {
            onAscensionChange.accept(ascension);
        }
    }

    public void update() {
        save.update();
        exit.update();
        if (ascension != 0) {
            if (showAdd) {
                add.update();
            } else {
                remove.update();
            }
        }
        ascensionDown.update();
        ascensionUp.update();
        undo.update();
        redo.update();
    }

    public void render(SpriteBatch sb) {
        save.render(sb);
        exit.render(sb);
        if (ascension != 0) {
            if (showAdd) {
                add.render(sb);
            } else {
                remove.render(sb);
            }
        }
        ascensionDown.render(sb);
        ascensionUp.render(sb);

        FontHelper.cardTitleFont.getData().setScale(1);
        FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont, CharacterSelectScreen.TEXT[6] + " " + ascension, x + 140 * Settings.scale, y + 38 * Settings.scale, Settings.CREAM_COLOR);

        undo.render(sb);
        redo.render(sb);
    }

    public void setOnSave(Runnable onSave) {
        save.setOnClick(button -> onSave.run());
    }

    public void setOnExit(Runnable onExit) {
        exit.setOnClick(button -> onExit.run());
    }

    public void setOnAdd(Runnable onAdd) {
        add.setOnClick(button -> onAdd.run());
    }

    public void setOnRemove(Runnable onRemove) {
        remove.setOnClick(button -> onRemove.run());
    }

    public void setOnUndo(Runnable onUndo) {
        undo.setOnClick(button -> onUndo.run());
    }

    public void setOnRedo(Runnable onRedo) {
        redo.setOnClick(button -> onRedo.run());
    }

    public void setShowAdd(boolean showAdd) {
        this.showAdd = showAdd;
    }

    public void setOnAscensionChange(IntConsumer onAscensionChange) {
        this.onAscensionChange = onAscensionChange;
    }

    public int getAscension() {
        if (ascension < 0) {
            return 0;
        }
        if (ascension > 20) {
            return 20;
        }
        return ascension;
    }
}
