package com.tyron.completion.java.drawable;

import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;

import com.tyron.completion.java.CompletionModule;
import com.tyron.completion.model.DrawableKind;

public class CircleDrawable extends Drawable {

    private final Paint mPaint;
    private final Paint mTextPaint;
    
    private final DrawableKind mKind;
    
    public CircleDrawable(DrawableKind kind) {
        mKind = kind;
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(kind.getColor());
        
        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(dp(14));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    @Override
    public void draw(Canvas canvas) {
        float width = getBounds().right;
        float height = getBounds().bottom;

        canvas.drawRect(0, 0, width, height, mPaint);

        canvas.save();
        canvas.translate(width / 2f, height / 2f);
        float textCenter = (-(mTextPaint.descent() + mTextPaint.ascent()) / 2f);
        canvas.drawText(mKind.getValue(), 0, textCenter, mTextPaint);
        canvas.restore();
        
    }

    @Override
    public void setAlpha(int p1) {
        throw new UnsupportedOperationException("setAlpha is not supported on CircleDrawable");
    }

    @Override
    public void setColorFilter(ColorFilter p1) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    private static float dp(int px) {
        return Math.round(CompletionModule.getContext()
                .getResources().getDisplayMetrics().density * px);
    }

}
