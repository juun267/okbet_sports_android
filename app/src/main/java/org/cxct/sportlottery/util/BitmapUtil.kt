package org.cxct.sportlottery.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateUtils
import android.util.Base64
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object BitmapUtil {

    //base64 轉化二進制字符成 Bitmap
    fun stringToBitmap(data: String?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val bitmapArray: ByteArray = Base64.decode(data, Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap?, callBack: (success: Boolean)->Unit) {
        if (bitmap == null) {
            return
        }
        val isSaveSuccess = if (Build.VERSION.SDK_INT < 29) {
            saveImageToGallery(context, bitmap)
        } else {
            saveImageToGallery1(context, bitmap)
        }
        callBack.invoke(isSaveSuccess)
    }

    /**
     * android 10 以下版本
     */
    private fun saveImageToGallery(context: Context, image: Bitmap): Boolean {
        // 首先保存图片
        val storePath =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "okbet"
        val appDir = File(storePath)
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            // 通过io流的方式来压缩保存图片
            val isSuccess = image.compress(Bitmap.CompressFormat.JPEG, 60, fos)
            fos.flush()
            fos.close()

            // 保存图片后发送广播通知更新数据库
            val uri = Uri.fromFile(file)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            return isSuccess
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * android 10 以上版本
     */
   private fun saveImageToGallery1(context: Context, image: Bitmap): Boolean {
        val mImageTime = System.currentTimeMillis()
        val imageDate = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date(mImageTime))
        val SCREENSHOT_FILE_NAME_TEMPLATE = "okbet_%s.png" //图片名称，以"okbet_"+时间戳命名
        val mImageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate)
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES
                + File.separator + "okbet") //Environment.DIRECTORY_SCREENSHOTS:截图,图库中显示的文件夹名。"dh"
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, mImageFileName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        values.put(MediaStore.MediaColumns.DATE_ADDED, mImageTime / 1000)
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, mImageTime / 1000)
        values.put(MediaStore.MediaColumns.DATE_EXPIRES,
            (mImageTime + DateUtils.DAY_IN_MILLIS) / 1000)
        values.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        try {
            // First, write the actual data for our screenshot
            resolver.openOutputStream(uri!!).use { out ->
                if (!image.compress(Bitmap.CompressFormat.PNG, 100,
                        out!!)
                ) {
                    return false
                }
            }
            // Everything went well above, publish it!
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            values.putNull(MediaStore.MediaColumns.DATE_EXPIRES)
            resolver.update(uri, values, null, null)
        } catch (e: IOException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                resolver.delete(uri!!, null)
            }
            return false
        }
        return true
    }
}
