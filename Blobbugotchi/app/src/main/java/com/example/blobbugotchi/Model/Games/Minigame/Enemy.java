package com.example.blobbugotchi.Model.Games.Minigame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Random;

public class Enemy {
    private float x, y;
    private int tileSize;
    private float speed;
    private int direction;
    private Random random = new Random();
    private SpriteSheet spriteSheet;
    private Paint spritePaint;
    private Paint debugPaint;

    public Enemy(int startCol, int startRow, int tileSize, SpriteSheet spriteSheet, float speedMultiplier) {
        this.tileSize  = tileSize;
        this.x = startCol * tileSize;
        this.y = startRow * tileSize;
        this.speed = tileSize * speedMultiplier;
        this.spriteSheet = spriteSheet;
        this.direction = random.nextInt(4) + 1;

        spritePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        debugPaint = new Paint();
        debugPaint.setColor(0xFFFF4444);
    }

    public void update(float deltaSeconds, TileMap map) {
        float dx = 0, dy = 0;

        switch (direction) {
            case 1: dy = -speed * deltaSeconds; break;
            case 2: dy =  speed * deltaSeconds; break;
            case 3: dx = -speed * deltaSeconds; break;
            case 4: dx =  speed * deltaSeconds; break;
        }

        float margin = tileSize * 0.1f;
        float newX = x + dx, newY = y + dy;

        boolean blocked =
                collidesWithWall(newX + margin, newY + margin, map) ||
                collidesWithWall(newX + tileSize - margin, newY + margin, map) ||
                collidesWithWall(newX + margin, newY + tileSize - margin, map) ||
                collidesWithWall(newX + tileSize - margin, newY + tileSize - margin, map);

        if (!blocked) {
            x = newX;
            y = newY;

            if (random.nextFloat() < 0.005f) {
                direction = random.nextInt(4) + 1;
            }
        }
        else {
            direction = random.nextInt(4) + 1;
        }

        if (spriteSheet != null) {
            spriteSheet.setDirection(direction);
            spriteSheet.update(deltaSeconds);
        }
    }

    private boolean collidesWithWall(float px, float py, TileMap map) {
        return map.isWall(map.toCol(px), map.toRow(py));
    }

    public void draw(Canvas canvas) {
        if (spriteSheet != null) {
            spriteSheet.draw(canvas, x, y, tileSize, spritePaint);
        }
        else {
            canvas.drawCircle(x + tileSize / 2f, y + tileSize / 2f, tileSize / 2f - 4, debugPaint);
        }
    }

    public RectF getBounds() {
        float margin = tileSize * 0.15f;
        return new RectF(x + margin, y + margin, x + tileSize - margin, y + tileSize - margin);
    }
}