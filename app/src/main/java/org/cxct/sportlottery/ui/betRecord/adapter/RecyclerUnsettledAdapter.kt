package org.cxct.sportlottery.ui.betRecord.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemBetListBinding
import org.cxct.sportlottery.net.sport.data.CheckCashOutResult
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.betRecord.ParlayType
import org.cxct.sportlottery.ui.betRecord.detail.BetDetailsActivity
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.copyToClipboard
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.setColors
import java.util.Locale

/**
 * isDetails 是否为详情页   需要隐藏进入详情页
 */
class RecyclerUnsettledAdapter(private val isDetails:Boolean=false) : BindingAdapter<Row, ItemBetListBinding>() {

    private var block:()->Unit= { }
    //待成立注单   倒计时结束监听
    fun setOnCountTime(task:()->Unit){
        block=task
    }
    init {
        //打印点击
        addChildClickViewIds(R.id.tvOrderPrint)
        addChildClickViewIds(R.id.cashoutBtn)
    }

    @SuppressLint("SetTextI18n")
    override fun onBinding(position: Int, binding: ItemBetListBinding, item: Row) {

        binding.run {

            if(item.matchOdds.isNotEmpty()){
                //开赛时间
                val startTime=item.matchOdds[0].startTime?:0
//                //下注时间
                val addTime=item.addTime
                tvInPlay.visible()
                //通过注单时间与比赛开始时间判断
                if(addTime-startTime>0){
                    //滚球
                    tvInPlay.text=context.getString(R.string.home_tab_in_play2)
                    tvInPlay.setBackgroundResource(R.drawable.bg_bet_title_red)
                }else{
                    //早盘
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
                    parlayString.append(context.getString(R.string.N948))
                    //隐藏详情跳转
                    linearBetDetail.gone()
//                    tvInPlay.visible()
                    //未结算的订单， 才显示 滚球，早盘
                    when(item.status){
                        0,1->{
                            tvInPlay.visible()
                        }
                        else->{
                            tvInPlay.gone()
                        }
                    }
                    linearDetails.onClick {
                    }
                }
                //串关
                else -> {
                    parlayString.append(context.getString(R.string.N949))
                    linearBetDetail.visible()
                    //如果已经是详情页，不可点击进入详情
                    if(isDetails){
                        linearBetDetail.gone()
                    }else{
                        linearBetDetail.visible()
                    }
                    tvInPlay.gone()
                    //跳转注单详情
                    linearDetails.onClick {
                        if(isDetails){
                            return@onClick
                        }
                        val intent = Intent(context, BetDetailsActivity::class.java)
                        intent.putExtra("data", item)
                        context.startActivity(intent)
                    }
                }
            }
            tvType.text=parlayString.toString()

            //投注金额
            tvBetTotal.text = " $showCurrencySign ${TextUtil.formatMoney(item.totalAmount,2)}"
            cashoutBtn.setCashOutStatus(item.cashoutStatus, "$showCurrencySign ${TextUtil.formatMoney(item.cashoutAmount?:0)}")
            //可赢金额
            when(item.status){
                //未结单  可赢：xxx
                0,1->{
                    tvBetWin.text = " $showCurrencySign ${TextUtil.formatMoney(item.winnable,2)}"
                    tvBetWin.setColors(R.color.color_ff0000)
                    when(item.parlayType){
                        //单注 描述用 可赢：
                        ParlayType.OUTRIGHT.key,ParlayType.SINGLE.key->{
                            tvWinLabel.text=context.getString(R.string.bet_info_list_win_quota)
                        }
                        //串关 描述用 最高可赢：
                        else->{
                            tvWinLabel.text=context.getString(R.string.N110)
                        }
                    }
                }
                //已中奖   赢：xxx
                2,3->{
                    tvBetWin.text = " $showCurrencySign ${TextUtil.formatMoney(item.win?:0,2)}"
                    tvBetWin.setColors(R.color.color_ff0000)
                    tvWinLabel.text="${context.getString(R.string.win)}："
                }
                //未中奖  输：xxx
                4,5->{
                    val tempRebate:Double=item.rebateAmount?:0.0
                    val totalMoney=(item.win?:0).toString().replace("-","").toDouble()+tempRebate
                    tvBetWin.text = " $showCurrencySign ${TextUtil.formatMoney(totalMoney,2)}"
                    tvBetWin.setColors(R.color.color_6D7693)
                    tvWinLabel.text="${context.getString(R.string.lose)}："
                }
                //其他  ₱ --
                else->{
                    tvBetWin.text = " $showCurrencySign --"
                    tvBetWin.setColors(R.color.color_6D7693)

                    tvWinLabel.text=""
                }
            }


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

            //1 未结算数据才显示打印
            if(item.status ==1){
                tvOrderPrint.visible()
            }else{
                tvOrderPrint.gone()
            }



            //注单列表  非详情页
            recyclerBetCard.layoutManager = LinearLayoutManager(context)
            val cardAdapter = RecyclerBetCardAdapter(item,block)
            recyclerBetCard.adapter = cardAdapter
            if(item.matchOdds.size>2){
                cardAdapter.setList(item.matchOdds.subList(0,2))
            }else{
                cardAdapter.setList(item.matchOdds)
            }

        }
    }

    /**
     * 更新选中状态
     */
    fun updateCashOut(list: List<CheckCashOutResult>){
        data.forEachIndexed { index, row ->
            list.forEach {
                if (row.orderNo == it.orderNo){
                    var needUpdate = false
                    if (row.cashoutStatus != it.cashoutStatus){
                        row.cashoutStatus = it.cashoutStatus
                        needUpdate = true
                    }
                    if (row.cashoutAmount != it.cashoutAmount){
                        row.cashoutStatus = it.cashoutStatus
                        needUpdate = true
                    }
                    if (needUpdate){
                        notifyItemChanged(index)
                    }
                }
            }
        }
    }

    /**
     * 将未选中的按钮恢复正常状态
     */
    fun selectedCashOut(uniqNo: String){
        data.forEachIndexed { index, row ->
            if (row.cashoutStatus in 1..2 && row.uniqNo!=uniqNo){
                notifyItemChanged(index)
            }
        }
    }
}