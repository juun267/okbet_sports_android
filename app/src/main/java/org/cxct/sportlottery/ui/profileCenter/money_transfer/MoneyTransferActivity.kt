package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.os.Bundle
import android.view.View
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


        viewModel.getMoney()
        viewModel.getAllBalance()
//        viewModel.getThirdGames() //TODO Cheryl: 平台名稱不太會換，跟ios先寫死，後續有更動再調整

        initObserver()
        initOnClick()
    }


    private fun initOnClick() {
        cb_change.setCheckedChange(cb_record) {
            setToolBarName(getString(R.string.account_transfer))
            my_nav_host_fragment.findNavController().navigate(MoneyTransferRecordFragmentDirections.actionMoneyTransferRecordFragmentToMoneyTransferFragment())
        }
        cb_record.setCheckedChange(cb_change) {
            viewModel.queryTransfers()
            setToolBarName(getString(R.string.record_conversion))
            my_nav_host_fragment.findNavController().navigate(MoneyTransferFragmentDirections.actionMoneyTransferFragmentToMoneyTransferRecordFragment())
        }
    }

    override fun setContentView(): Int {
        return R.layout.activity_money_transfer
    }

    override fun setToolBarName(): String {
        return getString(R.string.account_transfer)
    }

    private fun initObserver() {
        viewModel.loading.observe(this, {
            if (it)
                loading()
            else
                hideLoading()
        })

        viewModel.isShowTitleBar.observe(this, {
            ll_title_bar.visibility = if (it == true) View.VISIBLE else View.GONE
        })
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