package org.cxct.sportlottery.ui.menu.results

import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RotateDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_settlement_out_right_rv.view.*
import kotlinx.android.synthetic.main.content_settlement_rv.view.*
import org.cxct.sportlottery.databinding.ContentSettlementOutRightRvBinding
import org.cxct.sportlottery.databinding.ContentSettlementRvBinding
import org.cxct.sportlottery.network.matchresult.list.Row
import org.cxct.sportlottery.network.matchresult.playlist.SettlementRvData
import java.text.SimpleDateFormat
import java.util.*

class SettlementRvAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = listOf<Row>()
        set(value) {
            field = value

            mIsOpenList =
                MutableList(value.size) { false }//創建一個跟 DataList 一樣的 size，value 都為 true 的 List
            mAdapterList = MutableList(value.size) { null } //創建一個跟 DataList 一樣的 size，用來儲存下一層所使用的Adapter

            notifyDataSetChanged()
        }

    var mOutRightDatList = listOf<org.cxct.sportlottery.network.outright.Row>()
        set(value) {
            field = value

            mIsOpenList =
                MutableList(value.size) { false }//創建一個跟 DataList 一樣的 size，value 都為 true 的 List

            notifyDataSetChanged()
        }

    var gameType = ""

    var settleType: SettleType = SettleType.MATCH

    var mGameDetail: SettlementRvData? = null
        set(value) {
            field = value
            value?.settleRvPosition?.let {
                notifyItemChanged(it)
            }
        }

    interface SettlementRvListener {
        fun getGameResultDetail(settleRvPosition: Int, gameResultRvPosition: Int, matchId: String)
    }

    var mSettlementRvListener: SettlementRvListener? = null

    private var mIsOpenList: MutableList<Boolean> = mutableListOf()
    private var mAdapterList: MutableList<GameResultRvAdapter?> = mutableListOf()


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutView = LayoutInflater.from(viewGroup.context)
        return when (settleType) {
            SettleType.MATCH -> {
                ContentSettlementRvBinding.inflate(layoutView, viewGroup, false)
                ItemViewHolder(ContentSettlementRvBinding.inflate(layoutView, viewGroup, false))
            }
            SettleType.OUTRIGHT -> {
                OutRightItemViewHolder(ContentSettlementOutRightRvBinding.inflate(layoutView, viewGroup, false))
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is ItemViewHolder -> {
                setupData(viewHolder.itemView, position)
            }
            is OutRightItemViewHolder -> {
                setupOutRightData(viewHolder.itemView, position)
            }
        }
    }

    private fun setupData(itemView: View, position: Int) {
        itemView.apply {
            val data = mDataList[position]
            tv_type.text = data.league.name
            when (gameType) {
                GameType.FT.key -> { //上半場, 全場
                    tv_first_half.visibility = View.VISIBLE
                    tv_second_half.visibility = View.GONE
                    tv_end_game.visibility = View.GONE
                    tv_full_game.visibility = View.VISIBLE
                }
                GameType.BK.key -> { //上半場, 下半場, 賽果
                    tv_first_half.visibility = View.VISIBLE
                    tv_second_half.visibility = View.VISIBLE
                    tv_end_game.visibility = View.VISIBLE
                    tv_full_game.visibility = View.GONE
                }
                GameType.TN.key, GameType.BM.key, GameType.VB.key -> {
                    tv_first_half.visibility = View.GONE
                    tv_second_half.visibility = View.GONE
                    tv_end_game.visibility = View.VISIBLE
                    tv_full_game.visibility = View.GONE
                }
                else -> ""
            }

            if (mIsOpenList[position]) {
                setupDetailRv(this, position)
            }

            block_drawer_result.setExpanded(mIsOpenList[position], false)

            block_type.setOnClickListener {
                setupDetailRv(this, position)
                mIsOpenList[position] = !mIsOpenList[position]
                this.block_drawer_result.let { expandableLayout ->
                    expandableLayout.setExpanded(
                        mIsOpenList[position],
                        true
                    )
                }
                rotateTitleBlock(block_type)
            }
        }
    }

    private fun setupDetailRv(itemView: View, itemPosition: Int) {
        itemView.apply {
            val data = mDataList[itemPosition]
            if (mAdapterList[itemPosition] == null) {
                val newGameResultRvAdapter = GameResultRvAdapter()
                newGameResultRvAdapter.let {
                    rv_game_result.adapter = it
                    mAdapterList[itemPosition] = it
                    it.mDataList = data.list.toMutableList()
                }
            } else {
                rv_game_result.adapter = mAdapterList[itemPosition]
            }

            (rv_game_result.adapter as GameResultRvAdapter).apply {
                //下一層需要用到gameResultRvPosition判斷哪一個detail被點擊需展開，需在set mDataList前先賦值
                if (itemPosition == mGameDetail?.settleRvPosition && mGameDetailData == null)
                    mGameDetailData = this@SettlementRvAdapter.mGameDetail

                gameType = this@SettlementRvAdapter.gameType
                positionKey = itemPosition
                mGameResultDetailListener =
                    object : GameResultRvAdapter.GameResultDetailListener {
                        override fun getGameResultDetail(
                            gameResultRvPosition: Int,
                            matchId: String
                        ) {
                            mSettlementRvListener?.getGameResultDetail(
                                itemPosition,
                                gameResultRvPosition,
                                matchId
                            )
                        }
                    }
            }
        }
    }

    private fun setupOutRightData(itemView: View, position: Int) {
        itemView.apply {
            val data = mOutRightDatList[position]
            tv_league.text = data.season.name

            //TODO Dean : 之後可以寫成Util
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            tv_date.text = dateFormat.format(data.season.start.let {
                calendar.timeInMillis = it.toLong()
                calendar.time
            })

            tv_ranking.text = data.resultList[0].playCateName
            tv_winner.text = data.resultList[0].playName

            ep_champion.setExpanded(mIsOpenList[position], false)

            ll_type.setOnClickListener {
                mIsOpenList[position] = !mIsOpenList[position]
                this.ep_champion.let { expandableLayout ->
                    expandableLayout.setExpanded(
                        mIsOpenList[position],
                        true
                    )
                }
                rotateTitleBlock(ll_type)
            }
        }
    }

    private fun rotateTitleBlock(block: View) {
        val drawable = block.background
        ((drawable as LayerDrawable).getDrawable(1) as RotateDrawable).level += 10000
    }

    override fun getItemCount(): Int {
        return when (settleType) {
            SettleType.MATCH -> mDataList.size
            SettleType.OUTRIGHT -> mOutRightDatList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return settleType.ordinal
    }

    class ItemViewHolder internal constructor(private val binding: ContentSettlementRvBinding) : RecyclerView.ViewHolder(binding.root)

    class OutRightItemViewHolder internal constructor(private val binding: ContentSettlementOutRightRvBinding) : RecyclerView.ViewHolder(binding.root)
}