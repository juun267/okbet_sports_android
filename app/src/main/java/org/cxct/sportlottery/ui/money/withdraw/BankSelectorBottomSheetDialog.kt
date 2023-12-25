package org.cxct.sportlottery.ui.money.withdraw

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBottomSheetBankCardBinding
import org.cxct.sportlottery.network.money.config.Bank
import org.cxct.sportlottery.network.money.config.BankType
import org.cxct.sportlottery.network.money.config.MoneyRechCfgData

class BankSelectorBottomSheetDialog(context: Context,val selectListener: (item: Bank) -> Unit): BottomSheetDialog(context) {
    private val adapter by lazy { BankSelectorAdapter{
        selectListener.invoke(it)
        dismiss()
    }}
    private var bankType: BankType?=null
    private var bankList = listOf<Bank>()

    private val binding by lazy { DialogBottomSheetBankCardBinding.inflate(layoutInflater) }
    init {
        setContentView(binding.root)
        initView()
    }

    private fun initView() =binding.run{
        lvBankItem.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        lvBankItem.adapter = adapter
        tvGameTypeTitle.text = context.getString(R.string.select_bank)
        btnClose.setOnClickListener {
            dismiss()
        }
    }
    fun setBanks(bankList: List<Bank>){
        this.bankList = bankList
    }
    fun setBankType(bankType: BankType){
        this.bankType = bankType
        updateList()
    }
    fun updateList(){
        adapter.setList(bankList.filter { it.bankType == bankType?.ordinal })
        adapter.initSelectStatus()
        binding.lvBankItem.scrollToPosition(0)
    }

   fun getSelectedItem():Bank?{
        return adapter.getSelectedItem()
   }
}