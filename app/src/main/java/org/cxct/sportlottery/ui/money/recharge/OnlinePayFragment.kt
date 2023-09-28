package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_setting_center.*
import kotlinx.android.synthetic.main.content_game_detail_result_rv.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_icon_and_tick.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.fragment_bet_station.*
import kotlinx.android.synthetic.main.include_quick_money.*
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.*
import kotlinx.android.synthetic.main.online_pay_fragment.*
import kotlinx.android.synthetic.main.online_pay_fragment.btn_submit
import kotlinx.android.synthetic.main.online_pay_fragment.includeQuickMoney
import kotlinx.android.synthetic.main.online_pay_fragment.iv_bank_icon
import kotlinx.android.synthetic.main.online_pay_fragment.ll_remark
import kotlinx.android.synthetic.main.online_pay_fragment.tv_fee_amount
import kotlinx.android.synthetic.main.online_pay_fragment.tv_fee_rate
import kotlinx.android.synthetic.main.online_pay_fragment.tv_hint
import kotlinx.android.synthetic.main.online_pay_fragment.tv_remark
import kotlinx.android.synthetic.main.online_pay_fragment.txv_pay_bank
import kotlinx.android.synthetic.main.online_pay_fragment.linMaintenance
import kotlinx.android.synthetic.main.online_pay_fragment.view.*
import kotlinx.android.synthetic.main.view_payment_maintenance.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.OnlineType
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import kotlin.math.abs

/**
 * @app_destination 在线支付
 */
