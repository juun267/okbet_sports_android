package org.cxct.sportlottery.view.verticalMarquee

import android.text.SpannableString
import android.widget.TextView
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemMarqueeBinding

class VerticalMarqueeAdapter : BindingAdapter<SpannableString,ItemMarqueeBinding>() {

    override fun onBinding(position: Int, binding: ItemMarqueeBinding, item: SpannableString) {
        (binding.root as? TextView)?.text = item
    }

    override fun getDefItemCount() = if (data.isEmpty()) 0 else Int.MAX_VALUE

    override fun getItemViewType(position: Int) = 0

    override fun getItem(position: Int): SpannableString {
        return super.getItem(position % data.size)
    }
}
