package org.cxct.sportlottery.util

import android.app.Activity
import android.content.Context
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.listener.OnResultCallbackListener
import org.cxct.sportlottery.ui.profileCenter.profile.GlideEngine

object PictureSelectorUtils {


    fun selectPiture(activity: Activity,
                     nums: Int = 1,
                     ratio_x: Int = 1, // 裁剪比例 X
                     ratio_y: Int = 1, //  裁剪比例 Y
                     selectMediaListener: OnResultCallbackListener<LocalMedia>) {
        PictureSelector.create(activity)
            .openGallery(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .setLanguage(getLanguage(activity)) // 设置语言，默认中文
            .isGif(true)
            .isCamera(false) // 是否显示拍照按钮 true or false
            .selectionMode(if (nums > 1)  PictureConfig.MULTIPLE else PictureConfig.SINGLE) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
            .isEnableCrop(false) // 是否裁剪 true or false
            .isCompress(true) // 是否压缩 true or false
            .rotateEnabled(false) // 裁剪是否可旋转图片 true or false
            .circleDimmedLayer(false) // 是否圆形裁剪 true or false
            .showCropFrame(false) // 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
            .showCropGrid(false) // 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
            .withAspectRatio(ratio_x, ratio_y) // int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .minimumCompressSize(100) // 小于100kb的图片不压缩
            .forResult(selectMediaListener)
    }

    private fun getLanguage(context: Context): Int {
        return when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> LanguageConfig.CHINESE
            LanguageManager.Language.EN -> LanguageConfig.ENGLISH
            LanguageManager.Language.VI -> LanguageConfig.VIETNAM
            else -> LanguageConfig.ENGLISH // 套件無支援
        }
    }

}