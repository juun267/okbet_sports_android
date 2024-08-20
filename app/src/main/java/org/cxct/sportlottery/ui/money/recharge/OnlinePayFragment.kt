package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.runWithCatch
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
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.money.recharge.dialog.DepositHintDialog
import org.cxct.sportlottery.ui.money.recharge.dialog.RechargePromotionsDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * @app_destination 在线支付
 */
@SuppressLint("SetTextI18n")
class OnlinePayFragment : BaseFragment<MoneyRechViewModel, OnlinePayFragmentBinding>()
    , RechargePromotionsDialog.OnSelectListener, DepositHintDialog.ConfirmListener {

    private var mMoneyPayWay: MoneyPayWayData? = null //支付類型

    private var mSelectRechCfgs: RechCfg? = null //選擇的入款帳號

    private val mBankList = mutableListOf<BtsRvAdapter.SelectBank>()

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
        DialogBottomSheetIconAndTickBinding.inflate(layoutInflater,contentView,false)
    }

    private val dailyConfigAdapter = DailyConfigAdapter { updateDailyConfigSelect() }

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
        setUpPayWaysLayout()
    }

    private fun setUpPayWaysLayout() {
        (activity as? MoneyRechargeActivity)?.fillPayWaysLayoutTo(binding.content, 0)
    }

    private fun initObserve() {
        //充值金額訊息
        viewModel.rechargeOnlineAmountMsg.observe(viewLifecycleOwner) {
            binding.etRechargeOnlineAmount.setError(it)
        }

//        viewModel.rechargeOnlineAccountMsg.observe(viewLifecycleOwner) {
//            binding.etRechargeOnlinePayer.setError(it)
//        }

        //在線充值成功
        viewModel.onlinePayResult.observe(this.viewLifecycleOwner) {
            resetEvent()
//            hideFirstDesposit()
        }

        //在線充值首充提示
        viewModel.onlinePayFirstRechargeTips.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { tipString ->
                showPromptDialog(getString(R.string.prompt), tipString) {}
            }
        }

        viewModel.rechCheckMsg.observe(viewLifecycleOwner) {
            val msg = it.getContentIfNotHandled() ?: return@observe
            showErrorPromptDialog(getString(R.string.prompt), msg) {}
            val rechCfgs = mSelectRechCfgs ?: return@observe
            rechCfgs.open = 2
            setupMoneyCfgMaintanince(rechCfgs, binding.btnSubmit, binding.linMaintenance)
        }

        viewModel.dailyConfigEvent.observe(this){
            initFirstDeposit(it)
        }
    }

    private fun initView() = binding.run{

        etRechargeOnlineAmount.setHint(getAmountLimitHint())
        setupTextChangeEvent()
        setupFocusEvent()
        initViewMorePromotions()
        tvRemark.text = "・${getString(R.string.credit_bet_remark)}："
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

    private fun initButton() = binding.run {
        btnSubmit.setOnClickListener {
            if (viewModel.uniPaid) {
                if (submitForm(true)) {
                    DepositHintDialog.show(this@OnlinePayFragment)
                }
            } else {
                submitForm()
            }

        }

        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,linMaintenance) }

        llPayGap.setOnClickListener { payGapBottomSheet.show() }
        cvPayBank.setOnClickListener { bankBottomSheet.show() }
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
        etRechargeOnlinePayer.isVisible = mSelectRechCfgs?.isAccount==1
        etRechargeOnlineEmail.isVisible = mSelectRechCfgs?.isEmail ==1

        cvPayBank.visibility = if (mSelectRechCfgs?.banks != null) View.VISIBLE else View.GONE
        tvPayGapSubtitle.text = if (mSelectRechCfgs?.banks != null) getString(R.string.title_pay_channel) else getString(R.string.M132)
        dialogBinding.tvGameTypeTitle.text = if (mSelectRechCfgs?.banks != null) getString(R.string.title_choose_pay_channel) else getString(R.string.M132)

        //反利、手續費
        setupRebateFee()
        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,linMaintenance) }
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

    private fun setupTextChangeEvent() = binding.run {

        //充值金額
        etRechargeOnlineAmount.afterTextChanged {
            if (it.startsWith("0") && it.length > 1) {
                etRechargeOnlineAmount.setText(etRechargeOnlineAmount.getText().replace("0", ""))
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
            viewModel.checkRcgOnlineAmount(it, mSelectRechCfgs)
            if (it.isEmpty() || it.isBlank()) {
                if (includeQuickMoney.root.isVisible) {
                    (includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).selectItem(-1)
                }
                if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                    tvFeeAmount.text = String.format(
                        getString(R.string.hint_feeback_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00")
                } else {
                    tvFeeAmount.text = String.format(
                        getString(R.string.hint_fee_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00")
                }
                return@afterTextChanged
            }

            //返利/手續費金額
            if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                tvFeeAmount.text = String.format(
                    getString(R.string.hint_feeback_amount),sConfigData?.systemCurrencySign,
                    ArithUtil.toMoneyFormat((it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(mSelectRechCfgs?.rebateFee?:0.0))
                )
            } else {
                tvFeeAmount.text = String.format(
                    getString(R.string.hint_fee_amount),
                    sConfigData?.systemCurrencySign,
                    ArithUtil.toMoneyFormat(abs(it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0).times(mSelectRechCfgs?.rebateFee?:0.0)))
                )
            }

            if (mSelectRechCfgs?.rebateFee == 0.0 || mSelectRechCfgs?.rebateFee == null) {
                tvFeeRate.text = String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount), sConfigData?.systemCurrencySign, "0.00")
                tvFeeRate.gone()
                tvFeeAmount.gone()
            } else {
                tvFeeRate.show()
                tvFeeAmount.show()
            }

        }

        etRechargeOnlinePayer.afterTextChanged {
            if (it.isEmptyStr()) {
                etRechargeOnlinePayer.setError(getString(R.string.error_input_empty))
                return@afterTextChanged
            }
            etRechargeOnlinePayer.setError(null)
            viewModel.checkRcgNormalOnlineAccount(it)
        }

        etRechargeOnlineEmail.afterTextChanged {
            if (it.isEmptyStr()) {
                etRechargeOnlineEmail.setError(getString(R.string.error_input_empty))
                return@afterTextChanged
            }

            if (!VerifyConstUtil.verifyMail(it)) {
                etRechargeOnlineEmail.setError(getString(R.string.N889))
                return@afterTextChanged
            }

            etRechargeOnlineEmail.setError(null)
        }
    }

    private fun setupFocusEvent() = binding.run {
        etRechargeOnlineAmount.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus) {
                val amountStr = etRechargeOnlineAmount.getText()
                dailyConfigAdapter.getSelectedItem()?.let {
                    updateFirstDepositExtraMoney(it,amountStr.toIntS(0))
                }
                viewModel.checkRcgOnlineAmount(amountStr, mSelectRechCfgs)
            } else if (includeQuickMoney.root.isVisible) {
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

    private fun setupRebateFee() = binding.run {
        val rebateFee = mSelectRechCfgs?.rebateFee
        when{
            rebateFee == null || rebateFee == 0.0-> {
                tvFeeRate.gone()
                tvFeeAmount.gone()
                tvFeeRate.text = String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount), sConfigData?.systemCurrencySign, "0.00")
            }

            rebateFee > 0.0-> {
                tvFeeRate.visible()
                tvFeeAmount.visible()
                tvFeeRate.text = String.format(getString(R.string.hint_feeback_rate), ArithUtil.toOddFormat(abs(rebateFee).times(100))) + "%"
                tvFeeAmount.text = String.format(getString(R.string.hint_feeback_amount), sConfigData?.systemCurrencySign, TextUtil.formatMoney(ArithUtil.toOddFormat(0.0.times(100)).toDouble()))
            }

            else -> {
                tvFeeRate.visible()
                tvFeeAmount.visible()
                tvFeeRate.text = String.format(getString(R.string.hint_fee_rate), ArithUtil.toOddFormat(abs(rebateFee).times(100))) + "%"
                tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount), sConfigData?.systemCurrencySign, TextUtil.formatMoney(ArithUtil.toOddFormat(0.0.times(100)).toDouble()))
            }
        }
    }

    private fun setPayGapBottomSheet() = runWithCatch {

        val contentView: ViewGroup? = activity?.window?.decorView?.findViewById(android.R.id.content)
        val dialogBinding = DialogBottomSheetIconAndTickBinding.inflate(layoutInflater, contentView,false)
        payGapBottomSheet = BottomSheetDialog(this.requireContext())
        payGapBottomSheet.setContentView(dialogBinding.root)
        payGapAdapter = BtsRvAdapter(
            payRoadSpannerList,
            BtsRvAdapter.BankAdapterListener { _, position ->
                getPayGap(position)
                resetEvent()
                payGapBottomSheet.dismiss()
            }
        )

        dialogBinding.rvBankItem.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
        dialogBinding.rvBankItem.adapter = payGapAdapter

        if (mMoneyPayWay?.onlineType == OnlineType.WY.type) {
            dialogBinding.tvGameTypeTitle.text=String.format(resources.getString(R.string.title_choose_pay_channel))
        } else {
            dialogBinding.tvGameTypeTitle.text=String.format(resources.getString(R.string.M132))
        }

        dialogBinding.btnClose.setOnClickListener { payGapBottomSheet.dismiss() }
        getPayGap(0)
    }

    private fun setPayBankBottomSheet(view: View) = runWithCatch {

        bankBottomSheet = BottomSheetDialog(this.requireContext())
        bankBottomSheet.setContentView(dialogBinding.root)
        bankCardAdapter = BtsRvAdapter(mBankList,
            BtsRvAdapter.BankAdapterListener { it, position ->
                binding.ivBankIcon.setImageResource(it.bankIcon ?: 0)
                binding.txvPayBank.text = it.bankName.toString()
                bankPosition = position
                resetEvent()
                bankBottomSheet.dismiss()
        })

        dialogBinding.rvBankItem.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
        dialogBinding.rvBankItem.adapter = bankCardAdapter
        dialogBinding.tvGameTypeTitle.text=String.format(resources.getString(R.string.title_choose_pay_bank))
        dialogBinding.btnClose.setOnClickListener {
            bankBottomSheet.dismiss()
        }

    }


    private fun initData() {
        //支付類型的入款帳號清單
        rechCfgsList = viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType && it.onlineType == mMoneyPayWay?.onlineType && it.pcMobile != 1
        } ?: mutableListOf()

        payRoadSpannerList = mutableListOf()
        rechCfgsList.forEachIndexed { index, rechCfg ->
            val selectBank = BtsRvAdapter.SelectBank(
                "${viewModel.getOnlinePayTypeName(rechCfg.onlineType)} ${index + 1}",
                typeIcon
            )
            payRoadSpannerList.add(selectBank)
        }

        viewModel.getDailyConfig()
    }

    private fun getPayGap(position: Int)  = runWithCatch {
        if (payRoadSpannerList.size <= 0) {
            mSelectRechCfgs = null
            return@runWithCatch
        }

        //default 選擇第一個不為 null 的入款帳號資料
        mSelectRechCfgs = rechCfgsList[position]
        refreshSelectRechCfgs()
        if (mSelectRechCfgs?.banks != null) {
            refreshPayBank(mSelectRechCfgs)
        }
        binding.ivGapIcon.setImageResource(payRoadSpannerList[position].bankIcon ?: 0)
        binding.txvPayGap.text = payRoadSpannerList[position].bankName
        clearFocus()

    }

    //重置畫面事件
    private fun resetEvent() {
        clearFocus()
        binding.etRechargeOnlineAmount.setText("")
        viewModel.clearnRechargeStatus()
    }

    //联系客服
    private fun setupServiceButton() {
        binding.tvService.setServiceClick(childFragmentManager)
    }

    /**
     * 设置快捷金额
     */
    private fun setupQuickMoney() = binding.run{
        if (sConfigData?.selectedDepositAmountSettingList.isNullOrEmpty()) {
            includeQuickMoney.root.isVisible = false
            etRechargeOnlineAmount.showLine(true)
            etRechargeOnlineAmount.setMarginBottom(10.dp)
            return@run
        }

        includeQuickMoney.root.isVisible = true
        etRechargeOnlineAmount.showLine(false)
        etRechargeOnlineAmount.setMarginBottom(0.dp)

        val rvQuickMoney = includeQuickMoney.rvQuickMoney
        if (rvQuickMoney.adapter != null) {
            (rvQuickMoney.adapter as QuickMoneyAdapter).setList(sConfigData?.selectedDepositAmountSettingList)
            return@run
        }

        rvQuickMoney.layoutManager = GridLayoutManager(requireContext(), 3)
        rvQuickMoney.addItemDecoration(GridItemDecoration(0.dp, 0.dp, context().getColor(R.color.color_FFFFFF),false))
        rvQuickMoney.adapter = QuickMoneyAdapter().apply {
            setList(sConfigData?.selectedDepositAmountSettingList)
            setOnItemClickListener { adapter, _, position ->
                (adapter as QuickMoneyAdapter).selectItem(position)
                val item = adapter.data[position].toString()
                etRechargeOnlineAmount.setText(item)
                etRechargeOnlineAmount.clearFocus()
            }
        }
    }

    private fun initFirstDeposit(list: List<DailyConfig>) = binding.linFirstDeposit.run {
        val availableList = list.filter { it.first==1 }.take(2)
        binding.linFirstDeposit.root.isVisible = availableList.isNotEmpty()
        rvFirstDeposit.adapter = dailyConfigAdapter
        dailyConfigAdapter.setList(availableList)
//        tvRewardTC.setOnClickListener {
//            FirstDepositNoticeDialog(dailyConfig.content).show(childFragmentManager,null)
//        }
    }

    private fun updateDailyConfigSelect(){
        val dailyConfig = dailyConfigAdapter.getSelectedItem()
        if (dailyConfig == null) {
            binding.linFirstDeposit.linNoChoose.isSelected = true
            binding.linReceiveExtra.isVisible = false
            (binding.includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).setPercent(0)
        } else {
            binding.linFirstDeposit.linNoChoose.isSelected = false
            binding.linReceiveExtra.isVisible = true
            (binding.includeQuickMoney.rvQuickMoney.adapter as QuickMoneyAdapter).setPercent(dailyConfig.additional)
            updateFirstDepositExtraMoney(dailyConfig,binding.etRechargeOnlineAmount.getText().toIntS(0))
        }

    }
    private fun updateFirstDepositExtraMoney(dailyConfig: DailyConfig, rechargeMoney: Int){
        if (dailyConfig.first != 1) {
            return
        }
        val additional = dailyConfig.additional
        val capped = dailyConfig.capped
        if (additional > 0) {
            val additionalMoney = rechargeMoney.toDouble() * additional / 100
            val extraMoney = if(additionalMoney>capped) capped else  additionalMoney
            binding.tvExtraAmount.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney2(extraMoney)}"
        }
    }

    override fun onSelected(dailyConfig: DailyConfig?) {
        if (dailyConfig == null) {
            dailyConfigAdapter.clearSelected()
        } else {
            dailyConfigAdapter.changeSelect(dailyConfig)
        }
        updateDailyConfigSelect()
    }

    fun getSelectedDailyConfig() = dailyConfigAdapter.getSelectedItem()

    override fun onContinue() {
        submitForm()
    }

    private fun submitForm(onlyCheck: Boolean = false): Boolean = binding.run {
        var email: String? = null
        var payer = ""
        if (etRechargeOnlinePayer.isVisible) {
            payer = etRechargeOnlinePayer.getText()
            if (payer.isEmptyStr()) {
                ToastUtil.showToast(context(), getString(R.string.edt_hint_payer))
                etRechargeOnlinePayer.setError(getString(R.string.error_input_empty))
                return@run false
            }
        }

        if (etRechargeOnlineEmail.isVisible) {
            email = etRechargeOnlineEmail.getText()
            if (email.isEmptyStr()) {
                ToastUtil.showToast(context(), getString(R.string.J558))
                etRechargeOnlineEmail.setError(getString(R.string.error_input_empty))
                return@run false
            }

            if (!VerifyConstUtil.verifyMail(email!!)) {
                ToastUtil.showToast(context(), getString(R.string.N889))
                return@run false
            }
        }

        if (onlyCheck) {
            return@run true
        }

        val bankCode = if (cvPayBank.isVisible) {
            mSelectRechCfgs?.banks?.get(bankPosition)?.value
        } else {
            ""
        }

        val depositMoney = if (binding.etRechargeOnlineAmount.getText().isNotEmpty()) {
            binding.etRechargeOnlineAmount.getText()
        } else {
            ""
        }
        val dailyConfig = dailyConfigAdapter.getSelectedItem()
        val activityType = dailyConfig?.activityType
        val type = dailyConfig?.type
        viewModel.rechargeNormalOnlinePay(context(), mSelectRechCfgs, depositMoney, bankCode, payer, activityType, type, email)
        return@run true
    }
}