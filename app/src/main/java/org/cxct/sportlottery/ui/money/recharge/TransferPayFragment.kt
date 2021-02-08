package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.transfer_pay_fragment.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.CustomImageAdapter
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.MoneyManager.getBankAccountIcon
import org.cxct.sportlottery.util.MoneyManager.getBankIconByBankName
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import java.text.SimpleDateFormat
import java.util.*


class TransferPayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    companion object {
        private const val TAG = "TransferPayFragment"
    }

    lateinit var calendarBottomSheet: BottomSheetDialog

    private var mMoneyPayWay: MoneyPayWayData? = MoneyPayWayData("", "", "", "", 0) //支付類型

    private var mSelectRechCfgs: MoneyRechCfg.RechConfig? = null //選擇的入款帳號

    private val mSpannerList = mutableListOf<CustomImageAdapter.SelectBank>()

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

        initPayAccountSpinner()
        initView()
        initButton()
        initObserve()
    }

    private fun initButton() {
        //提交
        btn_submit.setOnClickListener {
            createMoneyAddRequest()?.let { viewModel.rechargeSubmit(it, mMoneyPayWay?.rechType, mSelectRechCfgs) }
        }

        //選取日曆
        cv_recharge_time.setOnClickListener {
            calendarBottomSheet.tv_calendar_title.text = getString(R.string.start_date)
            calendarBottomSheet.show()
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

    }

    private fun initView() {
        setupTextChangeEvent()
        setupFocusEvent()
        calendarBottomSheet()
        getMoney()
        updateMoneyRange()
    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {

        //充值金額訊息
        viewModel.rechargeAmountMsg.observe(viewLifecycleOwner, {
            et_recharge_amount.setError(it)
        })
        //微信
        viewModel.wxErrorMsg.observe(viewLifecycleOwner, {
            et_wx_id.setError(it)
        })
        //認證姓名
        viewModel.nameErrorMsg.observe(viewLifecycleOwner, {
            et_name.setError(it)
        })
        //銀行卡號
        viewModel.bankIDErrorMsg.observe(viewLifecycleOwner, {
            et_bank_account.setError(it)
        })
        //暱稱
        viewModel.nickNameErrorMsg.observe(viewLifecycleOwner, {
            et_nickname.setError(it)
        })
        viewModel.userMoneyResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            txv_wallet_money.text = (ArithUtil.toMoneyFormat(it?.money)) + " RMB"
        })

    }

    //入款帳號選單
    private fun initPayAccountSpinner() {
        //支付類型的入款帳號清單
        val rechCfgsList = viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType
        } ?: mutableListOf()

        //產生對應 spinner 選單
        var count = 1

        if (mMoneyPayWay?.rechType == "bankTransfer") //銀行卡轉帳 顯示銀行名稱，不用加排序數字
            rechCfgsList.forEach {
                val selectBank = CustomImageAdapter.SelectBank(
                    it.rechName.toString(),
                    getBankIconByBankName(it.rechName.toString())
                )
                mSpannerList.add(selectBank)
            }
        else {
            val title = mMoneyPayWay?.title

            if (rechCfgsList.size > 1)
                rechCfgsList.forEach {
                    val selectBank =
                        CustomImageAdapter.SelectBank(
                            title + count++,
                            getBankAccountIcon(it.rechType ?: "")
                        )
                    mSpannerList.add(selectBank)
                }
            else
                rechCfgsList.forEach {
                    val selectBank =
                        CustomImageAdapter.SelectBank(
                            title + "",
                            getBankAccountIcon(it.rechType ?: "")
                        )
                    mSpannerList.add(selectBank)
                }
        }
        sp_pay_account.adapter = CustomImageAdapter(context, mSpannerList)

        //選擇入款帳號
        sp_pay_account.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                try {
                    mSelectRechCfgs = rechCfgsList[position]
                    refreshSelectRechCfgs(mSelectRechCfgs)
                    getMoney()
                    updateMoneyRange()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        //default 選擇第一個不為 null 的入款帳號資料
        if (sp_pay_account.count > 0)
            sp_pay_account.setSelection(0)
        else
            mSelectRechCfgs = null
    }

    //依據選擇的支付渠道，刷新UI
    private fun refreshSelectRechCfgs(selectRechCfgs: MoneyRechCfg.RechConfig?) {
        //姓名
        tv_name.text = selectRechCfgs?.payeeName

        //帳號
        tv_account.text = selectRechCfgs?.payee

        //地址QR code
        if (selectRechCfgs?.qrCode.isNullOrEmpty()) {
            ll_qr_code.visibility = View.GONE
        } else {
            Glide.with(this).load(selectRechCfgs?.qrCode).into(iv_address)
        }


        //銀行卡轉帳 UI 特別處理
        if (mMoneyPayWay?.rechType == "bankTransfer") {
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
                ll_qr.visibility = View.GONE
                ll_address.visibility = View.VISIBLE
                et_wx_id.visibility = View.GONE
                et_nickname.visibility = View.GONE
                et_bank_account.visibility = View.VISIBLE
                et_name.visibility = View.VISIBLE
                tv_hint2.visibility = View.VISIBLE

                tv_hint1.text = getString(R.string.money_recharge_hint1)
                tv_hint2.text = getString(R.string.money_recharge_hint2)
            }
            MoneyType.CTF_TYPE.code -> {
                ll_qr.visibility = View.VISIBLE
                ll_address.visibility = View.GONE
                et_wx_id.visibility = View.GONE
                et_nickname.visibility = View.GONE
                et_bank_account.visibility = View.VISIBLE
                et_name.visibility = View.VISIBLE
                tv_hint2.visibility = View.GONE

                tv_hint1.text = getString(R.string.cft_recharge_hint)
            }
            MoneyType.WX_TYPE.code -> {
                ll_qr.visibility = View.VISIBLE
                ll_address.visibility = View.GONE
                et_wx_id.visibility = View.VISIBLE
                et_nickname.visibility = View.GONE
                et_bank_account.visibility = View.GONE
                et_name.visibility = View.GONE
                tv_hint2.visibility = View.GONE

                tv_hint1.text = getString(R.string.wx_recharge_hint)

            }
            MoneyType.ALI_TYPE.code -> {
                ll_qr.visibility = View.VISIBLE
                ll_address.visibility = View.GONE
                et_wx_id.visibility = View.GONE
                et_nickname.visibility = View.VISIBLE
                et_bank_account.visibility = View.GONE
                et_name.visibility = View.VISIBLE
                tv_hint2.visibility = View.GONE

                tv_hint1.text = getString(R.string.ali_recharge_hint)
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
            tv_fee_rate.text = ArithUtil.toOddFormat(rebateFee.times(100))
            tv_fee_amount.text = ArithUtil.toOddFormat(0.0.times(100))
        }

        //存款時間
        txv_recharge_time.text = TimeUtil.stampToDate(Date().time)

    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //充值金額
            et_recharge_amount.afterTextChanged {
                checkRechargeAmount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
                    tv_fee_amount.text = ArithUtil.toMoneyFormat(0.0)
                } else {
                    tv_fee_amount.text = ArithUtil.toMoneyFormat(it.toDouble().times(mSelectRechCfgs?.rebateFee ?: 0.0))
                }
            }
            //微信
            et_wx_id.afterTextChanged { checkWX(it) }
            //認證姓名
            et_name.afterTextChanged { checkUserName(it) }
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
            setupEditTextFocusEvent(et_name) { checkUserName(it) }
            //認證銀行卡號
            setupEditTextFocusEvent(et_bank_account) { checkBankID(it) }
            //暱稱
            setupEditTextFocusEvent(et_nickname) { checkNickName(it) }
        }
    }

    private fun setupEditTextFocusEvent(customEditText: LoginEditText, event: (String) -> Unit) {
        customEditText.setEditTextOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus)
                event.invoke(customEditText.et_input.text.toString())
        }
    }

    //日曆
    @SuppressLint("InflateParams")
    private fun calendarBottomSheet() {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.dialog_bottom_sheet_calendar_single, null)
        calendarBottomSheet = BottomSheetDialog(this.requireContext())
        calendarBottomSheet.setContentView(bottomSheetView)
        calendarBottomSheet.calendar.setSelectableDateRange(
            getDateInCalendar(30).first,
            getDateInCalendar(30).second
        )
        calendarBottomSheet.calendar.setCalendarListener(object : CalendarListener {
            override fun onFirstDateSelected(startDate: Calendar) {
                calendarBottomSheet.dismiss()
            }

            override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
                val formatter =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                txv_recharge_time.text = formatter.format(startDate.time)
                calendarBottomSheet.dismiss()
            }
        })
    }

    private fun getDateInCalendar(minusDays: Int? = 0): Pair<Calendar, Calendar> { //<startDate, EndDate>
        val todayCalendar = TimeUtil.getTodayEndTimeCalendar()
        val minusDaysCalendar = TimeUtil.getTodayStartTimeCalendar()
        if (minusDays != null) minusDaysCalendar.add(Calendar.DATE, -minusDays)
        return Pair(minusDaysCalendar, todayCalendar)
    }

    //取得餘額
    private fun getMoney() {
        viewModel.getMoney()
    }

    //修改hint
    private fun updateMoneyRange() {
        et_recharge_amount.setHint(
            String.format(
                getString(R.string.edt_hint_deposit_money),
                ArithUtil.toMoneyFormat(mSelectRechCfgs?.minMoney ?: 0.0),
                ArithUtil.toMoneyFormat(mSelectRechCfgs?.maxMoney ?: 999999.0)
            )
        )
    }

    //創建MoneyAddRequest
    private fun createMoneyAddRequest(): MoneyAddRequest? {
        var moneyAddRequest: MoneyAddRequest? = null
        moneyAddRequest = when (mMoneyPayWay?.rechType) {
            MoneyType.BANK_TYPE.code, MoneyType.CTF_TYPE.code -> {
                MoneyAddRequest(
                    rechCfgId = mSelectRechCfgs?.id ?: 0,
                    bankCode = mSpannerList[sp_pay_account.selectedItemPosition].bankName.toString(),
                    depositMoney = if (et_recharge_amount.getText().isNotEmpty()) {
                        et_recharge_amount.getText().toInt()
                    } else {
                        0
                    },
                    payer = et_bank_account.getText(),
                    payerName = et_name.getText(),
                    payerBankName = mSpannerList[sp_pay_account.selectedItemPosition].bankName.toString(),
                    payerInfo = "",
                    depositDate = calendarBottomSheet.calendar.startDate?.timeInMillis ?: Date().time
                )
            }
            MoneyType.WX_TYPE.code -> {
                MoneyAddRequest(
                    rechCfgId = mSelectRechCfgs?.id ?: 0,
                    bankCode = null,
                    depositMoney = if (et_recharge_amount.getText().isNotEmpty()) {
                        et_recharge_amount.getText().toInt()
                    } else {
                        0
                    },
                    payer = null,
                    payerName = et_wx_id.getText(),
                    payerBankName = null,
                    payerInfo = null,
                    depositDate = calendarBottomSheet.calendar.startDate?.timeInMillis ?: Date().time
                )
            }
            MoneyType.ALI_TYPE.code -> {
                MoneyAddRequest(
                    rechCfgId = mSelectRechCfgs?.id ?: 0,
                    bankCode = null,
                    depositMoney = if (et_recharge_amount.getText().isNotEmpty()) {
                        et_recharge_amount.getText().toInt()
                    } else {
                        0
                    },
                    payer = null,
                    payerName = et_nickname.getText(),
                    payerBankName = null,
                    payerInfo = et_name.getText(),
                    depositDate = calendarBottomSheet.calendar.startDate?.timeInMillis ?: Date().time
                )
            }
            else -> null
        }
        return moneyAddRequest
    }
}