package org.cxct.sportlottery.ui.game.betList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bet_receipt.*
import kotlinx.android.synthetic.main.view_match_receipt_total.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.Row
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TextUtil


/**
 * A simple [Fragment] subclass.
 * Use the [BetReceiptFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BetReceiptFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private var betResultData: List<BetResult>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bet_receipt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        initView()
    }

    private fun initObserver() {
        viewModel.userMoney.observe(viewLifecycleOwner, {
            it.let { money -> tv_balance.text = TextUtil.formatMoney(money ?: 0.0) }
        })
    }

    private fun initView() {
        setupTotalValue()

        initButton()
        initRecyclerView()
    }

    private fun setupTotalValue() {
        var betCount = 0
        var betTotalAmount = 0
        var winnableTotalAmount = 0.0
        betResultData?.forEach {
            betCount += (it.num ?: 0)
            betTotalAmount += (it.stake ?: 0)
            winnableTotalAmount += (it.winnable ?: 0.0)
        }
        tv_all_bet_count.text = betCount.toString()
        (context ?: requireContext()).apply {
            tv_total_bet_amount.text = "$betTotalAmount ${getString(R.string.currency)}"
            tv_total_winnable_amount.text = "$winnableTotalAmount ${getString(R.string.currency)}"
        }
    }

    private fun initButton() {
        btn_complete.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initRecyclerView() {
        rv_bet_receipt.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                ContextCompat.getDrawable(
                    context ?: requireContext(),
                    R.drawable.divider_color_white8
                )?.let {
                    setDrawable(it)
                }
            })

            adapter = BetReceiptDiffAdapter().apply {
                submitList(betResultData)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(betResultData: List<BetResult>?) = BetReceiptFragment().apply {
            this.betResultData = betResultData
        }
    }
}