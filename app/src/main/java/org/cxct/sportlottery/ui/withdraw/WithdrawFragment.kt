package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.content_common_bottom_sheet_item.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_bank_card.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.fragment_withdraw.*
import kotlinx.android.synthetic.main.fragment_withdraw.view.*
import kotlinx.android.synthetic.main.item_listview_bank_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.money.TransferType
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.ui.withdraw.BankActivity.Companion.ModifyBankTypeKey
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.MoneyManager.getBankIconByBankName
import org.cxct.sportlottery.util.MoneyManager.getCryptoIconByCryptoName
import org.cxct.sportlottery.util.TextUtil


class WithdrawFragment : BaseSocketFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private lateinit var bankCardBottomSheet: BottomSheetDialog
    private lateinit var bankCardAdapter: BankCardAdapter
    private var withdrawBankCardData: BankCardList? = null

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
        initSocketObserver()

        setupData()
    }

    private fun setupData() {
        viewModel.apply {
            getMoneyConfigs()
        }
    }

    private fun initView() {
        et_withdrawal_amount.apply {
            clearIsShow = false
            getAllIsShow = true
        }
        block_tab.visibility = View.GONE // 預設先隱藏 等待卡片資料讀取完畢後再顯示
    }

    private fun initEvent() {
        setupClickEvent()
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
        tab_bank_card.setOnClickListener {
            if (!it.isSelected) {
                selectDealType(TransferType.BANK)
                clearEvent()
            }
        }

        tab_crypto.setOnClickListener {
            if (!it.isSelected) {
                selectDealType(TransferType.CRYPTO)
                clearEvent()
            }
        }

        ll_select_bank.setOnClickListener {
            bankCardBottomSheet.show()
        }

        btn_withdraw.setOnClickListener {
            modifyFinish()
            withdrawBankCardData?.let { viewModel.addWithdraw(withdrawBankCardData, et_withdrawal_amount.getText(), et_withdrawal_password.getText()) }
        }

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
            setupEyeButtonVisibility(et_withdrawal_password) { checkWithdrawPassword(it) }
        }
    }

    private fun setupEyeButtonVisibility(setupView: LoginEditText, checkFun: (String) -> Unit) {
        setupView.let { view ->
            view.afterTextChanged {
                checkFun(it)
            }
        }
    }

    private fun clearEvent() {
        et_withdrawal_amount.setText("")
        et_withdrawal_password.setText("")
        bankCardAdapter.initSelectStatus()
        viewModel.resetWithdrawPage()
        modifyFinish()
    }

    private fun initObserve(view: View) {
        viewModel.loading.observe(this.viewLifecycleOwner, Observer {
            if (it)
                loading()
            else
                hideLoading()
        })

        viewModel.userMoney.observe(this.viewLifecycleOwner, Observer {
            tv_balance.text = TextUtil.format(ArithUtil.toMoneyFormat(it).toDouble())
            viewModel.getWithdrawHint()
        })

        viewModel.moneyCardList.observe(this.viewLifecycleOwner, Observer {
            val cardList = it.cardList
            if (cardList.isEmpty()) {
                jumpToMoneyCardSetting(true, it.transferType)
                return@Observer
            }
            val initData = cardList.firstOrNull()
            initData?.let { bankCardList ->
                setupDealView(bankCardList.transferType)
                withdrawBankCardData = initData
                tv_select_bank_card.text = getBankCardTailNo(initData)
                bankCardList.bankName.let { bankName ->
                    iv_bank_card_icon.setImageResource(getBankIconByBankName(bankName))
                }
            }
            initSelectBankCardBottomSheet(view, cardList.toMutableList())
        })

        viewModel.moneyCardExist.observe(this.viewLifecycleOwner, Observer { moneyCardSet ->
            val bankCardExist = moneyCardSet.find { it.transferType == TransferType.BANK }?.exist
            val cryptoCardExist = moneyCardSet.find { it.transferType == TransferType.CRYPTO }?.exist
            
            when {
                bankCardExist == true -> {
                    tab_bank_card.isChecked = true
                    viewModel.setDealType(TransferType.BANK)
                }
                cryptoCardExist == true -> {
                    tab_crypto.isChecked = true
                    viewModel.setDealType(TransferType.CRYPTO)
                }
                else -> {
                    jumpToMoneyCardSetting()
                }
            }
        })
        //Tab 顯示判斷
        viewModel.withdrawSystemOperation.observe(this.viewLifecycleOwner, Observer {
            isShow ->
            if (isShow.getContentIfNotHandled() == true)
                block_tab.visibility = View.VISIBLE
            else
                block_tab.visibility = View.GONE
        })

        //資金設定
        viewModel.rechargeConfigs.observe(this.viewLifecycleOwner, Observer {
            if (et_withdrawal_amount.getText().isEmpty()) {
                viewModel.getWithdrawRate(withdrawBankCardData)
            } else {
                viewModel.getWithdrawRate(withdrawBankCardData, et_withdrawal_amount.getText().toDouble())
            }
            viewModel.getWithdrawHint()
        })

        //提款金額提示訊息
        viewModel.withdrawAmountHint.observe(this.viewLifecycleOwner, Observer {
            et_withdrawal_amount.et_input.hint = it
        })

        //提款金額訊息
        viewModel.withdrawAmountMsg.observe(this.viewLifecycleOwner, Observer {
            et_withdrawal_amount.setError(it)
        })

        //提款手續費提示
        viewModel.withdrawRateHint.observe(this.viewLifecycleOwner, Observer {
            tv_hint_withdraw_rate.text = it
        })

        //提款虛擬幣所需餘額
        viewModel.withdrawCryptoAmountHint.observe(this.viewLifecycleOwner, Observer {
            tv_hint_withdraw_crypto_amount.visibility = if (it.isEmpty())
                View.GONE
            else {
                tv_hint_withdraw_crypto_amount.text = it
                View.VISIBLE
            }
        })

        //提款虛擬幣手續費
        viewModel.withdrawCryptoFeeHint.observe(this.viewLifecycleOwner, Observer {
            tv_hint_withdraw_crypto_fee.visibility = if (it.isEmpty())
                View.GONE
            else {
                tv_hint_withdraw_crypto_fee.text = it
                View.VISIBLE
            }
        })

        //提款密碼訊息
        viewModel.withdrawPasswordMsg.observe(this.viewLifecycleOwner, Observer {
            et_withdrawal_password.setError(it ?: "")
        })

        //提款
        viewModel.withdrawAddResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                clearEvent()
                showPromptDialog(getString(R.string.prompt), getString(R.string.text_money_get_success)) { viewModel.getMoney() }
            } else {
                showErrorPromptDialog(getString(R.string.prompt), it.msg) {}
            }
        })
    }

    /**
     * 跳轉至資金卡新增頁面
     * @param assignType 是否指定跳轉新增型態(銀行卡, 虛擬幣)
     */
    private fun jumpToMoneyCardSetting(assignType: Boolean = false, transferType: TransferType? = null) {
        val content = when (transferType) {
                TransferType.CRYPTO -> {
                    getString(R.string.please_setting_crypto)
                }
                else -> {
                    getString(R.string.please_setting_bank_card)
                }
            }
        showPromptDialog(getString(R.string.withdraw_setting),  content) {
            this@WithdrawFragment.activity?.finish()
            startActivity(Intent(requireContext(), BankActivity::class.java).apply { if (assignType) putExtra(ModifyBankTypeKey, transferType) })
        }
    }

    private fun initSocketObserver() {
        receiver.userMoney.observe(this.viewLifecycleOwner, Observer {
            tv_balance.text = TextUtil.format(ArithUtil.toMoneyFormat(it).toDouble())
            viewModel.getMoney() //TODO : 是否應該要讓將socket&api的userMoney統一來源
        })
    }

    private fun initSelectBankCardBottomSheet(
        view: View,
        bankCardList: MutableList<BankCardList>
    ) { //TODO Dean : 重構BottomSheet
        val bankCardBottomSheetView =
            layoutInflater.inflate(R.layout.dialog_bottom_sheet_bank_card, null)
        bankCardBottomSheet = BottomSheetDialog(requireContext())
        bankCardBottomSheet.apply {
            setContentView(bankCardBottomSheetView)
            bankCardAdapter = BankCardAdapter(lv_bank_item.context, bankCardList, BankCardAdapterListener {
                val cardIcon = when (it.transferType) {
                    TransferType.BANK -> getBankIconByBankName(it.bankName)
                    TransferType.CRYPTO -> getCryptoIconByCryptoName(it.transferType.type)
                }
                view.iv_bank_card_icon.setImageResource(cardIcon)

                view.tv_select_bank_card.text = getBankCardTailNo(it)

                withdrawBankCardData = it
                viewModel.setupWithdrawCard(it)
                dismiss()
            })
            lv_bank_item.adapter = bankCardAdapter

            bankCardBottomSheet.tv_game_type_title.text = when (bankCardList.firstOrNull()?.transferType) {
                TransferType.CRYPTO -> getString(R.string.select_crypto_card)
                else -> getString(R.string.select_bank)
            }
            bankCardBottomSheet.btn_close.setOnClickListener {
                this.dismiss()
            }
        }
        bankCardAdapter.initSelectStatus()
    }

    private fun getBankCardTailNo(data: BankCardList?): String {
        return String.format(getString(R.string.selected_bank_card), data?.bankName ?: "", data?.cardNo ?: "")
    }
}

