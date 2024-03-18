package org.cxct.sportlottery.ui.sport.endcard.bet

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.loginedRun

class EndCardOddsAdapter(private val itemClick: (String) -> Boolean)
    : BaseQuickAdapter<String, BaseViewHolder>(0) {

    private lateinit var endCardBet: EndCardBet

    private val defaultBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_1E2535))
            .setRadius(8.dp.toFloat())
            .setWidth(88.dp)
            .setHeight(44.dp)
    }

    private val disableBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_353B4E))
            .setRadius(8.dp.toFloat())
            .setWidth(88.dp)
            .setHeight(44.dp)
    }

    private val selectedBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_025BE8))
            .setRadius(8.dp.toFloat())
            .setWidth(88.dp)
            .setHeight(44.dp)
    }

    private val oddId = View.generateViewId()
    private val userId = View.generateViewId()
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val cxt = parent.context
        val root = LinearLayout(cxt)
        root.gravity = Gravity.CENTER
        root.orientation = LinearLayout.VERTICAL
        val dp3 = 3.dp
        root.setPadding(dp3, dp3, dp3, dp3)
        root.layoutParams = LinearLayout.LayoutParams(-1, 44.dp)
        val lp = LinearLayout.LayoutParams(-2, 0, 1f)

        val odd = AppCompatTextView(cxt)
        odd.id = oddId
        odd.textSize = 16f
        odd.typeface = AppFont.inter_bold
        odd.setTextColor(Color.WHITE)
        odd.gravity = Gravity.CENTER
        root.addView(odd, lp)

        val user = AppCompatTextView(cxt)
        user.id = userId
        user.textSize = 12f
        user.setTextColor(cxt.getColor(R.color.color_BEC7DC))
        user.gravity = Gravity.CENTER
        root.addView(user, lp)

        return BaseViewHolder(root)
    }

    override fun convert(holder: BaseViewHolder, item: String) {

        val oddText = holder.getView<TextView>(oddId)
        val userText = holder.getView<TextView>(userId)

        oddText.text = item

        val userName = endCardBet.lastBetName?.get(item)
        val noUserBet = userName.isEmptyStr()
        if (noUserBet) {
            userText.gone()
        } else {
            userText.text = userName
            userText.visible()
        }

        val itemView = holder.itemView
        val betted = endCardBet.betMyself?.contains(item) == true
        if (betted) {
            itemView.setBackgroundResource(R.drawable.bg_selected_endodd)
        } else if (!noUserBet) {
            itemView.background = disableBg
        } else {
            itemView.background = defaultBg
        }

        itemView.isEnabled = !betted && noUserBet
        itemView.setOnClickListener {

            loginedRun(it.context) {
                if (itemClick.invoke(item)) {
                    if (EndCardBetManager.containOdd(item)) {
                        holder.itemView.background = selectedBg
                    } else {
                        holder.itemView.background = defaultBg
                    }
                }
            }
        }
    }

    fun setUpData(bet: EndCardBet) {
        if (::endCardBet.isInitialized && endCardBet == bet) {
            return
        }

        this.endCardBet = bet
        if (itemCount > 0) {
            notifyDataSetChanged()
            return
        }

        val list = mutableListOf<String>()
        repeat(10) { home->
            repeat(10) { away->
                list.add("$home-$away")
            }
        }

        setNewInstance(list)
    }
}