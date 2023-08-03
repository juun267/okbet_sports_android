package org.cxct.sportlottery.util

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.text.TextUtils
import java.io.BufferedReader
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.DecimalFormat

object FileUtil {

    @JvmStatic
    fun writeStringToFile(string: String, file: File) {
        if (TextUtils.isEmpty(string)) {
            return
        }
        var writer: OutputStreamWriter? = null
        try {
            writer = OutputStreamWriter(FileOutputStream(file, false))
            writer.write(string)
            writer.flush()
        } catch (e: java.lang.Exception) {
        } finally {
            closeQuietly(writer)
        }
    }


    @JvmStatic
    fun readStringFromFile(file: File?): String? {
        if (!isFileExist(file)) {
            return null
        }
        var reader: BufferedReader? = null
        val builder = java.lang.StringBuilder()
        try {
            reader =
                BufferedReader(InputStreamReader(FileInputStream(file)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (builder.isNotEmpty()) {
                    builder.append("\n")
                }
                builder.append(line)
            }
        } catch (e: java.lang.Exception) {
        } finally {
            closeQuietly(reader)
        }
        return builder.toString()
    }

    @JvmStatic
    fun readStringFromAssetManager(assetManager: AssetManager?, path: String?): String? {
        var inputStream: InputStream? = null
        return try {
            inputStream = assetManager!!.open(path!!)
            val inputStreamReader = InputStreamReader(inputStream, Charsets.UTF_8)
            val reader = BufferedReader(inputStreamReader)
            val builder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null)
                builder.append(line)

            builder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            closeQuietly(inputStream)
        }
    }

    fun deleteFile(file: File?): Boolean {
        val flag = false
        return if (file == null || !file.exists()) {
            flag
        } else {
            if (file.isFile) {
                delete(file)
            } else {
                deleteDirectory(file)
            }
        }
    }


    /**
     * 刪除檔案
     * **/
    @JvmStatic
    private fun delete(file: File): Boolean {
        return isFileExist(file) && file.delete()
    }

    /**
     * 刪除文件夾
     * **/
    @JvmStatic
    private fun deleteDirectory(dirFile: File): Boolean {
        if (!isFileExist(dirFile) || !dirFile.isDirectory) {
            return false
        }
        var flag = true
        val files = dirFile.listFiles()
        for (file in files!!) {
            flag = if (file.isFile) {
                delete(file)
            } else {
                deleteDirectory(file)
            }
        }
        if (!flag) {
            return false
        }
        return dirFile.delete()
    }

    @JvmStatic
    fun isFileExist(file: File?): Boolean {
        return file != null && file.exists()
    }

    @JvmStatic
    private fun closeQuietly(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
            }
        }
    }

    /**
     * 获取指定路径文件大小
     * @param filePath 文件路径
     * @return double类型的值 对应MB
     */
    @JvmStatic
     fun getFilesSizeByType(filePath: String?,sizeType:Int): Double {
        var file = filePath?.let { File(it) }
        var blockSize:Long = 0
        try {
            if (file != null) {
                blockSize = getFileSize(file)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

        return formatFileSize(blockSize,sizeType)
    }
    /**
     * 获取文件大小
     */
    @JvmStatic
     fun getFileSize(file: File?): Long{
        var size:Long = 0
        try {
            if (file != null) {
                if (file.exists()){
                  var fis = FileInputStream(file)
                    size = fis.available().toLong()
                }else{

                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return size
    }
    /**
     * 转化文件大小
     * @param fileS 文件大小
     * @param sizeType 大小类型 0:B,1:KB,2:MB 3:GB
     */
    @JvmStatic
     fun formatFileSize(fileS: Long,sizeType: Int):Double{
        var df = DecimalFormat("#.00")
        var fileSizeLong = 0.0
        when(sizeType){
            0 ->{
                fileSizeLong = df.format(fileS).toDouble()
            }
            1 ->{
                fileSizeLong = df.format(fileS).toDouble()/1024
            }
            2->{
                fileSizeLong = df.format(fileS).toDouble()/1048576
            }
            3->{
                fileSizeLong = df.format(fileS).toDouble()/1073741824
            }
        }
        return  fileSizeLong
    }
    /**
     * 获取文件后缀名返回为png、jpeg、gif等字段
     * 手机中会显示jpg对应jpeg格式
     * @param imagePath 图片路径
     * @return imageType 图片类型
     */
    @JvmStatic
    fun getImageType(imagePath:String):String {
        var imageType:String
        var option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath,option)
        imageType = option.outMimeType
        if (!imageType.isNullOrEmpty()){
            imageType = imageType.substring(6,imageType.length)
        }
        return imageType
    }


    fun saveFile(file:File){
        val inputStream: InputStream = file.inputStream()
        val fos =FileOutputStream(file)
        try {
            val buf = ByteArray(2048)
            var len = 0
            while (inputStream.read(buf).also { len = it } != -1) {
                fos.write(buf, 0, len)
//            sum += len.toLong()
            }
            fos.flush()
        }catch (e:java.lang.Exception){

        }finally {
            try {
                if(inputStream!=null){
                    inputStream.close()
                }
                if(fos!=null){
                    fos.close()
                }
            }catch (e:java.lang.Exception){}
        }

    }
}