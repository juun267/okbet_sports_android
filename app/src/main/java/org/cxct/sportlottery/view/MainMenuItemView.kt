package org.cxct.sportlottery.view

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.view_main_menu_item.view.*
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ViewMainMenuItemBinding
import splitties.systemservices.layoutInflater

class MainMenuItemView (context: Context, attrs: AttributeSet) : LinearLayout(context, attrs)  {

    val binding = ViewMainMenuItemBinding.inflate(layoutInflater,this)

    init {
        orientation= VERTICAL
    }

    fun setItem(selectorDrawable: Drawable, @StringRes nameRes: Int,onClick: (()->Unit)? = null):MainMenuItemView{
        binding.ivIcon.setImageDrawable(selectorDrawable)
        binding.tvName.setText(nameRes)
        binding.linContent.setOnClickListener {
            onClick?.invoke()
        }
        return this
    }
    fun hideArrow(){
        binding.ivArrow.gone()
    }
    fun hideBottomLine(){
        bottomLine.gone()
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        binding.tvName.typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }
}