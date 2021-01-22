package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_bank_card.*
import kotlinx.android.synthetic.main.fragment_bank_card.*
import kotlinx.android.synthetic.main.fragment_bank_card.view.*
import kotlinx.android.synthetic.main.item_listview_bank_card.view.*
import kotlinx.android.synthetic.main.item_listview_bank_card.view.iv_bank_icon
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.network.money.MoneyRechCfgData
import org.cxct.sportlottery.repository.sLoginData
import org.cxct.sportlottery.repository.sUserInfo
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.util.MoneyManager
import org.cxct.sportlottery.util.ToastUtil

class BankCardFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private lateinit var mBankSelectorBottomSheetDialog: BottomSheetDialog
    private lateinit var mBankSelectorAdapter: BankSelectorAdapter
    private val mNavController by lazy { findNavController() }
    private val args: BankCardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bank_card, container, false).apply {

            setupTitle()

            setupInitData(this)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {

            initView()

            setupEvent()

            setupObserve()

            setupBankList()
        }
    }

    private fun setupBankList() {
        viewModel.getMoneyConfigs()
    }

    private fun setupInitData(view: View) {
        val initData = args.editBankCard
        initData?.let {
            view.apply {
                btn_delete_bank.visibility = View.VISIBLE
                tv_bank_name.text = initData.bankName
                et_create_name.setText(sLoginData?.fullName)
                et_bank_card_number.setText(initData.cardNo)
                et_network_point.setText(initData.subAddress)
            }
            return@setupInitData
        }
        view.btn_delete_bank.visibility = View.GONE
    }

    private fun setupTitle() {
        when (val currentActivity = this.activity) {
            is WithdrawActivity -> {
                if (args.editBankCard != null) {
                    currentActivity.setToolBarName(getString(R.string.edit_bank_card))
                } else {
                    currentActivity.setToolBarName(getString(R.string.add_credit_card))
                }
            }
            is BankActivity -> {
                if (args.editBankCard != null) {
                    currentActivity.setToolBarName(getString(R.string.edit_bank_card))
                } else {
                    currentActivity.setToolBarName(getString(R.string.add_credit_card))
                }
            }
        }
    }

    private fun initView() {
        initEditTextStatus(et_create_name)
        initEditTextStatus(et_bank_card_number)
        initEditTextStatus(et_network_point)

        //避免自動記住密碼被人看到，把顯示密碼按鈕功能隱藏，直到密碼被重新編輯才顯示
        et_withdrawal_password.eyeVisibility = View.GONE

    }

    private fun initEditTextStatus(setupView: LoginEditText) {
        setupView.apply {
            clearIsShow = getText().isNotEmpty()
        }
    }

    private fun setupBankSelector(rechCfgData: MoneyRechCfgData) {
        mBankSelectorBottomSheetDialog = BottomSheetDialog(requireContext())
        mBankSelectorBottomSheetDialog.apply {
            val bankSelectorBottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_bank_card, null)
            setContentView(bankSelectorBottomSheetView)
            mBankSelectorAdapter = BankSelectorAdapter(lv_bank_item.context, rechCfgData.banks, BankSelectorAdapterListener {
                updateSelectedBank(it)
                dismiss()
            })
            lv_bank_item.adapter = mBankSelectorAdapter
            mBankSelectorAdapter.initSelectStatus()
            tv_game_type_title.text = getString(R.string.select_bank)
            btn_close.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun updateSelectedBank(bank: MoneyRechCfg.Bank) {
        tv_bank_name.text = bank.name
        iv_bank_icon.setImageResource(MoneyManager.getBankIconByBankName(bank.name ?: ""))
    }

    private fun setupEvent() {
        setupClickEvent()

        setupTextChangeEvent()
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //開戶名
            setupClearButtonVisibility(et_create_name) { checkCreateName(it) }

            //銀行卡號
            setupClearButtonVisibility(et_bank_card_number) { checkBankCardNumber(it) }

            //開戶網點
            setupClearButtonVisibility(et_network_point) { checkNetWorkPoint(it) }

            //提款密碼
            setupEyeButtonVisibility(et_withdrawal_password) { checkWithdrawPassword(it) }
        }
    }

    private fun setupClearButtonVisibility(setupView: LoginEditText, checkFun: (String) -> Unit) {
        setupView.let { view ->
            view.afterTextChanged {
                view.clearIsShow = it.isNotEmpty()
                checkFun.invoke(it)
            }
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

    private fun setupClickEvent() {
        item_bank_selector.setOnClickListener {
            mBankSelectorBottomSheetDialog.show()
        }

        btn_submit.setOnClickListener {
            viewModel.addBankCard(
                bankName = tv_bank_name.text.toString(),
                subAddress = et_network_point.getText(),
                cardNo = et_bank_card_number.getText(),
                fundPwd = et_withdrawal_password.getText(),
                fullName = et_create_name.getText(),
                id = args.editBankCard?.id?.toString(),
                userId = sUserInfo.userId.toString(),
                uwType = "bank", //TODO Dean : 目前只有銀行一種, 還沒有UI可以做選擇, 先暫時寫死.
                bankCode = args.editBankCard?.bankCode.toString()
            )
        }

        btn_reset.setOnClickListener {
            resetAll()
        }

        btn_delete_bank.setOnClickListener {
            viewModel.deleteBankCard(args.editBankCard?.id.toString())
        }
    }

    private fun setupObserve() {
        viewModel.rechargeConfigs.observe(this.viewLifecycleOwner, Observer { rechCfgData ->
            setupBankSelector(rechCfgData)
        })

        viewModel.bankAddResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                //TODO Dean : bind bank card success Event
                //綁定成功後回至銀行卡列表bank card list
                when (args.navigateFrom) {
                    PageFrom.WITHDRAW -> {
                        val action = BankCardFragmentDirections.actionBankCardFragmentToWithdrawFragment(args.navigateFrom)
                        mNavController.navigate(action)
                    }
                    else -> {
                        mNavController.popBackStack()
                    }
                }
            }
        })

        viewModel.bankDeleteResult.observe(this.viewLifecycleOwner, Observer { result ->
            if (result.success) {
                //TODO Dean : delete bank card success Event
                ToastUtil.showToast(context, getString(R.string.text_bank_card_delete_success))
                //刪除銀行卡成功後回至銀行卡列表bank card list
                mNavController.popBackStack()
            } else {
                showPromptDialog(getString(R.string.text_bank_card_delete_fail), result.msg) {}
            }
        })

        //錯誤訊息
        //開戶名
        viewModel.createNameErrorMsg.observe(this.viewLifecycleOwner, Observer {
            et_create_name.setError(it ?: "")
        })

        //銀行卡號
        viewModel.bankCardNumberMsg.observe(this.viewLifecycleOwner, Observer {
            et_bank_card_number.setError(it ?: "")
        })

        //開戶網點
        viewModel.networkPointMsg.observe(this.viewLifecycleOwner, Observer {
            et_network_point.setError(it ?: "")
        })

        //提款密碼
        viewModel.withdrawPasswordMsg.observe(this.viewLifecycleOwner, Observer {
            et_withdrawal_password.setError(it ?: "")
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearBankCardFragmentStatus()
    }

    private fun resetAll() {
        et_create_name.setText("")
        et_bank_card_number.setText("")
        et_network_point.setText("")
        et_withdrawal_password.setText("")
        this@BankCardFragment.activity?.currentFocus?.clearFocus()
    }

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}

