package org.cxct.sportlottery.ui.infoCenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivityInfoCenterBinding
import org.cxct.sportlottery.databinding.ViewNoRecordBinding
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.setTitleLetterSpacing
import timber.log.Timber

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

    private var currentTab = BEEN_READ
    private var currentPage = 1

    private val refreshHelper by lazy { RefreshHelper.of(binding.rvData, this, true) }


    private val infoCenterAdapter by lazy {
        InfoCenterAdapter().apply {
            setEmptyView(ViewNoRecordBinding.inflate(layoutInflater).root)
            setOnItemClickListener { adapter, view, position ->
                val data = adapter.getItem(position) as InfoCenterData
                val detailDialog = InfoCenterDetailDialog()
                detailDialog.arguments = Bundle().apply { putParcelable("data", data) }
                detailDialog.show(supportFragmentManager, "")
                if (currentTab == YET_READ) {
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
        initToolbar()
        initLiveData()
        initRecyclerView()
        initTab()
        viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.READ)//已讀
        viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.UNREAD)//已讀
    }

    private fun initToolbar()=binding.toolbar.run {
        tvToolbarTitle.setTitleLetterSpacing()
        tvToolbarTitle.text = getString(R.string.news_center)
        btnToolbarBack.setOnClickListener {
            finish()
        }
    }


    private fun selectReadTab() {
        infoCenterAdapter.setList(mutableListOf())//清空資料
        currentPage = 1
        currentTab = BEEN_READ
        viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.READ)//已讀
    }

    private fun selectUnReadTab() {
        infoCenterAdapter.setList(mutableListOf())//清空資料
        currentPage = 1
        currentTab = YET_READ
        viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.UNREAD)//未讀
    }

    private fun initTab()=binding.run {
        customTabLayout.firstTabText = String.format(resources.getString(R.string.inbox), 0)
        customTabLayout.secondTabText = String.format(resources.getString(R.string.unread_letters), 0)
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
        viewModel.userReadMsgResult.observe(this) {
            refreshHelper.finishRefresh()
            refreshHelper.finishLoadMore()
            Timber.d("currentTab="+currentTab+","+it.infoCenterData?.size)
            if (currentTab == BEEN_READ) {
                currentPage = it.page?:1
                if (it.page == 1){
                    infoCenterAdapter.setList(it.infoCenterData)
                }else{
                    infoCenterAdapter.addData(it.infoCenterData?: listOf())
                }
                refreshHelper.setLoadMoreEnable(infoCenterAdapter.itemCount<(it.total?:0))
            }
        }
        //已讀總筆數
        viewModel.totalReadMsgCount.observe(this) {
            readedNum = it
            binding.customTabLayout.firstTabText = String.format(resources.getString(R.string.inbox), it)
        }
        //未讀訊息清單
        viewModel.userUnReadMsgResult.observe(this) {
            refreshHelper.finishRefresh()
            refreshHelper.finishLoadMore()
            if (currentTab == YET_READ) {
                currentPage = it.page?:1
                if (it.page == 1){
                    infoCenterAdapter.setList(it.infoCenterData)
                }else{
                    infoCenterAdapter.addData(it.infoCenterData?: listOf())
                }
                refreshHelper.setLoadMoreEnable(infoCenterAdapter.itemCount<(it.total?:0))
            }
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
        refreshHelper.setRefreshListener {
            currentPage = 1
            when (currentTab == BEEN_READ) {
                true ->  viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.READ)//已讀
                false -> viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.UNREAD)//未讀
            }
        }
        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore{
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                when (currentTab == BEEN_READ) {
                    true -> viewModel.getUserMsgList(currentPage+1, InfoCenterViewModel.DataType.READ)
                    false -> viewModel.getUserMsgList(currentPage+1, InfoCenterViewModel.DataType.UNREAD)
                }
            }
        })
        layoutManager = LinearLayoutManager(this@InfoCenterActivity, LinearLayoutManager.VERTICAL, false)
        adapter = infoCenterAdapter
    }


}