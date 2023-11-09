package org.cxct.sportlottery.ui.maintab.menu

import android.content.Intent
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.FragmentMainRightBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.adapter.GameBalanceAdapter
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.view.dialog.ToGcashDialog

class MainRightFragment : BindingFragment<MoneyTransferViewModel, FragmentMainRightBinding>() {

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
        btnDeposit.setOnClickListener {
            EventBusUtil.post(MenuEvent(false,Gravity.RIGHT))
            ToGcashDialog.showByClick{ viewModel.checkRechargeKYCVerify() }
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
        viewModel.isRechargeShowVerifyDialog.observe(this) {
            val b = it.getContentIfNotHandled() ?: return@observe
            if (b) {
                VerifyIdentityDialog().show(childFragmentManager, null)
            } else {
                loading()
                viewModel.checkRechargeSystem()
            }
        }
        viewModel.rechargeSystemOperation.observe(this) {
            hideLoading()
            val b = it.getContentIfNotHandled() ?: return@observe
            if (b) {
                requireContext().startActivity(Intent(requireContext(), MoneyRechargeActivity::class.java))
                return@observe
            }
            showPromptDialog(
                requireContext().getString(R.string.prompt),
                requireContext().getString(R.string.message_recharge_maintain)
            ) {}

        }
    }
    fun reloadData() {
        if (isAdded) {
            viewModel.getMoneyAndTransferOut()
            viewModel.getThirdGamesWithMoney()
        }
    }
}