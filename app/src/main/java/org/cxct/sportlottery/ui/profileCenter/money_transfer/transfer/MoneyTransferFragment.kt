package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_money_transfer.*
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.money_transfer.GameData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.TextUtil

class MoneyTransferFragment : BaseFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val rvAdapter by lazy {
        MoneyTransferAdapter(ItemClickListener {
            it.let { data ->
                Log.e(">>>", "data name = ${data.name}")
                view?.findNavController()?.navigate(MoneyTransferFragmentDirections.actionMoneyTransferFragmentToMoneyTransferSubFragment(data.name))
                //TODO Cheryl: change to next page
//                val detailDialog = BetRecordDetailDialog(data)
//                detailDialog.show(parentFragmentManager, "BetRecordDetailDialog")
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_money_transfer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAllBalance()

        initView()
        initOnclick()
        initObserver()
    }

    private fun initOnclick() {
        btn_recycle.setOnClickListener {
            //TODO Cheryl
        }
    }

    private fun initView() {
        rv_plat.adapter = rvAdapter
    }

    private fun initObserver() {

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                layout_balance.tv_account_balance.text = TextUtil.format(it)
            }
        }

        viewModel.allBalanceResult.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            val resultList = mutableListOf<GameData>()
            for ((key, value) in it.resultMap ?: mapOf()) {
                value?.apply {
                    val gameData = GameData(money, remark, transRemaining)
                    resultList.add(gameData.apply { name = key })
                }
            }
            rvAdapter.addFooterAndSubmitList(resultList)
        }
    }
}