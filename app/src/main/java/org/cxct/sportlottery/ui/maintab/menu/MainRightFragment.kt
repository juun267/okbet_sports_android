package org.cxct.sportlottery.ui.maintab.menu

import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.FragmentMainRightBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.adapter.GameBalanceAdapter
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.TextUtil

class MainRightFragment : BaseFragment<MoneyTransferViewModel, FragmentMainRightBinding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private val adapter =GameBalanceAdapter()

    override fun onInitView(view: View) {
        initView()
    }

    override fun onBindViewStatus(view: View) {
       initObserver()
        reloadData()
    }

    private fun initView() = binding.run {
        ivClose.setOnClickListener {
            EventBusUtil.post(MenuEvent(false,Gravity.RIGHT))
        }
        setOnClickListeners(tvTransfer,btnTransfer){
            viewModel.recycleAllMoney()
        }
        rvGame.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        rvGame.adapter = adapter
        tvNotice.text = "• *${getString(R.string.P206)}"
    }
    private fun initObserver() {
        viewModel.loading.observe(viewLifecycleOwner) {
            if (it)
                loading()
            else
                hideLoading()
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                binding.tvBanlance.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(it)}"
            }
        }

        viewModel.recycleAllMoneyResult.observe(viewLifecycleOwner) { result ->
            result?.getContentIfNotHandled()?.let {
                val msg = if (it.success) getString(R.string.money_recycle_succeed) else it.msg
                showPromptDialog(
                    title = getString(R.string.prompt),
                    message = msg,
                    success = it.success
                ){}
            }
        }
        viewModel.allBalanceResultList.observe(this) {
             adapter.setNewInstance(it.toMutableList())
        }
    }
    fun reloadData() {
        if (isAdded) {
            viewModel.getMoneyAndTransferOut()
            viewModel.getThirdGamesWithMoney()
        }
    }
}