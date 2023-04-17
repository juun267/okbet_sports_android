package org.cxct.sportlottery.ui.betRecord

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.tools.ToastUtils
import kotlinx.android.synthetic.main.activity_register.view
import kotlinx.android.synthetic.main.content_parlay_match.view.*
import kotlinx.android.synthetic.main.edittext_login.layout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.maintab.betdetails.BetDetailsActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.view.onClick


class ContentParlayMatchAdapter(val data: Row) :
    ListAdapter<MatchOdd, RecyclerView.ViewHolder>(ContentDiffCallBack()) {
    var gameType: String = ""
    var betConfirmTime: Long? = 0
    var matchType: String? = null
    var mData = data
    fun setupMatchData(
        gameType: String, dataList: List<MatchOdd>, betConfirmTime: Long?, matchType: String?
    ) {
        this.gameType = gameType
        this.betConfirmTime = betConfirmTime
        this.matchType = matchType
        submitList(dataList)
    }

    class ContentDiffCallBack : DiffUtil.ItemCallback<MatchOdd>() {
        override fun areItemsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
            return oldItem.oddsId == newItem.oddsId
        }

        override fun areContentsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ParlayMatchViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = getItem(holder.adapterPosition)
        when (holder) {
            is ParlayMatchViewHolder -> {
                holder.bind(gameType, data, position, betConfirmTime, data.status, matchType, mData)
            }
        }
    }

    class ParlayMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_parlay_match, viewGroup, false)
//                view.findViewById<TextView>(R.id.content_play).setCompoundDrawablesRelative(null, null, null, null)
                return ParlayMatchViewHolder(view)
            }
        }

        fun bind(
            gameType: String,
            data: MatchOdd,
            position: Int,
            betConfirmTime: Long?,
            status: Int,
            matchType: String?,
            rowData: Row
        ) {
            itemView.apply {
                ///串关详情跳转
                itemView.onClick {
                    val intent = Intent(context, BetDetailsActivity::class.java)
                    intent.putExtra("data", rowData)
                    context?.startActivity(intent)
                }
                topLine.isVisible = position != 0
//                content_play.text = "$gameTypeName ${data.playCateName}"
                //篮球 滚球 全场让分【欧洲盘】
                content_play.setGameType_MatchType_PlayCateName_OddsType(
                    gameType, matchType, data.playCateName, data.oddsType
                )

//                tv_team_names.setTeamNames(15, data.homeName, data.awayName)
                title_team_name_parlay.setTeamsNameWithVS(data.homeName, data.awayName)
                tvPrint.setOnClickListener {
                    val dialog = Dialog(context)
                    dialog.setContentView(R.layout.dialog_print_enter_full_name)
                    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    val etInputName = dialog.findViewById<EditText>(R.id.etInputFullName)
                    val tvCancel = dialog.findViewById<TextView>(R.id.tvCancel)
                    val tvPrint = dialog.findViewById<TextView>(R.id.tvPrint)
                    val etBg = DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
                        .setStrokeColor(context.getColor(R.color.color_e3e8ee))
                        .setStrokeWidth(1.dp.toFloat())
                        .setSolidColor(context.getColor(R.color.color_FFFFFF)).build()

                    etInputName.setHintTextColor(context.getColor(R.color.color_A7B2C4))
                    etInputName.background = etBg
                    tvCancel.setOnClickListener {
                        dialog.dismiss()
                    }
                    tvPrint.setOnClickListener {
                        if (etInputName.text.isEmpty()) {
                            ToastUtil.showToast(context, "请先输入全名")
                        }
                    }

                    dialog.show()
                }
                parlay_play_content.setPlayContent(
                    data.playName, data.spread, TextUtil.formatForOdd(data.odds)
                )

                parlay_play_time.text = TimeUtil.timeFormat(data.startTime, TimeUtil.DM_HM_FORMAT)
                itemView.iv_country.setSvgDrawable(data.categoryIcon)
                content_league.text = data.leagueName
//                if (position == 0) {
//                    if(betConfirmTime?.toInt() != 0){
//                        val leftTime = betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
//                        object : CountDownTimer(leftTime ?: 0, 1000) {
//
//                            override fun onTick(millisUntilFinished: Long) {
//                                tv_count_down_parley.text =
//                                    "${TimeUtil.longToSecond(millisUntilFinished)} ${context.getString(R.string.sec)}"
//                            }
//
//                            override fun onFinish() {
//                                tv_count_down_parley.text =
//                                    "0 ${context.getString(R.string.sec)}"
//                                if (status == BetRecordType.UNSETTLEMENT.code.firstOrNull()) {
//                                    tv_count_down_parley.visibility = View.GONE
//                                }
//                            }
//                        }.start()
//                    }else{
//                        tv_count_down_parley.visibility = View.GONE
//                    }
//                } else {
//                    tv_count_down_parley.visibility = View.GONE
//                }
//                if (data.rtScore?.isNotEmpty() == true) tv_score_parlay.text = "(${data.rtScore})"
            }
        }
    }
}