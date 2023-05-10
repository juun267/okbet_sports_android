package org.cxct.sportlottery.ui.sport.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_sport_type_v4.view.*
import kotlinx.android.synthetic.main.itemview_sport_type_v6.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeString
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.maintab.entity.GameCateData
import org.cxct.sportlottery.ui.maintab.entity.ThirdGameCategory

class GameTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        SPORT, SPORT_HOME, THIRD_GAME
    }

    var dataSport = listOf<Item>()
        set(value) {
            field = value
            field.forEachIndexed { index, item ->
                notifyItemChanged(index, item)
            }
            // TODO 這裡需要另外處理GameType列表更新問題 By Hewie
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {

        when {
            payloads.isNullOrEmpty() -> {
                onBindViewHolder(holder, position)
            }
            else -> {
                when (val data = payloads.firstOrNull()) {
                    is Item -> {
                        when (holder) {
                            is ViewHolderSport -> {
                                holder.update(data, gameTypeListener)
                            }
                            is ViewHolderSportHome -> {
                                holder.bind(data, gameTypeListener)
                            }
                            else -> {
                                onBindViewHolder(holder, position)
                            }
                        }
                    }
                    else -> {
                        onBindViewHolder(holder, position)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderSport -> {
                val item = dataSport[position]
                holder.bind(dataSport.size,position,item, gameTypeListener)
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

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is ViewHolderSport -> {
//                with(holder) {
//                    if (sportImageAnimation != null) {
//                        itemView.sport_type_img.startAnimation(sportImageAnimation)
//                    } else {
//                        itemView.sport_type_img.clearAnimation()
//                    }
//                }
            }
        }

        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is ViewHolderSport -> {
                holder.itemView.sport_type_img.clearAnimation()
            }
        }
        super.onViewDetachedFromWindow(holder)
    }

    class ViewHolderSport private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //用來讓ViewAttachedToWindow和ViewDetachedFromWindow時判斷球種icon動畫配置用, 在onBindViewHolder時都要重新賦值
//        var sportImageAnimation: Animation? = null

        fun bind(count: Int, position: Int, item: Item, gameTypeListener: GameTypeListener?) {
            itemView.sport_type_img.setImageResource(GameType.getGameTypeMenuIcon(item.code))
//            setupSportTypeImage(itemView.sport_type_img, item)

            itemView.apply {
                setupSportContent(item, gameTypeListener)
                isSelected = item.isSelected

//                if (isSelected) {
//                    sportImageAnimation = AnimationUtils.loadAnimation(sport_type_img.context, R.anim.rotate_sport)
//                    sport_type_img.startAnimation(sportImageAnimation)
//                } else {
//                    sport_type_img.clearAnimation()
//                    sportImageAnimation = null
//                }
            }

        }

        fun update(item: Item, gameTypeListener: GameTypeListener?) {
            itemView.sport_type_img.setImageResource(GameType.getGameTypeMenuIcon(item.code))
//            setupSportTypeImage(itemView.sport_type_img, item)
            setupSportContent(item, gameTypeListener)

            updateSelected(item)
        }

        private fun setupSportContent(item: Item, gameTypeListener: GameTypeListener?){
            with(itemView) {
                sport_type_text.text = getGameTypeString(context, item.code)

                val sportCountText: String
                val sportCountTextColor: Int
                val sportTypeTextColor: Int
                //暫時利用num判斷是否為coming soon
                if (item.num == -1) {
                    sportCountText = context.getString(R.string.coming_soon)
                    sportCountTextColor = R.color.color_F75452_E23434
                    isEnabled = false
                } else {
                    sportCountText = item.num.toString()
                    sportCountTextColor =
                        if (item.isSelected) R.color.color_0760D4
                        else R.color.color_BBBBBB_333333

                    isEnabled = true
                }

                sport_count_text.text = sportCountText
                sport_count_text.setTextColor(ContextCompat.getColor(context, sportCountTextColor))
                sport_type_text.setTextColor(ContextCompat.getColor(context, if (item.isSelected) R.color.color_0760D4
                else R.color.color_BBBBBB_333333))
                setOnClickListener {
                    gameTypeListener?.onClick(item)
                }
            }
        }

//        private fun setupSportTypeImage(img: ImageView, item: Item) {
//            when (item.code) {
//                GameType.FT.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_ft_v5)
//                }
//                GameType.BK.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_bk_v5)
//                }
//                GameType.TN.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_tn_v5)
//                }
//                GameType.VB.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_vb_v5)
//                }
//                GameType.BM.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_bm_v5)
//                }
//                GameType.TT.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_tt_v5)
//                }
//                GameType.IH.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_ih_v5)
//                }
//                GameType.BX.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_bx_v5)
//                }
//                GameType.CB.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_cb_v5)
//                }
//                GameType.CK.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_ck_v5)
//                }
//                GameType.BB.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_bb_v5)
//                }
//                GameType.RB.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_rb_v5)
//                }
//                GameType.AFT.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_aft_v5)
//                }
//                GameType.MR.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_mr_v5)
//                }
//                GameType.GF.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_gf_v5)
//                }
//                GameType.FB.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_fb_v5)
//                }
//                GameType.BB_COMING_SOON.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_bb_v5)
//                }
//                GameType.ES_COMING_SOON.key -> {
//                    img.setImageResource(R.drawable.selector_sport_type_item_img_es_v5)
//                }
//            }
//        }

        /**
         * 更新選中狀態,
         */
        private fun updateSelected(item: Item) {
            with(itemView) {
                var needUpdateAnimationStatus = false
                if (isSelected != item.isSelected) {
                    needUpdateAnimationStatus = true
                }

                isSelected = item.isSelected

//                if (needUpdateAnimationStatus) {
//                    when (isSelected) {
//                        true -> {
//                            sportImageAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_sport)
//                            sport_type_img.startAnimation(
//                                sportImageAnimation
//                            )
//                        }
//                        false -> {
//                            sport_type_img.clearAnimation()
//                            sportImageAnimation = null
//                        }
//                    }
//                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolderSport {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_sport_type_v6, parent, false)

                return ViewHolderSport(view)
            }
        }
    }

    class ViewHolderSportHome private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Item, gameTypeListener: GameTypeListener?) {
            itemView.sport_type_home_img.setImageResource(GameType.getGameTypeMenuIcon(item.code))
//            setupSportTypeImage(itemView.sport_type_home_img, item)

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

//        private fun setupSportTypeImage(img: ImageView, item: Item) {
//            when (item.code) {
//                GameType.FT.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_ft)
//                }
//                GameType.BK.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_bk)
//                }
//                GameType.TN.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_tn)
//                }
//                GameType.VB.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_vb)
//                }
//                GameType.BM.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_bm)
//                }
//                GameType.TT.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_pp)
//                }
//                GameType.IH.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_ih)
//                }
//                GameType.BX.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_bx)
//                }
//                GameType.CB.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_cb)
//                }
//                GameType.CK.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_ck)
//                }
//                GameType.BB.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_bb)
//                }
//                GameType.RB.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_rb)
//                }
//                GameType.AFT.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_aft)
//                }
//                GameType.MR.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_mr)
//                }
//                GameType.GF.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_gf)
//                }
//                GameType.FB.key -> {
//                    img.setImageResource(R.drawable.selector_left_menu_ball_fb)
//                }
//            }
//        }
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
                    itemView.sport_type_text.text = itemView.context.getString(R.string.J203)
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