package org.cxct.sportlottery.common.extentions

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.application.MultiLanguagesApplication
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

fun Fragment.startActivity(activity: Class<out Activity>) {
    startActivity(Intent(requireActivity(), activity))
}

fun Activity.startActivity(activity: Class<out Activity>) {
    startActivity(Intent(this, activity))
}

fun Activity.bindFinish(vararg views: View) {
    val finishClick = View.OnClickListener { finish() }
    views.forEach { it.setOnClickListener(finishClick) }
}

fun toast(str: String) {
    ToastUtil.showToast(MultiLanguagesApplication.appContext, str, Toast.LENGTH_LONG)
}

fun AppCompatActivity.addFragment(fragmentId: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction().add(
        fragmentId, fragment
    ).commitAllowingStateLoss()
}

fun AppCompatActivity.replaceFragment(fragmentId: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(fragmentId, fragment)
        .commitAllowingStateLoss()
}