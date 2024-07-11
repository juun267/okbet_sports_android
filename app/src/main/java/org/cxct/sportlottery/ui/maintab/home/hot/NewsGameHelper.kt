package org.cxct.sportlottery.ui.maintab.home.hot

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.ViewHomeNewgamesBinding
import splitties.systemservices.layoutInflater

class NewsGameHelper(private val parent: ViewGroup, private val index: Int, private val context: Context = parent.context) {

    private val gamesBinding by lazy {
        val binding = ViewHomeNewgamesBinding.inflate(parent.layoutInflater, parent, false)
        parent.addView(binding.root, index)
        binding
    }


    private fun initRecyclerView() {
        gamesBinding.rcv.setLinearLayoutManager(RecyclerView.HORIZONTAL)
    }





}