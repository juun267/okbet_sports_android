package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.LayoutRes
import kotlinx.android.synthetic.main.activity_base_tool_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.odds.OddsDetailViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.reflect.KClass

/***
 * 目前無法支援使用DataBinding的Activity
 * 但可以測試看看LiveData
 */


abstract class BaseToolBarActivity<T : BaseViewModel>(claazz: KClass<T>) : BaseActivity<T>(claazz) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_tool_bar)

        frame_layout.addView(layoutInflater.inflate(resources.getLayout(setContentView()), null))
        tv_toolbar_title.text = setToolBarName()
    }
    /**
     * 回傳 Layout
     * */
    abstract fun setContentView():Int

    /**
     * 回傳 標題名稱
     * */
    abstract fun setToolBarName():String
}