package org.cxct.sportlottery.ui.profileCenter.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.listener.CustomListener
import com.bigkoo.pickerview.view.TimePickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.include_user_profile.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.FLAG_NICKNAME_IS_SET
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.common.dialog.CustomSecurityDialog
import org.cxct.sportlottery.ui.login.signUp.info.DateTimePickerOptions
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.profileCenter.authbind.AuthActivity
import org.cxct.sportlottery.ui.profileCenter.cancelaccount.CancelAccountActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.profileCenter.modify.ModifyBindInfoActivity
import org.cxct.sportlottery.ui.profileCenter.modify.VerificationWaysActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.EditUserNameActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoActivity.Companion.MODIFY_INFO
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.isStatusOpen
import org.cxct.sportlottery.util.phoneNumCheckDialog
import org.cxct.sportlottery.view.dialog.SourceOfIncomeDialog
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.Calendar

/**
 * @app_destination 个人设置
 */
class ProfileActivity : BaseSocketActivity<ProfileModel>(ProfileModel::class) {

    //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null

    //KYC驗證彈窗
    private var kYCVerifyDialog: CustomSecurityDialog? = null

    enum class VerifiedType(val value: Int) {
        NOT_YET(0), PASSED(1), VERIFYING(2), VERIFIED_FAILED(3), VERIFIED_WAIT(4), REVERIFIED_NEED(5), REVERIFYING(6)
    }

    enum class SecurityCodeEnterType(val value: Int) {
        REALNAME(0), PW(1)
    }

    //生日选择
    private var dateTimePicker: TimePickerView? = null

    private var securityCodeEnter = SecurityCodeEnterType.REALNAME

    private var dialogBtmAdapter = DialogBottomDataAdapter(this)
    private lateinit var rvData: RecyclerView
    private lateinit var btnDialogTitle: TextView
    private lateinit var btnDialogDone: Button

