package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.content_security_code_style_edittext.view.*
import kotlinx.android.synthetic.main.content_verify_identity_kyc.view.*
import kotlinx.android.synthetic.main.fragment_verify_identity_kyc.*
import kotlinx.android.synthetic.main.view_bottom_navigation.view.*
import kotlinx.android.synthetic.main.view_upload.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.PicSelectorDialog
import org.cxct.sportlottery.util.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class VerifyKYCFragment : BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {
    private var firstFile: File? = null
    private var secondFile: File? = null
    private var dataList = mutableListOf<StatusSheetData>()
    private val mNavController by lazy {
        findNavController()
    }

    private val mfirstSelectDocMediaListener = object : OnResultCallbackListener<LocalMedia> {
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

    private val mSecondSelectPhotoMediaListener = object : OnResultCallbackListener<LocalMedia> {
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
        firstFile = file
        setupDocFile()
        checkSubmitStatus()
    }

    private fun selectedPhotoImg(file: File) {
        secondFile = file
        setupPhotoFile()
        checkSubmitStatus()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_identity_kyc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as VerifyIdentityActivity).setToolBar(getString(R.string.identity))
        initObserve()
        setupButton()
        setupUploadView()
        initView()
        setEdittext()
    }

    private fun initView() {
        identity_2nd.isVisible = sConfigData?.idUploadNumber.equals("2")
    }

    override fun onStart() {
        super.onStart()
        getIdentityType()
        setupDocFile()
        setupPhotoFile()
        checkSubmitStatus()
        getIdentityType()
    }

    private fun initObserve() {
        viewModel.docUrlResult.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                if (!result.success) {
                    hideLoading()
                    showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
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
                        title = getString(R.string.prompt),
                        message = getString(R.string.upload_success),
                        success = true
                    ) {
                        mNavController.navigate(R.id.action_verifyKYCFragment_to_verifyStatusFragment)
                    }
                } else {
                    showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                }
            }
        })
    }

    private fun setupButton() {
        btn_submit.setOnClickListener {
            when {
                firstFile == null -> {
                    showErrorPromptDialog(getString(R.string.prompt), getString(R.string.upload_fail)) {}
                }
                secondFile == null && identity_2nd.isVisible -> {
                    showErrorPromptDialog(getString(R.string.prompt), getString(R.string.upload_fail)) {}
                }
                else -> {
                    loading()
                    if(identity_2nd.isVisible)
                        viewModel.uploadVerifyPhoto(firstFile!!,identity_1st.selector_type.selectedCode?.toInt(),identity_1st.ed_num.text.toString(), secondFile!!, identity_2nd.selector_type.selectedCode?.toInt(), identity_2nd.ed_num.text.toString())
                    else
                        viewModel.uploadVerifyPhoto(firstFile!!,identity_1st.selector_type.selectedCode?.toInt(),identity_1st.ed_num.text.toString())
                }
            }
        }

        btn_submit.setTitleLetterSpacing()

        btn_service.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    activity?.supportFragmentManager?.let { it1 ->
                        ServiceDialog().show(
                            it1,
                            null
                        )
                    }
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl2) }
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl) }
                }
            }
        }
    }

    private fun setupUploadView() {
        activity?.let { activityNotNull ->
            identity_1st.apply {
                this.cl_pic.setOnClickListener {
                    PicSelectorDialog(
                        activityNotNull,
                        mfirstSelectDocMediaListener,
                        PicSelectorDialog.CropType.RECTANGLE
                    ).show(parentFragmentManager, VerifyKYCFragment::class.java.simpleName)
                }
            }

            identity_2nd.apply {
                this.cl_pic.setOnClickListener {
                    PicSelectorDialog(
                        activityNotNull,
                        mSecondSelectPhotoMediaListener,
                        PicSelectorDialog.CropType.RECTANGLE
                    ).show(parentFragmentManager, VerifyKYCFragment::class.java.simpleName)
                }
            }
        }
    }

    private fun setupDocFile() {
        firstFile?.let { file ->
            identity_1st.apply {
                this.btn_add_pic.isVisible = false
                Glide.with( this.img_pic.context).load(file.absolutePath).apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(this.img_pic)
                cl_pic.isSelected = true
                img_tri.isVisible = true
                pic_frame.isVisible = false
            }
        }
    }

    private fun setupPhotoFile() {
        secondFile?.let { file ->
            identity_2nd.apply {
                this.btn_add_pic.isVisible = false
                Glide.with( this.img_pic.context).load(file.absolutePath).apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(this.img_pic)
                cl_pic.isSelected = true
                img_tri.isVisible = true
                pic_frame.isVisible = false
            }
        }
    }

    private fun checkSubmitStatus() {
        btn_submit.isEnabled = (firstFile != null && identity_1st.ed_num.text.isNotEmpty()) && ((secondFile != null && identity_2nd.isVisible && identity_2nd.ed_num.text.isNotEmpty()) || !identity_2nd.isVisible)
    }

    private fun setEdittext() {
        identity_1st.ed_num.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                checkSubmitStatus()
            }
        })
        identity_2nd.ed_num.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                checkSubmitStatus()
            }
        })
    }

    private fun getIdentityType(){
        //根據config配置薪資來源選項
        val identityTypeList = mutableListOf<StatusSheetData>()
        sConfigData?.identityTypeList?.map { identityType ->
            identityTypeList.add(StatusSheetData(identityType.id.toString(), identityType.name))
        }
        dataList = identityTypeList
        identity_1st.selector_type.setItemData(dataList)
        identity_2nd.selector_type.setItemData(dataList)
    }

}