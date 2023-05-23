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
import org.cxct.sportlottery.R

class DetailSportGuideTipsView : LinearLayout {

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

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
        if (context != null && attrs != null) {
            initAttrs(context, attrs)
        }
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
        if (context != null && attrs != null) {
            initAttrs(context, attrs)
        }
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context)
        if (context != null && attrs != null) {
            initAttrs(context, attrs)
        }
    }

    fun initView(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.detail_sport_guide_tips, this)
        tvTitleIndex = findViewById<TextView>(R.id.tv_tips_index)
        tvTitle = findViewById<TextView>(R.id.tv_title)
        tvContent = findViewById<TextView>(R.id.tv_content)
        tvPrevious = findViewById<TextView>(R.id.tv_previous)
        tvNext = findViewById<TextView>(R.id.tv_next)
        ivTipsClose = findViewById<ImageView>(R.id.iv_tips_close)

    }

    private fun initAttrs(context: Context, attrs: AttributeSet) {
        val mTypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.DetailSportGuideTipsView)
        val titleIndex = mTypedArray.getString(R.styleable.DetailSportGuideTipsView_tvTitleIndex)
        val title = mTypedArray.getString(R.styleable.DetailSportGuideTipsView_tvTitle)
        val content = mTypedArray.getString(R.styleable.DetailSportGuideTipsView_tvContent)
        val titleVisibility =
            mTypedArray.getInteger(R.styleable.DetailSportGuideTipsView_titleVisibility, 0)
        var sbs = SpannableString(titleIndex)
        sbs.setSpan(
            ForegroundColorSpan(Color.parseColor("#025BE8")),
            0,
            1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvTitleIndex.text = sbs
        tvTitle.text = title
        tvContent.text = content
        tvTitle.visibility = titleVisibility
        mTypedArray.recycle()
    }

    fun setContent(content: String) {
        tvContent.text = content
    }

    fun setNextStepStr(str: String) {
        tvNext.text = str
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