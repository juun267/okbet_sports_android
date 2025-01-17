package org.cxct.sportlottery.ui.money.recharge

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.appevent.SensorsEventUtil
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityMoneyRechargeBinding
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.money.recharge.dialog.GiveUpDepositDialog
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager

/**
 * @app_destination 存款
 */
class MoneyRechargeActivity : BaseSocketActivity<MoneyRechViewModel,ActivityMoneyRechargeBinding>(MoneyRechViewModel::class) {

    override fun pageName() = "存款页面"

    companion object {
        const val RechargeViewLog = "rechargeViewLog"
        const val CRYPTO_PAY_INDEX = 11 //11-虚拟币支付

        fun startFrom(context: Context, from: String) {
            SensorsEventUtil.depositPageEvent(from)
            context.startActivity(Intent(context, MoneyRechargeActivity::class.java))
        }
    }

    enum class RechargeType(val tabPosition: Int) { TRANSFER_PAY(0), ONLINE_PAY(1) }

    private var bankTypeAdapter = MoneyBankTypeAdapter()
    private var transferPayList = mutableListOf<MoneyPayWayData>()
    private var onlinePayList = mutableListOf<MoneyPayWayData>()

    private var gotTransferPay: Boolean = false
    private var gotOnlinePay: Boolean = false

    private var mCurrentFragment: Fragment? = null

