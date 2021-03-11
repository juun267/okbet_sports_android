package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_money_transfer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.profileCenter.money_transfer.record.MoneyTransferRecordFragmentDirections
import org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer.MoneyTransferFragmentDirections

class MoneyTransferActivity : BaseOddButtonActivity<MoneyTransferViewModel>(MoneyTransferViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_transfer)

        viewModel.getMoney()
        viewModel.getAllBalance()
//        viewModel.getThirdGames() //TODO Cheryl: 平台名稱不太會換，跟ios先寫死，後續有更動再調整

        initToolbar()
        initObserver()
        initOnClick()
    }

    private fun initToolbar() {
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun initObserver() {
        viewModel.loading.observe(this, Observer {
            if (it)
                loading()
            else
                hideLoading()
        })

        viewModel.isShowTitleBar.observe(this, Observer {
            ll_title_bar.visibility = if (it == true) View.VISIBLE else View.GONE
        })

        viewModel.toolbarName.observe(this, Observer {
            tv_toolbar_title.text = it
        })
    }

    private fun CheckBox.setCheckedChange(unCheckedItem: CheckBox, checked: (Unit) -> Unit) {
        this.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                unCheckedItem.isChecked = false
                checked.invoke(Unit)
            }

        }
    }

    private fun initOnClick() {
        cb_change.setCheckedChange(cb_record) {
            my_nav_host_fragment.findNavController().navigate(MoneyTransferRecordFragmentDirections.actionMoneyTransferRecordFragmentToMoneyTransferFragment())
        }
        cb_record.setCheckedChange(cb_change) {
            my_nav_host_fragment.findNavController().navigate(MoneyTransferFragmentDirections.actionMoneyTransferFragmentToMoneyTransferRecordFragment())
        }
    }
}