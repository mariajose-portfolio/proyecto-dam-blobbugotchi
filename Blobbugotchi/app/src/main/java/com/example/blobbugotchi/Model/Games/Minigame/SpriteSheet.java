package com.example.blobbugotchi.Model.Games.Minigame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class SpriteSheet {

    private Bitmap sheet;
    private int frameW, frameH;
    private int cols, rows;
    private int totalFrames;
    private int directionRow = 0; // fila por defecto: abajo
    private boolean flipHorizontal = false;

    // Animación
    private int currentFrame = 0;
    private float frameDuration; // segundos por frame
    private float elapsed = 0f;
    private boolean loop;

    /**
     * @param sheet Bitmap completo del spritesheet
     * @param cols Número de columnas
     * @param rows Número de filas
     * @param totalFrames Total de frames válidos (puede ser < cols*rows)
     * @param fps Velocidad de animación en frames por segundo
     * @param loop Si la animación se repite
     */
    public SpriteSheet(Bitmap sheet, int cols, int rows, int totalFrames, float fps, boolean loop) {
        this.sheet = sheet;
        this.cols = cols;
        this.rows = rows;
        this.totalFrames = totalFrames;
        this.frameW = sheet.getWidth()  / cols;
        this.frameH = sheet.getHeight() / rows;
        this.frameDuration = 1f / fps;
        this.loop = loop;

        // Arrancar en fila 0 siempre; quien necesite otra fila inicial llama a setDirection()
        this.directionRow = 0;
        this.currentFrame = 0;
    }

    // Actualiza el frame según el tiempo transcurrido
    public void update(float deltaSeconds) {
        elapsed += deltaSeconds;

        if (elapsed >= frameDuration) {
            elapsed -= frameDuration;
            int frameInRow = currentFrame % cols; // posición dentro de la fila

            if (loop) {
                frameInRow = (frameInRow + 1) % cols;
            }
            else {
                frameInRow = Math.min(frameInRow + 1, cols - 1);
            }

            currentFrame = directionRow * cols + frameInRow;
        }
    }

    // Dibuja el frame actual escalado al rectángulo destino
    public void draw(Canvas canvas, float destX, float destY, int destSize, Paint paint) {
        int col = currentFrame % cols;
        int row = currentFrame / cols;

        Rect src = new Rect(col * frameW, row * frameH,
                col * frameW + frameW, row * frameH + frameH);
        RectF dst = new RectF(destX, destY, destX + destSize, destY + destSize);

        if (flipHorizontal) {
            canvas.save();

            // Voltea horizontalmente sobre el centro del sprite
            canvas.scale(-1f, 1f, destX + destSize / 2f, destY + destSize / 2f);
            canvas.drawBitmap(sheet, src, dst, paint);
            canvas.restore();
        }
        else {
            canvas.drawBitmap(sheet, src, dst, paint);
        }
    }

    // Dibuja un frame concreto (útil para objetos estáticos o con un solo frame)
    public void drawFrame(Canvas canvas, int frame, float destX, float destY, int destSize, Paint paint) {
        int col = frame % cols;
        int row = frame / cols;

        Rect src = new Rect(col * frameW, row * frameH,
                col * frameW + frameW, row * frameH + frameH);
        RectF dst = new RectF(destX, destY, destX + destSize, destY + destSize);
        canvas.drawBitmap(sheet, src, dst, paint);
    }

    public void reset() {
        currentFrame = 0;
        elapsed = 0f;
    }

    public void setDirection(int dir) {
        int newRow;
        switch (dir) {
            case 1: newRow = 1; flipHorizontal = false; break; // arriba
            case 2: newRow = 0; flipHorizontal = false; break; // abajo
            case 3: newRow = 2; flipHorizontal = false; break; // izquierda
            case 4: newRow = 2; flipHorizontal = true;  break; // derecha
            default: newRow = 0; flipHorizontal = false; break;
        }

        // Solo resetear si cambia de dirección, para no cortar la animación en curso
        if (newRow != directionRow) {
            directionRow = newRow;
            currentFrame = directionRow * cols; // primer frame de la nueva fila
            elapsed = 0f;
        }
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public int getFrameW() {
        return frameW;
    }

    public int getFrameH() {
        return frameH;
    }
}