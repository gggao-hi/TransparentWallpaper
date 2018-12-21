package com.fooww.transparentwallpaper;


import android.hardware.Camera;
import android.os.Environment;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author ggg
 * @version 1.0
 * @date 2018/12/21 11:17
 * @description
 */
public class TransparentWallPaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new CameraEngine();
    }

    class CameraEngine extends Engine implements Camera.PreviewCallback, Camera.PictureCallback {
        private Camera camera;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            startPreview();
            setTouchEventsEnabled(true);
        }


        private void startPreview() {
            camera = Camera.open();
            autoFous();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    camera.takePicture(null, null, CameraEngine.this);
                }
            });
            camera.setDisplayOrientation(90);
            try {
                camera.setPreviewDisplay(getSurfaceHolder());
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void autoFous() {
            Camera.Parameters parameters = camera.getParameters();
            //实现Camera自动对焦
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null) {
                for (String mode : focusModes) {
                    if (mode.contains("continuous-video")) {
                        parameters.setFocusMode("continuous-video");
                    } else if (mode.contains("auto")) {
                        parameters.setFocusMode("auto");
                    } else if (mode.contains("continuous-picture")) {
                        parameters.setFocusMode("continuous-picture");
                    }
                }
            }
            camera.setParameters(parameters);
        }

        private void stopPreview() {
            try {
                if (camera != null) {
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    camera.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            camera.addCallbackBuffer(data);
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File
                    .separator + "wallpapger", System.currentTimeMillis() + ".jpg");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(data);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            stopPreview();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                startPreview();
            } else {
                stopPreview();
            }
        }
    }
}
