package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.KvUtils

class DetailSportGuideView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : FrameLayout(context, attrs, defStyle) {

    companion object {

        //是否已经展示过新手引导
        const val BASKETBALL_GUIDE_TIP_FLAG = "basketball_guide_tip_flag"

        fun isPlayed(): Boolean {
            return KvUtils.decodeBooleanTure(BASKETBALL_GUIDE_TIP_FLAG, false)
        }
    }

    private val ivBg: ImageView
    private val dsgt: DetailSportGuideTipsView
    private var curIndex = 0
    private val dsgtLayoutParams: LayoutParams
    private var dsgtImgList =
        mutableListOf(
            Pair(-1, R.drawable.bg_sports_detail_tips_01),
            Pair(313.dp, R.drawable.bg_sports_detail_tips_02),
            Pair(156.dp, R.drawable.bg_sports_detail_tips_03),
            Pair(156.dp, R.drawable.bg_sports_detail_tips_04),
            Pair(240.dp, R.drawable.bg_sports_detail_tips_05),
            Pair(240.dp, R.drawable.bg_sports_detail_tips_06),
            Pair(273.dp, R.drawable.bg_sports_detail_tips_07),
            Pair(240.dp, R.drawable.bg_sports_detail_tips_08),
        )

    private val dsgtParams = mutableListOf(
        Triple(context.getString(R.string.P001), context.getString(R.string.P002), "1/8"),
        Triple("", context.getString(R.string.P004), "2/8"),
        Triple("", resources.getString(R.string.P006) + "\n" + resources.getString(R.string.P007), "3/8"),
        Triple("", context.getString(R.string.P016), "4/8"),
        Triple("", context.getString(R.string.P008), "5/8"),
        Triple("", context.getString(R.string.P009), "6/8"),
        Triple("", context.getString(R.string.P010), "7/8"),
        Triple("", context.getString(R.string.P011), "8/8")
    )

    init {
        ivBg = AppCompatImageView(context)
        ivBg.scaleType = ImageView.ScaleType.MATRIX
        ivBg.setBackgroundResource(R.color.transparent_black_40)
        addView(ivBg, LayoutParams(-1, -1))

        dsgt = DetailSportGuideTipsView(context, null)
        dsgtLayoutParams = LayoutParams(-1, -2).apply { gravity = Gravity.CENTER }
        addView(dsgt, dsgtLayoutParams)
        onRebind()

        var dsListener = object : DetailSportGuideTipsView.OnDSGTipsClickListener {
            override fun onPreviousClick() {
                if (curIndex > 0) {
                    curIndex--
                    onRebind()
                }
            }

            override fun onNextClick() {
                if (curIndex < dsgtParams.size - 1) {
                    curIndex++
                    onRebind()
                } else {
                    visibility = GONE
                    KvUtils.put(BASKETBALL_GUIDE_TIP_FLAG, true)
                }
            }

            override fun onCloseClick() {
                visibility = GONE
                KvUtils.put(BASKETBALL_GUIDE_TIP_FLAG, true)
            }

        }

        dsgt.setOnPreviousOrNextClickListener(dsListener)
    }

    private fun onRebind() {
        val imgParams = dsgtImgList[curIndex]
        ivBg.setImageResource(imgParams.second)
        val contentText = dsgtParams[curIndex]
        dsgt.setTitle(contentText.first)
        dsgt.setContent(contentText.second)
        dsgt.setIndexText(contentText.third)
        dsgt.setPreviousEnable(curIndex != 0)
        if (curIndex < dsgtParams.size - 1) {
            dsgt.setNextText()
        } else {
            dsgt.setNextBetText()
        }
        val topMargin = imgParams.first
        if (topMargin > 0) {
            dsgtLayoutParams.gravity = Gravity.TOP
            dsgtLayoutParams.topMargin = topMargin
        } else {
            dsgtLayoutParams.gravity = Gravity.CENTER
            dsgtLayoutParams.topMargin = 0
        }
        dsgt.layoutParams = dsgtLayoutParams
    }

}