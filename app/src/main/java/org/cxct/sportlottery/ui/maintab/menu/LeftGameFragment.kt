package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.core.view.isVisible
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.FragmentLeftSportGameBinding
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.getSportEnterIsClose

class LeftGameFragment: BindingFragment<MainViewModel,FragmentLeftSportGameBinding>() {

    override fun onInitView(view: View) {
        setBannerStatus()
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



}