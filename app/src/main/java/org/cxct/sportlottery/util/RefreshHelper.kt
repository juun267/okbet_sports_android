package org.cxct.sportlottery.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.scwang.smart.refresh.footer.BallPulseFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshHeader


// 基于SmartRefreshLayout的刷新加载逻辑处理辅助工具类
class RefreshHelper {

    private val refreshLayout: SmartRefreshLayout
    private var pageSize = 20
    private var pageIndex = 1
    var isRefreshing = false
        private set
    var isLoading = false
        private set

    companion object {

        fun of(contentView: View, lifecycleOwner: LifecycleOwner, refreshEnable: Boolean = true, loadMoreEnable: Boolean = true): RefreshHelper {

            val context = contentView.context
            val refreshLayout = SmartRefreshLayout(context)

            if (refreshEnable) {
                refreshLayout.setRefreshHeader(getDefaultRefreshHeader(context), -1, -2)
            } else {
                refreshLayout.setEnableRefresh(false)
            }
            if (loadMoreEnable) {
                refreshLayout.setRefreshFooter(getDefaultRefreshFooter(context), -1, -2)
            }

            val parent = contentView.parent as ViewGroup?
            parent?.let {
                val index = it.indexOfChild(contentView)
                it.removeView(contentView)
                it.addView(refreshLayout, index, contentView.layoutParams)
            }
            refreshLayout.setRefreshContent(contentView, -1, -1)
            return RefreshHelper(refreshLayout, lifecycleOwner)
        }

        fun of(refreshLayout: SmartRefreshLayout, lifecycleOwner: LifecycleOwner): RefreshHelper {
            return RefreshHelper(refreshLayout, lifecycleOwner)
        }

        private fun getDefaultRefreshHeader(context: Context): RefreshHeader {
            return MaterialHeader(context)
        }

        private fun getDefaultRefreshFooter(context: Context): RefreshFooter {
            return BallPulseFooter(context)
        }
    }

    private constructor(refreshLayout: SmartRefreshLayout, lifecycleOwner: LifecycleOwner) {
        this.refreshLayout = refreshLayout
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    finishRefresh()
                }
            }
        })
    }

    // 经过包装后的view, 后期还有可能变动。不要将SmartRefreshLayout直接暴露给外面
    fun getWrappedView(): View = refreshLayout

    fun getRefreshHeader(): View? = refreshLayout.refreshHeader as View?

    fun setPageSize(pageSize: Int) {
        this.pageSize = pageSize
    }

    fun startRefresh() {
        refreshLayout.autoRefresh()
    }

    fun startLoadMore() {
        refreshLayout.autoLoadMore()
    }

    fun setRefreshEnable(enable: Boolean = true) {
        if (enable && refreshLayout.refreshHeader == null) {
            refreshLayout.setRefreshHeader(getDefaultRefreshHeader(refreshLayout.context), -1, -2)
        }
        refreshLayout.setEnableRefresh(enable)
    }

    fun setNoMoreData(noMoreData: Boolean) {
        refreshLayout.setNoMoreData(noMoreData)
    }

    fun setLoadMoreEnable(enable: Boolean = true) {
        if (enable && refreshLayout.refreshFooter == null) {
            refreshLayout.setRefreshFooter(getDefaultRefreshFooter(refreshLayout.context), -1, -2)
        }
        refreshLayout.setEnableLoadMore(enable)
    }

    fun setRefreshListener(onRefresh: () -> Unit): RefreshHelper {
        refreshLayout.setOnRefreshListener {
            isRefreshing = true
            onRefresh()
        }
        return this
    }

    fun setLoadMoreListener(loadMore: LoadMore): RefreshHelper {
        refreshLayout.setOnLoadMoreListener {
            isLoading = true
            loadMore.onLoadMore(pageIndex, pageSize)
        }
        return this
    }

    fun reset() {
        pageIndex = 1
        isRefreshing = false
        isLoading = false
        refreshLayout.closeHeaderOrFooter()
    }

    fun finishRefresh(success: Boolean = true) {
        if (success) {
            pageIndex = 2
        }
        refreshLayout.finishRefresh(success)
        isRefreshing = false
    }

    fun finishRefreshWithNoMoreData() {
        refreshLayout.finishLoadMoreWithNoMoreData()
        isRefreshing = false
    }

    fun finishLoadMore(success: Boolean = true) {
        if (success) {
            pageIndex++
        }
        refreshLayout.finishLoadMore(success)
        isLoading = false
    }

    fun finishLoadMoreWithNoMoreData() {
        refreshLayout.finishLoadMoreWithNoMoreData()
        isLoading = false
    }

    fun finishWithSuccess(success: Boolean = true) {
        if (isRefreshing) {
            finishRefresh(success)
            return
        }
        if (isLoading) {
            finishLoadMore(success)
            return
        }
    }

    fun finishWithNoMoreData() {
        if (isRefreshing) {
            finishRefreshWithNoMoreData()
            return
        }
        if (isLoading) {
            finishLoadMoreWithNoMoreData()
            return
        }
    }

    interface LoadMore {
        fun onLoadMore(pageIndex: Int, pageSize: Int)
    }

}