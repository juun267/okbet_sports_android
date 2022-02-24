package org.cxct.sportlottery.ui.main.accountHistory.next


import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.dialog_combo_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOddsVO
import org.cxct.sportlottery.network.bet.settledDetailList.ParlayComsDetailVO
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.setGameStatusColor
import kotlin.coroutines.EmptyCoroutineContext.plus

class ComboDetailDialog internal constructor(
    private val mContext: Context,
    private val parlayComsDetailVOs: List<ParlayComsDetailVO>
) {

    private lateinit var mDialog: Dialog
    lateinit var adapter: CommonAdapter<ParlayComsDetailVO>
    lateinit var detailAdapter: CommonAdapter<MatchOddsVO>


    fun show(): ComboDetailDialog {
        mDialog = Dialog(mContext, R.style.CustomDialogStyle)
        mDialog.setContentView(R.layout.dialog_combo_detail)
        mDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        mDialog.setCanceledOnTouchOutside(true)
        mDialog.show()

        mDialog.btn_close.setOnClickListener {
            mDialog.dismiss()
        }
        mDialog.rvComboDetail.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        mDialog.rvComboDetail.isNestedScrollingEnabled = false
        adapter = object : CommonAdapter<ParlayComsDetailVO>(
            mContext,
            R.layout.item_parlay_combo, parlayComsDetailVOs
        ) {
            override fun convert(
                holder: ViewHolder,
                t: ParlayComsDetailVO,
                position: Int
            ) {
                holder.setText(
                    R.id.tvItem,
                    mContext.getString(R.string.text_combo_item) + " " + (position + 1)
                )
                val tvResult = holder.getView<TextView>(R.id.tvResult)

                when (t.status) {
                    0 -> {
                        tvResult.setTextColor(ContextCompat.getColor(mContext, R.color.colorRed))
                        tvResult.text =
                            "${mContext.getString(R.string.lose)} ${mContext.getString(R.string.minus)}${ArithUtil.toMoneyFormat(t.winMoney)}"
                    }
                    1 -> {
                        tvResult.setTextColor(
                            ContextCompat.getColor(
                                mContext,
                                R.color.colorGreenSea
                            )
                        )
                        tvResult.text =
                            "${mContext.getString(R.string.win)} ${mContext.getString(R.string.plus)}${ArithUtil.toMoneyFormat(t.winMoney)}"
                    }
                    else -> {
                        tvResult.setTextColor(
                            ContextCompat.getColor(
                                mContext,
                                R.color.colorGreenSea
                            )
                        )
                        tvResult.text = mContext.getString(R.string.draw)
                    }
                }
                val rvComboDetailItem = holder.getView<RecyclerView>(R.id.rvComboDetailItem)
                rvComboDetailItem.layoutManager =
                    LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
                rvComboDetailItem.isNestedScrollingEnabled = false
                detailAdapter = object : CommonAdapter<MatchOddsVO>(
                    mContext,
                    R.layout.item_parlay_combo_detail, t.matchOddsVOList
                ) {
                    override fun convert(
                        holder: ViewHolder,
                        matchOdds: MatchOddsVO,
                        position: Int
                    ) {
                        holder.setText(R.id.tvLeagueName, matchOdds.leagueName)
                        holder.setText(R.id.tvTime, matchOdds.startTimeDesc)
                        holder.setText(R.id.tvScore, matchOdds.rtScore)
                        holder.setText(
                            R.id.tvTeamNames,
                            matchOdds.homeName + " V.S " + matchOdds.awayName
                        )
                        holder.setText(
                            R.id.tvContent,
                            matchOdds.playName + " " + matchOdds.spread + " @ " + matchOdds.odds
                        )
                        if (position + 1 == t.matchOddsVOList.size) {
                            holder.getView<View>(R.id.divider2).visibility = View.GONE
                        }
                    }
                }
                rvComboDetailItem.adapter = detailAdapter
            }
        }
        mDialog.rvComboDetail.adapter = adapter
        return this
    }

    fun dismiss() {
        mDialog.dismiss()
    }
}