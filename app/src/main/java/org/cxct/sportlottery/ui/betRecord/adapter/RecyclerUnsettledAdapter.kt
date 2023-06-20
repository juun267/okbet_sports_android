package org.cxct.sportlottery.ui.betRecord.adapter

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemBetListBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.betRecord.ParlayType
import org.cxct.sportlottery.ui.betRecord.detail.BetDetailsActivity
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.copyToClipboard
import org.cxct.sportlottery.view.onClick
import java.util.Locale

/**
 * isDetails 是否为详情页   需要隐藏进入详情页
 */
class RecyclerUnsettledAdapter(private val isDetails:Boolean=false) : BindingAdapter<Row, ItemBetListBinding>() {

    init {
        //打印
        addChildClickViewIds(R.id.tvOrderPrint)
    }

    @SuppressLint("SetTextI18n")
    override fun onBinding(position: Int, binding: ItemBetListBinding, item: Row) {

        binding.run {


            if(item.matchOdds.isNotEmpty()){
                //开赛时间
                val startTime=item.matchOdds[0].startTime!!
                //下注时间
                val addTime=item.addTime
                tvInPlay.visible()
                if(addTime-startTime>0){
                    tvInPlay.text=context.getString(R.string.home_tab_in_play2)
                    tvInPlay.setBackgroundResource(R.drawable.bg_bet_title_red)
                }else{
                    tvInPlay.text=context.getString(R.string.home_tab_early)
                    tvInPlay.setBackgroundResource(R.drawable.bg_bet_title_green)
                }
            }else{
                tvInPlay.gone()
            }

            //投注方式
            val parlayString=StringBuffer()
            when (item.parlayType) {
                //单注
                ParlayType.OUTRIGHT.key,ParlayType.SINGLE.key -> {
                    parlayString.append(context.getString(R.string.N125))
                    ivDetails.gone()
                    tvInPlay.visible()
                    linearDetails.onClick {
                    }
//                    parlayString.append("-")
//                    parlayString.append(GameType.getGameTypeString(context, item.gameType))
                }
                //串关
                else -> {
                    parlayString.append(context.getString(R.string.N124))
                    ivDetails.visible()
                    if(isDetails){
                        ivDetails.gone()
                    }else{
                        ivDetails.visible()
                    }
                    tvInPlay.gone()
                    linearDetails.onClick {
                        if(isDetails){
                            return@onClick
                        }
                        val intent = Intent(context, BetDetailsActivity::class.java)
                        intent.putExtra("data", item)
                        context.startActivity(intent)
                    }
//                    ParlayType.getParlayStringRes(item.parlayType)?.let { parlayTypeStringResId ->
//                        parlayString.append(context.getString(R.string.bet_record_parlay) )
//                        parlayString.append("(")
//                        parlayString.append(context.getString(parlayTypeStringResId))
//                        parlayString.append(") -")
//                        parlayString.append(GameType.getGameTypeString(context, item.gameType))
//                    }
                }
            }
            tvType.text=parlayString.toString()

            //投注金额
            tvBetTotal.text = " ₱ ${TextUtil.format(item.totalAmount)}"
            //可赢金额
            tvBetWin.text = " ₱ ${TextUtil.format(item.winnable)}"
            //订单号
            tvOrderNumber.text = item.orderNo
            //时间
            tvOrderTime.text = TimeUtil.timeFormat(
                item.addTime,
                TimeUtil.NEWS_TIME_FORMAT,
                locale = Locale.ENGLISH
            )
            //copy订单号
            linearOrderNumber.onClick {
                context.copyToClipboard(item.orderNo)
            }

            //0,1 未结算数据才显示打印
            if(item.status in 0..1){
                tvOrderPrint.visible()
            }else{
                tvOrderPrint.gone()
            }


            //投注项 item
            recyclerBetCard.layoutManager = LinearLayoutManager(context)
            val cardAdapter = RecyclerBetCardAdapter(item)
            recyclerBetCard.adapter = cardAdapter
            cardAdapter.setList(item.matchOdds)


        }
    }
}