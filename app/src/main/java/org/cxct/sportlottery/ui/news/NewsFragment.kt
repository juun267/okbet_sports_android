package org.cxct.sportlottery.ui.news

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentNewsBinding
import org.cxct.sportlottery.ui.base.BaseFragment

class NewsFragment : BaseFragment<NewsViewModel>(NewsViewModel::class) {
    private var _binding: FragmentNewsBinding? = null

    private val binding get() = _binding!!

    private val tabLayoutSelectedListener by lazy {
        object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabCustomView = tab?.customView
                tabCustomView?.let {
                    val tabTextView: TextView = it as TextView
                    with(tabTextView) {
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(ContextCompat.getColor(context, R.color.colorBlackLight))
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                val tabCustomView = tab?.customView
                tabCustomView?.let {
                    val tabTextView: TextView = it as TextView
                    with(tabTextView) {
                        setTypeface(null, Typeface.NORMAL)
                        setTextColor(ContextCompat.getColor(context, R.color.colorGray))
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        initTabLayout()
    }

    private fun initTabLayout() {
        with(binding.tabLayout) {
            //region 設置TabCustomView 為了將選中Tab文字粗體
            for (i in 0..tabCount) {
                val tab = getTabAt(i)
                if (tab != null) {
                    val tabTextView = TextView(context)
                    tab.customView = tabTextView

                    with(tabTextView.layoutParams) {
                        width = ViewGroup.LayoutParams.WRAP_CONTENT
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }

                    tabTextView.text = tab.text

                    //預設第一個為選中狀態
                    if (i == 0)
                        tabTextView.setTypeface(null, Typeface.BOLD)
                }
            }
            //endregion

            addOnTabSelectedListener(tabLayoutSelectedListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.tabLayout.removeOnTabSelectedListener(tabLayoutSelectedListener)
        _binding = null
    }
}