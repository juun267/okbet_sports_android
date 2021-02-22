package org.cxct.sportlottery.ui.home.news

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import kotlinx.android.synthetic.main.dialog_event_msg.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.home.MainViewModel

class NewsDiaolog(context: Context) : BaseDialog<MainViewModel>(MainViewModel::class) {

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_event_msg, container, false).apply {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_tab.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)//LinearLayout
        rv_tab.adapter = NewsAdapter(context)
        var mPagerSnapHelper = PagerSnapHelper()
        mPagerSnapHelper.attachToRecyclerView(rv_tab)
    }
}