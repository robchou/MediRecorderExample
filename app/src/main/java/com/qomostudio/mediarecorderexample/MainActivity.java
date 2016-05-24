package com.qomostudio.mediarecorderexample;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();

    Camera mCamera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    ImageButton captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void initViews() {
        captureButton = (ImageButton)findViewById(R.id.btn_capture);
        surfaceView = (SurfaceView)findViewById(R.id.sv_preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;

        try {
            c = Camera.open(cameraId);
        } catch (Exception e) {
            Log.e(TAG, "Error while open camera " + cameraId + e.getMessage());
        }

        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
    }
}
