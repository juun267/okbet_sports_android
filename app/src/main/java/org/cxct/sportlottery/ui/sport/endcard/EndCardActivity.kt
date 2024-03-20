package org.cxct.sportlottery.ui.sport.endcard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityEndcardBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.ui.sport.endcard.bet.EndCardGameFragment
import org.cxct.sportlottery.ui.sport.endcard.home.EndCardHomeFragment
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardGuideDialog
import org.cxct.sportlottery.ui.sport.endcard.home.EndCardRuleFragment
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordDetailFragment
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordFragment
import org.cxct.sportlottery.util.AppManager
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.ObjectHolder
import org.cxct.sportlottery.util.Param
import splitties.fragments.addToBackStack

class EndCardActivity: BaseActivity<EndCardVM, ActivityEndcardBinding>() {

    private val fragmentHolder = ObjectHolder()

    private val fragmentHelper by lazy {
        FragmentHelper(supportFragmentManager,
            R.id.llContent,
            arrayOf(
                Param(EndCardHomeFragment::class.java),
                Param(EndCardRecordFragment::class.java)
            ))
    }

    override fun onInitView() {
        setStatusbar(R.color.color_1B2436,false)
        binding.toolbar.attach(this, { finish() }, viewModel)
        initTab()
        showHome()
        checkGuide()
        initObservable()
    }

    private fun initObservable() {
        viewModel.isRechargeShowVerifyDialog.observe(this) {
            val b = it.getContentIfNotHandled() ?: return@observe
            if (b) {
                VerifyIdentityDialog().show(supportFragmentManager, null)
            } else {
                loading()
                viewModel.checkRechargeSystem()
            }
        }

        viewModel.rechargeSystemOperation.observe(this) {
            hideLoading()
            val b = it.getContentIfNotHandled() ?: return@observe
            if (b) {
                startActivity(Intent(this, MoneyRechargeActivity::class.java))
                return@observe
            }

            showPromptDialog(
                getString(R.string.prompt),
                getString(R.string.message_recharge_maintain)
            ) {}

        }
    }

    private fun initTab() = binding.run {
        llRecord.clickDelay { showBetRecord() }
        llMain.clickDelay { showHome() }
        llCasino.clickDelay { finish() }
        llPromtion.clickDelay {
            finish()
            postDelayed(500){
                (AppManager.currentActivity() as MainTabActivity).startActivity(PromotionListActivity::class.java)
            }
        }
    }

    private fun showHome() {
        fragmentHelper.showFragment(0)
        binding.tvEndCard.setTextColor(getColor(R.color.color_2B7DFF))
        binding.tvBetRecord.setTextColor(Color.WHITE)
        binding.ivEndCard.setImageResource(R.drawable.ic_endcard_tab_main_1)
        binding.ivBetRecord.setImageResource(R.drawable.ic_endcard_tab_betrecord)
    }

    fun showBetRecord(recordPosition: Int?=null) {
        fragmentHelper.showFragment(1)
        recordPosition?.let {
            (fragmentHelper.getCurrentFragment() as EndCardRecordFragment).showPage(it)
        }
        binding.tvEndCard.setTextColor(Color.WHITE)
        binding.tvBetRecord.setTextColor(getColor(R.color.color_2B7DFF))
        binding.ivEndCard.setImageResource(R.drawable.ic_endcard_tab_main)
        binding.ivBetRecord.setImageResource(R.drawable.ic_endcard_tab_betrecord_1)
    }

    fun showEndCardGame(matchInfo: MatchInfo) {
        val endCardGameFragment = fragmentHolder.make(EndCardGameFragment::class.java)
        endCardGameFragment.arguments = Bundle().apply { putParcelable("matchInfo", matchInfo) }
        showFragment(endCardGameFragment)
    }

     fun showRecordDetail(data: Row) {
         val fragment = fragmentHolder.make(EndCardRecordDetailFragment::class.java)
         fragment.arguments = Bundle().apply { putParcelable("data",data) }
         showFragment(fragment)
    }

    fun showEndCardRule() {
        val fragment = fragmentHolder.make(EndCardRuleFragment::class.java)
        showFragment(fragment)
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.frContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun removeFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .remove(fragment)
            .addToBackStack()
            .commit()
    }

    fun checkGuide(){
        if(KvUtils.decodeBoolean(KvUtils.KEY_ENDCARD_GUIDE))
            return
        KvUtils.put(KvUtils.KEY_ENDCARD_GUIDE,true)
        EndCardGuideDialog().show(supportFragmentManager)
    }
}