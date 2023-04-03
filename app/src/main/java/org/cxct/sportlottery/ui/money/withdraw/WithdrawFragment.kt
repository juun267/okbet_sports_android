package org.cxct.sportlottery.ui.money.withdraw

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

/**
 * @app_destination 提款-tab
 */
class WithdrawFragment : BaseSocketFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private lateinit var bankCardAdapter: WithdrawBankCardAdapter
    private var withdrawBankCardData: BankCardList? = null
    private lateinit var betStationFragment: BetStationFragment
    lateinit var transferTypeAddSwitch : TransferTypeAddSwitch
    private val zero = 0.0
    private var dealType: TransferType = TransferType.BANK
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_withdraw, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
        initObserve(view)
        setupData()
        setupServiceButton()
    }

    private fun setupData() {
        viewModel.apply {
            getMoneyConfigs()
            getUwCheck()
        }
    }

    private fun setupServiceButton() {
        tv_service.setServiceClick(childFragmentManager)
    }
    private fun initView() {
        et_withdrawal_amount.apply {
            clearIsShow = false
            getAllIsShow = true
        }
        tv_add_bank.setOnClickListener {
            var intent = Intent(context, BankActivity::class.java)
            var numType:Int = 1
            when(dealType){
                TransferType.BANK-> numType = 1
                TransferType.CRYPTO -> numType =2
                TransferType.E_WALLET -> numType = 3
            }
            intent.putExtra("add_bank", numType)
            startActivity(intent)
        }


        ll_tab_layout.visibility = View.GONE // 預設先隱藏 等待卡片資料讀取完畢後再顯示

        btn_info.setOnClickListener {
            CommissionInfoDialog().show(childFragmentManager, null)
        }

        tv_detail.setOnClickListener {
            startActivity(Intent(activity, WithdrawCommissionDetailActivity::class.java))
        }

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
    }

    private fun setupDealView(type: TransferType) {
        when (type) {
            TransferType.BANK -> {
                tv_channel_select.text = getString(R.string.select_bank)
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
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
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
                            add_bank_group.visibility = if (walletTransfer)View.VISIBLE else View.GONE
                        }
                        tv_add_bank.text = context?.getString(R.string.bank_list_add, context?.getString(R.string.bank_list_e_wallet))
                        clearEvent()
                    }
//                    getString(R.string.pay_maya) -> {
//                        selectBetStationTab(false)
//                        selectDealType(TransferType.PAYMAYA)
//                        dealType = TransferType.PAYMAYA
//                        transferTypeAddSwitch.apply {
//                            add_bank_group.visibility = if (walletTransfer)View.VISIBLE else View.GONE
//                        }
//                        tv_add_bank.text = context?.getString(R.string.bank_list_add, context?.getString(R.string.pay_maya))
//                        clearEvent()
//                    }
                    getString(R.string.Outlets_Reserve) -> {
                        selectBetStationTab(true)
                        viewModel.setDealType(TransferType.STATION)
                        dealType = TransferType.STATION
                        clearEvent()
                    }
                }
                tab?.let {
                    if (TextUtils.equals(it.text, getString(R.string.Outlets_Reserve))) {
                        viewModel.setVisibleView(false)
                    } else {
                        viewModel.setVisibleView(true)
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

//            tab_bank_card.setOnClickListener {
//            if (!it.isSelected) {
//                selectDealType(TransferType.BANK)
//                clearEvent()
//            }
//        }
//
//        tab_crypto.setOnClickListener {
//            if (!it.isSelected) {
//                selectDealType(TransferType.CRYPTO)
//                clearEvent()
//            }
//        }
//
//        tab_wallet.setOnClickListener {
//            if (!it.isSelected) {
//                selectDealType(TransferType.E_WALLET)
//                clearEvent()
//            }
//        }


        btn_withdraw.setOnClickListener {
            modifyFinish()
            withdrawBankCardData?.let {
                viewModel.addWithdraw(
                    withdrawBankCardData,
                    et_withdrawal_amount.getText(),
                    et_withdrawal_password.getText(),
                    null,
                    null,
                    null,
                )
            }
        }

        btn_withdraw.setTitleLetterSpacing()

        et_withdrawal_amount.getAllButton {
            it.setText(viewModel.getWithdrawAmountLimit().max.toLong().toString())
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

    private fun clearEvent() {
        et_withdrawal_amount.setText("")
        et_withdrawal_password.setText("")
        bankCardAdapter.initSelectStatus()
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
            tv_balance.text = TextUtil.format(ArithUtil.toMoneyFormat(it).toDouble())
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

            val tabIndexList = viewModel.withdrawTabIsShow.value
            when {
                bankCardExist == true -> {
//                    tab_bank_card.isChecked = true
                    tab_layout.getTabAt(tabIndexList?.indexOf(TransferType.BANK.type)?:0)?.select()
                    viewModel.setDealType(TransferType.BANK)
                }
                cryptoCardExist == true -> {
//                    tab_crypto.isChecked = true
                    tab_layout.getTabAt(tabIndexList?.indexOf(TransferType.CRYPTO.type)?:0)?.select()
                    viewModel.setDealType(TransferType.CRYPTO)
                }
                eWalletCardExist == true -> {
//                    tab_e_wallet.isChecked = true
                    var aaaa = tabIndexList?.indexOf(TransferType.E_WALLET.type)?:0
                    tab_layout.getTabAt(tabIndexList?.indexOf(TransferType.E_WALLET.type)?:0)?.select()
                    viewModel.setDealType(TransferType.E_WALLET)
                }
                stationExit == true -> {
//                    tab_e_wallet.isChecked = true
                    tab_layout.getTabAt(tabIndexList?.indexOf(TransferType.STATION.type)?:0)?.select()
                    viewModel.setDealType(TransferType.STATION)
                }
                else -> {
                    jumpToMoneyCardSetting()
                }
            }
        })
        //Tab 顯示判斷
        viewModel.withdrawTabIsShow.observe(this.viewLifecycleOwner) { list ->

            if (list.isNullOrEmpty() || list.size == 1) {
                tab_layout.visibility = View.GONE
                ll_tab_layout.visibility = View.GONE
            } else {
                tab_layout.visibility = View.VISIBLE
                ll_tab_layout.visibility = View.VISIBLE

                tab_layout.getTabAt(0)?.view?.visibility =
                    if (list.contains(TransferType.BANK.type)) View.VISIBLE else View.GONE
                tab_layout.getTabAt(1)?.view?.visibility =
                    if (list.contains(TransferType.CRYPTO.type)) View.VISIBLE else View.GONE
                tab_layout.getTabAt(2)?.view?.visibility =
                    if (list.contains(TransferType.E_WALLET.type)) View.VISIBLE else View.GONE

                if (list.contains(TransferType.STATION.type)) {
                    betStationFragment = BetStationFragment()
                    tab_layout.getTabAt(3)?.view?.visibility = View.VISIBLE
                } else {
                    tab_layout.getTabAt(3)?.view?.visibility = View.GONE
                }
            }

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
                    TransferType.STATION.type -> tab_layout.addTab(tab_layout.newTab().setText(R.string.Outlets_Reserve))
                }
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
                    et_withdrawal_amount.getText().toDouble()
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
                } else {
                   showErrorPromptDialog(getString(R.string.prompt), it.msg) {}
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
//                    val cardIcon = when (it.transferType) {
//                        TransferType.BANK -> getBankIconByBankName(it.bankName)
//                        TransferType.CRYPTO -> getCryptoIconByCryptoName(it.transferType.type)
//                        TransferType.E_WALLET -> getBankIconByBankName(it.bankName)
//                        TransferType.STATION -> getBankIconByBankName(it.bankName)
//                      //  TransferType.PAYMAYA -> getBankIconByBankName(it.bankName)
//                    }


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

                tvNumber.text = "(尾号${bankCard.cardNo})"
                tvBankCard.text = bankCard.bankName
                ivBankIcon.setImageResource(
                    when (bankCard.transferType) {
                        TransferType.BANK -> getBankIconByBankName(bankCard.bankName)
                        TransferType.CRYPTO -> getCryptoIconByCryptoName(bankCard.transferType.type)
                        TransferType.E_WALLET -> getBankIconByBankName(bankCard.bankName)
                        TransferType.STATION -> getBankIconByBankName(bankCard.bankName)
                     //   TransferType.PAYMAYA -> getBankIconByBankName(bankCard.bankName)
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
