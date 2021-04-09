package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_money_transfer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.profileCenter.money_transfer.record.MoneyTransferRecordFragmentDirections
import org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer.MoneyTransferFragmentDirections

class MoneyTransferActivity : BaseOddButtonActivity<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val navController = my_nav_host_fragment.findNavController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_transfer)

        viewModel.getMoney()
        viewModel.getAllBalance()
        viewModel.getThirdGames()

        initToolbar()
        initObserver()
        initOnClick()
    }

    private fun initToolbar() {
        btn_toolbar_back.setOnClickListener {
            //因需求 轉換信息點擊左上角返回鍵要回到額度轉換 故採用同android 虛擬返回鍵
            onBackPressed()
        }
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

        viewModel.toolbarName.observe(this, {
            tv_toolbar_title.text = it
        })
    }

    private fun initOnClick() {
        rb_change.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.moneyTransferRecordFragment -> {
                    my_nav_host_fragment.findNavController().navigate(MoneyTransferRecordFragmentDirections.actionMoneyTransferRecordFragmentToMoneyTransferFragment())
                }
            }
        }
        rb_record.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.moneyTransferFragment -> {
                    my_nav_host_fragment.findNavController().navigate(MoneyTransferFragmentDirections.actionMoneyTransferFragmentToMoneyTransferRecordFragment())
                }
            }
        }
    }
}