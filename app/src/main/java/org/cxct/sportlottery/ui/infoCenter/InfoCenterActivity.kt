package org.cxct.sportlottery.ui.infoCenter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivityInfoCenterBinding
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.MsgType
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 消息中心
 */
class InfoCenterActivity : BaseActivity<InfoCenterViewModel, ActivityInfoCenterBinding>() {

    companion object {
        const val KEY_READ_PAGE = "key-read-page"
        const val BEEN_READ = 0
        const val YET_READ = 1

        fun startWith(context: Context, unRead: Boolean) {
            val intent = Intent(context, InfoCenterActivity::class.java)
            intent.putExtra(KEY_READ_PAGE, if (unRead) YET_READ else BEEN_READ)
            context.startActivity(intent)
        }
    }

    private val mDefaultShowPage by lazy { intent.getIntExtra(KEY_READ_PAGE, BEEN_READ) }

    private var currentPage = BEEN_READ //當前頁籤

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            binding.ivScrollToTop.apply {
                //置頂按鈕
                when (recyclerView.canScrollVertically(-1)) {
                    true -> {
                        visibility = View.VISIBLE
                        animate().alpha(1f).setDuration(1000).setListener(null)
                        when (currentPage == BEEN_READ) {
                            true -> viewModel.getUserMsgList(
                                false,
                                infoCenterAdapter.data.size,
                                InfoCenterViewModel.DataType.READ
                            )
                            false -> viewModel.getUserMsgList(
                                false,
                                infoCenterAdapter.data.size,
                                InfoCenterViewModel.DataType.UNREAD
                            )
                        }
                    }
                    false -> {
                        animate().alpha(0f).setDuration(1000)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    visibility = View.GONE
                                }
                            })
                    }
                }
            }
        }
    }

    val infoCenterAdapter by lazy {
        InfoCenterAdapter(this@InfoCenterActivity).apply {
            setOnItemClickListener { adapter, view, position ->

                val data = adapter.getItem(position) as InfoCenterData
                val detailDialog = InfoCenterDetailDialog()
                detailDialog.arguments = Bundle().apply { putParcelable("data", data) }
                detailDialog.show(supportFragmentManager, "")
                if (currentPage == YET_READ) {
                    markMessageReaded(data)
                }
            }
        }
    }

    private fun markMessageReaded(bean: InfoCenterData) {
        infoCenterAdapter.removeItem(bean)
        viewModel.setDataRead(bean)
        binding.customTabLayout.firstTabText = String.format(resources.getString(R.string.inbox), ++readedNum)
        unReadedNum = Math.max(0, unReadedNum - 1)
        binding.customTabLayout.secondTabText = String.format(resources.getString(R.string.unread_letters), unReadedNum)
    }

    private var mNowLoading: Boolean = false
    private var readedNum = 0
    private var unReadedNum = 0

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        viewModel.getMsgCount(MsgType.NOTICE_UNREAD)//未讀資料比數
        viewModel.getMsgCount(MsgType.NOTICE_READED)//已讀資料筆數
        initToolbar()
        initLiveData()
        initRecyclerView()
        initButton()
        initSelectTab()
    }

    private fun initToolbar()=binding.toolbar.run {
        tvToolbarTitle.setTitleLetterSpacing()
        tvToolbarTitle.text = getString(R.string.news_center)
        btnToolbarBack.setOnClickListener {
            finish()
        }
    }

    private fun initButton() {

        binding.ivScrollToTop.setOnClickListener {
            binding.rvData.smoothScrollToPosition(0)
        }

        binding.customTabLayout.setCustomTabSelectedListener { position ->
            when(position) {
                0 -> {
                    selectReadTab()
                }
                1 -> {
                    selectUnReadTab()
                }
            }
        }
    }

    private fun selectReadTab() {
        infoCenterAdapter.setList(mutableListOf())//清空資料
        viewModel.getMsgCount(MsgType.NOTICE_UNREAD)//未讀資料比數
        viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.READ)//已讀
        currentPage = BEEN_READ
        binding.ivScrollToTop.visibility = View.INVISIBLE
    }

    private fun selectUnReadTab() {
        infoCenterAdapter.setList(mutableListOf())//清空資料
        viewModel.getMsgCount(MsgType.NOTICE_READED)//已讀資料筆數
        viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.UNREAD)//未讀
        currentPage = YET_READ
        binding.ivScrollToTop.visibility = View.INVISIBLE
    }

    private fun initSelectTab()=binding.run {
        customTabLayout.firstTabText = String.format(resources.getString(R.string.inbox), 0)
        customTabLayout.secondTabText = String.format(resources.getString(R.string.unread_letters), 0)
        when (mDefaultShowPage) {
            BEEN_READ -> {
                if (customTabLayout.selectedTabPosition == BEEN_READ) {
                    selectReadTab()
                } else {
                    customTabLayout.selectTab(BEEN_READ)
                }
            }
            YET_READ -> {
                if (customTabLayout.selectedTabPosition == YET_READ) {
                    selectUnReadTab()
                } else {
                    customTabLayout.selectTab(YET_READ)
                }
            }
        }
    }

    private fun initLiveData() {

        viewModel.onMessageReaded.observe(this) { infoCenterAdapter.removeItem(it) }

        //已讀訊息清單
        viewModel.userReadMsgList.observe(this) {
            val userMsgList = it ?: return@observe
            if (currentPage == BEEN_READ) {
                if (InfoCenterRepository.isLoadMore) {
                    if (userMsgList.isNotEmpty()) {
                        infoCenterAdapter.addData(userMsgList)//上拉加載
                    }
                } else {
                    infoCenterAdapter.setList(userMsgList.toMutableList()) //重新載入
                }
            }
            viewModel.getResult()
        }
        //已讀總筆數
        viewModel.totalReadMsgCount.observe(this) {
            readedNum = it
            binding.customTabLayout.firstTabText = String.format(resources.getString(R.string.inbox), it)
        }
        //未讀訊息清單
        viewModel.userUnreadMsgList.observe(this) {
            val userMsgList = it ?: return@observe
            if (currentPage == YET_READ) {
                when(userMsgList.isNullOrEmpty()){
                    true ->{
                        infoCenterAdapter.setList(mutableListOf())
                    }
                    false ->{
                        infoCenterAdapter.setList(userMsgList.toMutableList()) //重新載入
                    }
                }
            }
            viewModel.getResult()
        }
        //未讀總筆數
        viewModel.totalUnreadMsgCount.observe(this) {
            unReadedNum = it
            binding.customTabLayout.secondTabText =
                String.format(resources.getString(R.string.unread_letters), it)
        }
        //Loading
        viewModel.isLoading.observe(this) {
            if (it) {
                mNowLoading = it
                loading()
            } else {
                mNowLoading = it
                hideLoading()
            }
        }
    }

    private fun initRecyclerView()=binding.rvData.run {
        layoutManager = LinearLayoutManager(this@InfoCenterActivity, LinearLayoutManager.VERTICAL, false)
        adapter = infoCenterAdapter
        addOnScrollListener(recyclerViewOnScrollListener)
    }


}