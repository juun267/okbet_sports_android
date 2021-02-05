package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.os.Bundle
import android.widget.CheckBox
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_money_transfer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity
import org.cxct.sportlottery.ui.profileCenter.money_transfer.record.MoneyTransferRecordFragmentDirections
import org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer.MoneyTransferFragmentDirections

class MoneyTransferActivity : BaseToolBarActivity<MoneyTransferViewModel>(MoneyTransferViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changePage()
    }

    private fun changePage() {
        cb_change.setCheckedChange(cb_record) {
            my_nav_host_fragment.findNavController().navigate(MoneyTransferRecordFragmentDirections.actionMoneyTransferRecordFragmentToMoneyTransferFragment())
        }
        cb_record.setCheckedChange(cb_change) {
            my_nav_host_fragment.findNavController().navigate(MoneyTransferFragmentDirections.actionMoneyTransferFragmentToMoneyTransferRecordFragment())
        }
    }

    override fun setContentView(): Int {
        return R.layout.activity_money_transfer
    }

    override fun setToolBarName(): String {
        return getString(R.string.account_transfer)
    }

    private fun CheckBox.setCheckedChange (unCheckedItem: CheckBox, checked: (Unit) -> Unit) {
        this.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                unCheckedItem.isChecked = false
                checked.invoke(Unit)
            }

        }
    }

}