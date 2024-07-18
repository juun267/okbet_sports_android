package org.cxct.sportlottery.ui.profileCenter.identity

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentReverifyRdentityKycBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.PicSelectorDialog
import org.cxct.sportlottery.util.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class ReverifyKYCFragment: BaseFragment<ProfileCenterViewModel, FragmentReverifyRdentityKycBinding>() {


    private val loadingHolder by lazy { Gloading.wrapView(binding.llContent) }

    private var updatedSelfPictureURL: String? = null
    private var updatedProofPictureURL: String? = null
    private var updatedIDBackPictureURL: String? = null
    override fun onInitView(view: View) = binding.run {
        tvSubmit.setOnClickListener { submit() }
        val selectPic: View.OnClickListener = View.OnClickListener { v -> selectPictrue(v) }
        llSelf.setOnClickListener(selectPic)
        llProof.setOnClickListener(selectPic)
        llIdBack.setOnClickListener(selectPic)
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
            val verifyConfig = it.getData()
            if (it.succeeded() && verifyConfig != null) {
                setSelfEnable(verifyConfig.selfiePictureRequired())
                setProofEnable(verifyConfig.wealthProofRequired())
                setIdBackEnable(verifyConfig.idbackRequired())
                loadingHolder.showLoadSuccess()
            } else {
                loadingHolder.showLoadFailed()
            }
        }

        imgUpdated.observe(viewLifecycleOwner) {
            hideLoading()

            val imgData = it.second?.path
            if (imgData.isEmptyStr()) {
                ToastUtil.showToast(context(), R.string.upload_fail)
                return@observe
            }

            if (it.first == binding.llSelf.tag) {
                updatedSelfPictureURL = imgData
                checkSubmitEnable()
                binding.ivSelf.load(it.first)
                return@observe
            }

            if (it.first == binding.llProof.tag) {
                updatedProofPictureURL = imgData
                checkSubmitEnable()
                binding.ivProof.load(it.first)
                return@observe
            }

            if (it.first == binding.llIdBack.tag) {
                updatedIDBackPictureURL = imgData
                checkSubmitEnable()
                binding.ivIdBack.load(it.first)
                return@observe
            }

        }

        uploadReview.observe(viewLifecycleOwner) {
            hideLoading()
            if (it.succeeded()) {
                viewModel.userInfo?.value?.verified = VerifiedType.REVERIFYING.value
                findNavController().navigate(R.id.action_reverifyKYCFragment_to_verifyStatusFragment)
            } else {
                ToastUtil.showToast(context, it.msg)
            }
        }

    }

    private fun checkSubmitEnable() {
        val proofEnable = binding.llProof.isGone || !updatedProofPictureURL.isEmptyStr()
        val selfEnable = binding.llSelf.isGone || !updatedSelfPictureURL.isEmptyStr()
        val idBackEnable = binding.llIdBack.isGone || !updatedIDBackPictureURL.isEmptyStr()
        binding.tvSubmit.setBtnEnable(proofEnable && selfEnable && idBackEnable)
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

    private fun setIdBackEnable(enable: Boolean) = binding.run {
        tvIdBack.isVisible = enable
        vIdBack.isVisible = enable
        llIdBack.isVisible = enable
    }

    private fun submit() {
        loading()
        viewModel.updateReverifyInfo(updatedSelfPictureURL, updatedProofPictureURL, updatedIDBackPictureURL)
    }

    private fun uploadImage(file: File) {
        loading()
        viewModel.uploadImage(file)
    }

    private fun selectPictrue(view: View) {
        val dialog = PicSelectorDialog()
        dialog.mSelectListener = getSelcetPictrueCallback(view)
        dialog.show(childFragmentManager, VerifyKYCFragment::class.java.simpleName)
    }

    private fun getSelcetPictrueCallback(view: View): OnResultCallbackListener<LocalMedia> {
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