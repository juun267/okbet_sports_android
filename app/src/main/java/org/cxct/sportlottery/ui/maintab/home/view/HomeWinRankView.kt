package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.doOnDestory
import org.cxct.sportlottery.common.extentions.doOnPause
import org.cxct.sportlottery.common.extentions.doWhenLife
import org.cxct.sportlottery.databinding.ViewHomeWinRankBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.RecentDataManager
import org.cxct.sportlottery.util.RecentRecord
import org.cxct.sportlottery.util.setTextTypeFace
import splitties.systemservices.layoutInflater
import kotlin.random.Random

class HomeWinRankView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle), OnItemClickListener {

    val binding = ViewHomeWinRankBinding.inflate(layoutInflater,this)
    val pageSize = 5

    private var winsRequest: (() -> Unit)? = null
    private var betRequest: (() -> Unit)? = null

    private val gameRecordAdapter by lazy { HomeWinRankAdapter().apply { setOnItemClickListener(this@HomeWinRankView) } }
    private val httpBetDataList: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新投注
    private val httpWinsDataList: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新大奖
    private lateinit var fragment: BaseFragment<out MainHomeViewModel,*>

    init {
        orientation = VERTICAL
        initViews()
    }

    private var recordHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            var newItem: RecordNewEvent? = null
            if (binding.rbtnLb.isChecked) {
                if (httpBetDataList.isNotEmpty()) {
                    newItem = httpBetDataList.removeAt(0)
                }
            } else if (binding.rbtnLbw.isChecked) {
                 if (httpWinsDataList.isNotEmpty()) {
                    newItem = httpWinsDataList.removeAt(0)
                }
            }

            if (newItem != null) {
                reecordAdapterNotify(newItem)
            }

            postLoop()
        }
    }
    private var callApiHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (binding.rbtnLb.isChecked) {
                betRequest?.invoke()
            } else if (binding.rbtnLbw.isChecked) {
                winsRequest?.invoke()
            }
            postCallApiLoop()
        }
    }
    private fun postCallApiLoop() {
        callApiHandler.sendEmptyMessageDelayed(0, 60*1000)
    }

    private fun stopCallApiLoop() {
        callApiHandler.removeCallbacksAndMessages(null)
    }

    fun setUp(fragment: BaseFragment<out MainHomeViewModel,*>,  blockBetRequest: () -> Unit, blockWinsRequest: () -> Unit) {
        fragment.viewModel.recordBetHttp.observe(fragment) {
            if (!it.isNullOrEmpty()) {
                onNewHttpBetData(it.reversed())
            }
        }
        fragment.viewModel.recordWinHttp.observe(fragment) {
            if (!it.isNullOrEmpty()) {
                onNewHttpWinsData(it.reversed())
            }
        }
        this.fragment = fragment
        betRequest = blockBetRequest
        winsRequest = blockWinsRequest
        startLoopCall()
    }
    fun startLoopCall(){
        postLoop()
        postCallApiLoop()
        loadData()
    }
    fun stopLoopCall(){
        stopCallApiLoop()
        stopPostLoop()
    }

    fun loadData() {
        clearAllData()
        betRequest?.invoke()
        winsRequest?.invoke()
    }
    fun clearAllData(){
        httpBetDataList.clear()
        httpWinsDataList.clear()
        gameRecordAdapter.setList(listOf())
    }

    private fun postLoop() {
        recordHandler.sendEmptyMessageDelayed(3, (Random.nextLong(1000) + 400))
    }

    private fun stopPostLoop() {
        recordHandler.removeCallbacksAndMessages(null)
    }

    private fun initViews() = binding.run{
        // 暂时保持原样
//        rvOkgameRecord.itemAnimator = null
        rvOkgameRecord.setHasFixedSize(false)
        rvOkgameRecord.adapter = gameRecordAdapter
//        rvOkgameRecord.addItemDecoration(RCVDecoration()
//            .setDividerHeight(2f)
//            .setColor(rvOkgameRecord.context.getColor(R.color.color_EEF3FC))
//            .setMargin(10.dp.toFloat())
//        )

        rGroupRecord.setOnCheckedChangeListener { _, checkedId ->
            if (rbtnLb.id== checkedId){
                rbtnLb.setTextTypeFace(Typeface.BOLD)
                rbtnLbw.setTextTypeFace(Typeface.NORMAL)
            }else{
                rbtnLb.setTextTypeFace(Typeface.NORMAL)
                rbtnLbw.setTextTypeFace(Typeface.BOLD)
            }
            if (winsRequest == null || betRequest == null) {
                return@setOnCheckedChangeListener
            }

            if (checkedId == R.id.rbtn_lb) {
                if (httpBetDataList.isNullOrEmpty()) {
                    betRequest!!.invoke()
                }
                if (gameRecordAdapter.data.isNotEmpty()) {
                    gameRecordAdapter.setNewInstance(null)
                }

                return@setOnCheckedChangeListener
            }

            if (httpWinsDataList.isNullOrEmpty()) {
                winsRequest!!.invoke()
            }

            if (gameRecordAdapter.data.isNotEmpty()) {
                gameRecordAdapter.setNewInstance(null)
            }
        }
    }

    private fun resetData(oldDataList: MutableList<RecordNewEvent>, newDataList: MutableList<RecordNewEvent>) {
        oldDataList.clear()
        oldDataList.addAll(gameRecordAdapter.data)
        gameRecordAdapter.setList(newDataList)
    }

    private fun reecordAdapterNotify(it: RecordNewEvent) {
        if (gameRecordAdapter.data.size >= pageSize) {
            gameRecordAdapter.removeAt(gameRecordAdapter.data.size - 1)
        }
        gameRecordAdapter.addData(0, it)
    }

    fun onNewHttpWinsData(dataList: List<RecordNewEvent>) {
        httpWinsDataList.clear()
        httpWinsDataList.addAll(dataList)
    }

    fun onNewHttpBetData(dataList: List<RecordNewEvent>) {
        httpBetDataList.clear()
        httpBetDataList.addAll(dataList)
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        WinsDialog.newInstance(adapter.getItem(position) as RecordNewEvent) .show((context as AppCompatActivity).supportFragmentManager)
    }

}