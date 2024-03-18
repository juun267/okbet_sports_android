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
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils

class EndCardGameFragment: BaseSocketFragment<EndCardVM, FragmentEndcardgameBinding>() {

    private lateinit var loadingHolder: Gloading.Holder
    private val oddsAdapter = EndCardOddsAdapter(::onOddClick)
    private val betAmountAdapter = BetAmountAdapter(::onBetAmountChanged)

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
        matchInfo = requireArguments().getParcelable("matchInfo")!!
        bindMatchInfo(matchInfo)
        initObserver()
        loadingHolder.withRetry{ viewModel.getLGPCOFLDetail(matchInfo.id) }
        loadingHolder.go()
    }
    
    private fun bindMatchInfo(matchInfo: MatchInfo) = binding.run {
        tvHomeName.text = matchInfo.homeName
        tvAwayName.text = matchInfo.awayName
        tvTime.text = TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DM_HM_FORMAT)
        ivHomeLogo.circleOf(matchInfo.homeIcon, R.drawable.ic_team_default_no_stroke)
        ivAwayLogo.circleOf(matchInfo.awayIcon, R.drawable.ic_team_default_no_stroke)
    }

    private fun initAmountList() {
        binding.rcvBetAmount.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        binding.rcvBetAmount.adapter = betAmountAdapter
    }

    private fun initOddsList() {
        binding.rcvOddsList.layoutManager = GridLayoutManager(context(), 4)
        binding.rcvOddsList.addItemDecoration(OddsItemDecoration())
        binding.rcvOddsList.adapter = oddsAdapter
    }

    private fun onBetAmountChanged(endCardBet: EndCardBet) = binding.run {
        val sign = showCurrencySign
        tvQ1Amount.text = "${endCardBet.lastDigit1}$sign"
        tvQ2Amount.text = "${endCardBet.lastDigit2}$sign"
        tvQ3Amount.text = "${endCardBet.lastDigit3}$sign"
        tvQ4Amount.text = "${endCardBet.lastDigit4}$sign"
        oddsAdapter.setUpData(endCardBet)
    }

    private fun onOddClick(oddId: String): Boolean {
        val isAdded = EndCardBetManager.containOdd(oddId)
        if (isAdded) {
            EndCardBetManager.removeBetOdd(oddId)
        } else {
            EndCardBetManager.addBetOdd(oddId)
        }
        return true
    }

    private fun initObserver() {
        viewModel.lgpcoflDetail.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                loadingHolder.showLoadFailed()
                return@observe
            }

            betAmountAdapter.setNewInstance(it.toMutableList())
            onBetAmountChanged(it.first())
            loadingHolder.showLoadSuccess()
        }
    }

}