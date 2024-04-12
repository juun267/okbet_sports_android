package org.cxct.sportlottery.ui.results

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingMutilAdapter
import org.cxct.sportlottery.databinding.*
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.results.vh.*

class MatchResultDiffAdapter(private val matchItemClickListener: MatchItemClickListener) :
    BindingMutilAdapter<MatchResultData>() {

    var gameType: String = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    override fun initItemType() {
       addItemType(ListType.TITLE.ordinal,object :OnMultiItemAdapterListener<MatchResultData, ItemMatchResultTitleBinding>(){
           override fun onBinding(
               position: Int,
               binding: ItemMatchResultTitleBinding,
               item: MatchResultData,
           ) {
               MatchTitleViewHolder(binding,position).bind(gameType, item, matchItemClickListener)
           }
       })
        addItemType(ListType.MATCH.ordinal,object :OnMultiItemAdapterListener<MatchResultData, ItemMatchResultMatchNewBinding>(){
            override fun onBinding(
                position: Int,
                binding: ItemMatchResultMatchNewBinding,
                item: MatchResultData,
            ) {
                MatchViewHolder(binding,position).bind(gameType, item, matchItemClickListener)
                setupBottomLine(position, binding.bottomLine)
            }
        })
        addItemType(ListType.MATCH_FT.ordinal,object :OnMultiItemAdapterListener<MatchResultData, ItemMatchResultFtBinding>(){
            override fun onBinding(
                position: Int,
                binding: ItemMatchResultFtBinding,
                item: MatchResultData,
            ) {
                FtMatchViewHolder(binding,position).bind(item, matchItemClickListener)
                setupBottomLine(position, binding.bottomLine)
            }
        })
        addItemType(ListType.FIRST_ITEM_FT.ordinal,object :OnMultiItemAdapterListener<MatchResultData, ContentGameDetailResultFtRvBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentGameDetailResultFtRvBinding,
                item: MatchResultData,
            ) {
                FtDetailFirstItemViewHolder(binding).bind(item.matchData)
                setupBottomLine(position, binding.bottomLine, binding.llRoot)
            }
        })
        addItemTypes(listOf(ListType.FIRST_ITEM_BK.ordinal,ListType.FIRST_ITEM_AFT.ordinal),object :OnMultiItemAdapterListener<MatchResultData, ContentGameDetailResultBkRvBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentGameDetailResultBkRvBinding,
                item: MatchResultData,
            ) {
                BkDetailFirstItemViewHolder(binding).bind(item)
                setupBottomLine(position, binding.bottomLine, binding.root)
            }
        })
        addItemType(ListType.FIRST_ITEM_TN.ordinal,object :OnMultiItemAdapterListener<MatchResultData, ContentGameDetailResultTnRvBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentGameDetailResultTnRvBinding,
                item: MatchResultData,
            ) {
                TnDetailFirstItemViewHolder(binding).bind(item.matchData)
                setupBottomLine(position, binding.bottomLine, binding.root)
            }
        })
        addItemType(ListType.FIRST_ITEM_BM.ordinal,object :OnMultiItemAdapterListener<MatchResultData, ContentGameDetailResultBmRvBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentGameDetailResultBmRvBinding,
                item: MatchResultData,
            ) {
                BmDetailFirstItemViewHolder(binding).bind(item.matchData)
                setupBottomLine(position, binding.bottomLine, binding.root)
            }
        })
        addItemTypes(listOf(ListType.FIRST_ITEM_VB.ordinal, ListType.FIRST_ITEM_MR.ordinal, ListType.FIRST_ITEM_GF.ordinal),object :OnMultiItemAdapterListener<MatchResultData, ContentGameDetailResultVbRvBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentGameDetailResultVbRvBinding,
                item: MatchResultData,
            ) {
                VbDetailFirstItemViewHolder(binding).bind(item.matchData)
                setupBottomLine(position, binding.bottomLine, binding.root)
            }
        })
        addItemType(ListType.FIRST_ITEM_BB.ordinal,object :OnMultiItemAdapterListener<MatchResultData, ContentGameDetailResultBbRvBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentGameDetailResultBbRvBinding,
                item: MatchResultData,
            ) {
                BbDetailFirstItemViewHolder(binding).bind(item.matchData)
                setupBottomLine(position, binding.bottomLine, binding.root)
            }
        })
        addItemType(ListType.FIRST_ITEM_TT.ordinal,object :OnMultiItemAdapterListener<MatchResultData, ContentGameDetailResultTtRvBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentGameDetailResultTtRvBinding,
                item: MatchResultData,
            ) {
                TtDetailFirstItemViewHolder(binding).bind(item.matchData)
                setupBottomLine(position, binding.bottomLine, binding.root)
            }
        })
        addItemType(ListType.FIRST_ITEM_IH.ordinal,object :OnMultiItemAdapterListener<MatchResultData, ContentGameDetailResultIhRvBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentGameDetailResultIhRvBinding,
                item: MatchResultData,
            ) {
                IhDetailFirstItemViewHolder(binding).bind(item.matchData)
                setupBottomLine(position, binding.bottomLine, binding.root)
            }
        })
        addItemTypes(listOf(ListType.FIRST_ITEM_CK.ordinal, ListType.FIRST_ITEM_BX.ordinal, ListType.FIRST_ITEM_CB.ordinal, ListType.FIRST_ITEM_RB.ordinal),
            object :OnMultiItemAdapterListener<MatchResultData, ItemviewEmptyBinding>(){
            override fun onBinding(
                position: Int,
                binding: ItemviewEmptyBinding,
                item: MatchResultData,
            ) {

            }
        })
        //listOf(ListType.FIRST_ITEM_ES.ordinal,ListType.DETAIL.ordinal)
        setDefaultItemType(object :OnMultiItemAdapterListener<MatchResultData, ContentGameDetailResultRvBinding>(){
                override fun onBinding(
                    position: Int,
                    binding: ContentGameDetailResultRvBinding,
                    item: MatchResultData,
                ) {
                    DetailItemViewHolder(binding).bind(item.matchDetailData)
                    setupBottomLine(position, binding.bottomLine, binding.root)
                }
            })
    }

    override fun onItemType(position: Int): Int {
        val itemType = getItem(position).dataType.ordinal
        return if (itemType == ListType.MATCH.ordinal &&  GameType.FT.key == gameType) ListType.MATCH_FT.ordinal else itemType
    }


    private fun setupBottomLine(position: Int, bottomLine: View, llRoot: View? = null) {
        bottomLine.visibility =
            if (position + 1 < itemCount && (getItemViewType(position + 1) != ListType.TITLE.ordinal || getItemViewType(
                    position
                ) == ListType.MATCH.ordinal)
            ) {
                llRoot?.setBackgroundResource(R.drawable.bg_no_top_bottom_stroke_white)
                View.VISIBLE
            } else {
                llRoot?.setBackgroundResource(R.drawable.bg_shape_bottom_8dp_white_stroke_no_top_stroke)
                View.GONE
            }
    }
}
class MatchItemClickListener(
    private val titleClick: (titlePosition: Int) -> Unit,
    private val matchClick: (matchClick: Int) -> Unit
) {
    fun leagueTitleClick(titlePosition: Int) = titleClick.invoke(titlePosition)
    fun matchClick(matchPosition: Int) = matchClick.invoke(matchPosition)
}
