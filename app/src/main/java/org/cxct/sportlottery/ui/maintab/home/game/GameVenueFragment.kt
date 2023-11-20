package org.cxct.sportlottery.ui.maintab.home.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.reflect.KClass


abstract class GameVenueFragment<VM : BaseViewModel, VB>: BindingFragment<VM, FragmentGamevenueBinding>() {

    override fun createVM(clazz: KClass<VM>): VM {
        return getViewModel(clazz = clazz)
    }

    private val loadingHolder by lazy { Gloading.wrapView(binding.root) }

    override fun loading(message: String?) = loadingHolder.showLoading()
    override fun hideLoading() = loadingHolder.showLoadSuccess()

    override fun createRootView(inflater: LayoutInflater,
                                container: ViewGroup?,
                                savedInstanceState: Bundle?) = loadingHolder.wrapper

    override fun onInitView(view: View) {
        binding.rvcGameType.setLinearLayoutManager()
        binding.rvcGameType.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_f6faff, R.color.color_E0E3EE)
    }

}