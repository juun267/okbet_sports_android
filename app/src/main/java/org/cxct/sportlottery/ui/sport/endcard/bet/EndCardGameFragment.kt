package org.cxct.sportlottery.ui.sport.endcard.bet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.circleOf
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.common.loading.LoadingAdapter
import org.cxct.sportlottery.databinding.FragmentEndcardgameBinding
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardBetDialog
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import timber.log.Timber

class EndCardGameFragment: BaseSocketFragment<EndCardVM, FragmentEndcardgameBinding>() {

    private lateinit var loadingHolder: Gloading.Holder
    private val oddsAdapter = EndCardOddsAdapter(::onOddClick)
    private val betAmountAdapter = BetAmountAdapter(::onBetAmountChanged)
    private var selectedEndCardBet: EndCardBet?=null

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
        initFloatMenu()
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
        reload()
    }
    
    private fun bindMatchInfo(matchInfo: MatchInfo) = binding.run {
        tvHomeName.text = matchInfo.homeName
        tvAwayName.text = matchInfo.awayName
        tvTime.text = "${getString(R.string.date)}: ${TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DMY_HM_FORMAT)}"
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
        selectedEndCardBet = endCardBet
        val sign = showCurrencySign
        tvQ1Amount.text = "$sign${endCardBet.lastDigit1}"
        tvQ2Amount.text = "$sign${endCardBet.lastDigit2}"
        tvQ3Amount.text = "$sign${endCardBet.lastDigit3}"
        tvQ4Amount.text = "$sign${endCardBet.lastDigit4}"
        oddsAdapter.setUpData(endCardBet)
        clearAllEndCardBet()
    }
    private fun initFloatMenu(){
        binding.parlayFloatWindow.onViewClick = {
            selectedEndCardBet?.let {
                EndCardBetDialog.newInstance(it).show(childFragmentManager)
                binding.parlayFloatWindow.gone()
            }
        }
    }
    private fun onOddClick(oddId: String): Boolean {
        val isAdded = EndCardBetManager.containOdd(oddId)
        if (isAdded) {
            EndCardBetManager.removeBetOdd(oddId)
        } else {
            EndCardBetManager.addBetOdd(oddId)
        }
        if (EndCardBetManager.getBetOdds().size==1&&!isAdded){
            binding.parlayFloatWindow.gone()
            selectedEndCardBet?.let {
                EndCardBetDialog.newInstance(it).show(childFragmentManager)
            }
        }else{
            showFloatBet()
        }
        return true
    }

    fun showFloatBet(){
        val size = EndCardBetManager.getBetOdds().size
        if (size==0){
            binding.parlayFloatWindow.gone()
        }else{
            binding.parlayFloatWindow.visible()
            binding.parlayFloatWindow.showRedCount(size.toString())
        }
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

    override fun onDestroy() {
        EndCardBetManager.removeAll()
        showFloatBet()
        super.onDestroy()
    }
    fun clearAllEndCardBet(){
        EndCardBetManager.removeAll()
        showFloatBet()
        oddsAdapter.notifyDataSetChanged()
    }
    fun reload(){
        loadingHolder.withRetry{ viewModel.getLGPCOFLDetail(matchInfo.id) }
        loadingHolder.go()
    }

}