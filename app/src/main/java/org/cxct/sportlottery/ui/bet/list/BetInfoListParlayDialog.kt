package org.cxct.sportlottery.ui.bet.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_bet_info_item_action.*
import kotlinx.android.synthetic.main.dialog_bet_info_list.iv_close
import kotlinx.android.synthetic.main.dialog_bet_info_parlay_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoParlayListBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.ToastUtil

class BetInfoListParlayDialog : BaseDialog<MainViewModel>(MainViewModel::class), BetInfoListMatchOddAdapter.OnItemClickListener,
    BetInfoListParlayAdapter.OnItemClickListener {

    companion object {
        val TAG = BetInfoListParlayDialog::class.java.simpleName
    }

    private lateinit var matchOddAdapter: BetInfoListMatchOddAdapter
    private lateinit var parlayAdapter: BetInfoListParlayAdapter

    private var deletePosition: Int = -1

    init {
        setStyle(R.style.Common)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: DialogBetInfoParlayListBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_info_parlay_list, container, false)
        binding.apply {
            mainViewModel = this@BetInfoListParlayDialog.viewModel
            lifecycleOwner = this@BetInfoListParlayDialog.viewLifecycleOwner
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
        iv_close.setOnClickListener { dismiss() }
        tv_add_more.setOnClickListener { dismiss() }
        tv_bet.setOnClickListener {  }

        matchOddAdapter = BetInfoListMatchOddAdapter(this@BetInfoListParlayDialog)
        parlayAdapter = BetInfoListParlayAdapter(this@BetInfoListParlayDialog)

        rv_match_odd_list.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = matchOddAdapter
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_bet_info_list
                )
            )
        }

        rv_parlay_list.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = parlayAdapter
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
        viewModel.betInfoResult.observe(this.viewLifecycleOwner, Observer { result ->

            result?.success?.let {
                if(it){
                    result.betInfoData?.matchOdds?.isNotEmpty().let {
                        result.betInfoData?.matchOdds?.let { list ->
                            matchOddAdapter.modify(list, deletePosition)
                        }
                        result.betInfoData?.parlayOdds?.let { list ->
                            parlayAdapter.modify(list, deletePosition)
                        }
                    }
                }else{
                    //確認toast樣式後在調整
                    ToastUtil.showToast(context, result.msg)
                }
            }


        })
    }


    override fun onDeleteClick(position: Int) {
        viewModel.removeBetInfoItemAndRefresh(matchOddAdapter.matchOddList[position].oddsId)
    }


}