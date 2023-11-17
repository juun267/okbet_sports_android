package org.cxct.sportlottery.net.games.data

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameLabel
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab

@KeepMembers
class OKGamesHall(
    val categoryList: List<OKGamesCategory>?,
    val firmList: List<OKGamesFirm>?,
    val collectList: List<OKGameBean>?,
)

@KeepMembers
data class OKGamesCategory(
    val id: Int,
    val categoryName: String?,
    val icon: String?,
    val iconSelected: String?,
    val iconUnselected: String?,
    val gameList: List<OKGameBean>
): OKGameTab, BaseNode() {

    override val childNode: MutableList<BaseNode>?
        get() = gameList as MutableList<BaseNode>?

    override fun getKey() = id

    override fun bindNameText(textView: TextView) {
        textView.text = categoryName
    }

    override fun bindTabIcon(imageView: ImageView, isSelected: Boolean) {
        imageView.load(if (isSelected) iconSelected else iconUnselected)
    }

    override fun bindLabelIcon(imageView: ImageView) {
        imageView.load(icon)
    }

    override fun bindLabelName(textView: TextView) {
        textView.text = categoryName
    }

}

@KeepMembers
data class OKGamesFirm(
    val id: Int,
    val firmName: String?,//厂商名称
    val img: String?,//厂商图
    val imgMobile: String?,
    val remark: String?,
    var maintain: Int?  // 0:游戏正常开启, 1: 维护状态
): OKGameLabel {

    override fun getKey() = id
    override fun bindLabelIcon(imageView: ImageView) {
        imageView.setImageResource(R.drawable.ic_okgame_p)
    }

    override fun bindLabelName(textView: TextView) {
        textView.text = firmName
    }
    fun isMaintain() = 1 == maintain

}

@KeepMembers
data class OKGameBean(
    val id: Int,
    val firmId: Int,
    val firmType: String?,
    val firmCode: String?,
    val firmName: String?,
    val gameCode: String?,
    val gameName: String?,
    val gameType: String?,
    val imgGame: String?,
    val gameEntryTagName: String?,
    val thirdGameCategory: String?,
    var markCollect: Boolean,
    var gameEntryType: String?,
    var maintain: Int?, // 0:游戏正常开启, 1: 维护状态
    val jackpotAmount:Double, //0不显示1显示
    val jackpotOpen:Int,
): BaseNode() {
    override val childNode: MutableList<BaseNode> = mutableListOf()
    var categoryId = -1
    fun isMaintain() = maintain == 1
    var isShowMore = false
    var isShowBlank = true
}
