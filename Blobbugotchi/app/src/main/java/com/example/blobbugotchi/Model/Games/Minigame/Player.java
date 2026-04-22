package com.example.blobbugotchi.Model.Games.Minigame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Player {
    public static final int MAX_LIVES = 3;

    private float x, y;
    private int tileSize;
    private float speed;

    private int lives;
    private int score;
    private boolean invulnerable;
    private long invulnerableTimer;
    private static final long INVULNERABLE_MS = 1500;

    private int direction = 0;

    private SpriteSheet spriteSheet;
    private Paint spritePaint;
    private Paint debugPaint;

    public Player(int startCol, int startRow, int tileSize, SpriteSheet spriteSheet, int initialScore) {
        this.tileSize = tileSize;
        this.x = startCol * tileSize;
        this.y = startRow * tileSize;
        this.speed = tileSize * 2.5f;
        this.lives = MAX_LIVES;
        this.score = initialScore;
        this.spriteSheet = spriteSheet;

        spritePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        debugPaint  = new Paint();
        debugPaint.setColor(0xFFFFD700);
    }

    public void setDirection(int dir) { this.direction = dir; }

    public void update(float deltaSeconds, TileMap map) {
        float dx = 0, dy = 0;
        switch (direction) {
            case 1: dy = -speed * deltaSeconds; break;
            case 2: dy =  speed * deltaSeconds; break;
            case 3: dx = -speed * deltaSeconds; break;
            case 4: dx =  speed * deltaSeconds; break;
        }

        if (dx != 0 || dy != 0) {
            float margin = tileSize * 0.1f;
            float newX = x + dx;
            float newY = y + dy;

            if (!collidesWithWall(newX + margin, newY + margin, map) &&
                    !collidesWithWall(newX + tileSize - margin, newY + margin, map) &&
                    !collidesWithWall(newX + margin, newY + tileSize - margin, map) &&
                    !collidesWithWall(newX + tileSize - margin, newY + tileSize - margin, map)) {
                x = newX;
                y = newY;
            }

            // Animación: dirección y avance SIEMPRE que haya intención de moverse
            if (spriteSheet != null) {
                spriteSheet.setDirection(direction);
                spriteSheet.update(deltaSeconds);
            }
        }

        if (invulnerable) {
            invulnerableTimer -= (long)(deltaSeconds * 1000);
            if (invulnerableTimer <= 0) invulnerable = false;
        }
    }

    private boolean collidesWithWall(float px, float py, TileMap map) {
        return map.isWall(map.toCol(px), map.toRow(py));
    }

    public void draw(Canvas canvas) {
        boolean visible = !invulnerable || (System.currentTimeMillis() / 150) % 2 == 0;

        if (!visible) {
            return;
        }

        if (spriteSheet != null) {
            spriteSheet.draw(canvas, x, y, tileSize, spritePaint);
        }
        else {
            canvas.drawCircle(x + tileSize / 2f, y + tileSize / 2f, tileSize / 2f - 4, debugPaint);
        }
    }

    public void takeDamage() {
        if (!invulnerable) {
            lives--;
            invulnerable = true;
            invulnerableTimer = INVULNERABLE_MS;
        }
    }

    public void addScore(int points) { score += points; }

    public RectF getBounds() {
        float margin = tileSize * 0.15f;
        return new RectF(x + margin, y + margin, x + tileSize - margin, y + tileSize - margin);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public boolean isDead() {
        return lives <= 0;
    }

    // Recuperar una vida (máximo MAX_LIVES)
    public void addLife() {
        lives = Math.min(lives + 1, MAX_LIVES);
    }

    public int getTileCol(TileMap map) {
        return map.toCol(x + tileSize / 2f);
    }

    public int getTileRow(TileMap map) {
        return map.toRow(y + tileSize / 2f);
    }
}