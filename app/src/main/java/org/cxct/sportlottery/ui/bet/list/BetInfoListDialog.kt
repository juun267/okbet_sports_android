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
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.ToastUtil

class BetInfoListDialog : BaseDialog<MainViewModel>(MainViewModel::class), BetInfoListAdapter.OnItemClickListener {

    private lateinit var betInfoListAdapter: BetInfoListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: DialogBetInfoListBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_info_list, container, false)
        binding.apply {
            mainViewModel = this@BetInfoListDialog.viewModel
            lifecycleOwner = this@BetInfoListDialog
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeData()
        getData()
    }


    private fun initUI() {
        iv_close.setOnClickListener {
            dismiss()//後續調整hide或dismiss
        }

        betInfoListAdapter = BetInfoListAdapter(this@BetInfoListDialog)
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
        viewModel.betInfoResult.observe(requireActivity(), Observer {
            betInfoListAdapter.addAll(it.betInfoData!!)
        })
    }


    private fun getData() {
//        val list = listOf(Odd("123", 456.00))
//        viewModel.getBetInfoList(list)
    }


    override fun onDeleteClick(position: Int) {
        betInfoListAdapter.removeItem(position)
    }


    override fun onBetClick() {

    }


    override fun onAddMoreClick() {
        dismiss()
    }

}