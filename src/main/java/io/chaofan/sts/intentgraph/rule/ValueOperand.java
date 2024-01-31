package io.chaofan.sts.intentgraph.rule;

public class ValueOperand implements IRule {
    private final int intValue;
    private final boolean boolValue;

    public ValueOperand(int value) {
        this.intValue = value;
        this.boolValue = false;
    }

    public ValueOperand(boolean value) {
        this.boolValue = value;
        this.intValue = 0;
    }

    @Override
    public int getInt() {
        return intValue;
    }

    @Override
    public boolean getBool() {
        return boolValue;
    }
}
