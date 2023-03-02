package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.fragment_credentials.*
import kotlinx.android.synthetic.main.view_upload.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.component.UploadImageView
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.PicSelectorDialog
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.getCompressFile
import org.cxct.sportlottery.util.setTitleLetterSpacing
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class CredentialsFragment : BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {
    private var docFile: File? = null
    private var photoFile: File? = null

    private val mSelectDocMediaListener = object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: MutableList<LocalMedia>?) {
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
        override fun onResult(result: MutableList<LocalMedia>?) {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_credentials, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        viewModel.docUrlResult.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                if (!result.success) {
                    hideLoading()
                    showErrorPromptDialog(LocalUtils.getString(R.string.prompt), result.msg) {}
                }
            }
        })

        viewModel.photoUrlResult.observe(viewLifecycleOwner, {
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
        })

        viewModel.uploadVerifyPhotoResult.observe(viewLifecycleOwner, { event ->
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
        })
    }

    private fun setupButton() {
        btn_submit.setOnClickListener {
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
        btn_submit.setTitleLetterSpacing()

        btn_reset.setOnClickListener {
            clearMediaFile()
            view_identity_doc.imgUploaded(false)
            view_identity_photo.imgUploaded(false)
            checkSubmitStatus()
        }

        btn_reset.setTitleLetterSpacing()
    }

    private fun setupUploadView() {
        activity?.let { activityNotNull ->
            view_identity_doc.apply {
                imgUploaded(false)
                tv_upload_title.text = LocalUtils.getString(R.string.upload_title)
                tv_upload_tips.visibility = View.GONE
                tv_upload.text = LocalUtils.getString(R.string.upload_content)
                uploadListener = UploadImageView.UploadListener {
                    PicSelectorDialog(
                        activityNotNull,
                        mSelectDocMediaListener,
                        PicSelectorDialog.CropType.RECTANGLE
                    ).show(parentFragmentManager, CredentialsFragment::class.java.simpleName)
                }
            }

            view_identity_photo.apply {
                imgUploaded(false)
                tv_upload_title.text = LocalUtils.getString(R.string.upload_photo_title)
                tv_upload_tips.visibility = View.GONE
                tv_upload.text = LocalUtils.getString(R.string.upload_photo_content)
                uploadListener = UploadImageView.UploadListener {
                    PicSelectorDialog(
                        activityNotNull,
                        mSelectPhotoMediaListener,
                        PicSelectorDialog.CropType.RECTANGLE
                    ).show(parentFragmentManager, CredentialsFragment::class.java.simpleName)
                }
            }
        }
    }

    private fun setupDocFile() {
        docFile?.let { file ->
            view_identity_doc.apply {
                imgUploaded(true)
                Glide.with(iv_selected_media.context).load(file.absolutePath).apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(this.iv_selected_media)
            }
        }
    }

    private fun setupPhotoFile() {
        photoFile?.let { file ->
            view_identity_photo.apply {
                imgUploaded(true)
                Glide.with(iv_selected_media.context).load(file.absolutePath).apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(this.iv_selected_media)
            }
        }
    }

    private fun checkSubmitStatus() {
        btn_submit.isEnabled = docFile != null && photoFile != null
    }

    private fun clearMediaFile() {
        docFile = null
        photoFile = null
    }

    private fun UploadImageView.imgUploaded(uploaded: Boolean) {
        when (uploaded) {
            true -> {
                bg_upload.visibility = View.INVISIBLE
                iv_upload.visibility = View.INVISIBLE
                tv_upload.visibility = View.INVISIBLE

                iv_selected_media.visibility = View.VISIBLE
            }
            false -> {
                bg_upload.visibility = View.VISIBLE
                iv_upload.visibility = View.VISIBLE
                tv_upload.visibility = View.VISIBLE

                iv_selected_media.visibility = View.INVISIBLE
            }
        }
    }
}