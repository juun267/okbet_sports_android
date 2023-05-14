package org.cxct.sportlottery.ui.betList.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ContentBetInfoItemV32Binding
import org.cxct.sportlottery.databinding.ContentBetInfoItemV3BaseketballEndingCardBinding
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter.BetRvType.*
import org.cxct.sportlottery.ui.betList.holder.BasketballEndingCardViewHolder
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.betList.listener.OnSelectedPositionListener
import org.cxct.sportlottery.util.KeyboardView
import org.cxct.sportlottery.ui.betList.holder.BatchParlayConnectViewHolder as BpcVh
import org.cxct.sportlottery.ui.betList.holder.BatchSingleInMoreOptionViewHolder as bsiMoVh
import org.cxct.sportlottery.ui.betList.holder.BetInfoItemViewHolder as BiVh
import org.cxct.sportlottery.ui.betList.holder.OddsChangedWarnViewHolder as OcWvH

class BetListRefactorAdapter(
    private val keyboardView: KeyboardView,
    private val onItemClickListener: OnItemClickListener,
    private val userBalance: () -> Double
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType { Bet, Parlay, BasketballEndingCard }
    enum class BetViewType { SINGLE, PARLAY, NULL }


    /**
     * @property SINGLE 单注
     * @property PARLAY_SINGLE 串关-单注
     * @property PARLAY 串关-多注
     */
    enum class BetRvType { SINGLE, PARLAY_SINGLE, PARLAY, BasketballEndingCard }

    var adapterBetType: BetRvType = SINGLE
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    var betList: MutableList<BetInfoListData>? = mutableListOf()
        set(value) {
            field = value
            //判斷是否有注單封盤
            hasBetClosed =
                value?.find { it.matchOdd.status != BetStatus.ACTIVATED.code || it.pointMarked } != null

            hasBetClosedForSingle =
                value?.find { it.matchOdd.status != BetStatus.ACTIVATED.code } != null
            notifyDataSetChanged()
        }
    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder is BiVh) {
            true -> holder.clearHandler()
            else -> {}
        }
    }

    var mSelectedPosition: Int = -1

    var mBetView = BetViewType.NULL

    var onSelectedPositionListener: OnSelectedPositionListener =
        object : OnSelectedPositionListener {
            override fun onSelectChange(position: Int, single: BetViewType) {
                if (mSelectedPosition != position || mBetView != single) {
                    mSelectedPosition = position
                    mBetView = single
                    notifyDataSetChanged()
                }
            }
        }

    var userLogin: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var userMoney: Double = 0.0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var hasBetClosed: Boolean = false
    var hasBetClosedForSingle: Boolean = false

    var hasParlayList: Boolean = false

    var parlayList: MutableList<ParlayOdd>? = mutableListOf()
        set(value) {
            //若無法組合串關時, 給予空物件用來紀錄”單注填充所有單注“的輸入金額
            field = value
            notifyDataSetChanged()
        }


