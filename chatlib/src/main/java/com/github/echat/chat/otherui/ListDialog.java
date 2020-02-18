package com.github.echat.chat.otherui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.blankj.utilcode.util.LogUtils;
import com.github.echat.chat.R;

@SuppressLint("ValidFragment")
public class ListDialog {

    public interface OnItemClickListener {
        void onClick(int position);
    }

    private Context mContext;
    private BaseDialog mDialog;
    private ListDialog.Builder mBuilder;


    private Button btn_cancle;
    private Button btn_save;
    private RelativeLayout rl_bg;

    public static final class Builder {
        private Context mContext;

        //全屏模式隐藏状态栏
        boolean windowFullscreen;
        //Dialog进出动画
        int animationID;
        private OnItemClickListener onItemClickListener;

        public Builder setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        public Builder(Context context) {
            mContext = context;
            windowFullscreen = false;
        }

        public ListDialog build() {
            return new ListDialog(mContext, this);
        }

        public Builder isWindowFullscreen(@Nullable boolean windowFullscreen) {
            this.windowFullscreen = windowFullscreen;
            return this;
        }

        public Builder setAnimationID(@StyleRes int resId) {
            this.animationID = resId;
            return this;
        }
    }

    public ListDialog(Context context, Builder builder) {
        mContext = context;
        mBuilder = builder;
        if (mBuilder == null) {
            mBuilder = new Builder(mContext);
        }
        //初始化
        initDialog();
    }

    public void show() {
        try {
            if (mDialog == null) {
                return;
            }
            checkDialogConfig();
            mDialog.show();
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }

    private void initDialog() {
        checkDialogConfig();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View rootView = inflater.inflate(R.layout.dialog_list, null);
        mDialog = new BaseDialog(mContext, R.style.CustomDialog);
        mDialog.setContentView(rootView);
        mDialog.initStatusBar(mBuilder.windowFullscreen);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                releaseDialog();
            }
        });


        btn_cancle = rootView.findViewById(R.id.btn_cancle);
        btn_save = rootView.findViewById(R.id.btn_save);
        rl_bg = rootView.findViewById(R.id.rl_bg);
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBuilder.onItemClickListener != null) {
                    mBuilder.onItemClickListener.onClick(0);
                }
                mDialog.dismiss();
            }
        });

        rl_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBuilder.onItemClickListener != null) {
                    mBuilder.onItemClickListener.onClick(0);
                }
                mDialog.dismiss();
            }
        });


        try {
            //设置动画
            if (mBuilder != null && mBuilder.animationID != 0 && mDialog.getWindow() != null) {
                mDialog.getWindow().setWindowAnimations(mBuilder.animationID);
            }
        } catch (Exception e) {

        }
    }

    private void releaseDialog() {
        mDialog = null;
        mContext = null;
        mBuilder = null;
    }

    private void checkDialogConfig() {
        if (mBuilder == null) {
            mBuilder = new Builder(mContext);
        }
    }


}
