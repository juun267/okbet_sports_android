package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.item_dailyconfig.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogBottomSheetIconAndTickBinding
import org.cxct.sportlottery.databinding.OnlinePayFragmentBinding
import org.cxct.sportlottery.net.money.data.DailyConfig
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.OnlineType
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import kotlin.math.abs

/**
 * @app_destination 在线支付
 */
@SuppressLint("SetTextI18n")
class OnlinePayFragment : BindingFragment<MoneyRechViewModel, OnlinePayFragmentBinding>() {

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

    private val dialogBinding by lazy {
        val contentView: ViewGroup? = activity?.window?.decorView?.findViewById(android.R.id.content)
        DialogBottomSheetIconAndTickBinding.inflate(layoutInflater,contentView,false) }

    private val dailyConfigAdapter = DailyConfigAdapter{
        updateDailyConfigSelect()
    }

    override fun onStart() {
        super.onStart()
        //獲取最新的使用者資料
        viewModel.getUserInfo()
    }

    override fun onInitView(view: View) {
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
            binding.etRechargeOnlineAmount.setError(it)
        }

        viewModel.rechargeOnlineAccountMsg.observe(viewLifecycleOwner) {
            binding.etRechargeOnlinePayer.setError(it)
        }

        //在線充值成功
        viewModel.onlinePayResult.observe(this.viewLifecycleOwner) {
            resetEvent()
            hideFirstDesposit()
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
                    setupMoneyCfgMaintanince(it, binding.btnSubmit, binding.linMaintenance.root)
                }
                showErrorPromptDialog(getString(R.string.prompt), it) {}
            }
        }
        viewModel.dailyConfigEvent.observe(this){
            initFirstDeposit(it)
        }
    }

    private fun initView() = binding.run{

        etRechargeOnlineAmount.setHint(getAmountLimitHint())

        setupTextChangeEvent()
        setupFocusEvent()

        btnSubmit.setTitleLetterSpacing()

        tvRemark.text = "・${getString(R.string.credit_bet_remark)}："
    }

    private fun initButton() =binding.run{
        btnSubmit.setOnClickListener {

            val bankCode = when (cvPayBank.visibility) {
                View.GONE -> ""
                else -> mSelectRechCfgs?.banks?.get(bankPosition)?.value
            }

            val depositMoney = if (binding.etRechargeOnlineAmount.getText().isNotEmpty()) {
                binding.etRechargeOnlineAmount.getText()
            } else {
                ""
            }

            val payer = if (needPayerField()) etRechargeOnlinePayer.getText() else ""
            val activityType = dailyConfigAdapter.getSelectedItem()?.activityType
            viewModel.rechargeNormalOnlinePay(requireContext(), mSelectRechCfgs, depositMoney, bankCode, payer,activityType)
        }
        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,linMaintenance.root) }

        llPayGap.setOnClickListener {
            payGapBottomSheet.show()
        }
        cvPayBank.setOnClickListener {
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
    private fun refreshSelectRechCfgs() = binding.run {
        llRemark.visibility = if (mSelectRechCfgs?.remark.isNullOrEmpty()) View.GONE else View.VISIBLE
        tvHint.text = mSelectRechCfgs?.remark
        binding.etRechargeOnlineAmount.setHint(getAmountLimitHint())
        etRechargeOnlinePayer.visibility = if (needPayerField()) View.VISIBLE else View.GONE

        cvPayBank.visibility = if (mSelectRechCfgs?.banks != null) View.VISIBLE else View.GONE
        tvPayGapSubtitle.text =
            if (mSelectRechCfgs?.banks != null) getString(R.string.title_pay_channel) else getString(R.string.M132)
        dialogBinding.tvGameTypeTitle.text =
            if (mSelectRechCfgs?.banks != null) getString(R.string.title_choose_pay_channel) else getString(R.string.M132)

        //反利、手續費
        setupRebateFee()
        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,linMaintenance.root) }
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
            mBankList[0].bankIcon?.let { icon -> binding.ivBankIcon.setImageResource(icon) }
            mBankList[0].bankName?.let { name -> binding.txvPayBank.text = name }
            bankPosition = 0
        }
    }

    private fun setupTextChangeEvent()=binding.run {
        viewModel.apply {
            //充值金額
            binding.etRechargeOnlineAmount.afterTextChanged {
                if (it.startsWith("0") && it.length > 1) {
                    binding.etRechargeOnlineAmount.setText(etRechargeOnlineAmount.getText()
                        .replace("0", ""))
                    etRechargeOnlineAmount.setCursor()
                    return@afterTextChanged
                }

                if (etRechargeOnlineAmount.getText().length > 9) {
                    etRechargeOnlineAmount.setText(etRechargeOnlineAmount.getText()
                        .substring(0, 9))
                    etRechargeOnlineAmount.setCursor()
                    return@afterTextChanged
                }
                dailyConfigAdapter.getSelectedItem()?.let { dailyConfig->
                    updateFirstDepositExtraMoney(dailyConfig,it.toIntS(0))
                }
                checkRcgOnlineAmount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
                    if (includeQuickMoney.root.isVisible) (includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).selectItem(
                        -1)
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                        tvFeeAmount.text =
                            String.format(getString(R.string.hint_feeback_amount),
                                sConfigData?.systemCurrencySign,
                                "0.00")
                    } else {
                        tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount),
                            sConfigData?.systemCurrencySign,
                            "0.00")
                    }
                } else {
                    //返利/手續費金額
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                        tvFeeAmount.text =
                            String.format(getString(R.string.hint_feeback_amount),sConfigData?.systemCurrencySign,
                                ArithUtil.toMoneyFormat((it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(mSelectRechCfgs?.rebateFee?:0.0))

                            )
                    } else {
                        tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount),
                            sConfigData?.systemCurrencySign,
                            ArithUtil.toMoneyFormat(abs(it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0).times(mSelectRechCfgs?.rebateFee?:0.0))))
                    }
                    if(mSelectRechCfgs?.rebateFee == 0.0 || mSelectRechCfgs?.rebateFee == null) {
                        tvFeeRate.text =
                            String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                        tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount),
                            sConfigData?.systemCurrencySign,
                            "0.00")
                        tvFeeRate.gone()
                        tvFeeAmount.gone()
                    }else{
                        tvFeeRate.show()
                        tvFeeAmount.show()
                    }
                }

            }

            etRechargeOnlinePayer.afterTextChanged {
                checkRcgNormalOnlineAccount(it)
            }
        }
    }

    private fun setupFocusEvent()=binding.run {
        etRechargeOnlineAmount.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus) {
                val amountStr = etRechargeOnlineAmount.getText()
                dailyConfigAdapter.getSelectedItem()?.let {
                    updateFirstDepositExtraMoney(it,amountStr.toIntS(0))
                }
                viewModel.checkRcgOnlineAmount(amountStr, mSelectRechCfgs)
            }else if (includeQuickMoney.root.isVisible) {
                (includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).selectItem(-1)
            }
        }
        etRechargeOnlinePayer.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                viewModel.checkRcgNormalOnlineAccount(etRechargeOnlinePayer.getText())
        }
    }


    private fun getAmountLimitHint(): String {
        return String.format(
            getString(R.string.edt_hint_deposit_money), sConfigData?.systemCurrencySign,
            TextUtil.formatBetQuota(mSelectRechCfgs?.minMoney?.toLong() ?: 0),
            TextUtil.formatBetQuota(mSelectRechCfgs?.maxMoney?.toLong() ?: 999999)
        )
    }

    private fun setupRebateFee() =binding.run{
        val rebateFee = mSelectRechCfgs?.rebateFee
        when{
            rebateFee == null || rebateFee == 0.0->{
                tvFeeRate.gone()
                tvFeeAmount.gone()
                tvFeeRate.text = String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount), sConfigData?.systemCurrencySign, "0.00")
            }
            rebateFee > 0.0->{
                tvFeeRate.visible()
                tvFeeAmount.visible()
                tvFeeRate.text = String.format(getString(R.string.hint_feeback_rate), ArithUtil.toOddFormat(abs(rebateFee).times(100))) + "%"
                tvFeeAmount.text = String.format(getString(R.string.hint_feeback_amount), sConfigData?.systemCurrencySign, TextUtil.formatMoney(ArithUtil.toOddFormat(0.0.times(100)).toDouble()))
            }
            else ->{
                tvFeeRate.visible()
                tvFeeAmount.visible()
                tvFeeRate.text = String.format(getString(R.string.hint_fee_rate), ArithUtil.toOddFormat(abs(rebateFee).times(100))) + "%"
                tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount), sConfigData?.systemCurrencySign, TextUtil.formatMoney(ArithUtil.toOddFormat(0.0.times(100)).toDouble()))
            }
        }
    }

    private fun setPayGapBottomSheet() {
        try {
            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)

            val dialogBinding = DialogBottomSheetIconAndTickBinding.inflate(layoutInflater,contentView,false)
            payGapBottomSheet = BottomSheetDialog(this.requireContext())
            payGapBottomSheet.apply {
                setContentView(dialogBinding.root)
                payGapAdapter = BtsRvAdapter(
                    payRoadSpannerList,
                    BtsRvAdapter.BankAdapterListener { _, position ->
                        getPayGap(position)
                        resetEvent()
                        payGapBottomSheet.dismiss()
                    })
                dialogBinding.rvBankItem.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
                dialogBinding.rvBankItem.adapter = payGapAdapter

                if (mMoneyPayWay?.onlineType == OnlineType.WY.type)
                    dialogBinding.tvGameTypeTitle.text=String.format(resources.getString(R.string.title_choose_pay_channel))
                else
                    dialogBinding.tvGameTypeTitle.text=String.format(resources.getString(R.string.M132))

                dialogBinding.btnClose.setOnClickListener {
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

            bankBottomSheet = BottomSheetDialog(this.requireContext())
            bankBottomSheet.apply {
                setContentView(dialogBinding.root)
                bankCardAdapter = BtsRvAdapter(
                    mBankList,
                    BtsRvAdapter.BankAdapterListener { it, position ->
                        binding.ivBankIcon.setImageResource(it.bankIcon ?: 0)
                        binding.txvPayBank.text = it.bankName.toString()
                        bankPosition = position
                        resetEvent()
                        dismiss()
                    })
                dialogBinding.rvBankItem.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
                dialogBinding.rvBankItem.adapter = bankCardAdapter
                dialogBinding.tvGameTypeTitle.text=String.format(resources.getString(R.string.title_choose_pay_bank))
                dialogBinding.btnClose.setOnClickListener {
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

        viewModel.getDailyConfig()
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

                binding.ivGapIcon.setImageResource(payRoadSpannerList[position].bankIcon ?: 0)
                binding.txvPayGap.text = payRoadSpannerList[position].bankName
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
        binding.etRechargeOnlineAmount.setText("")
        viewModel.clearnRechargeStatus()
    }

    private fun needPayerField(): Boolean = when (mMoneyPayWay?.onlineType) {
        OnlineType.DRAGON_PAY.type -> true
        else -> false
    }


    //联系客服
    private fun setupServiceButton() {
        binding.tvService.setServiceClick(childFragmentManager)
    }

    /**
     * 设置快捷金额
     */
    private fun setupQuickMoney() =binding.run{
        if (sConfigData?.selectedDepositAmountSettingList.isNullOrEmpty()) {
            includeQuickMoney.root.isVisible = false
            etRechargeOnlineAmount.showLine(true)
            etRechargeOnlineAmount.setMarginBottom(10.dp)
        } else {
            includeQuickMoney.root.isVisible = true
            etRechargeOnlineAmount.showLine(false)
            etRechargeOnlineAmount.setMarginBottom(0.dp)
            if (binding.includeQuickMoney.rvQuickMoney.adapter == null) {
                binding.includeQuickMoney.rvQuickMoney.layoutManager = GridLayoutManager(requireContext(), 3)
                binding.includeQuickMoney.rvQuickMoney.addItemDecoration(GridItemDecoration(0.dp,
                    0.dp,
                    requireContext().getColor(R.color.color_FFFFFF),
                    false))
                binding.includeQuickMoney.rvQuickMoney.adapter = QuickMoneyAdapter().apply {
                    setList(sConfigData?.selectedDepositAmountSettingList)
                    setOnItemClickListener { adapter, view, position ->
                        (adapter as QuickMoneyAdapter).selectItem(position)
                        adapter.data[position].toString().let {
                            etRechargeOnlineAmount.setText(it)
                            etRechargeOnlineAmount.clearFocus()
                        }
                    }
                }
            } else {
                (binding.includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).setList(sConfigData?.selectedDepositAmountSettingList)
            }
        }
    }
    private fun hideFirstDesposit(){
        binding.linFirstDeposit.linNoChoose.performClick()
        binding.linFirstDeposit.root.gone()
        binding.linReceiveExtra.gone()
    }
    private fun initFirstDeposit(list: List<DailyConfig>) =binding.linFirstDeposit.run{
        val availableList = list.filter { it.first==1 }
        binding.linFirstDeposit.root.isVisible = availableList.isNotEmpty()
        linNoChoose.isSelected = true
        rvFirstDeposit.adapter = dailyConfigAdapter
        dailyConfigAdapter.setList(availableList)
        linNoChoose.setOnClickListener {
            dailyConfigAdapter.clearSelected()
            updateDailyConfigSelect()
        }
//        tvRewardTC.setOnClickListener {
//            FirstDepositNoticeDialog(dailyConfig.content).show(childFragmentManager,null)
//        }
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
            updateFirstDepositExtraMoney(dailyConfig,binding.etRechargeOnlineAmount.getText().toIntS(0))
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
}