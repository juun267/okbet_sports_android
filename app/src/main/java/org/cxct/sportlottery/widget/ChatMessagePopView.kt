package org.cxct.sportlottery.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewChatMessagePopBinding

/**
 * @author Kevin
 * @create 2023/3/21
 * @description
 */
class ChatMessagePopView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var binding: ViewChatMessagePopBinding

    var borderDrawable: Drawable? = null
        set(value) {
            field = value
            field?.let {
                setupBorderDrawable(it)
            }
        }

    var fillDrawable: Drawable? = null
        set(value) {
            field = value
            field?.let {
                setupFillDrawable(it)
            }
        }

    var borderTintColor: Int? = null
        set(value) {
            field = value
            field?.let {
                setupBorderTintColor(it)
            }
        }

    var fillTintColor: Int? = null
        set(value) {
            field = value
            field?.let {
                setupFillTintColor(it)
            }
        }

    init {
        if (!isInEditMode) init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChatMessagePopView)
        borderDrawable =
            typedArray.getDrawable(R.styleable.ChatMessagePopView_cmp_border_src)
                ?: context.theme.getDrawable(0)
        fillDrawable =
            typedArray.getDrawable(R.styleable.ChatMessagePopView_cmp_fill_src)
                ?: context.theme.getDrawable(0)
        borderTintColor =
            typedArray.getColor(R.styleable.ChatMessagePopView_cmp_border_tint, 0)
        fillTintColor =
            typedArray.getColor(R.styleable.ChatMessagePopView_cmp_fill_tint, 0)
        try {
            binding = ViewChatMessagePopBinding.inflate(LayoutInflater.from(context))
            binding.flBorder.apply {
                background = borderDrawable
                backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, borderTintColor ?: 0))
            }
            binding.tvMessage.apply {
                background = fillDrawable
                backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, fillTintColor ?: 0))
            }
            addView(binding.root)
        } catch (e: Exception) {
            typedArray.recycle()
        }
    }

    private fun setupBorderDrawable(drawable: Drawable) {
        binding.flBorder.background = drawable
    }

    private fun setupFillDrawable(drawable: Drawable) {
        binding.tvMessage.background = drawable
    }

    private fun setupBorderTintColor(color: Int) {
        binding.flBorder.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, color))
    }

    private fun setupFillTintColor(color: Int) {
        binding.tvMessage.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, color))
    }

}