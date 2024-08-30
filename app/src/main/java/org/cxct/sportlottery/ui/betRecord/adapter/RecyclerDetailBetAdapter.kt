package org.cxct.sportlottery.ui.betRecord.adapter

import android.annotation.SuppressLint
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemBetOrderDetailBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.bet.settledDetailList.ParlayComsDetailVO
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.betRecord.ParlayType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.copyToClipboard
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.setColors
import java.util.Locale

class RecyclerDetailBetAdapter(val row: Row) : BindingAdapter<ParlayComsDetailVO, ItemBetOrderDetailBinding>()  {

    private var block:()->Unit= { }
    //待成立注单   倒计时结束监听
    fun setOnCountTime(task:()->Unit){
        block=task
    }

    init {
        //打印点击
        addChildClickViewIds(R.id.tvOrderPrint)
    }


    @SuppressLint("SetTextI18n")
    override fun onBinding(position: Int, binding: ItemBetOrderDetailBinding, item: ParlayComsDetailVO) {

        binding.run {


            //投注金额
            tvBetTotal.text = " $showCurrencySign ${TextUtil.formatMoney(item.stake.toString(),2)}"


            //可赢金额
            when(row.status){
                //未结单  可赢：xxx
                0,1->{
                    tvBetWin.text = " ${showCurrencySign} ${TextUtil.formatMoney(item.winMoney.toString().replace("-",""),2)}"
                    tvBetWin.setColors(R.color.color_ff0000)
                    when(row.parlayType){
                        //单注 描述用 可赢：
                        ParlayType.OUTRIGHT.key, ParlayType.SINGLE.key->{
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
                    tvBetWin.text = " ${showCurrencySign} ${TextUtil.formatMoney(item.winMoney.toString(),2)}"
                    tvBetWin.setColors(R.color.color_ff0000)
                    tvWinLabel.text="${context.getString(R.string.win)}："
                }
                //未中奖  输：xxx
                4,5->{
                    //输的金额
                    tvBetWin.text = " ${showCurrencySign} ${TextUtil.formatMoney(item.winMoney.toString().replace("-",""),2)}"
                    tvBetWin.setColors(R.color.color_6D7693)
                    tvWinLabel.text="${context.getString(R.string.lose)}："
                }
                //其他  ₱ --
                else->{
                    tvBetWin.text = " ${showCurrencySign} --"
                    tvBetWin.setColors(R.color.color_6D7693)
                    tvWinLabel.text=""
                }
            }


            //是详情页
            recyclerBetCard.layoutManager = LinearLayoutManager(context)
            val cardAdapter = RecyclerDetailCardAdapter(row,block)
            recyclerBetCard.adapter = cardAdapter
            cardAdapter.setList(item.matchOddsVOList)




            //投注金额
            tvBetTotal2.text = " ${showCurrencySign} ${TextUtil.formatMoney(row.totalAmount,2)}"

            //可赢金额
            when(row.status){
                //未结单  可赢：xxx
                0,1,8->{
                    val tempRebate:Double=row.rebateAmount?:0.0
                    tvBetWin2.text = " ${showCurrencySign} ${TextUtil.formatMoney(row.winnable+tempRebate,2)}"
                    tvBetWin2.setColors(R.color.color_ff0000)
                    when(row.parlayType){
                        //单注 描述用 可赢：
                        ParlayType.OUTRIGHT.key,ParlayType.SINGLE.key->{
                            tvWinLabel2.text=context.getString(R.string.bet_info_list_win_quota)
                        }
                        //串关 描述用 最高可赢：
                        else->{
                            tvWinLabel2.text=context.getString(R.string.N110)
                        }
                    }
                }
                //已中奖   赢：xxx
                2,3->{
                    tvBetWin2.text = " ${showCurrencySign} ${TextUtil.formatMoney(row.win?:0,2)}"
                    tvBetWin2.setColors(R.color.color_ff0000)
                    tvWinLabel2.text="${context.getString(R.string.win)}："
                }
                //未中奖  输：xxx
                4,5->{
                    val tempRebate:Double=row.rebateAmount?:0.0
                    val totalMoney=(row.win?:0).toString().replace("-","").toDouble()+tempRebate
                    tvBetWin2.text = " ${showCurrencySign} ${TextUtil.formatMoney(totalMoney,2)}"
                    tvBetWin2.setColors(R.color.color_6D7693)
                    tvWinLabel2.text="${context.getString(R.string.lose)}："
                }
                //提前结算
                9->{
                    tvBetWin2.text = " $showCurrencySign ${TextUtil.formatMoney(row?:0,2)}"
                    if ((row.win?:0.0)>0.0){
                        tvBetWin2.setColors(R.color.color_ff0000)
                    }else{
                        tvBetWin2.setColors(R.color.color_6D7693)
                    }
                    tvWinLabel2.text=""
                }
                //其他  ₱ --
                else->{
                    tvBetWin2.text = " ${showCurrencySign} --"
                    tvBetWin2.setColors(R.color.color_6D7693)
                    tvWinLabel2.text=""
                }
            }


            //订单号
            tvOrderNumber.text = row.orderNo
            //时间
            tvOrderTime.text = TimeUtil.timeFormat(
                row.addTime,
                TimeUtil.NEWS_TIME_FORMAT,
                locale = Locale.ENGLISH
            )
            //copy订单号
            linearOrderNumber.onClick {
                context.copyToClipboard(row.orderNo)
            }

            //1 未结算数据才显示打印
            if(item.status ==1){
                tvOrderPrint.visible()
            }else{
                tvOrderPrint.gone()
            }


            if(position==data.size-1){
                linearBottom.visible()
            }else{
                linearBottom.gone()
            }

        }
    }
}