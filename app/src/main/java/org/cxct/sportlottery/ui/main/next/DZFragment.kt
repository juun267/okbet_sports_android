package org.cxct.sportlottery.ui.main.next

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.content_dz_adapter.view.*
import kotlinx.android.synthetic.main.fragment_dz.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainViewModel
import kotlin.math.abs

class DZFragment : BaseFragment<MainViewModel>(MainViewModel::class)  {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dz, container, false)
    }

    private val tabIconList by lazy {
        listOf(R.drawable.ic_image_load, R.drawable.ic_cp, R.drawable.ic_live, R.drawable.ic_qp, R.drawable.ic_dz, R.drawable.ic_by)
    }
    private var x1 = 0f
    private var x2 = 0f
    private val MIN_DISTANCE = 150

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view_pager.adapter = DzPageAdapter(tabIconList)

        view_pager.adapter = DzVpAdapter(tabIconList)

        view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setSwipeable((view_pager.currentItem == 0) || (view_pager.currentItem == tabIconList.size - 1))
            }
        })
/*

        view_pager.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x1 = event.x
                }
                MotionEvent.ACTION_UP -> {
                    x2 = event.x
                    val deltaX = x2 - x1
                    if (abs(deltaX) > MIN_DISTANCE) {
                        Toast.makeText(context, "left2right swipe", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "test swipe", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            v.onTouchEvent(event)
        }
*/



        // attaching tab mediator
        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.setCustomView(R.layout.tab_item_game2)
            tab.customView?.apply {
                findViewById<ImageView>(R.id.iv_icon)?.setImageResource(tabIconList[position])
            }
        }.attach()

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


class DzVpAdapter(private val tabPageDataList: List<Int>): RecyclerView.Adapter<DzVpAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_dz_adapter, viewGroup, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return tabPageDataList.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val testDataList = listOf(R.string.sport, R.string.cp, R.string.live, R.string.qp, R.string.dz, R.string.by)
        private val dzSubRvAdapter = DzSubRvAdapter()

        fun bind() {
            itemView.apply {
                rv_dz_sub.adapter = dzSubRvAdapter
                dzSubRvAdapter.dataList = testDataList
            }
        }

    }

}
