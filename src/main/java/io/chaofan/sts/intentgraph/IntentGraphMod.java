package io.chaofan.sts.intentgraph;

import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.devcommands.ConsoleCommand;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostDeathSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostRenderSubscriber;
import com.badlogic.gdx.Gdx;
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
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import io.chaofan.sts.intentgraph.model.MonsterIntentGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpireInitializer
public class IntentGraphMod implements
        PostRenderSubscriber,
        PostInitializeSubscriber,
        PostBattleSubscriber,
        PostDeathSubscriber {

    public static final String MOD_ID = "intentgraph";
    public static final Logger logger = LogManager.getLogger(IntentGraphMod.class.getName());

    private static final String UNLOCK_ALL = "UnlockAll";

    public static String getImagePath(String file) {
        return MOD_ID + "/images/" + file;
    }

    public static String getLocalizationPath(String file) {
        return MOD_ID + "/localization/" + file;
    }

    public static final int GRID_SIZE = 80;

    private static SpireConfig config;
    private static boolean unlockAll = false;
    private static final Set<String> unlockMonsterInNextCombat = new HashSet<>();

    public static void initialize() {
        logger.info("Initializing IntentGraphMod");

        IntentGraphMod mod = new IntentGraphMod();
        instance = mod;
        BaseMod.subscribe(mod);
    }

    private String intentStringsPath;
    public final Map<String, String> intentStrings = new HashMap<>();
    private final Map<String, MonsterIntentGraph> intents = new HashMap<>();

    public static IntentGraphMod instance;
    public int overwriteAscension = -1;

    @Override
    public void receivePostInitialize() {
        this.intentStringsPath = getLocalizationFilePath("intents.json");
        ModPanel settingsPanel = initSettings();

        Texture badgeTexture = ImageMaster.loadImage(MOD_ID + "/images/badge.png");
        BaseMod.registerModBadge(badgeTexture, "Intent Graph", "Chaofan", "", settingsPanel);

        loadIntents();
        ConsoleCommand.addCommand("reloadintents", ReloadIntentsCommand.class);
    }

    @Override
    public void receivePostRender(SpriteBatch spriteBatch) {
        if (AbstractDungeon.getCurrMapNode() == null) {
            return;
        }

        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.monsters == null) {
            return;
        }

        for (AbstractMonster monster : room.monsters.monsters) {
            if (monster.hb.hovered && !monster.isDeadOrEscaped()) {
                renderIntentGraphForMonster(monster, spriteBatch);
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

        settingsPanel.addUIElement(unlockAllButton);
        return settingsPanel;
    }

    public void loadIntents() {
        intentStrings.clear();
        intents.clear();

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
            String json = Gdx.files.internal("intentgraph-intents-dev.json").readString(String.valueOf(StandardCharsets.UTF_8));
            Type intentType = (new TypeToken<Map<String, MonsterIntentGraph>>() {}).getType();
            intents.putAll(gson.fromJson(json, intentType));
        } catch (Exception ex) {
            if (!ex.getMessage().contains("File not found")) {
                logger.warn("Failed to load from intentgraph-intents-dev.json.", ex);
            }
        }

        try {
            String json = Gdx.files.internal("intentgraph-intentStrings-dev.json").readString(String.valueOf(StandardCharsets.UTF_8));
            Type intentType = (new TypeToken<Map<String, String>>() {}).getType();
            intentStrings.putAll(gson.fromJson(json, intentType));
        } catch (Exception ex) {
            if (!ex.getMessage().contains("File not found")) {
                logger.warn("Failed to load from intentgraph-intentStrings-dev.json.", ex);
            }
        }
    }

    private Map<String, String> loadIntentStringsFromModJar(URL jarURL) {
        Gson gson = new Gson();
        Type intentType = (new TypeToken<Map<String, String>>() {}).getType();

        try {
            URL eyeLocations = new URL("jar", "", jarURL + "!/" + intentStringsPath);
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
        Gson gson = new Gson();
        Type intentType = (new TypeToken<Map<String, MonsterIntentGraph>>() {}).getType();

        try {
            URL eyeLocations = new URL("jar", "", jarURL + "!/intentgraph/intents/intents.json");
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
