package org.cxct.sportlottery.ui.sport.endcard.bet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.circleOf
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.common.loading.LoadingAdapter
import org.cxct.sportlottery.databinding.FragmentEndcardgameBinding
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils

class EndCardGameFragment: BaseFragment<EndCardVM, FragmentEndcardgameBinding>() {

    private lateinit var loadingHolder: Gloading.Holder
    private lateinit var oddsAdapter: EndCardOddsAdapter


    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::loadingHolder.isInitialized) {
            loadingHolder = Gloading
                .from(LoadingAdapter(bgColor = context().getColor(R.color.color_0E131F)))
                .wrap(super.createRootView(inflater, container, savedInstanceState))
        }
        return loadingHolder.wrapper
    }

    override fun onInitView(view: View) {
        initMatchInfo()
        binding.ivBack.setOnClickListener { requireActivity().onBackPressed() }
        initAmountList()
        initOddsList()
    }

    private fun initMatchInfo() = binding.run {
        tvTime.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_1C283C)
        sections.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_202839)
        clMatchInfo.background = DrawableCreatorUtils.getCommonBackgroundStyle(
            leftTopCornerRadius = 16,
            rightTopCornerRadius = 16,
            solidColor = R.color.color_1A202E,
            strokeWidth = 0)

    }

    private lateinit var matchInfo: MatchInfo
    override fun onBindViewStatus(view: View) = binding.run {

        loadingHolder.showLoading()

        root.postDelayed({ loadingHolder.showLoadSuccess() }, 3000)

        matchInfo = requireArguments().getParcelable("matchInfo")!!
        tvHomeName.text = matchInfo.homeName
        tvAwayName.text = matchInfo.awayName
        tvTime.text = TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DM_HM_FORMAT)
        ivHomeLogo.circleOf(matchInfo.homeIcon, R.drawable.ic_team_default_no_stroke)
        ivAwayLogo.circleOf(matchInfo.awayIcon, R.drawable.ic_team_default_no_stroke)

        tvQ1Amount.text = "100$"
        tvQ2Amount.text = "100$"
        tvQ3Amount.text = "100$"
        tvQ4Amount.text = "100$"
    }

    private fun initAmountList() {
        binding.rcvBetAmount.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        val amountList = mutableListOf(100, 200, 300, 400, 500)
        val adapter = BetAmountAdapter()
        adapter.setNewInstance(amountList)
        binding.rcvBetAmount.adapter = adapter
    }

    private fun initOddsList() {
        binding.rcvOddsList.layoutManager = GridLayoutManager(context(), 4)
        binding.rcvOddsList.addItemDecoration(OddsItemDecoration())
        oddsAdapter = EndCardOddsAdapter()
        val dataList = mutableListOf<String>()
        repeat(10) { first->
            repeat(10) { second->
                dataList.add("$first-$second")
            }
        }
        oddsAdapter.setNewInstance(dataList)
        binding.rcvOddsList.adapter = oddsAdapter
    }




}