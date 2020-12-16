package org.cxct.sportlottery.interfaces

import android.content.Context
import android.view.View
import org.cxct.sportlottery.network.manager.NetworkStatusManager
import org.cxct.sportlottery.ui.base.BaseActivity

open class OnCheckConnectClickListener(private var context: Context, private var doing: Doing) : View.OnClickListener {

    interface Doing {
        fun onClick()
    }

    @Override
    override fun onClick(v: View?) {
        if (NetworkStatusManager.getInstance().isUnavailable()) {
            (context as BaseActivity).onNetworkUnavailable()
            return
        }
        doing.onClick()
    }
}