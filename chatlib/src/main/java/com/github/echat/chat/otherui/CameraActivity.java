package com.github.echat.chat.otherui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.echat.cameralibrary.CaptureLayout;
import com.echat.cameralibrary.JCameraView;
import com.echat.cameralibrary.listener.ClickListener;
import com.echat.cameralibrary.listener.ErrorListener;
import com.echat.cameralibrary.listener.JCameraListener;
import com.echat.cameralibrary.util.FileUtil;
import com.github.echat.chat.R;

import java.io.File;
import java.lang.reflect.Field;


public class CameraActivity extends AppCompatActivity {

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final String EXTRA_RESULT_PIC_PATH = "extra_result_pic_path";
    private static final String EXTRA_RESULT_VIDEO_PATH = "extra_result_video_path";

    JCameraView jCameraView;

    public static final int REQUEST_CODE_CUSTOM_CAMERA = 50001;
    public static final int RESULT_CODE_PICTURE = 101;
    public static final int RESULT_CODE_VIDEO = 102;
    public static final int RESULT_CODE_NO_PERMISSION = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.layout_camera_echat);

        jCameraView = findViewById(R.id.jcameraview);

        //设置视频保存路径
        String path;
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = PathUtils.getInternalAppCachePath();
        } else {
            path = PathUtils.getExternalAppDcimPath();
        }
        path = path + File.separator + "Echat";
        jCameraView.setSaveVideoPath(path);
        jCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);
        jCameraView.setTip("轻触拍照，长按摄像");

        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Intent intent = new Intent();
                setResult(RESULT_CODE_NO_PERMISSION, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(CameraActivity.this, "无录音权限", Toast.LENGTH_SHORT).show();
            }
        });
        //JCameraView监听
        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
                String path = FileUtil.saveBitmap(getBaseContext(), "JCamera", bitmap);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT_PIC_PATH, path);
                setResult(RESULT_CODE_PICTURE, intent);
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
                String path = FileUtil.saveBitmap(getBaseContext(), "JCamera", firstFrame);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT_PIC_PATH, path);
                intent.putExtra(EXTRA_RESULT_VIDEO_PATH, url);
                setResult(RESULT_CODE_VIDEO, intent);
                finish();
            }
        });

        jCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                CameraActivity.this.finish();
            }
        });
        jCameraView.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {
                Toast.makeText(CameraActivity.this, "Right", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        jCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        jCameraView.onPause();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public static String obtainPicPathResult(Intent data) {
        return data.getStringExtra(EXTRA_RESULT_PIC_PATH);
    }

    public static String obtainVideoPathResult(Intent data) {
        return data.getStringExtra(EXTRA_RESULT_VIDEO_PATH);
    }

}
