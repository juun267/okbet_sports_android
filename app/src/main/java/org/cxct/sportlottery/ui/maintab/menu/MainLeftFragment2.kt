package org.cxct.sportlottery.ui.maintab.menu

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentMainLeft2Binding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.user.UserInfo

import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui2.base.BindingFragment
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui2.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui2.maintab.home.news.NewsHomeFragment
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class MainLeftFragment2 : BindingFragment<MainViewModel, FragmentMainLeft2Binding>() {


    private inner class MenuItem(val group: View,
                                      val icon: ImageView,
                                      val tvName: TextView,
                                      val ivIndicator: ImageView?,
                                      val needClose: Boolean,
                                      val click: (() -> Unit)? = null) {

        var onSetSelected: (() -> Unit)? = null
        var onClearSelected: (() -> Unit)? = null

        init {

            click?.let {
                group.setOnClickListener {
                    click.invoke()
                    if (needClose) {
                        close()
                    }
                }
            }
        }

        fun clearSelected() {
            if (lastItem == this) {
                lastItem = null
            }

            onClearSelected?.let {
                it.invoke()
                return
            }

            group.isSelected = false
            icon.isSelected = false
            ivIndicator?.gone()
            tvName.typeface = Typeface.DEFAULT
            tvName.setTextColor(icon.context.resources.getColor(R.color.color_6D7693))


        }

        fun setSelected() {
            if (lastItem == this) {
                return
            } else if (lastItem != null) {
                lastItem!!.clearSelected()
            }
            lastItem = this
            onSetSelected?.let {
                it.invoke()
                return
            }

            group.isSelected = true
            icon.isSelected = true
            tvName.typeface = Typeface.DEFAULT_BOLD
            tvName.setTextColor(icon.context.resources.getColor(R.color.color_025BE8))
            ivIndicator?.visible()
        }
    }

    private fun getIconSelector(selected: Int, unSelected: Int): Drawable {
        val selectDrawable = resources.getDrawable(selected)
        val unSelecteDrawable = resources.getDrawable(unSelected)
        return DrawableCreator.Builder()
            .setSelectedDrawable(selectDrawable)
            .setUnSelectedDrawable(unSelecteDrawable)
            .setPressedDrawable(selectDrawable)
            .setUnPressedDrawable(unSelecteDrawable)
            .build()
    }

    private fun addMenu(index: Int,
                        params: LayoutParams,
                        iconParams: LayoutParams,
                        selectedIcon: Int,
                        unSelectedIcon: Int,
                        textParams: LayoutParams,
                        text: Int,
                        hasIndicator: Boolean = false,
                        needClose: Boolean = true,
                        click: (() -> Unit)? = null): MenuItem {

        val linearLayout = LinearLayout(context)
        linearLayout.gravity = Gravity.CENTER_VERTICAL
        linearLayout.setBackgroundResource(R.drawable.selector_cor30_tran_blue)
        binding.llMenuRoot.addView(linearLayout, index, params)

        val icon = AppCompatImageView(linearLayout.context)
        icon.setImageDrawable(getIconSelector(selectedIcon, unSelectedIcon))
        linearLayout.addView(icon, iconParams)

        val textView = AppCompatTextView(linearLayout.context)
        textView.textSize = 14f
        textView.setText(text)
        linearLayout.addView(textView, textParams)

        var imageView: ImageView? = null
        if (hasIndicator) {
            imageView = AppCompatImageView(linearLayout.context)
            imageView.setImageResource(R.drawable.ic_selected_indicator)
            imageView.gone()
            linearLayout.addView(imageView)
        }

        return MenuItem(linearLayout, icon, textView, imageView, needClose, click)
    }

    private inline fun getMainTabActivity() = activity as MainTabActivity

    private var lastItem: MenuItem? = null

    private lateinit var sportsItem: MenuItem
    private lateinit var okGamesItem: MenuItem
    private lateinit var promotionItem: MenuItem
    private lateinit var affiliateItem: MenuItem
    private lateinit var newsItem: MenuItem
    private lateinit var serviceItem: MenuItem
    private lateinit var languageItem: MenuItem
    private lateinit var scanItem: MenuItem

    // 新增菜单在这里修改
    private fun initMenuItem() = binding.run {

        val hMargin = 28.dp
        val groupParams = LayoutParams(-1, 40.dp)
        groupParams.topMargin = 4.5f.dp
        groupParams.leftMargin = hMargin
        groupParams.rightMargin = hMargin
        val iconParams = 20.dp.let { LayoutParams(it, it) }
        iconParams.leftMargin = 12.dp
        val textParams = LayoutParams(0, -2, 1f)
        textParams.leftMargin = iconParams.leftMargin

        sportsItem = addMenu(0,
            groupParams,
            iconParams,
            R.drawable.ic_main_menu_sports_1,
            R.drawable.ic_main_menu_sports_0,
            textParams,
            R.string.B001,
            true
        ) {
            //检查是否关闭入口
            checkSportStatus(requireActivity()){
                getMainTabActivity().jumpToEarlySport()
            }
        }

        okGamesItem = addMenu(1,
            groupParams,
            iconParams,
            R.drawable.ic_main_menu_okgames_1,
            R.drawable.ic_main_menu_okgames_0,
            textParams,
            R.string.J203,
            true
        ) { getMainTabActivity().jumpToOKGames() }

        var index = binding.llMenuRoot.indexOfChild(divider1)
        promotionItem = addMenu(++index,
            groupParams,
            iconParams,
            R.drawable.ic_main_menu_promo_1,
            R.drawable.ic_main_menu_promo_0,
            textParams,
            R.string.B005,
        )
        promotionItem.group.bindPromoClick { close() }

        affiliateItem = addMenu(++index,
            groupParams,
            iconParams,
            R.drawable.ic_main_menu_affiliate_1,
            R.drawable.ic_main_menu_affiliate_0,
            textParams,
            R.string.B015,
        ) {
            JumpUtil.toInternalWeb(
                binding.root.context,
                Constants.getAffiliateUrl(binding.root.context),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }

        newsItem = addMenu(++index,
            groupParams,
            iconParams,
            R.drawable.ic_main_menu_news_0,
            R.drawable.ic_main_menu_news_0,
            textParams,
            R.string.N909,
            hasIndicator = true
        ) {
            getMainTabActivity().jumpToNews()
        }

        serviceItem = addMenu(++index,
            groupParams,
            iconParams,
            R.drawable.ic_main_menu_livesupport_1,
            R.drawable.ic_main_menu_livesupport_0,
            textParams,
            R.string.LT050,
        )
        serviceItem.group.setServiceClick(childFragmentManager) { close() }

        val group2Params = LayoutParams(-1, 40.dp)
        group2Params.leftMargin = hMargin
        group2Params.rightMargin = hMargin
        group2Params.topMargin = 6.dp

        val toRight = resources.getDrawable(R.drawable.ic_arrow_gray_right)
        toRight.setTint(resources.getColor(R.color.color_6D7693))
        initLanguageItem(group2Params, iconParams, textParams, toRight)

        scanItem = addMenu(binding.llMenuRoot.indexOfChild(divider3) + 1,
            group2Params,
            iconParams,
            R.drawable.ic_main_menu_scan_0,
            R.drawable.ic_main_menu_scan_0,
            textParams,
            R.string.scan,
            true,
            false
        ) {
        }
        scanItem.onClearSelected = { scanItem.ivIndicator?.visible() }
        scanItem.ivIndicator?.let {
            it.visible()
            it.setImageDrawable(toRight)
        }
    }

    private fun initLanguageItem(group2Params: LayoutParams,
                                 iconParams: LayoutParams,
                                 textParams: LayoutParams,
                                 toRight: Drawable) = binding.run {
        languageItem = addMenu(binding.llMenuRoot.indexOfChild(rvLanguage),
            group2Params,
            iconParams,
            R.drawable.ic_main_menu_language_1,
            R.drawable.ic_main_menu_language_0,
            textParams,
            R.string.M169,
            true,
            false
        ) {
            if (rvLanguage.isVisible) languageItem.clearSelected() else languageItem.setSelected()
        }

        val toUp = resources.getDrawable(R.drawable.ic_arrow_gray_right)
        toUp.setTint(resources.getColor(R.color.color_025BE8))
        languageItem.ivIndicator?.setImageDrawable(toRight)
        languageItem.ivIndicator?.visible()

        languageItem.onClearSelected = {
            rvLanguage.isVisible = false
            (divider3.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 6.dp
            languageItem.ivIndicator?.rotation = 0f
            languageItem.ivIndicator?.setImageDrawable(toRight)
        }

        languageItem.onSetSelected = {
            showLanguageList()
            (divider3.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
            languageItem.ivIndicator?.rotation = -90f
            languageItem.ivIndicator?.setImageDrawable(toUp)
        }
    }

    private fun close() {
        getMainTabActivity().closeDrawerLayout()
    }

    private var currentContent: Class<BaseFragment<*>>? = null

    fun openWithFragment(menuContentFragment: Class<BaseFragment<*>>?) {
        val isSame = currentContent == menuContentFragment
        currentContent = menuContentFragment
        if (menuContentFragment == null) {
            lastItem?.clearSelected()
            return
        }

        if (isSame || !::okGamesItem.isInitialized) {
            return
        }

        binSelected()
    }


    private fun binSelected() = when(currentContent) {
        OKGamesFragment::class.java -> okGamesItem.setSelected()
        NewsHomeFragment::class.java -> newsItem.setSelected()

        else -> {}

    }

    override fun onInitView(view: View) {
        initMenuItem()
        initView()
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
        promotionItem.group.setVisibilityByMarketSwitch()
        bindVerifyStatus(UserInfoRepository.userInfo.value)
        binSelected()
    }

    private fun initView() = binding.run {
        llVerify.setOnClickListener { loginedRun(it.context) { startActivity(VerifyIdentityActivity::class.java) } }
        ivClose.setOnClickListener { close() }
        ivHome.setOnClickListener {
            getMainTabActivity().backMainHome()
            close()
        }
    }

    private fun showLanguageList() {
        binding.rvLanguage.visible()
        if (binding.rvLanguage.adapter != null) {
            return
        }

        val languageAdapter = LanguageAdapter(LanguageManager.makeUseLanguage())
//        languageItem.tvName.text = LanguageManager.getLanguageStringResource(context)
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
                setVerify(false, false, R.string.kyc_passed, resources.getColor(R.color.color_1CD219))

            }
            ProfileActivity.VerifiedType.NOT_YET.value -> {
                setVerify(true, true, R.string.kyc_unverified, resources.getColor(R.color.color_6D7693))
            }
            ProfileActivity.VerifiedType.VERIFYING.value -> {
                setVerify(false, false, R.string.kyc_unverifing, resources.getColor(R.color.color_6D7693))

            }
            else -> {
                setVerify(true, true, R.string.kyc_unverified, resources.getColor(R.color.color_6D7693))
            }
        }
    }

    private inline fun setVerify(enable: Boolean, clickAble: Boolean, text: Int, statusColor: Int) = binding.run  {
        llVerify.isEnabled = enable
        llVerify.isClickable = clickAble
        tvVerifyStatus.setText(text)
        binding.tvVerifyStatus.setTextColor(statusColor)
        binding.ivVerifyIndicator.isVisible = enable
    }

}