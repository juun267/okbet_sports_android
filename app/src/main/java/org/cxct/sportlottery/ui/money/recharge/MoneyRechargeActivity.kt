package org.cxct.sportlottery.ui.money.recharge

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_money_recharge.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.finance.FinanceActivity

class MoneyRechargeActivity : BaseSocketActivity<MoneyRechViewModel>(MoneyRechViewModel::class) {

    companion object {
        const val RechargeViewLog = "rechargeViewLog"
    }

    enum class RechargeType { TRANSFER_PAY, ONLINE_PAY }

    var currentTab = RechargeType.TRANSFER_PAY

    private var bankTypeAdapter: MoneyBankTypeAdapter? = null
    private var transferPayList = mutableListOf<MoneyPayWayData>()
    private var onlinePayList = mutableListOf<MoneyPayWayData>()

    private var mCurrentFragment: Fragment? = null

    var apiResult: MoneyAddResult = MoneyAddResult(0, "", false, "")

    var cryptoResult: MoneyAddResult = MoneyAddResult(0, "", false, "")

    private val CYRPTOPAY_INDEX = 11 //11-虚拟币支付


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_money_recharge)

        initToolbar()
        initRecyclerView()
        initLiveData()
        initData()
        initView()
        initButton()
    }

    private fun initToolbar() {
        tv_toolbar_title.text = getString(R.string.recharge)
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun initData() {
        getMoneyConfig()
    }

    private fun getMoneyConfig() {
        loading()
        block_tab.visibility = View.GONE
        viewModel.getRechCfg()
    }

    private fun initLiveData() {

        viewModel.transferPayList.observe(this@MoneyRechargeActivity, Observer {
            hideLoading()
            block_tab.visibility = View.VISIBLE //獲取資金資料完畢, 顯示充值分類
            transferPayList = it ?: return@Observer
            btn_transfer_pay.visibility = if (transferPayList.size > 0) {
                View.VISIBLE
            } else {
                setTab(RechargeType.ONLINE_PAY)
                View.GONE
            }
            changePage()
        })

        viewModel.onlinePayList.observe(this@MoneyRechargeActivity, Observer {
            onlinePayList = it ?: return@Observer
            btn_online_pay.visibility = if (onlinePayList.size > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (currentTab == RechargeType.ONLINE_PAY) {
                bankTypeAdapter?.data = onlinePayList
            }
        })

        //轉帳支付 - 銀行 微信 支付寶...
        viewModel.transferPayResult.observe(this@MoneyRechargeActivity, Observer {
            apiResult = it ?: return@Observer

            val payWay = this.getString(R.string.txv_transfer_pay)

            if (!apiResult.success) {
                //顯示彈窗
                val customAlertDialog = CustomAlertDialog(this@MoneyRechargeActivity)
                with(customAlertDialog) {
                    setTitle(resources.getString(R.string.prompt))
                    setMessage(apiResult.msg)
                    setNegativeButtonText(null)
                }.let {
                    customAlertDialog.show()
                }
            } else {
                //顯示成功彈窗
                val moneySubmitDialog = MoneySubmitDialog(
                    payWay,
                    (apiResult.result ?: 0).toString(),
                    MoneySubmitDialog.MoneySubmitDialogListener({
                        finish()
                        startActivity(Intent(this, FinanceActivity::class.java).apply {
                            putExtra(
                                RechargeViewLog,
                                getString(R.string.record_recharge)
                            )
                        })
                    }, {
                        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(sConfigData?.customerServiceUrl)))
                    })
                )
                moneySubmitDialog.show(supportFragmentManager, "")
            }
        })
        //轉帳支付 - 虛擬幣
        viewModel.cryptoPayResult.observe(this@MoneyRechargeActivity, Observer {
            cryptoResult = it ?: return@Observer

            val payWay = this.getString(R.string.txv_crypto_pay)

            if (!cryptoResult.success) {
                //顯示彈窗
                val customAlertDialog = CustomAlertDialog(this@MoneyRechargeActivity)
                with(customAlertDialog) {
                    setTitle(resources.getString(R.string.prompt))
                    setMessage(cryptoResult.msg)
                    setNegativeButtonText(null)
                    setTextColor(R.color.colorRed)
                }.let {
                    customAlertDialog.show()
                }
            } else {
                //顯示成功彈窗
                val moneySubmitDialog = MoneySubmitDialog(
                    payWay,
                    (cryptoResult.result ?: 0).toString(),
                    MoneySubmitDialog.MoneySubmitDialogListener({
                        finish()
                        startActivity(Intent(this, FinanceActivity::class.java).apply {
                            putExtra(
                                RechargeViewLog,
                                getString(R.string.record_recharge)
                            )
                        })
                    }, {
                        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(sConfigData?.customerServiceUrl)))
                    })
                )
                moneySubmitDialog.show(supportFragmentManager, "")
            }
        })

        //在線支付 - 提交申請
        viewModel.onlinePayResult.observe(this@MoneyRechargeActivity, Observer {
            val payWay = this.getString(R.string.txv_online_pay)

            //顯示成功彈窗
            val moneySubmitDialog = MoneySubmitDialog(
                payWay,
                it.toString(),
                MoneySubmitDialog.MoneySubmitDialogListener({
                    finish()
                    startActivity(Intent(this, FinanceActivity::class.java).apply {
                        putExtra(
                            RechargeViewLog,
                            getString(R.string.record_recharge)
                        )
                    })
                }, {
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(sConfigData?.customerServiceUrl)))
                })
            )

            moneySubmitDialog.show(supportFragmentManager, "")
            moneySubmitDialog.dialog?.setCanceledOnTouchOutside(true)
        })

        //在線支付 - 虛擬幣
        viewModel.onlinePayCryptoResult.observe(this@MoneyRechargeActivity, Observer {
            val payWay = this.getString(R.string.txv_online_pay)

            //顯示成功彈窗
            val moneySubmitDialog = MoneySubmitDialog(
                payWay,
                it.toString(),
                MoneySubmitDialog.MoneySubmitDialogListener({
                    finish()
                    startActivity(Intent(this, FinanceActivity::class.java).apply {
                        putExtra(
                            RechargeViewLog,
                            getString(R.string.record_recharge)
                        )
                    })
                }, {
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(sConfigData?.customerServiceUrl)))
                })
            )

            moneySubmitDialog.show(supportFragmentManager, "")
            moneySubmitDialog.dialog?.setCanceledOnTouchOutside(true)
        })
    }


    private fun initButton() {
        btn_transfer_pay.setOnClickListener {
            setTab(RechargeType.TRANSFER_PAY)
            initRecyclerView()
            changePage()
            viewModel.clearnRechargeStatus()
        }
        btn_online_pay.setOnClickListener {
            setTab(RechargeType.ONLINE_PAY)
            initRecyclerView()
            changePage()
            viewModel.clearnRechargeStatus()
        }

        btn_floating_service.setView(this)
    }

    private fun initView() {

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

    private fun getPayFragment(moneyPayWay: MoneyPayWayData): Fragment {
        return when {
            moneyPayWay.rechType == "onlinePayment" && moneyPayWay.onlineType == CYRPTOPAY_INDEX -> {
                OnlineCryptoPayFragment().setArguments(moneyPayWay)
            }
            moneyPayWay.rechType == "onlinePayment" && moneyPayWay.onlineType != CYRPTOPAY_INDEX -> {
                OnlinePayFragment().setArguments(moneyPayWay)
            }
            moneyPayWay.rechType == "cryptoPay" -> CryptoPayFragment().setArguments(moneyPayWay)
            else -> TransferPayFragment().setArguments(moneyPayWay)

        }
    }

    /**
     * 切換Tab 連同 rv_pay_type 一起切換
     * */
    private fun changePage() {
        when (currentTab) {
            RechargeType.TRANSFER_PAY -> {
                btn_transfer_pay.isChecked = true
                bankTypeAdapter?.data = transferPayList
                switchFragment(
                    getPayFragment(transferPayList[0]),
                    "TransferPayFragment"
                )
            }
            RechargeType.ONLINE_PAY -> {
                btn_online_pay.isChecked =true
                bankTypeAdapter?.data = onlinePayList
                switchFragment(
                    getPayFragment(onlinePayList[0]),
                    "OnlinePayFragment"
                )
            }
        }
    }

    private fun setTab(selectTab: RechargeType) {
        currentTab = selectTab
    }

    /**
     * 導覽列 頁面切換
     * @param changeToFragment: 要跳轉的 fragment
     */
    private fun switchFragment(changeToFragment: Fragment?, tag: String) {

        if (changeToFragment == null) return

        val ft = supportFragmentManager.beginTransaction()

        ft.replace(
            R.id.fl_pay_type_container,
            changeToFragment,
            tag
        )
        mCurrentFragment = changeToFragment
        ft.commitAllowingStateLoss()
    }

    private fun initRecyclerView() {
        bankTypeAdapter = MoneyBankTypeAdapter(MoneyBankTypeAdapter.ItemClickListener {
            switchFragment(getPayFragment(it), it.rechType)
            this@MoneyRechargeActivity.currentFocus?.clearFocus()
            viewModel.clearnRechargeStatus()
        })
        rv_pay_type.layoutManager = GridLayoutManager(this@MoneyRechargeActivity, 2)
        rv_pay_type.adapter = bankTypeAdapter
    }
}