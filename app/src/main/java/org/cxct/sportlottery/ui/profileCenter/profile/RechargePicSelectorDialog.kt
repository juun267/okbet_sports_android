package org.cxct.sportlottery.ui.profileCenter.profile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.dialog_avatar_selector.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LanguageManager

class RechargePicSelectorDialog(
    val activity: Activity,
    private val mSelectListener: OnResultCallbackListener<LocalMedia>,
    val cropType: CropType
) : BottomSheetDialogFragment() {

    enum class CropType(val code: MutableList<Int>) {
        SQUARE(mutableListOf<Int>(1, 1)),
        RECTANGLE(mutableListOf<Int>(16, 9))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_avatar_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    private fun initView() {
        tv_title.text = String.format(resources.getString(R.string.title_upload_pic))
    }

    private fun initEvent() {
        btn_choose_photo.setOnClickListener {
            pickPhoto() //進入相簿流程
            dismiss()
        }

        btn_take_photo.setOnClickListener {
            openCamera() //進入照相流程
            dismiss()
        }
    }

    //選擇相片
    private fun pickPhoto() {
        PictureSelector.create(activity)
            .openGallery(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .setLanguage(getLanguage()) // 设置语言，默认中文
            .isCamera(false) // 是否显示拍照按钮 true or false
            .selectionMode(PictureConfig.SINGLE) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
            .isEnableCrop(false) // 是否裁剪 true or false
            .withAspectRatio(cropType.code[0],cropType.code[1])
            .isCompress(true) // 是否压缩 true or false
            .rotateEnabled(false) // 裁剪是否可旋转图片 true or false
            .circleDimmedLayer(false) // 是否圆形裁剪 true or false
            .showCropFrame(false) // 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
            .showCropGrid(false) // 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
            .minimumCompressSize(100) // 小于100kb的图片不压缩
            .forResult(mSelectListener)
    }

    //拍照
    private fun openCamera() {
        PictureSelector.create(activity)
            .openCamera(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .setLanguage(getLanguage()) // 设置语言，默认中文
            .isEnableCrop(false) // 是否裁剪 true or false
            .isCompress(true) // 是否压缩 true or false
            .withAspectRatio(cropType.code[0],cropType.code[1])
            .rotateEnabled(false) // 裁剪是否可旋转图片 true or false
            .circleDimmedLayer(false) // 是否圆形裁剪 true or false
            .showCropFrame(false) // 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
            .showCropGrid(false) // 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
            .minimumCompressSize(100) // 小于100kb的图片不压缩
            .forResult(mSelectListener)
    }

    private fun getLanguage(): Int {
        return when (LanguageManager.getSelectLanguage(activity)) {
            LanguageManager.Language.ZH -> LanguageConfig.CHINESE
            LanguageManager.Language.ZHT -> LanguageConfig.TRADITIONAL_CHINESE
            LanguageManager.Language.EN -> LanguageConfig.ENGLISH
            LanguageManager.Language.VI -> LanguageConfig.VIETNAM
            LanguageManager.Language.TH -> LanguageConfig.ENGLISH // 套件無支援
        }
    }

    fun setTitle(titleName: String?) {
        tv_title.text = titleName
    }

}