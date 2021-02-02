package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_bank_card.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.fragment_withdraw.*
import kotlinx.android.synthetic.main.fragment_withdraw.view.*
import kotlinx.android.synthetic.main.item_listview_bank_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.MoneyManager
import org.cxct.sportlottery.util.ToastUtil

class WithdrawFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private lateinit var bankCardBottomSheet: BottomSheetDialog
    private lateinit var bankCardAdapter: BankCardAdapter
    private var withdrawBankCardData: BankCardList? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_withdraw, container, false).apply {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initEvent()
        initObserve(view)

        setupData()
    }

    private fun setupData() {
        viewModel.apply {
            getMoneyConfigs()
            getBankCardList()
            getMoney()
        }
    }

    private fun initView() {
        et_withdrawal_amount.apply {
            clearIsShow = false
            getAllIsShow = true
        }
    }

    private fun initEvent() {
        setupClickEvent()
        setupTextChangeEvent()
    }


    private fun setupClickEvent() {
        ll_select_bank.setOnClickListener {
            bankCardBottomSheet.show()
        }

        btn_withdraw.setOnClickListener {
            modifyFinish()
            withdrawBankCardData?.let { viewModel.addWithdraw(it.id.toLong(), et_withdrawal_amount.getText(), et_withdrawal_password.getText()) }

        }

        btn_reset.setOnClickListener {
            clearEvent()
        }

        et_withdrawal_amount.getAllButton {
            it.setText(viewModel.getWithdrawAmountLimit().max.toString())
            et_withdrawal_amount.et_input.apply { setSelection(this.length()) }
        }
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //提款金額
            et_withdrawal_amount.afterTextChanged { checkWithdrawAmount(it) }

            //提款密碼
            setupEyeButtonVisibility(et_withdrawal_password) { checkWithdrawPassword(it) }
        }
    }

    private fun setupEyeButtonVisibility(setupView: LoginEditText, checkFun: (String) -> Unit) {
        setupView.let { view ->
            view.afterTextChanged {
                view.eyeVisibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
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
            tv_balance.text = ArithUtil.toMoneyFormat(it)
        })

        viewModel.bankCardList.observe(this.viewLifecycleOwner, Observer {
            it.bankCardList?.let { list ->
                val iniData = it.bankCardList[0]
                withdrawBankCardData = iniData
                tv_select_bank_card.text = getBankCardTailNo(iniData)
                iv_bank_card_icon.setImageResource(MoneyManager.getBankIconByBankName(iniData.bankName))
                initSelectBankCardBottomSheet(view, list.toMutableList())
            }
        })

        //資金設定
        viewModel.rechargeConfigs.observe(this.viewLifecycleOwner, Observer {
            if (et_withdrawal_amount.getText().isEmpty()) {
                viewModel.getWithdrawRate(0)
            } else {
                viewModel.getWithdrawRate(et_withdrawal_amount.getText().toLong())
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

        //提款密碼訊息
        viewModel.withdrawPasswordMsg.observe(this.viewLifecycleOwner, Observer {
            et_withdrawal_password.setError(it ?: "")
        })

        //提款
        viewModel.withdrawAddResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                clearEvent()
                ToastUtil.showToastInCenter(context, getString(R.string.text_money_get_success))
                viewModel.getMoney()
            } else {
                showPromptDialog(getString(R.string.title_withdraw_fail), it.msg) {}
            }
        })
    }

    private fun initSelectBankCardBottomSheet(view: View, bankCardList: MutableList<BankCardList>) { //TODO Dean : 重構BottomSheet
        val bankCardBottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_bank_card, null)
        bankCardBottomSheet = BottomSheetDialog(requireContext())
        bankCardBottomSheet.apply {
            setContentView(bankCardBottomSheetView)
            bankCardAdapter = BankCardAdapter(lv_bank_item.context, bankCardList, BankCardAdapterListener {
                view.iv_bank_card_icon.setImageResource(MoneyManager.getBankIconByBankName(it.bankName))
                view.tv_select_bank_card.text = getBankCardTailNo(it)
                withdrawBankCardData = it
                dismiss()
            })
            lv_bank_item.adapter = bankCardAdapter
            bankCardBottomSheet.btn_close.setOnClickListener {
                this.dismiss()
            }
        }
    }

    private fun getBankCardTailNo(data: BankCardList): String {
        return String.format(getString(R.string.selected_bank_card), data.bankName, data.cardNo)
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
            /*val viewHolder = ViewHolder()*/
            tvBank?.text = data.bankName
            ivBankIcon?.setImageResource(MoneyManager.getBankIconByBankName(data.bankName))
            if (position == selectedPosition)
                this.llSelectBankCard?.setBackgroundColor(ContextCompat.getColor(context, R.color.blue2))
            else
                llSelectBankCard?.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            llSelectBankCard?.setOnClickListener {
                if (selectedPosition != position) {
                    //                data.isSelected = !data.isSelected
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