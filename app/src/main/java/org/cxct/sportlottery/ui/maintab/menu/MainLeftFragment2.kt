package org.cxct.sportlottery.ui.maintab.menu

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class MainLeftFragment2 : BindingFragment<MainViewModel, FragmentMainLeft2Binding>() {


    private open inner class MenuItem(val group: View,
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

        open fun clearSelected() {
            group.isSelected = false
            icon.isSelected = false
            ivIndicator?.gone()
            tvName.typeface = Typeface.DEFAULT
            tvName.setTextColor(resources.getColor(R.color.color_6D7693))
            if (lastItem == this) {
                lastItem = null
            }
            onClearSelected?.invoke()
        }

        open fun setSelected() {
            if (lastItem == this) {
                return
            } else if (lastItem != null) {
                lastItem!!.clearSelected()
            }
            group.isSelected = true
            icon.isSelected = true
            tvName.typeface = Typeface.DEFAULT_BOLD
            tvName.setTextColor(resources.getColor(R.color.color_025BE8))
            ivIndicator?.visible()
            lastItem = this
            onSetSelected?.invoke()
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
                        params: LinearLayout.LayoutParams,
                        iconParams: LinearLayout.LayoutParams,
                        selectedIcon: Int,
                        unSelectedIcon: Int,
                        textParams: LinearLayout.LayoutParams,
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

    private fun close() {
        getMainTabActivity().closeDrawerLayout()
    }

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private lateinit var languageAdapter: LanguageAdapter

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

        val p = LinearLayout.LayoutParams(-1, 40.dp)
        p.topMargin = 4.5f.dp
        val p1 = 20.dp.let { LinearLayout.LayoutParams(it, it) }
        p1.leftMargin = 12.dp
        val p2 = LinearLayout.LayoutParams(0, -2, 1f)
        p2.leftMargin = p1.leftMargin

        sportsItem = addMenu(0,
            p,
            p1,
            R.drawable.ic_main_menu_sports_1,
            R.drawable.ic_main_menu_sports_0,
            p2,
            R.string.B001,
            true
        ) { getMainTabActivity().jumpToEarlySport() }

        okGamesItem = addMenu(1,
            p,
            p1,
            R.drawable.ic_main_menu_okgames_1,
            R.drawable.ic_main_menu_okgames_0,
            p2,
            R.string.J203,
            true
        ) { getMainTabActivity().jumpToOKGames() }

        var index = binding.llMenuRoot.indexOfChild(divider1)
        promotionItem = addMenu(++index,
            p,
            p1,
            R.drawable.ic_main_menu_promo_1,
            R.drawable.ic_main_menu_promo_0,
            p2,
            R.string.B005,
        )
        promotionItem.group.bindPromoClick { close() }

        affiliateItem = addMenu(++index,
            p,
            p1,
            R.drawable.ic_main_menu_affiliate_1,
            R.drawable.ic_main_menu_affiliate_0,
            p2,
            R.string.B015,
        ) {
            JumpUtil.toInternalWeb(
                binding.root.context,
                Constants.getAffiliateUrl(binding.root.context),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }

        newsItem = addMenu(++index,
            p,
            p1,
            R.drawable.ic_main_menu_news_0,
            R.drawable.ic_main_menu_news_0,
            p2,
            R.string.N909,
        ) {  }

        serviceItem = addMenu(++index,
            p,
            p1,
            R.drawable.ic_main_menu_livesupport_1,
            R.drawable.ic_main_menu_livesupport_0,
            p2,
            R.string.LT050,
        )
        serviceItem.group.setServiceClick(childFragmentManager) { close() }

        languageItem = addMenu(binding.llMenuRoot.indexOfChild(rvLanguage),
            p,
            p1,
            R.drawable.ic_main_menu_language_1,
            R.drawable.ic_main_menu_language_0,
            p2,
            R.string.language_en,
            true,
            false
        ) {
            if (languageItem.group.isSelected) languageItem.clearSelected() else languageItem.setSelected()
        }
        languageItem.ivIndicator?.setImageResource(R.drawable.ic_arrow_gray_right)
        languageItem.ivIndicator?.visible()
        languageItem.onClearSelected = {
            rvLanguage.isVisible = false
            languageItem.ivIndicator?.visible()
            languageItem.ivIndicator?.rotation = 0f
        }

        languageItem.onSetSelected = {
            rvLanguage.isVisible = true
            languageItem.ivIndicator?.visible()
            languageItem.ivIndicator?.rotation = -90f
        }

        val scanParams = LinearLayout.LayoutParams(-1, 40.dp)
        16.dp.let {
            scanParams.topMargin = it
            scanParams.bottomMargin = it
        }
        scanItem = addMenu(binding.llMenuRoot.indexOfChild(divider3) + 1,
            scanParams,
            p1,
            R.drawable.ic_main_menu_scan_0,
            R.drawable.ic_main_menu_scan_0,
            p2,
            R.string.scan,
            true,
            false
        ) {
        }
        scanItem.onClearSelected = { scanItem.ivIndicator?.visible() }
        scanItem.ivIndicator?.visible()
        scanItem.ivIndicator?.setImageResource(R.drawable.ic_arrow_gray_right)
    }

    fun openWithOKGames() {
        if (lastItem == okGamesItem) {
            return
        }
        okGamesItem.setSelected()
    }

    override fun onInitView(view: View) {
        initMenuItem()
        initView()
    }

    override fun onBindViewStatus(view: View) {
        initLanguageList()
        initObserver()
        promotionItem.group.setVisibilityByMarketSwitch()
        bindVerifyStatus(UserInfoRepository.userInfo.value)
    }

    private fun initView() = binding.run {
        llVerify.setOnClickListener { startActivity(VerifyIdentityActivity::class.java) }
        ivClose.setOnClickListener { close() }
    }

    private fun initLanguageList() {
        languageAdapter = LanguageAdapter(LanguageManager.makeUseLanguage())
        languageItem.tvName.text = LanguageManager.getLanguageStringResource(context)
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