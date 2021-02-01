package org.cxct.sportlottery.util

import androidx.databinding.BindingAdapter
import net.cachapa.expandablelayout.ExpandableLayout

object ExpandableLayoutBindingAdapter {
    @JvmStatic
    @BindingAdapter("setExpanded")
    fun ExpandableLayout.expanded(expanded: Boolean) {
        setExpanded(expanded, true)
    }
}