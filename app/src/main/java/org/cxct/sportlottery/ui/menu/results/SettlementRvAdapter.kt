package org.cxct.sportlottery.ui.menu.results

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_settlement_rv.view.*
import org.cxct.sportlottery.databinding.ContentSettlementRvBinding
import org.cxct.sportlottery.network.matchresult.list.Row
import org.cxct.sportlottery.network.matchresult.playlist.SettlementRvData

class SettlementRvAdapter() : RecyclerView.Adapter<SettlementRvAdapter.ItemViewHolder>() {

    var mDataList = listOf<Row>()
        set(value) {
            field = value

            mIsOpenList =
                MutableList(value.size) { false }//創建一個跟 DataList 一樣的 size，value 都為 true 的 List

            notifyDataSetChanged()
        }
    var gameType = ""
        set(value) {
            field = value
        }

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


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutView = LayoutInflater.from(viewGroup.context)
        val binding = ContentSettlementRvBinding.inflate(layoutView, viewGroup, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.itemView.apply {
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
                //TODO Dean : 待確認
                GameType.TN.key -> ""
                GameType.BM.key -> ""
                GameType.VB.key -> ""
                else -> ""
            }

            if (mIsOpenList[position]) { //TODO Dean : 箭頭旋轉
                block_drawer_result.expand(false)
            } else {
                block_drawer_result.collapse(false)
            }

            block_type.setOnClickListener {
                this.block_drawer_result.let { expandableLayout ->
                    expandableLayout.setExpanded(
                        !(expandableLayout.isExpanded),
                        true
                    )
                }
                mIsOpenList[viewHolder.adapterPosition] = !mIsOpenList[viewHolder.adapterPosition]
            }

            rv_game_result.adapter = GameResultRvAdapter()
            (rv_game_result.adapter as GameResultRvAdapter).apply {
                //下一層需要用到gameResultRvPosition判斷哪一個detail被點擊需展開，需在set mDataList前先賦值
                if (position == mGameDetail?.settleRvPosition)
                    mGameDetailData = this@SettlementRvAdapter.mGameDetail

                gameType = this@SettlementRvAdapter.gameType
                positionKey = position
                mDataList = data.list.toMutableList()
                mGameResultDetailListener =
                    object : GameResultRvAdapter.GameResultDetailListener {
                        override fun getGameResultDetail(
                            gameResultRvPosition: Int,
                            matchId: String
                        ) {
                            mSettlementRvListener?.getGameResultDetail(
                                position,
                                gameResultRvPosition,
                                matchId
                            )
                        }
                    }
            }
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    class ItemViewHolder internal constructor(private val binding: ContentSettlementRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}