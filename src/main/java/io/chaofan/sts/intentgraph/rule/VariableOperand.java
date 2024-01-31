package io.chaofan.sts.intentgraph.rule;

public class VariableOperand implements IRule {
    private final String variableName;
    private final IRuleContext context;

    public VariableOperand(String variableName, IRuleContext context) {
        this.variableName = variableName;
        this.context = context;
    }

    @Override
    public int getInt() {
        return context.getIntVariable(variableName);
    }

    @Override
    public boolean getBool() {
        return getInt() != 0;
    }
}
