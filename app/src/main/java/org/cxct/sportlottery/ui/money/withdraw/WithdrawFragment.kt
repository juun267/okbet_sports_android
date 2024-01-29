package org.cxct.sportlottery.ui.money.withdraw

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.BankCardChangeEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.toDoubleS
import org.cxct.sportlottery.databinding.FragmentWithdrawBinding
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.money.withdraw.BankActivity.Companion.ModifyBankTypeKey
import org.cxct.sportlottery.ui.money.withdraw.BankActivity.Companion.TransferTypeAddSwitch
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.min

/**
 * @app_destination 提款-tab
 */
class WithdrawFragment : BaseFragment<WithdrawViewModel,FragmentWithdrawBinding>() {

    private lateinit var bankCardAdapter: WithdrawBankCardAdapter
    private var withdrawBankCardData: BankCardList? = null
    private lateinit var betStationFragment: BetStationFragment
    lateinit var transferTypeAddSwitch: TransferTypeAddSwitch
    private val zero = 0.0
    private var dealType: TransferType = TransferType.BANK

    override fun onInitView(view: View) {
        initView()
        initEvent()
        EventBusUtil.targetLifecycle(this)
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        initObserve()
        setupData()
    }

    private fun setupData() = viewModel.run {
        getBankCardList()
        getMoneyConfigs()
        getUwCheck()
    }

    private fun initView() =binding.run {
        etWithdrawalAmount.clearIsShow = false
        etWithdrawalAmount.getAllIsShow = true
        tvAddBank.setOnClickListener {
            var intent = Intent(context, BankActivity::class.java)
            var numType:Int = 1
            when(dealType){
                TransferType.BANK-> numType = 1
                TransferType.CRYPTO -> numType =2
                TransferType.E_WALLET -> numType = 3
                TransferType.PAYMAYA -> numType = 4
            }
            intent.putExtra("add_bank", numType)
            startActivity(intent)
        }

        llTabLayout.gone() // 預設先隱藏 等待卡片資料讀取完畢後再顯示
        btnInfo.setOnClickListener {CommissionInfoDialog().show(childFragmentManager, null) }
        tvDetail.setOnClickListener {startActivity(WithdrawCommissionDetailActivity::class.java) }
        tvCurrencyType.text = sConfigData?.systemCurrencySign

        checkNotificationVisiable(
            tvNotification1,
            tvDot1,
            sConfigData?.minRechMoney,
            getString(
                R.string.initial_withdrawal_needs_credited,
                sConfigData?.systemCurrencySign,
                sConfigData?.minRechMoney
            )
        )

        if (sConfigData?.enableMinRemainingBalance == FLAG_OPEN) {
            checkNotificationVisiable(
                tvNotification2,
                tvDot2,
                sConfigData?.minRemainingBalance,
                getString(
                    R.string.make_sure_valid_account,
                    sConfigData?.systemCurrencySign,
                    sConfigData?.minRemainingBalance
                )
            )
        }

        tvAddBank.text = context?.getString(R.string.bank_list_add, context?.getString(R.string.bank_list_bank))
        tvService.setServiceClick(childFragmentManager)
    }

    private fun checkNotificationVisiable(
        textView: TextView,
        dotView: TextView,
        value: String?,
        string: String
    ) {
        val needHide = value.isNullOrBlank() || value == "0"

        textView.visibility = if (needHide) View.GONE else View.VISIBLE
        dotView.visibility = if (needHide) View.GONE else View.VISIBLE

        textView.text = string
    }

    private fun initEvent() {
        setupTextChangeEvent()
    }

    private fun selectDealType(type: TransferType) {
        viewModel.setDealType(type)
        setupDealView(type)
    }

