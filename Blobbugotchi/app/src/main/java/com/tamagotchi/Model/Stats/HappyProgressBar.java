package com.tamagotchi.Model.Stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class HappyProgressBar extends View {

    private int progress ; // 0-100
    private int maxProgress = 100;
    private Paint borderPaint, fillPaint, bgPaint;

    public HappyProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#2D2D2D"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setAntiAlias(false); // Efecto pixelado

        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#C8E0C8")); // Fondo verde claro
        bgPaint.setAntiAlias(false);

        fillPaint = new Paint();
        fillPaint.setAntiAlias(false);
    }

    public void setProgress(int value) {
        this.progress = Math.min(value, maxProgress);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        int padding = 4;

        // Fondo
        canvas.drawRect(padding, padding, w - padding, h - padding, bgPaint);

        // Relleno con degradado de color según % (rojo -> amarillo -> verde)
        float ratio = (float) progress / maxProgress;
        int fillRight = (int) ((w - padding * 2) * ratio) + padding;

        // Segmentos de color
        int segments = 10;
        int segWidth = (w - padding * 2) / segments;
        int filledSegments = (int) (segments * ratio);

        for (int i = 0; i < filledSegments; i++) {
            float hue = 10f + (i / (float) segments) * 110f; // Rojo -> verde vivo
            fillPaint.setColor(Color.HSVToColor(new float[]{hue, 1f, 1f}));
            int left = padding + i * segWidth + 2;
            int right = left + segWidth - 2;
            canvas.drawRect(left, padding + 2, right, h - padding - 2, fillPaint);
        }

        // Borde exterior (estilo pixel)
        canvas.drawRect(padding, padding, w - padding, h - padding, borderPaint);
    }
}