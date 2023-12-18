package org.cxct.sportlottery.view

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ViewMainMenuItemBinding
import splitties.systemservices.layoutInflater

class MainMenuItemView (context: Context, attrs: AttributeSet) : LinearLayout(context, attrs)  {

    private val binding = ViewMainMenuItemBinding.inflate(layoutInflater,this)

    init {
        orientation= VERTICAL
        foreground = context.getDrawable(R.drawable.fg_ripple)
    }

    fun setItem(selectorDrawable: Drawable, @StringRes nameRes: Int,onClick: (()->Unit)? = null): MainMenuItemView {
        binding.ivIcon.setImageDrawable(selectorDrawable)
        binding.tvName.setText(nameRes)
        onClick?.let { setOnClickListener { onClick.invoke() } }
        return this
    }

    fun ivArrow() = binding.ivArrow

    fun hideArrow() {
        binding.ivArrow.gone()
    }

    fun showBottomLine(isVisible: Boolean) {
        binding.bottomLine.isVisible = isVisible
    }

    fun setSummaryStatus(enable: Boolean, text: Int, statusColor: Int) {
        binding.tvSummary.show()
        binding.tvSummary.setText(text)
        binding.tvSummary.setTextColor(statusColor)
        binding.ivArrow.isVisible = enable
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        binding.tvName.typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }
}