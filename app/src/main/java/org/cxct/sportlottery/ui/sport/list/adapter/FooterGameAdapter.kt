package org.cxct.sportlottery.ui.sport.list.adapter

import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemGameChildBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.util.DisplayUtil.dp
import splitties.views.dsl.core.add

class FooterGameAdapter(private val onFavoriteClick: (View, OKGameBean) -> Unit,
                        private val onGameClick: (OKGameBean) -> Unit): BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(GameCategroyProvider())
        addNodeProvider(GameProvider())
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        if (data[position] is OKGameBean) {
            return 2
        }

        return 1
    }

    fun setupOKGames(okgames: List<OKGameBean>, onMore: () -> Unit) {
        if (okgames.isEmpty()) {
            return
        }
        addData(GameCategroy("OKGames", R.drawable.ic_okgame_label_games, okgames.take(3).toMutableList(), onMore))
    }

    fun setupOKLives(okLive: List<OKGameBean>, onMore: () -> Unit) {
        if (okLive.isEmpty()) {
            return
        }
        addData(0, GameCategroy("OKLive", R.drawable.ic_okgame_label_oklive, okLive.take(3).toMutableList(), onMore))
    }

    fun updateFavoriteStatu(okGameBean: OKGameBean) {
        val position = getItemPosition(okGameBean)
        if (position >= 0) {
            notifyItemChanged(position, okGameBean)
        }
    }

    private inner class GameCategroyProvider(override val itemViewType: Int = 1,
                                       override val layoutId: Int = 0): BaseNodeProvider() {

        private val iconId = View.generateViewId()
        private val nameId = View.generateViewId()
        private val moreId = View.generateViewId()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val context = parent.context
            val dp24 = 24.dp
            val linearLayout = LinearLayout(context)
            linearLayout.gravity = Gravity.CENTER_VERTICAL
            linearLayout.layoutParams = LayoutParams(-1, dp24).apply {
                topMargin = 16.dp
                bottomMargin = 8.dp
            }

            val img = AppCompatImageView(context)
            img.id = iconId
            linearLayout.add(img, LayoutParams(dp24, dp24))

            val text = AppCompatTextView(context)
            text.id = nameId
            text.textSize = 16f
            text.typeface = Typeface.DEFAULT_BOLD
            text.setTextColor(ContextCompat.getColor(context, R.color.color_14366B))
            val textParam = LayoutParams(0, -2, 1f)
            textParam.leftMargin = 4.dp
            linearLayout.addView(text, textParam)

            val more = AppCompatTextView(context)
            more.id = moreId
            more.textSize = 14f
            more.setTextColor(ContextCompat.getColor(context, R.color.color_6D7693))
            more.setText(R.string.N702)
            more.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_game_gray_arrow_right, 0)
            linearLayout.addView(more)

            return BaseViewHolder(linearLayout)
        }

        override fun convert(helper: BaseViewHolder, item: BaseNode) {
            val gameCategroy = item as GameCategroy
            helper.setText(nameId, gameCategroy.name)
            helper.setImageResource(iconId, gameCategroy.icon)
            helper.getView<View>(moreId).setOnClickListener { gameCategroy.moreEvent.invoke() }
        }
    }

    private inner class GameProvider(override val itemViewType: Int = 2, override val layoutId: Int = 0) : BaseNodeProvider() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingVH<ItemGameChildBinding> {
            return BindingVH(ItemGameChildBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun convert(helper: BaseViewHolder, data: BaseNode) {

            val item = data as OKGameBean
            (helper as BindingVH<ItemGameChildBinding>).vb.apply {
                ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
                tvName.text = item.gameName
                tvFirmName.text = item.firmName
                ivFav.isSelected = item.markCollect
                ivFav.setOnClickListener { onFavoriteClick.invoke(ivFav, item) }
                root.setOnClickListener { onGameClick.invoke(item) }
            }
        }

    }

    override fun convert(holder: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
        (holder as BindingVH<ItemGameChildBinding>).vb.ivFav.isSelected = (item as OKGameBean).markCollect
    }

}

data class GameCategroy(val name: String,
                        @DrawableRes val icon: Int,
                        val games: MutableList<OKGameBean>,
                        val moreEvent: () -> Unit,
                        override val childNode: MutableList<BaseNode> = games as MutableList<BaseNode>): BaseNode()

