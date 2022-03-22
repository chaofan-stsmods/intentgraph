package io.chaofan.sts.intentgraph;

import basemod.BaseMod;
import basemod.ModPanel;
import basemod.devcommands.ConsoleCommand;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostRenderSubscriber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import io.chaofan.sts.intentgraph.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SpireInitializer
public class IntentGraphMod implements PostRenderSubscriber, PostInitializeSubscriber {

    public static final String MOD_ID = "intentgraph";
    public static final Logger logger = LogManager.getLogger(IntentGraphMod.class.getName());

    public static String getImagePath(String file) {
        return MOD_ID + "/images/" + file;
    }

    public static String getLocalizationPath(String file) {
        return MOD_ID + "/localization/" + file;
    }

    public static final int GRID_SIZE = 80;

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

    private ModPanel initSettings() {
        return new ModPanel();
    }

    public void loadIntents() {
        intentStrings.clear();
        intents.clear();

        Gson gson = new Gson();
        String json = Gdx.files.internal("intentgraph/intents/intents.json").readString(String.valueOf(StandardCharsets.UTF_8));
        Type intentType = (new TypeToken<Map<String, MonsterIntentGraph>>() {}).getType();
        intents.putAll(gson.fromJson(json, intentType));
        for (MonsterIntentGraph graph : intents.values()) {
            graph.initMonsterGraphDetail();
        }

        json = Gdx.files.internal(intentStringsPath).readString(String.valueOf(StandardCharsets.UTF_8));
        intentType = (new TypeToken<Map<String, String>>() {}).getType();
        intentStrings.putAll(gson.fromJson(json, intentType));
    }

    private void renderIntentGraphForMonster(AbstractMonster monster, SpriteBatch sb) {
        MonsterIntentGraph graph = intents.get(monster.id);
        if (graph == null) {
            return;
        }

        graph.render(monster, sb, overwriteAscension);
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
}
