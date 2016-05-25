package com.qomostudio.mediarecorderexample;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String outputFilePath = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/output.mp4";


    Camera mCamera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    ImageButton captureButton;
    MediaRecorder mMediaRecorder;

    volatile boolean isRecordingStared = false;

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
        setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void initViews() {
        captureButton = (ImageButton)findViewById(R.id.btn_capture);
        captureButton.setOnClickListener(new CaptureListener());
        surfaceView = (SurfaceView)findViewById(R.id.sv_preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    class CaptureListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isRecordingStared) {
                releaseMediaRecorder();
            } else {
                prepareVideoRecorder();
                mMediaRecorder.start();
            }
        }
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

    private boolean prepareVideoRecorder() {
        if (mCamera == null) {
            mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
            setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        }
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

        mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            outputFile.delete();
        }

        mMediaRecorder.setOutputFile(outputFilePath);

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.e(TAG, "IlleagalStateException prepare MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.e(TAG, "IOException preparing MeidaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "#### surfaceCreated ####");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "#### surfaceChanged ####");
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
        Log.i(TAG, "#### surfaceDestroyed ####");
    }
}
