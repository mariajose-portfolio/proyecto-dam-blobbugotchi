package com.tamagotchi.Model.Stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.tamagotchi.R;

public class SleepIconsView extends View {
    private int sleep = 0;
    private int maxIcons = 6;
    private Drawable moonFilled;
    private Drawable moonEmpty;

    public SleepIconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // Icono lleno — color morado original del tint
        moonFilled = AppCompatResources.getDrawable(context, R.drawable.ic_moon_filled);
        if (moonFilled != null) {
            moonFilled = DrawableCompat.wrap(moonFilled.mutate());
        }

        // Icono vacío — misma forma pero en gris
        moonEmpty = AppCompatResources.getDrawable(context, R.drawable.ic_moon_empty);
        if (moonEmpty != null) {
            moonEmpty = DrawableCompat.wrap(moonEmpty.mutate());
        }
    }

    public void setSleep(int value) {
        this.sleep = Math.min(value, maxIcons);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int iconSize = getHeight();
        int spacing = iconSize + 6;

        for (int i = 0; i < maxIcons; i++) {
            int left = i * spacing;
            Drawable icon = (i < sleep) ? moonFilled : moonEmpty;

            if (icon != null) {
                icon.setBounds(left, 0, left + iconSize, iconSize);
                icon.draw(canvas);
            }
        }
    }

    // Calcula el ancho ideal para que quepan todos los iconos
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int iconSize = getMeasuredHeight();
        int totalWidth = maxIcons * iconSize + (maxIcons - 1) * 6;
        setMeasuredDimension(totalWidth, iconSize);
    }
}