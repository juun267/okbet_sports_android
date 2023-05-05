package org.cxct.sportlottery.ui.maintab.menu

import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_main_left2.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentMainLeft2Binding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class MainLeftFragment2 : BindingFragment<MainViewModel, FragmentMainLeft2Binding>() {

    private inner class MenuItem(val group: View,
                                 val tvName: TextView,
                                 val ivIndicator: ImageView?,
                                 val click: () -> Unit) {
        init {
            group.setOnClickListener {
                if (this@MenuItem != lastItem) {
                    clearSelected()
                    click.invoke()
                }
            }
        }

        fun clearSelected() {
            group.isSelected = false
            ivIndicator?.gone()
            tvName.typeface = Typeface.DEFAULT
            tvName.setTextColor(resources.getColor(R.color.color_6D7693))
        }

        fun setSelected() {
            group.isSelected = true
            tvName.typeface = Typeface.DEFAULT_BOLD
            tvName.setTextColor(resources.getColor(R.color.color_025BE8))
            ivIndicator?.visible()
            lastItem = this
        }
    }

    private fun close() {
        getMainTabActivity().closeDrawerLayout()
    }

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private lateinit var languageAdapter: LanguageAdapter

    private val selectedBg by lazy {
        DrawableCreator.Builder()
            .setSolidColor(resources.getColor(R.color.color_025BE8))
            .setShapeAlpha(0.1f)
            .setCornersRadius(30.dp.toFloat())
            .build()
    }

    private var lastItem: MenuItem? = null
    private val okGamesItem by lazy {
        binding.run { MenuItem(llOKGames, tvOKGames, ivOKGamesIndicator) {
            getMainTabActivity().jumpToOKGames()
            close()
        } }
    }

    private val okBingosItem by lazy { binding.run { MenuItem(llOKBingo, tvBingo, ivBingoIndicatot) { getMainTabActivity().jumpToEarlySport() } } }
    private val okLiveItem by lazy { binding.run { MenuItem(llOKLive, tvOKLive, ivOKLiveIndicator) { getMainTabActivity().jumpToEarlySport() } } }
    private val newsItem by lazy { binding.run { MenuItem(llNews, tvNews, null) { getMainTabActivity().jumpToEarlySport() } } }
    private val languageItem by lazy { binding.run { MenuItem(llLanguage, tvLanguage, null) { getMainTabActivity().jumpToEarlySport() } } }

    private fun initMenuItem() = binding.run {
        okGamesItem.setSelected()
        llSports.setOnClickListener {
            resetSelected()
            close()
            getMainTabActivity().jumpToEarlySport()
        }

        llLive.setServiceClick(childFragmentManager) { close() }
        llPromo.setVisibilityByMarketSwitch()
        llPromo.bindPromoClick() // 优惠活动
        llAffliate.setOnClickListener { //代理加盟
            close()
            JumpUtil.toInternalWeb(
                llAffliate.context,
                Constants.getAffiliateUrl(llAffliate.context),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }

    }

    fun openWithOKGames() {
        resetSelected()
        okGamesItem.setSelected()
        lastItem = okGamesItem
    }

    private fun resetSelected() {
        lastItem?.clearSelected()
        lastItem = null
    }


    override fun onInitView(view: View) {
        initView()
        initMenuItem()
    }

    override fun onBindViewStatus(view: View) {
        initLanguageView()
        initObserver()
        bindVerifyStatus(UserInfoRepository.userInfo.value)
    }

    private fun initView() = binding.run {
        llVerify.setOnClickListener { startActivity(VerifyIdentityActivity::class.java) }
        ivClose.setOnClickListener { close() }
        llLanguage.setOnClickListener {
            resetSelected()
            var isSelected = !llLanguage.isSelected
            llLanguage.isSelected = isSelected
            rvLanguage.isVisible = isSelected
        }
    }

    private fun initLanguageView() {
        languageAdapter = LanguageAdapter(LanguageManager.makeUseLanguage())
        tvLanguage.text = LanguageManager.getLanguageStringResource(tvLanguage.context)
        binding.rvLanguage.layoutManager = GridLayoutManager(context, 2)
        binding.rvLanguage.adapter = languageAdapter
        languageAdapter.setOnItemClickListener { adapter, _, position ->
            viewModel.betInfoRepository.clear()
            selectLanguage(adapter.getItem(position) as LanguageManager.Language)
        }
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if (LanguageManager.getSelectLanguageName() != select.key) {
            context?.let {
                LanguageManager.saveSelectLanguage(it, select)
                MainTabActivity.reStart(it)
            }
        }
    }

    private fun initObserver() {
        viewModel.userInfo.observe(this) {
            bindVerifyStatus(userInfo = it)
        }
    }

    private fun bindVerifyStatus(userInfo: UserInfo?) = binding.run {

        llVerify.isVisible = sConfigData?.realNameWithdrawVerified.isStatusOpen()
                || sConfigData?.realNameRechargeVerified.isStatusOpen()

        when (userInfo?.verified) {
            ProfileActivity.VerifiedType.PASSED.value -> {
                llVerify.isEnabled = false
                llVerify.isClickable = false
                tvVerifyStatus.text = getString(R.string.kyc_passed)
            }
            ProfileActivity.VerifiedType.NOT_YET.value -> {
                llVerify.isEnabled = true
                llVerify.isClickable = true
                tvVerifyStatus.text = getString(R.string.kyc_unverified)

            }
            ProfileActivity.VerifiedType.VERIFYING.value -> {
                llVerify.isEnabled = false
                llVerify.isClickable = false
                tvVerifyStatus.text = getString(R.string.kyc_unverifing)

            }
            else -> {
                llVerify.isEnabled = true
                llVerify.isClickable = true
                tvVerifyStatus.text = getString(R.string.kyc_unverified)
            }
        }
    }

}