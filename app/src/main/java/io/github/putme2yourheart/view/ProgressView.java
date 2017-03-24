package io.github.putme2yourheart.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Frank on 2017/3/23.
 */

public class ProgressView extends View {

    private static final int min = 0;
    private static final int normal = 1;

    private static final int minSize = 32;
    private static final int normalSize = 48;

    private Context mContext;
    private int mWidth;
    private int mHeight;

    // 设置的大小min / normal
    private int mSize;
    // 当前进度
    private int mProgress;
    // 最大进度
    private int mMax;

    // 保存圆的大小(xp)
    private int s;
    // 两圆之间的间隔
    private int blank = 4;
    // 是否无限显示
    private volatile boolean isLimited;

    private RectF mRectF;
    private Paint mDrawArcPaint;
    private Paint mDrawCirclePaint;
    private Paint mPaint;

    // 完成时的接口
    private OnFinishListener mOnFinishListener = null;

    public ProgressView(Context context) {
        super(context);

        mContext = context;

        initData();
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;

        initAttrValue(context, attrs);
        initData();

        post(new Run());
    }

    private void initAttrValue(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, R.style.ProgressView);

        int count = typedArray.getIndexCount();

        for (int i = 0; i < count; i++) {
            int index = typedArray.getIndex(i);

            switch (index) {
                case R.styleable.ProgressView_progress:
                    mProgress = typedArray.getInteger(index, -1);
                    break;
                case R.styleable.ProgressView_max:
                    mMax = typedArray.getInteger(index, -1);
                    break;
                case R.styleable.ProgressView_size:
                    mSize = typedArray.getInteger(index, min);
                    break;
            }
        }

        typedArray.recycle();

        isLimited = !(mProgress < 0 || mMax <= 0 || mProgress >= mMax);
    }

    private void initData() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha(125);
        mPaint.setStrokeWidth(1f);
        // 消除锯齿
        mPaint.setAntiAlias(true);

        mDrawArcPaint = new Paint(mPaint);
        mDrawArcPaint.setColor(Color.GRAY);

        mDrawCirclePaint = new Paint(mPaint);
        mDrawCirclePaint.setColor(Color.BLACK);
        mDrawCirclePaint.setStyle(Paint.Style.STROKE);

        if (mSize == min) {
            s = dip2px(mContext, minSize);
        } else {
            s = dip2px(mContext, normalSize);
        }

        mRectF = new RectF(blank, blank, s - blank, s - blank);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);      // 取出宽度的确切数值
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);      // 取出宽度的测量模式

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);    // 取出高度的确切数值
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);    // 取出高度的测量模式

        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else {
            mWidth = s + getPaddingLeft() + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else {
            mHeight = s + getPaddingTop() + getPaddingBottom();
        }

        setMeasuredDimension(mWidth, mHeight);
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    int progress = 1;
    private class Run implements Runnable {

        @Override
        public void run() {
            if (progress >= 100) {
                progress = 0;
            }

            postInvalidate();
            progress++;
            if (!isLimited) {
                postDelayed(this, 50);
            }
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (isLimited) {
            canvas.drawBitmap(drawBitmap(), mWidth / 2 - s / 2, mHeight / 2 - s / 2, mPaint);

            if (mProgress == mMax) {
                if (mOnFinishListener != null) {
                    mOnFinishListener.onFinish();
                }
            }
        } else {
            canvas.drawBitmap(drawBitmap(progress), mWidth / 2 - s / 2, mHeight / 2 - s / 2, mPaint);
        }
    }

    private Bitmap drawBitmap() {

        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);

        canvas.drawCircle(s / 2, s / 2, s / 2, mDrawCirclePaint);

        canvas.drawCircle(s / 2, s / 2, s / 2 - blank, mDrawCirclePaint);

        canvas.drawArc(mRectF, -90, mMax == 0 ? 360 : ((float) mProgress / mMax) * 360, true, mDrawArcPaint);

        return b;
    }

    private Bitmap drawBitmap(int progress) {

        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);

        canvas.drawCircle(s / 2, s / 2, s / 2, mDrawCirclePaint);

        canvas.drawCircle(s / 2, s / 2, s / 2 - blank, mDrawCirclePaint);

        canvas.drawArc(mRectF, -90, ((float)progress / 100) * 360, true, mDrawArcPaint);

        return b;
    }

    public void setProgress(int progress) {
        mProgress = progress;

        if (mProgress < 0 || mProgress > mMax) {
            return;
        } else {
            isLimited = true;
        }

        postInvalidate();
    }

    public int getProgress() {
        return mProgress;
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        mMax = max;

        if (mMax > 0 && mMax > mProgress && mProgress >= 0) {
            isLimited = true;
        }
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        mOnFinishListener = onFinishListener;
    }
}
