package org.cxct.sportlottery.ui.profileCenter.identity


import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentCredentialsBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.PicSelectorDialog
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.getCompressFile
import org.cxct.sportlottery.util.setTitleLetterSpacing
import org.cxct.sportlottery.view.UploadImageView
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class CredentialsFragment : BindingFragment<ProfileCenterViewModel,FragmentCredentialsBinding>() {
    private var docFile: File? = null
    private var photoFile: File? = null

    private val mSelectDocMediaListener = object : OnResultCallbackListener<LocalMedia> {
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
                if (compressFile?.exists() == true)
                    selectedDocImg(compressFile)
                else
                    throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(activity, LocalUtils.getString(R.string.error_reading_file))
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }
    }
    private val mSelectPhotoMediaListener = object : OnResultCallbackListener<LocalMedia> {
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
                if (compressFile?.exists() == true)
                    selectedPhotoImg(compressFile)
                else
                    throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(activity, LocalUtils.getString(R.string.error_reading_file))
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }
    }

    private fun selectedDocImg(file: File) {
        docFile = file
        setupDocFile()
        checkSubmitStatus()
    }

    private fun selectedPhotoImg(file: File) {
        photoFile = file
        setupPhotoFile()
        checkSubmitStatus()
    }

    override fun onInitView(view: View) {
        (activity as VerifyIdentityActivity).setToolBar(LocalUtils.getString(R.string.identity))
        initObserve()
        setupButton()
        setupUploadView()
    }

    override fun onStart() {
        super.onStart()
        setupDocFile()
        setupPhotoFile()
        checkSubmitStatus()
    }

    private fun initObserve() {
        viewModel.docUrlResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                if (!result.success) {
                    hideLoading()
                    showErrorPromptDialog(LocalUtils.getString(R.string.prompt), result.msg) {}
                }
            }
        }

        viewModel.photoUrlResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                hideLoading()
                if (!result.success)
                    showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                else {
//                    val action = CredentialsFragmentDirections.actionCredentialsFragmentToCredentialsDetailFragment(null)
//                    findNavController().navigate(action)
                    /*showPromptDialog(
                        title = getString(R.string.prompt),
                        message = getString(R.string.upload_success),
                        success = true
                    ) {
                        activity?.onBackPressed()
                    }*/

                }
            }
        }

        viewModel.uploadVerifyPhotoResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                hideLoading()
                if (result.success) {
                    showPromptDialog(
                        title = LocalUtils.getString(R.string.prompt),
                        message = LocalUtils.getString(R.string.upload_success),
                        success = true
                    ) {
                        activity?.onBackPressed()

                    }
                } else {
                    showErrorPromptDialog(LocalUtils.getString(R.string.prompt), result.msg) {}
                }
            }
        }
    }

    private fun setupButton()=binding.run {
        btnSubmit.setOnClickListener {
            when {
                docFile == null -> {
                    showErrorPromptDialog(LocalUtils.getString(R.string.prompt), LocalUtils.getString(R.string.upload_fail)) {}
                }
                photoFile == null -> {
                    showErrorPromptDialog(LocalUtils.getString(R.string.prompt), LocalUtils.getString(R.string.upload_fail)) {}
                }
                else -> {
                    loading()
                    //3523需求 API結構調整，舊有邏輯不適用
                    //viewModel.uploadVerifyPhoto(docFile!!, photoFile!!)
                }
            }
        }
        btnSubmit.setTitleLetterSpacing()

        btnReset.setOnClickListener {
            clearMediaFile()
            viewIdentityDoc.imgUploaded(false)
            viewIdentityPhoto.imgUploaded(false)
            checkSubmitStatus()
        }

        btnReset.setTitleLetterSpacing()
    }

    private fun setupUploadView()=binding.run {
        activity?.let { activityNotNull ->
            viewIdentityDoc.apply {
                imgUploaded(false)
                binding.tvUploadTitle.text = LocalUtils.getString(R.string.upload_title)
                binding.tvUploadTips.visibility = View.GONE
                binding.tvUpload.text = LocalUtils.getString(R.string.upload_content)
                uploadListener = UploadImageView.UploadListener {
                    val dialog = PicSelectorDialog()
                    dialog.mSelectListener = mSelectDocMediaListener
                    dialog.show(parentFragmentManager, CredentialsFragment::class.java.simpleName)
                }
            }

            viewIdentityPhoto.apply {
                imgUploaded(false)
                binding.tvUploadTitle.text = LocalUtils.getString(R.string.upload_photo_title)
                binding.tvUploadTips.visibility = View.GONE
                binding.tvUpload.text = LocalUtils.getString(R.string.upload_photo_content)
                uploadListener = UploadImageView.UploadListener {
                    val dialog = PicSelectorDialog()
                    dialog.mSelectListener = mSelectPhotoMediaListener
                    dialog.show(parentFragmentManager, CredentialsFragment::class.java.simpleName)
                }
            }
        }
    }

    private fun setupDocFile() {
        docFile?.let { file ->
            binding.viewIdentityDoc.apply {
                imgUploaded(true)
                Glide.with(requireContext()).load(file.absolutePath).apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(binding.ivSelectedMedia)
            }
        }
    }

    private fun setupPhotoFile() {
        photoFile?.let { file ->
            binding.viewIdentityPhoto.apply {
                imgUploaded(true)
                Glide.with(requireContext()).load(file.absolutePath).apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(binding.ivSelectedMedia)
            }
        }
    }

    private fun checkSubmitStatus() {
        binding.btnSubmit.isEnabled = docFile != null && photoFile != null
    }

    private fun clearMediaFile() {
        docFile = null
        photoFile = null
    }

    private fun UploadImageView.imgUploaded(uploaded: Boolean)=binding.run {
        when (uploaded) {
            true -> {
                bgUpload.visibility = View.INVISIBLE
                ivUpload.visibility = View.INVISIBLE
                tvUpload.visibility = View.INVISIBLE

                ivSelectedMedia.visibility = View.VISIBLE
            }
            false -> {
                bgUpload.visibility = View.VISIBLE
                ivUpload.visibility = View.VISIBLE
                tvUpload.visibility = View.VISIBLE

                ivSelectedMedia.visibility = View.INVISIBLE
            }
        }
    }
}