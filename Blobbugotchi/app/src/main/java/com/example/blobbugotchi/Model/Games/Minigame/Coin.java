package com.example.blobbugotchi.Model.Games.Minigame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Coin {
    public static final int POINTS = 10;
    private float x, y;
    private int tileSize;
    private boolean collected = false;
    private SpriteSheet spriteSheet;
    private Paint spritePaint;
    private Paint debugPaint;

    public Coin(int col, int row, int tileSize, SpriteSheet spriteSheet) {
        this.tileSize = tileSize;
        this.x = col * tileSize;
        this.y = row * tileSize;
        this.spriteSheet = spriteSheet;

        spritePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        debugPaint.setColor(0xFFFFD700);
    }

    public void update(float deltaSeconds) {
        if (!collected && spriteSheet != null) {
            spriteSheet.update(deltaSeconds);
        }
    }

    public void draw(Canvas canvas) {
        if (collected) {
            return;
        }

        int drawSize = (int)(tileSize * 0.65f);
        float offset = (tileSize - drawSize) / 2f;

        if (spriteSheet != null) {
            spriteSheet.draw(canvas, x + offset, y + offset, drawSize, spritePaint);
        }
        else {
            canvas.drawCircle(x + tileSize / 2f, y + tileSize / 2f, tileSize * 0.22f, debugPaint);
        }
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public RectF getBounds() {
        float margin = tileSize * 0.25f;
        return new RectF(x + margin, y + margin, x + tileSize - margin, y + tileSize - margin);
    }
}