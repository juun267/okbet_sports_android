package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_icon_and_tick.*
import kotlinx.android.synthetic.main.online_pay_fragment.*
import kotlinx.android.synthetic.main.online_pay_fragment.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.OnlineType
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.MoneyManager
import java.math.RoundingMode
import kotlin.math.abs

class OnlinePayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    private var mMoneyPayWay: MoneyPayWayData? = MoneyPayWayData("", "", "", "", 0) //支付類型

    private var mSelectRechCfgs: RechCfg? = null //選擇的入款帳號

    private val mBankList: MutableList<BtsRvAdapter.SelectBank> by lazy {
        mutableListOf()
    }

    private var rechCfgsList: List<RechCfg> = mutableListOf()

    private var payRoadSpannerList = mutableListOf<BtsRvAdapter.SelectBank>()

    private lateinit var payGapBottomSheet: BottomSheetDialog

    private lateinit var bankBottomSheet: BottomSheetDialog

    private lateinit var payGapAdapter: BtsRvAdapter

    private lateinit var bankCardAdapter: BtsRvAdapter

    private var bankPosition = 0

    private var typeIcon: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.online_pay_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        initObserve()
        initData()
        initView()
        setPayGapBottomSheet()
        setPayBankBottomSheet(view)
    }

    private fun initObserve() {
        //充值金額訊息
        viewModel.rechargeOnlineAmountMsg.observe(viewLifecycleOwner, {
            et_recharge_online_amount.setError(it)
        })

        //在線充值成功
        viewModel.onlinePayResult.observe(this.viewLifecycleOwner, {
            resetEvent()
        })
    }

    private fun initView() {
        if (mMoneyPayWay?.image != "ic_online_pay") {
            cv_pay_bank.visibility = View.GONE
        } else {
            cv_pay_bank.visibility = View.VISIBLE
        }

        tv_pay_gap_subtitle.text =
            if (mMoneyPayWay?.onlineType == OnlineType.ONLINE.type) getString(R.string.title_pay_channel)
            else getString(R.string.title_pay_gap)

        et_recharge_online_amount.setHint(getAmountLimitHint())

        setupTextChangeEvent()
        setupFocusEvent()
    }

    private fun initButton() {
        btn_submit.setOnClickListener {

            val bankCode = when (cv_pay_bank.visibility) {
                View.GONE -> ""
                else -> mSelectRechCfgs?.banks?.get(bankPosition)?.value
            }

            val depositMoney = if (et_recharge_online_amount.getText().isNotEmpty()) {
                et_recharge_online_amount.getText()
            } else {
                ""
            }
            viewModel.rechargeOnlinePay(requireContext(), mSelectRechCfgs, depositMoney, bankCode)
        }
        ll_pay_gap.setOnClickListener {
            payGapBottomSheet.show()
        }
        cv_pay_bank.setOnClickListener {
            bankBottomSheet.show()
        }
    }

    fun setArguments(moneyPayWay: MoneyPayWayData?): OnlinePayFragment {
        mMoneyPayWay = moneyPayWay
        typeIcon = when (mMoneyPayWay?.onlineType) {
            1 -> R.drawable.ic_online_pay_type
            2 -> R.drawable.ic_alipay_type
            3 -> R.drawable.ic_wechat_pay_type
            else -> R.drawable.ic_online_pay_type
        }
        return this
    }

    //依據選擇的支付渠道，刷新UI
    @SuppressLint("SetTextI18n")
    private fun refreshSelectRechCfgs() {
        tv_hint.text = mSelectRechCfgs?.remark
        et_recharge_online_amount.setHint(getAmountLimitHint())

        //反利、手續費
        setupRebateFee()
    }

    private fun refreshPayBank(rechCfgsList: RechCfg?) {
        mBankList.clear()
        rechCfgsList?.banks?.forEach {
            val data =
                BtsRvAdapter.SelectBank(
                    it.bankName,
                    MoneyManager.getBankIconByBankName(it.bankName.toString())
                )
            mBankList.add(data)
        }.let {
            mBankList[0].bankIcon?.let { icon -> iv_bank_icon.setImageResource(icon) }
            mBankList[0].bankName?.let { name -> txv_pay_bank.text = name }
            bankPosition = 0
        }
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //充值金額
            et_recharge_online_amount.afterTextChanged {
                if(it.startsWith("0") && it.length > 1){
                    et_recharge_online_amount.setText(et_recharge_online_amount.getText().replace("0",""))
                    et_recharge_online_amount.setCursor()
                    return@afterTextChanged
                }

                if(et_recharge_online_amount.getText().length > 6){
                    et_recharge_online_amount.setText(et_recharge_online_amount.getText().substring(0,6))
                    et_recharge_online_amount.setCursor()
                    return@afterTextChanged
                }

                checkRcgOnlineAmount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
                    tv_fee_amount.text = ArithUtil.toMoneyFormat(0.0)
                } else {
                    tv_fee_amount.text = ArithUtil.toMoneyFormat(
                        it.toDouble().times(abs(mSelectRechCfgs?.rebateFee ?: 0.0))
                    )
                }
            }
        }
    }

    private fun setupFocusEvent() {
        et_recharge_online_amount.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                viewModel.checkRcgOnlineAmount(et_recharge_online_amount.getText(), mSelectRechCfgs)
        }
    }


    private fun getAmountLimitHint(): String {
        return String.format(
            getString(R.string.edt_hint_deposit_money),
            mSelectRechCfgs?.minMoney?.toLong() ?: 0,
            mSelectRechCfgs?.maxMoney?.toLong() ?: 999999
        )
    }

    private fun setupRebateFee() {
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
    }

    private fun setPayGapBottomSheet() {
        try {
            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)

            val bottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_icon_and_tick, contentView, false)
            payGapBottomSheet = BottomSheetDialog(this.requireContext())
            payGapBottomSheet.apply {
                setContentView(bottomSheetView)
                payGapAdapter = BtsRvAdapter(
                    payRoadSpannerList,
                    BtsRvAdapter.BankAdapterListener { _, position ->
                        getPayGap(position)
                        resetEvent()
                        payGapBottomSheet.dismiss()
                    })
                rv_bank_item.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
                rv_bank_item.adapter = payGapAdapter

                if (mMoneyPayWay?.onlineType == OnlineType.ONLINE.type)
                    tv_game_type_title.text=String.format(resources.getString(R.string.title_choose_pay_channel))
                else
                    tv_game_type_title.text=String.format(resources.getString(R.string.title_choose_pay_gap))

                payGapBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }
            getPayGap(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setPayBankBottomSheet(view: View) {
        try {

            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)

            val bottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_icon_and_tick, contentView, false)

            bankBottomSheet = BottomSheetDialog(this.requireContext())
            bankBottomSheet.apply {
                setContentView(bottomSheetView)
                bankCardAdapter = BtsRvAdapter(
                    mBankList,
                    BtsRvAdapter.BankAdapterListener { it, position ->
                        view.iv_bank_icon.setImageResource(it.bankIcon ?: 0)
                        view.txv_pay_bank.text = it.bankName.toString()
                        bankPosition = position
                        resetEvent()
                        dismiss()
                    })
                rv_bank_item.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
                rv_bank_item.adapter = bankCardAdapter
                tv_game_type_title.text=String.format(resources.getString(R.string.title_choose_pay_bank))
                bankBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun initData() {
        //支付類型的入款帳號清單
        rechCfgsList = viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType && it.onlineType == mMoneyPayWay?.onlineType && it.pcMobile != 1
        } ?: mutableListOf()

        //產生對應 spinner 選單
        var count = 1

        payRoadSpannerList = mutableListOf()

        if (rechCfgsList.size > 1)
            rechCfgsList.forEach { it ->
                val selectBank =
                    BtsRvAdapter.SelectBank(
                        viewModel.getOnlinePayTypeName(it.onlineType) + count++,
                        typeIcon
                    )
                payRoadSpannerList.add(selectBank)
            }
        else
            rechCfgsList.forEach { it ->
                val selectBank =
                    BtsRvAdapter.SelectBank(
                        viewModel.getOnlinePayTypeName(it.onlineType) + count,
                        typeIcon
                    )
                payRoadSpannerList.add(selectBank)
            }
    }

    private fun getPayGap(position: Int) {
        //default 選擇第一個不為 null 的入款帳號資料
        try {
            if (payRoadSpannerList.size > 0) {
                mSelectRechCfgs = rechCfgsList[position]
                refreshSelectRechCfgs()

                if (cv_pay_bank.visibility == View.VISIBLE)
                    refreshPayBank(mSelectRechCfgs)

                iv_gap_icon.setImageResource(payRoadSpannerList[position].bankIcon ?: 0)
                txv_pay_gap.text = payRoadSpannerList[position].bankName
                clearFocus()
            } else
                mSelectRechCfgs = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //重置畫面事件
    private fun resetEvent() {
        clearFocus()
        et_recharge_online_amount.setText("")
        viewModel.clearnRechargeStatus()
    }
}