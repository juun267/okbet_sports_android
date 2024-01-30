package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentLeftSportGameBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.util.*

class LeftGameFragment: BaseFragment<MainViewModel, FragmentLeftSportGameBinding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity

    override fun onInitView(view: View) {
        initBanners()
        initMenuItems()
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        setBannerStatus()
        binding.menuSupport.setServiceClick(getMainTabActivity().supportFragmentManager){ close() }
    }
    private fun initBanners() = binding.run{
        cvOkLive.setOnClickListener {
            close()
            getMainTabActivity().jumpToOkLive()
        }
        cvOkGame.setOnClickListener {
            close()
            getMainTabActivity().jumpToOKGames()
        }
        cvESport.setOnClickListener {
            close()
            getMainTabActivity().jumpToESport()
        }
    }
    fun setBannerStatus() = binding.run{
        if (StaticData.okLiveOpened()){
            cvOkLive.show()
        }else{
            cvOkLive.gone()
        }
        if (StaticData.okGameOpened()){
            cvOkGame.show()
        }else{
            cvOkGame.gone()
        }
        if (StaticData.okBingoOpened()){
            cvESport.show()
            maintenESport.root.isVisible = getSportEnterIsClose()
            cvESport.isEnabled = !getSportEnterIsClose()
        }else{
            cvESport.gone()
        }
    }
    private fun initMenuItems() = binding.run {
        menuPromo.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_promo_sel, R.drawable.ic_left_menu_promo_nor),
            R.string.B005
        ){
            close()
            startActivity(PromotionListActivity::class.java)
        }.apply {
            setVisibilityByMarketSwitch()
        }
        menuAffiliate.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_affiliate_sel, R.drawable.ic_left_menu_affiliate_nor),
            R.string.B015
        ){
            close()
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(binding.root.context),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }.apply {
            setVisibilityByMarketSwitch()
        }

        menuNews.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_news_sel, R.drawable.ic_left_menu_news_nor),
            R.string.N909
        ){
            close()
            getMainTabActivity().jumpToNews()
        }

        menuSupport.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_custom_sel, R.drawable.ic_left_menu_custom_nor),
            R.string.LT050
        ).setServiceClick(getMainTabActivity().supportFragmentManager) { close() }

        menuVerify.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_verify_sel, R.drawable.ic_left_menu_verify_nor),
            R.string.N914
        ){
            close()
            loginedRun(requireContext()) { startActivity(VerifyIdentityActivity::class.java) }
        }.showBottomLine(false)
    }
    fun close() {
        getMainTabActivity().closeDrawerLayout()
    }

}