package org.cxct.sportlottery.ui.profileCenter.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.language.LanguageConfig
import kotlinx.android.synthetic.main.dialog_selector_dialog.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialogFragment
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.selectpicture.ImageCompressEngine
import org.cxct.sportlottery.util.selectpicture.ImageFileCropEngine

class PicSelectorDialog : BaseDialogFragment() {

    var mSelectListener: OnResultCallbackListener<LocalMedia>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_selector_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    private fun initView() {
        tv_title.text = String.format(getString(R.string.prompt))
        tv_message.text = String.format(getString(R.string.upload_dialog_content))
        btn_negative.text = String.format(getString(R.string.upload_dialog_camera))
        btn_positive.text = String.format(getString(R.string.upload_dialog_gallery))
    }

    private fun initEvent() {
        btn_positive.setOnClickListener {
            pickPhoto() //進入相簿流程
            dismiss()
        }

        btn_negative.setOnClickListener {
            openCamera() //進入照相流程
            dismiss()
        }
    }

    //選擇相片
    private fun pickPhoto() {
        if (activity == null || mSelectListener == null) {
            dismiss()
            return
        }
        PictureSelector.create(activity)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setLanguage(getLanguage()) // 设置语言，默认中文
            .isDisplayCamera(false) // 是否显示拍照按钮 true or false
            .setSelectionMode(SelectModeConfig.SINGLE) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
//            .setCropEngine(ImageFileCropEngine(rotateEnabled = true, showCropFrame = true, ratio_x = 16, ratio_y = 9))
            .setCompressEngine(ImageCompressEngine(200))
            .forResult(mSelectListener)
    }

    //拍照
    private fun openCamera() {
        if (activity == null) {
            return
        }
        PictureSelector.create(activity)
            .openCamera(SelectMimeType.ofImage())
            .setLanguage(getLanguage()) // 设置语言，默认中文
            .setCompressEngine(ImageCompressEngine(200))
//            .setCropEngine(ImageFileCropEngine(rotateEnabled = true, showCropFrame = true, ratio_x = 16, ratio_y = 9))
            .forResult(mSelectListener)
    }

    private fun getLanguage(): Int {
        return when (LanguageManager.getSelectLanguage(activity)) {
            LanguageManager.Language.ZH -> LanguageConfig.CHINESE
            LanguageManager.Language.ZHT -> LanguageConfig.TRADITIONAL_CHINESE
            LanguageManager.Language.EN -> LanguageConfig.ENGLISH
            LanguageManager.Language.VI -> LanguageConfig.VIETNAM
            LanguageManager.Language.TH -> LanguageConfig.ENGLISH // 套件無支援
            else -> LanguageConfig.ENGLISH
        }
    }

    fun setTitle(titleName: String?) {
        tv_title.text = titleName
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (mSelectListener != null) {
            super.show(manager, tag)
        }
    }

}