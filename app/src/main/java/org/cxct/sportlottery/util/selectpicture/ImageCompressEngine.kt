package org.cxct.sportlottery.util.selectpicture

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.engine.CompressEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnCallbackListener
import com.luck.picture.lib.utils.DateUtils
import com.luck.picture.lib.utils.SdkVersionUtils
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

class ImageCompressEngine(private val maxSize: Int = 100) : CompressEngine {

    override fun onStartCompress(
        context: Context,
        list: ArrayList<LocalMedia>,
        listener: OnCallbackListener<ArrayList<LocalMedia>>) {
        // 自定义压缩
        val compress: MutableList<Uri> = ArrayList()
        for (i in list.indices) {
            val media = list[i]
            val availablePath = media.availablePath
            val uri = if (PictureMimeType.isContent(availablePath) || PictureMimeType.isHasHttp(
                    availablePath)
            ) Uri.parse(availablePath) else Uri.fromFile(File(availablePath))
            compress.add(uri)
        }
        if (compress.size == 0) {
            listener.onCall(list)
            return
        }
        Luban.with(context)
            .load(compress)
            .ignoreBy(maxSize)
            .filter { path ->
                PictureMimeType.isUrlHasImage(path) && !PictureMimeType.isHasHttp(path)
            }
            .setRenameListener { filePath ->
                val indexOf = filePath.lastIndexOf(".")
                val postfix = if (indexOf != -1) filePath.substring(indexOf) else ".jpg"
                DateUtils.getCreateFileName("CMP_") + postfix
            }
            .setCompressListener(object : OnCompressListener {
                override fun onStart() {}
                override fun onSuccess(index: Int, compressFile: File) {
                    val media = list[index]
                    if (compressFile.exists() && !TextUtils.isEmpty(compressFile.getAbsolutePath())) {
                        media.isCompressed = true
                        media.compressPath = compressFile.getAbsolutePath()
                        media.sandboxPath =
                            if (SdkVersionUtils.isQ()) media.compressPath else null
                    }
                    if (index == list.size - 1) {
                        listener.onCall(list)
                    }
                }

                override fun onError(index: Int, e: Throwable?) {
                    if (index != -1) {
                        val media = list[index]
                        media.isCompressed = false
                        media.compressPath = null
                        media.sandboxPath = null
                        if (index == list.size - 1) {
                            listener.onCall(list)
                        }
                    }
                }
            }).launch()
    }
}