//    private var isOddsChangedWarn = false //顯示賠率變更提示

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            //单注和串关
            ViewType.Bet.ordinal -> BiVh(
                ContentBetInfoItemV32Binding.inflate(layoutInflater), userBalance
            )

            //篮球末尾比分
            ViewType.BasketballEndingCard.ordinal -> BasketballEndingCardViewHolder(
                ContentBetInfoItemV3BaseketballEndingCardBinding.inflate(layoutInflater),
                userBalance
            )

            //串关多注、注单列表
            else -> BpcVh(
                layoutInflater.inflate(
                    R.layout.item_bet_list_batch_control_connect_v3, parent, false
                ), keyboardView
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var currentOddsType = oddsType
        betList?.getOrNull(position)?.apply {
            if (matchOdd.isOnlyEUType ||
                matchOdd.odds == matchOdd.malayOdds
                || matchType == MatchType.OUTRIGHT
                || matchType == MatchType.OTHER_OUTRIGHT) {
                currentOddsType = OddsType.EU
            }
        }
        extracted(holder, position, currentOddsType)
    }


    override fun getItemViewType(position: Int): Int {
        return when (adapterBetType) {
            SINGLE, PARLAY_SINGLE -> {
                ViewType.Bet.ordinal
            }

            PARLAY -> {
                ViewType.Parlay.ordinal
            }

            BasketballEndingCard -> {
                ViewType.BasketballEndingCard.ordinal
            }
        }
    }

    override fun getItemCount(): Int {
        //region 20220607 投注單版面調整
        return getListSize()
    }

    private fun extracted(
        holder: RecyclerView.ViewHolder, position: Int, currentOddsType: OddsType
    ) {
        when (holder) {
            is BasketballEndingCardViewHolder -> {
//                betList?.add(betList!![0])
                betList?.getOrNull(position)?.let { betInfoListData ->
                    holder.bind(
                        betList,
                        betInfoListData,
                        currentOddsType,
                        itemCount,
                        onItemClickListener,
                        betList?.size ?: 0,
                        mSelectedPosition,
                        onSelectedPositionListener,
                        position,
                        userMoney,
                        userLogin,
                        adapterBetType
                    )
                }
            }

            is BiVh -> {
                betList?.getOrNull(position)?.let { betInfoListData ->
                    holder.bind(
                        betInfoListData,
                        currentOddsType,
                        itemCount,
                        onItemClickListener,
                        betList?.size ?: 0,
                        mSelectedPosition,
                        onSelectedPositionListener,
                        position,
                        userMoney,
                        userLogin,
                        adapterBetType
                    )
                }
            }

            is BpcVh -> {
                holder.bind(
                    ////region 20220607 投注單版面調整
                    parlayList?.getOrNull(
                        position
//                        when (isOddsChangedWarn) {
//                            true -> position - 1
//                            false -> position
//                        }
                    ),
                    //endregion
                    currentOddsType,
                    hasBetClosed,
                    onItemClickListener,
                    mSelectedPosition,
                    mBetView,
                    onSelectedPositionListener,
                    position,
                    userMoney,
                    userLogin,
                    betList
                )
            }

            is bsiMoVh -> {
                holder.bind(
                    parlayList?.getOrNull(0),
                    betList ?: mutableListOf(),
                    currentOddsType,
                    onItemClickListener,
                    { notifyDataSetChanged() },
                    mSelectedPosition,
                    mBetView,
                    onSelectedPositionListener,
                    position,
                    hasBetClosedForSingle,
                    userMoney,
                    userLogin
                )
            }
        }
    }

//    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
//        super.onViewAttachedToWindow(holder)
//        attachedViewSet.add(holder)
//    }
//
//    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
//        super.onViewDetachedFromWindow(holder)
//        attachedViewSet.remove(holder)
//    }
//
//    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
//        super.onDetachedFromRecyclerView(recyclerView)
//        attachedViewSet.clear()
//    }

    //使用HasStabledIds需複寫回傳的position, 若仍使用super.getItemId(position), 數據刷新會錯亂.
    //https://blog.csdn.net/karsonNet/article/details/80598435
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


//    fun closeAllKeyboard() {
//        attachedViewSet.forEach {
//            it.itemView.findViewById<KeyboardView>(R.id.layoutKeyBoard)?.hideKeyboard()
//        }
//    }

    fun getListSize(): Int {
        //region 20220607 投注單版面調整
        val betListSize = betList?.size ?: 0

        return when (adapterBetType) {
            SINGLE, PARLAY_SINGLE -> {
                betListSize
            }

            //篮球末位比分只有一行数据
            BasketballEndingCard -> {
                1
            }

            PARLAY -> {
                when {
                    betListSize < 2 -> {
                        0
                    }

                    else -> {
                        val parlayListSize = parlayList?.size ?: 0
                        parlayListSize
                    }
                }
            }
        }
    }
}