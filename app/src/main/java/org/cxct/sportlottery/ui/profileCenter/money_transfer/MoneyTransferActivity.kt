package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import kotlinx.android.synthetic.main.activity_money_transfer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class MoneyTransferActivity : BaseToolBarActivity<MoneyTransferViewModel>(MoneyTransferViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel.getMoney()
        viewModel.getAllBalance()
//        viewModel.getThirdGames() //TODO Cheryl: 平台名稱不太會換，跟ios先寫死，後續有更動再調整

        initOnClick()
    }

    private fun initOnClick() {
        cb_change.setCheckedChange(cb_record) {
            layout_record.visibility = View.GONE
            layout_transfer.visibility = View.VISIBLE
        }
        cb_record.setCheckedChange(cb_change) {
            setToolBarName(getString(R.string.record_conversion))
            layout_record.visibility = View.VISIBLE
            layout_transfer.visibility = View.GONE
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