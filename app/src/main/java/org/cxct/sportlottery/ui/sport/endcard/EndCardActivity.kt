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
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.ui.sport.endcard.bet.EndCardGameFragment
import org.cxct.sportlottery.ui.sport.endcard.home.EndCardHomeFragment
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardGuideDialog
import org.cxct.sportlottery.ui.sport.endcard.home.EndCardRuleFragment
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordDetailFragment
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeGradientOrientation
import splitties.fragments.addToBackStack

class EndCardActivity: BaseSocketActivity<EndCardVM, ActivityEndcardBinding>(EndCardVM::class) {
    override fun pageName() = "篮球末尾比分专题页面"
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
        setStatusbar(R.color.color_1A2C38,false)
        binding.toolbar.attach(this, { finish() })
        initTab()
        showHome()
        checkGuide()
        initObservable()
        binding.vShadow.background = ShapeDrawable()
            .setSolidColor(getColor(R.color.transparent_black_20), Color.TRANSPARENT)
            .setSolidGradientOrientation(ShapeGradientOrientation.TOP_TO_BOTTOM)
    }

    private fun initObservable() {
        bindSportMaintenance()
    }

    private fun initTab() = binding.run {
        llRecord.clickDelay { loginedRun(this@EndCardActivity){
            showBetRecord()
        } }
        llMain.clickDelay { showHome() }
        llCasino.clickDelay { finish() }
        llPromtion.clickDelay {
            startActivity(PromotionListActivity::class.java)
            finish()
        }
    }

    private fun showHome() {
        fragmentHelper.showFragment(0)
        binding.tvEndCard.setTextColor(getColor(R.color.color_2B7DFF))
        binding.tvEndCard.typeface = AppFont.helvetica_bold
        binding.tvBetRecord.setTextColor(Color.WHITE)
        binding.tvBetRecord.typeface = AppFont.helvetica
        binding.ivEndCard.setImageResource(R.drawable.ic_endcard_tab_main_1)
        binding.ivBetRecord.setImageResource(R.drawable.ic_endcard_tab_betrecord)
    }

    fun showBetRecord(recordPosition: Int?=null) {
        fragmentHelper.showFragment(1)
        recordPosition?.let {
            (fragmentHelper.getCurrentFragment() as EndCardRecordFragment).showPage(it)
        }
        binding.tvEndCard.setTextColor(Color.WHITE)
        binding.tvEndCard.typeface = AppFont.helvetica
        binding.tvBetRecord.setTextColor(getColor(R.color.color_2B7DFF))
        binding.tvBetRecord.typeface = AppFont.helvetica_bold
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