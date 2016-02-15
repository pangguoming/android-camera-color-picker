package com.rainbow.whatcolor;

import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Camera.PreviewCallback;
/**
 * CameraPreview class that is extended only for the purpose of testing CameraPreview class.
 * This class is added functionality to set arbitrary preview size, and removed automated retry function to start preview on exception.
 */
public class ResizableCameraPreview extends CameraPreview implements PreviewCallback {
    private static boolean DEBUGGING = true;
    private static final String LOG_TAG = "ResizableCameraPreviewSample";
    private int[] pixels;
    private MainActivity  mainActivity;
    private Parameters parameters;
    private Size previewSize;

    public ResizableCameraPreview(Activity activity, int cameraId, LayoutMode mode, boolean addReversedSizes) {
        super(activity, cameraId, mode);
        mainActivity=(MainActivity) activity;
        pixels = new int[0];
        mCamera.setPreviewCallback(this);
        if (addReversedSizes) {
            List<Camera.Size> sizes = mPreviewSizeList;
            int length = sizes.size();
            for (int i = 0; i < length; i++) {
                Camera.Size size = sizes.get(i);
                Camera.Size revSize = mCamera.new Size(size.height, size.width);
                sizes.add(revSize);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        super.setPreviewCallback(this);
        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();
        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            if (DEBUGGING) { Log.v(LOG_TAG, "Desired Preview Size - w: " + width + ", h: " + height); }
            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            if (mSurfaceConfiguring) {
                return;
            }
        }
        pixels = new int[mPreviewSize.width * mPreviewSize.height];
        configureCameraParameters(cameraParams, portrait);
        mSurfaceConfiguring = false;
        try {
            mCamera.startPreview();
        } catch (Exception e) {
            Toast.makeText(mActivity, "Failed to start preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.w(LOG_TAG, "Failed to start preview: " + e.getMessage());
        }
    }
    // @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // transforms NV21 pixel data into RGB pixels
        if(pixels==null )return;
        decodeYUV420SP(pixels, data, mPreviewSize.width, mPreviewSize.height);
        mainActivity.setTv_color_HEX(Integer.toHexString(pixels[pixels.length / 2+mPreviewSize.height/2]));
        // Outuput the value of the top left pixel in the preview to LogCat
        //Log.i("Pixels", Integer.toHexString(pixels[0]));
    }

    /**
     *
     * @param index selects preview size from the list returned by CameraPreview.getSupportedPreivewSizes().
     * @param width is the width of the available area for this view
     * @param height is the height of the available area for this view
     */
    public void setPreviewSize(int index, int width, int height) {
        mCamera.stopPreview();
        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();
        Camera.Size previewSize = mPreviewSizeList.get(index);
        Camera.Size pictureSize = determinePictureSize(previewSize);
        if (DEBUGGING) { Log.v(LOG_TAG, "Requested Preview Size - w: " + previewSize.width + ", h: " + previewSize.height); }
        mPreviewSize = previewSize;
        mPictureSize = pictureSize;
        boolean layoutChanged = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
        if (layoutChanged) {
            mSurfaceConfiguring = true;
            return;
        }
        configureCameraParameters(cameraParams, portrait);
        try {
            mCamera.startPreview();
        } catch (Exception e) {
            Toast.makeText(mActivity, "Failed to satart preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        mSurfaceConfiguring = false;
    }

    public List<Camera.Size> getSupportedPreivewSizes() {
        return mPreviewSizeList;
    }

    // Method from Ketai project! Not mine! See below...
    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
}
