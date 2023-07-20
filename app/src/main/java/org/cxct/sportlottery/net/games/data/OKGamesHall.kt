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
    var gameList: List<OKGameBean>?,
    //标记gameList是否大于18个
    var isMoreThan18:Boolean=false
): OKGameTab {
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
): OKGameLabel {

    override fun getKey() = id
    override fun bindLabelIcon(imageView: ImageView) {
        imageView.setImageResource(R.drawable.ic_okgame_p)
    }

    override fun bindLabelName(textView: TextView) {
        textView.setText(firmName)
    }

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
    val imgGame: String?,
    val gameEntryTagName: String?,
    val thirdGameCategory: String?,
    var markCollect: Boolean,
): BaseNode() {
    override val childNode: MutableList<BaseNode> = mutableListOf()
}
