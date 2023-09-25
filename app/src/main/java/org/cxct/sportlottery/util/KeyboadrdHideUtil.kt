package org.cxct.sportlottery.util

import android.R
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by yinglan
 */
class KeyboadrdHideUtil private constructor(activity: Activity, content: ViewGroup?) {

    private fun getScrollView(viewGroup: ViewGroup?, activity: Activity) {
        if (null == viewGroup) {
            return
        }
        val count = viewGroup.childCount
        for (i in 0 until count) {
            val view = viewGroup.getChildAt(i)
            if (view is ScrollView) {
                setOnTouchListener(activity, view)
            } else if (view is AbsListView) {
                setOnTouchListener(activity, view)
            } else if (view is RecyclerView) {
                setOnTouchListener(activity, view)
            } else if (view is ViewGroup) {
                getScrollView(view, activity)
            }
            if (view.isClickable && view is TextView && view !is EditText) {
                setOnTouchListener(activity, view)
            }
        }
    }

    fun setOnTouchListener(activity: Activity, view: View) {
        view.setOnTouchListener { view, motionEvent ->
            dispatchTouchEvent(activity, motionEvent)
            false
        }
    }

    /**
     * @param mActivity
     * @param ev
     * @return
     */
    private fun dispatchTouchEvent(mActivity: Activity, ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = mActivity.currentFocus
            if (null != v && isShouldHideInput(v, ev)) {
                hideSoftInput(mActivity, v.windowToken)
            }
        }
        return false
    }

    /**
     * @param v
     * @param event
     * @return
     */
    private fun isShouldHideInput(v: View, event: MotionEvent): Boolean {
        if (v is EditText) {
            val rect = Rect()
            v.getHitRect(rect)
            if (rect.contains(event.x.toInt(), event.y.toInt())) {
                return false
            }
        }
        return true
    }

    /**
     * @param mActivity
     * @param token
     */
    private fun hideSoftInput(mActivity: Activity, token: IBinder?) {
        if (token != null) {
            val im = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    companion object {
        /**
         * Initialization method
         *  该实现使用了Activity顶层布局android.R.id.content的OnTouchListener监听,重写此监听需注意。
         * @param activity
         */
        fun init(activity: Activity) {
            KeyboadrdHideUtil(activity, null)
        }

        /**
         * Can pass the outer layout
         *
         * @param activity
         * @param content
         */
        fun init(activity: Activity, content: ViewGroup?) {
            KeyboadrdHideUtil(activity, content)
        }

        /**
         * Forced hidden keyboard
         *
         * @param activity
         */
        fun hideSoftKeyboard(activity: Activity?) {
            if (null == activity) {
                throw RuntimeException("参数错误")
            }
            val view = activity.currentFocus
            if (null != view) {
                val inputMethodManager =
                    activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }

        /**
         * Forced hidden keyboard
         *
         * @param view
         */
        fun hideSoftKeyboard(view: View?) {
            if (null != view) {
                val inputMethodManager =
                    view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            } else {
                throw RuntimeException("参数错误")
            }
        }

        /**
         * Forced hidden keyboard
         *
         * @param dialog
         */
        fun hideDialogSoftKeyboard(dialog: Dialog?) {
            if (null == dialog) {
                throw RuntimeException("参数错误")
            }
            val view = dialog.currentFocus
            if (null != view) {
                val inputMethodManager =
                    dialog.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }

    /**
     * @param activity
     */
    init {
        var content = content
        if (content == null) {
            content = activity.findViewById<View>(R.id.content) as ViewGroup
        }
        getScrollView(content, activity)
        content.setOnTouchListener { view, motionEvent ->
            dispatchTouchEvent(activity, motionEvent)
            false
        }
    }
}