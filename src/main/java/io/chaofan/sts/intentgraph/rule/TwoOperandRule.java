package io.chaofan.sts.intentgraph.rule;

public class TwoOperandRule implements IRule {
    private final Operator compareOperator;
    private final IRule operandA;
    private final IRule operandB;

    public TwoOperandRule(IRule operandA, Operator compareOperator, IRule operandB) {
        this.compareOperator = compareOperator;
        this.operandA = operandA;
        this.operandB = operandB;
    }

    private boolean check() {
        switch (compareOperator) {
            case EQ:
                return operandA.getInt() == operandB.getInt();
            case LT:
                return operandA.getInt() < operandB.getInt();
            case GT:
                return operandA.getInt() > operandB.getInt();
            case LE:
                return operandA.getInt() <= operandB.getInt();
            case GE:
                return operandA.getInt() >= operandB.getInt();
            case NE:
                return operandA.getInt() != operandB.getInt();
            case AND:
                return operandA.getBool() && operandB.getBool();
            case OR:
                return operandA.getBool() || operandB.getBool();
            default:
                return false;
        }
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
