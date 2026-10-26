package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The grid backdrop (config.xml, grid_backdrop_color): a radial fade from a dark colour to the background colour,
 * with a faint 3 x 3 grid over it. Everything that costs anything (the gradient shader and the line endpoints)
 * is built once per size in {@link #onBoundsChange}; {@link #draw} only issues two draw calls and allocates
 * nothing. Nothing animates it, so the view it sits on is recorded once and replayed as-is while the tiles, the
 * glass and the glow animate above it.
 */
final class GridBackdropDrawable extends Drawable {

    private final int center;
    private final int background;
    private final Paint fill = new Paint();
    private final Paint line = new Paint();
    private float[] lines = new float[0];

    GridBackdropDrawable(int center, int background) {
        this.center = center;
        this.background = background;
        fill.setColor(0xFF000000 | background);   // until the first size arrives
        line.setColor(GridBackdrop.LINE_COLOR);
        // Width 0 is a hairline: exactly one pixel at any density. Antialiasing stays off (the default), since the
        // lines are straight and sit on pixel centres.
        line.setStrokeWidth(0f);
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        int w = bounds.width();
        int h = bounds.height();
        if (w <= 0 || h <= 0) {
            fill.setShader(null);
            lines = new float[0];
            return;
        }
        fill.setShader(new RadialGradient(
                bounds.left + w * GridBackdrop.CENTER_X, bounds.top + h * GridBackdrop.CENTER_Y,
                GridBackdrop.radius(w, h), GridBackdrop.colors(center, background), GridBackdrop.STOPS,
                Shader.TileMode.CLAMP));
        float[] next = GridBackdrop.lines(w, h);
        for (int i = 0; i < next.length; i += 2) {
            next[i] += bounds.left;
            next[i + 1] += bounds.top;
        }
        lines = next;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRect(getBounds(), fill);
        if (lines.length > 0) canvas.drawLines(lines, line);
    }

    @Override
    public void setAlpha(int alpha) {
        fill.setAlpha(alpha);
        line.setAlpha(Color.alpha(GridBackdrop.LINE_COLOR) * alpha / 255);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter filter) {
        fill.setColorFilter(filter);
        line.setColorFilter(filter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return fill.getAlpha() == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }
}
