package io.chaofan.sts.intentgraph.model;

public class MonsterIntentGraph {

    public int width;
    public int height;
    public MonsterGraphDetail a0;
    public MonsterGraphDetail a1;
    public MonsterGraphDetail a2;

    public void initMonsterGraphDetail() {
        a1 = a0.copyAndApply(a1);
        a2 = a1.copyAndApply(a2);
    }
}
