package io.chaofan.sts.intentgraph.rule;

import java.util.Objects;

public class OneOperandRule implements IRule {
    private final Operator compareOperator;
    private final IRule operandA;

    public OneOperandRule(Operator compareOperator, IRule operandA) {
        this.compareOperator = compareOperator;
        this.operandA = operandA;
    }

    private boolean check() {
        if (Objects.requireNonNull(compareOperator) == Operator.NOT) {
            return !operandA.getBool();
        }
        return false;
    }

    @Override
    public int getInt() {
        return check() ? 1 : 0;
    }

    @Override
    public boolean getBool() {
        return check();
    }
}
