package io.chaofan.sts.intentgraph.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.chaofan.sts.intentgraph.IntentGraphMod;

import java.util.Arrays;

public class MonsterGraphDetail {
    public boolean overwrite;
    public float width;
    public float height;
    public Damage[] damages;
    public Icon[] icons;
    public IconGroup[] iconGroups;
    public Arrow[] arrows;
    public Label[] labels;

    public MonsterGraphDetail copyAndApply(MonsterGraphDetail another) {
        if (another == null) {
            return this;
        }

        if (another.overwrite) {
            return another;
        }

        MonsterGraphDetail detail = new MonsterGraphDetail();
        detail.overwrite = this.overwrite;
        detail.damages = this.damages == null ? null : this.damages.clone();
        detail.icons = this.icons == null ? null : this.icons.clone();
        detail.iconGroups = this.iconGroups == null ? null : this.iconGroups.clone();
        detail.arrows = this.arrows == null ? null : this.arrows.clone();
        detail.labels = this.labels == null ? null : this.labels.clone();

        detail.damages = apply(detail.damages, another.damages);
        detail.icons = apply(detail.icons, another.icons);
        detail.iconGroups = apply(detail.iconGroups, another.iconGroups);
        detail.arrows = apply(detail.arrows, another.arrows);
        detail.labels = apply(detail.labels, another.labels);
        detail.height = another.height != 0 ? another.height : detail.height;
        detail.width = another.width != 0 ? another.width : detail.width;

        return detail;
    }

    private <T> T[] apply(T[] v, T[] v1) {
        if (v == null) {
            return v1;
        }
        if (v1 == null) {
            return v;
        }
        if (v1.length > v.length) {
            v = Arrays.copyOf(v, v1.length);
        }
        for (int i = 0; i < v1.length; i++) {
            if (v1[i] != null) {
                v[i] = v1[i];
            }
        }
        return v;
    }

    public void init() {
        if (damages != null) {
            for (Damage damage : damages) {
                if (damage.max < damage.min) {
                    damage.max = damage.min;
                }
            }
        }
        if (icons != null) {
            for (Icon icon : icons) {
                if (icon.attackCount == 0) {
                    icon.attackCount = 1;
                }
            }
        }
    }

    public void render(float x, float y, SpriteBatch sb) {
        renderIcons(x, y, sb);
        renderIconGroups(iconGroups, x, y, sb);
        renderArrows(arrows, x, y, sb);
        renderLabels(labels, x, y, sb);
    }

    public void renderIcons(float x, float y, SpriteBatch sb) {
        if (icons == null) {
            return;
        }

        for (Icon icon : icons) {
            icon.render(this, x, y, sb);
        }
    }

    private void renderLabels(Label[] labels, float x, float y, SpriteBatch sb) {
        if (labels == null) {
            return;
        }

        for (Label label : labels) {
            label.render(x, y, sb);
        }
    }

    private void renderArrows(Arrow[] arrows, float x, float y, SpriteBatch sb) {
        if (arrows == null) {
            return;
        }

        for (Arrow arrow : arrows) {
            arrow.render(x, y, sb);
        }
    }

    private void renderIconGroups(IconGroup[] iconGroups, float x, float y, SpriteBatch sb) {
        if (iconGroups == null) {
            return;
        }

        for (IconGroup iconGroup : iconGroups) {
            if (iconGroup.hide) {
                continue;
            }
            iconGroup.render(x, y, sb);
        }
    }
}
