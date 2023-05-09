package org.cxct.sportlottery.ui2.login.signUp

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.fragment_register_credentials.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import kotlinx.android.synthetic.main.view_upload.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.profile.PicSelectorDialog
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.getCompressFile
import org.cxct.sportlottery.util.setTitleLetterSpacing
import org.cxct.sportlottery.view.UploadImageView
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class RegisterCredentialsFragment : BaseSocketFragment<RegisterViewModel>(RegisterViewModel::class) {
    companion object {

        fun newInstance(registerCredentialsListener: RegisterCredentialsListener): RegisterCredentialsFragment {
            return RegisterCredentialsFragment().apply {
                this.registerCredentialsListener = registerCredentialsListener
            }
        }
    }

    class RegisterCredentialsListener(private val onCloseFragment: () -> Unit) {
        fun onCloseFragment() = onCloseFragment.invoke()
    }

    private var registerCredentialsListener: RegisterCredentialsListener? = null

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
                ToastUtil.showToastInCenter(activity, getString(R.string.error_reading_file))
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
                ToastUtil.showToastInCenter(activity, getString(R.string.error_reading_file))
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
        return inflater.inflate(R.layout.fragment_register_credentials, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initObserve()
        setupButton()
        setupUploadView()
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.verification_status)
        btn_toolbar_back.setOnClickListener {
            activity?.onBackPressed()
        }
    }


    override fun onStart() {
        super.onStart()
        setupDocFile()
        setupPhotoFile()
        checkSubmitStatus()
    }

    private fun initObserve() {
        viewModel.docUrlResult.observe(viewLifecycleOwner) {
            it?.let { result ->
                if (!result.success) {
                    hideLoading()
                    showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                }
            }
        }

        viewModel.photoUrlResult.observe(viewLifecycleOwner) {
            it?.let { result ->
                hideLoading()
                if (!result.success)
                    showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                else {
                    showPromptDialog(
                        title = LocalUtils.getString(R.string.prompt),
                        message = LocalUtils.getString(R.string.upload_success),
                        success = true
                    ) {
                        registerCredentialsListener?.onCloseFragment()
                    }
                }
            }
        }
    }

    private fun setupButton() {
        btn_submit.setOnClickListener {
            when {
                docFile == null -> {
                    showErrorPromptDialog(getString(R.string.prompt), getString(R.string.upload_fail)) {}
                }
                photoFile == null -> {
                    showErrorPromptDialog(getString(R.string.prompt), getString(R.string.upload_fail)) {}
                }
                else -> {
                    loading()
                    viewModel.uploadVerifyPhoto(docFile!!, photoFile!!)
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
                tv_upload_title.text = getString(R.string.register_identity_photo_front)
                tv_upload_title.setTypeface(tv_upload_title.typeface, Typeface.BOLD)
                tv_upload_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13F)
                iv_upload.setImageResource(R.drawable.ic_upload_front)
                tv_upload_tips.visibility = View.GONE
                tv_upload.text = getString(R.string.register_identity_photo_hint_front)
                uploadListener = UploadImageView.UploadListener {
                    val dialog = PicSelectorDialog()
                    dialog.mSelectListener = mSelectDocMediaListener
                    dialog.show(parentFragmentManager, RegisterCredentialsFragment::class.java.simpleName)
                }
            }

            view_identity_photo.apply {
                imgUploaded(false)
                tv_upload_title.text = getString(R.string.register_identity_photo_back)
                tv_upload_title.setTypeface(tv_upload_title.typeface, Typeface.BOLD)
                tv_upload_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13F)
                iv_upload.setImageResource(R.drawable.ic_upload_back)
                tv_upload_tips.visibility = View.GONE
                tv_upload.text = getString(R.string.register_identity_photo_hint_back)
                uploadListener = UploadImageView.UploadListener {
                    val dialog = PicSelectorDialog()
                    dialog.mSelectListener = mSelectDocMediaListener
                    dialog.show(parentFragmentManager, RegisterCredentialsFragment::class.java.simpleName)
                }
            }
        }
    }

    private fun setupDocFile() {
        docFile?.let { file ->
            view_identity_doc.apply {
                imgUploaded(true)
                Glide.with(iv_selected_media.context).load(file.absolutePath)
                    .apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(this.iv_selected_media)
            }
        }
    }

    private fun setupPhotoFile() {
        photoFile?.let { file ->
            view_identity_photo.apply {
                imgUploaded(true)
                Glide.with(iv_selected_media.context).load(file.absolutePath)
                    .apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(this.iv_selected_media)
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