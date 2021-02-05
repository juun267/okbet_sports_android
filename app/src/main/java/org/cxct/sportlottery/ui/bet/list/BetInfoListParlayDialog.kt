package org.cxct.sportlottery.ui.bet.list

import android.content.res.ColorStateList
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
import kotlinx.android.synthetic.main.play_category_bet_btn.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoParlayListBinding
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.ui.base.BaseSocketDialog
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.TextUtil

class BetInfoListParlayDialog : BaseSocketDialog<MainViewModel>(MainViewModel::class), BetInfoListMatchOddAdapter.OnItemClickListener,
        BetInfoListParlayAdapter.OnTotalQuotaListener {


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

        rl_expandable.setOnClickListener {
            el_expandable_area.apply {
                if (this.isExpanded) {
                    iv_arrow_up.animate().rotation(180f).setDuration(200).start()
                    el_expandable_area.collapse(true)
                } else {
                    iv_arrow_up.animate().rotation(0f).setDuration(200).start()
                    el_expandable_area.setExpanded(true, true)
                }
            }
        }

        matchOddAdapter = BetInfoListMatchOddAdapter(requireContext(), this@BetInfoListParlayDialog)
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


    private fun observeData() {
        viewModel.betInfoResult.observe(this.viewLifecycleOwner, Observer { result ->
            result?.success?.let {
                if (!it) {
                    val dialog = CustomAlertDialog(requireActivity())
                    dialog.setTitle(getString(R.string.prompt))
                    dialog.setMessage(result.msg)
                    dialog.setNegativeButtonText(null)
                    dialog.setTextColor(R.color.red2)
                    dialog.show()
                }
            }
        })

        viewModel.newMatchOddList.observe(this.viewLifecycleOwner, Observer {
            matchOddAdapter.updatedBetInfoList = it
        })

        viewModel.matchOddList.observe(this.viewLifecycleOwner, Observer {
            matchOddAdapter.matchOddList = it
        })

        viewModel.parlayList.observe(this.viewLifecycleOwner, Observer {
            parlayAdapter.modify(it)
        })

        viewModel.betInfoRepository?.betInfoList?.observe(this.viewLifecycleOwner, Observer {
            if (it.size == 0) {
                dismiss()
            }
        })

        viewModel.betAddResult.observe(this.viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                val m: String
                val color: Int
                if (result.success) {
                    m = resources.getString(R.string.bet_info_add_bet_success)
                    color = R.color.gray6
                } else {
                    m = result.msg
                    color = R.color.red2
                }
                val dialog = CustomAlertDialog(requireActivity())
                dialog.setTitle(getString(R.string.prompt))
                dialog.setMessage(m)
                dialog.setNegativeButtonText(null)
                dialog.setTextColor(color)
                dialog.show()
            }
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val newList: MutableList<org.cxct.sportlottery.network.odds.list.Odd> =
                    mutableListOf()
            for ((key, value) in it.odds) {
                newList.addAll(value)
            }

            viewModel.updateBetInfoListByOddChange(newList)
        })

        receiver.matchOddsChange.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> =
                    mutableListOf()
            for ((key, value) in it.odds) {
                value.odds?.forEach { odd->
                    odd?.let { o ->
                        newList.add(o)
                    }
                }
            }
            viewModel.updateBetInfoList(newList)
        })

        receiver.globalStop.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val list = matchOddAdapter.matchOddList
            list.forEach { listData ->
                if (it.producerId == null || listData.producerId == it.producerId) {
                    listData.status = BetStatus.LOCKED.code
                }
            }
            matchOddAdapter.matchOddList = list
        })

        receiver.producerUp.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val list = matchOddAdapter.matchOddList
            list.forEach { listData ->
                if (it.producerId == null || listData.producerId == it.producerId) {
                    listData.status = BetStatus.ACTIVATED.code
                }
            }
            matchOddAdapter.matchOddList = list
        })
    }


    private fun getData() {
        viewModel.getBetInfoListForParlay(false)
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
                ), MatchType.PARLAY
        )
    }


    override fun onDeleteClick(position: Int) {
        viewModel.removeBetInfoItemAndRefresh(matchOddAdapter.matchOddList[position].oddsId)
    }


    override fun onOddChange() {
        if (tv_bet.isClickable) {
            tv_bet.apply {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red2))
                setTextColor(ContextCompat.getColor(tv_bet.context, R.color.white))
                text = getString(R.string.bet_info_list_odds_change)
            }
        }
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