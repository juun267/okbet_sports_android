package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.view.View
import androidx.navigation.findNavController
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivityMoneyTransferBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.profileCenter.money_transfer.record.MoneyTransferRecordFragmentDirections
import org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer.MoneyTransferFragmentDirections

class MoneyTransferActivity : BindingActivity<MoneyTransferViewModel,ActivityMoneyTransferBinding>() {

    private val navController by lazy { findNavController(R.id.my_nav_host_fragment) }

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        viewModel.getMoneyAndTransferOut()
        viewModel.getThirdGamesWithMoney()

        initToolbar()
        initObserver()
        initOnClick()
    }

    private fun initToolbar()=binding.run {
        toolBar.titleText = getString(R.string.account_transfer)
        toolBar.setOnBackPressListener {
            //因需求 轉換信息點擊左上角返回鍵要回到額度轉換 故採用同android 虛擬返回鍵
            onBackPressed()
        }

    }

    private fun initObserver() {
        viewModel.loading.observe(this) {
            if (it)
                loading()
            else
                hideLoading()
        }

        viewModel.isShowTitleBar.observe(this) {
            binding.llTitleBar.visibility = if (it == true) View.VISIBLE else View.GONE
        }

        viewModel.toolbarName.observe(this) {
            binding.toolBar.titleText = it
        }
    }

    private fun initOnClick()=binding.run {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        navController.navigate(MoneyTransferRecordFragmentDirections.actionMoneyTransferRecordFragmentToMoneyTransferFragment())
                    }
                    1 -> {
                        navController.navigate(MoneyTransferFragmentDirections.actionMoneyTransferFragmentToMoneyTransferRecordFragment())
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

    }
}