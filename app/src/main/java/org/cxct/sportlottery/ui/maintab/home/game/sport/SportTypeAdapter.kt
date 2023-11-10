package org.cxct.sportlottery.ui.maintab.home.game.sport

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp


class SportTypeAdapter : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(SportGroupProvider())
        addFullSpanNodeProvider(SportMatchProvider())
        setAnimationWithDefault(AnimationType.SlideInBottom)
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return if (data[position] is SportGroup) 1 else 2
    }

    fun setUp(datas: MutableList<Pair<Int, Sport>>) {
        val list = mutableListOf<SportGroup>()
        datas.forEach { list.add(SportGroup(it.first, it.second.items.toMutableList())) }
        setNewInstance(list as MutableList<BaseNode>)
    }


}

private data class SportGroup(val name: Int,
                              val items: MutableList<Item>,
                              override val childNode: MutableList<BaseNode>? = items as MutableList<BaseNode>?): BaseNode()

private class SportGroupProvider(override val itemViewType: Int = 1, override val layoutId: Int = 0): BaseNodeProvider() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val textView = AppCompatTextView(parent.context)
        textView.setTextColor(Color.BLACK)
        textView.textSize = 14f
        textView.setPadding(0, 0, 0, 8.dp)
        return BaseViewHolder(textView)
    }
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        (helper.itemView as TextView).setText((item as SportGroup).name)
    }

}

private class SportMatchProvider(override val itemViewType: Int = 2, override val layoutId: Int = 0): BaseNodeProvider() {

    private val numberId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val imgId = View.generateViewId()
    private val lp = LinearLayout.LayoutParams(-1, 100.dp).apply { bottomMargin = 8.dp }
    private val nameLp = FrameLayout.LayoutParams(-2, -2).apply { topMargin = 24.dp }
    private val imgLp = FrameLayout.LayoutParams(-2, -2).apply {
        gravity = Gravity.RIGHT or Gravity.BOTTOM
    }
    private val numLp = FrameLayout.LayoutParams(-2, -2).apply {
        gravity = Gravity.BOTTOM
        bottomMargin = 15.dp
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val context = parent.context
        val frameLayout = FrameLayout(context)
        frameLayout.layoutParams = lp
        frameLayout.setBackgroundResource(R.drawable.bg_dz_fl_ty)
        frameLayout.setPadding(24.dp, 0, 0, 0)

        val img = AppCompatImageView(context)
        img.id = imgId
        frameLayout.addView(img, imgLp)

        val nameText = AppCompatTextView(context)
        nameText.id = nameId
        nameText.setTextColor(context.getColor(R.color.color_025BE8))
        nameText.textSize = 16f
        nameText.typeface = AppFont.inter_bold
        frameLayout.addView(nameText, nameLp)

        val numText = AppCompatTextView(context)
        numText.id = numberId
        numText.setTextColor(context.getColor(R.color.color_0D2245))
        numText.textSize = 20f
        numText.typeface = AppFont.inter_bold
        frameLayout.addView(numText, numLp)

        return BaseViewHolder(frameLayout)
    }
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val bean = item as Item
        helper.setText(nameId, bean.name)
        helper.setText(numberId, bean.num.toString())
        helper.setImageResource(imgId, getSportGroundImg("${bean.code}"))
    }

    private fun getSportGroundImg(code: String): Int {
        return when (code) {
            GameType.FT.key -> R.drawable.img_game_sport_ft_2
            GameType.BK.key -> R.drawable.img_game_sport_bk_2
            GameType.TN.key -> R.drawable.img_game_sport_tn_2
            GameType.VB.key -> R.drawable.img_game_sport_vb_2
            GameType.BM.key -> R.drawable.img_game_sport_bm_2
            GameType.TT.key -> R.drawable.img_game_sport_tt_2
            GameType.IH.key -> R.drawable.img_game_sport_ih_2
            GameType.BX.key -> R.drawable.img_game_sport_bx_2
            GameType.CB.key -> R.drawable.img_game_sport_cb_2
            GameType.CK.key -> R.drawable.img_game_sport_ck_2
            GameType.BB.key -> R.drawable.img_game_sport_bb_2
            GameType.RB.key -> R.drawable.img_game_sport_rb
            GameType.AFT.key -> R.drawable.img_game_sport_aft_2
            GameType.MR.key-> R.drawable.img_game_sport_mr
            GameType.GF.key -> R.drawable.img_game_sport_gf_2
            GameType.ES.key -> R.drawable.img_game_sport_es_2
            else -> R.drawable.ic_game_champ
        }
    }
}