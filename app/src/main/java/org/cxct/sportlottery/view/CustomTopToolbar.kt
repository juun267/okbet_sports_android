package org.cxct.sportlottery.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewBaseToolBarNoDrawerBinding
import org.cxct.sportlottery.util.setTitleLetterSpacing
import splitties.systemservices.layoutInflater

/**
 * 客製化 TabLayout
 */
class CustomTopToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    val binding by lazy { ViewBaseToolBarNoDrawerBinding.inflate(layoutInflater,this,true) }

    init {
        initView(context.theme.obtainStyledAttributes( attrs, R.styleable.CustomTopToolbar,0, 0), binding.root)
    }

    var titleText: String? = null
        set(value) {
            binding.tvToolbarTitle.setTitleLetterSpacing()
            field = value
            binding.tvToolbarTitle.text = value
        }

    fun setOnBackPressListener(listener: () -> Unit) {
        binding.btnToolbarBack.setOnClickListener { listener.invoke() }
    }

    private fun initView(typedArray: TypedArray, view: View) {
        view.apply {
            binding.tvToolbarTitle.setTitleLetterSpacing()
            binding.tvToolbarTitle.text = typedArray.getString(R.styleable.CustomTopToolbar_topTitleText) ?:""
        }

        typedArray.recycle()
    }

}