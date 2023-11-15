package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import org.cxct.sportlottery.R
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseBottomNavigationFragment<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseSocketFragment<T>(clazz), Animation.AnimationListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receiver.dataSourceChange.observe(viewLifecycleOwner) {
            dataSourceChangeEven?.let {
                showErrorPromptDialog(
                    title = getString(R.string.prompt),
                    message = getString(R.string.message_source_change),
                    hasCancel = false
                ) { it.invoke() }
            }
        }
    }

    var afterAnimateListener: AfterAnimateListener? = null

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            if (nextAnim == 0) {
                try {
                    afterAnimateListener?.onAfterAnimate()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                super.onCreateAnimation(transit, enter, nextAnim)
            } else {
                AnimationUtils.loadAnimation(activity, nextAnim).apply {
                    setAnimationListener(this@BaseBottomNavigationFragment)
                }
            }
        } else {
            super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    override fun onAnimationStart(animation: Animation?) {

    }

    override fun onAnimationEnd(animation: Animation?) {
        try {
            afterAnimateListener?.onAfterAnimate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAnimationRepeat(animation: Animation?) {}

    class AfterAnimateListener(private val onAfterAnimate: () -> Unit) {
        fun onAfterAnimate() = onAfterAnimate.invoke()
    }

    fun clickMenu() {
        when (activity) {
            is BaseBottomNavActivity<*> -> {
                (activity as BaseBottomNavActivity<*>).clickMenuEvent()
            }
            else -> Timber.i("$this 尚未實作關閉菜單方法")
        }
    }

    private var dataSourceChangeEven: (() -> Unit)? = null

    /**
     * 设置有新赛事数据监听回调。
     *  重点!!!
     *  页面加载完成后再调用该方法就行设置回调，
     *  不然由于LiveData粘性事件的原因，在页面初始化的时候就有可能弹窗
     */
    fun setDataSourceChangeEvent(dataSourceChangeEven: () -> Unit) {
        this.dataSourceChangeEven = dataSourceChangeEven
    }

}