package org.cxct.sportlottery.net.games.data

import android.os.Parcelable
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.entity.node.BaseNode
import kotlinx.android.parcel.Parcelize
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
    var gameList: List<OKGameBean>
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
    var maintain: Int?,  // 0:游戏正常开启, 1: 维护状态
    val sort:Int?,
    val firmShowName: String?, //对应中文名
    val open: Int?, //平台开关状态,0-关闭，1-开启
    val gameEntryTypeEnum: String?
): OKGameLabel {
    override fun getKey() = id
    override fun bindLabelIcon(imageView: ImageView) {
        imageView.setImageResource(R.drawable.ic_okgame_p)
    }

    override fun bindLabelName(textView: TextView) {
        textView.text = textView.context.getString(R.string.N880)
    }
    fun isMaintain() = 1 == maintain

}

@KeepMembers
@Parcelize
data class OKGameBean(
    val id: Int = 0,
    val firmId: Int = 0,
    val firmType: String?,
    val firmCode: String? = null,
    val firmName: String? = null,
    val gameCode: String?,
    val gameName: String? = null,
    val gameType: String? = null,
    val imgGame: String? = null,
    val gameEntryTagName: String? = null,
    val thirdGameCategory: String? = null,
    var markCollect: Boolean = false,
    var gameEntryType: String?,
    var maintain: Int? = 0, // 0:游戏正常开启, 1: 维护状态
    val jackpotAmount:Double = 0.0, //0不显示1显示
    val jackpotOpen:Int = 0,
    val extraParam: String? = null,
    var imgBigGame: String? = null
): Parcelable,BaseNode() {
    // 列表的父节点
    @Transient
    lateinit var parentNode: BaseNode
    override val childNode: MutableList<BaseNode> = mutableListOf()
    var categoryId = -1
    fun isMaintain() = maintain == 1
    var isShowMore = false
}
