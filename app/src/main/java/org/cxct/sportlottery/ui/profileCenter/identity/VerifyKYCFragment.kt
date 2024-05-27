package org.cxct.sportlottery.ui.profileCenter.identity

import android.graphics.Paint
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bigkoo.pickerview.view.TimePickerView
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.FragmentVerifyIdentityKycBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.login.signUp.info.DateTimePickerOptions
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.PicSelectorDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.afterTextChanged
import org.cxct.sportlottery.view.boundsEditText.EditTextWatcher
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText
import org.cxct.sportlottery.view.boundsEditText.TextFormFieldBoxes
import org.cxct.sportlottery.view.dialog.VerificationTipDialog
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class VerifyKYCFragment : BaseFragment<ProfileCenterViewModel,FragmentVerifyIdentityKycBinding>() {
    private var firstFile: File? = null
    private var headIdFile: File? = null
    private var secondFile: File? = null
    private var dataList = mutableListOf<StatusSheetData>()
    private val mNavController by lazy {
        findNavController()
    }

    private val mfirstSelectDocMediaListener = object : OnResultCallbackListener<LocalMedia> {
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

    private val mHeadIdListener = object : OnResultCallbackListener<LocalMedia> {
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
                if (compressFile?.exists() == true)
                    selectedHeadPhotoImg(compressFile)
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

    private fun selectedHeadPhotoImg(file: File)=binding.identityHead.run {
        headIdFile = file
        tvUploadHeadPhoto.isVisible = true
        tvUploadHeadPhoto.text = getString(R.string.change_other_ID_photos)
        imgPicWithHeadPhoto.load(file.absolutePath, R.drawable.img_avatar_default)
        imgTriWithHeadPhoto.isVisible = true
        checkSubmitStatus()
    }

    override fun onInitView(view: View) {
        (activity as VerifyIdentityActivity).setToolBar(getString(R.string.identity))
        initObserve()
        setupButton()
        setupUploadView()
        initView()
        setEdittext()
        setSpinnerStyle()
    }

    private fun initView()=binding.run {
        //PM - Tom Wang 跟QA 要求只跟設計稿一樣只開一個認證上傳，OK-860
        //2023年04月06日11:57:52 【OKBET-历史遗留问题-安卓】后台配置KYC认证数量最大是2个，安卓与其他端支持认证数量不一致（ftt103/Aa123456）
        val isShow2nd = sConfigData?.idUploadNumber.equals("2")
        identity2nd.root.isVisible = isShow2nd
        identityHead.apply {
            tvSamplePhotoWithHead.paint.flags = Paint.UNDERLINE_TEXT_FLAG; //下划线
            tvSamplePhotoWithHead.paint.isAntiAlias = true;//抗锯齿
            tvSamplePhotoWithHead.setOnClickListener { VerificationTipDialog(it.context).show() }
        }
        setUserInfo()
    }

    private fun setUserInfo()=binding.run {
        val userInfo = UserInfoRepository.loginedInfo() ?: return

        if (userInfo.hasFullName()) {
            eetFirstName.setText(userInfo.firstName)
            val noneMiddleName = userInfo.noneMiddleName()
            if (noneMiddleName) {
                eedtMiddleName.setText("N/A")
            } else {
                eedtMiddleName.setText(userInfo.middleName)
            }
            cbNoMiddleName.isChecked = noneMiddleName
            eedtMiddleName.isEnabled = !noneMiddleName
            eedtLastName.setText(userInfo.lastName)
            etBirthday.setText(userInfo.birthday)
        }

        eetFirstName.afterTextChanged { checkInput(eetFirstName, etFirstName) }
        eedtMiddleName.afterTextChanged { checkInput(eedtMiddleName, edtMiddleName, !cbNoMiddleName.isChecked) }
        eedtLastName.afterTextChanged { checkInput(eedtLastName, edtLastName) }
        tvBirthday.setOnClickListener { showDateTimePicker() }
        cbNoMiddleName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                eedtMiddleName.isEnabled = false
                eedtMiddleName.setText("N/A")
            } else {
                eedtMiddleName.isEnabled = true
                eedtMiddleName.setText("")
                checkSubmitStatus()
            }
            edtMiddleName.setError(null, true)
        }
    }

    private fun checkInput(editText: ExtendedEditText, textFormFieldBoxes: TextFormFieldBoxes, needCheck: Boolean = true) {

        if (needCheck) {
            val inputString = editText.text.toString()
            if (inputString.isEmpty()) {
                textFormFieldBoxes.setError(getString(R.string.error_input_empty), false)
            } else {
                textFormFieldBoxes.setError(if (VerifyConstUtil.verifyFullName2(inputString)) "" else getString(R.string.N280), false)
            }
        }

        checkSubmitStatus()
    }

    private var dateTimePicker: TimePickerView? = null
    private fun showDateTimePicker() {
        KeyboadrdHideUtil.hideSoftKeyboard(binding.etBirthday)
        if (dateTimePicker != null) {
            dateTimePicker!!.show()
            return
        }
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.YEAR, -100)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.YEAR, -21)
        tomorrow.add(Calendar.DAY_OF_MONTH, -1)
        dateTimePicker = DateTimePickerOptions(requireContext()).getBuilder { date, _ ->
            TimeUtil.dateToStringFormatYMD(date)?.let {
                binding.etBirthday.setText(it)
                checkSubmitStatus()
            }
        }
            .setRangDate(yesterday, tomorrow)
            .setDate(tomorrow)
            .build()
        dateTimePicker!!.show()
    }

    override fun onStart() {
        super.onStart()
        getIdentityType()
        setupDocFile()
        setupPhotoFile()
        checkSubmitStatus()
    }

    private fun setSpinnerStyle()=binding.run {
        identity1st.selectorType.setOnItemSelectedListener { checkSubmitStatus() }
        identity1st.selectorType.binding.tvName.setPadding(0,0,0,0)
        val constraintLayout = identity1st.selectorType.binding.clRoot
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.clear(R.id.tv_name, ConstraintSet.END)
        constraintSet.connect(R.id.tv_name, ConstraintSet.START, R.id.clRoot, ConstraintSet.START, 14)
        constraintSet.clear(R.id.iv_arrow, ConstraintSet.START)
        constraintSet.connect(R.id.iv_arrow, ConstraintSet.END, R.id.clRoot, ConstraintSet.END, 14)
        constraintSet.applyTo(constraintLayout)

        identity2nd.selectorType.setOnItemSelectedListener { checkSubmitStatus() }
        identity2nd.selectorType.binding.tvName.setPadding(0,0,0,0)
        val constraintLayout2 = identity2nd.selectorType.binding.clRoot
        val constraintSet2 = ConstraintSet()
        constraintSet2.clone(constraintLayout2)
        constraintSet2.clear(R.id.tv_name, ConstraintSet.END)
        constraintSet2.connect(R.id.tv_name, ConstraintSet.START, R.id.clRoot, ConstraintSet.START, 14)
        constraintSet2.clear(R.id.iv_arrow, ConstraintSet.START)
        constraintSet2.connect(R.id.iv_arrow, ConstraintSet.END, R.id.clRoot, ConstraintSet.END, 14)
        constraintSet2.applyTo(constraintLayout2)
    }

    private fun initObserve() {
        viewModel.docUrlResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                if (!result.success) {
                    hideLoading()
                    showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
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
        }
    }

    private fun setupButton()=binding.run {
        btnSubmit.setOnClickListener {
            when {
                firstFile == null -> {
                    showErrorPromptDialog(getString(R.string.prompt), getString(R.string.upload_fail)) {}
                }

                secondFile == null && identity2nd.root.isVisible -> {
                    showErrorPromptDialog(getString(R.string.prompt), getString(R.string.upload_fail)) {}
                }

                else -> {
                    loading()
                    if (identity2nd.root.isVisible)
                        viewModel.uploadVerifyPhoto(
                            headIdFile!!,
                            firstFile!!,
                            identity1st.selectorType.selectedCode?.toInt(),
                            identity1st.edNum.text.toString(),
                            secondFile!!,
                            identity2nd.selectorType.selectedCode?.toInt(),
                            identity2nd.edNum.text.toString(),
                            firstName = eetFirstName.text.toString(),
                            middleName = eedtMiddleName.text.toString(),
                            lastName = eedtLastName.text.toString(),
                            birthday = etBirthday.text.toString(),
                        )
                    else
                        viewModel.uploadVerifyPhoto(
                            headIdFile,
                            firstFile!!,
                            identity1st.selectorType.selectedCode?.toInt(),
                            identity1st.edNum.text.toString(),
                            firstName = eetFirstName.text.toString(),
                            middleName = eedtMiddleName.text.toString(),
                            lastName = eedtLastName.text.toString(),
                            birthday = etBirthday.text.toString(),
                        )
                }
            }
        }

        btnSubmit.setTitleLetterSpacing()
        tvService.setServiceClick(childFragmentManager)
    }

    private fun setupUploadView()=binding.run {

        identity1st.apply {
            this.tvUploadTip.isVisible = false
            this.tvUploadTip2.isVisible = false
            this.txvTitleNum.text = getString(R.string.kyc_num)
            this.clPic.setOnClickListener {
                val dialog = PicSelectorDialog()
                dialog.mSelectListener = mfirstSelectDocMediaListener
                dialog.show(parentFragmentManager, VerifyKYCFragment::class.java.simpleName)
            }
        }

        identity2nd.apply {
            this.tvUploadTip.isVisible = false
            this.tvUploadTip2.isVisible = true
            this.txvTitleNum.text = getString(R.string.kyc_num2)
            this.clPic.setOnClickListener {
                val dialog = PicSelectorDialog()
                dialog.mSelectListener = mSecondSelectPhotoMediaListener
                dialog.show(parentFragmentManager, VerifyKYCFragment::class.java.simpleName)
            }
        }
        identityHead.root.gone()
        identityHead.layoutUploadIdWithHead.setOnClickListener {
            val dialog = PicSelectorDialog()
            dialog.mSelectListener = mHeadIdListener
            dialog.show(parentFragmentManager, VerifyKYCFragment::class.java.simpleName)
        }

    }

    private fun setupDocFile() =binding.run{
        val file = firstFile ?: return@run 
        identity1st.apply {
            btnAddPic.isVisible = true
            tvUploadIdPhoto.text = getString(R.string.change_other_ID_photos)
            imgPic.load(file.absolutePath, R.drawable.img_avatar_default)
            clPic.isSelected = true
            imgTri.isVisible = true
        }
    }

    private fun setupPhotoFile()=binding.run {
        val file = secondFile ?: return@run
        identity2nd.apply {
            btnAddPic.isVisible = true
            tvUploadIdPhoto.text = getString(R.string.change_other_ID_photos)
            imgPic.load(file.absolutePath, R.drawable.img_avatar_default)
            clPic.isSelected = true
            imgTri.isVisible = true
        }
    }

    private fun checkSubmitStatus()=binding.run {
        btnSubmit.isEnabled =
            (firstFile != null && !identity1st.selectorType.selectedCode.isNullOrBlank() && identity1st.edNum.text.isNotEmpty())
                    && ((secondFile != null && identity2nd.root.isVisible && !identity2nd.selectorType.selectedCode.isNullOrBlank() && identity2nd.edNum.text.isNotEmpty()) || !identity2nd.root.isVisible)
                    && (identityHead.root.isGone||(identityHead.root.isVisible && headIdFile!=null))
                    && (!etFirstName.isOnError && !edtLastName.isOnError && !edtMiddleName.isOnError && (etBirthday.isGone or etBirthday.text.toString().isNotEmpty()))
    }

    private fun setEdittext()=binding.run {
        val textWatcher = EditTextWatcher { checkSubmitStatus() }
        identity1st.edNum.addTextChangedListener(textWatcher)
        identity2nd.edNum.addTextChangedListener(textWatcher)
    }

    private fun getIdentityType()=binding.run {
        //根據config配置薪資來源選項
        val identityTypeList = mutableListOf<StatusSheetData>()
        sConfigData?.identityTypeList?.map { identityType ->
            identityTypeList.add(StatusSheetData(identityType.id.toString(), identityType.name))
        }
        dataList = identityTypeList
        identity1st.selectorType.setItemData(dataList, isSelectedDefault = false)
        identity1st.selectorType.selectedListener = View.OnClickListener {
            identity1st.icRecharge.setImageResource(R.drawable.ic_recharge_copy_30_2_selected)
        }
        identity2nd.selectorType.setItemData(dataList, isSelectedDefault = false)
        identity2nd.selectorType.selectedListener = View.OnClickListener {
            identity2nd.icRecharge.setImageResource(R.drawable.ic_recharge_copy_30_2_selected)
        }
    }

}