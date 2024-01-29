package org.cxct.sportlottery.ui.sport.detail.fragment

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentChartBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.chat.RecycleViewDivider
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.detail.adapter.RcvChartAdapter
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.fromJson
import timber.log.Timber

class SportChartFragment : BaseFragment<SportViewModel, FragmentChartBinding>() {

    private var matchInfo: MatchInfo? = null

    private lateinit var rcvAdapter: RcvChartAdapter

    override fun onInitView(view: View) {
        binding.clRoot.background = ResourcesCompat.getDrawable(
            resources, GameType.getGameTypeDetailBg(
                GameType.getGameType(matchInfo?.gameType) ?: GameType.FT
            ), null
        )
    }

    override fun onInitData() {
        super.onInitData()
        matchInfo = arguments?.get("matchInfo").toString().fromJson<MatchInfo>()
        initRecyclerView()
        updateChartView()
    }

    fun updateMatchInfo(matchInfo: MatchInfo) {
        this@SportChartFragment.matchInfo = matchInfo
        Timber.d("=== updateMatchInfo:更新图表数据")
        updateChartView()
    }

    private fun initRecyclerView() {
        rcvAdapter = RcvChartAdapter()
        //添加横线
        val divider = DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_table_chart)
            ?.let { divider.setDrawable(it) }
        binding.rcvChartView.addItemDecoration(divider)

        //添加竖线
        binding.rcvChartView.addItemDecoration(
            RecycleViewDivider(
                context,
                LinearLayoutManager.VERTICAL,
                MetricsUtil.convertDpToPixel(1f, context).toInt(),
                ContextCompat.getColor(requireContext(), R.color.color_002d68)
            )
        )

        //添加header
        val headerView = LayoutInflater.from(requireContext())
            .inflate(R.layout.header_sports_chart, binding.root, false)
        rcvAdapter.addHeaderView(headerView, -1, LinearLayout.HORIZONTAL)
        val homeTeam = headerView.findViewById<TextView>(R.id.tvHomeTeamName)
        val awayTeam = headerView.findViewById<TextView>(R.id.tvAwayTeamName)
        homeTeam.text = matchInfo?.homeName
        awayTeam.text = matchInfo?.awayName


        val rcvLayoutManager =
            GridLayoutManager(requireContext(), 3, RecyclerView.HORIZONTAL, false)
        rcvLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) {
                    rcvLayoutManager.spanCount
                } else 1
            }
        }
        binding.rcvChartView.layoutManager = rcvLayoutManager
        binding.rcvChartView.adapter = rcvAdapter
        rcvAdapter.setNewInstance(viewModel.chartViewList.value)
    }

    private var isObserved = false
    private fun updateChartView() {
        if (activity == null) return
        lifecycleScope.launch {
            Timber.d("开始拼装数据")
            matchInfo?.apply {
                when (gameType) {
                    GameType.FT.name, GameType.TN.name -> {
                        viewModel.assembleData1(gameType, this)
                    }

                    GameType.BK.name, GameType.AFT.name, GameType.IH.name -> {
                        viewModel.assembleData2(this)
                    }

                    GameType.VB.name, GameType.BM.name, GameType.TT.name, GameType.CB.name -> {
                        viewModel.assembleData3(this)
                    }
                }
            }
        }

        if (isObserved) {
            return
        }
        isObserved = true

        viewModel.chartViewList.observe(this) { cvList ->
            if (!::rcvAdapter.isInitialized) {
                return@observe
            }
            rcvAdapter.setCurrentGameType(matchInfo?.gameType)
            rcvAdapter.setCurrentSpt(matchInfo?.spt)
            rcvAdapter.setNewInstance(cvList)
        }
    }

    fun notifyRcv() {
        if (::rcvAdapter.isInitialized) {
            rcvAdapter.notifyDataSetChanged()
        }
    }
}