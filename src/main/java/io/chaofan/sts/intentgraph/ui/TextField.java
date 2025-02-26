package io.chaofan.sts.intentgraph.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import io.chaofan.sts.intentgraph.IntentGraphMod;

import java.util.function.Consumer;

public class TextField {
    private static ShaderProgram shader;
    private static boolean shaderLoaded = false;

    private static final float TEXT_Y_OFFSET = 27 * Settings.scale;
    public static TextField hoverField;
    private static TextField previousTextField;
    private static boolean pressedTab = false;

    private final String label;
    private String text = "";
    private final float x;
    private final float y;
    private final float labelWidth;
    private final float textWidth;
    private final float height;
    private final Hitbox hb;

    private int pressingKey = Input.Keys.ANY_KEY;
    private float pressTimer;

    private int selectCursor = 0;
    private int cursor = 0;
    private float cursorTimer;
    private float textOffsetX = 0;

    private Consumer<TextField> onChange;

    public TextField(String label, float x, float y, float labelWidth, float textWidth) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.labelWidth = labelWidth;
        this.textWidth = textWidth;
        this.height = 38 * Settings.scale;
        this.hb = new Hitbox(x + labelWidth, y, textWidth, height);
    }

    public void update() {
        this.hb.update(this.hb.x, this.hb.y);
        if (InputHelper.justClickedLeft) {
            if (this.hb.hovered) {
                InputHelper.justClickedLeft = false;
                clickToSetCursorLocation(InputHelper.mX);
                if (TextField.hoverField != this) {
                    if (TextField.hoverField != null) {
                        TextField.hoverField.triggerOnChange();
                    }
                    TextField.hoverField = this;
                    this.cursorTimer = 1.5f;
                }
            } else if (TextField.hoverField == this) {
                triggerOnChange();
                TextField.hoverField = null;
            }
        }

        if (pressedTab) {
            if (TextField.hoverField == null) {
                pressedTab = false;
            } else if (TextField.hoverField == previousTextField) {
                TextField.hoverField.triggerOnChange();
                TextField.hoverField = this;
                this.cursorTimer = 1.5f;
                pressedTab = false;
            }
        }

        if (TextField.hoverField != this) {
            pressingKey = Input.Keys.ANY_KEY;
        } else if (pressingKey != Input.Keys.ANY_KEY) {
            pressTimer -= Gdx.graphics.getDeltaTime();
            if (pressTimer <= 0) {
                this.keyPressed(pressingKey);
                pressTimer = 0.05f;
            }
        }

        cursorTimer -= Gdx.graphics.getDeltaTime();
        if (cursorTimer <= 0) {
            cursorTimer = 1.5f;
        }

        previousTextField = this;
    }

    public void render(SpriteBatch sb) {
        Color color;
        if (TextField.hoverField == this || this.hb.hovered) {
            color = Color.WHITE;
        } else {
            color = Color.LIGHT_GRAY;
        }

        BitmapFont labelFont = FontHelper.cardTitleFont;
        labelFont.getData().setScale(1);
        FontHelper.renderFontLeftTopAligned(sb, labelFont, label, x, y + TEXT_Y_OFFSET, color);

        sb.setColor(color);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x + labelWidth, y, textWidth, 1);

        BitmapFont textFont = FontHelper.cardDescFont_L;
        textFont.getData().setScale(1);

        loadShader();
        if (shader != null) {
            sb.setShader(shader);
            shader.setUniformf("u_boxLeftBottom", x + labelWidth, y);
            shader.setUniformf("u_boxRightTop", x + labelWidth + textWidth, y + height);
        }

        int cursorMin = Math.min(cursor, selectCursor);
        int cursorMax = Math.max(cursor, selectCursor);
        FontHelper.renderFontLeftTopAligned(sb, textFont, text.substring(0, cursorMin), x + labelWidth + textOffsetX, y + TEXT_Y_OFFSET, color);
        float minCursorX = FontHelper.layout.width;
        if (cursorMin != cursorMax) {
            FontHelper.renderFontLeftTopAligned(sb, textFont, text.substring(cursorMin, cursorMax), x + labelWidth + minCursorX + textOffsetX, y + TEXT_Y_OFFSET, color);
            float oldAlpha = color.a;
            color.a = 0.3f;
            sb.setColor(color);
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, x + labelWidth + minCursorX + textOffsetX, y, FontHelper.layout.width, height);
            color.a = oldAlpha;
        } else {
            FontHelper.layout.width = 0;
        }
        float maxCursorX = minCursorX + FontHelper.layout.width;
        FontHelper.renderFontLeftTopAligned(sb, textFont, text.substring(cursorMax), x + labelWidth + maxCursorX + textOffsetX, y + TEXT_Y_OFFSET, color);
        float fullWidth = FontHelper.layout.width + maxCursorX;
        if (fullWidth + textOffsetX < textWidth) {
            textOffsetX = Math.min(0, textWidth - fullWidth);
        }
        float cursorRenderX = cursor == cursorMin ? minCursorX : maxCursorX;

        if (shader != null) {
            sb.setShader(null);
        }

        if (TextField.hoverField == this) {
            sb.setColor(color);
            if (cursorTimer >= 0.75f) {
                sb.setColor(Color.WHITE);
                sb.draw(ImageMaster.WHITE_SQUARE_IMG, x + labelWidth + cursorRenderX + textOffsetX, y, 2, height);
            }
        }

        this.hb.render(sb);
    }

    public boolean keyDown(int keycode) {
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            if (keycode == Input.Keys.V) {
                insertText(Gdx.app.getClipboard().getContents());
                return true;
            } else if (keycode == Input.Keys.C) {
                if (cursor != selectCursor) {
                    int cursorMin = Math.min(cursor, selectCursor);
                    int cursorMax = Math.max(cursor, selectCursor);
                    String text = this.text.substring(cursorMin, cursorMax);
                    Gdx.app.getClipboard().setContents(text);
                }
                return true;
            } else if (keycode == Input.Keys.X) {
                if (cursor != selectCursor) {
                    int cursorMin = Math.min(cursor, selectCursor);
                    int cursorMax = Math.max(cursor, selectCursor);
                    String text = this.text.substring(cursorMin, cursorMax);
                    Gdx.app.getClipboard().setContents(text);
                    this.deleteText();
                }
                return true;
            } else if (keycode == Input.Keys.A) {
                cursor = 0;
                selectCursor = this.text.length();
                return true;
            }
        }

        if (pressingKey != keycode) {
            pressingKey = keycode;
            pressTimer = 0.8f;
            this.keyPressed(keycode);
            return true;
        }

        return false;
    }

    public boolean keyUp(int keycode) {
        if (pressingKey == keycode) {
            pressingKey = Input.Keys.ANY_KEY;
            return true;
        }

        return false;
    }

    public boolean keyTyped(char character) {
        if (character >= 32 && character != 127) {
            insertText(String.valueOf(character));
            return true;
        }

        return false;
    }

    private void keyPressed(int keycode) {
        switch (keycode) {
            case Input.Keys.BACKSPACE:
                backspaceText();
                break;
            case Input.Keys.FORWARD_DEL:
                deleteText();
                break;
            case Input.Keys.LEFT:
                if (cursor > 0) {
                    cursor--;
                }
                if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                    selectCursor = cursor;
                }
                break;
            case Input.Keys.RIGHT:
                if (cursor < text.length()) {
                    cursor++;
                }
                if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                    selectCursor = cursor;
                }
                break;
            case Input.Keys.HOME:
            case Input.Keys.UP:
                cursor = 0;
                if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                    selectCursor = cursor;
                }
                break;
            case Input.Keys.END:
            case Input.Keys.DOWN:
                cursor = text.length();
                if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                    selectCursor = cursor;
                }
                break;
            case Input.Keys.ESCAPE:
                triggerOnChange();
                TextField.hoverField = null;
                break;
            case Input.Keys.ENTER:
                triggerOnChange();
                break;
            case Input.Keys.TAB:
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                    triggerOnChange();
                    TextField.hoverField = previousTextField;
                } else {
                    pressedTab = true;
                }
                break;
        }
    }

    private void triggerOnChange() {
        if (this.onChange != null) {
            this.onChange.accept(this);
        }
    }

    private void insertText(String text) {
        int cursorMin = Math.min(cursor, selectCursor);
        int cursorMax = Math.max(cursor, selectCursor);
        this.text = this.text.substring(0, cursorMin) + text + this.text.substring(cursorMax);
        cursor += text.length();
        selectCursor = cursor;
    }

    private void backspaceText() {
        if (cursor != selectCursor) {
            deleteText();
        } else if (cursor > 0) {
            this.text = this.text.substring(0, cursor - 1) + this.text.substring(cursor);
            cursor--;
            selectCursor = cursor;
        }
    }

    private void deleteText() {
        if (cursor != selectCursor) {
            int cursorMin = Math.min(cursor, selectCursor);
            int cursorMax = Math.max(cursor, selectCursor);
            this.text = this.text.substring(0, cursorMin) + this.text.substring(cursorMax);
            cursor = cursorMin;
            selectCursor = cursor;
        } else if (cursor < this.text.length()) {
            this.text = this.text.substring(0, cursor) + this.text.substring(cursor + 1);
        }
    }

    private void clickToSetCursorLocation(float mouseX) {
        float xInText = mouseX - (x + labelWidth + textOffsetX);
        float roughTextWidth = textWidth + textOffsetX;
        int estimatedCursor = Math.max(Math.min(Math.round(xInText / roughTextWidth * text.length()), text.length()), 0);
        BitmapFont textFont = FontHelper.cardDescFont_L;

        float lastCharWidth = 0;
        float estimatedCursorOffset = FontHelper.getWidth(textFont, text.substring(0, estimatedCursor), 1);
        if (estimatedCursorOffset > xInText) {
            while (estimatedCursor > 0 && estimatedCursorOffset > xInText) {
                lastCharWidth = FontHelper.getWidth(textFont, text.substring(estimatedCursor - 1, estimatedCursor), 1);
                estimatedCursor--;
                estimatedCursorOffset -= lastCharWidth;
            }
            if (lastCharWidth > 0 && (xInText - estimatedCursorOffset) / lastCharWidth > 0.5f) {
                estimatedCursor++;
                estimatedCursorOffset += lastCharWidth;
            }
        } else if (estimatedCursorOffset < xInText) {
            while (estimatedCursor < text.length() && estimatedCursorOffset < xInText) {
                lastCharWidth = FontHelper.getWidth(textFont, text.substring(estimatedCursor, estimatedCursor + 1), 1);
                estimatedCursor++;
                estimatedCursorOffset += lastCharWidth;
            }
            if (lastCharWidth > 0 && (estimatedCursorOffset - xInText) / lastCharWidth > 0.5f) {
                estimatedCursor--;
                estimatedCursorOffset -= lastCharWidth;
            }
        }

        cursor = selectCursor = estimatedCursor;
    }

    public void setOnChange(Consumer<TextField> onChange) {
        this.onChange = onChange;
    }

    public void setText(String text) {
        this.text = text;
        this.cursor = Math.min(cursor, text.length());
        this.selectCursor = this.cursor;
    }

    public String getText() {
        return text;
    }

    public float getFloat() {
        try {
            return Float.parseFloat(getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getInt() {
        try {
            return Integer.parseInt(getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void loadShader() {
        if (!shaderLoaded) {
            shader = new ShaderProgram(Gdx.files.internal(IntentGraphMod.getShaderPath("common.vs")), Gdx.files.internal(IntentGraphMod.getShaderPath("textField.fs")));
            shaderLoaded = true;
            if (!shader.isCompiled()) {
                IntentGraphMod.logger.warn("Shader compile failed: {}", shader.getLog());
                shader = null;
            } else {
                IntentGraphMod.logger.info("Shader compiled: {}", shader.getLog());
            }
        }
    }
}
