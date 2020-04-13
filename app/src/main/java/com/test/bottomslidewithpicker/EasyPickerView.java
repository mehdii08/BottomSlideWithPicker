package com.test.bottomslidewithpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;
import java.util.ArrayList;

public class EasyPickerView extends View {
    private float bounceDistance;
    private int contentHeight;
    private int contentWidth;
    private int curIndex;

    private int cx;

    private int cy;
    private ArrayList<String> dataList;
    private float downY;

    private FontMetrics fm;
    private boolean isRecycleMode;
    private boolean isSliding;
    private int maxShowNum;
    private float maxTextWidth;
    private int maximumVelocity;
    private int minimumVelocity;
    private int offsetIndex;
    private float offsetY;
    private float oldOffsetY;
    private OnScrollChangedListener onScrollChangedListener;
    private int scaledTouchSlop;
    private Scroller scroller;
    private int textColor;
    private int textHeight;
    private float textMaxScale;
    private float textMinAlpha;
    private int textPadding;
    private TextPaint textPaint;
    private int textSize;
    private VelocityTracker velocityTracker;

    public interface OnScrollChangedListener {
        void onScrollChanged(int i);

        void onScrollFinished(int i);
    }

    public EasyPickerView(Context context) {
        this(context, null);
    }

    public EasyPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.dataList = new ArrayList<>();
        this.isSliding = false;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EasyPickerView, defStyleAttr, 0);
        textSize = a.getDimensionPixelSize(R.styleable.EasyPickerView_epvTextSize, (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
        textColor = a.getColor(R.styleable.EasyPickerView_epvTextColor, Color.BLACK);
        textPadding = a.getDimensionPixelSize(R.styleable.EasyPickerView_epvTextPadding, (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
        textMaxScale = a.getFloat(R.styleable.EasyPickerView_epvTextMaxScale, 2.0f);
        textMinAlpha = a.getFloat(R.styleable.EasyPickerView_epvTextMinAlpha, 0.4f);
        isRecycleMode = a.getBoolean(R.styleable.EasyPickerView_epvRecycleMode, true);
        maxShowNum = a.getInteger(R.styleable.EasyPickerView_epvMaxShowNum, 3);
        a.recycle();
        this.textPaint = new TextPaint();
        this.textPaint.setColor(this.textColor);
        this.textPaint.setTextSize((float) this.textSize);
        this.textPaint.setAntiAlias(true);
        this.fm = this.textPaint.getFontMetrics();
        this.textHeight = (int) (this.fm.bottom - this.fm.top);
//        this.textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "font/iransans_light.ttf"));
        this.scroller = new Scroller(context);
        this.minimumVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        this.maximumVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        this.scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        this.contentWidth = (int) ((this.maxTextWidth * this.textMaxScale) + ((float) getPaddingLeft()) + ((float) getPaddingRight()));
        if (mode != 1073741824) {
            width = this.contentWidth;
        }
        int mode2 = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int i = this.textHeight;
        int i2 = this.maxShowNum;
        this.contentHeight = (i * i2) + (this.textPadding * i2);
        if (mode2 != 1073741824) {
            height = this.contentHeight + getPaddingTop() + getPaddingBottom();
        }
        this.cx = width / 2;
        this.cy = height / 2;
        setMeasuredDimension(width, height);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        addVelocityTracker(event);
        int action = event.getAction();
        if (action == 0) {
            if (!this.scroller.isFinished()) {
                this.scroller.forceFinished(true);
                finishScroll();
            }
            this.downY = event.getY();
        } else if (action == 1) {
            int scrollYVelocity = (getScrollYVelocity() * 2) / 3;
            if (Math.abs(scrollYVelocity) > this.minimumVelocity) {
                this.oldOffsetY = this.offsetY;
                this.scroller.fling(0, 0, 0, scrollYVelocity, 0, 0, -2147483647, Integer.MAX_VALUE);
                invalidate();
            } else {
                finishScroll();
            }
            if (!this.isSliding) {
                float f = this.downY;
                int i = this.contentHeight;
                if (f < ((float) (i / 3))) {
                    moveBy(-1);
                } else if (f > ((float) ((i * 2) / 3))) {
                    moveBy(1);
                }
            }
            this.isSliding = false;
            recycleVelocityTracker();
        } else if (action == 2) {
            this.offsetY = event.getY() - this.downY;
            if (this.isSliding || Math.abs(this.offsetY) > ((float) this.scaledTouchSlop)) {
                this.isSliding = true;
                reDraw();
            }
        }
        return true;
    }

    protected void onDraw(Canvas canvas) {
        int centerPadding;
        int size;
        Canvas canvas2 = canvas;
        ArrayList<String> arrayList = this.dataList;
        if (arrayList != null && arrayList.size() > 0) {
            int i = this.cx;
            int i2 = this.contentWidth;
            int i3 = i - (i2 / 2);
            int i4 = this.cy;
            int i5 = this.contentHeight;
            canvas2.clipRect(i3, i4 - (i5 / 2), i + (i2 / 2), i4 + (i5 / 2));
            int size2 = this.dataList.size();
            int centerPadding2 = this.textHeight + this.textPadding;
            int half = (this.maxShowNum / 2) + 1;
            int i6 = -half;
            while (i6 <= half) {
                int index = (this.curIndex - this.offsetIndex) + i6;
                if (this.isRecycleMode) {
                    if (index < 0) {
                        index = (((index + 1) % this.dataList.size()) + this.dataList.size()) - 1;
                    } else if (index > this.dataList.size() - 1) {
                        index %= this.dataList.size();
                    }
                }
                if (index < 0 || index >= size2) {
                    size = size2;
                    centerPadding = centerPadding2;
                } else {
                    int i7 = this.cy;
                    int tempY = (int) (((float) ((i6 * centerPadding2) + i7)) + (this.offsetY % ((float) centerPadding2)));
                    float tempScale = ((this.textMaxScale - 1.0f) * (1.0f - ((((float) Math.abs(tempY - i7)) * 1.0f) / ((float) centerPadding2)))) + 1.0f;
                    float tempScale2 = tempScale < 1.0f ? 1.0f : tempScale;
                    float textAlpha = this.textMinAlpha;
                    float f = this.textMaxScale;
                    if (f != 1.0f) {
                        float tempAlpha = (tempScale2 - 1.0f) / (f - 1.0f);
                        float f2 = this.textMinAlpha;
                        textAlpha = ((1.0f - f2) * tempAlpha) + f2;
                    }
                    this.textPaint.setTextSize(((float) this.textSize) * tempScale2);
                    this.textPaint.setAlpha((int) (255.0f * textAlpha));
                    FontMetrics tempFm = this.textPaint.getFontMetrics();
                    String text = (String) this.dataList.get(index);
                    size = size2;
                    centerPadding = centerPadding2;
                    canvas2.drawText(text, ((float) this.cx) - (this.textPaint.measureText(text) / 2.0f), ((float) tempY) - ((tempFm.ascent + tempFm.descent) / 2.0f), this.textPaint);
                }
                i6++;
                size2 = size;
                centerPadding2 = centerPadding;
            }
            int i8 = centerPadding2;
        }
    }

    public void computeScroll() {
        if (this.scroller.computeScrollOffset()) {
            this.offsetY = this.oldOffsetY + ((float) this.scroller.getCurrY());
            if (!this.scroller.isFinished()) {
                reDraw();
            } else {
                finishScroll();
            }
        }
    }

    private void addVelocityTracker(MotionEvent event) {
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        this.velocityTracker.addMovement(event);
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker2 = this.velocityTracker;
        if (velocityTracker2 != null) {
            velocityTracker2.recycle();
            this.velocityTracker = null;
        }
    }

    private int getScrollYVelocity() {
        this.velocityTracker.computeCurrentVelocity(1000, (float) this.maximumVelocity);
        return (int) this.velocityTracker.getYVelocity();
    }

    private void reDraw() {
        int i = (int) (this.offsetY / ((float) (this.textHeight + this.textPadding)));
        if (!this.isRecycleMode) {
            int i2 = this.curIndex;
            if (i2 - i < 0 || i2 - i >= this.dataList.size()) {
                finishScroll();
                return;
            }
        }
        if (this.offsetIndex != i) {
            this.offsetIndex = i;
            OnScrollChangedListener onScrollChangedListener2 = this.onScrollChangedListener;
            if (onScrollChangedListener2 != null) {
                onScrollChangedListener2.onScrollChanged(getNowIndex(-this.offsetIndex));
            }
        }
        postInvalidate();
    }

    private void finishScroll() {
        int centerPadding = this.textHeight + this.textPadding;
        float v = this.offsetY % ((float) centerPadding);
        if (v > ((float) centerPadding) * 0.5f) {
            this.offsetIndex++;
        } else if (v < ((float) centerPadding) * -0.5f) {
            this.offsetIndex--;
        }
        this.curIndex = getNowIndex(-this.offsetIndex);
        float f = (float) (this.offsetIndex * centerPadding);
        float f2 = this.offsetY;
        this.bounceDistance = f - f2;
        this.offsetY = f2 + this.bounceDistance;
        OnScrollChangedListener onScrollChangedListener2 = this.onScrollChangedListener;
        if (onScrollChangedListener2 != null) {
            onScrollChangedListener2.onScrollFinished(this.curIndex);
        }
        reset();
        postInvalidate();
    }

    private int getNowIndex(int offsetIndex2) {
        int index = this.curIndex + offsetIndex2;
        if (this.isRecycleMode) {
            if (index < 0) {
                return (((index + 1) % this.dataList.size()) + this.dataList.size()) - 1;
            }
            if (index > this.dataList.size() - 1) {
                return index % this.dataList.size();
            }
            return index;
        } else if (index < 0) {
            return 0;
        } else {
            if (index > this.dataList.size() - 1) {
                return this.dataList.size() - 1;
            }
            return index;
        }
    }

    private void reset() {
        this.offsetY = 0.0f;
        this.oldOffsetY = 0.0f;
        this.offsetIndex = 0;
        this.bounceDistance = 0.0f;
    }

    public void setDataList(ArrayList<String> dataList2) {
        this.dataList.clear();
        this.dataList.addAll(dataList2);
        if (dataList2 != null && dataList2.size() > 0) {
            int size = dataList2.size();
            for (int i = 0; i < size; i++) {
                float tempWidth = this.textPaint.measureText((String) dataList2.get(i));
                if (tempWidth > this.maxTextWidth) {
                    this.maxTextWidth = tempWidth;
                }
            }
            this.curIndex = 0;
        }
        requestLayout();
        invalidate();
    }

    public int getCurIndex() {
        return getNowIndex(-this.offsetIndex);
    }

    public void moveTo(int index) {
        int dy;
        if (index >= 0 && index < this.dataList.size() && this.curIndex != index) {
            if (!this.scroller.isFinished()) {
                this.scroller.forceFinished(true);
            }
            finishScroll();
            int centerPadding = this.textHeight + this.textPadding;
            if (!this.isRecycleMode) {
                dy = (this.curIndex - index) * centerPadding;
            } else {
                int offsetIndex2 = this.curIndex - index;
                int d1 = Math.abs(offsetIndex2) * centerPadding;
                int d2 = (this.dataList.size() - Math.abs(offsetIndex2)) * centerPadding;
                if (offsetIndex2 > 0) {
                    if (d1 < d2) {
                        dy = d1;
                    } else {
                        dy = -d2;
                    }
                } else if (d1 < d2) {
                    dy = -d1;
                } else {
                    dy = d2;
                }
            }
            this.scroller.startScroll(0, 0, 0, dy, 500);
            invalidate();
        }
    }

    public void moveBy(int offsetIndex2) {
        moveTo(getNowIndex(offsetIndex2));
    }

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener2) {
        this.onScrollChangedListener = onScrollChangedListener2;
    }
}
