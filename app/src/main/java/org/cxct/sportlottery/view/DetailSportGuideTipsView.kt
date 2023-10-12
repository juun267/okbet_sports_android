package org.cxct.sportlottery.view

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import org.cxct.sportlottery.R

class DetailSportGuideTipsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle) {

    interface OnDSGTipsClickListener {
        fun onPreviousClick()
        fun onNextClick()
        fun onCloseClick()
    }

    private lateinit var tvTitleIndex: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var tvPrevious: TextView
    private lateinit var tvNext: TextView
    private lateinit var ivTipsClose: ImageView


    init {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.detail_sport_guide_tips, this)
        tvTitleIndex = findViewById(R.id.tv_tips_index)
        tvTitle = findViewById(R.id.tv_title)
        tvContent = findViewById(R.id.tv_content)
        tvPrevious = findViewById(R.id.tv_previous)
        tvNext = findViewById(R.id.tv_next)
        ivTipsClose = findViewById(R.id.iv_tips_close)

    }


    fun setIndexText(indexText: String) {
        var sbs = SpannableString(indexText)
        sbs.setSpan(
            ForegroundColorSpan(Color.parseColor("#025BE8")),
            0,
            1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvTitleIndex.text = sbs
    }

    fun setNextText() {
        tvNext.setText(R.string.P013)
    }

    fun setNextBetText() {
        tvNext.setText(R.string.N102)
    }

    fun setTitle(title: String) {
        tvTitle.text = title
        tvTitle.isVisible = title.isNotEmpty()
    }

    fun setContent(content: String) {
        tvContent.text = content
    }

    fun setPreviousEnable(flag: Boolean) {
        tvPrevious.isEnabled = flag
        tvPrevious.alpha = 0.4f
    }

    fun setOnPreviousOrNextClickListener(listener: OnDSGTipsClickListener) {
        tvPrevious.setOnClickListener {
            listener.onPreviousClick()
        }
        tvNext.setOnClickListener {
            listener.onNextClick()
        }
        ivTipsClose.setOnClickListener {
            listener.onCloseClick()
        }
    }

}