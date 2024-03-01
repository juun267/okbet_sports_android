package org.cxct.sportlottery.ui.sport.endcard

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityEndcardBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.FragmentHelper2
import java.lang.ref.WeakReference

class EndCardActivity: BaseActivity<EndCardVM, ActivityEndcardBinding>() {

    private val fragmentHelper2 by lazy { FragmentHelper2(supportFragmentManager, R.id.llContent) }
    private var gameFragment: WeakReference<EndCardGameFragment>? = null

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
        fragmentHelper2.show(EndCardHomeFragment::class.java)
    }

    private fun showBetRecord() {
        fragmentHelper2.show(EndCardBetRecordFragment::class.java)
    }

    private fun showEndCardGame() {
        var endCardGameFragment = gameFragment?.get()
        if (endCardGameFragment == null) {
            endCardGameFragment = EndCardGameFragment()
        }
        endCardGameFragment!!.let {
            it.arguments = Bundle()
            supportFragmentManager.beginTransaction()
                .add(R.id.frContainer, it)
                .addToBackStack(null)
                .commit()
        }
    }


}