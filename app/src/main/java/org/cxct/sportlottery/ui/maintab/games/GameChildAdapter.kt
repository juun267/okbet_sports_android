package org.cxct.sportlottery.ui.maintab.games

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemGameChildBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp

class GameChildAdapter(val moreClick: (() -> Unit)? = null) : BindingAdapter<OKGameBean, ItemGameChildBinding>() {

    private var moreTextView: TextView? = null
    private var gameTotal: Int = 0

    init {
        addChildClickViewIds(R.id.iv_fav)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (moreClick != null) {
            initLoadMoreLayout(recyclerView.context)
        }
    }

    private fun initLoadMoreLayout(cxt: Context) {
        val pH = 14.dp
        val pV = 8.dp
        moreTextView = AppCompatTextView(cxt).apply {
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(pH, pV, pH, pV)
            setTextColor(cxt.getColor(R.color.color_6D7693))
            setBackgroundResource(R.drawable.bg_white_radius_8)
            setOnClickListener { moreClick?.invoke() }
            layoutParams = LinearLayout.LayoutParams(-2, -2).apply {
                topMargin = 10.dp
                bottomMargin = topMargin
                gravity = Gravity.CENTER_HORIZONTAL
            }
            addFooterView(this)
        }

        updateMoreText(gameTotal, getDefItemCount())
    }

    override fun onBinding(position: Int, binding: ItemGameChildBinding, item: OKGameBean) {
        binding.apply {
            ivCover.load(item.imgGame, R.drawable.img_banner01)
            tvName.text = item.gameName
            tvFirmName.text = item.firmCode
            ivFav.isSelected = item.markCollect
        }
    }

    override fun onBinding(position: Int, binding: ItemGameChildBinding, item: OKGameBean, payloads: List<Any>) {
        binding.ivFav.isSelected = item.markCollect
    }

    private fun updateMoreText(total: Int, count: Int) {
        gameTotal = total
        moreTextView?.let { textView ->
            if (total > count) {
                textView.visible()
                textView.isEnabled = true
                textView.text = "${textView.context.getString(R.string.display_more)}(${total - count})"
            } else {
                disableMore()
            }
        }
    }

    fun setGameList(list: List<OKGameBean>?, total: Int): Int {
        if (!list.isNullOrEmpty()) {
            addData(list!!)
        }
        val count = getDefItemCount()
        updateMoreText(total, count)
        return count
    }

    fun disableMore() {
        moreTextView?.gone()
    }

    fun onLoadingMore() = moreTextView?.let {
        it.isEnabled = false
        it.text = "${it.context.getString(R.string.loading)}..."
    }

    // 如果是收藏列表，收藏状态跟新后就进行添加或移除操作
    fun removeOrAdd(bean: OKGameBean) {
        val item = data.firstOrNull { it.id == bean.id }
        if (item == null) {
            if (bean.markCollect) {
                addData(0, bean)
            }
            return
        }

        remove(item)
        if (bean.markCollect) {
            addData(0, bean)
        }
    }
}

