package com.github.echat.chat.otherui;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import com.echat.jzvd.JZDataSource;
import com.echat.jzvd.JZMediaManager;
import com.echat.jzvd.JZUserAction;
import com.echat.jzvd.JZVideoPlayerStandard;
import com.github.echat.chat.R;
import com.github.echat.chat.utils.Constants;

import static com.github.echat.chat.utils.Constants.EXTRA_VIDEO_FILE_NAME;
import static com.github.echat.chat.utils.Constants.EXTRA_VIDEO_URL;

public class CustomVideoPlayerStandard extends JZVideoPlayerStandard {

    public CustomVideoPlayerStandard(Context context) {
        super(context);
    }

    public CustomVideoPlayerStandard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    float xDown, yDown;
    boolean isLongClickModule = false;
    boolean isLongClicking = false;

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        if (v.getId() == R.id.surface_container) {
            //当按下时处理
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                xDown = motionEvent.getX();
                yDown = motionEvent.getY();
                return super.onTouch(v, motionEvent);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {// 松开处理
                //获取松开时的x坐标
                if (isLongClickModule) {
                    isLongClickModule = false;
                    isLongClicking = false;
                } else {
                    return super.onTouch(v, motionEvent);
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                //当滑动时背景为选中状态 //检测是否长按,在非长按时检测
                if (!isLongClickModule) {
                    isLongClickModule = isLongPressed(xDown, yDown, motionEvent.getX(),
                            motionEvent.getY(), motionEvent.getDownTime(), motionEvent.getEventTime(), 500);
                }
                if (isLongClickModule && !isLongClicking) {
                    //处理长按事件
                    isLongClicking = true;
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    v.playSoundEffect(SoundEffectConstants.CLICK);
                    if (currentState == CURRENT_STATE_PLAYING) {
                        onEvent(JZUserAction.ON_CLICK_PAUSE);
                        JZMediaManager.pause();
                        onStatePause();
                    }
                    Intent intent = new Intent(Constants.ACTION_DOWNLOAD_VIDEO);
                    try {
                        intent.putExtra(EXTRA_VIDEO_URL, String.valueOf(jzDataSource.urlsMap.get(JZDataSource.URL_KEY_DEFAULT)));
                        intent.putExtra(EXTRA_VIDEO_FILE_NAME, String.valueOf(jzDataSource.objects[0]));
                    } catch (Exception e) {
                    }
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                    return true;
                }
                if (!isLongClicking) {
                    return super.onTouch(v, motionEvent);
                }
            } else {
                //其他模式
                return super.onTouch(v, motionEvent);
            }
        } else {
            return super.onTouch(v, motionEvent);
        }
        return false;
    }

    private boolean isLongPressed(float lastX, float lastY,
                                  float thisX, float thisY,
                                  long lastDownTime, long thisEventTime,
                                  long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        return offsetX <= 10 && offsetY <= 10 && intervalTime >= longPressTime;
    }
}
