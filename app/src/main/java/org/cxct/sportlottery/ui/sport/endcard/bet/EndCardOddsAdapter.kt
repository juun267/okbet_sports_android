package org.cxct.sportlottery.ui.sport.endcard.bet

import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.isVisible

class EndCardOddsAdapter(private val itemClick: (String) -> Boolean)
    : BaseQuickAdapter<String, BaseViewHolder>(0) {

    private lateinit var endCardBet: EndCardBet

    private val defaultBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_071724))
            .setRadius(8.dp.toFloat())
            .setWidth(88.dp)
            .setHeight(44.dp)
    }

    private val disableBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_142635))
            .setRadius(8.dp.toFloat())
            .setWidth(88.dp)
            .setHeight(44.dp)
    }

    private val selectedBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_1475E1))
            .setRadius(8.dp.toFloat())
            .setWidth(88.dp)
            .setHeight(44.dp)
    }
    private val beforeBetBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_071724))
            .setStrokeColor(context.getColor(R.color.color_52718B))
            .setStrokeSize(1.dp)
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
        odd.typeface = AppFont.helvetica
        odd.setTextColor(Color.WHITE)
        odd.gravity = Gravity.CENTER
        root.addView(odd, lp)

        val user = AppCompatTextView(cxt)
        user.id = userId
        user.textSize = 12f
        user.setTextColor(cxt.getColor(R.color.color_FFFFFF))
        user.gravity = Gravity.CENTER
        user.maxLines = 1
        user.ellipsize = TextUtils.TruncateAt.END
        root.addView(user, lp)

        return BaseViewHolder(root)
    }

    override fun convert(holder: BaseViewHolder, item: String) {

        val oddText = holder.getView<TextView>(oddId)
        val userText = holder.getView<TextView>(userId)

        oddText.text = item

        val userName = endCardBet.lastBetName?.get(item)
        val noUserBet = userName.isEmptyStr()
        userText.text = userName

        val itemView = holder.itemView
        val betted = endCardBet.betMyself?.contains(item) == true

        when {
            betted -> {
                userText.text = UserInfoRepository.nickName()
                oddText.setTextColor(context.getColor(R.color.color_1CD219))
                oddText.paint.isFakeBoldText=true
                userText.setTextColor(context.getColor(R.color.color_FFFFFF))
                itemView.background = beforeBetBg
            }
            !noUserBet -> {
                oddText.setTextColor(context.getColor(R.color.color_728B9D))
                userText.setTextColor(context.getColor(R.color.color_728B9D))
                itemView.background = disableBg
            }
            EndCardBetManager.containOdd(item) -> itemView.background = selectedBg
            else -> itemView.background = defaultBg
        }

        userText.isVisible = !userText.text.isEmpty()
        itemView.isEnabled = !betted && noUserBet
        itemView.setOnClickListener {

            loginedRun(it.context) {
                if (itemClick.invoke(item)) {
                    if (EndCardBetManager.containOdd(item)) {
                        itemView.background = selectedBg
                    } else {
                        itemView.background = defaultBg
                    }
                }
            }
        }
    }

    fun setUpData(bet: EndCardBet) {
        if (::endCardBet.isInitialized && endCardBet === bet) {
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