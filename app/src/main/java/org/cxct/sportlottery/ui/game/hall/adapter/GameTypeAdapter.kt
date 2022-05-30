package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_sport_type_v4.view.*
import kotlinx.android.synthetic.main.itemview_sport_type_v5.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeString
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory

class GameTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        SPORT, SPORT_HOME, THIRD_GAME
    }

    var dataSport = listOf<Item>()
        set(value) {
            field = value
            notifyDataSetChanged() // TODO 這裡需要另外處理GameType列表更新問題 By Hewie
        }

    private var dataThirdGame = listOf<GameCateData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var gameTypeListener: GameTypeListener? = null

    var thirdGameListener: ThirdGameListener? = null

    var isFromHome = false

    override fun getItemViewType(position: Int): Int = when {
        isFromHome -> ItemType.SPORT_HOME.ordinal
        (position < dataSport.size) -> ItemType.SPORT.ordinal
        else -> ItemType.THIRD_GAME.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ItemType.SPORT_HOME.ordinal -> ViewHolderSportHome.from(parent)
            ItemType.SPORT.ordinal -> ViewHolderSport.from(parent)
            else -> ViewHolderThirdGame.from(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderSport -> {
                val item = dataSport[position]
                holder.bind(item, gameTypeListener)
            }
            is ViewHolderSportHome -> {
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

            setupSportTypeImage(itemView.sport_type_img, item)

            itemView.apply {

                sport_type_text.text = getGameTypeString(context, item.code)

                sport_count_text.text = item.num.toString()

                isSelected = item.isSelected

                if (isSelected) {
                    sport_type_img.startAnimation(
                        AnimationUtils.loadAnimation(context, R.anim.rotate_sport)
                    )
                }

                setOnClickListener {
                    gameTypeListener?.onClick(item)
                }

            }

        }

        private fun setupSportTypeImage(img: ImageView, item: Item) {
            when (item.code) {
                GameType.FT.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_ft_v5)
                }
                GameType.BK.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_bk_v5)
                }
                GameType.TN.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_tn_v5)
                }
                GameType.VB.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_vb_v5)
                }
                GameType.BM.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_bm_v5)
                }
                GameType.TT.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_tt_v5)
                }
                GameType.IH.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_ih_v5)
                }
                GameType.BX.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_bx_v5)
                }
                GameType.CB.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_cb_v5)
                }
                GameType.CK.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_ck_v5)
                }
                GameType.BB.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_bb_v5)
                }
                GameType.RB.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_rb_v5)
                }
                GameType.AFT.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_aft_v5)
                }
                GameType.MR.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_mr_v5)
                }
                GameType.GF.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_gf_v5)
                }
                GameType.FB.key -> {
                    img.setImageResource(R.drawable.selector_sport_type_item_img_fb_v5)
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

    class ViewHolderSportHome private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Item, gameTypeListener: GameTypeListener?) {

            setupSportTypeImage(itemView.sport_type_home_img, item)

            itemView.apply {

                sport_type_home_text.text = getGameTypeString(context, item.code)

                isSelected = item.isSelected

                setOnClickListener {
                    gameTypeListener?.onClick(item)
                }

            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolderSportHome {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_sport_type_v4, parent, false)

                return ViewHolderSportHome(view)
            }
        }

        private fun setupSportTypeImage(img: ImageView, item: Item) {
            when (item.code) {
                GameType.FT.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_ft)
                }
                GameType.BK.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_bk)
                }
                GameType.TN.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_tn)
                }
                GameType.VB.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_vb)
                }
                GameType.BM.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_bm)
                }
                GameType.TT.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_pp)
                }
                GameType.IH.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_ih)
                }
                GameType.BX.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_bx)
                }
                GameType.CB.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_cb)
                }
                GameType.CK.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_ck)
                }
                GameType.BB.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_bb)
                }
                GameType.RB.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_rb)
                }
                GameType.AFT.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_aft)
                }
                GameType.MR.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_mr)
                }
                GameType.GF.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_gf)
                }
                GameType.FB.key -> {
                    img.setImageResource(R.drawable.selector_left_menu_ball_fb)
                }
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