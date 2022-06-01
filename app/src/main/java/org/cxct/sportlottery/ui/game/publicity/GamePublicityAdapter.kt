package org.cxct.sportlottery.ui.game.publicity

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.HomeBottomNavigationBinding
import org.cxct.sportlottery.databinding.ItemPublicityRecommendBinding
import org.cxct.sportlottery.databinding.PublicitySubTitleViewBinding
import org.cxct.sportlottery.databinding.PublicityTitleViewBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.game.Page
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager


class GamePublicityAdapter(private val publicityAdapterListener: PublicityAdapterListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //排序對應表
    private val sortMap = mapOf<Any, Int>(
        //標題圖片
        PublicityTitleImageData::class to 1,
        //熱門推薦..更多
        PublicitySubTitleImageData::class to 2,
        //足球, 滾球, 數量, 聯賽名, 國旗, 賽事內容
        Recommend::class to 3,
        BottomNavigationItem::class to 4
    )

    var oddsType: OddsType = OddsType.EU
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var isLogin: Boolean = false
        set(value) {
            field = value
            notifyToolbar()
        }

    var hasNotice: Boolean = false
        set(value) {
            field = value
            notifyToolbar()
        }

    enum class ItemType {
        PUBLICITY_TITLE,
        PUBLICITY_SUB_TITLE,
        RECOMMEND,
        BOTTOM_NAVIGATION,
        NONE
    }

    // region ItemClass
    class PublicityTitleImageData {
        /**
         * 是否已經重新獲取ConfigData
         */
        var reloadConfig: Boolean = false
    }

    class PublicitySubTitleImageData
    class BottomNavigationItem
    // endregion

    // 接收任何型別
    private var mDataList = mutableListOf<Any>()

    //region addData Function
    fun addTitle() {
        removeData(PublicityTitleImageData())
        addDataWithSort(PublicityTitleImageData())
    }

    fun addSubTitle() {
        removeData(PublicitySubTitleImageData())
        addDataWithSort(PublicitySubTitleImageData())
    }

    fun addRecommend(recommendList: List<Recommend>) {
        removeData(recommendList.firstOrNull())
        recommendList.forEach { addDataWithSort(it) }
    }

    fun addBottomView() {
        removeData(BottomNavigationItem())
        addDataWithSort(BottomNavigationItem())
    }
    //endregion

    //region update Function
    private fun notifyToolbar() {
        val publicityTitleData = mDataList.firstOrNull { it is PublicityTitleImageData }

        notifyItemChanged(mDataList.indexOf(publicityTitleData), publicityTitleData)
    }

    fun updateToolbarBannerImage() {
        removeData(PublicityTitleImageData())
        addDataWithSort(PublicityTitleImageData().apply {
            this.reloadConfig = true
        })
    }

    fun updateRecommendData(position: Int, payload: Recommend) {
        val recommendIndexList = mutableListOf<Int>()
        mDataList.forEachIndexed { index, item -> if (item is Recommend) recommendIndexList.add(index) }
        notifyItemChanged(recommendIndexList[position], payload)
    }
    //endregion

    override fun getItemViewType(position: Int): Int {
        return when (mDataList[position]) {
            is PublicityTitleImageData -> {
                ItemType.PUBLICITY_TITLE.ordinal
            }
            is PublicitySubTitleImageData -> {
                ItemType.PUBLICITY_SUB_TITLE.ordinal
            }
            is Recommend -> {
                ItemType.RECOMMEND.ordinal
            }
            is BottomNavigationItem -> {
                ItemType.BOTTOM_NAVIGATION.ordinal
            }
            else -> {
                ItemType.NONE.ordinal
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.PUBLICITY_TITLE.ordinal -> {
                PublicityTitleViewHolder(
                    PublicityTitleViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ItemType.PUBLICITY_SUB_TITLE.ordinal -> {
                PublicitySubTitleViewHolder(
                    PublicitySubTitleViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ItemType.RECOMMEND.ordinal -> {
                PublicityRecommendViewHolder(
                    ItemPublicityRecommendBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), publicityAdapterListener
                )
            }
            ItemType.BOTTOM_NAVIGATION.ordinal -> {
                BottomNavigationViewHolder(
                    HomeBottomNavigationBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> UndefinedViewHolder(View(parent.context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEachIndexed { _, payload ->
                when (payload) {
                    is Recommend -> {
                        (holder as PublicityRecommendViewHolder).update(payload, oddsType) { notifyItemChanged(position, payload) }
                    }
                    is PublicityTitleImageData -> {
                        (holder as PublicityTitleViewHolder).updateToolbar(payload)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = mDataList[position]
        when (holder) {
            is PublicityRecommendViewHolder -> {
                if (data is Recommend) {
                    holder.bind(data, oddsType) { notifyItemChanged(position, data) }
                }
            }
            is PublicityTitleViewHolder -> {
                if (data is PublicityTitleImageData) {
                    holder.bind(data)
                }
            }
            is PublicitySubTitleViewHolder -> {
                holder.bind()
            }
            is BottomNavigationViewHolder -> {
                holder.bind()
            }
            is BaseItemListenerViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = mDataList.size

    // region ItemViewHolder
    inner class PublicityTitleViewHolder(val binding: PublicityTitleViewBinding) :
    //BaseItemListenerViewHolder(binding.root, publicityAdapterListener){
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
        fun bind(data: PublicityTitleImageData) {
            with(binding) {
                //region Toolbar
                with(publicityToolbar) {
                    //登入狀態, 訊息狀態
                    updateToolbar(data)

                    //region Transparent style
                    toolBar.setBackgroundColor(
                        ContextCompat.getColor(
                            MultiLanguagesApplication.appContext,
                            android.R.color.transparent
                        )
                    )
                    ivLogo.setImageResource(R.drawable.ic_logo_for_day_mode)
                    ivNotice.setImageResource(
                        if (hasNotice) {
                            R.drawable.icon_bell_white_red_dot
                        } else {
                            R.drawable.icon_bell_white
                        }
                    )
                    ivMenu.setImageResource(R.drawable.ic_menu_gray)
                    tvLanguage.setTextColor(
                        ContextCompat.getColor(
                            MultiLanguagesApplication.appContext,
                            R.color.color_FFFFFF_FFFFFF
                        )
                    )
                    //endregion

                    //region Language block
                    ivLanguage.setImageResource(LanguageManager.getLanguageFlag(MultiLanguagesApplication.appContext))
                    tvLanguage.text = LanguageManager.getLanguageStringResource(MultiLanguagesApplication.appContext)
                    //endregion

                    //region Click event
                    ivLogo.setOnClickListener { publicityAdapterListener.onLogoClickListener() }
                    blockLanguage.setOnClickListener { publicityAdapterListener.onLanguageBlockClickListener() }
                    ivNotice.setOnClickListener { publicityAdapterListener.onNoticeClickListener() }
                    ivMenu.setOnClickListener { publicityAdapterListener.onMenuClickListener() }
                    //endregion
                }
                //endregion

            }
        }

        fun updateToolbar(data: PublicityTitleImageData) {
            with(binding.publicityToolbar) {
                if (isLogin) {
                    ivNotice.visibility = View.VISIBLE
                    ivMenu.visibility = View.VISIBLE

                    blockLanguage.visibility = View.GONE
                } else {
                    ivNotice.visibility = View.GONE
                    ivMenu.visibility = View.GONE

                    blockLanguage.visibility = View.VISIBLE
                }

                ivNotice.setImageResource(
                    if (hasNotice) {
                        R.drawable.icon_bell_white_red_dot
                    } else {
                        R.drawable.icon_bell_white
                    }
                )

                setupBanner(data)
            }
        }

        private fun setupBanner(data: PublicityTitleImageData) {
            if (data.reloadConfig) {
                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_image_load)
                    .error(R.drawable.ic_image_broken)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform()

                with(binding) {
                    val imageList = sConfigData?.imageList?.filter {
                        it.imageType == 2
                    }
                    banner.setAdapter(object :
                        BannerImageAdapter<ImageData?>(imageList) {
                        override fun onBindView(
                            holder: BannerImageHolder,
                            data: ImageData?,
                            position: Int,
                            size: Int
                        ) {
                            val url = sConfigData?.resServerHost + data?.imageName1
                            Glide.with(holder.itemView)
                                .load(url)
                                .apply(requestOptions)
                                .into(holder.imageView)
                            holder.imageView.setOnClickListener {
                                publicityAdapterListener.onGoHomePageListener()
                            }
                        }
                    })
                }
            }
        }
    }

    inner class PublicitySubTitleViewHolder(val binding: PublicitySubTitleViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            with(binding) {
                tvMore.setGoHomePageListener()
                ivMore.setGoHomePageListener()
            }
        }

        private fun View.setGoHomePageListener() {
            setOnClickListener { publicityAdapterListener.onGoHomePageListener() }
        }
    }

    inner class BottomNavigationViewHolder(val binding: HomeBottomNavigationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context

        fun bind() {
            with(binding) {
                bottomNavigationView.setNowPage(Page.PUBLICITY)
                ContextCompat.getDrawable(context, R.color.color_141414_F3F4F5)?.let { background ->
                    bottomNavigationView.setTopBackground(background)
                }
                ContextCompat.getDrawable(context, R.color.color_191919_EEEFF0)?.let { background ->
                    bottomNavigationView.setBottomBackground(background)
                }
            }
        }
    }

    inner class UndefinedViewHolder(itemView: View) : BaseItemListenerViewHolder(itemView, publicityAdapterListener)
    // endregion

    //region Data Getter
    fun getRecommendData(): MutableList<Recommend> {
        val result = mutableListOf<Recommend>()
        mDataList.filterIsInstance<Recommend>().forEach { result.add(it) }
        return result
    }
    //endregion

    // region private functions
    // 依照傳入參數刪除同一個類別的資料
    private fun removeData(src: Any?) {
        src?.let {
            val iterator = mDataList.iterator()
            while (iterator.hasNext()) {
                if (iterator.next()::class.isInstance(src))
                    iterator.remove()
            }
        }
    }

    // 依照sortMap的順序插入資料
    private fun addDataWithSort(src: Any) {
        // 如果列表裡面沒東西，直接插
        if (mDataList.isEmpty()) {
            mDataList.add(src)
            notifyItemChanged(0)
            return
        }
        mDataList.forEachIndexed { index, target ->
            if (isPrev(src, target)) {
                mDataList.add(index, src)
                notifyItemChanged(index)
                return
            }
            if (index == mDataList.size) return
        }
        mDataList.add(src)
        notifyItemChanged(mDataList.size - 1)
    }

    private fun isPrev(src: Any, target: Any): Boolean {
        if (getSortPoint(src) < getSortPoint(target)) return true
        return false
    }

    private fun getSortPoint(item: Any): Int = sortMap[item::class] ?: 0
    // endregion

    class PublicityAdapterListener(
        private val onLogoClickListener: () -> Unit,
        private val onLanguageBlockClickListener: () -> Unit,
        private val onNoticeClickListener: () -> Unit,
        private val onMenuClickListener: () -> Unit,
        private val onItemClickListener: () -> Unit,
        private val onGoHomePageListener: () -> Unit,
        private val onClickBetListener: (gameType: String, matchType: MatchType, matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?, playCateMenuCode: String?) -> Unit,
        private val onClickFavoriteListener: (matchId: String?) -> Unit,
        private val onClickStatisticsListener: (matchId: String) -> Unit,
        private val onClickPlayTypeListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
        private val onClickLiveIconListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
        private val onClickAnimationIconListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit
    ) {
        fun onLogoClickListener() = onLogoClickListener.invoke()
        fun onLanguageBlockClickListener() = onLanguageBlockClickListener.invoke()
        fun onNoticeClickListener() = onNoticeClickListener.invoke()
        fun onMenuClickListener() = onMenuClickListener.invoke()
        fun onItemClickListener() = onItemClickListener.invoke()
        fun onGoHomePageListener() = onGoHomePageListener.invoke()
        fun onClickBetListener(
            gameType: String,
            matchType: MatchType,
            matchInfo: MatchInfo?,
            odd: Odd,
            playCateCode: String,
            playCateName: String,
            betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
            playCateMenuCode: String?
        ) = onClickBetListener.invoke(
            gameType,
            matchType,
            matchInfo,
            odd,
            playCateCode,
            playCateName,
            betPlayCateNameMap,
            playCateMenuCode
        )

        fun onClickFavoriteListener(matchId: String?) = onClickFavoriteListener.invoke(matchId)
        fun onClickStatisticsListener(matchId: String) = onClickStatisticsListener.invoke(matchId)
        fun onClickPlayTypeListener(
            gameType: String,
            matchType: MatchType?,
            matchId: String?,
            matchInfoList: List<MatchInfo>
        ) =
            onClickPlayTypeListener.invoke(gameType, matchType, matchId, matchInfoList)
        fun onClickLiveIconListener(gameType: String,
                                    matchType: MatchType?,
                                    matchId: String?,
                                    matchInfoList: List<MatchInfo>) = onClickLiveIconListener.invoke(gameType, matchType, matchId, matchInfoList)
        fun onClickAnimationIconListener(gameType: String,
                                    matchType: MatchType?,
                                    matchId: String?,
                                    matchInfoList: List<MatchInfo>) = onClickAnimationIconListener.invoke(gameType, matchType, matchId, matchInfoList)
    }
}

abstract class BaseItemListenerViewHolder(
    val view: View,
    private val publicityAdapterListener: GamePublicityAdapter.PublicityAdapterListener
) : RecyclerView.ViewHolder(view) {
    open fun bind() {
        view.rootView.setOnClickListener { publicityAdapterListener.onItemClickListener() }
    }
}