    private fun setupDealView(type: TransferType) =binding.run{
        when (type) {
            TransferType.BANK -> {
                tvChannelSelect.text = getString(R.string.select_bank)
                etWithdrawalAmount.setTitle(getString(R.string.withdraw_amount))
            }
            TransferType.E_WALLET, TransferType.PAYMAYA -> {
                tvChannelSelect.text = getString(R.string.ewallet)
                etWithdrawalAmount.setTitle(getString(R.string.withdraw_amount))
            }
            TransferType.CRYPTO -> {
                tvChannelSelect.text = getString(R.string.select_crypto_address)
                etWithdrawalAmount.setTitle(getString(R.string.withdraw_number))
            }
        }
    }
    private fun setupClickEvent() =binding.run{
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabSelect(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        btnWithdraw.setOnClickListener {
            modifyFinish()
            if(sConfigData?.auditFailureRestrictsWithdrawalsSwitch==1&&(viewModel.uwCheckData?.total?.unFinishValidAmount?:0.0)>0){
                showPromptDialog(getString(R.string.P150),getString(R.string.P149,"${sConfigData?.systemCurrencySign}${(viewModel.uwCheckData?.total?.unFinishValidAmount?:0).toInt()}")){}
                return@setOnClickListener
            }
            viewModel.showCheckDeductMoneyDialog {
                withdrawBankCardData?.let {
                    viewModel.addWithdraw(
                        withdrawBankCardData,
                        viewModel.getChannelMode(),
                        etWithdrawalAmount.getText(),
                        etWithdrawalPassword.getText(),
                        null,
                        null,
                        null,
                    )
                }
            }?.show(childFragmentManager,DeductDialog::class.java.name)
        }

        btnWithdraw.setTitleLetterSpacing()

        etWithdrawalAmount.getAllButton {
            it.setText(min(viewModel.getWithdrawAmountLimit().max,
                viewModel.userMoney.value ?: 0.0).toString())
            etWithdrawalAmount.setSelection()
        }
    }

    private fun setupTextChangeEvent() =binding.run{
        viewModel.apply {
            //提款金額
            etWithdrawalAmount.afterTextChanged { checkWithdrawAmount(withdrawBankCardData, it) }

            //提款密碼
            etWithdrawalPassword.afterTextChanged { checkWithdrawPasswordByWithdrawPage(it) }
        }
    }
    private fun tabSelect(tab:TabLayout.Tab) =binding.run{
        when (tab.text) {
            getString(R.string.bank_card) -> {
                selectBetStationTab(false)
                selectDealType(TransferType.BANK)
                dealType = TransferType.BANK
                transferTypeAddSwitch.apply {
                    addBankGroup.visibility = if (bankTransfer)View.VISIBLE else View.GONE
                }
                tvAddBank.text = context?.getString(R.string.bank_list_add, context?.getString(R.string.bank_list_bank))
                clearEvent()
            }
            getString(R.string.crypto) -> {
                selectBetStationTab(false)
                selectDealType(TransferType.CRYPTO)
                dealType = TransferType.CRYPTO
                transferTypeAddSwitch.apply {
                    addBankGroup.visibility = if (cryptoTransfer)View.VISIBLE else View.GONE
                }
                tvAddBank.text = context?.getString(R.string.bank_list_add, context?.getString(R.string.bank_list_crypto))
                clearEvent()
            }
            getString(R.string.ewallet) -> {
                selectBetStationTab(false)
                selectDealType(TransferType.E_WALLET)
                dealType = TransferType.E_WALLET
                transferTypeAddSwitch.apply {
                    addBankGroup.visibility =
                        if (walletTransfer) View.VISIBLE else View.GONE
                }
                tvAddBank.text = context?.getString(R.string.bank_list_add,
                    context?.getString(R.string.bank_list_e_wallet))
                clearEvent()
            }
            getString(R.string.online_maya) -> {
                selectBetStationTab(false)
                selectDealType(TransferType.PAYMAYA)
                dealType = TransferType.PAYMAYA
                transferTypeAddSwitch.apply {
                    addBankGroup.visibility =
                        if (paymataTransfer) View.VISIBLE else View.GONE
                }
                tvAddBank.text = context?.getString(R.string.bank_list_add,
                    context?.getString(R.string.online_maya))
                clearEvent()
            }
            getString(R.string.Outlets_Reserve) -> {
                selectBetStationTab(true)
                selectDealType(TransferType.STATION)
                dealType = TransferType.STATION
                clearEvent()
            }
        }
        if (TextUtils.equals(tab.text, getString(R.string.Outlets_Reserve))) {
            viewModel.setVisibleView(false)
        } else {
            viewModel.setVisibleView(true)
        }
    }

    private fun clearEvent() {
        binding.etWithdrawalAmount.setText("")
        binding.etWithdrawalPassword.setText("")
        if (::bankCardAdapter.isInitialized) {
            bankCardAdapter.initSelectStatus()
        }
        viewModel.resetWithdrawPage()
        modifyFinish()
    }

    private fun updateButtonStatus(isEnable: Boolean) {
        binding.btnWithdraw.setBtnEnable(isEnable)
    }

    private fun initObserve() {
        viewModel.submitEnable.observe(this){
            //当前有选中的卡片，并且卡片不维护
            val availabCard = withdrawBankCardData!=null&&withdrawBankCardData?.maintainStatus==0
            updateButtonStatus(it && availabCard )
        }
        viewModel.addMoneyCardSwitch.observe(this) {
            transferTypeAddSwitch = it
        }
        viewModel.commissionCheckList.observe(this) {
            binding.tvDetail.apply {
                isEnabled = it.isNotEmpty()
                isSelected = it.isNotEmpty()
            }
        }

        viewModel.needCheck.observe(this) {
            binding.llCommission.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.deductMoney.observe(this) {
            binding.tvCommission.apply {
                text = if (it.isNaN()) "0" else TextUtil.formatMoney(zero.minus(it ?: 0.0))

                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (zero.minus(
                                it ?: 0.0
                            ) > 0
                        ) R.color.color_08dc6e_08dc6e else R.color.color_E44438_e44438
                    )
                )
            }
        }

        viewModel.loading.observe(this) {
            if (it)
                loading()
            else
                hideLoading()
        }

        viewModel.withdrawAmountTotal.observe(this) {
            //提款总计加入千分符
            binding.tvWithdrawalTotal.text = TextUtil.format(it)

        }

        viewModel.userMoney.observe(this) {
            binding.tvBalance.text = TextUtil.format(ArithUtil.toMoneyFormat(it).toDoubleS())
            viewModel.getWithdrawHint()
        }

        viewModel.moneyCardList.observe(this) {
            if (it.transferType == TransferType.STATION)
                return@observe
            val cardList = it.cardList
            if (cardList.isEmpty()) {
                jumpToMoneyCardSetting(true, it.transferType)
                return@observe
            }
            initSelectBankCardBottomSheet(cardList.toMutableList())
        }

        viewModel.moneyCardExist.observe(this) { moneyCardSet ->
            val bankCardExist = moneyCardSet.find { it.transferType == TransferType.BANK }?.exist
            val cryptoCardExist =
                moneyCardSet.find { it.transferType == TransferType.CRYPTO }?.exist
            val eWalletCardExist =
                moneyCardSet.find { it.transferType == TransferType.E_WALLET }?.exist
            val stationExit =
                moneyCardSet.find { it.transferType == TransferType.STATION }?.exist
            val paymayaExit =
                moneyCardSet.find { it.transferType == TransferType.PAYMAYA }?.exist
            when {
                bankCardExist == true || cryptoCardExist == true || eWalletCardExist == true || stationExit == true || paymayaExit == true -> {
                    binding.tabLayout.getTabAt(0)?.let {
                        it.select()
                        tabSelect(it)
                    }
                }
                else -> {
                    jumpToMoneyCardSetting()
                }
            }
        }
        //Tab 顯示判斷
        viewModel.withdrawTabIsShow.observe(this) { list ->
            binding.tabLayout.removeAllTabs()
            list.forEach { type->
                when(type){
                    TransferType.BANK.type -> {
                        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.bank_card))
                        transferTypeAddSwitch.apply {
                            binding.addBankGroup.visibility = if (bankTransfer)View.VISIBLE else View.GONE
                        }
                    }
                    TransferType.CRYPTO.type -> binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.crypto))
                    TransferType.E_WALLET.type -> binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.ewallet))
                    TransferType.STATION.type -> binding.tabLayout.addTab(binding.tabLayout.newTab()
                        .setText(R.string.Outlets_Reserve))
                    TransferType.PAYMAYA.type -> binding.tabLayout.addTab(binding.tabLayout.newTab()
                        .setText(R.string.online_maya))
                }
            }
            if (list.isNullOrEmpty() || list.size == 1) {
                binding.tabLayout.visibility = View.GONE
                binding.llTabLayout.visibility = View.GONE
            } else {
                binding.tabLayout.visibility = View.VISIBLE
                binding.llTabLayout.visibility = View.VISIBLE
            }
            setupClickEvent()
        }

        //資金設定
        viewModel.rechargeConfigs.observe(this) {
            if (binding.etWithdrawalAmount.getText().isEmpty()) {
                viewModel.getWithdrawRate(withdrawBankCardData)
            } else {
                viewModel.getWithdrawRate(
                    withdrawBankCardData,
                    binding.etWithdrawalAmount.getText().toDoubleS()
                )
            }
            viewModel.getWithdrawHint()
        }

        //提款金額提示訊息
        viewModel.withdrawAmountHint.observe(this) {
            binding.etWithdrawalAmount.setHint(it)
        }

        //提款金額訊息
        viewModel.withdrawAmountMsg.observe(this) {
            binding.etWithdrawalAmount.setError(it)
        }

        //提款手續費提示
        viewModel.withdrawRateHint.observe(this) {
            binding.tvHintWithdrawRate.text = it
        }

        //提款虛擬幣所需餘額
        viewModel.withdrawCryptoAmountHint.observe(this) {
            binding.tvHintWithdrawCryptoAmount.text = it
            binding.tvHintWithdrawCryptoAmount.isVisible = it.isNotEmpty()
        }

        //提款虛擬幣手續費
        viewModel.withdrawCryptoFeeHint.observe(this) {
            binding.tvHintWithdrawCryptoFee.text = it
            binding.tvHintWithdrawCryptoFee.isVisible = it.isNotEmpty()
        }


        //提款密碼訊息
        viewModel.withdrawPasswordMsg.observe(this) {
            binding.etWithdrawalPassword.setError(it ?: "")
        }
        //提款
        viewModel.withdrawAddResultData.observe(this) {
            if (binding.linWithdraw.isVisible)
                if (it.success) {
                    clearEvent()
                    showPromptDialog(
                        getString(R.string.prompt),
                        getString(R.string.text_money_get_success)
                    ) { viewModel.getMoneyAndTransferOut() }
                    if (it.content?.authorizeUrl?.isNotEmpty() == true) {
                        JumpUtil.toExternalWeb(requireContext(), it.content.authorizeUrl)
                    }
                } else {
                    //流水不达标提醒
                    if (it.code == 2280){
                        showPromptDialog(getString(R.string.P150), it.msg) {}
                    }else{
                        showErrorPromptDialog(getString(R.string.prompt), it.msg) {}
                    }
                }
            if (it.content?.authorizeUrl?.isNotEmpty() == true) {
                JumpUtil.toExternalWeb(requireContext(), it.content?.authorizeUrl)
            }
        }
    }

    /**
     * 跳轉至資金卡新增頁面
     * @param assignType 是否指定跳轉新增型態(銀行卡, 虛擬幣)
     */
    private fun jumpToMoneyCardSetting(
        assignType: Boolean = false,
        transferType: TransferType? = null
    ) {
        val content = when (transferType) {
            TransferType.CRYPTO -> {
                getString(R.string.please_setting_crypto)
            }
            TransferType.E_WALLET -> {
                getString(R.string.please_setting_ewallet)
            }
            TransferType.PAYMAYA -> {
                getString(R.string.please_setting_paymaya)
            }
            else -> {
                getString(R.string.please_setting_bank_card)
            }
        }
        showPromptDialogNoCancel(getString(R.string.withdraw_setting), content) {
            this@WithdrawFragment.activity?.finish()
            startActivity(
                Intent(
                    requireContext(),
                    BankActivity::class.java
                ).apply {
                    if (assignType) {
                        putExtra(ModifyBankTypeKey, transferType)
                        putExtra(TransferTypeAddSwitch, viewModel.addMoneyCardSwitch.value)
                    }
                })
        }
    }

    private fun initSelectBankCardBottomSheet(
        bankCardList: MutableList<BankCardList>
    ) { //TODO Dean : 重構BottomSheet
        bankCardAdapter =
            WithdrawBankCardAdapter(
                bankCardList,
                BankCardAdapterListener {
                    withdrawBankCardData = it
                    if (it != null) {
                        viewModel.setupWithdrawCard(it)
                    }
                    binding.etWithdrawalAmount.resetText()
                })

        with(binding.rvBankItem) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = bankCardAdapter
        }
        bankCardAdapter.initSelectStatus()
    }

    private fun getBankCardTailNo(data: BankCardList?): String {
        return String.format(
            getString(R.string.selected_bank_card),
            data?.bankName ?: "",
            data?.cardNo ?: ""
        )
    }

    private fun selectBetStationTab(select: Boolean) {
        if (select) {
            binding.linWithdraw.visibility = View.GONE
            binding.flBetStation.visibility = View.VISIBLE
            if (!::betStationFragment.isInitialized) {
                betStationFragment = BetStationFragment()
                childFragmentManager.beginTransaction()
                    .add(R.id.fl_bet_station, betStationFragment)
                    .commit()
            } else {
                childFragmentManager.beginTransaction().show(betStationFragment).commit()
            }

            return
        }

        binding.linWithdraw.visibility = View.VISIBLE
        binding.flBetStation.visibility = View.GONE
        if (this::betStationFragment.isInitialized){
            if (betStationFragment != null && betStationFragment.isAdded) {
                childFragmentManager.beginTransaction().hide(betStationFragment).commit()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBankCardChange(event: BankCardChangeEvent){
        setupData()
    }


}



