package org.cxct.sportlottery.ui.maintab.home.game.slot

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemElecGameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.showCollectAmount

class ElectGameAdapter(val onFavoriate: (View, OKGameBean) -> Unit): BaseNodeAdapter() {

    private val MORE_LIMIT = 12
    private val groupIndex = mutableMapOf<Int, Int>()
    private val firmMap = mutableMapOf<Int, OKGamesFirm>()

    init {
        addFullSpanNodeProvider(GroupTitleProvider(this))
        addNodeProvider(ElectGameProvider(this,onFavoriate))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return if (data[position] is OKGamesCategory) 1 else 2
    }

    fun setupData(list: MutableList<OKGamesCategory>?) {
//        firmList?.forEach { firmMap[it.id] = it }
        groupIndex.clear()
        val groupList = list ?: return
        var position = 0
        groupList.forEach { baseNode ->
            groupIndex[baseNode.id] = position
            position++
            if (baseNode.gameList.size > MORE_LIMIT) {
                baseNode.gameList = baseNode.gameList.take(MORE_LIMIT)
                baseNode.gameList.last().isShowMore = true
            }
            baseNode.gameList.forEachIndexed { index, okGameBean ->
                okGameBean.parentNode = baseNode
                okGameBean.categoryId = baseNode.id
                position++
            }
        }
        setNewInstance(groupList as MutableList<BaseNode>)
    }



    fun findFirmPosition(firmId: Int): Int? {
        return groupIndex[firmId]
    }

}

private class GroupTitleProvider(val adapter: ElectGameAdapter, override val itemViewType: Int = 1, override val layoutId: Int = 0) : BaseNodeProvider() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val textView = AppCompatTextView(parent.context)
        textView.setTextColor(Color.BLACK)
        textView.textSize = 14f
        textView.setPadding(0, 0, 0, 8.dp)
        return BaseViewHolder(textView)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val textView = helper.itemView as TextView
        textView.text = (item as OKGamesCategory).categoryName
        textView.setPadding(0, if (adapter.getItemOrNull(0) == item) 0 else textView.paddingBottom, 0, textView.paddingBottom)
    }

}

private class ElectGameProvider(val adapter: ElectGameAdapter,
                                val onFavoriate: (View, OKGameBean) -> Unit,
                                override val itemViewType: Int = 2,
                                override val layoutId: Int = 0) : BaseNodeProvider() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemElecGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.tag = binding
        return BaseViewHolder(binding.root)
    }

    private val dp4 = 4.dp
    private val dp8 = 8.dp

    override fun convert(helper: BaseViewHolder, item: BaseNode): Unit = (helper.itemView.tag as ItemElecGameBinding).run {

        val bean = item as OKGameBean
        val childPosition = item.parentNode.childNode?.indexOf(item) ?: return@run
        val bottomMargin = if (adapter.getItemOrNull(adapter.data.size - 2)  == item|| adapter.data.last() == item) 16.dp else dp8
        if (childPosition % 2 == 0){
            setMargins(root, 0, 0, dp4, bottomMargin)
        }else{
            setMargins(root, dp4, 0, 0, bottomMargin)
        }

        linMaintenance.gone()
        cvJackpot.gone()
        ivCover.load(bean.imgGame, R.drawable.ic_okgames_nodata)
        tvCollect.showCollectAmount(bean.favoriteCount)
        ivFav.isSelected = bean.markCollect
        ivFav.setOnClickListener {
            onFavoriate.invoke(it,bean)
        }
        if (bean.isShowMore) {
            blurviewMore.visible()
            blurviewMore
                .setupWith(root)
                .setFrameClearDrawable(root.background)
                .setBlurRadius(4f)
            return@run
        }

        blurviewMore.gone()
        if (bean.isMaintain()) {
            linMaintenance.visible()
        }

        if (bean.jackpotOpen == 1) {
            cvJackpot.visible()
            blurviewJackpot
                .setupWith(root)
                .setFrameClearDrawable(root.background)
                .setBlurRadius(15f)
            tvJackPot.text = "$showCurrencySign ${TextUtil.formatMoney(bean.jackpotAmount)}"
        }else{
            cvJackpot.gone()
        }
    }
    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) = (helper.itemView.tag as ItemElecGameBinding).run {
        super.convert(helper, item, payloads)
        payloads.forEach {
            (it as? OKGameBean)?.let {
                tvCollect.showCollectAmount(it.favoriteCount)
                ivFav.isSelected = it.markCollect
            }
        }
    }
    private fun setMargins(btn: View, left: Int, top: Int, right: Int, bottom: Int) {
        val lParams = btn.layoutParams as ViewGroup.MarginLayoutParams
        lParams.leftMargin = left
        lParams.topMargin = top
        lParams.rightMargin = right
        lParams.bottomMargin = bottom
    }
}

