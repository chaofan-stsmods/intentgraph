package io.chaofan.sts.intentgraph;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.chaofan.sts.intentgraph.model.MonsterGraphDetail;
import io.chaofan.sts.intentgraph.rule.IRule;
import io.chaofan.sts.intentgraph.rule.IRuleContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphLibrary implements IRuleContext {
    private final List<RuleGraphPair> ruleGraphPairs = new ArrayList<>();

    private AbstractMonster processingMonster;

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
        return IntentGraphMod.instance.overwriteAscension >= 0 ?
                IntentGraphMod.instance.overwriteAscension :
                AbstractDungeon.ascensionLevel;
    }

    private int getActNum() {
        return AbstractDungeon.actNum;
    }

    @Override
    public int getIntVariable(String variableName) {
        switch (variableName) {
            case "ascension":
                return getAscensionLevel();
            case "act":
                return getActNum();
            case "index":
                if (AbstractDungeon.getCurrRoom() != null &&
                        AbstractDungeon.getCurrRoom().monsters != null) {
                    return AbstractDungeon.getCurrRoom().monsters.monsters.indexOf(processingMonster);
                } else {
                    return -1;
                }
        }

        if (variableName.startsWith("m.") && processingMonster != null) {
            String fieldName = variableName.substring(2);
            Class<?> clz = processingMonster.getClass();
            Field field = ReflectionHacks.getCachedField(clz, fieldName);
            while (field == null && clz != null) {
                clz = clz.getSuperclass();
                field = ReflectionHacks.getCachedField(clz, fieldName);
            }
            if (field != null) {
                field.setAccessible(true);
                if (field.getType() == int.class) {
                    try {
                        return field.getInt(processingMonster);
                    } catch (IllegalAccessException e) {
                        return 0;
                    }
                } else if (field.getType() == boolean.class) {
                    try {
                        return field.getBoolean(processingMonster) ? 1 : 0;
                    } catch (IllegalAccessException e) {
                        return 0;
                    }
                }
            }
        }
        return 0;
    }


    static class RuleGraphPair {
        public IRule rule;
        public MonsterGraphDetail graph;
    }
}
