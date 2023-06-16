package org.cxct.sportlottery.ui.sport.detail.fragment

import android.service.autofill.FieldClassification.Match
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.popup_window_list_bet_odds_change.rcv
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.FragmentChartBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.chat.RecycleViewDivider
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.fromJson
import timber.log.Timber

class SportChartFragment : BindingFragment<SportViewModel, FragmentChartBinding>() {

    var matchInfo: MatchInfo? = null


    val list = mutableListOf<String>()
    lateinit var rcvAdapter: RcvCharAdapter

    var home1st = "0"
    var away1st = "0"
    var home2nd = "0"
    var away2nd = "0"
    var home3rd = "0"
    var away3rd = "0"
    var home4th = "0"
    var away4th = "0"
    var home5th = "0"
    var away5th = "0"
    var home6th = "0"
    var away6th = "0"
    var home7th = "0"
    var away7th = "0"
    var home8th = "0"
    var away8th = "0"


    fun notifyRcv() {
        if (::rcvAdapter.isInitialized) {
            rcvAdapter.notifyDataSetChanged()
        }
    }

    override fun onInitView(view: View) {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.clRoot.background = ResourcesCompat.getDrawable(
            resources, GameType.getGameTypeDetailBg(
                GameType.getGameType(matchInfo?.gameType) ?: GameType.FT

            ), null
        )

        rcvAdapter = RcvCharAdapter()

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
        rcvAdapter.setNewInstance(list)
    }


    fun updateMatchInfo(matchInfo: MatchInfo) {
        this.matchInfo = matchInfo
        Timber.d("=== updateMatchInfo:更新图表数据")
        updateChartView()
    }


    override fun onInitData() {
        super.onInitData()
        matchInfo = arguments?.get("matchInfo").toString().fromJson<MatchInfo>()
        updateChartView()
    }

    private fun updateChartView() {
        matchInfo?.apply {
            when (gameType) {
                GameType.FT.name, GameType.TN.name -> {
                    assembleData1(gameType)
                }

                GameType.BK.name, GameType.AFT.name, GameType.IH.name -> {
                    assembleData2()
                }

                GameType.VB.name, GameType.BM.name, GameType.TT.name, GameType.CB.name -> {
                    assembleData3(gameType)
                }
            }
            if (::rcvAdapter.isInitialized) {
                rcvAdapter.setCurrentGameType(gameType)
                rcvAdapter.setCurrentSpt(spt)
                rcvAdapter.addData(list)
            }
        }
    }

    /**
     * 排球
     * 羽毛球
     * 乒乓球
     * 冰球
     * 台球
     */
    private fun assembleData3(gameType: String?) {
        matchInfo?.apply {
            matchStatusList?.let {
                if (it.isEmpty()) return@let
                home1st = it[0].homeScore.toString()
                away1st = it[0].awayScore.toString()
                if (it.size > 1) {
                    home2nd = it[1].homeScore.toString()
                    away2nd = it[1].awayScore.toString()
                }
                if (it.size > 2) {
                    home3rd = it[2].homeScore.toString()
                    away3rd = it[2].awayScore.toString()
                }
                if (it.size > 3) {
                    home4th = it[3].homeScore.toString()
                    away4th = it[3].awayScore.toString()
                }
                if (it.size > 4) {
                    home5th = it[4].homeScore.toString()
                    away5th = it[4].awayScore.toString()
                }
                if (it.size > 5) {
                    home7th = it[5].homeScore.toString()
                    away7th = it[5].awayScore.toString()
                }
                if (it.size > 6) {
                    home8th = it[6].homeScore.toString()
                    away8th = it[6].awayScore.toString()
                }
                home6th = homeTotalScore.toString()
                away6th = awayTotalScore.toString()

            }
        }
        list.apply {
            clear()
            add("1")
            add(home1st)
            add(away1st)
            add("2")
            add(home2nd)
            add(away2nd)
            add("3")
            add(home3rd)
            add(away3rd)
            matchInfo?.let {
                when (it.spt) {
                    3 -> {
                        add("局")
                        add(home6th)
                        add(away6th)
                    }

                    7 -> {
                        add("4")
                        add(home4th)
                        add(away4th)
                        add("5")
                        add(home5th)
                        add(away5th)
                        add("6")
                        add(home7th)
                        add(away7th)
                        add("7")
                        add(home8th)
                        add(away8th)
                        add("局")
                        add(home6th)
                        add(away6th)
                    }

                    else -> {
                        add("4")
                        add(home4th)
                        add(away4th)
                        add("5")
                        add(home5th)
                        add(away5th)
                        add("局")
                        add(home6th)
                        add(away6th)
                    }
                }
            }

        }
    }


