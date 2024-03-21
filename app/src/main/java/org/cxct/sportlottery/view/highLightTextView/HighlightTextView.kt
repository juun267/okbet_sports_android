package org.cxct.sportlottery.view.highLightTextView

import android.content.Context
import android.graphics.*
import android.text.*
import android.text.style.*
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R

class HighlightTextView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attributeSet, defStyleAttr) {

    private val isHighlighting
        get() = targetText.isNotEmpty()

    private val mPaint = Paint()

    private var mRectF: List<RectF> = emptyList()

    private var targetText: List<String> = emptyList()

    private var highLightColor = ContextCompat.getColor(context, R.color.color_317FFF_0760D4)
    private val highlightColorSpan: BackgroundColorSpan by lazy {
        BackgroundColorSpan(highLightColor)
    }

    private var highlightWidth = NO_STROKE_WIDTH

    private var highlightRadius = 0F

    private var highlightBoldFlag = false

    private var highlightTextColor = ContextCompat.getColor(context, R.color.color_FFFFFF)
    private val highlightTextColorSpan: ForegroundColorSpan by lazy {
        ForegroundColorSpan(highlightTextColor)
    }
    private val stringBuilder: SpannableStringBuilder by lazy {
        SpannableStringBuilder("")
    }
    private val isChangeSelectedTextColor: Boolean
        get() = highlightTextColor != currentTextColor

    private val highlightTextStyles: List<Any>
        get() = mutableListOf<Any>().apply {
            if (highlightBoldFlag) {
                add(StyleSpan(Typeface.BOLD))
            }

            if (isChangeSelectedTextColor) {
                add(ForegroundColorSpan(highlightTextColor))
            }
        }

    private var divider = ""
    private var textWithoutHighlight: SpannableString = SpannableString("")

    fun highlight(text: String) {
        highlight(listOf(text))
    }

    fun highlight(texts: List<String>) {
        targetText = texts
        resetHighlight()
    }

    fun setStrokeWidth(width: Float) {
        highlightWidth = width
        resetHighlight()
    }

    fun setHighlightBold(bold: Boolean) {
        highlightBoldFlag = bold
        resetHighlight()
    }

    fun setColor(@ColorRes color: Int) {
        highLightColor = ContextCompat.getColor(context, color)

        mPaint.color = highLightColor
        invalidate()
    }

    fun setRadius(dp: Float) {
        highlightRadius = dp
        invalidate()
    }

    init {
        setupAttributes(context, attributeSet, defStyleAttr)

        with(mPaint) {
            this.style = Paint.Style.FILL
            this.color = highLightColor
        }
    }

    private fun setupAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HighlightTextView, defStyleAttr, 0)
        highLightColor = typedArray.getInt(R.styleable.HighlightTextView_highlightColor, highLightColor)
        highlightWidth = typedArray.getDimension(R.styleable.HighlightTextView_highlightWidth, NO_STROKE_WIDTH)
        highlightRadius = typedArray.getDimension(R.styleable.HighlightTextView_highlightRadius, highlightRadius)
        highlightBoldFlag = typedArray.getBoolean(R.styleable.HighlightTextView_highlightBold, highlightBoldFlag)
        highlightTextColor = typedArray.getInt(R.styleable.HighlightTextView_highlightTextColor, highlightTextColor)

        val inputTargets = typedArray.getString(R.styleable.HighlightTextView_highlightText)
        targetText = inputTargets?.split(",") ?: emptyList()

        typedArray.recycle()
        resetHighlight()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        resetHighlight()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun resetHighlight() {
        stringBuilder.clear()
        stringBuilder.append(textWithoutHighlight)
        stringBuilder.append(divider)
        val spannableText = SpannableString(text)
        getSortedTargetTexts(spannableText, targetText).forEach {
            val start = it.first
            val end = it.first + it.second

            spannableText.setSpan(
                highlightColorSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableText.setSpan(
                highlightTextColorSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        stringBuilder.append(spannableText)
        text = stringBuilder
    }

    private fun measureTargetTextRect(target: String): RectF? {
        val bounds = Rect()
        val startIndex = text.indexOf(target, textWithoutHighlight.length + divider.length, true)
        if (startIndex == -1) {
            return null
        }

        val targetText = text.subSequence(startIndex, startIndex + target.length)
        paint.getTextBounds(text.toString(), startIndex, startIndex + target.length, bounds)

        if (highlightWidth == NO_STROKE_WIDTH) {
            highlightWidth = bounds.height().toFloat()
        }

        val preWord = text.substring(0, startIndex)
        val preWidth = measureTextWidth(preWord).toInt()
        val targetWidth = measureTextWidth(targetText.toString(), highlightBoldFlag)

        // TODO: support multiline.
        //  val positionOfLine = measureLineOfText(startIndex)
        val positionOfLine = 1
        val heightUntilUnderline = positionOfLine * lineHeight

        // TODO: check gravity

        return RectF(
            preWidth.toFloat() + paddingStart,
            heightUntilUnderline - (highlightWidth * (positionOfLine - 1)) + 20,
            preWidth + targetWidth + paddingStart,
            heightUntilUnderline - (highlightWidth * positionOfLine) - 30
        )
    }

    private fun measureTextWidth(text: String, bold: Boolean = false): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = this@HighlightTextView.textSize
            typeface = if (bold) Typeface.DEFAULT_BOLD else this@HighlightTextView.typeface
        }

        return paint.measureText(text)
    }

    private fun measureLineOfText(targetIndex: Int): Int {
        val texts = text.split(' ')
        val builder = StringBuilder()
        for (text in texts) {
            builder.append("$text ")

            if (builder.length > targetIndex) {
                return ((measureTextWidth(builder.toString()) / width).toInt() + 1)
            }
        }

        // Not found.
        return 0
    }

    private fun getSortedTargetTexts(spannableText: SpannableString, targets: List<String>) = targets
        .filter {
            spannableText.indexOf(it, 0, true) != -1
        }.map {
            val length = it.length
            val position = spannableText.indexOf(it, 0, true)

            position to length
        }.sortedBy { it.second }

    fun setCustomText(text: CharSequence?) {
        this.text = text
    }

    fun setSpannableTextWithoutHighlight(text: SpannableString) {
        this.textWithoutHighlight = text
    }

    fun needDivider(needDivider: Boolean) {
        if (needDivider) divider = "  |  "
    }

    companion object {
        const val NO_STROKE_WIDTH = -1F
    }
}