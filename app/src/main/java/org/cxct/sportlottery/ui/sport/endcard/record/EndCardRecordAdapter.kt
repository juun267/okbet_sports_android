package org.cxct.sportlottery.ui.sport.endcard.record

import androidx.recyclerview.widget.GridLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemEndcardRecordBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setLeagueLogo
import org.cxct.sportlottery.util.setTeamLogo
import org.cxct.sportlottery.view.setColors
import java.util.*
import kotlin.math.absoluteValue

class EndCardRecordAdapter:BindingAdapter<Row, ItemEndcardRecordBinding>() {

    override fun onBinding(position: Int, binding: ItemEndcardRecordBinding, item: Row): Unit=binding.run {
        item.matchOdds.firstOrNull()?.let {
            ivLeagueLogo.setLeagueLogo(it.categoryIcon)
            tvLeagueName.text = it.leagueName
            ivHomeLogo.setTeamLogo(it.homeIcon)
            ivAwayLogo.setTeamLogo(it.awayIcon)
            tvHomeName.text = it.homeName
            tvAwayName.text = it.awayName
        }
        tvTime.text = TimeUtil.timeFormat(item.addTime, TimeUtil.DMY_HM_FORMAT)
        tvBetAmount.text = "$showCurrencySign ${TextUtil.formatMoney(item.stake,2)}"
        val winMoney = if (item.status==2||item.status==3) item.win?.absoluteValue?:0 else 0
        tvWinnableAmount.text = "$showCurrencySign ${TextUtil.formatMoney(winMoney,2)}"
        //可赢金额
        when(item.status){
            //未结单  可赢：xxx
            0,1->{
                ivStatus.setImageResource(R.color.transparent)
                tvWinner.setTextColor(context.getColor(R.color.color_8B96AD))
                tvWinnableAmount.setTextColor(context.getColor(R.color.color_FFFFFF))
            }
            //已中奖   赢：xxx
            2,3->{
                ivStatus.setImageResource( R.drawable.ic_tag_win)
                tvWinner.setTextColor(context.getColor(R.color.color_00FF81))
                tvWinnableAmount.setTextColor(context.getColor(R.color.color_00FF81))
            }
            //未中奖  输：xxx
            4,5->{
                ivStatus.setImageResource( R.drawable.ic_tag_lost)
                tvWinner.setTextColor(context.getColor(R.color.color_8B96AD))
                tvWinnableAmount.setTextColor(context.getColor(R.color.color_FFFFFF))
            }
            //其他  ₱ --
            else->{
                ivStatus.setImageResource(R.color.transparent)
                tvWinner.setTextColor(context.getColor(R.color.color_8B96AD))
                tvWinnableAmount.setTextColor(context.getColor(R.color.color_FFFFFF))
            }
        }
      rvOdd.apply {
          if (adapter==null){
              layoutManager = GridLayoutManager(context,10)
              addItemDecoration(GridSpacingItemDecoration(10,4.dp,false))
              adapter = EndCardRecordOddAdapter()
          }
          (adapter as EndCardRecordOddAdapter).setList(item.matchOdds.firstOrNull()?.multiCode?.map { it.playName })
      }
    }
}