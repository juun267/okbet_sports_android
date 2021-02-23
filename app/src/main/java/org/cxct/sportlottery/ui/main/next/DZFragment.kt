package org.cxct.sportlottery.ui.main.next

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.content_dz_adapter.view.*
import kotlinx.android.synthetic.main.fragment_dz.tab_layout
import kotlinx.android.synthetic.main.fragment_dz.view_pager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainViewModel

class DZFragment : BaseFragment<MainViewModel>(MainViewModel::class)  {

    private val tabIconList by lazy {
        listOf(R.drawable.ic_image_load, R.drawable.ic_cp, R.drawable.ic_live)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dz, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view_pager.adapter = DzPageAdapter(tabIconList)

        tab_layout.clearOnTabSelectedListeners()
        view_pager.clearOnPageChangeListeners()
        tab_layout.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(view_pager) {})
        view_pager.addOnPageChangeListener(object : TabLayout.TabLayoutOnPageChangeListener(tab_layout) {})

    }

}

class DzPageAdapter(private val tabPageDataList: List<Int>): PagerAdapter() {

    private val testDataList = listOf(R.string.sport, R.string.cp, R.string.live, R.string.qp, R.string.dz, R.string.by)
    private val dzSubRvAdapter = DzSubRvAdapter()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val rootView = LayoutInflater.from(container.context).inflate(R.layout.content_dz_adapter, container, false)
        rootView.apply {
            try {
                rv_dz_sub.adapter = dzSubRvAdapter
                dzSubRvAdapter.dataList = testDataList
                container.addView(rootView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return rootView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        //強迫調用 notifyDataSetChanged() 後，每次都重新加載 data //會比較耗資源
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return tabPageDataList.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return obj == view
    }

}

