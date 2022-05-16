package org.cxct.sportlottery.ui.money.recharge

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_money_recharge.*
import kotlinx.android.synthetic.main.activity_money_recharge.custom_tab_layout
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GridItemDecoration
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 存款
 */
class MoneyRechargeActivity : BaseSocketActivity<MoneyRechViewModel>(MoneyRechViewModel::class) {

    companion object {
        const val RechargeViewLog = "rechargeViewLog"
        const val CRYPTO_PAY_INDEX = 11 //11-虚拟币支付
    }

    enum class RechargeType(val tabPosition: Int) { TRANSFER_PAY(0), ONLINE_PAY(1) }

    private var bankTypeAdapter: MoneyBankTypeAdapter? = null
    private var transferPayList = mutableListOf<MoneyPayWayData>()
    private var onlinePayList = mutableListOf<MoneyPayWayData>()

    private var gotTransferPay: Boolean = false
    private var gotOnlinePay: Boolean = false

    private var mCurrentFragment: Fragment? = null

    private var apiResult: MoneyAddResult = MoneyAddResult(0, "", false, "")
    private var cryptoResult: MoneyAddResult = MoneyAddResult(0, "", false, "")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_money_recharge)

        initToolbar()
        initRecyclerView()
        initLiveData()
        initData()
        initView()
        initTabLayout()
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
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
        custom_tab_layout.visibility = View.GONE
        viewModel.getRechCfg()
    }

    private fun initLiveData() {

        viewModel.transferPayList.observe(this@MoneyRechargeActivity, Observer {
            hideLoading()
            gotTransferPay = true
            transferPayList = it ?: return@Observer
            custom_tab_layout.firstTabVisibility = if (transferPayList.size > 0) {
                transferPageChange()
                View.VISIBLE
            } else {
                custom_tab_layout.selectTab(RechargeType.ONLINE_PAY.tabPosition)
                View.GONE
            }

            updateTabLayoutVisibility()
        })

        viewModel.onlinePayList.observe(this@MoneyRechargeActivity, Observer {
            gotOnlinePay = true
            onlinePayList = it ?: return@Observer
            custom_tab_layout.secondTabVisibility = if (onlinePayList.size > 0) {
                if (custom_tab_layout.selectedTabPosition == RechargeType.ONLINE_PAY.tabPosition) {
                    onlinePageChange()
                }
                View.VISIBLE
            } else {
                View.GONE
            }

            updateTabLayoutVisibility()
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
                    customAlertDialog.show(supportFragmentManager, null)
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
                    setTextColor(R.color.color_E44438_e44438)
                }.let {
                    customAlertDialog.show(supportFragmentManager, null)
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


    private fun initTabLayout() {
        custom_tab_layout.setCustomTabSelectedListener { position ->
            when (position) {
                RechargeType.TRANSFER_PAY.tabPosition -> {
                    transferPageChange()
                }
                RechargeType.ONLINE_PAY.tabPosition -> {
                    onlinePageChange()
                }
            }
        }
//        btn_floating_service.setView(this)
    }

    private fun onlinePageChange() {
        viewModel.clearnRechargeStatus()
        bankTypeAdapter?.data = onlinePayList
        switchFragment(
            onlinePayList.getOrNull(0)?.let { getPayFragment(it) },
            "OnlinePayFragment"
        )
    }

    private fun transferPageChange() {
        viewModel.clearnRechargeStatus()
        bankTypeAdapter?.data = transferPayList
        switchFragment(
            transferPayList.getOrNull(0)?.let { getPayFragment(it) },
            "TransferPayFragment"
        )
    }

    private fun initView() {
        if ((!transferPayList.isNullOrEmpty() && custom_tab_layout.selectedTabPosition == RechargeType.TRANSFER_PAY.tabPosition)
            || (!onlinePayList.isNullOrEmpty() && custom_tab_layout.selectedTabPosition == RechargeType.ONLINE_PAY.tabPosition)
        ) {
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
            moneyPayWay.rechType == "onlinePayment" && moneyPayWay.onlineType == CRYPTO_PAY_INDEX -> {
                OnlineCryptoPayFragment().setArguments(moneyPayWay)
            }
            moneyPayWay.rechType == "onlinePayment" && moneyPayWay.onlineType != CRYPTO_PAY_INDEX -> {
                OnlinePayFragment().setArguments(moneyPayWay)
            }
            moneyPayWay.rechType == "cryptoPay" -> CryptoPayFragment().setArguments(moneyPayWay)
            else -> TransferPayFragment().setArguments(moneyPayWay)

        }
    }

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
        rv_pay_type.addItemDecoration(
            GridItemDecoration(
                8.dp,
                8.dp,
                ContextCompat.getColor(this, R.color.color_191919_FCFCFC),
                false
            )
        )
    }

    /**
     * 檢查是否需要顯示充值分類Tab
     */
    private fun updateTabLayoutVisibility() {
        if (gotOnlinePay && gotTransferPay) {
            custom_tab_layout.visibility = if (!transferPayList.isNullOrEmpty() && !onlinePayList.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }
}