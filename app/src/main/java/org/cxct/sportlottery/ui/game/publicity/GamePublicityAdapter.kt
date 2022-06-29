package org.cxct.sportlottery.ui.game.publicity

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.*
import org.cxct.sportlottery.network.common.GameStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.game.Page
import org.cxct.sportlottery.ui.game.common.OddStatePublicityViewHolder
import org.cxct.sportlottery.ui.game.widget.OddsButtonPublicity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import timber.log.Timber


class GamePublicityAdapter(private val publicityAdapterListener: PublicityAdapterListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //排序對應表
    private val sortMap = mapOf<Any, Int>(
        //標題圖片
        PublicityTitleImageData::class to 1,
        //跑馬燈
        PublicityAnnouncementData::class to 2,
        //用戶資訊
        PublicityUserInfoData::class to 3,
        //熱門推薦..更多
        PublicitySubTitleImageData::class to 4,

        PreloadItem::class to 5,
        //足球, 滾球, 數量, 聯賽名, 國旗, 賽事內容
        Recommend::class to 6,
        //E-Games
        PublicityEGamesData::class to 7,
        BottomNavigationItem::class to 8
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

    private val mOddStateRefreshListener by lazy {
        object : OddStatePublicityViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) { }
        }
    }

    /**
     * 僅用來記錄當前適配的折扣率
     */
    var discount: Float = 1.0F

    enum class ItemType {
        PUBLICITY_TITLE,
        PUBLICITY_ANNOUNCEMENT,
        PUBLICITY_USER_INFO,
        PUBLICITY_SUB_TITLE,
        PRELOAD,
        RECOMMEND,
        E_GAMES,
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

    class PublicityAnnouncementData {
        var titleList: MutableList<String> = mutableListOf()
    }

    class PublicityUserInfoData {
        var userId: String = ""
        var userMoney: Double = 0.0
    }

    class PublicitySubTitleImageData
    class PublicityEGamesData {
        var thirdDictValues: ThirdDictValues? = null
    }

    class PreloadItem
    class BottomNavigationItem
    // endregion

    // 接收任何型別
    private var mDataList = mutableListOf<Any>()

    //region addData Function
    fun addTitle() {
        removeData(PublicityTitleImageData())
        addDataWithSort(PublicityTitleImageData())
    }

    fun addAnnouncement() {
        removeData(PublicityAnnouncementData())
        addDataWithSort(PublicityAnnouncementData())
    }

    fun addUserInfo() {
        removeData(PublicityUserInfoData())
        addDataWithSort(PublicityUserInfoData())
    }

    fun addSubTitle() {
        removeData(PublicitySubTitleImageData())
        addDataWithSort(PublicitySubTitleImageData())
    }

    fun addPreload() {
        removeData(PreloadItem())
        val preloadList = listOf(PreloadItem(), PreloadItem())
        preloadList.forEach { addDataWithSort(it) }
    }

    fun addRecommend(recommendList: List<Recommend>) {
        removeData(recommendList.firstOrNull())
        recommendList.forEach { addDataWithSort(it) }
    }

    fun addEGames() {
        removeData(PublicityEGamesData())
        addDataWithSort(PublicityEGamesData())
    }

//    fun addBottomView() {
//        removeData(BottomNavigationItem())
//        addDataWithSort(BottomNavigationItem())
//    }
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

    fun updateAnnouncementData(titleList: MutableList<String>) {
        removeData(PublicityAnnouncementData())
        addDataWithSort(PublicityAnnouncementData().apply {
            this.titleList = titleList
        })
    }

    fun updateUserInfoData(userId: String, userMoney: Double) {
        removeData(PublicityUserInfoData())
        addDataWithSort(PublicityUserInfoData().apply {
            this.userId = userId
            this.userMoney = userMoney
        })
    }

    fun updateEGamesData(thirdDictValues: ThirdDictValues?) {
        removeData(PublicityEGamesData())
        addDataWithSort(PublicityEGamesData().apply {
            this.thirdDictValues = thirdDictValues
        })
    }
    //endregion

    override fun getItemViewType(position: Int): Int {
        return when (mDataList[position]) {
            is PublicityTitleImageData -> {
                ItemType.PUBLICITY_TITLE.ordinal
            }
            is PublicityAnnouncementData -> {
                ItemType.PUBLICITY_ANNOUNCEMENT.ordinal
            }
            is PublicityUserInfoData -> {
                ItemType.PUBLICITY_USER_INFO.ordinal
            }
            is PublicitySubTitleImageData -> {
                ItemType.PUBLICITY_SUB_TITLE.ordinal
            }
            is PreloadItem -> {
                ItemType.PRELOAD.ordinal
            }
            is Recommend -> {
                ItemType.RECOMMEND.ordinal
            }
            is PublicityEGamesData -> {
                ItemType.E_GAMES.ordinal
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
            ItemType.PUBLICITY_ANNOUNCEMENT.ordinal -> {
                PublicityAnnouncementViewHolder(
                    PublicityAnnouncementViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ItemType.PUBLICITY_USER_INFO.ordinal -> {
                PublicityUserInfoViewHolder(
                    PublicityUserInfoViewBinding.inflate(
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
            ItemType.PRELOAD.ordinal -> {
                PreloadViewHolder(
                    ViewLoadingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
//            ItemType.RECOMMEND.ordinal -> {
//                PublicityRecommendViewHolder(
//                    ItemPublicityRecommendBinding.inflate(
//                        LayoutInflater.from(parent.context),
//                        parent,
//                        false
//                    ), publicityAdapterListener
//                )
//            }
            ItemType.RECOMMEND.ordinal -> {
                PublicityNewRecommendViewHolder(
                    PublicityRecommendViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ItemType.E_GAMES.ordinal -> {
                PublicityEGamesViewHolder(
                    PublicityEGamesViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
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

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEachIndexed { _, payload ->
                when (payload) {
//                    is Recommend -> {
//                        (holder as PublicityRecommendViewHolder).update(payload, oddsType) { notifyItemChanged(position, payload) }
//                    }
                    is Recommend -> {
                        (holder as PublicityNewRecommendViewHolder).update(payload, oddsType, publicityAdapterListener)
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
//            is PublicityRecommendViewHolder -> {
//                if (data is Recommend) {
//                    holder.bind(data, oddsType) { notifyItemChanged(position, data) }
//                }
//            }
            is PublicityNewRecommendViewHolder -> {
                if (data is Recommend) {
                    holder.bind(data, oddsType)
                }
            }
            is PublicityAnnouncementViewHolder -> {
                if (data is PublicityAnnouncementData) {
                    holder.bind(data)
                }
            }
            is PublicityUserInfoViewHolder -> {
                if (data is PublicityUserInfoData) {
                    holder.bind(data)
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
            is PublicityEGamesViewHolder -> {
                if (data is PublicityEGamesData) {
                    holder.bind(data)
                }
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
                            R.drawable.icon_bell_with_red_dot
                        } else {
                            R.drawable.icon_bell
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
                    ivLanguage.setImageResource(
                        LanguageManager.getLanguageFlag(
                            MultiLanguagesApplication.appContext
                        )
                    )
                    tvLanguage.text =
                        LanguageManager.getLanguageStringResource(MultiLanguagesApplication.appContext)
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
                        R.drawable.icon_bell_with_red_dot
                    } else {
                        R.drawable.icon_bell
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

    inner class PublicityAnnouncementViewHolder(val binding: PublicityAnnouncementViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var marqueeAdapter = MarqueeAdapter()

        fun bind(data: PublicityAnnouncementData) {
            with(binding) {
                root.setOnClickListener {
                    publicityAdapterListener.onGoNewsPageListener()
                }
                rvMarquee.apply {
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = marqueeAdapter
                }

                marqueeAdapter.setData(data.titleList)
                if (data.titleList.size > 0) {
                    rvMarquee.startAuto(false) //啟動跑馬燈
                } else {
                    rvMarquee.stopAuto(true) //停止跑馬燈
                }
            }
        }
    }

    inner class PublicityUserInfoViewHolder(val binding: PublicityUserInfoViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: PublicityUserInfoData) {
            with(binding) {
                if (isLogin) {
                    llUserData.visibility = View.VISIBLE
                    llLoginSignup.visibility = View.GONE
                } else {
                    llUserData.visibility = View.GONE
                    llLoginSignup.visibility = View.VISIBLE
                }

                tvUserId.text = data.userId
                val userBalanceText =
                    "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(data.userMoney)}"
                tvUserBalance.text = userBalanceText

                btnLogin.setOnClickListener {
                    publicityAdapterListener.onGoLoginListener()
                }
                btnRegister.setOnClickListener {
                    publicityAdapterListener.onGoRegisterListener()
                }
                btnDeposit.apply {
                    setVisibilityByCreditSystem()
                    setOnClickListener {
                        publicityAdapterListener.onGoDepositListener()
                    }
                }
                btnWithdraw.apply {
                    setVisibilityByCreditSystem()
                    setOnClickListener {
                        publicityAdapterListener.onGoWithdrawListener()
                    }
                }
            }
        }
    }

    inner class PublicitySubTitleViewHolder(val binding: PublicitySubTitleViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            with(binding) {
                tvMore.setGoHomePageListener()
            }
        }

        private fun View.setGoHomePageListener() {
            setOnClickListener { publicityAdapterListener.onGoHomePageListener() }
        }
    }

    inner class PublicityNewRecommendViewHolder(val binding: PublicityRecommendViewBinding,
                                                override val oddStateChangeListener: OddStateChangeListener = mOddStateRefreshListener) :
        ViewHolderUtils.TimerViewHolderTimer(binding.root) {
        private val mRequestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontTransform()

        private var oddList = listOf<Odd?>()

        fun bind(data: Recommend, oddsType: OddsType) {
            with(binding) {
                clSportsBackground.setBackgroundResource(GameType.getGameTypeBackground(data.gameType))

                tvHomeName.text = data.homeName
                tvAwayName.text = data.awayName

                updateMatchScore(data)

                tvGamePlayCateCodeName.text = data.playCateNameMap?.get(PlayCate.SINGLE.value)
                    ?.get(LanguageManager.getSelectLanguage(binding.root.context).key)

                Glide.with(binding.root.context)
                    .load(data.matchInfo?.homeIcon)
                    .apply(mRequestOptions)
                    .fallback(R.drawable.bg_recommend_game_default)
                    .error(R.drawable.bg_recommend_game_default)
                    .into(ivHomeIcon)

                Glide.with(binding.root.context)
                    .load(data.matchInfo?.awayIcon)
                    .apply(mRequestOptions)
                    .fallback(R.drawable.bg_recommend_game_default)
                    .error(R.drawable.bg_recommend_game_default)
                    .into(ivAwayIcon)

                val matchOddList = transferMatchOddList(data)
                Timber.e("matchOddList: $matchOddList")
                val gameType = data.gameType
                val matchType = data.matchType
                setupMatchTimeAndStatus(
                    item = data,
                    isTimerEnable = (gameType == GameType.FT.key || gameType == GameType.BK.key || gameType == GameType.RB.key || gameType == GameType.AFT.key || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT),
                    isTimerPause = data.matchInfo?.stopped == TimeCounting.STOP.value
                )
            }
        }

        fun update(data: Recommend, oddsType: OddsType, publicityAdapterListener: PublicityAdapterListener) {
            val matchOddList = transferMatchOddList(data)
            Timber.e("matchOddList2: $matchOddList")
            //玩法Code
            var oddPlayCateCode = ""
            var oddList = listOf<Odd?>()
            data.oddsMap?.forEach { (key, value) ->
                if (key == PlayCate.SINGLE.value) {
                    oddPlayCateCode = key
                    oddList = value.orEmpty()
                }
            }
            //玩法名稱
            val playCateName = data.playCateNameMap?.get(oddPlayCateCode)?.get(LanguageManager.getSelectLanguage(binding.root.context).key) ?: ""
//            Timber.e("oddList: $oddList")
            with(binding) {
                //更新賽事比分
                updateMatchScore(data)

                //region 主隊賠率項
                if (oddList.isNotEmpty()) {
                    val homeOdd = oddList[0]
                    with(oddBtnHome) {
                        visibility = View.VISIBLE
                        setupOddsButton(this, homeOdd)
                        setButtonBetClick(
                            data = data,
                            odd = homeOdd,
                            playCateCode = oddPlayCateCode,
                            playCateName = playCateName,
                            publicityAdapterListener = publicityAdapterListener
                        )
                    }
                } else {
                    oddBtnHome.visibility = View.GONE
                }
                //endregion

                //region 客隊賠率項
                if (oddList.size > 1) {
                    val awayOdd = oddList[1]
                    with(oddBtnAway) {
                        visibility = View.VISIBLE
                        setupOddsButton(this, awayOdd)
                        setButtonBetClick(
                            data = data,
                            odd = awayOdd,
                            playCateCode = oddPlayCateCode,
                            playCateName = playCateName,
                            publicityAdapterListener = publicityAdapterListener
                        )
                    }
                } else {
                    oddBtnAway.visibility = View.GONE
                }
                //endregion

                //region 平局賠率項
                if (oddList.size > 2) {
                    val drawOdd = oddList[2]
                    with(oddBtnDraw) {
                        visibility = View.VISIBLE
                        setupOddsButton(this, drawOdd)
                        setButtonBetClick(
                            data = data,
                            odd = drawOdd,
                            playCateCode = oddPlayCateCode,
                            playCateName = playCateName,
                            publicityAdapterListener = publicityAdapterListener
                        )
                    }
                } else {
                    oddBtnDraw.visibility = View.GONE
                }
                //endregion

                //region 比賽狀態(狀態、時間)
                val gameType = data.gameType
                val matchType = data.matchType
                setupMatchTimeAndStatus(
                    item = data,
                    isTimerEnable = (gameType == GameType.FT.key || gameType == GameType.BK.key || gameType == GameType.RB.key || gameType == GameType.AFT.key || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT),
                    isTimerPause = data.matchInfo?.stopped == TimeCounting.STOP.value
                )
                //endregion
            }
        }

        /**
         * 配置投注按鈕Callback
         */
        private fun OddsButtonPublicity.setButtonBetClick(
            data: Recommend,
            odd: Odd?,
            playCateCode: String,
            playCateName: String,
            publicityAdapterListener: PublicityAdapterListener
        ) {
            setOnClickListener {
                data.matchType?.let { matchType ->
                    odd?.let { odd ->
                        publicityAdapterListener.onClickBetListener(
                            gameType = data.gameType,
                            matchType = matchType,
                            matchInfo = data.matchInfo,
                            odd = odd,
                            playCateCode = playCateCode,
                            playCateName = playCateName,
                            betPlayCateNameMap = data.betPlayCateNameMap,
                            playCateMenuCode = data.menuList.firstOrNull()?.code
                        )
                    }
                }
            }
        }

        private fun PublicityRecommendViewBinding.updateMatchScore(data: Recommend) {
            tvHomeScore.text = (data.matchInfo?.homeScore ?: 0).toString()
            tvAwayScore.text = (data.matchInfo?.awayScore ?: 0).toString()
        }

        private fun setupMatchTimeAndStatus(
            item: Recommend,
            isTimerEnable: Boolean,
            isTimerPause: Boolean
        ) {
            setupMatchTime(item, isTimerEnable, isTimerPause)
            setStatusText(item)
            setTextViewStatus(item)
        }

        /**
         * 賽事時間
         */
        private fun setupMatchTime(
            item: Recommend,
            isTimerEnable: Boolean,
            isTimerPause: Boolean
        ) {

            /* TODO 依目前開發方式優化，將狀態和時間保存回 viewModel 於下次刷新頁面前 api 取得資料時先行代入相關 data 內，
                此處倒數計時前須先設置時間及狀態，可解決控件短暫空白。(賽事狀態已於 BaseFavoriteViewModel #1 處調整過)*/

            when {
                TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                    binding.ivLiveIcon.visibility = View.VISIBLE
                    val socketValue = item.matchInfo?.socketMatchStatus

                    if (needCountStatus(socketValue)) {
                        binding.tvGamePlayTime.visibility = View.VISIBLE
                        listener = object : TimerListener {
                            override fun onTimerUpdate(timeMillis: Long) {
                                if (timeMillis > 1000) {
                                    binding.tvGamePlayTime.text =
                                        TimeUtil.longToMmSs(timeMillis)
                                } else {
                                    binding.tvGamePlayTime.text =
                                        binding.root.context.getString(R.string.time_up)
                                }
                                item.matchInfo?.leagueTime = (timeMillis / 1000).toInt()
                            }
                        }

                        updateTimer(
                            isTimerEnable,
                            isTimerPause,
                            item.matchInfo?.leagueTime ?: 0,
                            (item.matchInfo?.gameType == GameType.BK.key ||
                                    item.matchInfo?.gameType == GameType.RB.key ||
                                    item.matchInfo?.gameType == GameType.AFT.key)
                        )

                    } else {
                        binding.tvGamePlayTime.visibility = View.GONE
                    }
                }

                TimeUtil.isTimeAtStart(item.matchInfo?.startTime) -> {
                    binding.ivLiveIcon.visibility = View.VISIBLE
                    listener = object : TimerListener {
                        override fun onTimerUpdate(timeMillis: Long) {
                            if (timeMillis > 1000) {
                                val min = TimeUtil.longToMinute(timeMillis)
                                binding.tvGamePlayTime.text = String.format(
                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
                                    min
                                )
                            } else {
                                //等待Socket更新
                                binding.tvGamePlayTime.text = String.format(
                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
                                    0
                                )
                            }
                            item.matchInfo?.remainTime = timeMillis
                            binding.ivLiveIcon.visibility = View.VISIBLE
                        }
                    }

                    item.matchInfo?.remainTime?.let { remainTime ->
                        updateTimer(
                            true,
                            isTimerPause,
                            (remainTime / 1000).toInt(),
                            true
                        )
                    }
                }
                else -> {
                    binding.tvGamePlayTime.text = TimeUtil.timeFormat(item.matchInfo?.startTime, "HH:mm")
                    binding.ivLiveIcon.visibility = View.GONE
                }
            }
        }

        private fun setStatusText(item: Recommend) {
            binding.tvGameStatus.text = when {
                (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)
                        && item.matchInfo?.status == GameStatus.POSTPONED.code
                        && (item.matchInfo?.gameType == GameType.FT.name || item.matchInfo?.gameType == GameType.BK.name || item.matchInfo?.gameType == GameType.TN.name)) -> {
                    itemView.context.getString(R.string.game_postponed)
                }
                TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                    if (item.matchInfo?.statusName18n != null) {
                        item.matchInfo?.statusName18n
                    } else {
                        ""
                    }
                }
                else -> {
                    if (TimeUtil.isTimeToday(item.matchInfo?.startTime))
                        itemView.context.getString((R.string.home_tab_today))
                    else
                        item.matchInfo?.startDateDisplay
                }
            }
        }

        private fun setTextViewStatus(item: Recommend) {
            when {
                (TimeUtil.isTimeInPlay(item.matchInfo?.startTime) && item.matchInfo?.status == GameStatus.POSTPONED.code && (item.matchInfo?.gameType == GameType.FT.name || item.matchInfo?.gameType == GameType.BK.name || item.matchInfo?.gameType == GameType.TN.name)) -> {
                    binding.tvGameSpt.visibility = View.GONE
                    binding.tvGamePlayTime.visibility = View.GONE
                }

                TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                    if (item.matchInfo?.statusName18n != null) {
                        binding.tvGameStatus.visibility = View.VISIBLE
                    } else {
                    }
                }
                TimeUtil.isTimeAtStart(item.matchInfo?.startTime) -> {
                    binding.tvGameStatus.visibility = View.GONE
                }
            }
        }

//        private fun setupOddsButton(oddsButton: OddsButtonPublicity, odd: Odd?, matchOdd: MatchOdd, recommend: Recommend) {
        private fun setupOddsButton(oddsButton: OddsButtonPublicity, odd: Odd?) {

            oddsButton.apply {
                setupOdd(odd, oddsType)
                setupOddState(oddsButton, odd)
                odd?.let {
                    this.isSelected = it.isSelected ?: false

//                    setOnClickListener {
//                        onClickOddListener?.onClickBet(matchOdd.apply {
//                            this.matchInfo?.gameType = recommend.gameType
//                        }, odd, recommend.playCateCode, matchOdd.playCateName , recommend.betPlayCateNameMap)
//                    }
                }
            }
        }

        private fun transferMatchOddList(recommend: Recommend): MutableList<MatchOdd> {
            with(recommend) {
                return mutableListOf(
                    MatchOdd(
                        matchInfo = matchInfo,
                        oddsMap = oddsMap,
                        playCateNameMap = playCateNameMap,
                        betPlayCateNameMap = betPlayCateNameMap,
                        oddsSort = oddsSort
                    )
                )
            }
        }
    }

    inner class PublicityEGamesViewHolder(val binding: PublicityEGamesViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: PublicityEGamesData) {
            with(binding) {
                root.setOnClickListener {
                    publicityAdapterListener.onGoThirdGamesListener(data.thirdDictValues)
                }
            }
        }
    }

    inner class PreloadViewHolder(val binding: ViewLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

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

    inner class UndefinedViewHolder(itemView: View) :
        BaseItemListenerViewHolder(itemView, publicityAdapterListener)
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
    fun removeData(src: Any?) {
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
        private val onGoNewsPageListener: () -> Unit,
        private val onGoLoginListener: () -> Unit,
        private val onGoRegisterListener: () -> Unit,
        private val onGoDepositListener: () -> Unit,
        private val onGoWithdrawListener: () -> Unit,
        private val onGoThirdGamesListener: (thirdDictValues: ThirdDictValues?) -> Unit,
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
        fun onGoNewsPageListener() = onGoNewsPageListener.invoke()
        fun onGoLoginListener() = onGoLoginListener.invoke()
        fun onGoRegisterListener() = onGoRegisterListener.invoke()
        fun onGoDepositListener() = onGoDepositListener.invoke()
        fun onGoWithdrawListener() = onGoWithdrawListener.invoke()
        fun onGoThirdGamesListener(thirdDictValues: ThirdDictValues?) =
            onGoThirdGamesListener.invoke(thirdDictValues)

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

        fun onClickLiveIconListener(
            gameType: String,
            matchType: MatchType?,
            matchId: String?,
            matchInfoList: List<MatchInfo>
        ) = onClickLiveIconListener.invoke(gameType, matchType, matchId, matchInfoList)

        fun onClickAnimationIconListener(
            gameType: String,
            matchType: MatchType?,
            matchId: String?,
            matchInfoList: List<MatchInfo>
        ) = onClickAnimationIconListener.invoke(gameType, matchType, matchId, matchInfoList)
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