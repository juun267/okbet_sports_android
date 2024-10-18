package org.cxct.sportlottery.common.extentions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.LayoutLoadingBinding
import org.cxct.sportlottery.util.ToastUtil
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass


fun Any.getKClass(index: Int): KClass<*> {
    val parameterizedType = this::class.java.genericSuperclass as ParameterizedType
    val actualTypeArguments = parameterizedType.actualTypeArguments
    return (actualTypeArguments[index] as Class<*>).kotlin
}

fun <VB : ViewBinding> Any.createVBinding(layoutInflater: LayoutInflater, index: Int = 0): VB {
    val parameterizedType = this::class.java.genericSuperclass as ParameterizedType
    val actualTypeArguments = parameterizedType.actualTypeArguments
    val clazz = actualTypeArguments[index] as Class<VB>
    val method = clazz.getMethod("inflate", LayoutInflater::class.java)
    return method.invoke(null, layoutInflater) as VB
}

fun <VDB : ViewDataBinding> LifecycleOwner.createDataBinding(
    layoutInflater: LayoutInflater, index: Int = 0
): VDB {
    val owner = this
    val parameterizedType = this::class.java.genericSuperclass as ParameterizedType
    val actualTypeArguments = parameterizedType.actualTypeArguments
    val clazz = actualTypeArguments[index] as Class<VDB>
    val method = clazz.getMethod("inflate", LayoutInflater::class.java)
    return (method.invoke(null, layoutInflater) as VDB).apply { lifecycleOwner = owner }
}

// 防止LiveData数据倒灌
@MainThread
fun LiveData<*>.clean() {
    val versionField = LiveData::class.java.getDeclaredField("mVersion")
    versionField.isAccessible = true
    versionField.setInt(this, -1)
}

@MainThread
fun ViewModel.releaseVM() {
    val declaredField = ViewModel::class.java.getDeclaredField("mCleared")
    declaredField.isAccessible = true
    if (declaredField.getBoolean(this)) {
        return
    }

    val field = ViewModel::class.java.getDeclaredMethod("clear")
    field.isAccessible = true
    field.invoke(this)
}

fun LifecycleOwner.doOnResume(interval: Int = 30_000, once: Boolean = false, block: () -> Unit) {
    doWhenLife(Lifecycle.Event.ON_RESUME, interval, block, once)
}

fun LifecycleOwner.doOnPause(once: Boolean = false, block: () -> Unit) {
    doWhenLife(Lifecycle.Event.ON_PAUSE, 0, block, once)
}

fun LifecycleOwner.doOnStop(once: Boolean = false, block: () -> Unit) {
    doWhenLife(Lifecycle.Event.ON_STOP, 0, block, once)
}

fun LifecycleOwner.doOnDestory(block: () -> Unit) {
    doWhenLife(Lifecycle.Event.ON_DESTROY, 0, block, true)
}

fun LifecycleOwner.doWhenLife(
    lifeEvent: Lifecycle.Event, interval: Int = 0, block: () -> Unit, once: Boolean
) {
    lifecycle.addObserver(object : LifecycleEventObserver {

        var time = 0L
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == lifeEvent && System.currentTimeMillis() - time > interval) {
                if (once) {
                    lifecycle.removeObserver(this)
                }
                time = System.currentTimeMillis()
                block.invoke()
            }
        }
    })
}

fun Activity.finishWithOK() {
    setResult(Activity.RESULT_OK)
    finish()
}

fun Fragment.startActivity(activity: Class<out Activity>, intentParams: ((Intent)->Unit)?=null) {
    startActivity(Intent(requireActivity(), activity).apply { intentParams?.invoke(this)})
}

fun Activity.startActivity(activity: Class<out Activity>, intentParams: ((Intent)->Unit)?=null) {
    startActivity(Intent(this, activity).apply { intentParams?.invoke(this)})
}

fun Activity.bindFinish(vararg views: View) {
    val finishClick = View.OnClickListener { finish() }
    views.forEach { it.setOnClickListener(finishClick) }
}

fun toast(str: String) {
    ToastUtil.showToast(MultiLanguagesApplication.appContext, str, Toast.LENGTH_SHORT)
}

fun FragmentActivity.addFragment(fragmentId: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction().add(
        fragmentId, fragment
    ).commitAllowingStateLoss()
}

fun FragmentActivity.replaceFragment(fragmentId: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(fragmentId, fragment)
        .commitAllowingStateLoss()
}
//隱藏鍵盤
fun FragmentActivity.hideSoftKeyboard() {
    try {
        val inputMethodManager = getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

//var loadingViewBinding: LayoutLoadingBinding?=null
private val loadingViewId = View.generateViewId()
@SuppressLint("InflateParams")
fun FragmentActivity.loading(message: String?=null) {
    var loadingView = findViewById<View>(loadingViewId)
    val loadingBinding: LayoutLoadingBinding
    if (loadingView == null) {
        loadingBinding = LayoutLoadingBinding.inflate(layoutInflater)
        loadingView = loadingBinding.root.apply {
            id = loadingViewId
            tag = loadingBinding
        }
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
        )
        addContentView(loadingView, params)
    } else {
        loadingBinding = loadingView.tag as LayoutLoadingBinding
        loadingBinding.rlLoading.apply {
            visibility = View.VISIBLE
            isClickable = true
        }
    }
    (loadingBinding.ivLoading.background as? AnimationDrawable)?.start()
}

/*关闭加载界面*/
fun FragmentActivity.hideLoading() {
    (findViewById<View>(loadingViewId)?.tag as? LayoutLoadingBinding)?.let {
        it.root.visibility = View.GONE
        (it.ivLoading.background as? AnimationDrawable)?.stop()
    }
}
