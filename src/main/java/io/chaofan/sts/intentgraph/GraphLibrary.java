package io.chaofan.sts.intentgraph;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.chaofan.sts.intentgraph.model.MonsterGraphDetail;
import io.chaofan.sts.intentgraph.rule.IRule;
import io.chaofan.sts.intentgraph.rule.IRuleContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GraphLibrary implements IRuleContext {
    private final List<RuleGraphPair> ruleGraphPairs = new ArrayList<>();

    private AbstractMonster processingMonster;
    private int overwriteAscension = -1;

    public GraphLibrary(List<MonsterGraphDetail> graphList) {
        init(graphList);
    }

    public MonsterGraphDetail get(AbstractMonster monster) {
        processingMonster = monster;
        try {
            for (RuleGraphPair pair : ruleGraphPairs) {
                if (pair.rule.getBool()) {
                    return pair.graph;
                }
            }
            return null;
        } finally {
            processingMonster = null;
        }
    }

    public void setOverwriteAscension(int overwriteAscension) {
        this.overwriteAscension = overwriteAscension;
    }

    private void init(List<MonsterGraphDetail> graphList) {
        HashMap<String, RuleGraphPair> idToGraph = new HashMap<>();
        for (MonsterGraphDetail graph : graphList) {
            IRule rule = IRule.parse(graph.condition, this);
            RuleGraphPair pair = new RuleGraphPair();
            pair.rule = rule;
            pair.graph = graph;
            ruleGraphPairs.add(pair);
            idToGraph.put(graph.id, pair);
        }

        for (RuleGraphPair pair : ruleGraphPairs) {
            extendGraphDetail(idToGraph, pair);
        }

        for (RuleGraphPair pair : ruleGraphPairs) {
            pair.graph.init();
        }
    }

    private void extendGraphDetail(HashMap<String, RuleGraphPair> graphs, RuleGraphPair pair) {
        MonsterGraphDetail current = pair.graph;
        if (current.extend != null && !current.overwrite) {
            RuleGraphPair extend = graphs.get(current.extend);
            if (extend != null) {
                extendGraphDetail(graphs, extend);
                pair.graph = extend.graph.copyAndApply(current);
            }
        }
    }

    private int getAscensionLevel() {
        return overwriteAscension >= 0 ? overwriteAscension :
                (IntentGraphMod.instance.overwriteAscension >= 0 ?
                IntentGraphMod.instance.overwriteAscension :
                AbstractDungeon.ascensionLevel);
    }

    private int getActNum() {
        return AbstractDungeon.actNum;
    }

    private List<AbstractMonster> tryGetMonsters() {
        if (AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() != null &&
                AbstractDungeon.getCurrRoom().monsters != null) {
            return AbstractDungeon.getCurrRoom().monsters.monsters;
        }
        return Collections.emptyList();
    }

    @Override
    public int getIntVariable(String variableName) {
        switch (variableName) {
            case "ascension":
                return getAscensionLevel();
            case "act":
                return getActNum();
            case "index":
                return tryGetMonsters().indexOf(processingMonster);
            case "bossInRoom":
                return tryGetMonsters().stream().anyMatch(m -> m.type == AbstractMonster.EnemyType.BOSS) ? 1 : 0;
            case "eliteTrigger":
                return !tryGetMonsters().isEmpty() && AbstractDungeon.getCurrRoom().eliteTrigger ? 1 : 0;
        }

        if (variableName.startsWith("m.") && processingMonster != null) {
            String fieldName = variableName.substring(2);
            Class<?> clz = processingMonster.getClass();
            Field field = tryGetField(clz, fieldName);
            while (field == null) {
                clz = clz.getSuperclass();
                if (clz == null) {
                    break;
                }
                field = tryGetField(clz, fieldName);
            }
            if (field != null) {
                field.setAccessible(true);
                try {
                    if (field.getType() == int.class) {
                        return field.getInt(processingMonster);
                    } else if (field.getType() == short.class) {
                        return field.getShort(processingMonster);
                    } else if (field.getType() == byte.class) {
                        return field.getByte(processingMonster);
                    } else if (field.getType() == long.class) {
                        return (int) field.getLong(processingMonster);
                    } if (field.getType() == boolean.class) {
                        return field.getBoolean(processingMonster) ? 1 : 0;
                    }
                } catch (IllegalAccessException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private Field tryGetField(Class<?> clz, String fieldName) {
        try {
            return clz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ignored) {
        }

        return null;
    }

    static class RuleGraphPair {
        public IRule rule;
        public MonsterGraphDetail graph;
    }
}
