package org.cxct.sportlottery.ui.money.recharge

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_money_recharge.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.ui.base.BaseToolBarActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog

class MoneyRechargeActivity : BaseToolBarActivity<MoneyRechViewModel>(MoneyRechViewModel::class) {

    enum class RechargeType { TRANSFER_PAY, ONLINE_PAY }

    var currentTab = RechargeType.TRANSFER_PAY

    private var bankTypeAdapter: MoneyBankTypeAdapter? = null


    private var transferPayList = mutableListOf<MoneyPayWayData>()
    private var onlinePayList = mutableListOf<MoneyPayWayData>()
    private var currentPayList = mutableListOf<MoneyPayWayData>()

    private var mCurrentFragment: Fragment? = null

    var apiResult: MoneyAddResult = MoneyAddResult(0, "", false, "")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRecyclerView()
        initLiveData()
        initData()
        initView()
        initButton()
    }

    private fun initData() {
        viewModel.getRechCfg()
    }

    private fun initLiveData() {
//        viewModel.rechargeConfigs.observe(this@MoneyRechargeActivity, Observer {
//            var listData = it ?: return@Observer
//            bankTypeAdapter?.data=it.rechCfg
//
//        })

        viewModel.transferPayList.observe(this@MoneyRechargeActivity, Observer {
            transferPayList = it ?: return@Observer
            if (currentTab == RechargeType.TRANSFER_PAY)
                bankTypeAdapter?.data = transferPayList


            when (currentTab) {
                RechargeType.TRANSFER_PAY -> switchFragment(
                    getPayFragment(transferPayList[0]),
                    "TransferPayFragment"
                )
                RechargeType.ONLINE_PAY -> switchFragment(
                    getPayFragment(onlinePayList[0]),
                    "OnlinePayFragment"
                )
            }
        })

        viewModel.onlinePayList.observe(this@MoneyRechargeActivity, Observer {
            onlinePayList = it ?: return@Observer
            if (currentTab == RechargeType.ONLINE_PAY) {
                bankTypeAdapter?.data = onlinePayList
            }
        })

        viewModel.apiResult.observe(this@MoneyRechargeActivity, Observer {
            apiResult = it ?: return@Observer

            var payWay = if (btn_transfer_pay.isSelected)
                this.getString(R.string.txv_transfer_pay)
            else
                this.getString(R.string.txv_online_pay)

            if (!apiResult.success) {
                //顯示彈窗
                var customAlertDialog= CustomAlertDialog(this@MoneyRechargeActivity)
                with(customAlertDialog){
                    setTitle("提示")
                    setMessage(apiResult.msg)
                }.let {
                    customAlertDialog.show()
                }
            } else {
                //顯示成功彈窗
                val moneySubmitDialog = MoneySubmitDialog(
                    payWay,
                    (apiResult.result ?: 0).toString()
                )
                moneySubmitDialog.show(supportFragmentManager, "")
            }
        })

    }

    private fun initButton() {
        btn_transfer_pay.setOnClickListener {
            currentTab = RechargeType.TRANSFER_PAY
            btn_transfer_pay.isSelected = true
            btn_online_pay.isSelected = false
            bankTypeAdapter?.data = transferPayList
            switchFragment(
                getPayFragment(transferPayList[0]),
                "TransferPayFragment"
            )
        }
        btn_online_pay.setOnClickListener {
            currentTab = RechargeType.ONLINE_PAY
            btn_transfer_pay.isSelected = false
            btn_online_pay.isSelected = true
            bankTypeAdapter?.data = onlinePayList
            switchFragment(
                getPayFragment(onlinePayList[0]),
                "OnlinePayFragment"
            )
        }
    }

    private fun initView() {
        btn_transfer_pay.isSelected = true
        btn_online_pay.isSelected = false

        if ((!transferPayList.isNullOrEmpty() && currentTab == RechargeType.TRANSFER_PAY) || (!onlinePayList.isNullOrEmpty() && currentTab == RechargeType.ONLINE_PAY)) {
            block_no_type.visibility = View.VISIBLE
            rv_pay_type.visibility = View.GONE
            fl_pay_type_container.visibility = View.GONE
        } else {
            block_no_type.visibility = View.GONE
            rv_pay_type.visibility = View.VISIBLE
            fl_pay_type_container.visibility = View.VISIBLE
        }
    }

    private fun getPayFragment(moneyPayWay: MoneyPayWayData): Fragment? {
        return when (moneyPayWay.rechType) {
            "onlinePayment" -> OnlinePayFragment().setArguments(moneyPayWay)
            else -> TransferPayFragment().setArguments(moneyPayWay)
        }
    }

    /**
     * 導覽列 頁面切換
     * @param changeToFragment: 要跳轉的 fragment
     */
    private fun switchFragment(changeToFragment: Fragment?, tag: String) {

        if (changeToFragment == null) return
//        if (mCurrentFragment === changeToFragment) {
//            return
//        }
        val ft = supportFragmentManager.beginTransaction()

//        ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)

//        if (mCurrentFragment == null) { //首次添加 fragment
//            ft.add(R.id.fl_pay_type_container, changeToFragment, tag)
//
//        } else if (!changeToFragment.isAdded) {
//            ft.hide(mCurrentFragment!!).add(R.id.fl_pay_type_container, changeToFragment, tag)
//        } else {
//            ft.hide(mCurrentFragment!!).show(changeToFragment)
//        }

        ft.replace(
            R.id.fl_pay_type_container,
            changeToFragment,
            tag
        ) //replace fragment in the container layout.
        ft.addToBackStack(tag)



        mCurrentFragment = changeToFragment
        ft.addToBackStack(null)
        ft.commitAllowingStateLoss()
    }

    private fun initRecyclerView() {
        bankTypeAdapter = MoneyBankTypeAdapter(MoneyBankTypeAdapter.ItemClickListener {
            switchFragment(getPayFragment(it), it.rechType)
            viewModel.clearnTransferStatus()
        })
        rv_pay_type.layoutManager = GridLayoutManager(this@MoneyRechargeActivity, 2)
        rv_pay_type.adapter = bankTypeAdapter
    }

    override fun setContentView(): Int {
        return R.layout.activity_money_recharge
    }

    override fun setToolBarName(): String {
        return "充值"
    }

}