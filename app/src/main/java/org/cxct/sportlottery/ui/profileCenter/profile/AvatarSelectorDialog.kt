package org.cxct.sportlottery.ui.profileCenter.profile

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.luck.picture.lib.permissions.PermissionChecker
import com.luck.picture.lib.tools.PictureFileUtils
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LanguageManager

class AvatarSelectorDialog(
    val activity: Activity,
    private val mSelectListener: OnResultCallbackListener<LocalMedia>
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_avatar_selector, null)
        dialog.setContentView(view)

        clearCache()
        initEvent(view)
        return dialog
    }

    private fun initEvent(rootView: View?) {
        rootView?.apply {
            findViewById<Button>(R.id.btn_choose_photo)?.setOnClickListener {
                pickPhoto() //進入相簿流程
                dismiss()
            }

            findViewById<Button>(R.id.btn_take_photo)?.setOnClickListener {
                openCamera() //進入照相流程
                dismiss()
            }
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
            .isEnableCrop(true) // 是否裁剪 true or false
            .isCompress(true) // 是否压缩 true or false
            .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
            .circleDimmedLayer(true) // 是否圆形裁剪 true or false
            .showCropFrame(false) // 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
            .showCropGrid(false) // 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
            .withAspectRatio(1, 1) // int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .minimumCompressSize(100) // 小于100kb的图片不压缩
            .forResult(mSelectListener)
    }

    //拍照
    private fun openCamera() {
        PictureSelector.create(activity)
            .openCamera(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .setLanguage(getLanguage()) // 设置语言，默认中文
            .isEnableCrop(true) // 是否裁剪 true or false
            .isCompress(true) // 是否压缩 true or false
            .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
            .circleDimmedLayer(true) // 是否圆形裁剪 true or false
            .showCropFrame(false) // 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
            .showCropGrid(false) // 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
            .withAspectRatio(1, 1) // int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .minimumCompressSize(100) // 小于100kb的图片不压缩
            .forResult(mSelectListener)
    }

    private fun getLanguage(): Int {
        return when (LanguageManager.getSelectLanguage(activity)) {
            LanguageManager.Language.ZH -> LanguageConfig.CHINESE
            LanguageManager.Language.ZHT -> LanguageConfig.TRADITIONAL_CHINESE
            LanguageManager.Language.EN -> LanguageConfig.ENGLISH
            LanguageManager.Language.VI -> LanguageConfig.VIETNAM
        }
    }

    /**
     * 清空缓存包括裁剪、压缩、AndroidQToPath所生成的文件，注意调用时机必须是处理完本身的业务逻辑后调用；非强制性
     */
    private fun clearCache() {
        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PictureFileUtils.deleteCacheDirFile(context, PictureMimeType.ofImage())
        } else {
            PermissionChecker.requestPermissions(
                activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE) { // 存储权限
            for (grantResult in grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    PictureFileUtils.deleteCacheDirFile(context, PictureMimeType.ofImage())
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}