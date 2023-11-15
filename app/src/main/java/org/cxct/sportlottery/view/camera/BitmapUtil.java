package org.cxct.sportlottery.view.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.Surface;

import java.io.ByteArrayOutputStream;

public class BitmapUtil {

    /**
     * 解决拍出的图片的方向问题
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static Bitmap setRightRotationBitmap(Activity activity, int sensorRotation, byte[] data, int width, int height) {
        final Bitmap result;
        if (data != null && data.length > 0) {
            //直接用这个方法获取的bitmap是null的
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            //使用下面方式获取bitmap
            Bitmap bitmap = getBitmapFromByte(data, width, height);
            Matrix matrix = new Matrix();
            //利用传感器获取当前屏幕方向对应角度 加上 开始预览是角度
            int rotation = (calculateCameraPreviewOrientation(activity) + sensorRotation) % 360;
            if (CameraUtils.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                //如果是后置摄像头因为没有镜面效果直接旋转特定角度
                matrix.setRotate(rotation);
            } else {
                //如果是前置摄像头需要做镜面操作，然后对图片做镜面postScale(-1, 1)
                //因为镜面效果需要360-rotation，才是前置摄像头真正的旋转角度
                rotation = (360 - rotation) % 360;
                matrix.setRotate(rotation);
                matrix.postScale(-1, 1);
            }
            result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     */
    public static int calculateCameraPreviewOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(CameraUtils.getCameraId(), info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 将byte[]转换成Bitmap
     *
     * @param bytes
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getBitmapFromByte(byte[] bytes, int width, int height) {
        final YuvImage image = new YuvImage(bytes, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream(bytes.length);
        if (!image.compressToJpeg(new Rect(0, 0, width, height), 100, os)) {
            return null;
        }
        byte[] tmp = os.toByteArray();
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
        return bmp;
    }



}
