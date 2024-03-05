package org.cxct.sportlottery.ui.sport.endcard

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityEndcardBinding
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.sport.SportFragment
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardBetDialog
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordDetailFragment
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordFragement
import org.cxct.sportlottery.ui.sport.esport.ESportFragment
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.Param
import splitties.bundle.put
import splitties.fragments.addToBackStack
import java.lang.ref.WeakReference

class EndCardActivity: BaseActivity<EndCardVM, ActivityEndcardBinding>() {

    private var gameFragment: WeakReference<EndCardGameFragment>? = null
    private var recordDetailFragment: WeakReference<EndCardRecordDetailFragment>? = null
    private val betDialog by lazy { EndCardBetDialog().apply {
        onDismissListener={
             binding.parlayFloatWindow.visible()
        }
    } }

    private val fragmentHelper by lazy {
        FragmentHelper(supportFragmentManager,
            R.id.llContent,
            arrayOf(
                Param(EndCardHomeFragment::class.java),
                Param(EndCardRecordFragement::class.java, needRemove = true)
            ))
    }

    override fun onInitView() {
        setStatusbar(R.color.color_1B2436,false)
        binding.toolbar.attach(this, { finish() }, viewModel)
        initTab()
        showHome()
    }

    private fun initTab() = binding.run {
        parlayFloatWindow.onViewClick = {
            betDialog.show(supportFragmentManager)
            parlayFloatWindow.gone()
        }
        llRecord.setOnClickListener { showBetRecord() }
        llMain.setOnClickListener { showHome() }
    }

    private fun showHome() {
        fragmentHelper.showFragment(0)
        binding.tvEndCard.setTextColor(getColor(R.color.color_2B7DFF))
        binding.tvBetRecord.setTextColor(Color.WHITE)
        binding.ivEndCard.setImageResource(R.drawable.ic_endcard_tab_main_1)
        binding.ivBetRecord.setImageResource(R.drawable.ic_endcard_tab_betrecord)
    }

    private fun showBetRecord() {
        fragmentHelper.showFragment(1)
        binding.tvEndCard.setTextColor(Color.WHITE)
        binding.tvBetRecord.setTextColor(getColor(R.color.color_2B7DFF))
        binding.ivEndCard.setImageResource(R.drawable.ic_endcard_tab_main)
        binding.ivBetRecord.setImageResource(R.drawable.ic_endcard_tab_betrecord_1)
    }

    private fun showEndCardGame() {
        var endCardGameFragment = gameFragment?.get()
        val fragment: EndCardGameFragment
        if (endCardGameFragment == null) {
            fragment = EndCardGameFragment()
            gameFragment = WeakReference(fragment)
        } else {
            fragment = endCardGameFragment
        }

        fragment.arguments = Bundle()
        supportFragmentManager.beginTransaction()
            .add(R.id.frContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
     fun showRecordDetail(orderId: String) {
        var endCardRecordDetailFragment = recordDetailFragment?.get()
        val fragment: EndCardRecordDetailFragment
        if (endCardRecordDetailFragment == null) {
            fragment = EndCardRecordDetailFragment()
            recordDetailFragment = WeakReference(fragment)
        } else {
            fragment = endCardRecordDetailFragment
        }

        fragment.arguments = Bundle().apply { put("orderId",orderId) }
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

    open fun getBetListPageVisible(): Boolean {
        return betDialog.isVisible
    }

    private var betListCount = 0

    fun updateBetListCount(num: Int) {
        betListCount = num
        setupBetBarVisiblity()
        binding.parlayFloatWindow.updateCount(betListCount.toString())
        if (num > 0) viewModel.getMoneyAndTransferOut()
    }

    private fun setupBetBarVisiblity() {
        val needShowBetBar = fragmentHelper.getCurrentPosition() >= 0
                && (fragmentHelper.getCurrentFragment() is HomeFragment
                || fragmentHelper.getCurrentFragment() is SportFragment
                || fragmentHelper.getCurrentFragment() is ESportFragment)

        if (betListCount == 0
            || !needShowBetBar
            || BetInfoRepository.currentBetType == BetListFragment.SINGLE) {
            binding.parlayFloatWindow.gone()
            return
        }

        if (BetInfoRepository.currentBetType == BetListFragment.PARLAY) {
            binding.parlayFloatWindow.setBetText(getString(R.string.conspire))
            binding.parlayFloatWindow.updateCount(betListCount.toString())
        } else {
            binding.parlayFloatWindow.setBetText(getString(R.string.bet_slip))
        }
        binding.parlayFloatWindow.visible()
    }

    fun backMainHome() {
        finish()
    }
}