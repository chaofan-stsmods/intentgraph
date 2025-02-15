package io.chaofan.sts.intentgraph.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import io.chaofan.sts.intentgraph.IntentGraphMod;

import static io.chaofan.sts.intentgraph.IntentGraphMod.getImagePath;

public class Arrow {

    private static final Texture ARROW = ImageMaster.loadImage(getImagePath("ui/arrow.png"));
    private static final TextureRegion ARROW_HORIZONTAL = new TextureRegion(ARROW, 1, 0, 62, 10);
    private static final TextureRegion ARROW_VERTICAL = new TextureRegion(ARROW, 65, 1, 10, 62);
    private static final TextureRegion ARROW_DR = new TextureRegion(ARROW, 0, 11, 10, 10);
    private static final TextureRegion ARROW_DL = new TextureRegion(ARROW, 11, 11, 10, 10);
    private static final TextureRegion ARROW_UR = new TextureRegion(ARROW, 0, 22, 10, 10);
    private static final TextureRegion ARROW_UL = new TextureRegion(ARROW, 11, 22, 10, 10);
    private static final TextureRegion ARROW_U = new TextureRegion(ARROW, 91, 0, 20, 15);
    private static final TextureRegion ARROW_D = new TextureRegion(ARROW, 91, 35, 20, 15);
    private static final TextureRegion ARROW_R = new TextureRegion(ARROW, 111, 15, 15, 20);
    private static final TextureRegion ARROW_L = new TextureRegion(ARROW, 76, 15, 15, 20);
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_END_LENGTH = 15;
    private static final Color INSTANT_COLOR = new Color(1, 0.2f, 0.2f, 1);
    
    public float[] path;
    public boolean instant;

    public void render(float x, float y, SpriteBatch sb) {
        if (path.length <= 3) {
            return;
        }

        sb.setColor(instant ? INSTANT_COLOR : Color.WHITE);
        float scale = Settings.scale;
        float[] path = this.path;
        boolean isHorizontal = path[0] == 0;
        float arrowX = path[1];
        float arrowY = path[2];
        int direction = -1; // U, R, D, L
        for (int i = 3; i < path.length; i++) {
            boolean isStart = i == 3;
            boolean isEnd = i == path.length - 1;
            float nextArrowX = isHorizontal ? path[i] : arrowX;
            float nextArrowY = isHorizontal ? arrowY : path[i];
            int nextDirection;
            float dy = y - (arrowY * IntentGraphMod.GRID_SIZE + ARROW_WIDTH / 2f) * scale;
            float dx = x + (arrowX * IntentGraphMod.GRID_SIZE - ARROW_WIDTH / 2f) * scale;
            if (isHorizontal) {
                boolean isRight = nextArrowX > arrowX;
                nextDirection = isRight ? 1 : 3;
                int startDistance = (isStart ? 0 : (isRight ? 1 : -1)) * ARROW_WIDTH / 2;
                int endDistance = (isEnd ? ARROW_END_LENGTH : ARROW_WIDTH / 2) * (isRight ? -1 : 1);
                float dxs = x + (arrowX * IntentGraphMod.GRID_SIZE + startDistance) * scale;
                float dxe = x + (nextArrowX * IntentGraphMod.GRID_SIZE + endDistance) * scale;
                float len = Math.abs(dxs - dxe);
                sb.draw(ARROW_HORIZONTAL, Math.min(dxs, dxe), dy, len, ARROW_WIDTH * scale);
            } else {
                boolean isDown = nextArrowY > arrowY;
                nextDirection = isDown ? 2 : 0;
                int startDistance = (isStart ? 0 : (isDown ? 1 : -1)) * ARROW_WIDTH / 2;
                int endDistance = (isEnd ? ARROW_END_LENGTH : ARROW_WIDTH / 2) * (isDown ? -1 : 1);
                float dys = y - (arrowY * IntentGraphMod.GRID_SIZE + startDistance) * scale;
                float dye = y - (nextArrowY * IntentGraphMod.GRID_SIZE + endDistance) * scale;
                float len = Math.abs(dys - dye);
                sb.draw(ARROW_VERTICAL, dx, Math.min(dys, dye), ARROW_WIDTH * scale, len);
            }

            if (!isStart) {
                if ((direction == 2 && nextDirection == 1) || (direction == 3 && nextDirection == 0)) {
                    sb.draw(ARROW_UR, dx, dy, ARROW_WIDTH * scale, ARROW_WIDTH * scale);
                } else if ((direction == 2 && nextDirection == 3) || (direction == 1 && nextDirection == 0)) {
                    sb.draw(ARROW_UL, dx, dy, ARROW_WIDTH * scale, ARROW_WIDTH * scale);
                } else if ((direction == 0 && nextDirection == 3) || (direction == 1 && nextDirection == 2)) {
                    sb.draw(ARROW_DL, dx, dy, ARROW_WIDTH * scale, ARROW_WIDTH * scale);
                } else if ((direction == 0 && nextDirection == 1) || (direction == 3 && nextDirection == 2)) {
                    sb.draw(ARROW_DR, dx, dy, ARROW_WIDTH * scale, ARROW_WIDTH * scale);
                }
            }

            isHorizontal = !isHorizontal;
            arrowX = nextArrowX;
            arrowY = nextArrowY;
            direction = nextDirection;
        }

        float dy = y - arrowY * IntentGraphMod.GRID_SIZE * scale;
        float dx = x + arrowX * IntentGraphMod.GRID_SIZE * scale;
        switch (direction) {
            case 0:
                sb.draw(ARROW_U, dx - ARROW_U.getRegionWidth() / 2f * scale, dy - ARROW_END_LENGTH * scale, ARROW_U.getRegionWidth() * scale, ARROW_U.getRegionHeight() * scale);
                break;
            case 1:
                sb.draw(ARROW_R, dx - ARROW_END_LENGTH * scale, dy - ARROW_R.getRegionHeight() / 2f * scale, ARROW_R.getRegionWidth() * scale, ARROW_R.getRegionHeight() * scale);
                break;
            case 2:
                sb.draw(ARROW_D, dx - ARROW_D.getRegionWidth() / 2f * scale, dy, ARROW_D.getRegionWidth() * scale, ARROW_D.getRegionHeight() * scale);
                break;
            case 3:
                sb.draw(ARROW_L, dx, dy - ARROW_L.getRegionHeight() / 2f * scale, ARROW_L.getRegionWidth() * scale, ARROW_L.getRegionHeight() * scale);
                break;
        }
    }
}
