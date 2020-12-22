package org.cxct.sportlottery.util

import androidx.databinding.BindingAdapter
import org.cxct.sportlottery.ui.home.HomeGameCard

/**
 * 自定義物件的屬性，要使用 data binding 給值，要在此添加方法
 * https://stackoverflow.com/questions/62894573/custom-attribute-setter-error-when-data-binding-android
 * create by Simon Chang
 */
object BindingUtil {
    @JvmStatic
    @BindingAdapter("hgCard_count")
    fun setTitle(view: HomeGameCard, title: String?) {
        view.setTitle(title)
    }

    @JvmStatic
    @BindingAdapter("hgCard_count")
    fun setCount(view: HomeGameCard, count: String?) {
        view.setCount(count)
    }
}