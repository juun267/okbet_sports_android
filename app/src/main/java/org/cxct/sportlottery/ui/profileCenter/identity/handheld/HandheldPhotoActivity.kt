package org.cxct.sportlottery.ui.profileCenter.identity.handheld

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.KYCEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityHandheldPhotoBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.profile.PicSelectorDialog
import org.cxct.sportlottery.util.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class HandheldPhotoActivity: BaseActivity<ProfileCenterViewModel, ActivityHandheldPhotoBinding>() {

    override fun pageName(): String {
       return "上传手持照片"
    }
    private var updatedIDPictureURL: String? = null
    override fun onInitView() = binding.run {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        customToolBar.setOnBackPressListener {
            finish()
        }
        tvSubmit.setOnClickListener { submit() }
        llPhoto.setOnClickListener{
            selectPictrue(it)
        }
        binding.tvSubmit.setBtnEnable(false)
        initObserver()
    }

    fun initObserver(){
        viewModel.imgUpdated.observe(this) {
            hideLoading()

            val imgData = it.second?.path
            if (imgData.isEmptyStr()) {
                ToastUtil.showToast(this, R.string.upload_fail)
                return@observe
            }

            if (it.first == binding.llPhoto.tag) {
                updatedIDPictureURL = imgData
                checkSubmitEnable()
                binding.ivIdHandheld.load(it.first)
                return@observe
            }

        }

        viewModel.uploadReview.observe(this) {
            hideLoading()
            if (it.succeeded()) {
                EventBusUtil.post(KYCEvent())
                UserInfoRepository.loadUserInfo()
                startActivity(Intent(this,VerifyIdentityActivity::class.java).apply {
                    putExtra("backToMainPage",true)
                })
                finishWithOK()
            } else {
                ToastUtil.showToast(this, it.msg)
            }
        }

    }

    private fun checkSubmitEnable() {
        val idEnable = binding.llPhoto.isGone || !updatedIDPictureURL.isEmptyStr()
        binding.tvSubmit.setBtnEnable(idEnable)
    }

    private fun submit() {
        loading()
        viewModel.updateReverifyInfo(updatedIDPictureURL, null, null)
    }

    private fun uploadImage(file: File) {
        loading()
        viewModel.uploadImage(file)
    }

    private fun selectPictrue(view: View) {
        val dialog = PicSelectorDialog()
        dialog.mSelectListener = getSelcetPictrueCallback(view)
        dialog.show(supportFragmentManager, HandheldPhotoActivity::class.java.simpleName)
    }

    private fun getSelcetPictrueCallback(view: View): OnResultCallbackListener<LocalMedia> {
        return object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>?) {
                try {
                    // 图片选择结果回调
                    // LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                    val media = result?.firstOrNull() //這裡應當只會有一張圖片
                    val path = when {
                        media?.isCompressed == true -> media.compressPath
                        media?.isCut == true -> media.cutPath
                        else -> media?.path
                    }

                    val compressFile = getCompressFile(path)
                    if (compressFile?.exists() == true) {
                        view.tag = compressFile
                        uploadImage(compressFile)
                    } else {
                        throw FileNotFoundException()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastUtil.showToast(this@HandheldPhotoActivity, getString(R.string.error_reading_file))
                }
            }

            override fun onCancel() {
                Timber.i("PictureSelector Cancel")
            }

        }
    }
}