package com.example.blobbugotchi.Model.Games.Minigame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Trap {
    private float x, y;
    private int tileSize;
    private SpriteSheet sprite;
    private Paint paint;
    private Paint strokePaint;

    public Trap(int col, int row, int tileSize, SpriteSheet ssHook) {
        this.tileSize = tileSize;
        this.x = col * tileSize;
        this.y = row * tileSize;
        this.sprite = ssHook; // ← GUARDAR EL SPRITE

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFFFF3300);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(0xFFFF8800);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2f);
    }

    public void draw(Canvas canvas) {
        if (sprite != null) {
            sprite.drawFrame(canvas, 0, x, y, tileSize, null); // frame 0, sin paint especial
        }
        else {
            float cx = x + tileSize / 2f;
            float cy = y + tileSize / 2f;
            float r = tileSize / 2f - tileSize * 0.2f;
            float half = tileSize * 0.25f;

            paint.setAlpha(180);
            canvas.drawCircle(cx, cy, r, paint);

            strokePaint.setStrokeWidth(tileSize * 0.12f);
            canvas.drawLine(cx - half, cy - half, cx + half, cy + half, strokePaint);
            canvas.drawLine(cx + half, cy - half, cx - half, cy + half, strokePaint);
        }
    }

    public RectF getBounds() {
        float margin = tileSize * 0.25f;
        return new RectF(x + margin, y + margin, x + tileSize - margin, y + tileSize - margin);
    }
}