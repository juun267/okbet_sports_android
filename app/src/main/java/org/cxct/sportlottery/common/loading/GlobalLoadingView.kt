package org.cxct.sportlottery.common.loading

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import org.cxct.sportlottery.R

class GlobalLoadingView @JvmOverloads constructor(
    context: Context,
    set: AttributeSet?,
    defTheme: Int = 0
) : LinearLayout(context, set, defTheme), View.OnClickListener {

    private val mTextView: TextView
    private var mRetryTask: Runnable? = null
    private val mImageView: ImageView
    private var emptyText = ""
    private var emptyRes = -1
    private var errorText = ""
    private var errorRes = -1
    private var loadingText = ""
    private var loadingRes = -1

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.view_global_loading, this, true)
        mImageView = findViewById(R.id.image)
        mTextView = findViewById(R.id.text)
        setBackgroundColor(Color.WHITE)
    }

    fun setMsgViewVisibility(visible: Boolean) {
        mTextView.visibility = if (visible) VISIBLE else GONE
    }

    fun setRetryTask(retryTask: Runnable?) {
        mRetryTask = retryTask
    }

    fun setStatus(status: Int) {
        var show = true
        var onClickListener: OnClickListener? = null
        var image = loadingRes
        var str = ""
        when (status) {
            Gloading.STATUS_LOAD_SUCCESS -> show = false
            Gloading.STATUS_LOADING -> str = loadingText
            Gloading.STATUS_LOAD_FAILED -> {
                val networkConn = isNetworkConnected(context)
                if (!networkConn) {
                    str = resources.getString(R.string.chat_connection_error)
                    image = errorRes
                } else {
                    str = errorText
                    image = errorRes
                }
                onClickListener = this
            }
            Gloading.STATUS_EMPTY_DATA -> {
                str = emptyText
                image = emptyRes
                onClickListener = this
            }
            else -> {}
        }
        mTextView.text = str
        mImageView.setImageResource(image)
        setOnClickListener(onClickListener)
        isVisible = show
    }

    override fun onClick(v: View) {
        mRetryTask?.run()
    }

    fun setEmptyText(emptyText: String) {
        this.emptyText = emptyText
    }

    fun setEmptyIcon(emptyIcon: Int) {
        emptyRes = emptyIcon
    }

    fun setErrorText(errorText: String) {
        this.errorText = errorText
    }

    fun setErrorRes(errorRes: Int) {
        this.errorRes = errorRes
    }

    fun setLoadingText(loadingText: String) {
        this.loadingText = loadingText
    }

    fun setLoadingRes(loadingRes: Int) {
        this.loadingRes = loadingRes
    }

    private fun isNetworkConnected(context: Context): Boolean {
        var context = context
        try {
            context = context.applicationContext
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (cm != null) {
                val networkInfo = cm.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
        } catch (ignored: Exception) {
        }
        return false
    }
}