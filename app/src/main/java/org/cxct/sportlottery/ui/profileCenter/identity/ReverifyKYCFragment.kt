package org.cxct.sportlottery.ui.profileCenter.identity

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentReverifyRdentityKycBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.PicSelectorDialog
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class ReverifyKYCFragment: BindingFragment<ProfileCenterViewModel, FragmentReverifyRdentityKycBinding>() {


    private val loadingHolder by lazy { Gloading.wrapView(binding.llContent) }

    private var updatedSelfPictureURL: String? = null
    private var updatedProofPictureURL: String? = null
    override fun onInitView(view: View) = binding.run {
        tvSubmit.setOnClickListener { submit() }
        val selectPic: View.OnClickListener = View.OnClickListener { v -> selectPictrue(v) }
        llSelf.setOnClickListener(selectPic)
        llProof.setOnClickListener(selectPic)
    }

    override fun onBindViewStatus(view: View) {
        (activity as VerifyIdentityActivity).setToolBarTitleForReverify()
        binding.tvService.setServiceClick(childFragmentManager)
        binding.tvSubmit.setBtnEnable(false)
        loadingHolder.withRetry {
            loadingHolder.showLoading()
            viewModel.getVerifyConfig()
        }
        loadingHolder.go()
        initObserver()
    }

    fun initObserver() = viewModel.run {
        verifyConfig.observe(viewLifecycleOwner) {
            if (it.succeeded()) {
                setSelfEnable(it.getData()?.requiredSelfiePicture.toString()?.isStatusOpen())
                setProofEnable(it.getData()?.requiredWealthProof.toString()?.isStatusOpen())
                loadingHolder.showLoadSuccess()
            } else {
                loadingHolder.showLoadFailed()
            }
        }

        imgUpdated.observe(viewLifecycleOwner) {
            hideLoading()

            val imgData = it.second
            if (imgData?.path.isEmptyStr()) {
                ToastUtil.showToast(context(), R.string.upload_fail)
                return@observe
            }

            if (it.first == binding.llSelf.tag) {
                updatedSelfPictureURL = imgData!!.path
                binding.tvSubmit.setBtnEnable(binding.llProof.isGone || !updatedProofPictureURL.isEmptyStr())
                binding.ivSelf.load(it.first)
                return@observe
            }

            if (it.first == binding.llProof.tag) {
                updatedProofPictureURL = imgData!!.path
                binding.tvSubmit.setBtnEnable(binding.llSelf.isGone || !updatedSelfPictureURL.isEmptyStr())
                binding.ivProof.load(it.first)
                return@observe
            }

        }

        uploadReview.observe(viewLifecycleOwner) {
            hideLoading()
            if (it.succeeded()) {
                viewModel.userInfo?.value?.verified = ProfileActivity.VerifiedType.REVERIFYING.value
                findNavController().navigate(R.id.action_reverifyKYCFragment_to_verifyStatusFragment)
            } else {
                ToastUtil.showToast(context, it.msg)
            }
        }

    }

    private fun setSelfEnable(enable: Boolean) = binding.run{
        tvSelf.isVisible = enable
        vSelf.isVisible = enable
        llSelf.isVisible = enable
    }

    private fun setProofEnable(enable: Boolean) = binding.run{
        tvProof.isVisible = enable
        vProof.isVisible = enable
        llProof.isVisible = enable
    }

    fun submit() {
        loading()
        viewModel.updateReverifyInfo(updatedSelfPictureURL, updatedProofPictureURL)
    }

    fun uploadImage(file: File) {
        loading()
        viewModel.uploadImage(file)
    }

    private fun selectPictrue(view: View) {
        val dialog = PicSelectorDialog()
        dialog.mSelectListener = getSelcetPitrueCallback(view)
        dialog.show(childFragmentManager, VerifyKYCFragment::class.java.simpleName)
    }

    private fun getSelcetPitrueCallback(view: View): OnResultCallbackListener<LocalMedia> {
        return object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>?) {
                if (activity == null) {
                    return
                }
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
                    ToastUtil.showToastInCenter(activity, getString(R.string.error_reading_file))
                }
            }

            override fun onCancel() {
                Timber.i("PictureSelector Cancel")
            }

        }
    }
}