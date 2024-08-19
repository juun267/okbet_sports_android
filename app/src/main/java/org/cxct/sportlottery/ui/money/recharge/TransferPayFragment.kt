package org.cxct.sportlottery.ui.money.recharge


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.appsflyer.AppsFlyerLib
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.DialogBottomSheetIconAndTickBinding
import org.cxct.sportlottery.databinding.TransferPayFragmentBinding
import org.cxct.sportlottery.net.money.data.DailyConfig
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.common.RechType
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.money.recharge.dialog.DepositHintDialog
import org.cxct.sportlottery.ui.money.recharge.dialog.RechargePromotionsDialog
import org.cxct.sportlottery.view.LoginEditText
import org.cxct.sportlottery.ui.profileCenter.profile.RechargePicSelectorDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.MoneyManager.getBankAccountIcon
import org.cxct.sportlottery.util.MoneyManager.getBankIconByBankName
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * @app_destination 存款-转账支付  //存款时间格式需要修改
 */
class TransferPayFragment : BaseFragment<MoneyRechViewModel, TransferPayFragmentBinding>()
    , RechargePromotionsDialog.OnSelectListener, DepositHintDialog.ConfirmListener  {

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

    private var depositDate = Date()
    private var depositDate2 = Date()
    private var mCalendar: Calendar =Calendar.getInstance()
    private val dailyConfigAdapter = DailyConfigAdapter{
        updateDailyConfigSelect()
    }

    fun setArguments(moneyPayWay: MoneyPayWayData?): TransferPayFragment {
        mMoneyPayWay = moneyPayWay
        return this
    }
    override fun onInitView(view: View) {
        initPayAccountBottomSheet()
        initView()
        initButton()
        initObserve()
        setPayBankBottomSheet()
        setupQuickMoney()
        setUpPayWaysLayout()
        viewModel.getDailyConfig()
    }

    private fun setUpPayWaysLayout() {
        (activity as? MoneyRechargeActivity)?.fillPayWaysLayoutTo(binding.content, 0)
    }

    private fun initButton() = binding.run {
        //提交
        btnSubmit.setTitleLetterSpacing()
        btnSubmit.setOnClickListener {
            if (viewModel.uniPaid) {
                DepositHintDialog.show(this@TransferPayFragment)
            } else {
                submitForm()
            }
        }

        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,linMaintenance) }

        //選取日曆
       /* cv_recharge_time.setOnClickListener {
            dateTimePicker.show()
        }*/
        //年月日的时间选择器
        llTransferTime.setOnClickListener {
            dateTimePicker.show()
        }
        //时分秒的选择器
        llTransferTime2.setOnClickListener {
            dateTimePickerHMS.show()
        }

        btCheckFiles.setOnClickListener {
                val dialog = RechargePicSelectorDialog()
                dialog.mSelectListener = mSelectMediaListener
                dialog.show(childFragmentManager, null)
        }
        //複製姓名
        btnNameCopy.setOnClickListener {
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText(null, tvName.text)
            clipboard?.setPrimaryClip(clipData)
            ToastUtil.showToastInCenter(activity, getString(R.string.text_money_copy_success))
        }

        //複製帳號
        btnAccountCopy.setOnClickListener {
            val clipboard =
                activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText(null, tvAccount.text)
            clipboard?.setPrimaryClip(clipData)
            ToastUtil.showToastInCenter(activity, getString(R.string.text_money_copy_success))
        }

        //選擇銀行
        cvBank.setOnClickListener {
            bankBottomSheet.show()
        }

        tvCustomerService.setServiceClick(childFragmentManager)
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
        initViewMorePromotions()
        binding.tvCurrencyType.text = "${sConfigData?.systemCurrencySign}"
    }

    private fun initViewMorePromotions() {
        val icRight = ContextCompat.getDrawable(context(), R.drawable.ic_right)!!
        DrawableCompat.setTint(icRight.mutate(), context().getColor(R.color.color_025BE8))
        val tvViewMore = binding.linFirstDeposit.tvViewMore
        tvViewMore.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icRight, null)
        tvViewMore.setOnClickListener {
            RechargePromotionsDialog.show(this, dailyConfigAdapter.data as ArrayList<DailyConfig>, dailyConfigAdapter.getSelectedItem())
        }
    }

    private fun refreshFieldTitle() {
        if (mMoneyPayWay?.rechType == RechType.BANKTRANSFER.code)
            binding.tvPayType.text = String.format(resources.getString(R.string.title_bank))
        else
            binding.tvPayType.text = String.format(resources.getString(R.string.title_main_account))
    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {

        //充值金額訊息
        viewModel.rechargeAmountMsg.observe(this) {
            binding.etRechargeAmount.setError(it)
        }
        //微信
        viewModel.wxErrorMsg.observe(this) {
            binding.etWxId.setError(it)
        }
        //認證姓名
        viewModel.nameErrorMsg.observe(this) {
            binding.etName.setError(it)
        }
        //銀行卡號
        viewModel.bankIDErrorMsg.observe(this) {
            binding.etBankAccount.setError(it)
        }
        //暱稱
        viewModel.nickNameErrorMsg.observe(this) {
            binding.etNickname.setError(it)
        }
        viewModel.userMoney.observe(this) {
            binding.txvWalletMoney.text =
                "${sConfigData?.systemCurrencySign} ${ArithUtil.toMoneyFormat(it)} "
        }

        viewModel.transferPayResult.observe(this) {
            if (it.success) {
                resetEvent()
                hideFirstDesposit()
                getBankType(0)
            }
        }
        viewModel.uploadPayResult.observe(this){
            it.getContentIfNotHandled()?.let { upload->
                if (upload.success){
                    var lastIndexOf = upload.imgData?.path?.lastIndexOf("/")
                    imgResultUrl = upload.imgData?.path
                    binding.tvHintUpload.setText(lastIndexOf?.let { it1 ->
                        upload.imgData?.path?.substring(
                            it1+1, upload.imgData?.path?.length)
                    })
                }else{
                    context?.let { it1 -> SingleToast.showSingleToast(it1,false,LocalUtils.getString(R.string.upload_fail),0) }
                }
            }
        }
        //转账充值首充提示
        viewModel.onlinePayFirstRechargeTips.observe(this) { event ->
            event.getContentIfNotHandled()?.let { tipString ->
                showPromptDialog(getString(R.string.prompt), tipString) {}
            }
        }
        viewModel.dailyConfigEvent.observe(this){
            initFirstDeposit(it)
        }
    }

    private fun setPayBankBottomSheet() {
        try {

            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val dialogBinding = DialogBottomSheetIconAndTickBinding.inflate(layoutInflater,contentView,false)

            bankBottomSheet = BottomSheetDialog(this.requireContext())
            bankBottomSheet.apply {
                setContentView(dialogBinding.root)

                bankCardAdapter = BtsRvAdapter(
                    mBottomSheetList,
                    BtsRvAdapter.BankAdapterListener { _, position ->
                        bankPosition = position
                        //更新銀行
                        getBankType(position)
                        resetEvent()
                        dismiss()
                    })
                dialogBinding.rvBankItem.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                dialogBinding.rvBankItem.adapter = bankCardAdapter


                if (mMoneyPayWay?.rechType == RechType.BANKTRANSFER.code)
                    dialogBinding.tvGameTypeTitle.text =
                        String.format(resources.getString(R.string.title_bank))
                else
                    dialogBinding.tvGameTypeTitle.text =
                        String.format(resources.getString(R.string.title_main_account))

                dialogBinding.btnClose.setOnClickListener {
                    this.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //重置畫面事件
    private fun resetEvent() = binding.run{
        clearFocus()
        etRechargeAmount.setText("")
        etWxId.setText("")
        etName.setText("")
        etBankAccount.setText("")
        etNickname.setText("")
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
                    binding.txvTransferTime.text = TimeUtil.dateToStringFormatYMD(date)
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
                    requireContext(),
                    R.color.color_CCCCCC_000000
                )
            )
            .setTitleBgColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_2B2B2B_e2e2e2
                )
            )
            .setBgColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_191919_FCFCFC
                )
            )
            .setSubmitColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    requireContext(),
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
                binding.txvTransferTime2.text = TimeUtil.dateToStringFormatHMS(date)
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
                    requireContext(),
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_7F7F7F_999999
                )
            )
            .isDialog(false)
            .build() as TimePickerView
    }
    //依據選擇的支付渠道，刷新UI
    private fun refreshSelectRechCfgs(selectRechCfgs: RechCfg?) =binding.run {
        //姓名
        tvName.text = selectRechCfgs?.payeeName

        //帳號
        tvAccount.text = selectRechCfgs?.payee

        //地址QR code
        if (selectRechCfgs?.qrCode.isNullOrEmpty()) {
            ivAddress.visibility = View.GONE
        } else {
            ivAddress.visibility = View.VISIBLE
            ivAddress.load(selectRechCfgs?.qrCode)
        }


        //銀行卡轉帳 UI 特別處理
        if (mMoneyPayWay?.rechType == RechType.BANKTRANSFER.code) {
            llRemark.visibility = View.GONE
            llQr.visibility = View.GONE
            llAddress.visibility = View.VISIBLE
            etWxId.visibility = View.GONE

            //銀行備註放在地址
            tvAddress.text = selectRechCfgs?.remark
        } else {
            llRemark.visibility = View.VISIBLE
            llQr.visibility = View.VISIBLE
            llAddress.visibility = View.GONE

            //備註
            tvRemark.text = selectRechCfgs?.remark
        }
        when (mMoneyPayWay?.rechType) {
            MoneyType.BANK_TYPE.code -> {
                hideEditText()
                llAddress.visibility = View.VISIBLE
                etBankAccount.visibility = View.VISIBLE
                etName.visibility = View.VISIBLE
                llHit2.visibility = View.VISIBLE

                tvHint1.text = getString(R.string.money_recharge_hint1)
                tvHint2.text = getString(R.string.money_recharge_hint2)
            }
            MoneyType.CTF_TYPE.code -> {
                hideEditText()
                llQr.visibility = View.VISIBLE
                etBankAccount.visibility = View.VISIBLE
                etName.visibility = View.VISIBLE
                tvDio.isVisible = false
                tvHint1.text = getString(R.string.cft_recharge_hint)
            }
            MoneyType.WX_TYPE.code -> {
                hideEditText()
                llQr.visibility = View.VISIBLE
                etWxId.visibility = View.VISIBLE
                tvDio.isVisible = false
                tvHint1.text = getString(R.string.wx_recharge_hint)

            }
            MoneyType.ALI_TYPE.code -> {
                hideEditText()
                llQr.visibility = View.VISIBLE
                etNickname.visibility = View.VISIBLE
                etName.visibility = View.VISIBLE
                tvDio.isVisible = false
                tvHint1.text = getString(R.string.ali_recharge_hint)
            }
            MoneyType.GCASH_TYPE.code -> {
                hideEditText()
                llQr.visibility = View.VISIBLE
                etNickname.visibility = View.GONE
                etName.visibility = View.VISIBLE
                tvDio.isVisible = false
                tvHint1.text = getString(R.string.gcash_recharge_hint)
            }
            MoneyType.GRABPAY_TYPE.code -> {
                hideEditText()
                llQr.visibility = View.VISIBLE
                etNickname.visibility = View.GONE
                etName.visibility = View.VISIBLE
                tvDio.isVisible = false
                tvHint1.text = getString(R.string.grabpay_recharge_hint)
            }
            MoneyType.PAYMAYA_TYPE.code -> {
                hideEditText()
                llQr.visibility = View.VISIBLE
                etNickname.visibility = View.GONE
                etName.visibility = View.VISIBLE
                tvDio.isVisible = false
                tvHint1.text = getString(R.string.paymaya_recharge_hint)
            }
        }

        //反利、手續費
        val rebateFee = mSelectRechCfgs?.rebateFee
        if (rebateFee == null || rebateFee == 0.0) {
            llFeeRate.visibility = View.GONE
            llFeeAmount.visibility = View.GONE
        } else {
            llFeeRate.visibility = View.VISIBLE
            llFeeAmount.visibility = View.VISIBLE
            if (rebateFee < 0.0) {
                titleFeeRate.text = getString(R.string.title_fee_rate)
                titleFeeAmount.text = getString(R.string.title_fee_amount)
            } else {
                titleFeeRate.text = getString(R.string.title_rebate_rate)
                titleFeeAmount.text = getString(R.string.title_rebate_amount)
            }
            tvFeeRate.text = ArithUtil.toOddFormat(abs(rebateFee).times(100))
            tvFeeAmount.text = ArithUtil.toOddFormat(0.0.times(100))
        }

        //存款時間
        txvTransferTime.text = TimeUtil.timeFormat(Date().time,TimeUtil.YMD_FORMAT)
        txvTransferTime2.text = TimeUtil.dateToStringFormatHMS(Date())
        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,linMaintenance) }
    }

    private fun hideEditText()=binding.run {
        llQr.visibility = View.GONE
        llAddress.visibility = View.GONE
        etWxId.visibility = View.GONE
        etNickname.visibility = View.GONE
        etBankAccount.visibility = View.GONE
        etName.visibility = View.GONE
        llHit2.visibility = View.GONE
    }

    private fun setupTextChangeEvent() =binding.run{
        viewModel.apply {
            //充值金額
            etRechargeAmount.afterTextChanged {
                if (it.startsWith("0") && it.length > 1) {
                    etRechargeAmount.setText(etRechargeAmount.getText().replace("0", ""))
                    etRechargeAmount.setCursor()
                    return@afterTextChanged
                }

                if (etRechargeAmount.getText().length > 9) {
                    etRechargeAmount.setText(etRechargeAmount.getText().substring(0, 9))
                    etRechargeAmount.setCursor()
                    return@afterTextChanged
                }
                dailyConfigAdapter.getSelectedItem()?.let { it1 -> updateFirstDepositExtraMoney(it1,it.toIntS(0)) }
                checkRechargeAmount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
                    if (includeQuickMoney.root.isVisible) (includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).selectItem(
                        -1)
                    tvFeeAmount.text = ArithUtil.toMoneyFormat(0.0)
                } else {
                    tvFeeAmount.text = ArithUtil.toMoneyFormat(
                        it.toDouble().times(abs(mSelectRechCfgs?.rebateFee ?: 0.0))
                    )
                }
            }
            //微信
            etWxId.afterTextChanged { checkWX(it) }
            //認證姓名
            etName.afterTextChanged {
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
            etBankAccount.afterTextChanged { checkBankID(it) }
            //暱稱
            etNickname.afterTextChanged { checkNickName(it) }
        }
    }

    private fun setupFocusEvent() =binding.run{
        viewModel.apply {
            //充值金額
            setupEditTextFocusEvent(etRechargeAmount) {
                dailyConfigAdapter.getSelectedItem()?.let { it1 -> updateFirstDepositExtraMoney(it1,it.toIntS(0)) }
                checkRechargeAmount(it, mSelectRechCfgs)
            }
            //微信
            setupEditTextFocusEvent(etWxId) { checkWX(it) }
            //認證姓名
            setupEditTextFocusEvent(etName) {
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
            setupEditTextFocusEvent(etBankAccount) { checkBankID(it) }
            //暱稱
            setupEditTextFocusEvent(etNickname) { checkNickName(it) }
        }
    }

    private fun setupEditTextFocusEvent(customEditText: LoginEditText, event: (String) -> Unit)=binding.run {
        customEditText.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                event.invoke(customEditText.getText())
            } else if (customEditText == etRechargeAmount && includeQuickMoney.root.isVisible) {
                (includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).selectItem(-1)
            }
        }
    }

    //取得餘額
    private fun getMoney() {
        viewModel.getMoneyAndTransferOut()
    }

    //修改hint
    private fun updateMoneyRange() =binding.run{
        etRechargeAmount.setHint(
            String.format(
                getString(R.string.edt_hint_deposit_money), sConfigData?.systemCurrencySign,
                TextUtil.formatBetQuota(
                    ArithUtil.toMoneyFormatForHint(
                        mSelectRechCfgs?.minMoney ?: 0.00
                    ).toIntS(Int.MAX_VALUE)
                ),
                TextUtil.formatBetQuota(
                    ArithUtil.toMoneyFormatForHint(
                        mSelectRechCfgs?.maxMoney ?: 999999.00
                    ).toIntS(Int.MAX_VALUE)
                )
            )
        )
    }

    //創建MoneyAddRequest
    private fun createMoneyAddRequest(activityType:Int?, type: Int?): MoneyAddRequest? =binding.run{
        return when (mMoneyPayWay?.rechType) {
            MoneyType.BANK_TYPE.code, MoneyType.CTF_TYPE.code -> {
                MoneyAddRequest(
                    rechCfgId = mSelectRechCfgs?.id ?: 0,
                    bankCode = mBottomSheetList[bankPosition].bankName.toString(),
                    depositMoney = if (etRechargeAmount.getText().isNotEmpty()) {
                        etRechargeAmount.getText()
                    } else {
                        ""
                    },
                    payer = etBankAccount.getText(),
                    payerName = etName.getText(),
                    payerBankName = mBottomSheetList[bankPosition].bankName.toString(),
                    payerInfo = "",
                    depositDate = mCalendar.time.time,
                    appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext()),
                    appsFlyerKey = BuildConfig.AF_APPKEY,
                    appsFlyerPkgName = BuildConfig.APPLICATION_ID,
                    activityType = activityType,
                    type = type
                ).apply {
                    proofImg = imgResultUrl
                }
            }
            MoneyType.WX_TYPE.code, MoneyType.GCASH_TYPE.code, MoneyType.GRABPAY_TYPE.code, MoneyType.PAYMAYA_TYPE.code -> {
                MoneyAddRequest(
                    rechCfgId = mSelectRechCfgs?.id ?: 0,
                    bankCode = null,
                    depositMoney = if (etRechargeAmount.getText().isNotEmpty()) {
                        etRechargeAmount.getText()
                    } else {
                        ""
                    },
                    payer = null,
                    payerName = etWxId.getText(),
                    payerBankName = null,
                    payerInfo = null,
                    depositDate = mCalendar.time.time,
                    appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext()),
                    appsFlyerKey = BuildConfig.AF_APPKEY,
                    appsFlyerPkgName = BuildConfig.APPLICATION_ID,
                    activityType = activityType,
                    type = type
                ).apply {
                    proofImg = imgResultUrl
                }
            }
            MoneyType.ALI_TYPE.code -> {
                MoneyAddRequest(
                    rechCfgId = mSelectRechCfgs?.id ?: 0,
                    bankCode = null,
                    depositMoney = if (etRechargeAmount.getText().isNotEmpty()) {
                        etRechargeAmount.getText()
                    } else {
                        ""
                    },
                    payer = null,
                    payerName = etNickname.getText(),
                    payerBankName = null,
                    payerInfo = etName.getText(),
                    depositDate = mCalendar.time.time,
                    appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext()),
                    appsFlyerKey = BuildConfig.AF_APPKEY,
                    appsFlyerPkgName = BuildConfig.APPLICATION_ID,
                    activityType = activityType,
                    type = type
                ).apply {
                    proofImg = imgResultUrl
                }
            }
            else -> null
        }
    }

    private fun getBankType(position: Int)=binding.run {
        if (rechCfgsList.size > 0) {
            mSelectRechCfgs = rechCfgsList[position]
            refreshSelectRechCfgs(mSelectRechCfgs)
            getMoney()
            updateMoneyRange()

            ivBankIcon.setImageResource(mBottomSheetList[position].bankIcon ?: 0)
            txvPayBank.text = mBottomSheetList[position].bankName.toString()
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
                    else -> media?.realPath
                }
                path?.let { LogUtil.d(it) }
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
        val userId = LoginRepository.userId.toString()
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.VOUCHER)
        viewModel.uploadImage(uploadImgRequest)
    }

    /**
     * 设置快捷金额
     */
    private fun setupQuickMoney() =binding.run{
        if (sConfigData?.selectedDepositAmountSettingList.isNullOrEmpty()) {
            includeQuickMoney.root.isVisible = false
            etRechargeAmount.showLine(true)
            etRechargeAmount.setMarginBottom(10.dp)
        } else {
            includeQuickMoney.root.isVisible = true
            etRechargeAmount.showLine(false)
            etRechargeAmount.setMarginBottom(0.dp)
            if (includeQuickMoney.rvQuickMoney.adapter == null) {
                includeQuickMoney.rvQuickMoney.layoutManager = GridLayoutManager(requireContext(), 3)
                includeQuickMoney.rvQuickMoney.addItemDecoration(GridItemDecoration(0.dp,
                    0.dp,
                    requireContext().getColor(R.color.color_FFFFFF),
                    false))
                includeQuickMoney.rvQuickMoney.adapter = QuickMoneyAdapter().apply {
                    setOnItemClickListener { adapter, view, position ->
                        (adapter as QuickMoneyAdapter).selectItem(position)
                        adapter.data[position].toString().let {
                            etRechargeAmount.setText(it)
                            etRechargeAmount.clearFocus()
                        }
                    }
                    setList(sConfigData?.selectedDepositAmountSettingList)
                }
            } else {
                (includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).setList(sConfigData?.selectedDepositAmountSettingList)
            }
        }
    }
    private fun hideFirstDesposit(){
        binding.linFirstDeposit.linNoChoose.performClick()
        binding.linFirstDeposit.root.gone()
        binding.linReceiveExtra.gone()
    }
    private fun initFirstDeposit(list: List<DailyConfig>) =binding.linFirstDeposit.run{
        val availableList = list.filter { it.first==1 }.take(2)
        binding.linFirstDeposit.root.isVisible = availableList.isNotEmpty()
        linNoChoose.isSelected = true
        rvFirstDeposit.adapter = dailyConfigAdapter
        dailyConfigAdapter.setList(availableList)
        dailyConfigAdapter.setOnItemClickListener { adapter, view, position ->

        }
        linNoChoose.setOnClickListener {
            dailyConfigAdapter.clearSelected()
            updateDailyConfigSelect()
        }
    }
    private fun updateDailyConfigSelect(){
        val dailyConfig = dailyConfigAdapter.getSelectedItem()
        if (dailyConfig==null){
            binding.linFirstDeposit.linNoChoose.isSelected = true
            binding.linReceiveExtra.isVisible = false
            (binding.includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).setPercent(0)
        }else{
            binding.linFirstDeposit.linNoChoose.isSelected = false
            binding.linReceiveExtra.isVisible = true
            (binding.includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).setPercent(dailyConfig.additional)
            updateFirstDepositExtraMoney(dailyConfig,binding.etRechargeAmount.getText().toIntS(0))
        }

    }
    private fun updateFirstDepositExtraMoney(dailyConfig: DailyConfig, rechargeMoney: Int){
        if (dailyConfig!=null && dailyConfig.first==1){
            val additional = dailyConfig.additional
            val capped = dailyConfig.capped
            if (additional>0){
                val additionalMoney = rechargeMoney*additional/100
                val extraMoney = if(additionalMoney>capped) capped else  additionalMoney
                binding.tvExtraAmount.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(extraMoney,0)}"
            }
        }
    }

    override fun onSelected(dailyConfig: DailyConfig?) {
        if (dailyConfig == null) {
            dailyConfigAdapter.clearSelected()
            binding.linReceiveExtra.gone()
        } else {
            dailyConfigAdapter.changeSelect(dailyConfig)
        }
    }

    fun getSelectedDailyConfig() = dailyConfigAdapter.getSelectedItem()

    private fun submitForm() {
        val dailyConfig = dailyConfigAdapter.getSelectedItem() ?: return
        val activityType = dailyConfig.activityType
        val type = dailyConfig.type
        val request = createMoneyAddRequest(activityType, type) ?: return
        viewModel.rechargeSubmit(
            request,
            mMoneyPayWay?.rechType,
            mSelectRechCfgs
        )
    }

    override fun onContinue() {
        submitForm()
    }

}