package org.cxct.sportlottery.ui.vip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stx.xhb.androidx.transformers.Transformer
import kotlinx.android.synthetic.main.activity_vip.*
import kotlinx.android.synthetic.main.content_common_bottom_sheet_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.network.user.info.UserInfoData
import org.cxct.sportlottery.network.vip.growth.GrowthConfig
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.ScreenUtil
import org.cxct.sportlottery.util.TextUtil

class VipActivity : BaseSocketActivity<VipViewModel>(VipViewModel::class) {

    private val levelBubbleList by lazy {
        listOf<TextView>(
            bubble_level_one,
            bubble_level_two,
            bubble_level_three,
            bubble_level_four,
            bubble_level_five,
            bubble_level_six
        )
    }

    private val thirdRebatesAdapter by lazy { ThirdRebatesAdapter() }

    private val thirdGameAdapter by lazy {
        ThirdGameAdapter(OnSelectThirdGames {
            sv_third_games.apply {
                setRebatesFormGame(it)
                dismiss()
            }
        })
    }

    private var userVipLevel: Level? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vip)

        initView()
        initObserve()
        initEvent()
        updateThirdUI()
    }

    private fun initObserve() {
        viewModel.apply {
            //讀取資料
            loadingResult.observe(this@VipActivity, Observer {
                it.apply {
                    when {
                        !userInfoLoading && !userGrowthLoading && !thirdRebatesLoading -> hideLoading()
                        else -> loading()
                    }
                }
            })
            //獲取用戶資料
            userInfoResult.observe(this@VipActivity, Observer {
                it.userInfoData?.let { data -> userInfoUpdateView(data) }
            })
            //會員層級成長值
            userLevelGrowthResult.observe(this@VipActivity, Observer {
                it.config?.growthConfigs?.let { growthConfigs ->
                    setupGrowthHint(growthConfigs)
                }
                setupBannerData()
            })
            //第三方遊戲列表
            getThirdGamesFirmMap.observe(this@VipActivity, Observer {
                thirdGameAdapter.dataList = it

                setRebatesFormGame(it.first())
            })
            //第三方遊戲各會員層級資料
            thirdRebatesReformatDataList.observe(this@VipActivity, Observer {
                thirdRebatesAdapter.dataList = it
            })
        }
    }

    private fun updateThirdUI() {
        ll_third_table.visibility = if (sConfigData?.thirdOpen != FLAG_OPEN) View.GONE else View.VISIBLE
    }

    private fun initView() {
        getDataFromApi()

        pb_user_level.max = levelBubbleList.size

        banner_vip_level.setPageTransformer(Transformer.Default)

        //第三方遊戲反水選擇列
        sv_third_games.apply {
            selectedTextColor = R.color.color_FFFFFF_000000
            setAdapter(thirdGameAdapter)
        }

        //第三方遊戲反水列表
        rv_third_rebates.adapter = thirdRebatesAdapter
    }

    private fun getDataFromApi() {
        viewModel.apply {
            getThirdGamesFirmMap()
            getUserLevelGrowth()
        }
    }

    private fun initEvent() {
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun setRebatesFormGame(gameFirmValues: GameFirmValues) {
        sv_third_games.selectedText = gameFirmValues.firmShowName
        viewModel.getThirdRebates(gameFirmValues.firmCode ?: "", gameFirmValues.firmType ?: "")
    }

    private fun userInfoUpdateView(userInfo: UserInfoData) {
        updateUserVipLevel(userInfo.testFlag, userInfo.userLevelId)
        setupViewByUserInfo(userInfo)
        updateUserLevelBlock(userInfo)
        updateUserGrowthBar(userInfo)
    }

    private fun updateUserGrowthBar(userInfo: UserInfoData) {
        when (StaticData.getTestFlag(userInfo.testFlag)) {
            TestFlag.GUEST -> {
                tv_requirement_amount.text = String.format(getString(R.string.next_level_tips), "1")
            }
            else -> {
                if (verifyMaxLevel()) {
                    tv_requirement_amount.text = getString(R.string.level_max)
                    return
                }
                val growthRequirement = getUpgradeGrowthRequirement()
                val userGrowth = userInfo.growth?.toInt() ?: 0
                val nextLevelRequirement = (growthRequirement - userGrowth).let { if (it < 0) 0 else it }.toLong()
                tv_requirement_amount.text = String.format(getString(R.string.next_level_tips), nextLevelRequirement)
            }
        }
    }

    private fun verifyMaxLevel(): Boolean {
        return Level.values().last() === userVipLevel
    }

    private fun getUpgradeGrowthRequirement(): Int {
        return getNextLevel()?.levelRequirement?.growthRequirement ?: 0
    }

    private fun getNextLevel(): Level? {
        val nextLevelId = userVipLevel?.name?.let { Level.valueOf(it).levelRequirement.levelId?.plus(1) }
        return Level.values().find { level -> level.levelRequirement.levelId == nextLevelId }
    }

    private fun setupViewByUserInfo(userInfo: UserInfoData) {
        userInfo.let { user ->
            when (StaticData.getTestFlag(userInfo.testFlag)) {
                TestFlag.GUEST -> {
                    tv_greet.text =
                        if ((user.fullName ?: "").isNotEmpty()) user.fullName else (TextUtil.maskUserName(user.fullName
                            ?: ""))
                }
                else -> {
                    tv_greet.text =
                        if (user.nickName.isNotEmpty()) user.nickName else (TextUtil.maskUserName(user.userName))
                }
            }
        }
    }

    private fun updateUserVipLevel(testFlag: Long, levelId: Int) {
        userVipLevel = when (StaticData.getTestFlag(testFlag)) {
            TestFlag.GUEST -> null
            else -> {
                Level.values().find { it.levelRequirement.levelId == levelId }
            }
        }
    }

    private fun updateUserLevelBlock(userInfo: UserInfoData) {
        userVipLevel?.apply {
            when (StaticData.getTestFlag(userInfo.testFlag)) {
                TestFlag.GUEST -> tv_vip_name.text = ""
                else -> tv_vip_name.text = levelRequirement.levelName
            }
            iv_vip.setImageDrawable(
                ContextCompat.getDrawable(
                    this@VipActivity,
                    levelRequirement.levelTitleIcon
                )
            )
            updateLevelBar(ordinal)
            banner_vip_level.bannerCurrentItem = ordinal
        }
    }
    private fun updateLevelBar(levelIndex: Int) {
        //0: VIP1, 1: VIP2 ...
        val level = levelIndex + 1
        pb_user_level.progress = level
        runOnUiThread {
            levelBubbleList.forEachIndexed { index, textView ->
                textView.isSelected = index == levelIndex
                textView.requestLayout()
            }
        }
    }

    private fun setupGrowthHint(growthConfigs: List<GrowthConfig>) {
        growthConfigs.firstOrNull()?.growth?.let { growth ->
            growthConfigs.firstOrNull()?.unit?.let { unit ->
                tv_hint_recharge_growth.text = String.format(getString(R.string.hint_recharge_growth), unit, sConfigData?.systemCurrency, growth)
            }
        }

        growthConfigs.getOrNull(1)?.growth?.let { growth ->
            growthConfigs.getOrNull(1)?.unit?.let { unit ->
                tv_hint_bet_growth.text = String.format(getString(R.string.hint_bet_growth), unit, sConfigData?.systemCurrency, growth)
            }
        }
    }

    private fun setupBannerData() {
        banner_vip_level.apply {
            val layoutParams: android.widget.LinearLayout.LayoutParams =
                android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    (ScreenUtil.getScreenWidth(this@VipActivity) / 2.5).toInt())
            setLayoutParams(layoutParams)
            setBannerData(R.layout.item_banner_member_level, setupBannerLevelRequirement())
            loadImage { _, model, view, _ ->
                val tvLevel: TextView? = view?.findViewById(R.id.tv_level)
                val ivLevel: ImageView? = view?.findViewById(R.id.iv_level_icon)
                val tvLevelName: TextView? = view?.findViewById(R.id.tv_level_name)
                val tvGrowthRequirement: TextView? = view?.findViewById(R.id.tv_growth_requirement)
                val cardInfo = (model as BannerLevelCard).xBannerUrl

                tvLevel?.text = getString(cardInfo.level)
                ivLevel?.setImageDrawable(ContextCompat.getDrawable(this@VipActivity, cardInfo.levelIcon))
                tvLevelName?.text = cardInfo.levelName //TODO Bill 這裡要請API改成多語系
                tvGrowthRequirement?.text = getGrowthRequirementTips(cardInfo.levelId, cardInfo.growthRequirement)
            }
            bannerCurrentItem = userVipLevel?.ordinal?.plus(1) ?: 0
        }
    }

    private fun getGrowthRequirementTips(level: Int?, requirement: Int?): String {
        return when {
            level == 2 -> {
                getString(R.string.no_value)
            }
            requirement == null -> ""
            else -> "$requirement"
        }
    }

    /**
     * 設置會員層級資料至BannerItem
     */
    private fun setupBannerLevelRequirement(): List<BannerLevelCard> {
        return mutableListOf<BannerLevelCard>().apply {
            Level.values().forEach {
                this.add(BannerLevelCard(it.levelRequirement))
            }
        }
    }
}

