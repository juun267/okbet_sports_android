package org.cxct.sportlottery.ui.game.publicity

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.PublicityMenuItemBinding

class PublicityMenuCard @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    LinearLayout(context, attrs, defStyle) {
    private var _binding: PublicityMenuItemBinding
    val binding get() = _binding

    init {
        _binding = PublicityMenuItemBinding.inflate(LayoutInflater.from(context), this, false)
        addView(binding.root)

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            with(binding) {
                ivName.text = typedArray.getText(R.styleable.CustomView_cvTitle)
                ivIcon.setImageResource(typedArray.getResourceId(R.styleable.CustomView_cvIcon, 0))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    fun setTitle(title: String) {
        binding.ivName.text = title
    }

    fun setIconResource(@DrawableRes drawableRes: Int) {
        binding.ivIcon.setImageResource(drawableRes)
    }
}