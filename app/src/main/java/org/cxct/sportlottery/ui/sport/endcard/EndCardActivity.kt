package org.cxct.sportlottery.ui.sport.endcard

import android.graphics.Color
import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityEndcardBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordFragement
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.Param
import java.lang.ref.WeakReference

class EndCardActivity: BaseActivity<EndCardVM, ActivityEndcardBinding>() {

    private var gameFragment: WeakReference<EndCardGameFragment>? = null
    private val fragmentHelper by lazy {
        FragmentHelper(supportFragmentManager,
            R.id.llContent,
            arrayOf(
                Param(EndCardHomeFragment::class.java),
                Param(EndCardRecordFragement::class.java, needRemove = true)
            ))
    }

    override fun onInitView() {
        setStatusBarDarkFont(false)
        binding.toolbar.attach(this, { finish() }, viewModel)
        initTab()
        showHome()
    }

    private fun initTab() = binding.run {
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


}