@SuppressLint("SetTextI18n")
class OnlinePayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    private var mMoneyPayWay: MoneyPayWayData? = null //支付類型

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

    override fun onStart() {
        super.onStart()
        //獲取最新的使用者資料
        viewModel.getUserInfo()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        initObserve()
        initData()
        initView()
        setPayGapBottomSheet()
        setPayBankBottomSheet(view)
        setupServiceButton()
        setupQuickMoney()
    }

    private fun initObserve() {
        //充值金額訊息
        viewModel.rechargeOnlineAmountMsg.observe(viewLifecycleOwner) {
            et_recharge_online_amount.setError(it)
        }

        viewModel.rechargeOnlineAccountMsg.observe(viewLifecycleOwner) {
            et_recharge_online_payer.setError(it)
        }

        //在線充值成功
        viewModel.onlinePayResult.observe(this.viewLifecycleOwner) {
            resetEvent()
        }

        //在線充值首充提示
        viewModel.onlinePayFirstRechargeTips.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { tipString ->
                showPromptDialog(getString(R.string.prompt), tipString) {}
            }
        }
        viewModel.rechCheckMsg.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                mSelectRechCfgs?.let {
                    it.open = 2
                    setupMoneyCfgMaintanince(it, btn_submit, linMaintenance)
                }
                showErrorPromptDialog(getString(R.string.prompt), it) {}
            }
        }
    }

    private fun initView() {

        et_recharge_online_amount.setHint(getAmountLimitHint())

        setupTextChangeEvent()
        setupFocusEvent()

        btn_submit.setTitleLetterSpacing()

        tv_remark.text = "・${getString(R.string.credit_bet_remark)}："

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

            val payer = if (needPayerField()) et_recharge_online_payer.getText() else ""

            viewModel.rechargeNormalOnlinePay(requireContext(), mSelectRechCfgs, depositMoney, bankCode, payer)
        }
        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btn_submit,linMaintenance) }

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
            OnlineType.WY.type  -> R.drawable.ic_online_pay_type
            OnlineType.ZFB.type  -> R.drawable.ic_alipay_type
            OnlineType.WX.type -> R.drawable.ic_wechat_pay_type
            OnlineType.JUAN.type -> R.drawable.ic_juancash_type
            OnlineType.DISPENSHIN.type -> R.drawable.ic_juancash_type
            OnlineType.ONLINEBANK.type -> R.drawable.icon_onlinebank//阿喵說照Ian回應用此圖
            OnlineType.GCASH.type -> R.drawable.ic_g_cash_type
            OnlineType.GRABPAY.type -> R.drawable.ic_grab_pay_type
            OnlineType.PAYMAYA.type -> R.drawable.ic_pay_maya_type
            OnlineType.PAYPAL.type -> R.drawable.ic_paypal_type
            OnlineType.DRAGON_PAY.type -> R.drawable.ic_gragon_pay_type
            OnlineType.FORTUNE_PAY.type -> R.drawable.icon_fortunepay
            OnlineType.AUB.type -> R.drawable.ic_aub_round
            OnlineType.EPON.type -> R.drawable.ic_epon_round
            else -> R.drawable.ic_online_pay_type
        }
        return this
    }

    //依據選擇的支付渠道，刷新UI
    @SuppressLint("SetTextI18n")
    private fun refreshSelectRechCfgs() {
        ll_remark.visibility = if (mSelectRechCfgs?.remark.isNullOrEmpty()) View.GONE else View.VISIBLE
        tv_hint.text = mSelectRechCfgs?.remark
        et_recharge_online_amount.setHint(getAmountLimitHint())
        et_recharge_online_payer.visibility = if (needPayerField()) View.VISIBLE else View.GONE

        cv_pay_bank.visibility = if (mSelectRechCfgs?.banks != null) View.VISIBLE else View.GONE
        tv_pay_gap_subtitle.text =
            if (mSelectRechCfgs?.banks != null) getString(R.string.title_pay_channel) else getString(R.string.M132)
        payGapBottomSheet.tv_game_type_title.text =
            if (mSelectRechCfgs?.banks != null) getString(R.string.title_choose_pay_channel) else getString(R.string.M132)

        //反利、手續費
        setupRebateFee()
        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btn_submit,linMaintenance) }
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
                if (it.startsWith("0") && it.length > 1) {
                    et_recharge_online_amount.setText(et_recharge_online_amount.getText()
                        .replace("0", ""))
                    et_recharge_online_amount.setCursor()
                    return@afterTextChanged
                }

                if (et_recharge_online_amount.getText().length > 9) {
                    et_recharge_online_amount.setText(et_recharge_online_amount.getText()
                        .substring(0, 9))
                    et_recharge_online_amount.setCursor()
                    return@afterTextChanged
                }

                checkRcgOnlineAmount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
                    if (includeQuickMoney.isVisible) (rv_quick_money.adapter as QuickMoneyAdapter).selectItem(
                        -1)
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                        tv_fee_amount.text =
                            String.format(getString(R.string.hint_feeback_amount),
                                sConfigData?.systemCurrencySign,
                                "0.00")
                    } else {
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount),
                            sConfigData?.systemCurrencySign,
                            "0.00")
                    }
                } else {
                    //返利/手續費金額
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                        tv_fee_amount.text =
                            String.format(getString(R.string.hint_feeback_amount),sConfigData?.systemCurrencySign,
                                ArithUtil.toMoneyFormat((it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(mSelectRechCfgs?.rebateFee?:0.0))

                            )
                    } else {
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount),
                            sConfigData?.systemCurrencySign,
                            ArithUtil.toMoneyFormat(abs(it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0).times(mSelectRechCfgs?.rebateFee?:0.0))))
                    }
                    if(mSelectRechCfgs?.rebateFee == 0.0 || mSelectRechCfgs?.rebateFee == null) {
                        tv_fee_rate.text =
                            String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount),
                            sConfigData?.systemCurrencySign,
                            "0.00")
                        tv_fee_rate.gone()
                        tv_fee_amount.gone()
                    }else{
                        tv_fee_rate.show()
                        tv_fee_amount.show()
                    }
                }

            }

            et_recharge_online_payer.afterTextChanged {
                checkRcgNormalOnlineAccount(it)
            }
        }
    }

    private fun setupFocusEvent() {
        et_recharge_online_amount.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                viewModel.checkRcgOnlineAmount(et_recharge_online_amount.getText(), mSelectRechCfgs)
            else if (includeQuickMoney.isVisible) {
                (rv_quick_money.adapter as QuickMoneyAdapter).selectItem(-1)
            }
        }
        et_recharge_online_payer.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                viewModel.checkRcgNormalOnlineAccount(et_recharge_online_payer.getText())
        }
    }


    private fun getAmountLimitHint(): String {
        return String.format(
            getString(R.string.edt_hint_deposit_money), sConfigData?.systemCurrencySign,
            TextUtil.formatBetQuota(mSelectRechCfgs?.minMoney?.toLong() ?: 0),
            TextUtil.formatBetQuota(mSelectRechCfgs?.maxMoney?.toLong() ?: 999999)
        )
    }

    private fun setupRebateFee() {
        val rebateFee = mSelectRechCfgs?.rebateFee
        when{
            rebateFee == null || rebateFee == 0.0->{
                tv_fee_rate.gone()
                tv_fee_amount.gone()
                tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), sConfigData?.systemCurrencySign, "0.00")
            }
            rebateFee > 0.0->{
                tv_fee_rate.visible()
                tv_fee_amount.visible()
                tv_fee_rate.text = String.format(getString(R.string.hint_feeback_rate), ArithUtil.toOddFormat(abs(rebateFee).times(100))) + "%"
                tv_fee_amount.text = String.format(getString(R.string.hint_feeback_amount), sConfigData?.systemCurrencySign, TextUtil.formatMoney(ArithUtil.toOddFormat(0.0.times(100)).toDouble()))
            }
            else ->{
                tv_fee_rate.visible()
                tv_fee_amount.visible()
                tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate), ArithUtil.toOddFormat(abs(rebateFee).times(100))) + "%"
                tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), sConfigData?.systemCurrencySign, TextUtil.formatMoney(ArithUtil.toOddFormat(0.0.times(100)).toDouble()))
            }
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

                if (mMoneyPayWay?.onlineType == OnlineType.WY.type)
                    tv_game_type_title.text=String.format(resources.getString(R.string.title_choose_pay_channel))
                else
                    tv_game_type_title.text=String.format(resources.getString(R.string.M132))

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
                        "${viewModel.getOnlinePayTypeName(it.onlineType)} ${count++}",
                        typeIcon
                    )
                payRoadSpannerList.add(selectBank)
            }
        else
            rechCfgsList.forEach { it ->
                val selectBank =
                    BtsRvAdapter.SelectBank(
                        "${viewModel.getOnlinePayTypeName(it.onlineType)} $count",
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

                if (mSelectRechCfgs?.banks != null) {
                    refreshPayBank(mSelectRechCfgs)
                }

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

    private fun needPayerField(): Boolean = when (mMoneyPayWay?.onlineType) {
        OnlineType.DRAGON_PAY.type -> true
        else -> false
    }


    //联系客服
    private fun setupServiceButton() {
        tv_service.setServiceClick(childFragmentManager)
    }

    /**
     * 设置快捷金额
     */
    private fun setupQuickMoney() {
        if (sConfigData?.selectedDepositAmountSettingList.isNullOrEmpty()) {
            includeQuickMoney.isVisible = false
            et_recharge_online_amount.showLine(true)
            et_recharge_online_amount.setMarginBottom(10.dp)
        } else {
            includeQuickMoney.isVisible = true
            et_recharge_online_amount.showLine(false)
            et_recharge_online_amount.setMarginBottom(0.dp)
            if (rv_quick_money.adapter == null) {
                rv_quick_money.layoutManager = GridLayoutManager(requireContext(), 3)
                rv_quick_money.addItemDecoration(GridItemDecoration(10.dp,
                    12.dp,
                    requireContext().getColor(R.color.color_FFFFFF),
                    false))
                rv_quick_money.adapter = QuickMoneyAdapter().apply {
                    setList(sConfigData?.selectedDepositAmountSettingList)
                    setOnItemClickListener { adapter, view, position ->
                        (adapter as QuickMoneyAdapter).selectItem(position)
                        adapter.data[position].toString().let {
                            et_recharge_online_amount.setText(it)
                            et_recharge_online_amount.et_input.clearFocus()
                        }
                    }
                }
            } else {
                (rv_quick_money.adapter as QuickMoneyAdapter).setList(sConfigData?.selectedDepositAmountSettingList)
            }
        }
    }
}