package org.cxct.sportlottery.ui.sport.endcard.bet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.common.loading.LoadingAdapter
import org.cxct.sportlottery.databinding.FragmentEndcardgameBinding
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardBetDialog
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardClearTipDialog
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardWinTipDialog
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeGradientOrientation
import org.cxct.sportlottery.util.setLeagueLogo

class EndCardGameFragment: BaseSocketFragment<EndCardVM, FragmentEndcardgameBinding>() {

    private lateinit var loadingHolder: Gloading.Holder
    private val oddsAdapter = EndCardOddsAdapter(::onOddClick)
    private val betAmountAdapter = BetAmountAdapter(this,::onBetAmountChanged)
    private var selectedEndCardBet: EndCardBet?=null
    private var endCardBetDialog: EndCardBetDialog?=null

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
        binding.ivBack.setOnClickListener {
            showClearTip{
                requireActivity().onBackPressed()
            }
        }
        initAmountList()
        initOddsList()
        initFloatMenu()

        binding.vShadow.background = ShapeDrawable()
            .setSolidColor(context().getColor(R.color.transparent_black_30), Color.TRANSPARENT)
            .setSolidGradientOrientation(ShapeGradientOrientation.BOTTOM_TO_TOP)
    }

    private lateinit var matchInfo: MatchInfo
    override fun onBindViewStatus(view: View) = binding.run {
        matchInfo = requireArguments().getParcelable("matchInfo")!!
        selectedEndCardBet = null
        bindMatchInfo(matchInfo)
        initObserver()
        reload()
    }
    
    private fun bindMatchInfo(matchInfo: MatchInfo) = binding.run {
        tvHomeName.text = matchInfo.homeName
        tvAwayName.text = matchInfo.awayName
        tvTime.text = "${getString(R.string.date)}: ${TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DMY_HM_FORMAT)}"
        ivHomeLogo.circleOf(matchInfo.homeIcon, R.drawable.ic_team_default_1)
        ivAwayLogo.circleOf(matchInfo.awayIcon, R.drawable.ic_team_default_1)
        ivLeague.setLeagueLogo(matchInfo.categoryIcon, R.drawable.ic_team_default_1)
        tvWinQuestion.setOnClickListener {
            selectedEndCardBet?.let { it1 -> EndCardWinTipDialog.newInstance(it1).show(childFragmentManager) }
        }
    }

    private fun initAmountList() {
        binding.rcvBetAmount.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        binding.rcvBetAmount.adapter = betAmountAdapter
        30.dp.toFloat().let {
            binding.rcvBetAmount.background = ShapeDrawable()
                .setSolidColor(context().getColor(R.color.color_0F212E))
                .setRadius(it, 0f, it, 0f)
        }
    }

    private fun initOddsList() {
        binding.rcvOddsList.layoutManager = GridLayoutManager(context(), 4)
        binding.rcvOddsList.addItemDecoration(OddsItemDecoration(context()))
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
                endCardBetDialog=EndCardBetDialog.newInstance(it)
                endCardBetDialog?.show(childFragmentManager)
                binding.parlayFloatWindow.gone()
            }
        }
    }
    private fun onOddClick(oddId: String): Boolean {
        if (matchInfo.startTime < System.currentTimeMillis()) {
            ToastUtil.showToast(context(), getString(R.string.P337))
            return false
        }

        val isAdded = EndCardBetManager.containOdd(oddId)
        if (isAdded) {
            EndCardBetManager.removeBetOdd(oddId)
        } else {
            EndCardBetManager.addBetOdd(oddId)
        }
        if (EndCardBetManager.getBetOdds().size==1&&!isAdded){
            binding.parlayFloatWindow.gone()
            selectedEndCardBet?.let {
                endCardBetDialog=EndCardBetDialog.newInstance(it)
                endCardBetDialog?.show(childFragmentManager)
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

            var selectItem: EndCardBet? = null
            if (selectedEndCardBet != null) {
                val lastId = selectedEndCardBet!!.id
                selectItem = it.find { it.id == lastId }
            }
            if (selectItem == null) {
                selectItem = it.first()
            }
            betAmountAdapter.setUpData(it.toMutableList(), selectItem)
            onBetAmountChanged(selectItem)
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
        endCardBetDialog?.dismiss()
    }
    fun removeEndCardBet(oddId: String){
        EndCardBetManager.removeBetOdd(oddId)
        showFloatBet()
        oddsAdapter.notifyDataSetChanged()
    }
    fun startBet(){
        binding.parlayFloatWindow.gone()
        selectedEndCardBet?.let {
            endCardBetDialog=EndCardBetDialog.newInstance(it)
            endCardBetDialog?.show(childFragmentManager)
        }
    }
    fun reload(){
        loadingHolder.withRetry{ viewModel.getLGPCOFLDetail(matchInfo.id) }
        loadingHolder.go()
    }
    fun showClearTip(onNext: ()->Unit){
        if (EndCardBetManager.getBetOdds().size>0 && EndCardClearTipDialog.isNeedShow()){
            EndCardClearTipDialog.newInstance().apply {
                this.onConfirm ={
                    onNext.invoke()
                }
            }.show(childFragmentManager)
        }else{
            onNext.invoke()
        }
    }
}