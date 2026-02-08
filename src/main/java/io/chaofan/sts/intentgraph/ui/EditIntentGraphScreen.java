package io.chaofan.sts.intentgraph.ui;

import basemod.BaseMod;
import basemod.abstracts.CustomScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.chaofan.sts.intentgraph.IntentGraphMod;
import io.chaofan.sts.intentgraph.model.Damage;
import io.chaofan.sts.intentgraph.model.MonsterGraphDetail;
import io.chaofan.sts.intentgraph.model.MonsterIntentGraph;
import io.chaofan.sts.intentgraph.model.editor.*;
import io.chaofan.sts.intentgraph.patches.DisableInputActionPatch;
import io.chaofan.sts.intentgraph.utils.BooleanIgnoreFalseTypeAdapter;
import io.chaofan.sts.intentgraph.utils.IntegerIgnoreZeroTypeAdapter;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.chaofan.sts.intentgraph.IntentGraphMod.getImagePath;

public class EditIntentGraphScreen extends CustomScreen {
    public static final String[] TEXT = CardCrawlGame.languagePack.getUIString("intentgraph:EditIntentScreen").TEXT;

    private static final Texture buttonTexture = ImageMaster.loadImage(getImagePath("ui/button.png"));

    private final UndoRedoHelper undoHelper = new UndoRedoHelper();

    private final EditorControl editorControl = new EditorControl(50 * Settings.scale, Settings.HEIGHT - 500 * Settings.scale);
    private final Toolbox toolbox = new Toolbox(50 * Settings.scale, Settings.HEIGHT - 500 * Settings.scale);
    private final EditorCanvas editorCanvas = new EditorCanvas(350 * Settings.scale, Settings.HEIGHT - 250 * Settings.scale, Settings.WIDTH - 870 * Settings.scale, toolbox, undoHelper);
    private final GraphPropertiesControl graphPropertiesControl = new GraphPropertiesControl(Settings.WIDTH - 500 * Settings.scale, Settings.HEIGHT - 250 * Settings.scale, undoHelper);
    private final IconPropertiesControl iconPropertiesControl = new IconPropertiesControl(Settings.WIDTH - 500 * Settings.scale, Settings.HEIGHT - 250 * Settings.scale, undoHelper);
    private final IconGroupPropertiesControl iconGroupPropertiesControl = new IconGroupPropertiesControl(Settings.WIDTH - 500 * Settings.scale, Settings.HEIGHT - 250 * Settings.scale, undoHelper);
    private final LabelPropertiesControl labelPropertiesControl = new LabelPropertiesControl(Settings.WIDTH - 500 * Settings.scale, Settings.HEIGHT - 250 * Settings.scale, undoHelper);
    private final ArrowPropertiesControl arrowPropertiesControl = new ArrowPropertiesControl(Settings.WIDTH - 500 * Settings.scale, Settings.HEIGHT - 250 * Settings.scale, undoHelper);
    private PropertiesControl propertyControl = null;

    private final HashMap<String, EditableMonsterIntentGraph> intents = new HashMap<>();
    private String editingMonsterId;
    private EditableMonsterIntentGraph monsterIntentGraph;

    private float saveTimer = 0;
    private String saveString = TEXT[24];
    private final Color saveTimerColor = Color.WHITE.cpy();

    public EditIntentGraphScreen() {
        editorControl.setOnExit(this::exit);
        editorControl.setOnSave(this::save);
        editorControl.setOnAscensionChange(this::onAscensionChange);
        editorControl.setOnAdd(this::onAdd);
        editorControl.setOnRemove(this::onRemove);
        editorControl.setOnUndo(undoHelper::undo);
        editorControl.setOnRedo(undoHelper::redo);
        editorCanvas.setOnSelectedItemChange(this::onCanvasSelectedItemChange);
        editorCanvas.setOnSelectedItemPositionChange(this::onCanvasSelectedItemPositionChange);
    }

