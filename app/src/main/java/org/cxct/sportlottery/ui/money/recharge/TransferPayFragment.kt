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
import kotlinx.android.synthetic.main.transfer_pay_fragment.*
import kotlinx.android.synthetic.main.transfer_pay_fragment.btn_submit
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.CustomImageAdapter
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
            val moneyAddRequest = MoneyAddRequest(
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
            viewModel.rechargeAdd(moneyAddRequest)
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

        //複製地址
        btn_address_copy.setOnClickListener {
            val clipboard =
                activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText(null, tv_address.text)
            clipboard?.setPrimaryClip(clipData)
            ToastUtil.showToastInCenter(activity, getString(R.string.text_money_copy_success))
        }
    }

    private fun initView() {
        setupTextChangeEvent()
        calendarBottomSheet()
    }

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
        Glide.with(this).load(selectRechCfgs?.qrCode).into(iv_address)

        //備註
        tv_remark.text = selectRechCfgs?.remark

        //銀行卡轉帳 UI 特別處理
        if (mMoneyPayWay?.rechType == "bankTransfer") {
            ll_qr.visibility = View.GONE
            ll_address.visibility = View.VISIBLE
            et_wx_id.visibility = View.GONE

        } else {
            ll_qr.visibility = View.VISIBLE
            ll_address.visibility = View.GONE

        }
        when (mMoneyPayWay?.rechType) {
            MoneyType.BANK_TYPE.code, MoneyType.CTF_TYPE.code -> {
                ll_qr.visibility = View.GONE
                ll_address.visibility = View.VISIBLE
                et_wx_id.visibility = View.GONE
                et_nickname.visibility = View.GONE
                et_bank_account.visibility = View.VISIBLE
                et_name.visibility = View.VISIBLE
            }
            MoneyType.WX_TYPE.code -> {
                ll_qr.visibility = View.VISIBLE
                ll_address.visibility = View.GONE
                et_wx_id.visibility = View.VISIBLE
                et_nickname.visibility = View.GONE
                et_bank_account.visibility = View.GONE
                et_name.visibility = View.GONE
            }
            MoneyType.ALI_TYPE.code -> {
                ll_qr.visibility = View.VISIBLE
                ll_address.visibility = View.GONE
                et_wx_id.visibility = View.GONE
                et_nickname.visibility = View.VISIBLE
                et_bank_account.visibility = View.GONE
                et_name.visibility = View.VISIBLE
            }
        }

        //存款時間
        txv_recharge_time.text = TimeUtil.stampToDate(Date().time)

    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //充值金額
            et_recharge_amount.afterTextChanged { checkRechargeAmount(it) }
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

}