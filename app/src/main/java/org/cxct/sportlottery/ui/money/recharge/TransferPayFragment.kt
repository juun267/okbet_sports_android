package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.appsflyer.AppsFlyerLib
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.crypto_pay_fragment.*
import kotlinx.android.synthetic.main.dialog_bet_record_detail_list.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_icon_and_tick.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.transfer_pay_fragment.*
import kotlinx.android.synthetic.main.transfer_pay_fragment.btn_submit
import kotlinx.android.synthetic.main.transfer_pay_fragment.cv_recharge_time
import kotlinx.android.synthetic.main.transfer_pay_fragment.et_recharge_amount
import kotlinx.android.synthetic.main.transfer_pay_fragment.ll_qr


import kotlinx.android.synthetic.main.transfer_pay_fragment.tv_fee_amount
import kotlinx.android.synthetic.main.transfer_pay_fragment.tv_fee_rate

import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.common.RechType
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.ui.profileCenter.profile.RechargePicSelectorDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.MoneyManager.getBankAccountIcon
import org.cxct.sportlottery.util.MoneyManager.getBankIconByBankName
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.math.abs

/**
 * @app_destination 存款-转账支付  //存款时间格式需要修改
 */
class TransferPayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    private var mMoneyPayWay: MoneyPayWayData? = null //支付類型

    private var mSelectRechCfgs: RechCfg? = null //選擇的入款帳號

    private val mBottomSheetList = mutableListOf<BtsRvAdapter.SelectBank>()

    private var rechCfgsList = mutableListOf<RechCfg>()

    private lateinit var bankBottomSheet: BottomSheetDialog

    private lateinit var bankCardAdapter: BtsRvAdapter

    private var bankPosition = 0
    private var imgResultUrl: String? = null
    private lateinit var dateTimePicker: TimePickerView
    private lateinit var dateTimePickerHMS: TimePickerView

    var depositDate = Date()
    var depositDate2 = Date()
    var mCalendar: Calendar =Calendar.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.transfer_pay_fragment, container, false)
    }

    fun setArguments(moneyPayWay: MoneyPayWayData?): TransferPayFragment {
        mMoneyPayWay = moneyPayWay
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPayAccountBottomSheet()
        initView()
        initButton()
        initObserve()
        setPayBankBottomSheet()
    }

    private fun initButton() {
        //提交
        btn_submit.setTitleLetterSpacing()
        btn_submit.setOnClickListener {
            createMoneyAddRequest()?.let {
                viewModel.rechargeSubmit(
                    it,
                    mMoneyPayWay?.rechType,
                    mSelectRechCfgs
                )
            }
        }

        //選取日曆
       /* cv_recharge_time.setOnClickListener {
            dateTimePicker.show()
        }*/
        //年月日的时间选择器
        ll_transfer_time.setOnClickListener {
            dateTimePicker.show()
        }
        //时分秒的选择器
        ll_transfer_time2.setOnClickListener {
            dateTimePickerHMS.show()
        }

        bt_check_files.setOnClickListener {
            this.activity?.let { activity ->
                activity.supportFragmentManager.let { fragmentManager ->
                    RechargePicSelectorDialog(
                        activity,
                        mSelectMediaListener,
                        RechargePicSelectorDialog.CropType.SQUARE
                    ).show(
                        fragmentManager, null
                    )
                }
            }
        }
        //複製姓名
        btn_name_copy.setOnClickListener {
            val clipboard =
                activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText(null, tv_name.text)
            clipboard?.setPrimaryClip(clipData)
            ToastUtil.showToastInCenter(activity, getString(R.string.text_money_copy_success))
        }

        //複製帳號
        btn_account_copy.setOnClickListener {
            val clipboard =
                activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText(null, tv_account.text)
            clipboard?.setPrimaryClip(clipData)
            ToastUtil.showToastInCenter(activity, getString(R.string.text_money_copy_success))
        }

        //選擇銀行
        cv_bank.setOnClickListener {
            bankBottomSheet.show()
        }

        tv_customer_service.setServiceClick(childFragmentManager)
    }

    private fun initView() {
        setupTextChangeEvent()
        setupFocusEvent()
        initTimePicker()
        initTimePickerForHMS()
        getMoney()
        updateMoneyRange()
        getBankType(0)
        refreshFieldTitle()
        tv_currency_type.text = sConfigData?.systemCurrencySign

    }

    private fun refreshFieldTitle() {
        if (mMoneyPayWay?.rechType == RechType.BANKTRANSFER.code)
            tv_pay_type.text = String.format(resources.getString(R.string.title_bank))
        else
            tv_pay_type.text = String.format(resources.getString(R.string.title_main_account))
    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {

        //充值金額訊息
        viewModel.rechargeAmountMsg.observe(viewLifecycleOwner) {
            et_recharge_amount.setError(it)
        }
        //微信
        viewModel.wxErrorMsg.observe(viewLifecycleOwner) {
            et_wx_id.setError(it)
        }
        //認證姓名
        viewModel.nameErrorMsg.observe(viewLifecycleOwner) {
            et_name.setError(it)
        }
        //銀行卡號
        viewModel.bankIDErrorMsg.observe(viewLifecycleOwner) {
            et_bank_account.setError(it)
        }
        //暱稱
        viewModel.nickNameErrorMsg.observe(viewLifecycleOwner) {
            et_nickname.setError(it)
        }
        viewModel.userMoney.observe(viewLifecycleOwner) {
            txv_wallet_money.text =
                "${sConfigData?.systemCurrencySign} ${ArithUtil.toMoneyFormat(it)} "
        }

        viewModel.transferPayResult.observe(viewLifecycleOwner) {
            if (it.success) {
                resetEvent()
                getBankType(0)
            }
        }
        viewModel.uploadPayResult.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let { upload->
                if (upload.success){
                    var lastIndexOf = upload.imgData?.path?.lastIndexOf("/")
                    imgResultUrl = upload.imgData?.path
                    tv_hint_upload.setText(lastIndexOf?.let { it1 ->
                        upload.imgData?.path?.substring(
                            it1+1, upload.imgData?.path?.length)
                    })
                }else{
                    context?.let { it1 -> SingleToast.showSingleToast(it1,false,LocalUtils.getString(R.string.upload_fail),0) }
                }
            }
        }
        //转账充值首充提示
        viewModel.onlinePayFirstRechargeTips.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { tipString ->
                showPromptDialog(getString(R.string.prompt), tipString) {}
            }
        }
    }

    private fun setPayBankBottomSheet() {
        try {

            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val bottomSheetView =
                layoutInflater.inflate(
                    R.layout.dialog_bottom_sheet_icon_and_tick,
                    contentView,
                    false
                )
            bankBottomSheet = BottomSheetDialog(this.requireContext())
            bankBottomSheet.apply {
                setContentView(bottomSheetView)

                bankCardAdapter = BtsRvAdapter(
                    mBottomSheetList,
                    BtsRvAdapter.BankAdapterListener { _, position ->
                        bankPosition = position
                        //更新銀行
                        getBankType(position)
                        resetEvent()
                        dismiss()
                    })
                rv_bank_item.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                rv_bank_item.adapter = bankCardAdapter


                if (mMoneyPayWay?.rechType == RechType.BANKTRANSFER.code)
                    tv_game_type_title.text =
                        String.format(resources.getString(R.string.title_bank))
                else
                    tv_game_type_title.text =
                        String.format(resources.getString(R.string.title_main_account))

                bankBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //重置畫面事件
    private fun resetEvent() {
        clearFocus()
        et_recharge_amount.setText("")
        et_wx_id.setText("")
        et_name.setText("")
        et_bank_account.setText("")
        et_nickname.setText("")

        viewModel.clearnRechargeStatus()
    }

    //入款帳號選單
    private fun initPayAccountBottomSheet() {
        //支付類型的入款帳號清單
        rechCfgsList = (viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType
        } ?: mutableListOf()) as MutableList<RechCfg>

        var count = 1


        if (mMoneyPayWay?.rechType == RechType.BANKTRANSFER.code) //銀行卡轉帳 顯示銀行名稱，不用加排序數字
            rechCfgsList.forEach {
                val selectBank = BtsRvAdapter.SelectBank(
                    it.rechName.toString(),
                    getBankIconByBankName(it.rechName.toString())
                )
                mBottomSheetList.add(selectBank)
            }
        else {
            if (rechCfgsList.size > 0)//對照H5改為全部都要加數字
                rechCfgsList.forEach {
                    val selectBank =
                        BtsRvAdapter.SelectBank(
                            viewModel.getPayTypeName(it.rechType) + " " + count++,
                            getBankAccountIcon(it.rechType ?: "")
                        )
                    mBottomSheetList.add(selectBank)
                }
            else
                rechCfgsList.forEach {
                    val selectBank =
                        BtsRvAdapter.SelectBank(
                            viewModel.getPayTypeName(it.rechType) + "",
                            getBankAccountIcon(it.rechType ?: "")
                        )
                    mBottomSheetList.add(selectBank)
                }
        }
    }

    //充值時間
    private fun initTimePicker() {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -30)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, +30)
        dateTimePicker = TimePickerBuilder(activity,
            OnTimeSelectListener { date, _ ->
                try {
                    depositDate = date
                    txv_transfer_time.text = TimeUtil.dateToStringFormatYMD(date)
                    upDataTimeYMD()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
            .setLabel("", "", "", "", "", "")
            .setRangDate(yesterday, tomorrow)
            .setDate(Calendar.getInstance())
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setTitleText(resources.getString(R.string.title_recharge_time))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setTitleColor(
                ContextCompat.getColor(
                    cv_recharge_time.context,
                    R.color.color_CCCCCC_000000
                )
            )
            .setTitleBgColor(
                ContextCompat.getColor(
                    cv_recharge_time.context,
                    R.color.color_2B2B2B_e2e2e2
                )
            )
            .setBgColor(
                ContextCompat.getColor(
                    cv_recharge_time.context,
                    R.color.color_191919_FCFCFC
                )
            )
            .setSubmitColor(
                ContextCompat.getColor(
                    cv_recharge_time.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    cv_recharge_time.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .isDialog(false)
            .build() as TimePickerView
    }
    private fun initTimePickerForHMS() {

        dateTimePickerHMS = TimePickerBuilder(activity) { date, _ ->
            try {
                depositDate2 = date
//                txv_recharge_time2.text = TimeUtil.dateToStringFormatHMS(date)
                txv_transfer_time2.text = TimeUtil.dateToStringFormatHMS(date)
                upDataTimeHMS()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            .setLabel("", "", "", "", "", "")
            .setDate(Calendar.getInstance())
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(false, false, false, true, true, true))
            .setTitleText(resources.getString(R.string.title_recharge_time))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setSubmitColor(
                ContextCompat.getColor(
                    txv_transfer_time.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    txv_transfer_time2.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .isDialog(false)
            .build() as TimePickerView
    }
    //依據選擇的支付渠道，刷新UI
    private fun refreshSelectRechCfgs(selectRechCfgs: RechCfg?) {
        //姓名
        tv_name.text = selectRechCfgs?.payeeName

        //帳號
        tv_account.text = selectRechCfgs?.payee

        //地址QR code
        if (selectRechCfgs?.qrCode.isNullOrEmpty()) {
            iv_address.visibility = View.GONE
        } else {
            iv_address.visibility = View.VISIBLE
            Glide.with(this).load(selectRechCfgs?.qrCode).into(iv_address)
        }


        //銀行卡轉帳 UI 特別處理
        if (mMoneyPayWay?.rechType == RechType.BANKTRANSFER.code) {
            ll_remark.visibility = View.GONE
            ll_qr.visibility = View.GONE
            ll_address.visibility = View.VISIBLE
            et_wx_id.visibility = View.GONE

            //銀行備註放在地址
            tv_address.text = selectRechCfgs?.remark
        } else {
            ll_remark.visibility = View.VISIBLE
            ll_qr.visibility = View.VISIBLE
            ll_address.visibility = View.GONE

            //備註
            tv_remark.text = selectRechCfgs?.remark
        }
        when (mMoneyPayWay?.rechType) {
            MoneyType.BANK_TYPE.code -> {
                hideEditText()
                ll_address.visibility = View.VISIBLE
                et_bank_account.visibility = View.VISIBLE
                et_name.visibility = View.VISIBLE
                ll_hit2.visibility = View.VISIBLE

                tv_hint1.text = getString(R.string.money_recharge_hint1)
                tv_hint2.text = getString(R.string.money_recharge_hint2)
            }
            MoneyType.CTF_TYPE.code -> {
                hideEditText()
                ll_qr.visibility = View.VISIBLE
                et_bank_account.visibility = View.VISIBLE
                et_name.visibility = View.VISIBLE
                tv_dio.isVisible = false
                tv_hint1.text = getString(R.string.cft_recharge_hint)
            }
            MoneyType.WX_TYPE.code -> {
                hideEditText()
                ll_qr.visibility = View.VISIBLE
                et_wx_id.visibility = View.VISIBLE
                tv_dio.isVisible = false
                tv_hint1.text = getString(R.string.wx_recharge_hint)

            }
            MoneyType.ALI_TYPE.code -> {
                hideEditText()
                ll_qr.visibility = View.VISIBLE
                et_nickname.visibility = View.VISIBLE
                et_name.visibility = View.VISIBLE
                tv_dio.isVisible = false
                tv_hint1.text = getString(R.string.ali_recharge_hint)
            }
            MoneyType.GCASH_TYPE.code -> {
                hideEditText()
                ll_qr.visibility = View.VISIBLE
                et_nickname.visibility = View.GONE
                et_name.visibility = View.VISIBLE
                tv_dio.isVisible = false
                tv_hint1.text = getString(R.string.gcash_recharge_hint)
            }
            MoneyType.GRABPAY_TYPE.code -> {
                hideEditText()
                ll_qr.visibility = View.VISIBLE
                et_nickname.visibility = View.GONE
                et_name.visibility = View.VISIBLE
                tv_dio.isVisible = false
                tv_hint1.text = getString(R.string.grabpay_recharge_hint)
            }
            MoneyType.PAYMAYA_TYPE.code -> {
                hideEditText()
                ll_qr.visibility = View.VISIBLE
                et_nickname.visibility = View.GONE
                et_name.visibility = View.VISIBLE
                tv_dio.isVisible = false
                tv_hint1.text = getString(R.string.paymaya_recharge_hint)
            }
        }

        //反利、手續費
        val rebateFee = mSelectRechCfgs?.rebateFee
        if (rebateFee == null || rebateFee == 0.0) {
            ll_fee_rate.visibility = View.GONE
            ll_fee_amount.visibility = View.GONE
        } else {
            ll_fee_rate.visibility = View.VISIBLE
            ll_fee_amount.visibility = View.VISIBLE
            if (rebateFee < 0.0) {
                title_fee_rate.text = getString(R.string.title_fee_rate)
                title_fee_amount.text = getString(R.string.title_fee_amount)
            } else {
                title_fee_rate.text = getString(R.string.title_rebate_rate)
                title_fee_amount.text = getString(R.string.title_rebate_amount)
            }
            tv_fee_rate.text = ArithUtil.toOddFormat(abs(rebateFee).times(100))
            tv_fee_amount.text = ArithUtil.toOddFormat(0.0.times(100))
        }

        //存款時間
        txv_transfer_time.text = TimeUtil.timeFormat(Date().time,TimeUtil.YMD_FORMAT)
        txv_transfer_time2.text = TimeUtil.dateToStringFormatHMS(Date())

    }

    private fun hideEditText() {
        ll_qr.visibility = View.GONE
        ll_address.visibility = View.GONE
        et_wx_id.visibility = View.GONE
        et_nickname.visibility = View.GONE
        et_bank_account.visibility = View.GONE
        et_name.visibility = View.GONE
        ll_hit2.visibility = View.GONE
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //充值金額
            et_recharge_amount.afterTextChanged {
                if (it.startsWith("0") && it.length > 1) {
                    et_recharge_amount.setText(et_recharge_amount.getText().replace("0", ""))
                    et_recharge_amount.setCursor()
                    return@afterTextChanged
                }

                if (et_recharge_amount.getText().length > 6) {
                    et_recharge_amount.setText(et_recharge_amount.getText().substring(0, 6))
                    et_recharge_amount.setCursor()
                    return@afterTextChanged
                }

                checkRechargeAmount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
                    tv_fee_amount.text = ArithUtil.toMoneyFormat(0.0)
                } else {
                    tv_fee_amount.text = ArithUtil.toMoneyFormat(
                        it.toDouble().times(abs(mSelectRechCfgs?.rebateFee ?: 0.0))
                    )
                }
            }
            //微信
            et_wx_id.afterTextChanged { checkWX(it) }
            //認證姓名
            et_name.afterTextChanged {
                when (mMoneyPayWay?.rechType) {
                    MoneyType.BANK_TYPE.code -> {
                        checkUserName(MoneyType.BANK_TYPE.code, it)
                    }
                    MoneyType.CTF_TYPE.code -> {
                        checkUserName(MoneyType.CTF_TYPE.code, it)
                    }
                    MoneyType.ALI_TYPE.code, MoneyType.GCASH_TYPE.code, MoneyType.GRABPAY_TYPE.code, MoneyType.PAYMAYA_TYPE.code -> {
                        checkUserName(MoneyType.ALI_TYPE.code, it)
                    }
                }
            }
            //認證銀行卡號
            et_bank_account.afterTextChanged { checkBankID(it) }
            //暱稱
            et_nickname.afterTextChanged { checkNickName(it) }
        }
    }

    private fun setupFocusEvent() {
        viewModel.apply {
            //充值金額
            setupEditTextFocusEvent(et_recharge_amount) { checkRechargeAmount(it, mSelectRechCfgs) }
            //微信
            setupEditTextFocusEvent(et_wx_id) { checkWX(it) }
            //認證姓名
            setupEditTextFocusEvent(et_name) {
                when (mMoneyPayWay?.rechType) {
                    MoneyType.BANK_TYPE.code -> {
                        checkUserName(MoneyType.BANK_TYPE.code, it)
                    }
                    MoneyType.CTF_TYPE.code -> {
                        checkUserName(MoneyType.CTF_TYPE.code, it)
                    }
                    MoneyType.ALI_TYPE.code, MoneyType.GCASH_TYPE.code, MoneyType.GRABPAY_TYPE.code, MoneyType.PAYMAYA_TYPE.code -> {
                        checkUserName(MoneyType.ALI_TYPE.code, it)
                    }
                }
            }
            //認證銀行卡號
            setupEditTextFocusEvent(et_bank_account) { checkBankID(it) }
            //暱稱
            setupEditTextFocusEvent(et_nickname) { checkNickName(it) }
        }
    }

    private fun setupEditTextFocusEvent(customEditText: LoginEditText, event: (String) -> Unit) {
        customEditText.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                event.invoke(customEditText.et_input.text.toString())
        }
    }

    //取得餘額
    private fun getMoney() {
        viewModel.getMoney()
    }

    //修改hint
    private fun updateMoneyRange() {
        et_recharge_amount.setHint(
            String.format(
                getString(R.string.edt_hint_deposit_money), sConfigData?.systemCurrencySign,
                TextUtil.formatBetQuota(
                    ArithUtil.toMoneyFormatForHint(
                        mSelectRechCfgs?.minMoney ?: 0.00
                    ).toInt()
                ),
                TextUtil.formatBetQuota(
                    ArithUtil.toMoneyFormatForHint(
                        mSelectRechCfgs?.maxMoney ?: 999999.00
                    ).toInt()
                )
            )
        )
    }

    //創建MoneyAddRequest
    private fun createMoneyAddRequest(): MoneyAddRequest? {
        return when (mMoneyPayWay?.rechType) {
            MoneyType.BANK_TYPE.code, MoneyType.CTF_TYPE.code -> {
                MoneyAddRequest(
                    rechCfgId = mSelectRechCfgs?.id ?: 0,
                    bankCode = mBottomSheetList[bankPosition].bankName.toString(),
                    depositMoney = if (et_recharge_amount.getText().isNotEmpty()) {
                        et_recharge_amount.getText()
                    } else {
                        ""
                    },
                    payer = et_bank_account.getText(),
                    payerName = et_name.getText(),
                    payerBankName = mBottomSheetList[bankPosition].bankName.toString(),
                    payerInfo = "",
                    depositDate = mCalendar.time.time,
                    appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext())
                ).apply {
                    proofImg = imgResultUrl
                }
            }
            MoneyType.WX_TYPE.code, MoneyType.GCASH_TYPE.code, MoneyType.GRABPAY_TYPE.code, MoneyType.PAYMAYA_TYPE.code -> {
                MoneyAddRequest(
                    rechCfgId = mSelectRechCfgs?.id ?: 0,
                    bankCode = null,
                    depositMoney = if (et_recharge_amount.getText().isNotEmpty()) {
                        et_recharge_amount.getText()
                    } else {
                        ""
                    },
                    payer = null,
                    payerName = et_wx_id.getText(),
                    payerBankName = null,
                    payerInfo = null,
                    depositDate = mCalendar.time.time,
                    appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext())
                ).apply {
                    proofImg = imgResultUrl
                }
            }
            MoneyType.ALI_TYPE.code -> {
                MoneyAddRequest(
                    rechCfgId = mSelectRechCfgs?.id ?: 0,
                    bankCode = null,
                    depositMoney = if (et_recharge_amount.getText().isNotEmpty()) {
                        et_recharge_amount.getText()
                    } else {
                        ""
                    },
                    payer = null,
                    payerName = et_nickname.getText(),
                    payerBankName = null,
                    payerInfo = et_name.getText(),
                    depositDate = mCalendar.time.time,
                    appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext())
                ).apply {
                    proofImg = imgResultUrl
                }
            }
            else -> null
        }
    }

    private fun getBankType(position: Int) {
        if (rechCfgsList.size > 0) {
            mSelectRechCfgs = rechCfgsList[position]
            refreshSelectRechCfgs(mSelectRechCfgs)
            getMoney()
            updateMoneyRange()

            iv_bank_icon.setImageResource(mBottomSheetList[position].bankIcon ?: 0)
            txv_pay_bank.text = mBottomSheetList[position].bankName.toString()
        } else {
            mSelectRechCfgs = null
        }
    }

    private fun upDataTimeHMS(){
        mCalendar.apply {
            var despsitCal=Calendar.getInstance().apply {
                time=depositDate2
            }
            mCalendar.set(Calendar.HOUR_OF_DAY,despsitCal.get(Calendar.HOUR_OF_DAY))
            mCalendar.set(Calendar.MINUTE,despsitCal.get(Calendar.MINUTE))
            mCalendar.set(Calendar.SECOND,despsitCal.get(Calendar.SECOND))

        }
    }
    private fun upDataTimeYMD(){
        mCalendar.apply {
            var despsitCal=Calendar.getInstance().apply {
                time=depositDate
            }
            mCalendar.set(Calendar.DAY_OF_MONTH,despsitCal.get(Calendar.DAY_OF_MONTH))
            mCalendar.set(Calendar.MONTH,despsitCal.get(Calendar.MONTH))
            mCalendar.set(Calendar.YEAR,despsitCal.get(Calendar.YEAR))

        }
    }
    //选择图片回调监听
    private val mSelectMediaListener = object : OnResultCallbackListener<LocalMedia> {
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
                    else -> media?.realPath
                }
                LogUtil.d(path)
                val file = File(path!!)
                val imageType = FileUtil.getImageType(path)
                val fileSize = FileUtil.getFilesSizeByType(path,2)
                if (imageType!="jpeg"&&imageType!="png"&&imageType!="jpg"){
                    //弹出类型错误的弹窗
                    context?.let { it1 -> SingleToast.showSingleToast(it1,false,LocalUtils.getString(R.string.format_error),0) }
                    return
                }
                if (fileSize>2.0){
                    //弹出文件过大的弹窗
                    context?.let { it1 -> SingleToast.showSingleToast(it1,false,LocalUtils.getString(R.string.over_size),0) }
                    return
                }
                if (file.exists())
                    uploadImg(file)
                    else throw FileNotFoundException()


            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(activity, getString(R.string.error_reading_file))
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }
    }

    //上传凭证接口
    private fun uploadImg(file: File) {
        val userId = viewModel.loginRepository.userId.toString()
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.VOUCHER)
        viewModel.uploadImage(uploadImgRequest)
    }
}