    @Override
    public AbstractDungeon.CurrentScreen curScreen() {
        return Enums.EDIT_INTENT_GRAPH_SCREEN;
    }

    @SuppressWarnings("unused")
    private void open(String monsterId) {
        reopen();

        editingMonsterId = monsterId;
        monsterIntentGraph = intents.get(monsterId);
        AbstractMonster monster = AbstractDungeon.getMonsters().monsters.stream().filter(m -> m.id.equals(monsterId)).findFirst().orElse(null);
        String monsterName = monster != null ? monster.name : "";
        if (monsterIntentGraph == null) {
            MonsterIntentGraph intentGraph = IntentGraphMod.instance.getIntentGraph(monsterId);
            if (intentGraph == null) {
                monsterIntentGraph = new EditableMonsterIntentGraph();
                EditableMonsterGraphDetail graphDetail = new EditableMonsterGraphDetail(editorCanvas.getGraphRenderX(), editorCanvas.getGraphRenderY(), monsterName);
                graphDetail.damages.add(new Damage());
                for (int i = 0; i <= 20; i++) {
                    monsterIntentGraph.graphs.put(i, graphDetail);
                }
            } else {
                intentGraph.initMonsterGraphDetail(monster != null ? monster.type : AbstractMonster.EnemyType.NORMAL);
                monsterIntentGraph = new EditableMonsterIntentGraph(editorCanvas.getGraphRenderX(), editorCanvas.getGraphRenderY(), intentGraph, monsterName);
            }
            intents.put(monsterId, monsterIntentGraph);
        }
        onAscensionChange(editorControl.getAscension());
        undoHelper.clear();
    }

    public void openScreen(String monsterId) {
        BaseMod.openCustomScreen(Enums.EDIT_INTENT_GRAPH_SCREEN, monsterId);
    }

    @Override
    public void reopen() {
        AbstractDungeon.screen = curScreen();
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.overlayMenu.showBlackScreen();
        Gdx.input.setInputProcessor(new EditScreenInputProcessor());
        TextField.hoverField = null;
        ComboBox.openedComboBox = null;
        DisableInputActionPatch.disabled = true;
    }

    @Override
    public void close() {
        genericScreenOverlayReset();
        InputHelper.regainInputFocus();
        TextField.hoverField = null;
        ComboBox.openedComboBox = null;
        DisableInputActionPatch.disabled = false;
    }

    @Override
    public void update() {
        if (saveTimer > 0) {
            saveTimer -= Gdx.graphics.getDeltaTime();
            if (saveTimer <= 0) {
                saveTimer = 0;
            }
        }

        if (ComboBox.updateOpened()) {
            return;
        }

        if (propertyControl != null) {
            propertyControl.update();
        }
        editorControl.update();
        toolbox.update();
        editorCanvas.update();

        updateShortcuts();
    }