class BankSelectorAdapter(private val context: Context, private val dataList: List<MoneyRechCfg.Bank>, private val listener: BankSelectorAdapterListener) : BaseAdapter() {
    //TODO Dean : dataList的型態待充值的recharcfg/map取得後更改為相同資料型態
    private var selectedPosition = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder: ListViewHolder
        // if remove "if (convertView == null)" will get a warning about reuse view.
        val data = dataList[position]
        if (convertView == null) {
            holder = ListViewHolder()
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.item_listview_bank_card, parent, false)
            view.tag = holder
            view.apply {
                holder.tvBank = tv_bank_card
                holder.ivBankIcon = iv_bank_icon
                holder.llSelectBankCard = ll_select_bank_card
                setupView(holder, data, position)
            }
            view.tag = holder
            return view
        } else {
            holder = convertView.tag as ListViewHolder
            setupView(holder, data, position)
        }
        return convertView
    }

    private fun setupView(holder: ListViewHolder, data: MoneyRechCfg.Bank, position: Int) {
        holder.apply {
            /*val viewHolder = ViewHolder()*/
            tvBank?.text = data.name
            ivBankIcon?.setImageResource(MoneyManager.getBankIconByBankName(data.name ?: ""))
            if (position == selectedPosition)
                this.llSelectBankCard?.setBackgroundColor(ContextCompat.getColor(context, R.color.blue2))
            else
                llSelectBankCard?.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            llSelectBankCard?.setOnClickListener {
                if (selectedPosition != position) {
                    //                data.isSelected = !data.isSelected
                    selectedPosition = position
                    notifyDataSetChanged()
                    listener.onSelect(data)
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
        if (dataList.size > 0) {
            listener.onSelect(dataList[0])
        }
        notifyDataSetChanged()
    }
}

class ListViewHolder {
    var tvBank: TextView? = null
    var ivBankIcon: ImageView? = null
    var llSelectBankCard: LinearLayout? = null
}

class BankSelectorAdapterListener(private val selectListener: (item: MoneyRechCfg.Bank) -> Unit) {
    fun onSelect(item: MoneyRechCfg.Bank) = selectListener(item)
}