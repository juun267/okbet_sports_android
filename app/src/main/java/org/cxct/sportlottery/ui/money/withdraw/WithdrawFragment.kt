package org.cxct.sportlottery.ui.money.withdraw

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_withdraw.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.fragment_withdraw.*
import kotlinx.android.synthetic.main.fragment_withdraw.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.BankCardChangeEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.toDoubleS
import org.cxct.sportlottery.databinding.ItemListviewBankCardBinding
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.money.withdraw.BankActivity.Companion.ModifyBankTypeKey
import org.cxct.sportlottery.ui.money.withdraw.BankActivity.Companion.TransferTypeAddSwitch
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.MoneyManager.getBankIconByBankName
import org.cxct.sportlottery.util.MoneyManager.getCryptoIconByCryptoName
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.min

/**
 * @app_destination 提款-tab
 */
class WithdrawFragment : BaseSocketFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private lateinit var bankCardAdapter: WithdrawBankCardAdapter
    private var withdrawBankCardData: BankCardList? = null
    private lateinit var betStationFragment: BetStationFragment
    lateinit var transferTypeAddSwitch: TransferTypeAddSwitch
    private val zero = 0.0
    private var dealType: TransferType = TransferType.BANK

    override fun layoutId() = R.layout.fragment_withdraw
    override fun onBindView(view: View) {
        initView()
        initEvent()
        initObserve(view)
        setupServiceButton()
        setupData()
        EventBusUtil.targetLifecycle(this)
    }

    private fun setupData() = viewModel.run {
        getBankCardList()
        getMoneyConfigs()
        getUwCheck()
    }

    private fun setupServiceButton() {
        tv_service.setServiceClick(childFragmentManager)
    }
    private fun initView() {
        et_withdrawal_amount.clearIsShow = false
        et_withdrawal_amount.getAllIsShow = true

        tv_add_bank.setOnClickListener {
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

        ll_tab_layout.gone() // 預設先隱藏 等待卡片資料讀取完畢後再顯示
        btn_info.setOnClickListener {CommissionInfoDialog().show(childFragmentManager, null) }
        tv_detail.setOnClickListener {startActivity(WithdrawCommissionDetailActivity::class.java) }
        tv_currency_type.text = sConfigData?.systemCurrencySign

        checkNotificationVisiable(
            tv_notification_1,
            tv_dot_1,
            sConfigData?.minRechMoney,
            getString(
                R.string.initial_withdrawal_needs_credited,
                sConfigData?.systemCurrencySign,
                sConfigData?.minRechMoney
            )
        )

        if (sConfigData?.enableMinRemainingBalance == FLAG_OPEN) {
            checkNotificationVisiable(
                tv_notification_2,
                tv_dot_2,
                sConfigData?.minRemainingBalance,
                getString(
                    R.string.make_sure_valid_account,
                    sConfigData?.systemCurrencySign,
                    sConfigData?.minRemainingBalance
                )
            )
        }

        tv_add_bank.text = context?.getString(R.string.bank_list_add, context?.getString(R.string.bank_list_bank))
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

    private fun setupDealView(type: TransferType) {
        when (type) {
            TransferType.BANK -> {
                tv_channel_select.text = getString(R.string.select_bank)
                et_withdrawal_amount.setTitle(getString(R.string.withdraw_amount))
            }
            TransferType.E_WALLET, TransferType.PAYMAYA -> {
                tv_channel_select.text = getString(R.string.ewallet)
                et_withdrawal_amount.setTitle(getString(R.string.withdraw_amount))
            }
            TransferType.CRYPTO -> {
                tv_channel_select.text = getString(R.string.select_crypto_address)
                et_withdrawal_amount.setTitle(getString(R.string.withdraw_number))
            }
        }
    }
    private fun setupClickEvent() {
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabSelect(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        btn_withdraw.setOnClickListener {
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
                        et_withdrawal_amount.getText(),
                        et_withdrawal_password.getText(),
                        null,
                        null,
                        null,
                    )
                }
            }?.show(childFragmentManager,DeductDialog::class.java.name)
        }

        btn_withdraw.setTitleLetterSpacing()

        et_withdrawal_amount.getAllButton {
            it.setText(min(viewModel.getWithdrawAmountLimit().max,
                viewModel.userMoney.value ?: 0.0).toString())
            et_withdrawal_amount.et_input.apply { setSelection(this.length()) }
        }
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //提款金額
            et_withdrawal_amount.afterTextChanged { checkWithdrawAmount(withdrawBankCardData, it) }

            //提款密碼
            et_withdrawal_password.afterTextChanged { checkWithdrawPasswordByWithdrawPage(it) }
        }
    }
    private fun tabSelect(tab:TabLayout.Tab){
        when (tab.text) {
            getString(R.string.bank_card) -> {
                selectBetStationTab(false)
                selectDealType(TransferType.BANK)
                dealType = TransferType.BANK
                transferTypeAddSwitch.apply {
                    add_bank_group.visibility = if (bankTransfer)View.VISIBLE else View.GONE
                }
                tv_add_bank.text = context?.getString(R.string.bank_list_add, context?.getString(R.string.bank_list_bank))
                clearEvent()
            }
            getString(R.string.crypto) -> {
                selectBetStationTab(false)
                selectDealType(TransferType.CRYPTO)
                dealType = TransferType.CRYPTO
                transferTypeAddSwitch.apply {
                    add_bank_group.visibility = if (cryptoTransfer)View.VISIBLE else View.GONE
                }
                tv_add_bank.text = context?.getString(R.string.bank_list_add, context?.getString(R.string.bank_list_crypto))
                clearEvent()
            }
            getString(R.string.ewallet) -> {
                selectBetStationTab(false)
                selectDealType(TransferType.E_WALLET)
                dealType = TransferType.E_WALLET
                transferTypeAddSwitch.apply {
                    add_bank_group.visibility =
                        if (walletTransfer) View.VISIBLE else View.GONE
                }
                tv_add_bank.text = context?.getString(R.string.bank_list_add,
                    context?.getString(R.string.bank_list_e_wallet))
                clearEvent()
            }
            getString(R.string.online_maya) -> {
                selectBetStationTab(false)
                selectDealType(TransferType.PAYMAYA)
                dealType = TransferType.PAYMAYA
                transferTypeAddSwitch.apply {
                    add_bank_group.visibility =
                        if (paymataTransfer) View.VISIBLE else View.GONE
                }
                tv_add_bank.text = context?.getString(R.string.bank_list_add,
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
        et_withdrawal_amount.setText("")
        et_withdrawal_password.setText("")
        if (::bankCardAdapter.isInitialized) {
            bankCardAdapter.initSelectStatus()
        }
        viewModel.resetWithdrawPage()
        modifyFinish()
    }

    private fun updateButtonStatus(isEnable: Boolean) {
        if (isEnable) {
            btn_withdraw.isEnabled = true
            btn_withdraw.alpha = 1.0f
        } else {
            btn_withdraw.isEnabled = false
            btn_withdraw.alpha = 0.5f
        }
    }

    private fun initObserve(view: View) {
        viewModel.submitEnable.observe(viewLifecycleOwner){
            updateButtonStatus(it)
        }
        viewModel.addMoneyCardSwitch.observe(this.viewLifecycleOwner) {
            transferTypeAddSwitch = it
        }
        viewModel.commissionCheckList.observe(this.viewLifecycleOwner) {
            tv_detail.apply {
                isEnabled = it.isNotEmpty()
                isSelected = it.isNotEmpty()
            }
        }

        viewModel.needCheck.observe(this.viewLifecycleOwner, Observer {
            ll_commission.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewModel.deductMoney.observe(this.viewLifecycleOwner, Observer {
            tv_commission.apply {
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
        })

        viewModel.loading.observe(this.viewLifecycleOwner, Observer {
            if (it)
                loading()
            else
                hideLoading()
        })

        viewModel.withdrawAmountTotal.observe(this.viewLifecycleOwner) {
            //提款总计加入千分符
            tv_withdrawal_total.text = TextUtil.format(it)

        }

        viewModel.userMoney.observe(this.viewLifecycleOwner, Observer {
            tv_balance.text = TextUtil.format(ArithUtil.toMoneyFormat(it).toDoubleS())
            viewModel.getWithdrawHint()
        })

        viewModel.moneyCardList.observe(this.viewLifecycleOwner, Observer {
            if (it.transferType == TransferType.STATION)
                return@Observer
            val cardList = it.cardList
            if (cardList.isEmpty()) {
                jumpToMoneyCardSetting(true, it.transferType)
                return@Observer
            }
            initSelectBankCardBottomSheet(view, cardList.toMutableList())
        })

        viewModel.moneyCardExist.observe(this.viewLifecycleOwner, Observer { moneyCardSet ->
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
                    tab_layout.getTabAt(0)?.let {
                        it.select()
                        tabSelect(it)
                    }
                }
                else -> {
                    jumpToMoneyCardSetting()
                }
            }
        })
        //Tab 顯示判斷
        viewModel.withdrawTabIsShow.observe(this.viewLifecycleOwner) { list ->
            tab_layout.removeAllTabs()
            list.forEach { type->
                when(type){
                    TransferType.BANK.type -> {
                        tab_layout.addTab(tab_layout.newTab().setText(R.string.bank_card))
                        transferTypeAddSwitch.apply {
                            add_bank_group.visibility = if (bankTransfer)View.VISIBLE else View.GONE
                        }
                    }
                    TransferType.CRYPTO.type -> tab_layout.addTab(tab_layout.newTab().setText(R.string.crypto))
                    TransferType.E_WALLET.type -> tab_layout.addTab(tab_layout.newTab().setText(R.string.ewallet))
                    TransferType.STATION.type -> tab_layout.addTab(tab_layout.newTab()
                        .setText(R.string.Outlets_Reserve))
                    TransferType.PAYMAYA.type -> tab_layout.addTab(tab_layout.newTab()
                        .setText(R.string.online_maya))
                }
            }
            if (list.isNullOrEmpty() || list.size == 1) {
                tab_layout.visibility = View.GONE
                ll_tab_layout.visibility = View.GONE
            } else {
                tab_layout.visibility = View.VISIBLE
                ll_tab_layout.visibility = View.VISIBLE
            }
            setupClickEvent()
        }

        //資金設定
        viewModel.rechargeConfigs.observe(this.viewLifecycleOwner) {
            if (et_withdrawal_amount.getText().isEmpty()) {
                viewModel.getWithdrawRate(withdrawBankCardData)
            } else {
                viewModel.getWithdrawRate(
                    withdrawBankCardData,
                    et_withdrawal_amount.getText().toDoubleS()
                )
            }
            viewModel.getWithdrawHint()
        }

        //提款金額提示訊息
        viewModel.withdrawAmountHint.observe(this.viewLifecycleOwner) {
            et_withdrawal_amount.et_input.hint = it
        }

        //提款金額訊息
        viewModel.withdrawAmountMsg.observe(this.viewLifecycleOwner) {
            et_withdrawal_amount.setError(it)
        }

        //提款手續費提示
        viewModel.withdrawRateHint.observe(this.viewLifecycleOwner) {
            tv_hint_withdraw_rate.text = it
        }

        //提款虛擬幣所需餘額
        viewModel.withdrawCryptoAmountHint.observe(this.viewLifecycleOwner) {
            tv_hint_withdraw_crypto_amount.visibility = if (it.isEmpty())
                View.GONE
            else {
                tv_hint_withdraw_crypto_amount.text = it
                View.VISIBLE
            }
        }

        //提款虛擬幣手續費
        viewModel.withdrawCryptoFeeHint.observe(this.viewLifecycleOwner) {
            tv_hint_withdraw_crypto_fee.visibility = if (it.isEmpty())
                View.GONE
            else {
                tv_hint_withdraw_crypto_fee.text = it
                View.VISIBLE
            }
        }


        //提款密碼訊息
        viewModel.withdrawPasswordMsg.observe(this.viewLifecycleOwner) {
            et_withdrawal_password.setError(it ?: "")
        }
        //提款
        viewModel.withdrawAddResultData.observe(this.viewLifecycleOwner) {
            if (lin_withdraw.isVisible)
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
        view: View,
        bankCardList: MutableList<BankCardList>
    ) { //TODO Dean : 重構BottomSheet
        bankCardAdapter =
            WithdrawBankCardAdapter(
                requireContext(),
                bankCardList,
                BankCardAdapterListener {
                    val cardIcon = when (it.transferType) {
                        TransferType.BANK -> getBankIconByBankName(it.bankName)
                        TransferType.CRYPTO -> getCryptoIconByCryptoName(it.transferType.type)
                        TransferType.E_WALLET -> getBankIconByBankName(it.bankName)
                        TransferType.STATION -> getBankIconByBankName(it.bankName)
                        TransferType.PAYMAYA -> getBankIconByBankName(it.bankName)
                    }


                    withdrawBankCardData = it
                    viewModel.setupWithdrawCard(it)

                    view.et_withdrawal_amount.resetText()


                })

        with(rv_bank_item) {
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
            lin_withdraw.visibility = View.GONE
            fl_bet_station.visibility = View.VISIBLE
            if (betStationFragment != null) {
                betStationFragment = BetStationFragment()
            }
            if (betStationFragment.isAdded) {
                childFragmentManager.beginTransaction().show(betStationFragment).commit()
            } else {
                childFragmentManager.beginTransaction().add(R.id.fl_bet_station, betStationFragment)
                    .commit()
            }
        } else {
            lin_withdraw.visibility = View.VISIBLE
            fl_bet_station.visibility = View.GONE
            if (this::betStationFragment.isInitialized){
                if (betStationFragment != null && betStationFragment.isAdded) {
                    childFragmentManager.beginTransaction().hide(betStationFragment).commit()
                }
            }

        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBankCardChange(event: BankCardChangeEvent){
        setupData()
    }
}

class WithdrawBankCardAdapter(
    private val context: Context,
    private val dataList: MutableList<BankCardList>,
    private val listener: BankCardAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPosition = 0

    fun initSelectStatus() {
        //初始化選中狀態
        dataList.forEach { bankCard ->
            bankCard.isSelected = false
        }
        dataList.firstOrNull()?.isSelected = true

        selectedPosition = 0
        listener.onClick(dataList[0])
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        WithdrawBankItemViewHolder(
            ItemListviewBankCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is WithdrawBankItemViewHolder -> {
            holder.bind(dataList[position], position)
        }
        else -> {

        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class WithdrawBankItemViewHolder(val binding: ItemListviewBankCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(bankCard: BankCardList, position: Int) {
            with(binding) {
                root.setOnClickListener {
                    selectBankCard(position)
                    listener.onClick(bankCard)
                    notifyDataSetChanged()
                }
                checkBank.setOnClickListener {
                    selectBankCard(position)
                    listener.onClick(bankCard)
                    notifyDataSetChanged()
                }

                tvNumber.text = "(${context.getString(R.string.tail_number)}${bankCard.cardNo})"
                tvBankCard.text = bankCard.bankName
                ivBankIcon.setImageResource(
                    when (bankCard.transferType) {
                        TransferType.BANK -> getBankIconByBankName(bankCard.bankName)
                        TransferType.CRYPTO -> getCryptoIconByCryptoName(bankCard.transferType.type)
                        TransferType.E_WALLET -> getBankIconByBankName(bankCard.bankName)
                        TransferType.STATION -> getBankIconByBankName(bankCard.bankName)
                        TransferType.PAYMAYA -> getBankIconByBankName(bankCard.bankName)
                    }
                )
                checkBank.isChecked = selectedPosition == position
            }
        }
    }


    private fun selectBankCard(bankPosition: Int) {
        dataList[selectedPosition].isSelected = false
        notifyItemChanged(selectedPosition)
        selectedPosition = bankPosition
        dataList[bankPosition].isSelected = true
        notifyItemChanged(bankPosition)
    }
}


class BankCardAdapterListener(val listener: (bankCard: BankCardList) -> Unit) {
    fun onClick(bankCard: BankCardList) = listener(bankCard)
}

