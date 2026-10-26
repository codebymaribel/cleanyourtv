package com.gathin.tvhome;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A nav tab's page indicator (nav_tabs): one short dash per title, the current one longer and white. It is also the
 * carousel's control: focused, Left and Right move between titles (TitleBrowser handles the keys), and it shows
 * brighter, over its focus background. Drawing allocates nothing.
 */
public final class PageDashes extends View {

    private static final int ACTIVE_COLOR = 0xFFFFFFFF;
    private static final int IDLE_COLOR = 0x59FFFFFF;
    private static final int IDLE_COLOR_FOCUSED = 0x8CFFFFFF;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float dash;
    private final float activeDash;
    private final float gap;
    private final float thickness;
    private int count;
    private int active = -1;

    public PageDashes(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        dash = res.getDimension(R.dimen.dash_width);
        activeDash = res.getDimension(R.dimen.dash_active_width);
        gap = res.getDimension(R.dimen.dash_gap);
        thickness = res.getDimension(R.dimen.dash_thickness);
    }

    /** Shows {@code count} dashes with {@code active} highlighted; lays out again only when the count changes. */
    void set(int count, int active) {
        if (count == this.count && active == this.active) return;
        boolean resize = count != this.count;
        this.count = count;
        this.active = active;
        if (resize) requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = (int) Math.ceil(DashLayout.totalWidth(count, dash, activeDash, gap)) + getPaddingLeft() + getPaddingRight();
        int h = (int) Math.ceil(thickness) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(w, widthMeasureSpec), resolveSize(h, heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float top = (getHeight() - thickness) / 2f;
        float radius = thickness / 2f;
        int idle = isFocused() ? IDLE_COLOR_FOCUSED : IDLE_COLOR;
        for (int i = 0; i < count; i++) {
            float left = getPaddingLeft() + DashLayout.left(i, active, dash, activeDash, gap);
            paint.setColor(i == active ? ACTIVE_COLOR : idle);
            canvas.drawRoundRect(left, top, left + DashLayout.width(i, active, dash, activeDash), top + thickness,
                    radius, radius, paint);
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable android.graphics.Rect previous) {
        super.onFocusChanged(gainFocus, direction, previous);
        invalidate();   // the idle dashes brighten while the carousel has focus
    }
}
