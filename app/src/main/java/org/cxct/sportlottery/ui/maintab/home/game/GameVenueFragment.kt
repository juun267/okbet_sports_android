package org.cxct.sportlottery.ui.maintab.home.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.reflect.KClass


abstract class GameVenueFragment<VM : BaseViewModel, VB>: BaseFragment<VM, FragmentGamevenueBinding>() {

    override fun createVM(clazz: KClass<VM>): VM {
        return getViewModel(clazz = clazz)
    }

    fun getMainTabActivity() = activity as MainTabActivity?
    protected val loadingHolder by lazy { Gloading.wrapView(binding.root) }

    protected fun showLoadingView() = loadingHolder.showLoading()
    protected fun hideLoadingView() = loadingHolder.showLoadSuccess()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return loadingHolder.wrapper
    }

    override fun onInitView(view: View) {
        binding.rvcGameType.setLinearLayoutManager()
        binding.rvcGameType.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_f6faff, R.color.color_E0E3EE)
    }

}