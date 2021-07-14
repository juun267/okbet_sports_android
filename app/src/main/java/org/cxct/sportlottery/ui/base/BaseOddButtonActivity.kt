package org.cxct.sportlottery.ui.base


import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.cxct.sportlottery.ui.bet.list.BetInfoListDialog
import org.cxct.sportlottery.ui.bet.list.BetInfoListParlayDialog
import kotlin.reflect.KClass


abstract class BaseOddButtonActivity<T : BaseOddButtonViewModel>(clazz: KClass<T>) :
    BaseSocketActivity<T>(clazz) {


    private var oddListDialog: DialogFragment? = null


    init {
        oddListDialog = BetInfoListDialog() //方便測試 暫時預設一般注單
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.betInfoRepository.isParlayPage.observe(this, {
            oddListDialog = when (it) {
                true -> {
                    BetInfoListParlayDialog()
                }
                false -> {
                    BetInfoListDialog()
                }
            }

            viewModel.betInfoRepository.getCurrentBetInfoList()
        })
    }


    fun showBetListDialog() {
        oddListDialog?.show(
            supportFragmentManager,
            BaseOddButtonActivity::class.java.simpleName
        )
    }


    override fun onResume() {
        super.onResume()
        viewModel.betInfoRepository.getCurrentBetInfoList()
    }


}