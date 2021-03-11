package org.cxct.sportlottery.ui.main.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.util.TimeUtil

class NewsViewPagerAdapter(private val mActivity: FragmentActivity?) : PagerAdapter() {

    var data = mutableListOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount() = data.size

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view = LayoutInflater.from(mActivity)
            .inflate(R.layout.content_msg_dialog_viewpager, container, false)

        try {
            val arrowLeft: ImageView = view.findViewById(R.id.img_arrow_left)
            val arrowRight: ImageView = view.findViewById(R.id.img_arrow_right)
            val txvTitle: TextView = view.findViewById(R.id.txv_title)
            val txvContent: TextView = view.findViewById(R.id.txv_content)
            val txvTime: TextView = view.findViewById(R.id.txv_time)

            txvTitle.text = data[position].title
            txvContent.text = data[position].message
            txvTime.text = TimeUtil.stampToDateHMS(data[position].addTime.toLong())

            when (position) {
                0 -> {
                    arrowLeft.visibility = View.INVISIBLE
                    arrowRight.visibility = View.VISIBLE
                }
                data.size - 1 -> {
                    arrowLeft.visibility = View.VISIBLE
                    arrowRight.visibility = View.INVISIBLE
                }
                else -> {
                    arrowLeft.visibility = View.VISIBLE
                    arrowRight.visibility = View.VISIBLE
                }
            }
            if (data.size < 2) {
                arrowLeft.visibility = View.INVISIBLE
                arrowRight.visibility = View.INVISIBLE
            }
            container.addView(view)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}