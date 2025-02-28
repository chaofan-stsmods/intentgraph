package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.InputProcessor;
import io.chaofan.sts.intentgraph.IntentGraphMod;

public class EditScreenInputProcessor implements InputProcessor {
    @Override
    public boolean keyDown(int keycode) {
        if (TextField.hoverField != null) {
            return TextField.hoverField.keyDown(keycode);
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (TextField.hoverField != null) {
            return TextField.hoverField.keyUp(keycode);
        }

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        if (TextField.hoverField != null) {
            return TextField.hoverField.keyTyped(character);
        }

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return IntentGraphMod.editIntentGraphScreen.scrolled(amount);
    }
}
