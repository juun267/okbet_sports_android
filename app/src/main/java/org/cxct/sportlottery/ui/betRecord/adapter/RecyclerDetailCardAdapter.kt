package org.cxct.sportlottery.ui.betRecord.adapter

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemDetailCardBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOddsVO
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.ui.betRecord.BetRecordEndScoreAdapter
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setBetReceiptStatus2
import org.cxct.sportlottery.util.setGameType_MatchType_PlayCateName_OddsType
import org.cxct.sportlottery.view.dialog.BetEndScoreDialog
import org.cxct.sportlottery.view.onClick
import java.util.Locale

class RecyclerDetailCardAdapter(val row: Row, val block:()->Unit) :
    BindingAdapter<MatchOddsVO, ItemDetailCardBinding>()  {


    @SuppressLint("SetTextI18n")
    override fun onBinding(position: Int, binding: ItemDetailCardBinding, item: MatchOddsVO) {
        binding.run {
            //玩法 title
            tvBetType.text = GameType.getGameTypeString(context, row.gameType)
            //玩法
            tvMethodValue.setGameType_MatchType_PlayCateName_OddsType(
                row.gameType,
                row.matchType,
                item.playCateName,
                item.oddsType
            )
            //bet items 如果是篮球末尾比分
            if (row.matchOdds.firstOrNull()?.playCateCode == PlayCate.FS_LD_CS.value) {
                //篮球末尾比分
                val sortList = row.matchOdds.firstOrNull()?.multiCode?.sortedBy { it.playCode }
                    ?: listOf()
                val listData = if (sortList.size > 4) {
                    tvMore.visible()
                    sortList.subList(0, 4)
                } else {
                    tvMore.gone()
                    sortList
                }
                rvEndScoreInfo.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                rvEndScoreInfo.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_2))
                val scoreAdapter = BetRecordEndScoreAdapter()
                rvEndScoreInfo.adapter = scoreAdapter
                scoreAdapter.setList(listData)
                tvBetItemValue.inVisible()
                linearEndScore.visible()
                tvMore.onClick {
                    val dialog = BetEndScoreDialog(context)
                    dialog.showEndScoreDialog(sortList)
                }
            } else {
                //不是篮球末尾比分
                tvBetItemValue.visible()
                linearEndScore.inVisible()
                tvBetItemValue.text = "${item.playName} ${item.spread}"
            }

            //赔率
            tvOddsValue.text = "@${TextUtil.formatForOdd(item.odds)}"
            //主客队
            if(item.homeName.isNullOrEmpty()){
                tvTeamValue.text = "-"
            }else{
                tvTeamValue.text = "${item.homeName} vs ${item.awayName}"
            }
            //主客队弹框
            tvFullTeam.text = "${item.homeName} vs ${item.awayName}"
            tvTeamValue.onClick {
                if (frameTeam.visibility == View.VISIBLE) {
                    frameTeam.gone()
                } else {
                    frameTeam.visible()
                }
            }
            //联赛
            tvLeagueValue.text = item.leagueName
            //时间
            if(item.startTime==null){
                tvTimeValue.text="-"
            }else{
                tvTimeValue.text = TimeUtil.timeFormat(
                    item.startTime,
                    TimeUtil.NEWS_TIME_FORMAT,
                    locale = Locale.ENGLISH
                )
            }
            //icon
            ivGameIcon.load(GameType.getLeftGameTypeMenuIcon2(row.gameType))

            //状态
            when (row.status) {
                0 -> {
                    //处理中
                    tvStatus.setBackgroundResource(R.drawable.bg_bet_status_yellow)
                }

                7 -> {
                    //已取消
                    tvStatus.setBackgroundResource(R.drawable.bg_bet_status_cancel)
                }

                4, 5,6 -> {
                    //输 和
                    tvStatus.setBackgroundResource(R.drawable.bg_bet_status_gray)
                }

                else -> {
                    //投注成功
                    tvStatus.setBackgroundResource(R.drawable.bg_bet_status_green)
                }
            }



            if(row.betConfirmTime!=null&&System.currentTimeMillis() <row.betConfirmTime){
                //订单剩余时间
                val leftTime = row.betConfirmTime.minus(TimeUtil.getNowTimeStamp())
                //倒计时投注处理中
                object : CountDownTimer(leftTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        if(binding.tvStatus.isAttachedToWindow){
                            //倒计时 待成立 x 秒
                            tvStatus.text = context.getString(R.string.log_state_processing)
                        }
                    }

                    override fun onFinish() {
                        //执行监听
                        block()
//                        if(binding.tvStatus.isAttachedToWindow){
//                            tvStatus.setBetReceiptStatus2(row.status, row.cancelledBy)
//                        }
                    }
                }.start()
            }else{
                tvStatus.setBetReceiptStatus2(row.status, row.cancelledBy)
            }


            if(position==0){
                linearTitle.visible()
            }else{
                linearTitle.gone()
            }
        }

    }
}