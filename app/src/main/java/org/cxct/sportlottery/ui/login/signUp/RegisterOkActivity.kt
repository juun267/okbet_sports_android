package org.cxct.sportlottery.ui.login.signUp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import cn.jpush.android.api.JPushInterface
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_register_ok.*
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.ActivityRegisterOkBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.login.checkRegisterListener
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.profile.PicSelectorDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*

/**
 * @app_destination 註冊
 */
class RegisterOkActivity : BaseActivity<RegisterViewModel>(RegisterViewModel::class),
    View.OnClickListener {
    private var firstFile: File? = null
    private var secondFile: File? = null

    private var mSmsTimer: Timer? = null
    private lateinit var binding: ActivityRegisterOkBinding

    private var birthdayTimePickerView: TimePickerView? = null

    private var salarySourceSelectedData: StatusSheetData? = null
    private var bettingShopSelectedData: StatusSheetData? = null
    private var identityTypeSelectedData: StatusSheetData? = null //當前證件類型選中
    private var backupIdentityTypeSelectedData: StatusSheetData? = null //當前證件類型選中
    private var backupIdentityTypeSelectedData2: StatusSheetData? = null //當前證件類型選中
    private var securityPbTypeSelectedData: StatusSheetData? = null //當前證件類型選中

    private var credentialsFragment: RegisterCredentialsFragment? = null
    private var isUploaded = false
    private var page = 1

    private var etIdentityTypeName: String = "";
    private var etIdentityTypeName2: String = "";
    override fun onClick(v: View?) {
        when (v) {
            binding.ivReturn -> {
                updateValidCode()
            }
            binding.tvDuty -> {
                JumpUtil.toInternalWeb(
                    this,
                    Constants.getDutyRuleUrl(this),
                    resources.getString(R.string.responsible)
                )

            }
            binding.tvPrivacy -> {
                JumpUtil.toInternalWeb(
                    this,
                    Constants.getPrivacyRuleUrl(this),
                    resources.getString(R.string.privacy_policy)
                )

            }
            binding.tvAgreement -> {
                JumpUtil.toInternalWeb(
                    this,
                    Constants.getAgreementRuleUrl(this),
                    resources.getString(R.string.terms_conditions)
                )
            }

        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterOkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .statusBarView(v_statusbar)
            .transparentStatusBar()
            .fitsSystemWindows(false)
            .init()
        initView()
        setPage()
        setupBackButton()
        setupAgreement()
        setupRegisterButton()
        setupGoToLoginButton()
        initObserve()

    }

    private fun initView() {
        binding.apply {
            eetLoginPassword.transformationMethod =
                AsteriskPasswordTransformationMethod()
            eetConfirmPassword.transformationMethod =
                AsteriskPasswordTransformationMethod()
            etLoginPassword.endIconImageButton.setOnClickListener {
                if (etLoginPassword.endIconResourceId == R.drawable.ic_eye_open) {
                    eetLoginPassword.transformationMethod =
                        AsteriskPasswordTransformationMethod()
                    etLoginPassword.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etLoginPassword.setEndIcon(R.drawable.ic_eye_open)
                    eetLoginPassword.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                }
                etLoginPassword.hasFocus = true
                eetLoginPassword.setSelection(eetLoginPassword.text.toString().length)
            }
            etConfirmPassword.endIconImageButton.setOnClickListener {
                if (etConfirmPassword.endIconResourceId == R.drawable.ic_eye_open) {
                    eetConfirmPassword.transformationMethod =
                        AsteriskPasswordTransformationMethod()
                    etConfirmPassword.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etConfirmPassword.setEndIcon(R.drawable.ic_eye_open)
                    eetConfirmPassword.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                }
                etConfirmPassword.hasFocus = true
                eetConfirmPassword.setSelection(eetConfirmPassword.text.toString().length)
            }
            etWithdrawalPwd.endIconImageButton.setOnClickListener {
                if (etWithdrawalPwd.endIconResourceId == R.drawable.ic_eye_open) {
                    eetWithdrawalPwd.transformationMethod =
                        AsteriskPasswordTransformationMethod()
                    etWithdrawalPwd.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etWithdrawalPwd.setEndIcon(R.drawable.ic_eye_open)
                    eetWithdrawalPwd.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                }
                etWithdrawalPwd.hasFocus = true
                eetWithdrawalPwd.setSelection(eetWithdrawalPwd.text.toString().length)
            }
            btnRegister.setTitleLetterSpacing()
        }

        binding.ivReturn.setOnClickListener(this)
        binding.tvDuty.setOnClickListener(this)
        binding.tvDuty.apply {
            paint.flags = Paint.UNDERLINE_TEXT_FLAG
            paint.isAntiAlias = true
        }
        //Html.fromHtml("<u>"+getString(R.string.register_privacy_policy)+"</u>")
        //TODO  遗留富文本下划线需要设置
        binding.tvPrivacy.text =
            "1." + String.format(getString(R.string.register_privacy_policy_promotions),
                getString(R.string.register_privacy_policy))
        binding.tvPrivacy.makeLinks(
            Pair(
                applicationContext.getString(R.string.register_privacy_policy),
                View.OnClickListener {
                    JumpUtil.toInternalWeb(
                        this,
                        Constants.getPrivacyRuleUrl(this),
                        resources.getString(R.string.register_privacy_policy)
                    )
                })
        )
        val appName = getString(R.string.app_name)
        //中英appName在前半 越南文appName會在後半
        binding.tvAgreement.text =
            when (LanguageManager.getSelectLanguage(this@RegisterOkActivity)) {
                LanguageManager.Language.VI -> "2." + String.format(
                    getString(R.string.register_over_21),
                    appName
                ) + getString(R.string.terms_conditions) + String.format(
                    getString(R.string.register_rules_2nd_half),
                    appName
                )
                else -> "2." + String.format(
                    getString(R.string.register_over_21),
                    appName
                ) + getString(
                    R.string.terms_conditions
                )
            }

        binding.tvAgreement.makeLinks(
            Pair(getString(R.string.terms_conditions), View.OnClickListener {
                JumpUtil.toInternalWeb(
                    this,
                    Constants.getAgreementRuleUrl(this),
                    resources.getString(R.string.terms_conditions)
                )
            })
        )

        binding.tvNotPHOfficial.text = "3." + getString(R.string.register_not_ph_official)
        binding.tvNotPHSchool.text = "4." + getString(R.string.register_not_ph_school)
        binding.tvRuleOkbet.text = "5." + getString(R.string.register_rule_okbet)
        binding.tvAgreeAll.text = getString(R.string.register_rule_agree_all)
        setLetterSpace()
    }
    private fun setLetterSpace() {
        if (LanguageManager.getSelectLanguage(this) == LanguageManager.Language.ZH) {
            binding.btnRegister.letterSpacing = 0.6f
        }
    }

    private fun setPage() {
        val isEnableKYCVerify = sConfigData?.enableKYCVerify == FLAG_OPEN
        val isSecondVerifyKYCOpen = sConfigData?.idUploadNumber.equals("2")
        val bettingStationVisibility = sConfigData?.enableBettingStation == FLAG_OPEN
        when (page) {
            1 -> {
                btn_register.text = getString(R.string.next_step)

                binding.etFullName.visibility = View.GONE
                binding.etWithdrawalPwd.visibility = View.GONE
                binding.etPhone.visibility = View.GONE
                block_sms_valid_code.visibility = View.GONE
                block_valid_code.visibility = View.GONE

                binding.etIdentityType.visibility = View.GONE
                binding.etIdentityNumber.visibility = View.GONE
                binding.etIdentity.visibility = View.GONE

                binding.etIdentityType2.visibility = View.GONE
                binding.etIdentityNumber2.visibility = View.GONE
                binding.etIdentity2.visibility = View.GONE

                binding.etBirth.visibility = View.GONE
                binding.etSalary.visibility = View.GONE
                binding.etBettingShop.visibility = View.GONE
                binding.etMail.visibility = View.GONE

                binding.etAddress.visibility = View.GONE
                binding.etSecurityPb.visibility = View.GONE
                binding.etSecurityPbType.visibility = View.GONE

                binding.etPostal.visibility = View.GONE
                binding.etProvince.visibility = View.GONE
                binding.etCity.visibility = View.GONE

                binding.etQq.visibility = View.GONE
                binding.etWeChat.visibility = View.GONE
                binding.etZalo.visibility = View.GONE
                binding.etFacebook.visibility = View.GONE
                binding.etWhatsApp.visibility = View.GONE
                binding.etTelegram.visibility = View.GONE

                binding.clAgreement.visibility = View.VISIBLE
                binding.etRecommendCode.visibility = View.VISIBLE
                binding.etMemberAccount.visibility = View.VISIBLE
                binding.etLoginPassword.visibility = View.VISIBLE
                binding.etConfirmPassword.visibility = View.VISIBLE
            }
            2 -> {
                btn_register.text = getString(R.string.next_step)

                binding.clAgreement.visibility = View.GONE
                binding.etRecommendCode.visibility = View.GONE
                binding.etMemberAccount.visibility = View.GONE
                binding.etLoginPassword.visibility = View.GONE
                binding.etConfirmPassword.visibility = View.GONE

                binding.etAddress.visibility = View.GONE
                binding.etSecurityPbType.visibility = View.GONE
                binding.etSecurityPb.visibility = View.GONE

                block_sms_valid_code.visibility = View.GONE
                block_valid_code.visibility = View.GONE

                binding.etBettingShop.visibility = View.GONE
                binding.etMail.visibility = View.GONE
                binding.etPostal.visibility = View.GONE
                binding.etProvince.visibility = View.GONE
                binding.etCity.visibility = View.GONE

                binding.etQq.visibility = View.GONE
                binding.etWeChat.visibility = View.GONE
                binding.etZalo.visibility = View.GONE
                binding.etFacebook.visibility = View.GONE
                binding.etWhatsApp.visibility = View.GONE
                binding.etTelegram.visibility = View.GONE


                binding.etIdentityType.isVisible = isEnableKYCVerify
                binding.etIdentityNumber.isVisible = isEnableKYCVerify
                binding.etIdentity.isVisible = isEnableKYCVerify
                binding.endButton.isVisible = isEnableKYCVerify

                binding.etIdentityType2.isVisible = isEnableKYCVerify && isSecondVerifyKYCOpen
                binding.etIdentityNumber2.isVisible = isEnableKYCVerify && isSecondVerifyKYCOpen
                binding.etIdentity2.isVisible = isEnableKYCVerify && isSecondVerifyKYCOpen
                binding.endButton2.isVisible = isEnableKYCVerify && isSecondVerifyKYCOpen

                setupFullName()
                setupWithdrawalPassword()
                setupPhone()
                setupBirthday()
                setupSmsValidCode()
                setupRegisterIdentity()
                setupSalarySource()

                setupIdentityType(null)
                setupIdentityType2(null)
            }
            else -> {
                btn_register.text = getString(R.string.btn_register)

                binding.clAgreement.visibility = View.GONE
                binding.etRecommendCode.visibility = View.GONE
                binding.etMemberAccount.visibility = View.GONE
                binding.etLoginPassword.visibility = View.GONE
                binding.etConfirmPassword.visibility = View.GONE

                binding.etFullName.visibility = View.GONE
                binding.etWithdrawalPwd.visibility = View.GONE
                binding.etPhone.visibility = View.GONE
                binding.blockSmsValidCode.visibility = View.GONE
                binding.etBirth.visibility = View.GONE
                binding.etSalary.visibility = View.GONE

                binding.etIdentityType.visibility = View.GONE
                binding.etIdentityNumber.visibility = View.GONE
                binding.etIdentity.visibility = View.GONE

                binding.etIdentityType2.visibility = View.GONE
                binding.etIdentityNumber2.visibility = View.GONE
                binding.etIdentity2.visibility = View.GONE

                if (bettingStationVisibility) {
                    binding.etBettingShop.visibility = View.VISIBLE
                    setupBettingShop()
                } else {
                    binding.etBettingShop.visibility = View.GONE
                }
                setupMail()
                setupAddress()
                setupValidCode()
                setupQQ()
                setupWeChat()
                setupZalo()
                setupFacebook()
                setupWhatsApp()
                setupTelegram()
                setupSecurityPb()
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        stopSmeTimer()
    }

    /**
     * 開啟上傳證件照片頁面
     */
    private fun openCredentialsPage() {
        //進入前先清除照片上傳狀態
        viewModel.resetCredentialsStatus()
        binding.flCredentials.visibility = View.VISIBLE
        val transaction = supportFragmentManager.beginTransaction()
        credentialsFragment = RegisterCredentialsFragment.newInstance(
            registerCredentialsListener = RegisterCredentialsFragment.RegisterCredentialsListener(
                onCloseFragment = {
                    supportFragmentManager.popBackStack()
                    binding.flCredentials.visibility = View.GONE
                })
        )


        credentialsFragment?.let { fragment ->
            transaction
                .add(
                    binding.flCredentials.id,
                    fragment,
                    RegisterCredentialsFragment::class.java.simpleName
                )
                .addToBackStack(RegisterCredentialsFragment::class.java.simpleName)
                .commit()
        }
        hideSoftKeyboard(this)
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            when (page) {
                1 -> {
                    finish()
                }
                else -> {
                    page--
                    setPage()
                    viewModel.focusChangeCheckAllInputComplete(page)
                }
            }

        }
    }

    private fun setupFullName() {
        binding.etFullName.visibility =
            if (sConfigData?.enableFullName == FLAG_OPEN) View.VISIBLE else View.GONE
    }

    private fun setupWithdrawalPassword() {
        binding.etWithdrawalPwd.visibility =
            if (sConfigData?.enableFundPwd == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupPhone() {
        binding.etPhone.visibility =
            if (sConfigData?.enablePhone == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupMail() {
        binding.etMail.visibility =
            if (sConfigData?.enableEmail == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupQQ() {
        binding.etQq.visibility =
            if (sConfigData?.enableQQ == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupWeChat() {
        binding.etWeChat.visibility =
            if (sConfigData?.enableWechat == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupZalo() {
        binding.etZalo.visibility =
            if (sConfigData?.enableZalo == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupFacebook() {
        binding.etFacebook.visibility =
            if (sConfigData?.enableFacebook == FLAG_OPEN) View.VISIBLE else View.GONE
    }

    private fun setupWhatsApp() {
        binding.etWhatsApp.visibility =
            if (sConfigData?.enableWhatsApp == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupTelegram() {
        binding.etTelegram.visibility =
            if (sConfigData?.enableTelegram == FLAG_OPEN) View.VISIBLE else View.GONE

    }


    private fun setupAddress() {
        (if (sConfigData?.enableAddress == FLAG_OPEN) View.VISIBLE else View.GONE).let { visible ->
            with(binding) {
                etPostal.visibility = visible
                etProvince.visibility = visible
                etCity.visibility = visible
                etAddress.visibility = visible
            }
        }
    }


    private fun setupSecurityPb() {
        //TODO etSecurityPbType 預設選中第一項故沒有補上未填入的錯誤提示
        with(binding) {
            //顯示隱藏該選項
            if (sConfigData?.enableSafeQuestion == FLAG_OPEN) {
                etSecurityPbType.visibility = View.VISIBLE
                etSecurityPb.visibility = View.VISIBLE
            } else {
                etSecurityPbType.visibility = View.GONE
                etSecurityPb.visibility = View.GONE
            }

            //根據config配置薪資來源選項
            val securityPbTypeList = mutableListOf<StatusSheetData>()
            sConfigData?.safeQuestionList?.map { securityPbType ->
                securityPbTypeList.add(
                    StatusSheetData(
                        securityPbType.id.toString(),
                        securityPbType.name
                    )
                )
            }

            //預設顯示第一項
            securityPbTypeSelectedData = securityPbTypeList.firstOrNull()
            eetSecurityPbType.setText(securityPbTypeList.firstOrNull()?.showName)
            //設置預設文字後會變成選中狀態, 需清除focus
            etSecurityPbType.hasFocus = false
            viewModel.checkSecurityPb(eetSecurityPbType.text.toString())

            //配置點擊展開選項選單
            etSecurityPbType.post {
                securityPbTypeSpinner.setSpinnerView(
                    eetSecurityPbType,
                    etSecurityPbType,
                    securityPbTypeList,
                    touchListener = {
                        //旋轉箭頭
                        etSecurityPbType.endIconImageButton.rotation = 180F
                    },
                    itemSelectedListener = {
                        securityPbTypeSelectedData = it
                        eetSecurityPbType.setText(it?.showName)
                    },
                    popupWindowDismissListener = {
                        //旋轉箭頭
                        etSecurityPbType.endIconImageButton.rotation = 0F
                    })
            }

            eetSecurityPbType.post {
                //TODO 可重構 不需要蓋一層View
                /**
                 * 若eetSecurityPb取得focus的話點擊securityPbSpinner
                 */
                eetSecurityPbType.onFocusChangeListener =
                    View.OnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            securityPbTypeSpinner.performClick()
                        }
                    }
            }
        }
    }

    private fun setupBirthday() {
        binding.etBirth.visibility =
            if (sConfigData?.enableBirthday == FLAG_OPEN) View.VISIBLE else View.GONE

        with(binding) {
            birthPicker.setOnClickListener {
                //設置TextFieldBoxes為選中狀態
                etBirth.hasFocus = true
                //隱藏光標
                eetBirth.isCursorVisible = false
                if (binding.root != null && binding.root.windowToken != null) {
                    //隱藏鍵盤
                    val inputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
                }
                birthdayTimePickerView?.show()
            }

            birthdayTimePickerView = createTimePicker { date ->
                eetBirth.setText(TimeUtil.stampToRegisterBirthdayFormat(date))
            }

            eetBirth.post {
                //TODO 可重構獲取focus直接展開不必蓋一個View
                /**
                 * 若Birth取得focus的話點擊birthdayTimePickerView
                 */
                eetBirth.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        birthPicker.performClick()
                    }
                }
            }
        }
    }

    private fun setupRegisterIdentity() {
        with(binding) {
            etIdentity.visibility =
                if (sConfigData?.enableKYCVerify == FLAG_OPEN) View.VISIBLE else View.GONE

            etIdentity.isEnabled = false
            etIdentity.isClickable = false
            etIdentity.hasFocus = false


            etIdentity.setError("", false)


            endButton.visibility =
                if (sConfigData?.enableKYCVerify == FLAG_OPEN) View.VISIBLE else View.GONE
            endButton.setOnClickListener {
                val dialog = PicSelectorDialog()
                dialog.mSelectListener = mfirstSelectDocMediaListener
                dialog.show(supportFragmentManager, null)
            }


            endButton2.visibility =
                if (sConfigData?.enableKYCVerify == FLAG_OPEN && sConfigData?.idUploadNumber.equals(
                        "2")
                ) View.VISIBLE else View.GONE
            endButton2.setOnClickListener {
                val dialog = PicSelectorDialog()
                dialog.mSelectListener = mSecondSelectPhotoMediaListener
                dialog.show(supportFragmentManager, null)
            }
        }

    }

    private fun setupSalarySource() {
        with(binding) {
            //顯示隱藏該選項
            etSalary.visibility =
                if (sConfigData?.enableSalarySource == FLAG_OPEN) View.VISIBLE else View.GONE

            //根據config配置薪資來源選項
            val salarySourceList = mutableListOf<StatusSheetData>()
            sConfigData?.salarySource?.map { salarySource ->
                salarySourceList.add(StatusSheetData(salarySource.id.toString(), salarySource.name))
            }

            //預設顯示第一項
            salarySourceSelectedData = salarySourceList.firstOrNull()
            eetSalary.setText(salarySourceList.firstOrNull()?.showName)
            //設置預設文字後會變成選中狀態, 需清除focus
            etSalary.hasFocus = false
            viewModel.checkSalary(eetSalary.text.toString())

            //配置點擊展開選項選單
            etSalary.post {
                salarySpinner.setSpinnerView(
                    eetSalary,
                    etSalary,
                    salarySourceList,
                    touchListener = {
                        //旋轉箭頭
                        etSalary.endIconImageButton.rotation = 180F
                    },
                    itemSelectedListener = {
                        salarySourceSelectedData = it
                        eetSalary.setText(it?.showName)

                    },
                    popupWindowDismissListener = {
                        //旋轉箭頭
                        etSalary.endIconImageButton.rotation = 0F
                    })
            }

            eetSalary.post {
                //TODO 可重構 不需要蓋一層View
                /**
                 * 若Salary取得focus的話點擊salarySpinner
                 */
                eetSalary.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        salarySpinner.performClick()
                    }
                }
            }
        }
    }


    private fun setupIdentityType2(statusSheetData: StatusSheetData?) {
        with(binding) {
            //顯示隱藏該選項

            //根據config配置薪資來源選項
            val identityTypeList2 = mutableListOf<StatusSheetData>()
            sConfigData?.identityTypeList?.map { identityType ->
                identityTypeList2.add(
                    StatusSheetData(
                        identityType.id.toString(),
                        identityType.name
                    )
                )
            }

            etIdentity2.isEnabled = false
            etIdentity2.isClickable = false
            etIdentity2.hasFocus = false


            etIdentity2.setError("", false)

            if (statusSheetData == null) {
                identityTypeList2.removeAt(0)

                //預設顯示第一項
                backupIdentityTypeSelectedData2 = identityTypeList2[1]
                eetIdentityType2.setText(backupIdentityTypeSelectedData2?.showName)
                //設置預設文字後會變成選中狀態, 需清除focus
                etIdentityType2.hasFocus = false
                viewModel.checkIdentityBackupType(eetIdentityType2.text.toString())
            } else {
                identityTypeList2.remove(statusSheetData)
            }


            etIdentityTypeName = identityTypeSelectedData?.showName.toString()
            //配置點擊展開選項選單
            etIdentityType2.post {
                identityTypeSpinner2.setSpinnerView(
                    eetIdentityType2,
                    etIdentityType2,
                    identityTypeList2,
                    touchListener = {
                        //旋轉箭頭
                        etIdentityType2.endIconImageButton.rotation = 180F
                    },
                    itemSelectedListener = {
                        backupIdentityTypeSelectedData2 = it
                        etIdentityTypeName2 = it?.showName.toString()
                        eetIdentityType2.setText(it?.showName)
                        setupIdentityType(it)
                    },
                    popupWindowDismissListener = {
                        //旋轉箭頭
                        etIdentityType2.endIconImageButton.rotation = 0F
                    })
            }

            eetIdentityType2.post {
                //TODO 可重構 不需要蓋一層View
                /**
                 * 若IdentityType取得focus的話點擊identityTypeSpinner
                 */
                eetIdentityType2.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        identityTypeSpinner2.performClick()
                    }
                }
            }
        }
    }

    private fun setupIdentityType(statusSheetData: StatusSheetData?) {
        with(binding) {
            //顯示隱藏該選項
//            etIdentityType.visibility =
//                if (sConfigData?.enableKYCVerify == FLAG_OPEN) View.VISIBLE else View.GONE

            //根據config配置薪資來源選項
            val identityTypeList = mutableListOf<StatusSheetData>()
            sConfigData?.identityTypeList?.map { identityType ->
                identityTypeList.add(StatusSheetData(identityType.id.toString(), identityType.name))
            }
            if (statusSheetData == null) {
                identityTypeList.removeAt(1)
                //預設顯示第一項
                identityTypeSelectedData = identityTypeList.firstOrNull()
                eetIdentityType.setText(identityTypeList.firstOrNull()?.showName)

                //設置預設文字後會變成選中狀態, 需清除focus
                etIdentityType.hasFocus = false
                viewModel.checkIdentityType(eetIdentityType.text.toString())
            } else {
                identityTypeList.remove(statusSheetData)
            }


            //配置點擊展開選項選單
            etIdentityType.post {
                identityTypeSpinner.setSpinnerView(
                    eetIdentityType,
                    etIdentityType,
                    identityTypeList,
                    touchListener = {
                        //旋轉箭頭
                        etIdentityType.endIconImageButton.rotation = 180F
                    },
                    itemSelectedListener = {
                        identityTypeSelectedData = it
                        etIdentityTypeName = it?.showName.toString()
                        eetIdentityType.setText(it?.showName)
                        setupIdentityType2(it)

                    },
                    popupWindowDismissListener = {
                        //旋轉箭頭
                        etIdentityType.endIconImageButton.rotation = 0F
                    })
            }

            eetIdentityType.post {
                //TODO 可重構 不需要蓋一層View
                /**
                 * 若IdentityType取得focus的話點擊identityTypeSpinner
                 */
                eetIdentityType.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        identityTypeSpinner.performClick()
                    }
                }
            }
        }
    }

    private fun setupBettingShop() {
        val bettingStationVisibility = sConfigData?.enableBettingStation == FLAG_OPEN

        if (bettingStationVisibility) {
            // etBettingShop.visibility = View.VISIBLE
            //查詢投注站列表
            viewModel.bettingStationQuery()
        }

    }

    private fun setupValidCode() {
        if (sConfigData?.enableRegValidCode == FLAG_OPEN) {
            binding.blockValidCode.visibility = View.VISIBLE
            updateValidCode()
        } else {
            binding.blockValidCode.visibility = View.GONE
        }
    }

    private fun setupSmsValidCode() {
        binding.blockSmsValidCode.visibility =
            if (sConfigData?.enableSmsValidCode == FLAG_OPEN) View.VISIBLE else View.GONE
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN) {
            //手機驗證碼開啟，必定需要手機號欄位輸入
            binding.etPhone.visibility = View.VISIBLE
            binding.blockSmsValidCode.visibility = View.VISIBLE
        } else {
            binding.blockSmsValidCode.visibility = View.GONE
        }

        binding.btnSendSms.setOnClickListener {
            sendSms()
        }
    }

    private fun setupAgreement() {
        binding.apply {
            cbAgreeAll.setOnClickListener {
                viewModel.apply {
                    checkCbAgreeAll(cbAgreeAll.isChecked)
                }
            }
            btnRegister.setTitleLetterSpacing()
        }
    }


    private fun setupRegisterButton() {
        binding.apply {
            val defaultInviteCode = Constants.getInviteCode()
            eetRecommendCode.apply {
                checkRegisterListener {
                    if (it != "") {
                        viewModel.checkInviteCode(it)
                    } else {
                        etBettingShopSelectTrue()

                    }
                }
            }
            eetRecommendCode.setText(defaultInviteCode)
            eetRecommendCode.isEnabled = defaultInviteCode.isNullOrEmpty()
            eetMemberAccount.apply {
                checkRegisterListener { viewModel.checkMemberAccount(it) }
            }
            eetLoginPassword.apply {
                checkRegisterListener {
                    viewModel.checkLoginPassword(it,
                        confirmPassword = eetConfirmPassword.text.toString())
                }
            }
            eetConfirmPassword.apply {
                checkRegisterListener {
                    viewModel.checkConfirmPassword(
                        eetLoginPassword.text.toString(),
                        it
                    )
                }
            }
            eetFullName.apply {
                checkRegisterListener { viewModel.checkFullName(it) }
            }
            eetBirth.apply {
                checkRegisterListener { viewModel.checkBirth(it) }
            }
//            eetIdentity.apply {
//                checkRegisterListener { viewModel.checkIdentity() }
//            }
            eetSalary.apply {
                checkRegisterListener { viewModel.checkSalary(it) }
            }
            eetIdentityType.apply { checkRegisterListener { viewModel.checkIdentityType(it) } }
            eetIdentityType2.apply { checkRegisterListener { viewModel.checkIdentityBackupType(it) } }
            eetIdentityNumber.apply { checkRegisterListener { viewModel.checkIdentityNumber(it) } }
            eetIdentityNumber2.apply {
                checkRegisterListener {
                    viewModel.checkIdentityBackupNumber(it)
                }
            }

            eetBettingShop.apply {
                checkRegisterListener {
                    if (it != "") {
                        viewModel.checkBettingShop(it)
                    }
                }
            }

            eetWithdrawalPwd.apply {
                checkRegisterListener { viewModel.checkFundPwd(it) }
            }
            eetQq.apply {
                checkRegisterListener { viewModel.checkQQ(it) }
            }
            eetPhone.apply {
                checkRegisterListener { viewModel.checkPhone(it) }
            }
            eetMail.apply {
                checkRegisterListener { viewModel.checkEmail(it) }
            }
            eetPostal.apply {
                checkRegisterListener { viewModel.checkPostal(it) }
            }
            eetProvince.apply {
                checkRegisterListener { viewModel.checkProvince(it) }
            }
            eetCity.apply {
                checkRegisterListener { viewModel.checkCity(it) }
            }
            eetAddress.apply {
                checkRegisterListener { viewModel.checkAddress(it) }
            }
            eetWeChat.apply {
                checkRegisterListener { viewModel.checkWeChat(it) }
            }
            eetZalo.apply {
                checkRegisterListener { viewModel.checkZalo(it) }
            }
            eetFacebook.apply {
                checkRegisterListener { viewModel.checkFacebook(it) }
            }
            eetWhatsApp.apply {
                checkRegisterListener { viewModel.checkWhatsApp(it) }
            }
            eetTelegram.apply {
                checkRegisterListener { viewModel.checkTelegram(it) }
            }
            eetSecurityPb.apply {
                checkRegisterListener { viewModel.checkSecurityPb(it) }
            }
            eetSmsValidCode.apply {
                checkRegisterListener { viewModel.checkSecurityCode(it) }
            }
            eetVerificationCode.apply {
                checkRegisterListener { viewModel.checkValidCode(it) }
            }
        }

        binding.btnRegister.setOnClickListener {
            if (page == 3) {
                Log.i(">>>", "btnRegister onclicked")
                val deviceId = Settings.Secure.getString(
                    applicationContext.contentResolver, Settings.Secure.ANDROID_ID
                )
                val deviceSn = JPushInterface.getRegistrationID(this)
                binding.apply {
                    var phone = eetPhone.text.toString()
                    if (phone.isNotEmpty() && phone.substring(0, 1) == "0") {
                        phone = phone.substring(1, phone.length)
                    }
                    viewModel.registerSubmit(
                        eetRecommendCode.text.toString(),
                        eetMemberAccount.text.toString(),
                        eetLoginPassword.text.toString(),
                        eetConfirmPassword.text.toString(),
                        eetFullName.text.toString(),
                        eetWithdrawalPwd.text.toString(),
                        eetQq.text.toString(),
                        phone,
                        eetMail.text.toString(),
                        eetPostal.text.toString(),
                        eetProvince.text.toString(),
                        eetCity.text.toString(),
                        eetAddress.text.toString(),
                        eetWeChat.text.toString(),
                        eetZalo.text.toString(),
                        eetFacebook.text.toString(),
                        eetWhatsApp.text.toString(),
                        eetTelegram.text.toString(),
                        securityPbTypeCode = securityPbTypeSelectedData?.code,
                        securityPb = eetSecurityPb.text.toString(),
                        eetSmsValidCode.text.toString(),
                        eetVerificationCode.text.toString(),
                        cbAgreeAll.isChecked,
                        deviceSn,
                        deviceId,
                        birth = eetBirth.text.toString().replace(" ", ""), //傳給後端的不需要有空白間隔
                        identity = eetIdentity.text.toString(),
                        salarySource = salarySourceSelectedData?.code,
                        bettingShop = bettingShopSelectedData?.code,
                        nationCode = null, //國家選項不會出現在有分頁式的註冊頁
                        currency = null, //幣種選項不會出現在有分頁式的註冊頁
                        firstFile = firstFile,
                        identityType = identityTypeSelectedData?.code,
                        identityNumber = eetIdentityNumber.text.toString(),
                        secndFile = secondFile,
                        identityTypeBackup = backupIdentityTypeSelectedData?.code,
                        identityNumberBackup = eetIdentityNumber2.text.toString()
                    )
                }
            }
            if (page < 3) {
                page++
                setPage()
                viewModel.focusChangeCheckAllInputComplete(page)
            }


        }

    }

    private fun sendSms() {
        var phone = binding.eetPhone.text.toString()
        if (phone.isBlank())
            showErrorPromptDialog(
                getString(R.string.prompt),
                getString(R.string.hint_phone_number)
            ) {}
        else {
            binding.btnSendSms.isEnabled = false
            if (phone.substring(0, 1) == "0") {
                phone = phone.substring(1, phone.length)
            }
            viewModel.sendSms(phone)
        }
    }

    private fun updateValidCode() {
        viewModel.getValidCode()
        if (binding.eetVerificationCode.text.toString().isNotEmpty()) {
            binding.eetVerificationCode.setText("");
        }
    }

    private fun setupGoToLoginButton() {
//        binding.btnLogin.setOnClickListener {
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }
//
//        binding.btnVisitFirst.setOnClickListener {
//            viewModel.loginAsGuest()
//        }
    }

    private fun initObserve() {
        setEditTextIme(binding.btnRegister.isEnabled)

        viewModel.registerEnable.observe(this) {
            binding.btnRegister.isEnabled = it
            if(it){
                binding.btnRegister.alpha = 1.0f
            }else{
                binding.btnRegister.alpha = 0.5f
            }
            setEditTextIme(it)
        }

        viewModel.registerResult.observe(this) {
            updateUiWithResult(it)
        }

        viewModel.validCodeResult.observe(this) {
            updateUiWithResult(it)
        }

        viewModel.smsResult.observe(this) {
            updateUiWithResult(it)
        }

        viewModel.loginForGuestResult.observe(this) {
            updateUiWithResult(it)
        }

        /**
         * 輸入欄位判斷後錯誤提示
         */
        viewModel.apply {
            inviteCodeMsg.observe(this@RegisterOkActivity) {
                binding.etRecommendCode.setError(
                    it,
                    false
                )
                if (it == null) {
                    viewModel.queryPlatform(binding.eetRecommendCode.text.toString())
                } else {
                    etBettingShopSelectTrue()
                }

            }

            checkBettingResult.observe(this@RegisterOkActivity) {
                if (it != null && it.success) {
                    etBettingShopSelectFalse(it.checkBettingData?.name.toString())
                } else {
                    etBettingShopSelectTrue()
                    var msg = ""
                    if (it?.msg == null) {
                        msg = getString(R.string.error_recommend_code)
                    } else {
                        msg = it?.msg
                    }
                    binding.etRecommendCode.setError(
                        msg,
                        false
                    )

                }

            }



            memberAccountMsg.observe(this@RegisterOkActivity) {
                if (it.first == null) {
                    viewModel.checkAccountExist(binding.eetMemberAccount.text.toString())
                    return@observe
                } else {
                    binding.etMemberAccount.setError(
                        it.first,
                        false
                    )
                }
            }
            checkAccountMsg.observe(this@RegisterOkActivity) {
                if (it.isExist) {
                    binding.etMemberAccount.setError(
                        getString(R.string.error_register_id_exist),
                        false
                    )
                }


            }
            loginPasswordMsg.observe(this@RegisterOkActivity) {
                binding.etLoginPassword.setError(
                    it.first,
                    false
                )
            }
            confirmPasswordMsg.observe(this@RegisterOkActivity) {
                binding.etConfirmPassword.setError(
                    it.first,
                    false
                )
            }
            fullNameMsg.observe(this@RegisterOkActivity) {
                binding.etFullName.setError(
                    it.first,
                    false
                )
            }
            fundPwdMsg.observe(this@RegisterOkActivity) {
                binding.etWithdrawalPwd.setError(
                    it.first,
                    false
                )
            }
            qqMsg.observe(this@RegisterOkActivity) { binding.etQq.setError(it.first, false) }
            phoneMsg.observe(this@RegisterOkActivity) { binding.etPhone.setError(it.first, false) }
            emailMsg.observe(this@RegisterOkActivity) { binding.etMail.setError(it.first, false) }
            postalMsg.observe(this@RegisterOkActivity) {
                binding.etPostal.setError(
                    it.first,
                    false
                )
            }
            provinceMsg.observe(this@RegisterOkActivity) {
                binding.etProvince.setError(
                    it.first,
                    false
                )
            }
            cityMsg.observe(this@RegisterOkActivity) { binding.etCity.setError(it.first, false) }
            addressMsg.observe(this@RegisterOkActivity) {
                binding.etAddress.setError(
                    it.first,
                    false
                )
            }
            salaryMsg.observe(this@RegisterOkActivity) {
                binding.etSalary.setError(
                    it.first,
                    false
                )
            }
            birthMsg.observe(this@RegisterOkActivity) { binding.etBirth.setError(it.first, false) }
            identityMsg.observe(this@RegisterOkActivity) {
                binding.etIdentity.setError(
                    it.first,
                    false
                )
            }
            identityBackupMsg.observe(this@RegisterOkActivity) {
                binding.etIdentity2.setError(
                    it.first,
                    false
                )
            }
            identityTypeMsg.observe(this@RegisterOkActivity) {
                binding.etIdentityType.setError(
                    it.first,
                    false
                )
            }
            identityBackupTypeMsg.observe(this@RegisterOkActivity) {
                binding.etIdentityType2.setError(
                    it.first,
                    false
                )
            }
            eetIdentityNumber.observe(this@RegisterOkActivity) {
                binding.etIdentityNumber.setError(
                    it.first,
                    false
                )
            }
            eetIdentityBackupNumber.observe(this@RegisterOkActivity) {
                binding.etIdentityNumber2.setError(
                    it.first,
                    false
                )
            }
            bettingShopMsg.observe(this@RegisterOkActivity) {
                binding.etBettingShop.setError(
                    it.first,
                    false
                )
            }
            weChatMsg.observe(this@RegisterOkActivity) {
                binding.etWeChat.setError(
                    it.first,
                    false
                )
            }
            zaloMsg.observe(this@RegisterOkActivity) { binding.etZalo.setError(it.first, false) }
            facebookMsg.observe(this@RegisterOkActivity) {
                binding.etFacebook.setError(
                    it.first,
                    false
                )
            }
            whatsAppMsg.observe(this@RegisterOkActivity) {
                binding.etWhatsApp.setError(
                    it.first,
                    false
                )
            }
            telegramMsg.observe(this@RegisterOkActivity) {
                binding.etTelegram.setError(
                    it.first,
                    false
                )
            }
            securityPbMsg.observe(this@RegisterOkActivity) {
                binding.etSecurityPb.setError(
                    it.first,
                    false
                )
            }
            securityCodeMsg.observe(this@RegisterOkActivity) {
                binding.etSmsValidCode.setError(
                    it.first,
                    false
                )
            }
            validCodeMsg.observe(this@RegisterOkActivity) {
                binding.etVerificationCode.setError(
                    it.first,
                    false
                )
            }
        }

        viewModel.bettingStationList.observe(this) { bettingStationList ->
            with(binding) {
                //設置投注站清單選項
                bettingShopSpinner.setSpinnerView(
                    eetBettingShop,
                    etBettingShop,
                    bettingStationList,
                    touchListener = {
                        //旋轉箭頭
                        etBettingShop.endIconImageButton.rotation = 180F
                    },
                    itemSelectedListener = {
                        bettingShopSelectedData = it
                        eetBettingShop.setText(it?.showName)
                    },
                    popupWindowDismissListener = {
                        //旋轉箭頭
                        etBettingShop.endIconImageButton.rotation = 0F
                    })

                //預設第一項
                bettingShopSelectedData = bettingStationList.firstOrNull()
                eetBettingShop.setText(bettingStationList.firstOrNull()?.showName)
                //預設後會變為選中狀態, 需清除focus
                etBettingShop.hasFocus = false
                viewModel.checkBettingShop(eetBettingShop.text.toString())



                eetBettingShop.post {
                    //TODO 可重構 不需要蓋一層View
                    /**
                     * 若BettingShop取得focus的話點擊bettingShopSpinner
                     */
                    eetBettingShop.onFocusChangeListener =
                        View.OnFocusChangeListener { _, hasFocus ->
                            if (hasFocus) {
                                bettingShopSpinner.performClick()
                            }
                        }
                }
            }
        }

        viewModel.docUrlResult.observe(this) {
            it?.let { result ->
                if (!result.success) {
                    hideLoading()
                    showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                }
            }
        }
        viewModel.photoUrlResult.observe(this) {
            it?.let { result ->
                if (!result.success) {
                    hideLoading()
                    showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                }
            }
        }

        viewModel.isRechargeShowVerifyDialog.observe(this) {
            it.getContentIfNotHandled()?.let { showKycVerify ->
                if (showKycVerify) {
                    //跳宣傳頁顯示驗證彈窗
                    MainTabActivity.reStart(this@RegisterOkActivity)
                    Handler().postDelayed({
                        MultiLanguagesApplication.showKYCVerifyDialog(AppManager.currentActivity() as FragmentActivity)
                    }, 1000)
                } else {
                    //檢查充值系統
                    viewModel.checkRechargeSystem()
                }
            }
        }

        viewModel.rechargeSystemOperation.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    startActivity(Intent(this@RegisterOkActivity, MoneyRechargeActivity::class.java))
                    finish()
                } else {
                    showPromptDialog(
                        getString(R.string.prompt),
                        getString(R.string.message_recharge_maintain)
                    ) {}
                }
            }
        }
    }

    //當所有值都有填，按下enter時，自動點擊註冊鈕
    private fun setEditTextIme(registerEnable: Boolean) {
        binding.apply {
            when (page) {
                1 -> {
                    eetMemberAccount.setActionListener(registerEnable)
                    eetLoginPassword.setActionListener(registerEnable)
                    eetConfirmPassword.setActionListener(registerEnable)
                }
                2 -> {
                    //eetRecommendCode.setActionListener(registerEnable)
                    eetFullName.setActionListener(registerEnable)
                    eetWithdrawalPwd.setActionListener(registerEnable)
//                    eetPhone.setActionListener(registerEnable)
//                    eetSecurityPb.setActionListener(registerEnable)
                }
                else -> {
                    eetMail.setActionListener(registerEnable)
                }
            }


        }
    }

    private fun EditText.setActionListener(isRegisterEnable: Boolean) {
        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId and EditorInfo.IME_MASK_ACTION != 0 && isRegisterEnable) {
                binding.btnRegister.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun updateUiWithResult(loginResult: LoginResult) {
        hideLoading()
        if (loginResult.success) {
            //finish()
            RegisterSuccessDialog().apply {
                setNegativeClickListener {
                    dismiss()
                    //判斷要跳宣傳頁顯示驗證彈窗，還是檢查充值系統
                    viewModel.checkRechargeKYCVerify()
                }
            }.show(supportFragmentManager, null)

        } else {
            updateValidCode()
            showErrorDialog(loginResult.msg)
        }
    }

    private fun updateUiWithResult(validCodeResult: ValidCodeResult?) {
        if (validCodeResult?.success == true) {
            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
            Glide.with(this)
                .load(bitmap)
                .into(binding.ivVerification)
        } else {
            updateValidCode()
            //et_verification_code.setVerificationCode(null)
            ToastUtil.showToastInCenter(
                this@RegisterOkActivity,
                getString(R.string.get_valid_code_fail_point)
            )
        }
    }

    private fun updateUiWithResult(smsResult: NetResult?) {
        binding.btnSendSms.isEnabled = true
        if (smsResult?.success == true) {
            showSmeTimer300()
        } else {
            smsResult?.msg?.let { showErrorPromptDialog(getString(R.string.prompt), it) {} }
            showSmeTimer300()
        }
    }

    //發送簡訊後，倒數五分鐘
    private fun showSmeTimer300() {
        try {
            stopSmeTimer()

            var sec = 60
            mSmsTimer = Timer()
            mSmsTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        if (sec-- > 0) {
                            binding.btnSendSms.isEnabled = false
                            binding.btnSendSms.text = "${sec}s"
                            binding.btnSendSms.setTextColor(
                                ContextCompat.getColor(
                                    this@RegisterOkActivity,
                                    R.color.color_AEAEAE_404040
                                )
                            )
                        } else {
                            stopSmeTimer()
                            binding.btnSendSms.isEnabled = true
                            binding.btnSendSms.text = getString(R.string.get_verification_code)
                            binding.btnSendSms.setTextColor(Color.WHITE)
                        }
                    }
                }
            }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            binding.btnSendSms.isEnabled = true
            binding.btnSendSms.text = getString(R.string.get_verification_code)
        }
    }

    private fun stopSmeTimer() {
        if (mSmsTimer != null) {
            mSmsTimer?.cancel()
            mSmsTimer = null
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show(supportFragmentManager, null)
    }


    fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color = textPaint.linkColor
                    textPaint.isUnderlineText = false
                }

                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
            if (startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
            spannableString.setSpan(
                clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(UnderlineSpan(),
                startIndexOfLink,
                startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        this.movementMethod =
            LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    /**
     * 創建生日用日期選擇器
     * 日期範圍: ~今天
     * 日期格式: 年月日
     */
    private fun createTimePicker(timeSelectedListener: (time: Date) -> Unit): TimePickerView {
        //用來限制生日的結束日期(滿21歲)
        val limit21YearsOld = Calendar.getInstance()
        limit21YearsOld.add(Calendar.YEAR, -21)

        val dateTimePicker: TimePickerView = TimePickerBuilder(
            this
        ) { date, _ ->
            try {
                timeSelectedListener(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            .setLabel("", "", "", "", "", "")
            .setRangDate(null, limit21YearsOld)
            .setDate(Calendar.getInstance())
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.btn_sure))
            .setTitleColor(ContextCompat.getColor(this, R.color.color_CCCCCC_000000))
            .setTitleBgColor(ContextCompat.getColor(this, R.color.color_2B2B2B_E2E2E2))
            .setBgColor(ContextCompat.getColor(this, R.color.color_191919_FCFCFC))
            .setSubmitColor(ContextCompat.getColor(this, R.color.color_7F7F7F_999999))
            .setCancelColor(ContextCompat.getColor(this, R.color.color_7F7F7F_999999))
            .isDialog(false)
            .build() as TimePickerView
        return dateTimePicker
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (supportFragmentManager.fragments.isEmpty()) {
            binding.flCredentials.visibility = View.GONE
        }
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
                    selectedFirstPhotoImg(compressFile)
                else
                    throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(
                    this@RegisterOkActivity,
                    getString(R.string.error_reading_file)
                )
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
                    selectedSecondPhotoImg(compressFile)
                else
                    throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(
                    this@RegisterOkActivity,
                    getString(R.string.error_reading_file)
                )
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }
    }

    private fun selectedFirstPhotoImg(file: File) {
        firstFile = file
        if (firstFile != null) {
            //binding.endButton.setImageResource(R.drawable.ic_upload_done)
            viewModel.checkIdentity(firstFile)
            binding.etIdentity.setHintText(getString(R.string.hint_file_selected))
            isUploaded = true
        } else {
            //binding.endButton.setImageResource(R.drawable.ic_camera)
            viewModel.checkIdentity(firstFile)
            binding.etIdentity.setHintText(getString(R.string.hint_no_file_selected))
            isUploaded = false
        }
    }

    private fun selectedSecondPhotoImg(file: File) {
        secondFile = file
        if (secondFile != null) {
            //binding.endButton2.setImageResource(R.drawable.ic_upload_done)
            viewModel.checkBackupIdentity(secondFile)
            etIdentity2.setHintText(getString(R.string.hint_file_selected))
            isUploaded = true
        } else {
            //binding.endButton2.setImageResource(R.drawable.ic_camera)
            viewModel.checkBackupIdentity(secondFile)
            etIdentity2.setHintText(getString(R.string.hint_no_file_selected))
            isUploaded = false
        }
    }

    private fun etBettingShopSelectTrue() {
        binding.etBettingShop.setEndIcon(R.drawable.ic_arrow_gray)
        binding.bettingShopSpinner.isEnabled = true
        binding.bettingShopSpinner.isClickable = true
        binding.etBettingShop.isEnabled = true
        binding.etBettingShop.isClickable = true
        binding.eetBettingShop.setText(bettingShopSelectedData?.showName)
        binding.eetBettingShop.setTextColor(getColor(R.color.color_FFFFFF_DE000000))
    }

    private fun etBettingShopSelectFalse(eetBetting: String) {
        binding.etBettingShop.setEndIcon(null)
        binding.bettingShopSpinner.isEnabled = false
        binding.bettingShopSpinner.isClickable = false

        binding.etBettingShop.isEnabled = false
        binding.etBettingShop.isClickable = false

        binding.etBettingShop.hasFocus = false
        binding.eetBettingShop.setText(eetBetting)
        binding.eetBettingShop.setTextColor(getColor(R.color.color_AFAFB1))
    }

}
