package com.github.echat.chat.otherui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.github.echat.chat.R;


/**
 * @Author: xuhaoyang
 * @Email: xuhaoyang3x@gmail.com
 * @program: BottomSheets
 * @create: 2018-12-19
 * @describe
 */
public class EChatCustomWebview extends WebView {

    private CoordinatorLayout bottomCoordinator;
    private float downY;
    private float moveY;
    private MoveCallbak moveCallbak;

    private boolean isOpenTouchInject;

    public EChatCustomWebview(Context context) {
        super(context);
    }

    public EChatCustomWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EChatCustomWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EChatCustomWebview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public void setOpenTouchInject(boolean openTouchInject) {
        isOpenTouchInject = openTouchInject;
    }

    private void init(Context context, AttributeSet attrs) {
        // 获取控件资源
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EChatCustomWebview);
        isOpenTouchInject = typedArray.getBoolean(R.styleable.EChatCustomWebview_openTouchInject, false);
    }

    public void bindBottomSheetDialog(FrameLayout container) {
        if (!isOpenTouchInject) return;
        try {
            bottomCoordinator =
                    (CoordinatorLayout) container.findViewById(R.id.coordinator);
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {


                    if (bottomCoordinator == null) {
                        return false;
                    }
                    if (moveCallbak != null) {
                        if (moveCallbak.isIntercept()) {
                            bottomCoordinator.requestDisallowInterceptTouchEvent(true);
                            return false;
                        }
                    }
                    final float diffValue = getContentHeight() * getScale() - (getHeight() + getScrollY());
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.i("setOnTouchListener", "ACTION_DOWN");
                            downY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            Log.i("setOnTouchListener", "ACTION_MOVE");
                            moveY = event.getRawY();
                            if (diffValue < 10) {
                                if (moveCallbak != null) {
                                    if (!moveCallbak.isIntercept())
                                        bottomCoordinator.requestDisallowInterceptTouchEvent(false);
                                }
                                break;
                            }
                            bottomCoordinator.requestDisallowInterceptTouchEvent(true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            bottomCoordinator.requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMoveCallbak(MoveCallbak moveCallbak) {
        this.moveCallbak = moveCallbak;
    }


    interface MoveCallbak {
        boolean isIntercept();
    }
}
