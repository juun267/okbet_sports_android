package org.cxct.sportlottery.ui.money.recharge

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.ui.base.BaseFragment

class TransferPayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    companion object {
        private const val TAG = "TransferPayFragment"    }

    private var mMoneyPayWay: MoneyPayWayData? = MoneyPayWayData("","","","",0) //支付類型

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.transfer_pay_fragment, container, false)
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.transfer_pay_fragment, container, false)
//    }


    fun setArguments(moneyPayWay: MoneyPayWayData?): TransferPayFragment {
        mMoneyPayWay = moneyPayWay
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        initPayAccountSpinner()
//        initButton()
//        initTimePicker()
    }
}