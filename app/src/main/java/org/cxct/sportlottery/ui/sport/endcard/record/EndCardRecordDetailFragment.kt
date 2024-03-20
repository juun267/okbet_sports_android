package org.cxct.sportlottery.ui.sport.endcard.record

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.style.TextAppearanceSpan
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentEndcardRecordDetailBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.setColors
import timber.log.Timber
import java.util.*

class EndCardRecordDetailFragment: BaseFragment<EndCardVM, FragmentEndcardRecordDetailBinding>() {

    private val row by lazy { arguments?.getParcelable<Row>("data")!! }
    private val rowAdapter by lazy { EndCardRecordRowAdapter() }
    private val oddAdapter by lazy { EndCardRecordOddAdapter() }
    override fun onInitView(view: View) {
        initView()
        initOddList()
        initResultList()

    }
    private fun initView()=binding.run{
        Timber.d("${row}")
        binding.linBack.setOnClickListener {
            (activity as EndCardActivity).removeFragment(this@EndCardRecordDetailFragment)
        }
        row.matchOdds.firstOrNull()?.let {
            tvLeagueName.text = it.leagueName
            tvHomeName.text = it.homeName
            tvAwayName.text = it.awayName
        }
        tvBettingTime.text = TimeUtil.timeFormat(row.addTime, TimeUtil.DMY_HM_FORMAT)
        tvBet.text = "$showCurrencySign ${TextUtil.formatMoney(row.stake,2)}"
        tvBetAmount.text = "$showCurrencySign ${TextUtil.formatMoney(row.stake,2)}"
//        tvTotalWin.text = "$showCurrencySign ${TextUtil.formatMoney(row.winnable,2)}"
        tvOrderNumber.text = row.orderNo
        val winMoney= "$showCurrencySign ${TextUtil.formatMoney(row.winnable,2)}"
        val notice = String.format(getString(R.string.P322),winMoney)
        tvTips.text = Spanny(notice).findAndSpan(winMoney) {
            TextAppearanceSpan(null, Typeface.NORMAL, 14.dp, ColorStateList.valueOf(requireContext().getColor(R.color.color_6AA4FF)), null)
        }
        if (row.settleTime==null){
            tvSettleTime.text = ""
        }else{
            tvSettleTime.text = "${getString(R.string.N664)} ${TimeUtil.timeFormat(row.settleTime,TimeUtil.DMY_HM_FORMAT)}"
        }
        //可赢金额
        when(row.status){
            //未结单  可赢：xxx
            0,1->{
                ivStatus.setImageResource(R.color.transparent)
                tvTotalWin.setColors(R.color.color_FFFFFF)
                tvTotalWin.text = " $showCurrencySign ${TextUtil.formatMoney(row.winnable,2)}"
                tvWinner.text=getString(R.string.bet_info_list_win_quota)
            }
            //已中奖   赢：xxx
            2,3->{
                ivStatus.setImageResource( R.drawable.ic_tag_win)
                tvTotalWin.setColors(R.color.color_00FF81)
                tvTotalWin.text = " $showCurrencySign ${TextUtil.formatMoney(row.win?:0,2)}"
                tvWinner.text="${getString(R.string.win)}："
            }
            //未中奖  输：xxx
            4,5->{
                val tempRebate:Double=row.rebateAmount?:0.0
                val totalMoney=(row.win?:0).toString().replace("-","").toDouble()+tempRebate
                ivStatus.setImageResource( R.drawable.ic_tag_lost)
                tvTotalWin.setColors(R.color.color_ff0000)
                tvTotalWin.text = " $showCurrencySign ${TextUtil.formatMoney(totalMoney,2)}"
                tvWinner.text="${getString(R.string.lose)}："
            }
            //其他  ₱ --
            else->{
                ivStatus.setImageResource(R.color.transparent)
                tvTotalWin.setColors(R.color.color_FFFFFF)
                tvTotalWin.text = " $showCurrencySign --"
                tvWinner.text=""
            }
        }
    }
    private fun initOddList(){
        binding.rvOdd.apply {
            layoutManager = GridLayoutManager(context,10)
            addItemDecoration(GridSpacingItemDecoration(10,4.dp,false))
            adapter = oddAdapter
            oddAdapter.setList(row.matchOdds?.firstOrNull()?.multiCode?.map { it.playName })
        }
    }
    private fun initResultList(){
        binding.rvResult.apply {
            layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
            addItemDecoration(SpaceItemDecoration(requireContext(), R.dimen.margin_0_5))
            adapter = rowAdapter
            rowAdapter.setList(listOf("","","","","",))
        }
    }
}