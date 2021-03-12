package org.cxct.sportlottery.ui.vip

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.stx.xhb.androidx.transformers.Transformer
import kotlinx.android.synthetic.main.activity_vip.*
import kotlinx.android.synthetic.main.content_common_bottom_sheet_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.network.user.info.UserInfoData
import org.cxct.sportlottery.network.vip.growth.GROWTH_CONFIG_BET_ID
import org.cxct.sportlottery.network.vip.growth.GROWTH_CONFIG_RECHARGE_ID
import org.cxct.sportlottery.network.vip.growth.GrowthConfig
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.util.TextUtil

class VipActivity : BaseOddButtonActivity<VipViewModel>(VipViewModel::class) {

    private val levelBubbleList by lazy { listOf<TextView>(bubble_level_one, bubble_level_two, bubble_level_three, bubble_level_four, bubble_level_five, bubble_level_six) }

    private val thirdRebatesAdapter by lazy { ThirdRebatesAdapter() }

    private val thirdGameAdapter by lazy {
        ThirdGameAdapter(this, OnSelectThirdGames {
            sv_third_games.apply {
                setRebatesFormGame(it)
                dismiss()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vip)

        initView()
        initObserve()
        initEvent()
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

    private fun initView() {
        getDataFromApi()

        pb_user_level.max = levelBubbleList.size

        banner_vip_level.setPageTransformer(Transformer.Default)

        //第三方遊戲反水選擇列
        sv_third_games.apply {
            selectedTextColor = R.color.colorBlack
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
        sv_third_games.selectedText = gameFirmValues.firmName
        viewModel.getThirdRebates(gameFirmValues.firmCode ?: "", gameFirmValues.firmType ?: "")
    }

    private fun userInfoUpdateView(userInfo: UserInfoData) {
        setupViewByUserInfo(userInfo)
        updateUserLevel(userInfo.userLevelId)
        updateUserGrowthBar(userInfo)
        updateNextLevelTips(userInfo)
    }

    private fun updateNextLevelTips(userInfo: UserInfoData) {
        getNextLevel(userInfo.userLevelId)?.let { nextLevel ->
            tv_next_level_tips.text = "${getString(nextLevel.levelRequirement.level)}  ${nextLevel.levelRequirement.levelName}"
        }
    }

    private fun updateUserGrowthBar(userInfo: UserInfoData) {
        if (verifyMaxLevel(userInfo.userLevelId)) {
            pb_user_growth.apply {
                max = 1
                progress = 1
            }
            return
        }
        val growthRequirement = getUpgradeGrowthRequirement(userInfo.userLevelId)
        val userGrowth = userInfo.growth?.toInt() ?: 0
        val nextLevelRequirement = (growthRequirement - userGrowth).let { if (it < 0) 0 else it }
        pb_user_growth.apply {
            max = growthRequirement
            progress = userGrowth
        }
        tv_requirement_amount.text = TextUtil.format(nextLevelRequirement.toDouble())
    }

    private fun verifyMaxLevel(levelId: Int): Boolean {
        return Level.values().last() === Level.values().find { level -> level.levelRequirement.levelId == levelId }
    }

    private fun getUpgradeGrowthRequirement(levelId: Int): Int {
        return getNextLevel(levelId)?.levelRequirement?.growthRequirement ?: 0
    }

    private fun getNextLevel(levelId: Int): Level? {
        return Level.values().find { level -> level.levelRequirement.levelId == levelId + 1 }
    }

    private fun setupViewByUserInfo(userInfo: UserInfoData) {
        userInfo.let { user ->
            tv_greet.text = user.nickName
        }
    }

    private fun updateUserLevel(levelId: Int) {
        Level.values().find { it.levelRequirement.levelId == levelId }?.apply {
            tv_vip_name.text = levelRequirement.levelName
            iv_vip.setImageDrawable(ContextCompat.getDrawable(this@VipActivity, levelRequirement.levelTitleIcon))
            updateLevelBar(ordinal)
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
        val rechargeGrowthHint = growthConfigs.find { it.id == GROWTH_CONFIG_RECHARGE_ID }?.growth
        val betGrowthHint = growthConfigs.find { it.id == GROWTH_CONFIG_BET_ID }?.growth

        tv_hint_recharge_growth.text = String.format(getString(R.string.hint_recharge_growth), rechargeGrowthHint)
        tv_hint_bet_growth.text = String.format(getString(R.string.hint_bet_growth), betGrowthHint)
    }

    private fun setupBannerData() {
        banner_vip_level.apply {
            setBannerData(R.layout.item_banner_member_level, setupBannerLevelRequirement())
            loadImage { _, model, view, _ ->
                val tvLevel: TextView? = view?.findViewById(R.id.tv_level)
                val ivLevel: ImageView? = view?.findViewById(R.id.iv_level_icon)
                val tvLevelName: TextView? = view?.findViewById(R.id.tv_level_name)
                val tvGrowthRequirement: TextView? = view?.findViewById(R.id.tv_growth_requirement)
                val cardInfo = (model as BannerLevelCard).xBannerUrl

                tvLevel?.text = getString(cardInfo.level)
                ivLevel?.setImageDrawable(ContextCompat.getDrawable(this@VipActivity, cardInfo.levelIcon))
                tvLevelName?.text = cardInfo.levelName
                tvGrowthRequirement?.text = getGrowthRequirementTips(cardInfo.growthRequirement)
            }
        }
    }

    private fun getGrowthRequirementTips(requirement: Int?): String {
        return when {
            requirement == null -> ""
            (requirement <= 1) -> {
                getString(R.string.no_value)
            }
            else -> "$requirement ${getString(R.string.level_requirement_unit)}"
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

class ThirdGameAdapter(private val context: Context, private val selectedListener: OnSelectThirdGames) :
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
        fun bind(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, dataList: List<GameFirmValues>, position: Int, selectedListener: OnSelectThirdGames) {
            val data = dataList[position]
            val itemChecked = dataCheckedList[position]
            itemView.apply {
                checkbox_item.text = data.firmName
                checkbox_item.background = if (itemChecked) ContextCompat.getDrawable(context, R.color.colorWhite6) else ContextCompat.getDrawable(context, android.R.color.white)
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