    val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(this) }

    private val mSelectMediaListener = object : OnResultCallbackListener<LocalMedia> {
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

                val file = File(path!!)
                if (file.exists())
                    uploadImg(file)
                else
                    throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(
                    this@ProfileActivity,
                    getString(R.string.error_reading_file)
                )
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(R.layout.activity_profile)

        initView()
        initButton()
        initObserve()
        initDateTimeView()
        setupLogout()
        viewModel.getUserSalaryList()
        initBottomDialog()

    }

    private fun setupLogout() {
        btn_sign_out.setOnClickListener {
            viewModel.doLogoutAPI()
            viewModel.doLogoutCleanUser { MainTabActivity.reStart(this) }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserInfo()
    }

    private fun initView() {
        custom_tool_bar.titleText = getString(R.string.profile_info)
        sConfigData?.apply {
            ll_qq_number.isVisible = enableWithdrawQQ.isStatusOpen()
            ll_e_mail.isVisible = enableWithdrawEmail.isStatusOpen()
            ll_phone_number.isVisible = enableWithdrawPhone.isStatusOpen()
            ll_wechat.isVisible = enableWithdrawWechat.isStatusOpen()
            ll_real_name.isVisible = enableWithdrawFullName.isStatusOpen()
        }
        tv_pass_word.text =
            if (viewModel.userInfo.value?.passwordSet == true) getString(R.string.set) else getString(
                R.string.edit
            )
    }

    private fun initButton() {
        custom_tool_bar.setOnBackPressListener { finish() }
        //設定個人資訊頁面
        setupToInfoSettingPage()
        btn_head.setOnClickListener {
            val dialog = AvatarSelectorDialog()
            dialog.mSelectListener = mSelectMediaListener
            dialog.show(supportFragmentManager, null)
        }
    }

    private fun initBottomDialog() {
        val btmLays = layoutInflater.inflate(R.layout.dialog_bottom_select, null)
        val btnCancel = btmLays.findViewById<Button>(R.id.btnBtmCancel)
        btnDialogDone = btmLays.findViewById(R.id.btnBtmDone)
        btnDialogTitle = btmLays.findViewById(R.id.tvBtmTitle)
        rvData = btmLays.findViewById(R.id.rvBtmData)
        rvData.adapter = dialogBtmAdapter
        btnCancel.setOnClickListener { bottomSheet.dismiss() }

        bottomSheet.setContentView(btmLays)
    }

    private fun showBottomDialog(
        list: MutableList<DialogBottomDataEntity>,
        title: String,
        currStr: String?,
        sourceOtherFlag: Boolean = false,
        callBack: (item: DialogBottomDataEntity) -> Unit
    ) {
        var item: DialogBottomDataEntity? = list.find { it.flag }
        val listNew: MutableList<DialogBottomDataEntity> = mutableListOf()
        var trueFlag = false
        list.forEach {
            val ne = it.copy()
            if (ne.name == currStr) {
                ne.flag = true
                trueFlag = true
            }
            listNew.add(ne)
        }
        if (sourceOtherFlag && !trueFlag && listNew.isNotEmpty()) {
            listNew.last().flag = true
        }
        btnDialogTitle.text = title
        dialogBtmAdapter.data = listNew
        dialogBtmAdapter.notifyDataSetChanged()
        rvData.scrollToPosition(0)
        dialogBtmAdapter.setOnItemClickListener { ater, view, position ->
            dialogBtmAdapter.data.forEach {
                it.flag = false
            }
            item = dialogBtmAdapter.data[position]
            item!!.flag = true
            dialogBtmAdapter.notifyDataSetChanged()
        }
        btnDialogDone.setOnClickListener {
            item?.let { it1 -> callBack(it1) }
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }

    private fun setupToInfoSettingPage() {
        //真實姓名
        ll_real_name.setOnClickListener {
            startActivity(EditUserNameActivity::class.java)
//            securityCodeEnter = SecurityCodeEnterType.REALNAME
//            viewModel.checkNeedToShowSecurityDialog()//檢查有需不需要簡訊認證
        }
        //暱稱
        btn_nickname.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.NickName) }
        //出生地
        llPlaceOfBirth.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.PlaceOfBirth) }

        llZipCodeCurrent.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.ZipCode) }
        //密碼設置
        btn_pwd_setting.setOnClickListener {
            securityCodeEnter = SecurityCodeEnterType.PW
            viewModel.checkNeedToShowSecurityDialog()//檢查有需不需要簡訊認證
        }
        //登录授权
        lin_auth.setOnClickListener { startActivity(AuthActivity::class.java) }
        //QQ號碼
        ll_qq_number.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.QQNumber) }
        //郵箱
        ll_e_mail.setOnClickListener { editBindInfo(ModifyType.Email) }
        //手機號碼
        ll_phone_number.setOnClickListener { editBindInfo(ModifyType.PhoneNumber) }
        //微信
        ll_wechat.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.WeChat) }
        //實名制
        ll_verified.setOnClickListener {
            if (ll_verified.isEnabled)
                startActivity(VerifyIdentityActivity::class.java)
        }
        llNationality.setOnClickListener {
            showBottomDialog(
                viewModel.nationalityList,
                resources.getString(R.string.P103),
                tvNationality.text.toString()
            ) {
                tvNationality.text = it.name
                viewModel.userCompleteUserDetails(
                    Uide(
                        nationality = it.name
                    )
                )
            }
        }
        llProvinceCurrent.setOnClickListener {
            showProvinceDialog()
        }
        llProvincePermanent.setOnClickListener {
            showProvincePDialog()
        }
        llCityCurrent.setOnClickListener {
            if (viewModel.cityList.isEmpty()) {
                showProvinceDialog()
            } else {
                showBottomDialog(
                    viewModel.cityList,
                    resources.getString(R.string.J901),
                    tvCityCurrent.text.toString()
                ) {
                    tvCityCurrent.text = it.name
                    viewModel.userCompleteUserDetails(
                        Uide(
                            city = it.name
                        )
                    )
                }
            }

        }
        llCityPermanent.setOnClickListener {
            if (viewModel.cityPList.isEmpty()) {
                showProvincePDialog()
            } else {
                showBottomDialog(
                    viewModel.cityPList,
                    resources.getString(R.string.J901),
                    tvCityPermanent.text.toString()
                ) {
                    tvCityPermanent.text = it.name
                    viewModel.userCompleteUserDetails(
                        Uide(
                            permanentCity = it.name,
                        )
                    )
                }
            }

        }
        llNatureOfWork.setOnClickListener {
            showBottomDialog(
                viewModel.workList,
                resources.getString(R.string.P106),
                tvNatureOfWork.text.toString()
            ) {
                tvNatureOfWork.text = it.name
                viewModel.userCompleteUserDetails(
                    Uide(
                        natureOfWork = it.name
                    )
                )
            }
        }
        //address
        llAddressCurrent.setOnClickListener {
            putExtraForProfileInfoActivity(ModifyType.Address)
        }
        llAddressPermanent.setOnClickListener {
            putExtraForProfileInfoActivity(ModifyType.AddressP)
        }
        //zip code
        llZipCodeCurrent.setOnClickListener {
            putExtraForProfileInfoActivity(ModifyType.ZipCode)
        }
        llZipCodePermanent.setOnClickListener {
            putExtraForProfileInfoActivity(ModifyType.ZipCodeP)
        }

        llSourceOfIncome.setOnClickListener {
            showBottomDialog(
                viewModel.salaryStringList,
                resources.getString(R.string.P105),
                tvSourceOfIncome.text.toString(),
                true
            ) {
                if (it.id == 6) {
                    val dialog = SourceOfIncomeDialog(this)
                    dialog.setPositiveClickListener(object :
                        SourceOfIncomeDialog.OnPositiveListener {
                        override fun positiveClick(str: String) {
                            val workstr = str.ifEmpty {
                                resources.getString(R.string.other)
                            }
                            tvSourceOfIncome.text = workstr
                            viewModel.userCompleteUserDetails(
                                Uide(
                                    salarySource = SalarySource(
                                        it.id,
                                        workstr
                                    )
                                )
                            )
                        }
                    })
                    dialog.show()
                } else {
                    tvSourceOfIncome.text = it.name
                    viewModel.userCompleteUserDetails(
                        Uide(
                            salarySource = SalarySource(
                                it.id,
                                it.name
                            )
                        )
                    )
                }
            }
        }
        ll_cancel_account.isVisible = Constants.isVersonControl
        //注销账号
        ll_cancel_account.setOnClickListener { startActivity(CancelAccountActivity::class.java) }
        llBirthday.setOnClickListener { dateTimePicker?.show() }
    }

    private fun showProvinceDialog() {
        if (viewModel.areaData == null) {
            return
        }
        showBottomDialog(
            viewModel.provincesList,
            resources.getString(R.string.J036),
            tvProvinceCurrent.text.toString()
        ) {
            viewModel.updateCityData(it.id)
            if (it.name != tvProvinceCurrent.text.toString()) {
                tvCityCurrent.text = viewModel.cityList.first().name
                viewModel.userCompleteUserDetails(
                    Uide(
                        city = viewModel.cityList.first().name
                    )
                )
            }
            tvProvinceCurrent.text = it.name
            viewModel.userCompleteUserDetails(
                Uide(
                    province = it.name
                )
            )
        }
    }

    private fun showProvincePDialog() {
        if (viewModel.areaData == null) {
            return
        }
        showBottomDialog(
            viewModel.provincesPList,
            resources.getString(R.string.J036),
            tvProvincePermanent.text.toString()
        ) {
            viewModel.updateCityPData(it.id)
            if (it.name != tvCityPermanent.text.toString()) {
                tvCityPermanent.text = viewModel.cityPList.first().name
                viewModel.userCompleteUserDetails(
                    Uide(
                        permanentCity = viewModel.cityPList.first().name,
                    )
                )
            }
            tvProvincePermanent.text = it.name
            viewModel.userCompleteUserDetails(
                Uide(
                    permanentProvince = it.name
                )
            )
        }
    }

    private fun editBindInfo(@ModifyType modifyType: Int) {
        val userInfo = viewModel.userInfo.value
        val phoneNo = userInfo?.phone
        val email = userInfo?.email
        val oldInfo = if (modifyType == ModifyType.Email) email else phoneNo
        // 如果未设置过对应的信息，就直接去设置不需要校验
        if (oldInfo.isEmptyStr()) {
//            putExtraForProfileInfoActivity(modifyType)
            ModifyBindInfoActivity.start(this, modifyType, 100, null, null, null)
        } else {
            VerificationWaysActivity.start(this, modifyType, phoneNo, email)
        }
    }

    private fun putExtraForProfileInfoActivity(@ModifyType modifyType: Int) {
        val intent = Intent(this, ModifyProfileInfoActivity::class.java)
        intent.putExtra(MODIFY_INFO, modifyType)
        startActivity(intent)
    }

    private fun updateAvatar(iconUrl: String?) {
        iv_head.load("$iconUrl", R.drawable.ic_person_avatar)
    }

    private fun uploadImg(file: File) {
        val userId = viewModel.userInfo.value?.userId.toString()
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.AVATAR)
        viewModel.uploadImage(uploadImgRequest)
    }


    private fun checkStr(str: String?): String {
        return if (str.isNullOrEmpty()) {
            if(viewModel.userInfo.value?.verified == VerifiedType.PASSED.value) "" else resources.getString(R.string.set)
        } else {
            str
        }
    }


    private fun initObserve() {
        viewModel.userDetail.observe(this) {
            setIdentityDetail(it)
        }
        viewModel.editIconUrlResult.observe(this) {
            val iconUrlResult = it?.getContentIfNotHandled()
            if (iconUrlResult?.success != true) {
                iconUrlResult?.msg?.let { msg -> showErrorPromptDialog(msg) {} }
                return@observe
            }
            showPromptDialog(getString(R.string.prompt), getString(R.string.save_avatar_success)) {}
        }

        viewModel.userInfo.observe(this) {
            if (it != null) {
//                tvAddressPermanent.text = checkStr(it.permanentAddress)
//                tvZipCodePermanent.text = checkStr(it.permanentZipCode)
//                tvAddressCurrent.text = checkStr(it.address)
//                tvZipCodeCurrent.text = checkStr(it.zipCode)
//                tvPlaceOfBirth.text = checkStr(it.placeOfBirth)
                updateAvatar(it.iconUrl)
                tv_nickname.text = it.nickName
                tv_member_account.text = it.userName
                tv_id.text = it.userId.toString()
                tv_real_name.text = it.fullName
                setWithdrawInfo(it)
            }
            ll_verified.isVisible =
                sConfigData?.realNameWithdrawVerified.isStatusOpen() || sConfigData?.realNameRechargeVerified.isStatusOpen()
            tv_pass_word.text =
                if (it?.passwordSet == true) getString(R.string.set) else getString(R.string.edit)
            setIdentifyStatus(ll_verified.isVisible&&it?.verified==VerifiedType.PASSED.value)
            viewModel.userDetail.value?.let { setIdentityDetail(it) }
            when (it?.verified) {
                VerifiedType.PASSED.value -> {
                    ll_verified.isEnabled = true
                    ll_verified.isClickable = true
                    tv_verified.text = getString(R.string.kyc_passed)
                }

                VerifiedType.NOT_YET.value, VerifiedType.VERIFIED_FAILED.value -> {
                    ll_verified.isEnabled = true
                    ll_verified.isClickable = true
                    tv_verified.text = getString(R.string.kyc_unverified)

                }

                VerifiedType.REVERIFIED_NEED.value -> {
                    ll_verified.isEnabled = true
                    ll_verified.isClickable = true
                    tv_verified.text = getString(R.string.P211)

                }

                VerifiedType.VERIFYING.value, VerifiedType.VERIFIED_WAIT.value -> {
                    ll_verified.isEnabled = true
                    ll_verified.isClickable = true
                    tv_verified.text = getString(R.string.kyc_unverifing)

                }
                VerifiedType.REVERIFYING.value -> {
                    ll_verified.isEnabled = true
                    ll_verified.isClickable = true
                    tv_verified.text = getString(R.string.P196)

                }

                else -> {
                    ll_verified.isEnabled = true
                    ll_verified.isClickable = true
                    tv_verified.text = getString(R.string.kyc_unverified)

                }
            }

            if (it?.setted == FLAG_NICKNAME_IS_SET) {
                btn_nickname.isEnabled = false
                icon_arrow_nickname.visibility = View.GONE
            } else {
                btn_nickname.isEnabled = true
                icon_arrow_nickname.visibility = View.VISIBLE
            }

        }

        //是否顯示簡訊驗證彈窗
        viewModel.needToSendTwoFactor.observe(this) {
            val b = it.getContentIfNotHandled() ?: return@observe
            if (b) {
                customSecurityDialog = CustomSecurityDialog().apply {
                    getSecurityCodeClickListener {
                        this.showSmeTimer300()
                        viewModel.sendTwoFactor()
                    }

                    positiveClickListener = CustomSecurityDialog.PositiveClickListener { number ->
                        viewModel.validateTwoFactor(ValidateTwoFactorRequest(number))
                    }
                }

                customSecurityDialog?.show(supportFragmentManager, null)
                return@observe
            }

            //有手機號碼又不用驗證的狀態下
            securityEnter()
        }

        //簡訊驗證失敗
        viewModel.errorMessageDialog.observe(this) {
            val errorMsg = it ?: getString(R.string.unknown_error)
            CustomAlertDialog(this).apply {
                setMessage(errorMsg)
                setNegativeButtonText(null)
                setCanceledOnTouchOutside(false)
                isCancelable = false
            }.show(supportFragmentManager, null)
        }

        //簡訊驗證成功
        viewModel.twoFactorSuccess.observe(this) {
            if (it != true) {
                return@observe
            }

            customSecurityDialog?.dismiss()
            securityEnter()
        }

        //確認收到簡訊驗證碼
        viewModel.twoFactorResult.observe(this) {
            //傳送驗證碼成功後才能解鎖提交按鈕
            customSecurityDialog?.setPositiveBtnClickable(it?.success ?: false)
            sConfigData?.hasGetTwoFactorResult = true
        }

        //使用者沒有電話號碼
        viewModel.showPhoneNumberMessageDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(this, supportFragmentManager)
            }
        }

        viewModel.isWithdrawShowVerifyDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b)
                    showKYCVerifyDialog()
                else
                    viewModel.checkWithdrawSystem()
            }
        }

        viewModel.isRechargeShowVerifyDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b)
                    showKYCVerifyDialog()
                else
                    viewModel.checkRechargeSystem()
            }
        }
    }

    private fun securityEnter() {
        if (securityCodeEnter == SecurityCodeEnterType.REALNAME) {
            putExtraForProfileInfoActivity(ModifyType.RealName)
            return
        }

        if (securityCodeEnter == SecurityCodeEnterType.PW) {
            startActivity(SettingPasswordActivity::class.java)
        }
    }

    private fun setWithdrawInfo(userInfo: UserInfo) = userInfo.run {
        judgeImproveInfo(ll_real_name, tv_real_name, icon_real_name, fullName)
        judgeImproveInfo(ll_qq_number, tv_qq_number, icon_qq_number, qq)
        judgeImproveInfo(ll_e_mail, tv_e_mail, icon_e_mail, email, true)
        judgeImproveInfo(ll_phone_number, tv_phone_number, icon_phone_number, phone, true)
        judgeImproveInfo(ll_wechat, tv_we_chat, icon_wechat, wechat)
        if (viewModel.userInfo.value?.isGlifeAccount()==true){
            ll_phone_number.isEnabled = false
            icon_phone_number.gone()
        }
    }

    private fun judgeImproveInfo(
        itemLayout: LinearLayout,
        tvInfo: TextView,
        iconModify: ImageView,
        infoData: String?,
        editable: Boolean = false
    ) = tvInfo.run {

        if (infoData.isNullOrEmpty()) {
            text = getString(if (editable) R.string.set else R.string.need_improve)
            iconModify.isVisible = true
            itemLayout.isEnabled = true
        } else {
            text = infoData
            iconModify.isVisible = editable
            itemLayout.isEnabled = editable
        }

        setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.color_939393_999999))
    }

    private fun showKYCVerifyDialog() {
        VerifyIdentityDialog().show(supportFragmentManager, null)
    }


    /**
     * 初始化时间选择控件
     */
    private fun initDateTimeView() {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.YEAR, -100)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.YEAR, -21)
        tomorrow.add(Calendar.DAY_OF_MONTH, -1)
        dateTimePicker = DateTimePickerOptions(this).getBuilder { date, _ ->
            TimeUtil.dateToStringFormatYMD(date).let {
                tvBirthday.text = it
                viewModel.userCompleteUserDetails(
                    Uide(
                        birthday = it
                    )
                )
            }
        }
            .setLayoutRes(R.layout.dialog_date_select, object : CustomListener {
                override fun customLayout(v: View) {
                    //自定义布局中的控件初始化及事件处理
                    v.findViewById<View>(R.id.btnBtmCancel).setOnClickListener {
                        dateTimePicker?.dismiss()
                    }
                    v.findViewById<View>(R.id.btnBtmDone).setOnClickListener {
                        dateTimePicker?.returnData()
                        dateTimePicker?.dismiss()
                    }

                }
            })
            .setItemVisibleCount(6)
            .setLineSpacingMultiplier(2.0f)
            .setRangDate(yesterday, tomorrow)
            .setDate(tomorrow)
            .build()

    }
    private fun setIdentityDetail(it: UserInfoDetailsEntity){
        tvNationality.text = checkStr(it.t.nationality)
        tvBirthday.text = checkStr(it.t.birthday)
        tvPlaceOfBirth.text = checkStr(it.t.placeOfBirth)
        tvSourceOfIncome.text = if (it.t.salarySource?.id == 6) {
            checkStr(it.t.salarySource.name)
        } else if (it.t.salarySource?.id == null) {
            resources.getString(R.string.set)
        } else {
            it.t.salarySource.id.let { it1 ->
                viewModel.getSalaryName(
                    it1,
                    resources.getString(R.string.set)
                )
            }
        }
        tvNatureOfWork.text = checkStr(it.t.natureOfWork)
        tvProvinceCurrent.text = checkStr(it.t.province)
        tvCityCurrent.text = checkStr(it.t.city)
        tvAddressCurrent.text = checkStr(it.t.address)
        tvZipCodeCurrent.text = checkStr(it.t.zipCode)
        tvProvincePermanent.text = checkStr(it.t.permanentProvince)
        tvCityPermanent.text = checkStr(it.t.permanentCity)
        tvAddressPermanent.text = checkStr(it.t.permanentAddress)
        tvZipCodePermanent.text = checkStr(it.t.permanentZipCode)
    }
    private fun setIdentifyStatus(verified: Boolean){
        val iconViews = arrayOf(
            iconNationality,
            iconBirthday,
            iconPlaceOfBirth,
            iconSourceOfIncome,
            iconNatureOfWork,
            iconProvinceCurrent,
            iconCityCurrent,
            iconAddressCurrent,
            iconZipCodeCurrent,
            iconProvincePermanent,
            iconCityPermanent,
            iconAddressPermanent,
            iconZipCodePermanent,
        )
        if (verified){
            setViewGone(*iconViews)
        }else{
            setViewVisible(*iconViews)
        }
        val itemViews = arrayOf(
            llNationality,
            llBirthday,
            llPlaceOfBirth,
            llSourceOfIncome,
            llNatureOfWork,
            llProvinceCurrent,
            llCityCurrent,
            llAddressCurrent,
            llZipCodeCurrent,
            llProvincePermanent,
            llCityPermanent,
            llAddressPermanent,
            llZipCodePermanent,
        )
        setViewClickable(!verified,*itemViews)
    }

}