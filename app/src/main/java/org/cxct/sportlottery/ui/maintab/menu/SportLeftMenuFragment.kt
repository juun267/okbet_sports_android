package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import org.cxct.sportlottery.databinding.FragmentSportLeftMenuBinding
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.view.onClick

class SportLeftMenuFragment:BindingSocketFragment<SportLeftMenuViewModel, FragmentSportLeftMenuBinding> (){
    private inline fun getMainTabActivity() = activity as MainTabActivity

    override fun onInitView(view: View) =binding.run {

        //关闭按钮
        ivClose.onClick {
            close()
        }
    }


    override fun onInitData() {
    }


    //退出
    private fun close() {
        getMainTabActivity().closeDrawerLayout()
    }

}