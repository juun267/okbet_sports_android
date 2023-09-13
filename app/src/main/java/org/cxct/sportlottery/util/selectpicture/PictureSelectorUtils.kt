package org.cxct.sportlottery.util.selectpicture

import android.app.Activity
import android.content.Context
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.language.LanguageConfig

import org.cxct.sportlottery.ui.profileCenter.profile.GlideEngine
import org.cxct.sportlottery.util.LanguageManager


object PictureSelectorUtils {


    fun selectPiture(
        activity: Activity,
        nums: Int = 1,
        ratio_x: Int = 1, // 裁剪比例 X
        ratio_y: Int = 1, //  裁剪比例 Y
        selectMediaListener: OnResultCallbackListener<LocalMedia>,
    ) {
        PictureSelector.create(activity)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setLanguage(getLanguage(activity)) // 设置语言，默认中文
            .isGif(true)
            .isDisplayCamera(false)
//            .isCamera(false) // 是否显示拍照按钮 true or false
            .setSelectionMode(if(nums>1) SelectModeConfig.MULTIPLE else SelectModeConfig.SINGLE)
//            .setMinSelectNum(nums)
//            .selectionMode(if (nums > 1)  SelectMimeType.MULTIPLE else PictureConfig.SINGLE) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
            .setCropEngine(ImageFileCropEngine(ratio_x = ratio_x, ratio_y = ratio_y))
//            .isEnableCrop(false) // 是否裁剪 true or false
            .setCompressEngine(ImageCompressEngine())
//            .isCompress(true) // 是否压缩 true or false
            .isCameraRotateImage(false)
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