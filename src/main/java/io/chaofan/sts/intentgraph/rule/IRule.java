package io.chaofan.sts.intentgraph.rule;

public interface IRule {
    int getInt();
    boolean getBool();

    enum Operator {
        EQ, LT, GT, LE, GE, NE, AND, OR, NOT
    }

    static IRule parse(String expression, IRuleContext ruleContext) {
        return RuleParser.parse(expression, ruleContext);
    }
}