class ThirdGameAdapter(private val selectedListener: OnSelectThirdGames) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        var dataCheckedList = mutableListOf<Boolean>()
        var selectedPosition = 0
    }

    var dataList = listOf<GameFirmValues>()
        set(value) {
            field = value
            dataCheckedList = MutableList(value.size) { it == 0 }
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ThirdGamesItemViewHolder.form(parent)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ThirdGamesItemViewHolder -> {
                holder.bind(this, dataList, position, selectedListener)
            }
        }
    }

    class ThirdGamesItemViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            dataList: List<GameFirmValues>,
            position: Int,
            selectedListener: OnSelectThirdGames,
        ) {
            val data = dataList[position]
            val itemChecked = dataCheckedList[position]
            itemView.apply {
                checkbox_item.text = data.firmShowName
                checkbox_item.background = if (itemChecked) ContextCompat.getDrawable(context,
                    R.color.color_191919_EEEFF0) else ContextCompat.getDrawable(context, android.R.color.white)
                checkbox_item.setOnClickListener {
                    if (selectedPosition != position) {
                        selectedListener.onSelected(data)
                        itemChecked(adapter, position)
                    }
                }
            }
        }

        private fun itemChecked(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, checkIndex: Int) {
            dataCheckedList[selectedPosition] = false
            dataCheckedList[checkIndex] = true
            adapter.apply {
                notifyItemChanged(selectedPosition)
                notifyItemChanged(checkIndex)
            }
            selectedPosition = checkIndex
        }

        companion object {
            fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val inflater = LayoutInflater.from(viewGroup.context)
                val view = inflater.inflate(R.layout.content_common_bottom_sheet_item, viewGroup, false)
                return ThirdGamesItemViewHolder(view)
            }
        }
    }

}

class OnSelectThirdGames(val selectedListener: (gameFirmValues: GameFirmValues) -> Unit) {
    fun onSelected(gameFirmValues: GameFirmValues) = selectedListener(gameFirmValues)
}