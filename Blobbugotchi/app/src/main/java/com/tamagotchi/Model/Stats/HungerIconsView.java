package com.tamagotchi.Model.Stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;

import com.tamagotchi.R;

public class HungerIconsView extends View {

    private int hunger = 0;
    private int maxIcons = 6;
    private Drawable fishFilled;
    private Drawable fishEmpty;

    public HungerIconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // Pescado relleno — azul sólido
        fishFilled = AppCompatResources.getDrawable(context, R.drawable.ic_fish_filled);

        // Pescado vacío — solo contorno
        fishEmpty = AppCompatResources.getDrawable(context, R.drawable.ic_fish_empty);
    }

    public void setHunger(int value) {
        this.hunger = Math.min(value, maxIcons);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int iconSize = getHeight();
        int spacing = iconSize + 6;

        for (int i = 0; i < maxIcons; i++) {
            int left = i * spacing;
            Drawable icon = (i < hunger) ? fishFilled : fishEmpty;

            if (icon != null) {
                icon.setBounds(left, 0, left + iconSize, iconSize);
                icon.draw(canvas);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int iconSize = getMeasuredHeight();
        int totalWidth = maxIcons * iconSize + (maxIcons - 1) * 6;
        setMeasuredDimension(totalWidth, iconSize);
    }
}