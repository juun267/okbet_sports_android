package org.cxct.sportlottery.ui.sport.endcard.record

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.style.TextAppearanceSpan
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentEndcardRecordDetailBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.setColors

class EndCardRecordDetailFragment: BaseFragment<EndCardVM, FragmentEndcardRecordDetailBinding>() {

    private val rowAdapter by lazy { EndCardRecordRowAdapter() }
    private val oddAdapter by lazy { EndCardRecordOddAdapter() }

    override fun onInitView(view: View) {
        binding.linBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        initOddList()
        initResultList()
    }
    override fun onInitData() {
        super.onInitData()
        arguments?.getParcelable<Row>("data")?.let {
            setData(it)
        }
    }

    private fun initOddList(){
        binding.rvOdd.apply {
            layoutManager = GridLayoutManager(context,10)
            addItemDecoration(GridItemDecoration(4.dp,4.dp, Color.TRANSPARENT,false))
            adapter = oddAdapter
        }
    }
    private fun initResultList(){
        binding.rvResult.apply {
            layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
            addItemDecoration(SpaceItemDecoration(requireContext(), R.dimen.margin_0_5))
            adapter = rowAdapter
        }
    }
    private fun setData(row: Row)=binding.run{
        LogUtil.toJson(row)
        row.matchOdds.firstOrNull()?.let {
            tvLeagueName.text = it.leagueName
            tvHomeName.text = it.homeName
            tvAwayName.text = it.awayName
            tvBet.text = "$showCurrencySign ${TextUtil.formatMoney(it.cardMoney?:0,2)}"
            val winMoney= "$showCurrencySign ${TextUtil.formatMoney(it.maximumWinnable?:0,2)}"
            val notice = String.format(getString(R.string.P322),winMoney)
            tvTips.text = Spanny(notice).findAndSpan(winMoney) {
                TextAppearanceSpan(null, Typeface.NORMAL, 14.dp, ColorStateList.valueOf(requireContext().getColor(R.color.color_00E701)), null)
            }
        }
        tvBettingTime.text = TimeUtil.timeFormat(row.addTime, TimeUtil.DMY_HM_FORMAT)
        tvBetAmount.text = "$showCurrencySign ${TextUtil.formatMoney(row.stake,2)}"
        tvTotalWin.apply {
            text = if (row.status==0||row.status==1) "-" else "$showCurrencySign ${TextUtil.formatMoney(row.grossWin?:0.0,2)}"
            if((row.grossWin?:0.0) > 0){
                setColors(R.color.color_00FF81)
            }else{
                setColors(R.color.color_FFFFFF)
            }
        }
        tvOrderNumber.text = row.orderNo

        tvSettleTime.apply {
            text = "${getString(R.string.N664)}: ${TimeUtil.timeFormat(row.settleTime,TimeUtil.DMY_HM_FORMAT)}"
            isVisible = row.settleTime!=null
        }

        //可赢金额
        when(row.status){
            //未结单  可赢：xxx
            0,1->{
                ivStatus.setImageResource(R.color.transparent)
            }
            //已中奖   赢：xxx
            2,3->{
                ivStatus.setImageResource( R.drawable.ic_tag_win)
            }
            //未中奖  输：xxx
            4,5->{
                ivStatus.setImageResource( R.drawable.ic_tag_lost)
            }
            7->{
                ivStatus.setImageResource( R.drawable.ic_tag_cancel)
            }
            //其他  ₱ --
            else->{
                ivStatus.setImageResource(R.color.transparent)
            }
        }
        oddAdapter.setList(row.matchOdds?.firstOrNull()?.multiCode?.map { it.playName })
        row.matchOdds.firstOrNull()?.endingCardOFLWinnable?.apply{
            rowAdapter.setList(listOf(
                Item("Q1",lastDigit1Score,if(lastDigit1Result==true) lastDigit1Winnable else 0),
                Item("Q2",lastDigit2Score,if(lastDigit2Result==true) lastDigit2Winnable else 0),
                Item("Q3",lastDigit3Score,if(lastDigit3Result==true) lastDigit3Winnable else 0),
                Item("FT",lastDigit4Score,if(lastDigit4Result==true) lastDigit4Winnable else 0)
            ))
        }
    }
}