class BankCardAdapter(private val context: Context, private val dataList: MutableList<BankCardList>, private val listener: BankCardAdapterListener) : BaseAdapter() {

    private var selectedPosition = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val data = dataList[position]
        val holder: ListViewHolder
        // if remove "if (convertView == null)" will get a warning about reuse view.
        if (convertView == null) {
            holder = ListViewHolder()
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.item_listview_bank_card, parent, false)
            view.tag = holder

            view.apply {
                holder.imgCheck = img_check_bank
                holder.ivBankIcon = iv_bank_icon
                holder.tvBank = tv_bank_card
                holder.llSelectBankCard = ll_select_bank_card
                setView(holder, data, position, listener)
            }
            return view
        } else {
            holder = convertView.tag as ListViewHolder
            setView(holder, data, position, listener)
        }
        return convertView
    }

    private fun setView(holder: ListViewHolder, data: BankCardList, position: Int, listener: BankCardAdapterListener) {
        holder.apply {
            imgCheck?.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
            tvBank?.text = tvBank?.context?.getString(R.string.selected_bank_card)?.let { String.format(it, data.bankName, data.cardNo) }
            val cardIcon = when (data.transferType) {
                TransferType.BANK -> getBankIconByBankName(data.bankName)
                TransferType.CRYPTO -> getCryptoIconByCryptoName(data.transferType.type)
            }
            ivBankIcon?.setImageResource(cardIcon)
            if (position == selectedPosition)
                this.llSelectBankCard?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite6))
            else
                llSelectBankCard?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            llSelectBankCard?.setOnClickListener {
                if (selectedPosition != position) {
                    selectedPosition = position
                    notifyDataSetChanged()
                    listener.onClick(data)
                }
            }
        }
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    fun initSelectStatus() {
        selectedPosition = 0
        listener.onClick(dataList[0])
        notifyDataSetChanged()
    }
}

class BankCardAdapterListener(val listener: (bankCard: BankCardList) -> Unit) {
    fun onClick(bankCard: BankCardList) = listener(bankCard)
}