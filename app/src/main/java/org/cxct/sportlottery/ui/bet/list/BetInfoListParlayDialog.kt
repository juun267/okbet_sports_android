package org.cxct.sportlottery.ui.bet.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_bet_info_list.iv_close
import kotlinx.android.synthetic.main.dialog_bet_info_parlay_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoParlayListBinding
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration

class BetInfoListParlayDialog : BaseDialog<MainViewModel>(MainViewModel::class), BetInfoListMatchOddAdapter.OnItemClickListener,
    BetInfoListParlayAdapter.OnItemClickListener {

    companion object {
        val TAG = BetInfoListParlayDialog::class.java.simpleName
    }

    private lateinit var matchOddAdapter: BetInfoListMatchOddAdapter
    private lateinit var parlayAdapter: BetInfoListParlayAdapter

    init {
        setStyle(R.style.Common)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: DialogBetInfoParlayListBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_info_parlay_list, container, false)
        binding.apply {
            mainViewModel = this@BetInfoListParlayDialog.viewModel
            lifecycleOwner = this@BetInfoListParlayDialog
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        getData()
        observeData()
    }

    private fun initUI() {
        iv_close.setOnClickListener {
            dismiss()
        }

        matchOddAdapter = BetInfoListMatchOddAdapter(this@BetInfoListParlayDialog)
        parlayAdapter = BetInfoListParlayAdapter(this@BetInfoListParlayDialog)

        rv_match_odd_list.apply {
            adapter = matchOddAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_bet_info_list
                )
            )
        }

        rv_parlay_list.apply {
            adapter = parlayAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_bet_info_list
                )
            )
        }

    }

    private fun getData() {
        viewModel.getBetInfoListForParlay()
    }

    private fun observeData() {
        viewModel.betInfoList.observe(requireActivity(), Observer {
            if (it.size == 0) {
                dismiss()
            } else {
                matchOddAdapter.modify(it)
                parlayAdapter.modify(it)
            }
        })
    }

    override fun onDeleteClick(position: Int) {
        viewModel.removeBetInfoItemAndRefresh(matchOddAdapter.betInfoList[position].matchOdd.oddsId)
    }

}