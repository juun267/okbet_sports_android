package org.cxct.sportlottery.ui.sport.endcard.bet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.circleOf
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.common.loading.LoadingAdapter
import org.cxct.sportlottery.databinding.FragmentEndcardgameBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.util.loginedRun

class EndCardGameFragment: BaseSocketFragment<EndCardVM, FragmentEndcardgameBinding>() {

    private lateinit var loadingHolder: Gloading.Holder
    private val oddsAdapter = EndCardOddsAdapter(::onOddClick)

    private val oddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            if (context == null || oddsChangeEvent.odds.isNullOrEmpty()) {
                return@OddsChangeListener
            }

            if (oddsAdapter.itemCount == 0) {
                oddsAdapter.setNewInstance(oddsChangeEvent.odds[PlayCate.FS_LD_CS.value])
            }
            if (loadingHolder.isLoading) {
                loadingHolder.showLoadSuccess()
            }
        }
    }

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

        matchInfo = requireArguments().getParcelable("matchInfo")!!
        tvHomeName.text = matchInfo.homeName
        tvAwayName.text = matchInfo.awayName
        tvTime.text = TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DM_HM_FORMAT)
        ivHomeLogo.circleOf(matchInfo.homeIcon, R.drawable.ic_team_default_no_stroke)
        ivAwayLogo.circleOf(matchInfo.awayIcon, R.drawable.ic_team_default_no_stroke)

        subscribeChannelHall(GameType.BK.key, matchInfo.id)
    }

    override fun onResume() {
        super.onResume()
        ServiceBroadcastReceiver.addOddsChangeListener(this@EndCardGameFragment, oddsChangeListener)
    }

    private fun initAmountList() {
        binding.rcvBetAmount.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        val amountList = mutableListOf(100, 200, 300, 400, 500)
        val adapter = BetAmountAdapter(::onBetAmountChanged)
        adapter.setNewInstance(amountList)
        binding.rcvBetAmount.adapter = adapter
        onBetAmountChanged(amountList.first())
    }

    private fun initOddsList() {
        binding.rcvOddsList.layoutManager = GridLayoutManager(context(), 4)
        binding.rcvOddsList.addItemDecoration(OddsItemDecoration())
        binding.rcvOddsList.adapter = oddsAdapter
    }

    private fun onBetAmountChanged(amount: Int) = binding.run {
        val money = "$amount$showCurrencySign"
        tvQ1Amount.text = money
        tvQ2Amount.text = money
        tvQ3Amount.text = money
        tvQ4Amount.text = money
    }

    private fun onOddClick(odd: Odd): Boolean {
        val oddId = odd.id!!
        val isAdded = EndCardBetManager.containOdd(oddId)
        if (isAdded) {
            EndCardBetManager.removeBetOdd(oddId)
        } else {
            EndCardBetManager.addBetOdd(oddId)
        }
        return true
    }

}