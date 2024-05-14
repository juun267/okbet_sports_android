package org.cxct.sportlottery.ui.maintab.games

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemGameChildBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.GameCollectManager.showCollectAmount
import org.cxct.sportlottery.view.setTextColorGradient

class GameChildAdapter(val onFavoriate: (View, OKGameBean) -> Unit,
                       val moreClick: (() -> Unit)? = null,
                       val gameEntryType: String = GameEntryType.OKGAMES,
                       val showFavorite: Boolean = true) : BindingAdapter<OKGameBean, ItemGameChildBinding>() {

    private val GAME_MAINTAIN = Any()

    fun bindLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleOwner.lifecycleScope) { gamesMaintain ->
            data.forEachIndexed { index, okGameBean ->
                if (okGameBean.maintain != gamesMaintain.maintain && (okGameBean.firmType == gamesMaintain.firmType)) {
                    okGameBean.maintain = gamesMaintain.maintain
                    notifyItemChanged(index, GAME_MAINTAIN)
                }
            }
        }
    }

    private var moreTextView: TextView? = null
    private var gameTotal: Int = 0

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
//            setOnClickListener { moreClick?.invoke() }
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
            // //关闭jackpot ==0
            if(item.jackpotOpen==0){
                tvPot.gone()
                blurBottom.gone()
            }else{
                //==1 显示
                tvPot.visible()
                blurBottom.visible()
                blurBottom.setupWith(binding.root)
                    .setFrameClearDrawable(binding.root.background)
                    .setBlurRadius(1.3f)
                tvPot.setTextColorGradient()
                tvPot.text="$showCurrencySign ${TextUtil.formatMoney(item.jackpotAmount)}"
            }
            ivCover.layoutParams.apply {
                height = if (gameEntryType==GameEntryType.OKGAMES) 110.dp else 88.dp
                ivCover.layoutParams = this
            }
            ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
            tvName.text = item.gameName
            tvFirmName.text = item.firmName
            tvCollect.showCollectAmount(item.id)
            ivFav.isVisible = showFavorite
            ivFav.isSelected = item.markCollect
            ivFav.isEnabled = !item.isMaintain()
            ivFav.setOnClickListener {
                item.gameEntryType = gameEntryType
                onFavoriate.invoke(ivFav, item)
            }

            root.setOnClickListener {
                if (!item.isMaintain()) {
                    getOnItemClickListener()?.onItemClick(this@GameChildAdapter, root, position)
                }
            }

            setMaintain(tvCover, item.isMaintain())
        }
    }

    private fun setMaintain(tvCover: TextView, isMaintain: Boolean) {
        if (isMaintain) {
            tvCover.visible()
            tvCover.setTextColor(Color.WHITE)
            tvCover.setBackgroundColor(context.getColor(R.color.transparent_black_70))
            tvCover.setText(R.string.N257)
        } else {
            tvCover.gone()
        }
    }

    override fun onBinding(position: Int, binding: ItemGameChildBinding, item: OKGameBean, payloads: List<Any>) {
        payloads.forEach {
            if (it == GAME_MAINTAIN) {
                setMaintain(binding.tvCover, item.isMaintain())
            } else {
                binding.ivFav.isSelected = item.markCollect
            }
        }
    }

    private fun updateMoreText(total: Int, count: Int) {
        gameTotal = total
        moreTextView?.let { textView ->
            if (total > count) {
                textView.visible()
                textView.isEnabled = true
                textView.text = "${textView.context.getString(R.string.N885)}(${total - count})"
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

