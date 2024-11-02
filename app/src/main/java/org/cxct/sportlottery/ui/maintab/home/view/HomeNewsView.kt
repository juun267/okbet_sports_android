package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.spannable.addSpan
import com.drake.spannable.span.CenterImageSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.TabHomeNewsBinding
import org.cxct.sportlottery.databinding.ViewHomeNewsBinding
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.ui.maintab.home.news.NewsDetailActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.util.isHalloweenStyle
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter
import splitties.systemservices.layoutInflater

class HomeNewsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewHomeNewsBinding.inflate(layoutInflater,this)
    lateinit var viewModel:MainHomeViewModel
    val pageSize = 3
    private val homeHotNewsAdapter = HomeHotNewsAdapter().apply {
        setOnItemClickListener{ adapter, view, position ->
            NewsDetailActivity.start(
                context,
                (adapter.data[position] as NewsItem)
            )
        }
    }
    init {
        orientation = VERTICAL
        initView()
    }

    private fun initView() =binding.run {
        tabNews.addOnTabSelectedListener(TabSelectedAdapter { tab, _ ->
            getSelectCategoryId()?.let {
                viewModel.getHomeNews(1, pageSize, listOf(it))
            }
        })
        binding.rvNews.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvNews.adapter = homeHotNewsAdapter
    }
    fun setup(fragment: HomeHotFragment) {
        viewModel = fragment.viewModel
        viewModel.newsCategory.observe(fragment) {
            binding.tabNews.removeAllTabs()
            binding.root.isVisible = it.isNotEmpty()
            it.forEach {
                val itemBinding = TabHomeNewsBinding.inflate(layoutInflater)
                if (isHalloweenStyle()) {
                    itemBinding.tvTitle.background = createTabBackground()
                    itemBinding.tvTitle.setTextColor(Color.WHITE)
                }

                binding.tabNews.addTab(binding.tabNews.newTab().setTag(it.id).setCustomView(itemBinding.root))
                itemBinding.tvTitle.text = it.categoryName
            }
            binding.tabNews.getTabAt(0)?.select()
            getSelectCategoryId()?.let {
                viewModel.getHomeNews(1, pageSize, listOf(it))
            }
        }
        viewModel.homeNewsList.observe(fragment) {
            val dataList = if (it.size > pageSize) it.subList(0, pageSize) else it
            homeHotNewsAdapter.setList(dataList)
        }
        binding.tvMore.setOnClickListener {
            (fragment.activity as MainTabActivity).jumpToNews()
        }
        viewModel.getNewsCategory()
    }
    private fun getSelectCategoryId():Int?=binding.tabNews.getTabAt(binding.tabNews.selectedTabPosition)?.tag as? Int

    fun applyHalloweenStyle() = binding.run {

        val imageView = AppCompatImageView(context)
        imageView.setImageResource(R.drawable.ic_halloween_logo_5)
        val dp24 = 24.dp
        val lp = LayoutParams(dp24, dp24)
        lp.gravity = Gravity.CENTER_VERTICAL
        (tvCateName.parent as ViewGroup).addView(imageView, 0, lp)

        val frameLayout = FrameLayout(context)
        val ivMonster = AppCompatImageView(context)
        ivMonster.setBackgroundResource(R.drawable.img_monster_2_h)
        val monsterLP = FrameLayout.LayoutParams(134.dp, 60.dp)
        monsterLP.gravity = Gravity.RIGHT
        frameLayout.addView(ivMonster, monsterLP)

        val tabLayout = tvCateName.parent as View
        val lp1 = FrameLayout.LayoutParams(-1, -2)
        lp1.topMargin = 15.dp
        removeView(tabLayout)
        frameLayout.addView(tabLayout, lp1)

        val contentLayout = linTab.parent as View
        val lp2 = FrameLayout.LayoutParams(-1, -2)
        lp2.topMargin = 50.dp
        removeView(contentLayout)
        frameLayout.addView(contentLayout, lp2)

        addView(frameLayout)

        (layoutParams as MarginLayoutParams).bottomMargin = 0
        linTab.setBackgroundResource(R.drawable.bg_home_new_title_h)
        tvMore.setPadding(8.dp, 0, 5.dp, 0)
        tvMore.text = context.getString(R.string.N702)
            .addSpan("AAA", CenterImageSpan(context, R.drawable.ic_to_right_withe).setDrawableSize(13.dp).setMarginHorizontal(2.dp))
        tvMore.setTextColor(Color.WHITE)
        tvMore.setBackgroundResource(R.drawable.ic_more_but_bg)
        tvMore.compoundDrawablePadding = 0
        tvMore.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)

    }

    private fun createTabBackground(): Drawable {
        val radius = 24.dp.toFloat()
        return DrawableCreator.Builder()
            .setSelectedDrawable(DrawableCreator.Builder()
                .setCornersRadius(radius)
                .setGradientColor(Color.parseColor("#5DCAF9"), context.getColor(R.color.color_025BE8))
                .setGradientCenterXY(0.5f, 0f)
                .setGradientAngle(270)
                .build())
            .setUnSelectedDrawable(
                DrawableCreator.Builder()
                    .setCornersRadius(radius)
                    .setSolidColor(Color.parseColor("#80025BE8"))
                    .build()
            ).build()

    }

}