package org.cxct.sportlottery.ui.bet.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_bet_info_item_action.*
import kotlinx.android.synthetic.main.dialog_bet_info_list.iv_close
import kotlinx.android.synthetic.main.dialog_bet_info_parlay_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoParlayListBinding
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil

class BetInfoListParlayDialog : BaseDialog<MainViewModel>(MainViewModel::class), BetInfoListMatchOddAdapter.OnItemClickListener,
    BetInfoListParlayAdapter.OnTotalQuotaListener {


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
        tv_bet.setOnClickListener {
            addBet()
        }

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

        tv_bet.apply {
            isClickable = false
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_radius_5_button_unselected)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.bright_gray))
        }
    }


    private fun getData() {
        viewModel.getBetInfoListForParlay()
    }


    private fun observeData() {
        viewModel.betInfoResult.observe(this.viewLifecycleOwner, Observer { result ->

            result?.success?.let {
                if (it) {
                    result.betInfoData?.matchOdds?.isNotEmpty().let {
                        result.betInfoData?.matchOdds?.let { list ->
                            matchOddAdapter.modify(list, deletePosition)
                        }
                        result.betInfoData?.parlayOdds?.let { list ->
                            parlayAdapter.modify(list)
                        }
                    }
                } else {
                    //確認toast樣式後在調整
                    ToastUtil.showToast(context, result.msg)
                }
            }
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, Observer {
            if (it.size == 0) {
                dismiss()
            }
        })

        viewModel.betAddResult.observe(this.viewLifecycleOwner, Observer {
            if (!it.success) {
                //確認toast樣式後在調整
                ToastUtil.showToast(context, it.msg)
            }
        })

    }


    private fun addBet() {
        val matchList: MutableList<Odd> = mutableListOf()
        for (i in matchOddAdapter.matchOddList.indices) {
            matchList.add(Odd(matchOddAdapter.matchOddList[i].oddsId, matchOddAdapter.matchOddList[i].odds))
        }
        val parlayList: MutableList<Stake> = mutableListOf()
        for (i in parlayAdapter.parlayOddList.indices) {
            parlayList.add(Stake(parlayAdapter.parlayOddList[i].parlayType, parlayAdapter.betQuotaList[i]))
        }

        viewModel.addBet(
            BetAddRequest(
                matchList,
                parlayList,
                1,
                "EU"
            ), true
            , null
        )
    }


    override fun onDeleteClick(position: Int) {
        viewModel.removeBetInfoItemAndRefresh(matchOddAdapter.matchOddList[position].oddsId)
    }


    override fun count(totalWin: Double, totalBet: Double) {
        tv_win_quota.text = TextUtil.format(totalWin)
        tv_bet_quota.text = TextUtil.format(totalBet)
    }


    override fun status(statusList: MutableList<Boolean>) {
        tv_bet.apply {
            isClickable = if (statusList.contains(true)) {
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_radius_5_button_unselected)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.bright_gray))
                false
            } else {
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_radius_5_button_pumkinorange)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                true
            }
        }
    }


}