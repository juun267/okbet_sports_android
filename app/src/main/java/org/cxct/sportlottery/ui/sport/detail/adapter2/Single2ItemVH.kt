package org.cxct.sportlottery.ui.sport.detail.adapter2

import android.content.Context
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import splitties.systemservices.layoutInflater
import org.cxct.sportlottery.databinding.ContentOddsDetailListSingle2ItemBinding as Single2Binding


class Single2ItemVH(parent: ViewGroup,
                    private val context: Context = parent.context,
                    private val binding: Single2Binding = Single2Binding.inflate(context.layoutInflater, parent, false))
    : BaseViewHolder(binding.root) {


}