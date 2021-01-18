package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_bank_card.*
import kotlinx.android.synthetic.main.fragment_withdraw.*
import kotlinx.android.synthetic.main.fragment_withdraw.view.*
import kotlinx.android.synthetic.main.item_listview_bank_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.T
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddRequest
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity.Companion.navigateKey
import org.cxct.sportlottery.util.MD5Util

class WithdrawFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private lateinit var bankCardBottomSheet: BottomSheetDialog
    private lateinit var bankCardAdapter: BankCardAdapter
    private var withdrawBankCardData: T? = null

    private val mNavController by lazy {
        findNavController()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_withdraw, container, false).apply {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.checkPermissions()
        initEvent()
        initObserve(view)


        btn_reset.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable(navigateKey, PageFrom.WITHDRAW)
            mNavController.navigate(R.id.bankListFragment, bundle)
        }
    }

    private fun initEvent() {
        ll_select_bank.setOnClickListener {
            bankCardBottomSheet.show()
        }

        btn_withdraw.setOnClickListener {
            setupWithdrawData()?.let { request -> viewModel.addWithdraw(request) }
        }
    }

    private fun initObserve(view: View) {
        viewModel.bankCardList.observe(this.viewLifecycleOwner, Observer {
            it.bankCardList?.let { list ->
                val iniData = it.bankCardList[0]
                withdrawBankCardData = iniData
                tv_select_bank_card.text = getBankCardTailNo(iniData)
                initSelectBankCardBottomSheet(view, list.toMutableList())
            }
        })
    }

    private fun initSelectBankCardBottomSheet(view: View, bankCardList: MutableList<T>) {
        val bankCardBottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_bank_card, null)
        bankCardBottomSheet = BottomSheetDialog(requireContext())
        bankCardBottomSheet.apply {
            setContentView(bankCardBottomSheetView)
            bankCardAdapter = BankCardAdapter(lv_bank_card.context, bankCardList, BankCardAdapterListener {
                view.tv_select_bank_card.text = getBankCardTailNo(it)
                dismiss()
            })
            lv_bank_card.adapter = bankCardAdapter
            bankCardBottomSheet.btn_close.setOnClickListener {
                this.dismiss()
            }
        }
    }

    private fun setupWithdrawData(): WithdrawAddRequest? {
        if (withdrawBankCardData == null)
            return null
        if (edit_withdraw_password.text.toString().length != 4)
            return null
        if (!judgmentWithdrawAmount())
            return null
        return WithdrawAddRequest(
            id = withdrawBankCardData!!.id.toLong(),
            applyMoney = edit_withdraw_amount.text.toString().toLong(),
            withdrawPwd = MD5Util.MD5Encode(edit_withdraw_password.text.toString())
        )
    }

    private fun judgmentWithdrawAmount(): Boolean {
        //TODO Dean : 判斷因額是否符合範圍
        return true
    }

    private fun getBankCardTailNo(data: T): String {
        return String.format(getString(R.string.selected_bank_card), data.bankName, data.cardNo)
    }
}

class BankCardAdapter(private val context: Context, private val dataList: MutableList<T>, private val listener: BankCardAdapterListener) : BaseAdapter() {

    private var selectedPosition = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_listview_bank_card, parent, false)
        val data = dataList[position]

        view.apply {
            tv_bank_card.text = String.format(context.getString(R.string.selected_bank_card), data.bankName, data.cardNo)
            if (position == selectedPosition)
                ll_select_bank_card.setBackgroundColor(ContextCompat.getColor(context, R.color.blue2))
            else
                ll_select_bank_card.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            ll_select_bank_card.setOnClickListener {
                if (selectedPosition != position) {
                    //                data.isSelected = !data.isSelected
                    selectedPosition = position
                    notifyDataSetChanged()
                    listener.onClick(data)
                }
            }
        }

        return view
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
}

class BankCardAdapterListener(val listener: (bankCard: T) -> Unit) {
    fun onClick(bankCard: T) = listener(bankCard)
}