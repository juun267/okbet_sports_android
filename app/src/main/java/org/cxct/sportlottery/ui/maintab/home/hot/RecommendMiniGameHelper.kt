package org.cxct.sportlottery.ui.maintab.home.hot

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PLAYING
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ItemMinigameBinding
import org.cxct.sportlottery.databinding.LayoutRecommendMinigameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.VisibilityFragment
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

    private val binding by lazy {
        val vb = LayoutRecommendMinigameBinding.inflate(context.layoutInflater)
        val lp = LinearLayout.LayoutParams(-1, 246.dp)
        lp.horizontalMargin = 12.dp
        vb.root.layoutParams = lp
        onLayout.invoke(vb.root)
        initViewPager(vb.vp)
        initTabLayout(vb.tabLayout)
        vb
    }

    private val miniGameAdapter = MiniGameAdapter(onClick)
    private var currentPlayer: OKVideoPlayer? = null
    private val tabLayout: TabLayout get() = binding.tabLayout
    private val viewPager2: ViewPager2 get() = binding.vp

    private var dataList: List<OKGameBean>? = null
    private var isClosed: Boolean = false

    fun bindLifeEvent(lifecycleOwner: LifecycleOwner) {
        isClosed = !StaticData.miniGameOpened()
        OKGamesViewModel.miniGameList.observe(lifecycleOwner) { setup(it.first) }
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleOwner.lifecycleScope) {
            if (it.firmCode != "OKMINI" || dataList.isNullOrEmpty() || dataList!!.first().maintain == it.maintain) {
                return@collectWith
            }

            val maintain = it.maintain
            dataList!!.forEach { it.maintain = maintain }
            miniGameAdapter.notifyDataSetChanged()

            if (it.maintain != 1) {
                postPlay()
            }
        }

        ConfigRepository.onNewConfig(lifecycleOwner) {
            isClosed = false
            if (isClosed) {
                clearPostPlay()
                if (!dataList.isNullOrEmpty()) {
                    binding.root.hide()
                }
            } else {
                if (!dataList.isNullOrEmpty()) {
                    binding.root.show()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onResume(owner: LifecycleOwner) {
                if (StaticData.miniGameOpened()
                    && dataList?.getOrNull(0)?.isMaintain() == false
                    && (owner !is VisibilityFragment || owner.isVisibleToUser())) {
                    postPlay()
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                clearPostPlay()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                currentPlayer?.release()
            }
        })
    }

    fun resumePlay() {
        postPlay()
    }

    fun pausePlay() {
        clearPostPlay()
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
                val position = viewPager2.currentItem - viewPager2.currentItem % tabLayout.tabCount + tab.position
                viewPager2.currentItem = position
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

                val index = position % tabLayout.tabCount
                tabLayout.selectTab(tabLayout.getTabAt(index), true)
                val playPosition = GSYVideoManager.instance().playPosition
                if (playPosition >= 0
                    && GSYVideoManager.instance().playTag == PLAY_TAG
                    && position != playPosition) {
                    GSYVideoManager.releaseAllVideos()
                }

                if (!isClosed && false == dataList?.getOrNull(index)?.isMaintain()) {
                    postPlay()
                }
            }

        })
    }

    private val handler = Handler(Looper.getMainLooper())
    private val playRunnable by lazy { Runnable { playPosition() } }
    private fun postPlay() {
        clearPostPlay()
        handler.postDelayed(playRunnable, 100)
    }

    private fun clearPostPlay() {
        currentPlayer?.onVideoPause()
        currentPlayer = null
        handler.removeCallbacks(playRunnable)
    }

    private fun playPosition() {
        val viewHolder = (binding.vp.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(binding.vp.currentItem)
        if (viewHolder != null) {
            currentPlayer = (viewHolder as BindingVH<ItemMinigameBinding>).vb.videoPlayer
            currentPlayer!!.startPlayLogic()
        }
    }

    fun setup(gameList: List<OKGameBean>?) {

        if (gameList.isNullOrEmpty()) {
            if (dataList.isNullOrEmpty()) {
                binding.root.hide()
            }
            return
        }

        if (!dataList.isNullOrEmpty()) {
            return
        }

        binding.root.isVisible = !isClosed
        currentPlayer?.release()
        dataList = gameList


//        val dataList = mutableListOf<OKGameBean>()
//        dataList.addAll(gameList)
//        dataList.addAll(gameList)
//        dataList.addAll(gameList)
//        dataList.addAll(gameList)
//        dataList.addAll(gameList)
//        dataList.addAll(gameList)
//        miniGameAdapter.setNewInstance(gameList.toMutableList())
        gameList.toMutableList().let {
            miniGameAdapter.setNewInstance(it)
            setTabData(it)
        }

        viewPager2.setCurrentItem(100000 * gameList.size, false)
    }

    private fun setTabData(gameList: MutableList<OKGameBean>) {
        binding.tabLayout.removeAllTabs()

        gameList.forEachIndexed { index, okGameBean ->
            val tab = binding.tabLayout.newTab()
            var itemView = if (tab.tag == null) {
                val triple = createTabItem(context)
                tab.tag = triple
                triple
            } else {
                tab.tag as Triple<View, ImageView, TextView>
            }

            tab.customView = itemView.first
            tab.text = okGameBean.gameName
            itemView.third.text = okGameBean.gameName
            itemView.second.load(okGameBean.imgGame, R.drawable.ic_okbet_round)
            binding.tabLayout.addTab(tab)
        }

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
                            && oldItem.maintain == newItem.maintain
                }

            })
        }

        override fun getItem(position: Int): OKGameBean {
            return super.getItem(position % super.getDefItemCount())
        }

        override fun getDefItemCount() : Int {
            val count = super.getDefItemCount()
            return if (count > 1) Int.MAX_VALUE else count
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BindingVH<ItemMinigameBinding> {
            val holder = super.onCreateDefViewHolder(parent, viewType)
            holder.vb.tvJackPotAmount.setPrefixString("$showCurrencySign ")
            holder.vb.videoPlayer.tag = GSYVideoOptionBuilder()
            val context = parent.context
            holder.vb.tvBetToWin.text = "${showCurrencySign}5 To Win"
            holder.vb.tvBetToWin.background = ShapeDrawable()
                .setSolidColor(context.getColor(R.color.color_0063FF), context.getColor(R.color.color_00C2FF))
                .setRadius(30.dp.toFloat())
                .setSolidGradientOrientation(ShapeGradientOrientation.TOP_TO_BOTTOM)
            holder.vb.videoPlayer.playStatusListener = object: OKVideoPlayer.PlayStatusListener{
                override fun onStatuChanged(state: Int) {
                    holder.vb.vCover.alpha = if (state != CURRENT_STATE_PLAYING) 1f else 0f
                }

            }
            return holder
        }

        override fun onBinding(position: Int, binding: ItemMinigameBinding, item: OKGameBean) { }

        override fun onBindViewHolder(holder: BindingVH<ItemMinigameBinding>, position: Int) {
            val binding = holder.vb
            val item = getItem(position)
            binding.vCover.setOnClickListener {
                if (!item.isMaintain() && StaticData.miniGameOpened()) {
                    onClick.invoke(item)
                }
            }
            binding.tvJackPotAmount.setNumberString(item.jackpotAmount.toString())
            binding.vCover.load(item.imgBigGame, R.drawable.img_placeholder_default)
            with(binding.videoPlayer.tag as GSYVideoOptionBuilder) {
                setIsTouchWiget(false)
                setCacheWithPlay(false)
                setPlayTag(PLAY_TAG)
                setUrl(item.extraParam)
                setPlayPosition(position)
                build(binding.videoPlayer)
            }

            if (item.isMaintain()) {
                enableMaintain(binding)
            } else {
                disableMaintain(binding)
            }
        }

        private fun enableMaintain(binding: ItemMinigameBinding) {
            binding.vAmountBg.hide()
            binding.tvJackPotAmount.hide()
            binding.tvBetToWin.hide()
            binding.vCover.show()
            binding.linMaintenance.root.show()

        }

        private fun disableMaintain(binding: ItemMinigameBinding) {
            binding.vAmountBg.show()
            binding.tvJackPotAmount.show()
            binding.tvBetToWin.show()
            binding.vCover.show()
            binding.linMaintenance.root.hide()
        }

    }


}