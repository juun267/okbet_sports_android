package org.cxct.sportlottery.ui.bet.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_bet_info_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoListBinding
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.ToastUtil

class BetInfoListDialog : BaseDialog<MainViewModel>(MainViewModel::class), BetInfoListAdapter.OnItemClickListener {


    companion object {
        val TAG = BetInfoListDialog::class.java.simpleName
    }


    private lateinit var binding: DialogBetInfoListBinding


    private lateinit var betInfoListAdapter: BetInfoListAdapter


    private var deletePosition: Int = -1


    init {
        setStyle(R.style.Common)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_info_list, container, false)
        binding.apply {
            mainViewModel = this@BetInfoListDialog.viewModel
            lifecycleOwner = this@BetInfoListDialog.viewLifecycleOwner
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeData()
    }


    private fun initUI() {
        iv_close.setOnClickListener {
            dismiss()
        }

        betInfoListAdapter = BetInfoListAdapter(requireContext(),this@BetInfoListDialog)

        rv_bet_list.apply {
            adapter = betInfoListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_bet_info_list
                )
            )
        }

    }


    private fun observeData() {
        viewModel.br?.betInfoList?.observe(this.viewLifecycleOwner, Observer {
            if (it.size == 0) {
                dismiss()
            } else {
                betInfoListAdapter.modify(it, deletePosition)
            }
        })

        viewModel.betAddResult.observe(this.viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                ToastUtil.showBetResultToast(requireActivity(), result.msg, result.success)
            }
        })
    }


    override fun onDeleteClick(position: Int) {
        //mock模式下 因為回傳內容都一樣 所以不會移除
        viewModel.removeBetInfoItem(betInfoListAdapter.betInfoList[position].matchOdd.oddsId)
        deletePosition = position
    }


    override fun onBetClick(betInfoListData: BetInfoListData, stake: Double) {
        viewModel.addBet(
            BetAddRequest(
                listOf(Odd(betInfoListData.matchOdd.oddsId, betInfoListData.matchOdd.odds)),
                listOf(Stake(betInfoListData.parlayOdds.parlayType, stake)),
                1,
                "EU"
            ), false
        )
    }


    override fun onAddMoreClick() {
        dismiss()
    }
}