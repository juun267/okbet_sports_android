package org.cxct.sportlottery.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R

/**
 * @author kevin
 * @create 2022/6/8
 * @description 陸續優化
 * @note 外部設定 itemType 時，是使用 BaseItemType.xxx.type 判別
 */
abstract class BaseGameAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isPreload: Boolean = false

    fun initBaseViewHolders(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            BaseItemType.PRELOAD_ITEM.type -> {
                PreloadItemViewHolder.from(parent)
            }
            else -> {
                NoDataViewHolder.from(parent)
            }
        }

    class NoDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.itemview_game_no_record, parent, false)
                )
        }
    }

    class PreloadItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(parent: ViewGroup) = PreloadItemViewHolder(View(parent.context) )
        }
    }


    enum class BaseItemType(val type: Int) {
        NO_DATA(9999),
        PRELOAD_ITEM(9998),
    }

}