package com.min.album.view.divider;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 列表布局分割线，可以设置最后一个条目是否绘制分割线
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable mDivider;

    private int mOrientation;

    private boolean mIsEndDraw;

    public DividerItemDecoration(Context context, int orientation) {
        setDefaultDrawable(context);
        setOrientation(orientation);
    }

    public DividerItemDecoration(Context context, int orientation , int drawableId) {
        if(drawableId>0){
            mDivider = context.getResources().getDrawable(drawableId);
        }else{
            setDefaultDrawable(context);
        }
        setOrientation(orientation);
    }

    public DividerItemDecoration(Context context, int orientation , Drawable drawable) {
        if(drawable!=null){
            mDivider = drawable;
        }else{
            setDefaultDrawable(context);
        }
        setOrientation(orientation);
    }

    private void setDefaultDrawable(Context context){
        TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    public void setIsEndDraw(boolean isEndDraw){
        mIsEndDraw=isEndDraw;
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount=parent.getChildCount()-1;
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        int top = parent.getPaddingTop();
        int bottom = parent.getHeight() - parent.getPaddingBottom();

        int childCount = parent.getChildCount()-1;
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            int left = child.getRight() + params.rightMargin;
            int right = left + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(mIsEndDraw){
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }else{
            int adapterCount = parent.getAdapter().getItemCount();
            int itemPosition=((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

            if (mOrientation == VERTICAL_LIST) {
                if(itemPosition!=adapterCount-1){
                    outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
                }else{
                    outRect.set(0, 0, 0, 0);
                }
            } else {
                if(itemPosition!=adapterCount-1){
                    outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
                }else{
                    outRect.set(0, 0, 0, 0);
                }
            }
        }
    }
}