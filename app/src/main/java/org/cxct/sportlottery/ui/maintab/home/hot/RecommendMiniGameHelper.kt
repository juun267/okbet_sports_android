package org.cxct.sportlottery.ui.maintab.home.hot

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.alpahAnimation
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ItemMinigameBinding
import org.cxct.sportlottery.databinding.LayoutRecommendMinigameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeGradientOrientation
import org.cxct.sportlottery.view.OKVideoPlayer
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import splitties.systemservices.layoutInflater
import splitties.views.dsl.core.horizontalMargin
import java.util.Objects

private const val PLAY_TAG = "RecommendMiniGameHelper"
class RecommendMiniGameHelper(private val context: Context,
                              onClick: (OKGameBean) -> Unit,
                              onLayout: (View) -> Unit) {

    private val miniGameAdapter = MiniGameAdapter(onClick)
    private var currentPlayer: OKVideoPlayer? = null
    private val binding by lazy {
        val vb = LayoutRecommendMinigameBinding.inflate(context.layoutInflater)
        val lp = LinearLayout.LayoutParams(-1, 220.dp)
        lp.horizontalMargin = 12.dp
        vb.root.layoutParams = lp
        onLayout.invoke(vb.root)
        initViewPager(vb.vp)
        initTabLayout(vb.tabLayout)
        vb
    }

    private var dataList: List<OKGameBean>? = null

    fun bindLifeEvent(lifecycleOwner: LifecycleOwner) {
        setup(OKGamesViewModel.getActiveMiniGameData())
        OKGamesViewModel.miniGameList.observe(lifecycleOwner) { setup(it.first) }
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onResume(owner: LifecycleOwner) {
                currentPlayer?.startPlayLogic()
            }

            override fun onPause(owner: LifecycleOwner) {
                currentPlayer?.onVideoPause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                currentPlayer?.release()
            }
        })
    }

    private fun initTabLayout(tabLayout: TabLayout) {
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.color_025BE8))
        tabLayout.setSelectedTabIndicatorHeight(2.dp)
        OverScrollDecoratorHelper.setUpOverScroll(tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            private fun changeSelect(tab: TabLayout.Tab, tf: Typeface, textColor: Int) {
                val textView = (tab.tag as Triple<View, ImageView, TextView>).third
                textView.typeface = tf
                textView.setTextColor(textColor)
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                changeSelect(tab, AppFont.inter_bold, ContextCompat.getColor(context, R.color.color_025BE8))
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                changeSelect(tab, AppFont.helvetica, ContextCompat.getColor(context, R.color.color_6D7693))
            }

            override fun onTabReselected(tab: TabLayout.Tab) { }

        })

    }

    private fun initViewPager(viewPager2: ViewPager2) {
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager2.adapter = miniGameAdapter
        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {

                val playPosition = GSYVideoManager.instance().playPosition
                if (playPosition >= 0
                    && GSYVideoManager.instance().playTag == PLAY_TAG
                    && position != playPosition) {
                    GSYVideoManager.releaseAllVideos()
                }
                playPosition(position)
            }

        })
    }

    private fun playPosition(position: Int) {
        val viewHolder = (binding.vp.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position)
        currentPlayer?.onVideoPause()
        if (viewHolder != null) {
            currentPlayer = (viewHolder as BindingVH<ItemMinigameBinding>).vb.videoPlayer
            currentPlayer!!.startPlayLogic()
        }
    }

    fun setup(gameList: List<OKGameBean>?) {
        if (gameList == dataList) {
            return
        }

        if (gameList.isNullOrEmpty()) {
            if (dataList.isNullOrEmpty()) {
                binding.root.hide()
            }
            return
        }

        currentPlayer?.release()
        dataList = gameList
        gameList.toMutableList().let {
            miniGameAdapter.setNewInstance(it)
            setTabData(it)
        }
    }

    private fun setTabData(gameList: MutableList<OKGameBean>) {

        val tabMediator = TabLayoutMediator(binding.tabLayout, binding.vp) { tab, position ->
            var itemView = if (tab.tag == null) {
                val triple = createTabItem(context)
                tab.tag = triple
                triple
            } else {
                tab.tag as Triple<View, ImageView, TextView>
            }

            val item = gameList[position]
            tab.customView = itemView.first
            tab.text = item.gameName
            itemView.third.text = item.gameName
//            itemView.second.load(item.imgGame)
            itemView.second.load(R.drawable.ic_minigame_dice)
        }

        tabMediator.attach()
    }

    private fun createTabItem(context: Context): Triple<View, ImageView, TextView> {
        val lin = LinearLayout(context)
        lin.layoutParams = LinearLayout.LayoutParams(-2, -1)
        lin.gravity = Gravity.CENTER

        val img = AppCompatImageView(context)
        28.dp.let { img.layoutParams = LinearLayout.LayoutParams(it, it) }
        lin.addView(img)

        val text = AppCompatTextView(context)
        text.maxLines = 1
        text.textSize = 14f
        text.layoutParams = LinearLayout.LayoutParams(-2, -2).apply { leftMargin = 8.dp }
        lin.addView(text)

        return Triple(lin, img, text)
    }

    private class MiniGameAdapter(private val onClick: (OKGameBean) -> Unit)
        : BindingAdapter<OKGameBean, ItemMinigameBinding>() {

        init {
            setDiffCallback(object : DiffUtil.ItemCallback<OKGameBean>() {
                override fun areItemsTheSame(oldItem: OKGameBean, newItem: OKGameBean) = oldItem == newItem

                override fun areContentsTheSame(oldItem: OKGameBean, newItem: OKGameBean): Boolean {
                    return oldItem.id == newItem.id
                            && oldItem.jackpotAmount == newItem.jackpotAmount
                            && Objects.equals(oldItem.imgGame, newItem.imgGame)
                            && Objects.equals(oldItem.extraParam, newItem.extraParam)
                }

            })
        }

        override fun onCreateDefViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BindingVH<ItemMinigameBinding> {
            val holder = super.onCreateDefViewHolder(parent, viewType)
            holder.vb.tvJackPotAmount.setPrefixString("$showCurrencySign ")
            holder.vb.videoPlayer.tag = GSYVideoOptionBuilder()
            val context = parent.context
            holder.vb.tvBetToWin.background = ShapeDrawable()
                .setSolidColor(context.getColor(R.color.color_0063FF), context.getColor(R.color.color_00C2FF))
                .setRadius(30.dp.toFloat())
                .setSolidGradientOrientation(ShapeGradientOrientation.TOP_TO_BOTTOM)
            holder.vb.videoPlayer.setPlayStatusListener(object : OKVideoPlayer.PlayStatusListener {
                override fun onPrepare() {
                    holder.vb.vCover.show()
                }
                override fun onPlaying() {
                    holder.vb.vCover.alpahAnimation(400, 1f, 0f)
                }
                override fun onPause() {
                    holder.vb.vCover.alpha = 1f
                }
                override fun onPlayComplete() {
                    holder.vb.vCover.alpha = 1f
                }
                override fun onError() {
                    holder.vb.vCover.alpha = 1f
                }

            })
            return holder
        }

        override fun onBinding(position: Int, binding: ItemMinigameBinding, item: OKGameBean) {
            binding.vCover.setOnClickListener { onClick.invoke(item) }
            binding.tvJackPotAmount.setNumberString(item.jackpotAmount.toString())
//            binding.vCover.load(item.imgGame, R.drawable.img_mini_game_cover)
            with(binding.videoPlayer.tag as GSYVideoOptionBuilder) {
                setIsTouchWiget(false)
                setCacheWithPlay(false)
                setPlayTag(PLAY_TAG)
                setUrl(item.extraParam)
                setPlayPosition(position)
                build(binding.videoPlayer)
            }

        }

    }


}