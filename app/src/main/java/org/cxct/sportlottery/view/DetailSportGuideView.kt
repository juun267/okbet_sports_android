package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.util.KvUtils

class DetailSportGuideView : LinearLayout {

    private lateinit var dsgt1: DetailSportGuideTipsView
    private lateinit var dsgt2: DetailSportGuideTipsView
    private lateinit var dsgt3: DetailSportGuideTipsView
    private lateinit var dsgt4: DetailSportGuideTipsView
    private lateinit var dsgt5: DetailSportGuideTipsView
    private lateinit var dsgt6: DetailSportGuideTipsView
    private lateinit var dsgt7: DetailSportGuideTipsView
    private lateinit var dsgt8: DetailSportGuideTipsView
    private lateinit var ivBg: ImageView
    private var curIndex = 0
    var dsgtImgList =
        mutableListOf(
            R.drawable.bg_sports_detail_tips_01,
            R.drawable.bg_sports_detail_tips_02,
            R.drawable.bg_sports_detail_tips_03,
            R.drawable.bg_sports_detail_tips_04,
            R.drawable.bg_sports_detail_tips_05,
            R.drawable.bg_sports_detail_tips_06,
            R.drawable.bg_sports_detail_tips_07,
            R.drawable.bg_sports_detail_tips_08,
        )

    constructor(context: Context?) : super(context) {
        initView(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context)
    }

    fun initView(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.detail_sport_guide, this)
        ivBg = findViewById(R.id.iv_bg)
        dsgt1 = findViewById(R.id.dsgt1)
        dsgt2 = findViewById(R.id.dsgt2)
        dsgt3 = findViewById(R.id.dsgt3)
        dsgt4 = findViewById(R.id.dsgt4)
        dsgt5 = findViewById(R.id.dsgt5)
        dsgt6 = findViewById(R.id.dsgt6)
        dsgt7 = findViewById(R.id.dsgt7)
        dsgt8 = findViewById(R.id.dsgt8)
        dsgt3.setContent(resources.getString(R.string.P006) + "\n" + resources.getString(R.string.P007))
        var dsgtList = mutableListOf<DetailSportGuideTipsView>()
        dsgtList.add(dsgt1)
        dsgtList.add(dsgt2)
        dsgtList.add(dsgt3)
        dsgtList.add(dsgt4)
        dsgtList.add(dsgt5)
        dsgtList.add(dsgt6)
        dsgtList.add(dsgt7)
        dsgtList.add(dsgt8)

        var dsListener = object :
            DetailSportGuideTipsView.OnDSGTipsClickListener {
            override fun onPreviousClick() {
                if (curIndex > 0) {
                    dsgtList[curIndex].visibility = GONE
                    curIndex--
                    dsgtList[curIndex].visibility = visibility
                    ivBg.setImageResource(dsgtImgList[curIndex])
                }
            }

            override fun onNextClick() {
                if (curIndex < dsgtList.size - 1) {
                    dsgtList[curIndex].visibility = GONE
                    curIndex++
                    dsgtList[curIndex].visibility = visibility
                    ivBg.setImageResource(dsgtImgList[curIndex])
                } else {
                    visibility = GONE
                    KvUtils.put(KvUtils.BASKETBALL_GUIDE_TIP_FLAG, true)
                }
            }

            override fun onCloseClick() {
                visibility = GONE
                KvUtils.put(KvUtils.BASKETBALL_GUIDE_TIP_FLAG, true)
            }

        }
        dsgt1.setOnPreviousOrNextClickListener(dsListener)
        dsgt2.setOnPreviousOrNextClickListener(dsListener)
        dsgt3.setOnPreviousOrNextClickListener(dsListener)
        dsgt4.setOnPreviousOrNextClickListener(dsListener)
        dsgt5.setOnPreviousOrNextClickListener(dsListener)
        dsgt6.setOnPreviousOrNextClickListener(dsListener)
        dsgt7.setOnPreviousOrNextClickListener(dsListener)
        dsgt8.setOnPreviousOrNextClickListener(dsListener)
    }
}