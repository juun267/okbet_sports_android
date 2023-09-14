package org.cxct.sportlottery.view

import android.app.Activity
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import org.cxct.sportlottery.ui.profileCenter.profile.GlideEngine
import org.cxct.sportlottery.util.LanguageUtil
import org.cxct.sportlottery.util.selectpicture.ImageCompressEngine
import org.cxct.sportlottery.util.selectpicture.PictureSelectorUtils

/**
 * 图片选择的工具类
 */
object PictureSelectUtil {

    fun pictureSelect(
        context: Activity,
        listener: OnResultCallbackListener<LocalMedia>,
        isCamera: Boolean = false,
        selectionMode: Int = SelectModeConfig.SINGLE
    ) {
        PictureSelector.create(context).openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setLanguage(LanguageUtil.getLanguage()) // 设置语言，默认中文
            .isDisplayCamera(isCamera) // 是否显示拍照按钮 true or false
            .setSelectionMode(selectionMode) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
            .setCompressEngine(ImageCompressEngine(100))
            // 建议设为false   true or false
            .forResult(listener)
    }

}