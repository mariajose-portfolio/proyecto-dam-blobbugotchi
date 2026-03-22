package com.tamagotchi.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircularTimerView extends View {

    // 0f = vacío, 1f = completo
    private float progress = 1f;

    private final Paint trackPaint;
    private final Paint progressPaint;
    private final RectF oval = new RectF();
    private final float strokeWidth;

    public CircularTimerView(Context context) {
        this(context, null);
    }

    public CircularTimerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularTimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = context.getResources().getDisplayMetrics().density;
        strokeWidth = 10f * density;

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(0x22000000); // gris translúcido para el fondo del arco
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(strokeWidth);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(0xFF1565C0); // dark_blue — cámbialo a tu color
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(strokeWidth);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - strokeWidth / 2f;

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Arco de fondo completo
        canvas.drawArc(oval, -90f, 360f, false, trackPaint);

        // Arco de progreso invertido — se llena con el paso del tiempo
        float sweepAngle = 360f * (1f - progress);
        canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint);
    }
}