    private var apiResult: MoneyAddResult = MoneyAddResult(0, "", false, "")
    private var cryptoResult: MoneyAddResult = MoneyAddResult(0, "", false, "")

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        initToolbar()
        initRecyclerView()
        initLiveData()
        initData()
        initView()
        initTabLayout()
        initMarquee()
    }

    private fun initToolbar() {
        binding.toolBar.titleText = getString(R.string.J285)
        binding.toolBar.setOnBackPressListener { doOnBackPress() }
    }

    override fun onBackPressed() {
        doOnBackPress()
    }

    private fun doOnBackPress() {

        if (viewModel.hasCharged || viewModel.uniPaid) {
            super.onBackPressed()
            return
        }

        val dailyConfig = viewModel.dailyConfigEvent.value?.find { it.isFirstDepositActivity() && it.capped > 0 }
        if (dailyConfig == null) {
            super.onBackPressed()
            return
        }

        GiveUpDepositDialog.show(supportFragmentManager, dailyConfig.capped.toString())

    }

    private fun initData() {
        getMoneyConfig()
    }

    private fun getMoneyConfig() {
        loading()
        binding.customTabLayout.visibility = View.GONE
        viewModel.getRechCfg()
    }

    private fun initLiveData() {

        viewModel.transferPayList.observe(this) {
            hideLoading()
            gotTransferPay = true
            transferPayList = it ?: return@observe
            binding.customTabLayout.firstTabVisibility = if (transferPayList.size > 0) {
                transferPageChange()
                View.VISIBLE
            } else {
                binding.customTabLayout.selectTab(RechargeType.ONLINE_PAY.tabPosition)
                View.GONE
            }

            updateTabLayoutVisibility()
        }

        viewModel.onlinePayList.observe(this) {
            gotOnlinePay = true
            onlinePayList = it ?: return@observe
            binding.customTabLayout.secondTabVisibility = if (onlinePayList.size > 0) {
                if (binding.customTabLayout.selectedTabPosition == RechargeType.ONLINE_PAY.tabPosition) {
                    onlinePageChange()
                }
                View.VISIBLE
            } else {
                View.GONE
            }

            updateTabLayoutVisibility()
        }

        //轉帳支付 - 銀行 微信 支付寶...
        viewModel.transferPayResult.observe(this) {
            apiResult = it ?: return@observe

            val payWay = this.getString(R.string.txv_transfer_pay)

            if (!apiResult.success) {
                //顯示彈窗
                CustomAlertDialog().apply {
                    setTitle(this@MoneyRechargeActivity.getString(R.string.prompt))
                    setMessage(apiResult.msg)
                    setNegativeButtonText(null)
                }.show(supportFragmentManager,null)
            } else {
                //顯示成功彈窗
                MoneySubmitDialog.newInstance(
                    payWay,
                    (apiResult.result ?: 0).toString(),
                ).show(supportFragmentManager)
            }
        }

        //轉帳支付 - 虛擬幣
        viewModel.cryptoPayResult.observe(this) {
            cryptoResult = it ?: return@observe

            val payWay = this.getString(R.string.txv_crypto_pay)

            if (!cryptoResult.success) {
                //顯示彈窗
                CustomAlertDialog().apply {
                    setTitle(this@MoneyRechargeActivity.getString(R.string.prompt))
                    setMessage(cryptoResult.msg)
                    setNegativeButtonText(null)
                    setTextColor(R.color.color_E44438_e44438)
                }.show(supportFragmentManager,null)
            } else {
                //顯示成功彈窗
                MoneySubmitDialog.newInstance(
                    payWay,
                    (cryptoResult.result ?: 0).toString()
                ).show(supportFragmentManager)
            }
        }

        //在線支付 - 提交申請
        viewModel.onlinePayResult.observe(this) {
            val payWay = this.getString(R.string.recharge_channel_online)

            //顯示成功彈窗
            MoneySubmitDialog.newInstance(
                payWay,
                it.toString()
            ).show(supportFragmentManager)
        }

        //在線支付 - 虛擬幣
        viewModel.onlinePayCryptoResult.observe(this) {
            val payWay = this.getString(R.string.recharge_channel_online)

            //顯示成功彈窗
            MoneySubmitDialog.newInstance(
                payWay,
                it.toString()
            ).show(supportFragmentManager)
        }
    }


    private fun initTabLayout() {
        binding.customTabLayout.setCustomTabSelectedListener { position ->
            if (position == RechargeType.TRANSFER_PAY.tabPosition) {
                transferPageChange()
            } else if (position == RechargeType.ONLINE_PAY.tabPosition) {
                onlinePageChange()
            }
        }

    }
    private fun initMarquee() {
        binding.announcement.setUp(this,viewModel, arrayOf(3),5)
    }
    private fun onlinePageChange() {
        viewModel.clearnRechargeStatus()
        bankTypeAdapter.setList(onlinePayList)
        switchFragment(
            onlinePayList.getOrNull(0)?.let { getPayFragment(it) },
            "OnlinePayFragment"
        )
    }

    private fun transferPageChange() {
        viewModel.clearnRechargeStatus()
        bankTypeAdapter.setList(transferPayList)
        switchFragment(
            transferPayList.getOrNull(0)?.let { getPayFragment(it) },
            "TransferPayFragment"
        )
    }

    private fun initView()=binding.run {
        if ((transferPayList.isNotEmpty() && customTabLayout.selectedTabPosition == RechargeType.TRANSFER_PAY.tabPosition)
            || (onlinePayList.isNotEmpty() && customTabLayout.selectedTabPosition == RechargeType.ONLINE_PAY.tabPosition)
        ) {
            blockNoType.visibility = View.VISIBLE
            rvPayType.visibility = View.GONE
            flPayTypeContainer.visibility = View.GONE
        } else {
            blockNoType.visibility = View.GONE
            rvPayType.visibility = View.VISIBLE
            flPayTypeContainer.visibility = View.VISIBLE
        }
    }

    private fun getPayFragment(moneyPayWay: MoneyPayWayData): Fragment {
        if (moneyPayWay.rechType == "onlinePayment" && moneyPayWay.onlineType == CRYPTO_PAY_INDEX) {
            return OnlineCryptoPayFragment().setArguments(moneyPayWay)
        }

        if (moneyPayWay.rechType == "onlinePayment" && moneyPayWay.onlineType != CRYPTO_PAY_INDEX) {
            return OnlinePayFragment().setArguments(moneyPayWay)
        }

        if (moneyPayWay.rechType == "cryptoPay") {
            return CryptoPayFragment().setArguments(moneyPayWay)
        }

        return TransferPayFragment().setArguments(moneyPayWay)
    }

    private fun switchFragment(changeToFragment: Fragment?, tag: String) {
        if (changeToFragment == null) return

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fl_pay_type_container, changeToFragment, tag)
        mCurrentFragment = changeToFragment
        ft.commitAllowingStateLoss()
    }

    private fun initRecyclerView()=binding.rvPayType.run {
        bankTypeAdapter.setOnItemClickListener { _, _, position ->
            bankTypeAdapter.setSelectedPosition(position)
            val itemData = bankTypeAdapter.getItem(position)
            //SensorsEventUtil.selectPaymentEvent("${itemData.titleNameMap[LanguageManager.getLanguageString()]}", fromPage, fromPage)
            switchFragment(getPayFragment(itemData), itemData.rechType)
            this@MoneyRechargeActivity.currentFocus?.clearFocus()
            viewModel.clearnRechargeStatus()
        }
        layoutManager = GridLayoutManager(this@MoneyRechargeActivity, 4)
        adapter = bankTypeAdapter
        addItemDecoration(GridSpacingItemDecoration(4, 10.dp, false))
    }

    /**
     * 檢查是否需要顯示充值分類Tab
     */
    private fun updateTabLayoutVisibility() {
        if (!gotOnlinePay || !gotTransferPay) {
            return
        }

        binding.customTabLayout.isVisible = transferPayList.isNotEmpty() && onlinePayList.isNotEmpty()
    }

    fun fillPayWaysLayoutTo(viewGroup: ViewGroup, index: Int) {
        val llPayWays = binding.llPayWays
        val parent = llPayWays.parent
        if (parent != null) {
            if (parent == viewGroup) {
                return
            }
            (parent as ViewGroup).removeView(llPayWays)
        }
        viewGroup.addView(llPayWays, index)
    }

    /**
     * 用户更新通道返利比例
     */
    fun updateSelectRechCfgs(rechCfg: RechCfg){
       bankTypeAdapter.updaterebateFeeNew(rechCfg)
    }
}