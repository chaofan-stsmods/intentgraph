package io.chaofan.sts.intentgraph;

import basemod.*;
import basemod.devcommands.ConsoleCommand;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import io.chaofan.sts.intentgraph.crossovers.BattleTowers;
import io.chaofan.sts.intentgraph.model.MonsterIntentGraph;
import io.chaofan.sts.intentgraph.ui.EditIntentGraphScreen;
import io.chaofan.sts.intentgraph.utils.IconRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpireInitializer
public class IntentGraphMod implements
        PostRenderSubscriber,
        PostInitializeSubscriber,
        PostBattleSubscriber,
        PostDeathSubscriber,
        EditStringsSubscriber {

    public static final String MOD_ID = "intentgraph";
    public static final Logger logger = LogManager.getLogger(IntentGraphMod.class.getName());

    private static final String UNLOCK_ALL = "UnlockAll";
    private static final String TOGGLE_KEY = "ToggleIntentGraph";
    private static final String PRESS_ANY_KEY_TO_SET = "PressAnyKeyToSet";

    public static final String INTENTGRAPH_INTENTS_DEV_JSON = "intentgraph-intents-dev.json";
    public static final String INTENTGRAPH_INTENT_STRINGS_DEV_JSON = "intentgraph-intentStrings-dev.json";

    public static String getImagePath(String file) {
        return MOD_ID + "/images/" + file;
    }

    public static String getLocalizationPath(String file) {
        return MOD_ID + "/localization/" + file;
    }

    public static String getShaderPath(String file) {
        return MOD_ID + "/shaders/" + file;
    }

    public static final int GRID_SIZE = 80;

    public static EditIntentGraphScreen editIntentGraphScreen;

    private static SpireConfig config;
    private static boolean unlockAll = false;
    private static int toggleKey = Input.Keys.F1;
    private static boolean showIntentGraph = true;
    private static final Set<String> unlockMonsterInNextCombat = new HashSet<>();

    public static List<IconRenderer> iconRenderers = new ArrayList<>();
    public static String visibleGraphMonsterId = null;

    public static void initialize() {
        logger.info("Initializing IntentGraphMod");

        IntentGraphMod mod = new IntentGraphMod();
        instance = mod;
        BaseMod.subscribe(mod);
    }

    public static void registerIconRenderer(IconRenderer iconRenderer) {
        iconRenderers.add(iconRenderer);
    }

    public static void unregisterIconRenderer(IconRenderer iconRenderer) {
        iconRenderers.remove(iconRenderer);
    }

    public final Map<String, String> intentStrings = new HashMap<>();
    private final Map<String, MonsterIntentGraph> intents = new HashMap<>();

    public static IntentGraphMod instance;
    public int overwriteAscension = -1;

    @Override
    public void receivePostInitialize() {
        ModPanel settingsPanel = initSettings();

        Texture badgeTexture = ImageMaster.loadImage(MOD_ID + "/images/badge.png");
        BaseMod.registerModBadge(badgeTexture, "Intent Graph", "Chaofan", "", settingsPanel);

        loadIntents();

        editIntentGraphScreen = new EditIntentGraphScreen();
        BaseMod.addCustomScreen(editIntentGraphScreen);

        ConsoleCommand.addCommand("reloadintents", ReloadIntentsCommand.class);
        ConsoleCommand.addCommand("editintent", EditIntentCommand.class);

        if (Loader.isModLoaded("BattleTowers")) {
            registerIconRenderer(BattleTowers::renderIcon);
        }
    }

    @Override
    public void receivePostRender(SpriteBatch spriteBatch) {
        if (AbstractDungeon.getCurrMapNode() == null || AbstractDungeon.isScreenUp) {
            return;
        }

        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.monsters == null) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(toggleKey)) {
            showIntentGraph = !showIntentGraph;
        }

        if (!showIntentGraph) {
            return;
        }

        for (AbstractMonster monster : room.monsters.monsters) {
            if (monster.hb.hovered && !monster.isDeadOrEscaped()) {
                visibleGraphMonsterId = monster.id;
                renderIntentGraphForMonster(monster, spriteBatch);
                visibleGraphMonsterId = null;
                break;
            }
        }
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        unlockMonstersInCurrentCombat();
    }

    @Override
    public void receivePostDeath() {
        unlockMonstersInCurrentCombat();
    }

    @Override
    public void receiveEditStrings() {
        BaseMod.loadCustomStringsFile(UIStrings.class, getLocalizationFilePath("ui.json"));
    }

    public MonsterIntentGraph getIntentGraph(String monsterId) {
        return intents.get(monsterId);
    }

    private void unlockMonstersInCurrentCombat() {
        for (String monsterId : unlockMonsterInNextCombat) {
            setMonsterUnlocked(monsterId);
        }

        trySaveConfig(config);
        unlockMonsterInNextCombat.clear();
    }

    private ModPanel initSettings() {
        if (config == null) {
            config = tryCreateConfig();
        }

        if (config != null) {
            unlockAll = config.has(UNLOCK_ALL) ? config.getBool(UNLOCK_ALL) : unlockAll;
            toggleKey = config.has(TOGGLE_KEY) ? config.getInt(TOGGLE_KEY) : toggleKey;
        }

        ModPanel settingsPanel = new ModPanel();

        Gson gson = new Gson();
        String json = Gdx.files.internal(getLocalizationFilePath("config.json")).readString(String.valueOf(StandardCharsets.UTF_8));
        Type configType = (new TypeToken<Map<String, String>>() {}).getType();
        Map<String, String> configStrings = gson.fromJson(json, configType);

        float yPos = 750f;

        ModLabeledToggleButton unlockAllButton = new ModLabeledToggleButton(
                configStrings.get(UNLOCK_ALL),
                350.0f,
                yPos,
                Settings.CREAM_COLOR,
                FontHelper.charDescFont,
                unlockAll,
                settingsPanel,
                (label) -> {},
                (button) -> {
                    unlockAll = button.enabled;
                    if (config != null) {
                        config.setBool(UNLOCK_ALL, unlockAll);
                        trySaveConfig(config);
                    }
                });

        yPos -= 50f;
        ModLabel toggleKeyLabel1 = new ModLabel(
                configStrings.get(TOGGLE_KEY),
                350.0f,
                yPos,
                Settings.CREAM_COLOR,
                FontHelper.charDescFont,
                settingsPanel,
                (label) -> {}
        );
        float textWidth = FontHelper.getWidth(FontHelper.charDescFont, configStrings.get(TOGGLE_KEY), 1);

        ModLabel toggleKeyLabel2 = new ModLabel(
                Input.Keys.toString(toggleKey),
                350.0f + textWidth + 160f,
                yPos,
                Settings.CREAM_COLOR,
                FontHelper.charDescFont,
                settingsPanel,
                (label) -> {}
        );

        ModButton toggleKeyButton = new ModButton(
                350.0f + textWidth + 50f,
                yPos - 50f,
                settingsPanel,
                (button) -> {
                    toggleKeyLabel2.text = configStrings.get(PRESS_ANY_KEY_TO_SET);
                    Gdx.input.setInputProcessor(new InputAdapter() {
                        @Override
                        public boolean keyDown(int keycode) {
                            InputHelper.regainInputFocus();
                            CInputHelper.regainInputFocus();
                            toggleKey = keycode;
                            toggleKeyLabel2.text = Input.Keys.toString(toggleKey);
                            if (config != null) {
                                config.setInt(TOGGLE_KEY, toggleKey);
                                trySaveConfig(config);
                                showIntentGraph = true;
                            }
                            return false;
                        }
                    });
                });

        settingsPanel.addUIElement(unlockAllButton);
        settingsPanel.addUIElement(toggleKeyLabel1);
        settingsPanel.addUIElement(toggleKeyButton);
        settingsPanel.addUIElement(toggleKeyLabel2);
        return settingsPanel;
    }

    public void loadIntents() {
        intentStrings.clear();
        intents.clear();

        ModInfo intentGraph = Arrays.stream(Loader.MODINFOS).filter(m -> m.ID.equals(MOD_ID)).findFirst().orElse(null);
        loadAdditionalIntents(intentGraph);

        for (ModInfo modinfo : Loader.MODINFOS) {
            Map<String, MonsterIntentGraph> intentsFromModJar = loadIntentsFromModJar(modinfo.jarURL);
            if (intentsFromModJar != null) {
                intents.putAll(intentsFromModJar);
            }

            Map<String, String> stringsFromModJar = loadIntentStringsFromModJar(modinfo.jarURL);
            if (stringsFromModJar != null) {
                intentStrings.putAll(stringsFromModJar);
            }
        }

        Gson gson = new Gson();
        try {
            String json = Gdx.files.local(INTENTGRAPH_INTENTS_DEV_JSON).readString(String.valueOf(StandardCharsets.UTF_8));
            Type intentType = (new TypeToken<Map<String, MonsterIntentGraph>>() {}).getType();
            intents.putAll(gson.fromJson(json, intentType));
        } catch (Exception ex) {
            if (!ex.getMessage().contains("File not found")) {
                logger.warn("Failed to load from intentgraph-intents-dev.json.", ex);
            }
        }

        try {
            String json = Gdx.files.local(INTENTGRAPH_INTENT_STRINGS_DEV_JSON).readString(String.valueOf(StandardCharsets.UTF_8));
            Type intentType = (new TypeToken<Map<String, String>>() {}).getType();
            intentStrings.putAll(gson.fromJson(json, intentType));
        } catch (Exception ex) {
            if (!ex.getMessage().contains("File not found")) {
                logger.warn("Failed to load from intentgraph-intentStrings-dev.json.", ex);
            }
        }
    }

    private void loadAdditionalIntents(ModInfo intentGraph) {
        String[] files = { "intents-battleTowers" };
        for (String file : files) {
            Map<String, MonsterIntentGraph> intentsFromModJar = loadIntentsFromModJar(intentGraph.jarURL, file);
            if (intentsFromModJar != null) {
                intents.putAll(intentsFromModJar);
            }

            Map<String, String> stringsFromModJar = loadIntentStringsFromModJar(intentGraph.jarURL, file);
            if (stringsFromModJar != null) {
                intentStrings.putAll(stringsFromModJar);
            }
        }
    }

    private Map<String, String> loadIntentStringsFromModJar(URL jarURL) {
        return loadIntentStringsFromModJar(jarURL, "intents");
    }

    private Map<String, String> loadIntentStringsFromModJar(URL jarURL, String filename) {
        Gson gson = new Gson();
        Type intentType = (new TypeToken<Map<String, String>>() {}).getType();

        try {
            URL eyeLocations = new URL("jar", "", jarURL + "!/" + getLocalizationFilePath(filename + ".json"));
            try (InputStream in = eyeLocations.openStream()) {
                return gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), intentType);
            }
        } catch (Exception ex) {
            if (!(ex instanceof FileNotFoundException)) {
                logger.warn("Failed to load intent strings from " + jarURL, ex);
            }
        }
        return null;
    }

    private Map<String, MonsterIntentGraph> loadIntentsFromModJar(URL jarURL) {
        return loadIntentsFromModJar(jarURL, "intents");
    }

    private Map<String, MonsterIntentGraph> loadIntentsFromModJar(URL jarURL, String filename) {
        Gson gson = new Gson();
        Type intentType = (new TypeToken<Map<String, MonsterIntentGraph>>() {}).getType();

        try {
            URL eyeLocations = new URL("jar", "", jarURL + "!/intentgraph/intents/" + filename + ".json");
            try (InputStream in = eyeLocations.openStream()) {
                return gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), intentType);
            }
        } catch (Exception ex) {
            if (!(ex instanceof FileNotFoundException)) {
                logger.warn("Failed to load intents from " + jarURL, ex);
            }
        }
        return null;
    }

    private void renderIntentGraphForMonster(AbstractMonster monster, SpriteBatch sb) {
        MonsterIntentGraph graph = intents.get(monster.id);
        if (graph == null) {
            return;
        }

        if (!unlockAll && !isMonsterUnlocked(monster.id)) {
            graph = intents.get("intentgraph:Locked");
            unlockMonsterInNextCombat.add(monster.id);
        }

        graph.render(monster, sb);
    }

    private boolean isMonsterUnlocked(String monsterId) {
        return config.getBool("intent_unlocked_" + monsterId);
    }

    private void setMonsterUnlocked(String monsterId) {
        config.setBool("intent_unlocked_" + monsterId, true);
    }

    private static String getLocalizationFilePath(String file) {
        String language = Settings.language.toString().toLowerCase();
        logger.info("getLocalizationFilePath - file=" + file + ", language=" + language);

        String path = getLocalizationPath(language + "/" + file);
        URL url = IntentGraphMod.class.getResource("/" + path);
        if (url != null) {
            return path;
        } else {
            return getLocalizationPath("eng/" + file);
        }
    }

    private static SpireConfig tryCreateConfig() {
        String configFileName = MOD_ID + "config";
        try {
            return new SpireConfig(MOD_ID, configFileName);
        } catch (IOException e) {
            logger.warn(e);
            return null;
        }
    }

    private static void trySaveConfig(SpireConfig config) {
        try {
            config.save();
        } catch (IOException e) {
            logger.warn(e);
        }
    }
}
