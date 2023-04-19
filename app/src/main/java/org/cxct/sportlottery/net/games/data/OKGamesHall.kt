package org.cxct.sportlottery.net.games.data

import android.widget.ImageView
import android.widget.TextView
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.proguards.KeepMembers
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
    val gameList: List<OKGameBean>?,
): OKGameTab {
    override fun tabId() = id

    override fun bindNameText(textView: TextView) {
        textView.text = categoryName
    }

    override fun bindTabIcon(imageView: ImageView, isSelected: Boolean) {
        imageView.load(if (isSelected) iconSelected else iconUnselected)
    }

    override fun bindLabelIcon(imageView: ImageView) {
        imageView.load(icon)
    }

}

@KeepMembers
data class OKGamesFirm(
    val id: Int,
    val firmName: String?,//厂商名称
    val img: String?,//厂商图
)

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
)
