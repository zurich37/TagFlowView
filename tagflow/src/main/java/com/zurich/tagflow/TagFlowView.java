package com.zurich.tagflow;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class TagFlowView extends ViewGroup {
    /**
     * 居中
     */
    private static final int POSITION_CENTER = 0;
    /**
     * 上部对齐
     */
    private static final int POSITION_TOP = 1;
    /**
     * 底部对齐
     */
    private static final int POSITION_BOTTOM = 2;
    /**
     * 默认居中对齐
     */
    private int layoutPosition = POSITION_CENTER;

    List<List<View>> mViewLines = new ArrayList<>();
    List<Integer> mLineHeights = new ArrayList<>();

    public TagFlowView(Context context) {
        this(context, null);
    }

    public TagFlowView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagFlowView);
        layoutPosition = typedArray.getInt(R.styleable.TagFlowView_layout_position, 0);
        typedArray.recycle();
    }

    /* 处理子View带有Margin情况 需重写以下三个方法 */

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 获取开发者限定的尺寸模式
        int iWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int iHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        int iWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int iHeightSize = MeasureSpec.getSize(heightMeasureSpec);

        int measureWidth = 0;
        int measureHeight = 0;

        // 如果宽高都是确定的值，子View也是确定的值
        if (iWidthMode == MeasureSpec.EXACTLY && iHeightMode == MeasureSpec.EXACTLY) {
            measureWidth = iWidthSize;
            measureHeight = iHeightSize;
        } else {
            int childCount = getChildCount();
            int curLineWidth = 0;
            int curLineHeight = 0;
            List<View> lineViews = new ArrayList<>();
            if (mLineHeights.size() > 0) {
                mLineHeights.clear();
            }
            if (mViewLines.size() > 0) {
                mViewLines.clear();
            }

            // 测量每个子View
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

                // 获取子View的margin
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) child.getLayoutParams();
                int iChildWidth = child.getMeasuredWidth() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
                int iChildHeight = child.getMeasuredHeight() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;

                // 如果一行子view的宽度超过父View的期望值就换行
                if (curLineWidth + iChildWidth > iWidthSize) {
                    measureWidth = Math.max(measureWidth, curLineWidth);
                    measureHeight += curLineHeight;

                    // 保存每一行的view、高度值
                    mViewLines.add(lineViews);
                    mLineHeights.add(curLineHeight);

                    // 换行
                    curLineWidth = iChildWidth;
                    curLineHeight = iChildHeight;

                    // 换行后重新保存新一行的view
                    lineViews = new ArrayList<>();
                    lineViews.add(child);
                } else {
                    // 宽度累加、高度取一行中view高度的最大值
                    curLineWidth += iChildWidth;
                    curLineHeight = Math.max(iChildHeight, curLineHeight);

                    lineViews.add(child);
                }

                // 处理最后一行，因为没有换行，行信息没有保存
                if (i == getChildCount() - 1) {
                    measureWidth = Math.max(measureWidth, curLineWidth);
                    measureHeight += curLineHeight;
                    mViewLines.add(lineViews);
                    mLineHeights.add(curLineHeight);
                }
            }
        }

        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int lineCount = mViewLines.size();

        int left;
        int top = 0;
        int right;
        int bottom = 0;
        int curLeft = 0;
        int curTop = 0;
        for (int i = 0; i < lineCount; i++) {
            //每一行
            List<View> lineViews = mViewLines.get(i);
            int lineViewSize = lineViews.size();

            // 如果居中或底部对齐，需重新计算top位置
            int max = 0;
            for (View view : lineViews) {
                max = Math.max(view.getMeasuredHeight(), max);
            }

            for (int j = 0; j < lineViewSize; j++) {
                // 每行的每一个View
                View childView = lineViews.get(j);

                MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();

                left = curLeft + params.leftMargin;
                switch (layoutPosition) {
                    case POSITION_CENTER:
                        if (max > childView.getMeasuredHeight()) {
                            int diff = (max - childView.getMeasuredHeight()) / 2;
                            top = diff + curTop + params.topMargin;
                            bottom = top + childView.getMeasuredHeight();
                        } else {
                            top = curTop + params.topMargin;
                            bottom = top + childView.getMeasuredHeight();
                        }
                        break;
                    case POSITION_BOTTOM:
                        if (max > childView.getMeasuredHeight()) {
                            int diff = max - childView.getMeasuredHeight();
                            top = diff + curTop + params.topMargin;
                            bottom = top + childView.getMeasuredHeight();
                        } else {
                            top = curTop + params.topMargin;
                            bottom = top + childView.getMeasuredHeight();
                        }
                        break;
                    case POSITION_TOP:
                        top = curTop + params.topMargin;
                        bottom = top + childView.getMeasuredHeight();
                        break;
                    default:
                        break;

                }
                right = left + childView.getMeasuredWidth();


                childView.layout(left, top, right, bottom);
                curLeft += params.leftMargin + childView.getMeasuredWidth() + params.rightMargin;
            }

            // 换行后curLeft复位
            curLeft = 0;
            // 更新curTop值
            curTop += mLineHeights.get(i);
        }
    }

    public void setStyle(int style) {
        layoutPosition = style;
        requestLayout();
    }

    public int getLayoutPosition() {
        return layoutPosition;
    }

    public interface OnItemClickListener {
        /**
         * item点击回调
         * @param view 被点击的view
         * @param position 位置
         */
        void onItemClick(View view, int position);
    }

    private OnItemClickListener clickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            final int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemClick(v, finalI);
                    }
                }
            });
        }
    }
}