    /**
     *篮球
     *美式足球
     *冰球
     */
    private fun assembleData2() {
        matchInfo?.apply {
            matchStatusList?.let {
                if (it.isEmpty()) return@let
                home1st = it[0].homeScore.toString()
                away1st = it[0].awayScore.toString()
                if (it.size > 1) {
                    home2nd = it[1].homeScore.toString()
                    away2nd = it[1].awayScore.toString()
                }
                home3rd = (home1st.toIntS() + home2nd.toIntS()).toString()
                away3rd = (away1st.toIntS() + away2nd.toIntS()).toString()

                if (it.size > 2) {
                    home4th = it[2].homeScore.toString()
                    away4th = it[2].awayScore.toString()
                    Timber.d("home4th:${home4th}")
                    Timber.d("away4th:${away4th}")
                }
                if (it.size > 3) {
                    home5th = it[3].homeScore.toString()
                    away5th = it[3].awayScore.toString()
                }
//                home6th = (home3rd.toIntS() + home4th.toIntS() + home5th.toIntS()).toString()
//                away6th = (away3rd.toIntS() + away4th.toIntS() + away5th.toIntS()).toString()
                home6th = homeScore.toString()
                home6th = awayScore.toString()
            }
        }
        list.apply {
            clear()
            add("第一节")
            add(home1st)
            add(away1st)
            add("第二节")
            add(home2nd)
            add(away2nd)
            add("半场比分")
            add(home3rd)
            add(away3rd)
            add("第三节")
            add(home4th)
            add(away4th)
            add("第四节")
            add(home5th)
            add(away5th)
            add("全场比分")
            add(home6th)
            add(away6th)
        }
    }

    /**
     * 足球
     * 网球数据
     */
    private fun assembleData1(gameType: String?) {
        matchInfo?.apply {
            if (gameType == GameType.FT.name) {
                home1st = homeCornerKicks.toString()
                away1st = awayCornerKicks.toString()
                home2nd = homeCards.toString()
                away2nd = awayCards.toString()
                home3rd = homeYellowCards.toString()
                away4th = awayYellowCards.toString()
                home4th = homeHalfScore ?: "0"
                home4th = awayHalfScore ?: "0"
                home5th = homeScore ?: "0"
                away5th = awayScore ?: "0"
            } else {
                try {
                    matchStatusList?.let {
                        if (it.isEmpty()) return@let
                        home1st = it[0].homeScore.toString()
                        away1st = it[0].awayScore.toString()
                        if (it.size > 1) {
                            home2nd = it[1].homeScore.toString()
                            away2nd = it[1].awayScore.toString()
                        }
                        if (it.size > 2) {
                            home3rd = it[2].homeScore.toString()
                            away3rd = it[2].awayScore.toString()
                        }
                        home4th = homeScore.toString()
                        away4th = awayScore.toString()
                        home5th = homeTotalScore.toString()
                        away5th = awayTotalScore.toString()
                    }
                } catch (e: Exception) {
                    home4th = homeScore.toString()
                    away4th = awayScore.toString()
                    home5th = homeTotalScore.toString()
                    away5th = awayTotalScore.toString()
                }
            }
        }
        list.apply {
            clear()
            if (gameType == GameType.FT.name) {
                add("Corners")
            } else {
                add("1")
            }
            add(home1st)
            add(away1st)
            if (gameType == GameType.FT.name) {
                add("Red\ncard")
            } else {
                add("2")
            }
            add(home2nd)
            add(away2nd)
            if (gameType == GameType.FT.name) {
                add("Yellow\ncard")
            } else {
                add("3")
            }
            add(home3rd)
            add(away3rd)
            if (gameType == GameType.FT.name) {
                add("1st Half\nscore")
            } else {
                add("赛盘")
            }
            add(home4th)
            add(away4th)
            if (gameType == GameType.FT.name) {
                add("Half time\nscore")
            } else {
                add("得分")
            }
            add(home5th)
            add(away5th)
        }
    }


    class RcvCharAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_text_view) {

        private var gameType: String? = null
        private var currentSpt: Int? = null
        fun setCurrentGameType(gameType: String?) {
            this.gameType = gameType
        }

        fun setCurrentSpt(currentSpt: Int?) {
            this.currentSpt = currentSpt
        }

        override fun convert(holder: BaseViewHolder, item: String) {

            val width = 258

            when (gameType) {
                GameType.FT.name -> {
                    holder.itemView.layoutParams.let { lp ->
                        lp.width = (width / 5).dp
                        holder.itemView.layoutParams = lp
                    }
                }

                //Tennis 网球
                GameType.TN.name -> {
                    holder.itemView.layoutParams.let { lp ->
                        val setLp: (Int) -> Unit = { it ->
                            lp.width = width / it
                        }
                        when (currentSpt) {
                            3, 5, 7 -> {
                                setLp((currentSpt ?: 3) + 2)
                            }

                            else -> {
                                setLp(5)
                            }
                        }
                        holder.itemView.layoutParams = lp
                    }
                }

                //VolleyBall VB排球
                //TableTennis TT乒乓球
                //Badminton BM羽毛球
                GameType.VB.name, GameType.TT.name, GameType.BM.name -> {
                    holder.itemView.layoutParams.let { lp ->
                        val setLp: (Int) -> Unit = { it ->
                            lp.width = width / it
                        }
                        when (currentSpt) {
                            3, 5, 7 -> {
                                setLp((currentSpt ?: 4) + 1)
                            }

                            else -> {
                                lp.width = (width / 5).dp
                            }
                        }
                        holder.itemView.layoutParams = lp
                    }
                }

                GameType.BK.name, GameType.AFT.name, GameType.IH.name, GameType.CB.name -> {
                    holder.itemView.layoutParams.let { lp ->
                        lp.width = (258 / 6).dp
                        holder.itemView.layoutParams = lp
                    }
                }

            }

            holder.setText(R.id.tvContent, item)
        }
    }


}