    private void updateShortcuts() {
        Input input = Gdx.input;
        boolean isTextFieldInactive = TextField.hoverField == null;
        if (input.isKeyPressed(Input.Keys.CONTROL_LEFT) || input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            if (input.isKeyJustPressed(Input.Keys.Z)) {
                if (isTextFieldInactive) {
                    if (input.isKeyPressed(Input.Keys.SHIFT_LEFT) || input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                        undoHelper.redo();
                    } else {
                        undoHelper.undo();
                    }
                    if (propertyControl != null) {
                        propertyControl.refresh();
                    }
                }
            } else if (input.isKeyJustPressed(Input.Keys.Y)) {
                if (isTextFieldInactive) {
                    undoHelper.redo();
                }
                if (propertyControl != null) {
                    propertyControl.refresh();
                }
            } else if (input.isKeyJustPressed(Input.Keys.S)) {
                save();
            }
        }

        if (isTextFieldInactive) {
            if (input.isKeyJustPressed(Input.Keys.LEFT)) {
                editorCanvas.moveSelected(-0.25f, 0);
            } else if (input.isKeyJustPressed(Input.Keys.RIGHT)) {
                editorCanvas.moveSelected(0.25f, 0);
            } else if (input.isKeyJustPressed(Input.Keys.UP)) {
                editorCanvas.moveSelected(0, -0.25f);
            } else if (input.isKeyJustPressed(Input.Keys.DOWN)) {
                editorCanvas.moveSelected(0, 0.25f);
            } else if (input.isKeyJustPressed(Input.Keys.FORWARD_DEL)) {
                editorCanvas.deleteSelected();
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (propertyControl != null) {
            propertyControl.render(sb);
        }
        editorControl.render(sb);
        toolbox.render(sb);
        editorCanvas.render(sb);

        if (saveTimer > 1) {
            FontHelper.renderFontLeftDownAligned(sb, FontHelper.cardTitleFont, saveString,
                    50 * Settings.scale, Settings.HEIGHT - 240 * Settings.scale,
                    Color.WHITE);
        } else if (saveTimer > 0) {
            saveTimerColor.a = saveTimer;
            FontHelper.renderFontLeftDownAligned(sb, FontHelper.cardTitleFont, saveString,
                    50 * Settings.scale, Settings.HEIGHT - 210 * Settings.scale - 30 * Settings.scale * saveTimer,
                    saveTimerColor);
        }

        ComboBox.renderOpened(sb);
    }

    @Override
    public void openingSettings() {
        AbstractDungeon.previousScreen = curScreen();
    }

    public static TextureRegion getButtonImage(int index) {
        return new TextureRegion(buttonTexture, 64 * (index % 4), 64 * (index / 4), 64, 64);
    }

    private void onAscensionChange(int ascension) {
        EditableMonsterGraphDetail graphDetail = monsterIntentGraph.graphs.get(ascension);
        editorCanvas.setGraphDetail(editingMonsterId, graphDetail);
        editorControl.setShowAdd(graphDetail.ascensionLevel != ascension);
    }

    private void save() {
        try {
            Map<String, MonsterIntentGraph> intents = new HashMap<>();
            Map<String, String> intentStrings = new HashMap<>();

            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(Boolean.class, new BooleanIgnoreFalseTypeAdapter())
                    .registerTypeAdapter(Integer.class, new IntegerIgnoreZeroTypeAdapter())
                    .create();
            try {
                String json = Gdx.files.local(IntentGraphMod.INTENTGRAPH_INTENTS_DEV_JSON).readString(String.valueOf(StandardCharsets.UTF_8));
                Type intentType = (new TypeToken<Map<String, MonsterIntentGraph>>() {}).getType();
                intents.putAll(gson.fromJson(json, intentType));
            } catch (Exception ex) {
                if (!ex.getMessage().contains("File not found") && !(ex.getCause() instanceof FileNotFoundException)) {
                    throw ex;
                }
            }

            try {
                String json = Gdx.files.local(IntentGraphMod.INTENTGRAPH_INTENT_STRINGS_DEV_JSON).readString(String.valueOf(StandardCharsets.UTF_8));
                Type intentType = (new TypeToken<Map<String, String>>() {}).getType();
                intentStrings.putAll(gson.fromJson(json, intentType));
            } catch (Exception ex) {
                if (!ex.getMessage().contains("File not found") && !(ex.getCause() instanceof FileNotFoundException)) {
                    throw ex;
                }
            }

            intents.put(editingMonsterId, monsterIntentGraph.save());
            Map<String, String> localizedStrings = monsterIntentGraph.getLocalizedStrings();
            intentStrings.putAll(localizedStrings);

            for (MonsterIntentGraph graph : intents.values()) {
                for (MonsterGraphDetail detail : graph.graphList) {
                    if (detail.id.matches("default\\d+")) {
                        detail.id = null;
                    }
                }
            }

            Gdx.files.local(IntentGraphMod.INTENTGRAPH_INTENTS_DEV_JSON).writeString(gson.toJson(intents), false);
            Gdx.files.local(IntentGraphMod.INTENTGRAPH_INTENT_STRINGS_DEV_JSON).writeString(gson.toJson(intentStrings), false);

            IntentGraphMod.instance.loadIntents();

            saveTimer = 2;
            saveString = TEXT[24];
        } catch (Exception ex) {
            IntentGraphMod.logger.info(ex);
            saveTimer = 3;
            saveString = TEXT[25];
        }
    }

    private void exit() {
        AbstractDungeon.closeCurrentScreen();
    }

    private void onAdd() {
        int ascension = editorControl.getAscension();
        if (ascension == 0) {
            return;
        }
        EditableMonsterGraphDetail oldGraphDetail = monsterIntentGraph.graphs.get(ascension);
        EditableMonsterGraphDetail newGraphDetail = new EditableMonsterGraphDetail(editorCanvas.getGraphRenderX(), editorCanvas.getGraphRenderY(), oldGraphDetail);
        newGraphDetail.ascensionLevel = ascension;
        undoHelper.runAndPush(
                () -> setGraphForAscension(ascension, newGraphDetail),
                () -> setGraphForAscension(ascension, monsterIntentGraph.graphs.get(ascension - 1)));
    }

    private void onRemove() {
        int ascension = editorControl.getAscension();
        if (ascension == 0) {
            return;
        }
        EditableMonsterGraphDetail oldGraphDetail = monsterIntentGraph.graphs.get(ascension);
        undoHelper.runAndPush(
                () -> setGraphForAscension(ascension, monsterIntentGraph.graphs.get(ascension - 1)),
                () -> setGraphForAscension(ascension, oldGraphDetail));
    }

    private void setGraphForAscension(int ascension, EditableMonsterGraphDetail graphDetail) {
        EditableMonsterGraphDetail oldGraphDetail = monsterIntentGraph.graphs.get(ascension);
        for (int i = ascension; i <= 20; i++) {
            if (monsterIntentGraph.graphs.get(i) == oldGraphDetail) {
                monsterIntentGraph.graphs.put(i, graphDetail);
            } else {
                break;
            }
        }
        onAscensionChange(editorControl.getAscension());
    }

    private void onCanvasSelectedItemChange(EditorCanvas canvas) {
        TextField.hoverField = null;
        ComboBox.openedComboBox = null;

        List<EditableItem> selectedItems = editorCanvas.getSelectedItems();
        EditableItem selectedItem = selectedItems.size() != 1 ? null : selectedItems.get(0);
        if (selectedItem == null) {
            graphPropertiesControl.setGraphDetail(editorCanvas.getGraphDetail());
            propertyControl = graphPropertiesControl;
        } else if (selectedItem instanceof EditableIcon) {
            iconPropertiesControl.setIcon(editorCanvas.getGraphDetail(), (EditableIcon) selectedItem);
            propertyControl = iconPropertiesControl;
        } else if (selectedItem instanceof EditableIconGroup) {
            iconGroupPropertiesControl.setGroup((EditableIconGroup) selectedItem);
            propertyControl = iconGroupPropertiesControl;
        } else if (selectedItem instanceof EditableLabel) {
            labelPropertiesControl.setLabel(editorCanvas.getGraphDetail(), (EditableLabel) selectedItem);
            propertyControl = labelPropertiesControl;
        } else if (selectedItem instanceof EditableArrow) {
            arrowPropertiesControl.setArrow((EditableArrow) selectedItem);
            propertyControl = arrowPropertiesControl;
        } else {
            propertyControl = null;
        }
    }

    private void onCanvasSelectedItemPositionChange(EditorCanvas editorCanvas) {
        if (propertyControl != null && editorCanvas.getSelectedItems().size() == 1) {
            propertyControl.refresh();
        }
    }

    public boolean scrolled(int amount) {
        if (propertyControl != null) {
            return propertyControl.scrolled(amount);
        }
        return false;
    }

    public static class Enums {
        @SpireEnum
        public static AbstractDungeon.CurrentScreen EDIT_INTENT_GRAPH_SCREEN;
    }
}
