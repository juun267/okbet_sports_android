package org.cxct.sportlottery.fix

import android.content.Context
import android.os.Environment
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.utils.FileDirMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.runWithCatch
import java.io.File


/**
 *  修复三方库pictureselector v3.11.1 bug (版本升级后删除该修复类)
 *  Fatal Exception: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.io.File.getPath()' on a null object reference
 *  at com.luck.picture.lib.utils.FileDirMap.init(FileDirMap.java:30)
  */

object PictureSelectorFix {

    fun fixBug(context: Context) = GlobalScope.launch(Dispatchers.IO) {
        runWithCatch {

            val clazz = FileDirMap::class.java
            val dirMapField = clazz.declaredFields.getOrNull(0) ?: return@launch
            dirMapField.isAccessible = true
            val dirMap = dirMapField.get(null) as HashMap<Int, String>

            val imageFilesDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            dirMap[SelectMimeType.TYPE_IMAGE] = if (imageFilesDir != null && imageFilesDir.exists()) {
                imageFilesDir.path
            } else {
                context.cacheDir.path
            }

            val videoFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            dirMap[SelectMimeType.TYPE_VIDEO] = if (videoFilesDir != null && videoFilesDir.exists()) {
                videoFilesDir.path
            } else {
                context.cacheDir.path
            }

            val musicFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            dirMap[SelectMimeType.TYPE_AUDIO] = if (musicFilesDir != null && musicFilesDir.exists()) {
                musicFilesDir.path
            } else {
                context.cacheDir.path
            }
        }
    }
}