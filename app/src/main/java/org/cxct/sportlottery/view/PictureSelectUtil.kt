package org.cxct.sportlottery.view

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import com.budiyev.android.codescanner.BarcodeUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.listener.OnResultCallbackListener
import org.cxct.sportlottery.ui.profileCenter.profile.GlideEngine
import org.cxct.sportlottery.util.LanguageUtil


/**
 * 图片选择的工具类
 */
object PictureSelectUtil {

    fun <T> pictureSelect(
        context: Activity,
        listener: OnResultCallbackListener<T>,
        isCamera: Boolean = false,
        selectionMode: Int = PictureConfig.SINGLE
    ) {
        PictureSelector.create(context).openGallery(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .setLanguage(LanguageUtil.getLanguage()) // 设置语言，默认中文
            .isCamera(isCamera) // 是否显示拍照按钮 true or false
            .selectionMode(selectionMode) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
            .isEnableCrop(false) // 是否裁剪 true or false
            .isCompress(true) // 是否压缩 true or false
            .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
            .circleDimmedLayer(false) // 是否圆形裁剪 true or false
            .showCropFrame(false) // 是否显示裁剪矩形边框 圆形裁剪时
            // 建议设为false   true or false
            .showCropGrid(false) // 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
            .withAspectRatio(1, 1) // int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .minimumCompressSize(100) // 小于100kb的图片不压缩
            .forResult(listener)
    }

}