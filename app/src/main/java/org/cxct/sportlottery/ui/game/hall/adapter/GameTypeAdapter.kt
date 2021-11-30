package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_sport_type.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory

class GameTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        SPORT, THIRD_GAME
    }

    var dataSport = listOf<Item>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var dataThirdGame = listOf<GameCateData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var gameTypeListener: GameTypeListener? = null

    var thirdGameListener: ThirdGameListener? = null

    override fun getItemViewType(position: Int): Int = when {
        (position < dataSport.size) -> ItemType.SPORT.ordinal
        else -> ItemType.THIRD_GAME.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ItemType.SPORT.ordinal -> ViewHolderSport.from(parent)
            else -> ViewHolderThirdGame.from(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderSport -> {
                val item = dataSport[position]
                holder.bind(item, gameTypeListener)
            }
            is ViewHolderThirdGame -> {
                val item = dataThirdGame[position - dataSport.size]
                holder.bind(item, thirdGameListener)
            }
        }
    }

    override fun getItemCount(): Int = dataSport.size + dataThirdGame.size

    class ViewHolderSport private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Item, gameTypeListener: GameTypeListener?) {

            setupSportTypeImage(item)

            itemView.sport_type_text.text = item.name

            itemView.isSelected = item.isSelected

            itemView.setOnClickListener {
                gameTypeListener?.onClick(item)
            }
        }

        private fun setupSportTypeImage(item: Item) {
            when (item.code) {
                GameType.FT.key -> {
                    itemView.sport_type_img.setImageResource(R.drawable.selector_sport_type_item_img_ft_v5)
                }
                GameType.BK.key -> {
                    itemView.sport_type_img.setImageResource(R.drawable.selector_sport_type_item_img_bk_v5)
                }
                GameType.TN.key -> {
                    itemView.sport_type_img.setImageResource(R.drawable.selector_sport_type_item_img_tn_v5)
                }
                GameType.VB.key -> {
                    itemView.sport_type_img.setImageResource(R.drawable.selector_sport_type_item_img_vb_v4)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolderSport {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_sport_type_v5, parent, false)

                return ViewHolderSport(view)
            }
        }
    }

    class ViewHolderThirdGame private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: GameCateData, thirdGameListener: ThirdGameListener?) {
            when (item.categoryThird) {
                ThirdGameCategory.CGCP -> {
                    itemView.sport_type_img.setImageResource(R.drawable.ic_sportbookiconlottery)
                    itemView.sport_type_text.text = itemView.context.getString(R.string.lottery)
                }
                ThirdGameCategory.LIVE -> {
                    itemView.sport_type_img.setImageResource(R.drawable.ic_sportbookiconlivecasino)
                    itemView.sport_type_text.text = itemView.context.getString(R.string.live)
                }
                ThirdGameCategory.QP -> {
                    itemView.sport_type_img.setImageResource(R.drawable.ic_sportbookiconpoker)
                    itemView.sport_type_text.text = itemView.context.getString(R.string.poker)
                }
                ThirdGameCategory.DZ -> {
                    itemView.sport_type_img.setImageResource(R.drawable.ic_sportbookiconslotgame)
                    itemView.sport_type_text.text = itemView.context.getString(R.string.slot)
                }
                ThirdGameCategory.BY -> {
                    itemView.sport_type_img.setImageResource(R.drawable.ic_sportbookiconfishing)
                    itemView.sport_type_text.text = itemView.context.getString(R.string.fishing)
                }
                else -> {
                }
            }

            itemView.setOnClickListener {
                thirdGameListener?.onClick(item.categoryThird)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolderThirdGame {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_sport_type, parent, false)

                return ViewHolderThirdGame(view)
            }
        }
    }
}

class GameTypeListener(val clickListener: (item: Item) -> Unit) {
    fun onClick(item: Item) = clickListener(item)
}

class ThirdGameListener(val clickListener: (thirdGameCategory: ThirdGameCategory) -> Unit) {
    fun onClick(thirdGameCategory: ThirdGameCategory) = clickListener(thirdGameCategory)
}