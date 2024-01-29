package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.view.View
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentMoneyTransferBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.refreshMoneyLoading
import org.cxct.sportlottery.util.setTitleLetterSpacing

class MoneyTransferFragment : BaseFragment<MoneyTransferViewModel,FragmentMoneyTransferBinding>() {

    private val rvAdapter by lazy {
        MoneyTransferAdapter(ItemClickListener {
            it.let { data ->
                view?.findNavController()?.navigate(MoneyTransferFragmentDirections.actionMoneyTransferFragmentToMoneyTransferSubFragment(data))
            }
        })
    }

    override fun onInitView(view: View) {
        viewModel.setToolbarName(getString(R.string.account_transfer))
        viewModel.showTitleBar(true)
        initView()
        initOnclick()
        initObserver()
    }

    private fun initView()=binding.run {
        rvPlat.adapter = rvAdapter
        btnRecycle.setTitleLetterSpacing()
        layoutBalance.tvCurrencyType.text = sConfigData?.systemCurrencySign
    }

    private fun initOnclick()=binding.run {
        btnRecycle.setOnClickListener {
            viewModel.recycleAllMoney()
        }

        layoutBalance.btnRefresh.setOnClickListener {
            it.refreshMoneyLoading()
            viewModel.getMoneyAndTransferOut()
        }
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
                binding.layoutBalance.tvAccountBalance.text = TextUtil.format(it)
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

        viewModel.allBalanceResultList.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            rvAdapter.addFooterAndSubmitList(it)
            binding.btnRecycle.isEnabled = it.any { data -> data.money != 0.0 }

        }
    }
}